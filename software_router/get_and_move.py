# -*- coding: utf-8 -*-

#
# * Copyright (c) 2009-2016. Authors: see NOTICE file.
# *
# * Licensed under the Apache License, Version 2.0 (the "License");
# * you may not use this file except in compliance with the License.
# * You may obtain a copy of the License at
# *
# *      http://www.apache.org/licenses/LICENSE-2.0
# *
# * Unless required by applicable law or agreed to in writing, software
# * distributed under the License is distributed on an "AS IS" BASIS,
# * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# * See the License for the specific language governing permissions and
# * limitations under the License.
# */


__author__ = "Vessiere Thomas <vessiere.thomas@hotmail.com>"
__copyright__ = "Copyright 2010-2017 University of Liège, Belgium, http://www.cytomine.be/"

from argparse import ArgumentParser
from sldc import Loggable, Logger, StandardOutputLogger
from cytomine import Cytomine
from cytomine_utilities import CytomineJob
from cytomine import models
import SimpleITK as sitk
import numpy as np
import shutil
import os
import cv2


class SimpleElastixJob(CytomineJob, Loggable):
    # set parameters for your algo, 4 first params must be like this #
    print("Before init")

    def __init__(self, cytomine, software_id, project_id, job_parameters,
                 fix_image_id, moving_image_id, nb_spatial_sample, nb_iterations, storage_id,
                 annotation_fix_id, annotation_moving_id, working_path, cytomine_host, cytomine_upload, pk, prk,
                 export_overlay_images,number_of_resolutions,result_file_name):
        print("After init")
        # call init from parent classes #
        print("project id = " + str(project_id))
        if (cytomine == None):
            print "Cytomine obj null"
        print("soft_id =" + str(software_id))
        CytomineJob.__init__(self, cytomine, software_id, project_id, parameters=None)
        Loggable.__init__(self, logger=StandardOutputLogger(Logger.INFO))

        # keep var from parameters #
        self._fix_image_id = fix_image_id
        self._moving_image_id = moving_image_id
        self._nb_spatial_sample = nb_spatial_sample
        self._nb_iterations = nb_iterations
        self._storage_id = storage_id
        self._id_annotation_fix = annotation_fix_id
        self._id_annotation_moving = annotation_moving_id
        self._working_path = working_path
        self._cytomine_upload = cytomine_upload
        self._cytomine_host = cytomine_host
        self._cytomine = cytomine
        self._project_id = project_id
        self._pk = pk
        self._prk = prk
        self._overlayed_images = export_overlay_images
        self._job_parameters = job_parameters
        self._number_of_resolutions = number_of_resolutions
        self._result_file_name = result_file_name
    # run method, the logic of your algorithm is here #
    def run(self):

        print "Start Running..."
        path_job = os.path.join(self._working_path,str(self.job.id))
        print "path job = " + path_job
        os.mkdir(path_job)
        if self._id_annotation_fix == "" and self._id_annotation_fix == "":
            # dump images #
            # fix #
            fix_image_instance = self._cytomine.get_image_instance(long(self._fix_image_id))
            fix_collection = models.ImageInstanceCollection()
            fix_collection.data().append(fix_image_instance)
            self._cytomine.dump_project_images(image_instances=fix_collection, id_project=long(self._project_id),
                                               dest_path=os.path.join(str(self.job.id),"images/"),
                                               override=False, max_size=5000)

            # moving #
            moving_image_instance = self._cytomine.get_image_instance(int(self._moving_image_id))
            moving_collection = models.ImageInstanceCollection()
            moving_collection.data().append(moving_image_instance)
            self._cytomine.dump_project_images(image_instances=moving_collection, id_project=long(self._project_id),
                                               dest_path= os.path.join(str(self.job.id),"images/"),
                                               override=False, max_size=5000)
            # format paths #
            path_to_fix_image = os.path.join(self._working_path,str(self.job.id), "images", str(self._project_id),
                                             str(self._fix_image_id) + ".jpg")
            path_to_moving_image = os.path.join(self._working_path,str(self.job.id), "images", str(self._project_id),
                                                str(self._moving_image_id) + ".jpg")

            # debug #
            print "path_to_fix_image : " + str(path_to_fix_image)
            print "path_to_moving_image : " + str(path_to_moving_image)

        else:
            # dump annotations#
            annotation_fix = self._cytomine.get_annotation(long(self._id_annotation_fix))
            collection_fix = models.AnnotationCollection()
            collection_fix.data().append(annotation_fix)
            #os.makedirs(os.path.join(self._working_path,str(self.job.id),"images", "annotation_fix/"))
            print len(collection_fix)
            self._cytomine.dump_annotations \
                    (
                    annotations=collection_fix,
                    get_image_url_func=models.Annotation.get_annotation_crop_url,
                    dest_path=os.path.join(self._working_path,str(self.job.id),"images", "annotation_fix/"),
                    desired_zoom=0
                )
            print "dest path dump " + os.path.join(str(self.job.id), "images", "annotation_fix")
            annotation_moving = self._cytomine.get_annotation(long(self._id_annotation_moving))
            collection_moving = models.AnnotationCollection()
            collection_moving.data().append(annotation_moving)
            #os.makedirs(os.path.join(self._working_path,str(self.job.id), "images", "annotation_moving/"))
            print len(collection_moving)
            self._cytomine.dump_annotations \
                    (
                    annotations=collection_moving,
                    get_image_url_func=models.Annotation.get_annotation_crop_url,
                    dest_path=os.path.join(self._working_path,str(self.job.id),"images", "annotation_moving/"),
                    desired_zoom=0
                )
            print "dest path dump " + os.path.join(str(self.job.id),"images", "annotation_moving")
            # get id_term for path #
            id_term = annotation_fix.term[0]

            # because the name of the file is vague, just list the file and get the elem at 0 #
            list_fix = os.listdir(os.path.join(self._working_path,str(self.job.id), "images", "annotation_fix",str(id_term)))
            list_moving = os.listdir(os.path.join(self._working_path,str(self.job.id), "images", "annotation_moving",str(id_term)))

            # format paths #
            path_to_fix_image = os.path.join(self._working_path,str(self.job.id), "images", "annotation_fix",
                                             str(id_term), str(list_fix[0]))
            path_to_moving_image = os.path.join(self._working_path,str(self.job.id), "images", "annotation_moving",
                                                str(id_term), str(list_moving[0]))

            # debug #
            print "path_to_fix_image : " + str(path_to_fix_image)
            print "path_to_moving_image : " + str(path_to_moving_image)

        # load images #
        fix_image_grey = sitk.ReadImage(path_to_fix_image,sitk.sitkFloat32)
        moving_image_grey = sitk.ReadImage(path_to_moving_image, sitk.sitkFloat32)

        # open img color with CV #
        cv_moving_image = cv2.imread(path_to_moving_image)
        cv_fix_image = cv2.imread(path_to_fix_image)

        # start processing algorithm #
        # got all the channel for keep orignal color #
        itk_mov_image_color_0 = sitk.GetImageFromArray(cv_moving_image[:, :, 0])
        itk_mov_image_color_1 = sitk.GetImageFromArray(cv_moving_image[:, :, 1])
        itk_mov_image_color_2 = sitk.GetImageFromArray(cv_moving_image[:, :, 2])

        # set ParamtersMap to sitk for compute transformation #
        simple_elastix = sitk.SimpleElastix()
        simple_elastix.SetFixedImage(fix_image_grey)
        simple_elastix.SetMovingImage(moving_image_grey)
        parameter_map_translation = sitk.GetDefaultParameterMap("translation")
        parameter_map_affine = sitk.GetDefaultParameterMap("affine")

        # translation & affine #
        simple_elastix.SetParameterMap(parameter_map_translation)
        simple_elastix.AddParameterMap(parameter_map_affine)

        # params set by user #
        simple_elastix.SetParameter("MaximumNumberOfIterations", str(self._nb_iterations))
        simple_elastix.SetParameter("NumberOfSpatialSamples", str(self._nb_spatial_sample))
        simple_elastix.SetParameter("NumberOfResolutions", str(self._number_of_resolutions))
        # start computing #
        simple_elastix.Execute()

        # get parameters of the transform for apply it on 3 channels #
        transform_map = simple_elastix.GetTransformParameterMap()
        # for set shape of images #
        np_img = sitk.GetArrayFromImage(simple_elastix.GetResultImage())

        # set parameterMap & complete properties_map #
        properties_map = {}
        transform_x = sitk.SimpleTransformix()
        transform_x.SetTransformParameterMap(transform_map)

        # translation map #
        # IMPORTANT FOR UNDERSTAND #
        # those dicts are specific here, i added a tuple with a boolean for know if i need to get one or all element
        # in the tuple returned by sitk, after that i give to the function "MakeUpProperties" the dict, mode(number
        # of parameter map (e.g : 0 || 1 etc)) the properties map to link at the image and the transform_x object.
        # Basically if the boolean is True, got all the parameters of the tuple
        # and format it for got a single Key/value pair like direction_0 = x, direction_1 = y. If the boolean is
        # False, only get the first element #

        dict_translation = \
            {
                "DefaultPixelValue": ("sitk_translation_default_pixel_value", False),  # int #
                "Direction": ("sitk_translation_direction", True),  # tuple with 4 ints #
                "FinalBSplineInterpolationOrder": ("sitk_translation_final_bspline_interpolation_order", False),
            # int #
                "FixedImageDimension": ("sitk_translation_fixed_image_dimension", False),  # int #
                "FixedInternalImagePixelType": ("sitk_translation_fixed_internal_image_pixel_type", False),  # string #
                "HowToCombineTransforms": ("sitk_translation_how_combine_transforms", False),  # string #
                "Index": ("sitk_translation_index", True),  # tuple with 2 int #
                "InitialTransformParametersFileName": (
                "sitk_translation_initial_transform_parameters_file_name", False),
                "MovingImageDimension": ("sitk_translation_moving_image_dimension", False),
                "MovingInternalImagePixelType": ("sitk_translation_moving_internal_image_pixel_type", False),
                "NumberOfParameters": ("sitk_translation_number_of_parameters", False),
                "Origin": ("sitk_translation_origin", True),
                "ResampleInterpolator": ("sitk_translation_resample_interpolator", False),
                "Resampler": ("sitk_translation_resampler", False),
                "Spacing": ("sitk_translation_spacing", True),
                "Transform": ("sitk_translation_transform", False),
                "TransformParameters": ("sitk_translation_transform_parameters", True),
                "UseDirectionCosines": ("sitk_translation_use_directions_Cosines", False),
            }

        MakeUpProperties(dict_translation, 0, properties_map, transform_x)

        dict_affine = \
            {
                "CenterOfRotationPoint": ("sitk_affine_centrer_of_rotation", True),
                "DefaultPixelValue": ("sitk_affine_default_pixel_value", False),
                "Direction": ("sitk_affine_direction", True),
                "FinalBSplineInterpolationOrder": ("sitk_affine_final_bspline_interpolation_order", False),
                "FixedImageDimension": ("sitk_affine_fixed_image_dimension", False),
                "FixedInternalImagePixelType": ("sitk_affine_fixed_internal_image_pixel_type", False),
                "HowToCombineTransforms": ("sitk_affine_how_combine_transforms", False),
                "Index": ("sitk_affine_index", True),
                "InitialTransformParametersFileName": ("sitk_affine_initial_transform_parameters_file_name", False),
                "MovingImageDimension": ("sitk_affine_moving_image_dimension", False),
                "MovingInternalImagePixelType": ("sitk_affine_moving_internal_image_pixel_type", False),
                "NumberOfParameters": ("sitk_affine_number_of_parameters", False),
                "Origin": ("sitk_affine_origin", True),
                "ResampleInterpolator": ("sitk_affine_resample_interpolator", False),
                "Resampler": ("sitk_affine_resampler", False),
                "Spacing": ("sitk_affine_spacing", True),
                "Transform": ("sitk_affine_transform", False),
                "TransformParameters": ("sitk_affine_transform_parameters", True),
                "UseDirectionCosines": ("sitk_affine_use_directions_Cosines", False),
            }

        MakeUpProperties(dict_affine, 1, properties_map, transform_x)

        # apply transforms on all channels #
        transform_x.SetMovingImage(itk_mov_image_color_0)
        img_to_save_0 = transform_x.Execute()
        transform_x.SetMovingImage(itk_mov_image_color_1)
        img_to_save_1 = transform_x.Execute()
        transform_x.SetMovingImage(itk_mov_image_color_2)
        img_to_save_2 = transform_x.Execute()

        # format image color #
        img_color_final = np.zeros((np_img.shape[0], np_img.shape[1], 3))

        img_color_final[:, :, 0] = sitk.GetArrayFromImage(img_to_save_0)
        img_color_final[:, :, 1] = sitk.GetArrayFromImage(img_to_save_1)
        img_color_final[:, :, 2] = sitk.GetArrayFromImage(img_to_save_2)

        # save images #
        img_transform_to_save_path = os.path.join(self._working_path, str(self.job.id), "images" , str(self._result_file_name))

        if (self._overlayed_images == "true"):
            img_overlay_to_save_path = os.path.join(self._working_path, str(self.job.id),"images",
                                                    "overlayed_images.png")
            cv2.imwrite(img_transform_to_save_path, img_color_final)
            cv2.imwrite(img_overlay_to_save_path, img_color_final + (0.80 * cv_fix_image))

            # connection to demo-upload & upload #
            demo_upload = Cytomine(self._cytomine_upload, self._pk, self._prk, verbose=True)
            demo_upload.upload_image(img_transform_to_save_path, self._project_id, self._storage_id,
                                     str(self._cytomine_host), properties=properties_map)
            demo_upload.upload_image(img_overlay_to_save_path, self._project_id, self._storage_id,
                                     str(self._cytomine_host), properties=None)

        else:
            cv2.imwrite(img_transform_to_save_path, img_color_final)

            # connection to demo-upload & upload #
            demo_upload = Cytomine(self._cytomine_upload, self._pk, self._prk, verbose=True)
            demo_upload.upload_image(img_transform_to_save_path, self._project_id, self._storage_id,
                                     str(self._cytomine_host), properties=properties_map)

        # remove the directory of the current job #
        shutil.rmtree(os.path.join(self._working_path, str(self.job.id)), ignore_errors=True)
        self.done(True)

def main(argv):
    # parsing arguments #
    print("In Parser")
    parser = ArgumentParser(prog="get_and_move.py", description="workflow with simple elastix")

    parser.add_argument('--cytomine_host', dest="cytomine_host", default='demo.cytomine.be')
    parser.add_argument('--cytomine_public_key', dest="cytomine_public_key")
    parser.add_argument('--cytomine_private_key', dest="cytomine_private_key")
    parser.add_argument('--cytomine_id_software', dest="cytomine_id_software")
    parser.add_argument("--cytomine_id_project", dest="cytomine_id_project")
    parser.add_argument('--fix_image_id', dest="id_fix_image")
    parser.add_argument("--mov_image_id,", dest="id_mov_image")
    parser.add_argument('--nb_iterations', dest="nb_iterations")
    parser.add_argument("--nb_spatialsampels", dest="nb_spatialsampels")
    parser.add_argument("--cytomine_storage_id", dest="storage_id")
    parser.add_argument("--annotation_fix_id", dest="annotation_fix_id")
    parser.add_argument("--annotation_moving_id", dest="annotation_moving_id")
    parser.add_argument("--cytomine_working_path", dest="working_path")
    parser.add_argument("--cytomine_upload", dest="cytomine_upload")
    parser.add_argument("--export_overlay_images", dest="export_overlay_images")
    parser.add_argument("--number_of_resolution", dest="number_of_resolution")
    parser.add_argument("--result_file_name", dest="result_file_name")
    arguments, others = parser.parse_known_args(argv)

    # connection to demo #
    print("Connect to demo")
    print("Argument host : " + arguments.cytomine_host)
    print("Pk = " + arguments.cytomine_public_key)
    print("Prk = " + arguments.cytomine_private_key)
    cytomine = Cytomine(
        arguments.cytomine_host,
        arguments.cytomine_public_key,
        arguments.cytomine_private_key,
        working_path=arguments.working_path  # = software_router/algo/simple_elastix #
    )
    # instance SimpleElastixJob object and run the logic of the algorithm #
    print ("In with")
    print arguments.__dict__
    with SimpleElastixJob(
            cytomine, arguments.cytomine_id_software, arguments.cytomine_id_project, arguments.__dict__,
            arguments.id_fix_image, arguments.id_mov_image, arguments.nb_spatialsampels, arguments.nb_iterations,
            arguments.storage_id, arguments.annotation_fix_id, arguments.annotation_moving_id, arguments.working_path,
            arguments.cytomine_host, arguments.cytomine_upload, arguments.cytomine_public_key,
            arguments.cytomine_private_key,
            arguments.export_overlay_images,
            arguments.number_of_resolution,
            arguments.result_file_name
    ) as context:
        context.run()


# function for make-up properties map, see above for more information #
def MakeUpProperties(dictionary, mode, properties_map, transform_x):
    for param_name, prop_name in dictionary.items():
        if not prop_name[1]:
            properties_map[prop_name[0]] = transform_x.GetTransformParameter(mode, param_name)[0]
        else:
            for i, v in enumerate(transform_x.GetTransformParameter(mode, param_name)):
                properties_map["{}_{}".format(prop_name[0], i)] = v
    print properties_map


# call main #
if __name__ == "__main__":
    import sys
    main(sys.argv[1:])
