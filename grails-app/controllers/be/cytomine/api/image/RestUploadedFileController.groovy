package be.cytomine.api.image


import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.Mime
import be.cytomine.image.UploadedFile
import be.cytomine.image.server.Storage
import be.cytomine.laboratory.Sample
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiResponseObject
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.annotation.RestApiResponseObject
import org.restapidoc.pojo.RestApiParamType

import javax.activation.MimetypesFileTypeMap

/**
 * Controller that handle request on file uploading (when a file is uploaded, list uploaded files...)
 */
@RestApi(name = "uploaded file services", description = "Methods for managing an uploaded image file.")
class RestUploadedFileController extends RestController {

    def imageProcessingService
    def cytomineService
    def imagePropertiesService
    def projectService
    def storageService
    def grailsApplication
    def uploadedFileService
    def storageAbstractImageService
    def imageInstanceService
    def abstractImageService
    def notificationService
    def securityACLService
    def secUserService

    static allowedMethods = [image: 'POST']

    @RestApiMethod(description="Get all uploaded file made by the current user")
    def list() {
        //get all uploaded file for this user
        def uploadedFiles = uploadedFileService.list((User)cytomineService.getCurrentUser())

        //if view is datatables, change way to store data
        if (params.dataTables) {
            uploadedFiles = ["aaData" : uploadedFiles]
        }
        responseSuccess(uploadedFiles)
    }

    @RestApiMethod(description="Delete all file properties for an image")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image id")
    ])
    @RestApiResponseObject(objectIdentifier = "empty")
    def clearProperties () {
        AbstractImage abstractImage = abstractImageService.read(params.long('id'))
        imagePropertiesService.clear(abstractImage)
        responseSuccess([:])
    }

    @RestApiMethod(description="Get all file properties for an image")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image id")
    ])
    @RestApiResponseObject(objectIdentifier = "empty")
    def populateProperties () {
        AbstractImage abstractImage = abstractImageService.read(params.long('id'))
        imagePropertiesService.populate(abstractImage)
        responseSuccess([:])
    }

    @RestApiMethod(description="Fill image field (magn, width,...) with all file properties")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image id")
    ])
    @RestApiResponseObject(objectIdentifier = "empty")
    def extractProperties () {
        AbstractImage abstractImage = abstractImageService.read(params.long('id'))
        imagePropertiesService.extractUseful(abstractImage)
        responseSuccess([:])
    }


    @RestApiMethod(description="Get an uploaded file")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The uploaded file id")
    ])
    def show () {
        UploadedFile up = uploadedFileService.read(params.long('id'))
        if (up) {
            responseSuccess(up)
        } else {
            responseNotFound("UploadedFile", params.id)
        }
    }

    /**
     * Add a new image
     * TODO:: how to manage security here?
     *
     */
    @RestApiMethod(description="Add a new uploaded file. This DOES NOT upload the file, just create the domain.")
    def add () {
        add(uploadedFileService, request.JSON)
    }

    /**
     * Update a new image
     * TODO:: how to manage security here?
     */
    @RestApiMethod(description="Edit an uploaded file domain (usefull to edit its status during upload)")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The uploaded file id")
    ])
    def update () {
        update(uploadedFileService, request.JSON)
    }

    /**
     * Delete a new image
     * TODO:: how to manage security here?
     */
    @RestApiMethod(description="Delete an uploaded file domain. This do not delete the file on disk.")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The uploaded file id")
    ])
    def delete () {
        delete(uploadedFileService, JSON.parse("{id : $params.id}"),null)
    }

    @RestApiMethod(description="Get the uploaded file of a given Abstract image")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The uploaded file id")
    ])
    def getByAbstractImage () {
        AbstractImage im = abstractImageService.read(params.long('idimage'))
        UploadedFile up = UploadedFile.findByImage(im);
        if (up) {
            responseSuccess(up)
        } else {
            responseNotFound("UploadedFile", params.id)
        }
    }

    def upRedirect () {
        redirect(url: "http://localhost:9090/upload")
    }

    @RestApiMethod(description="Create an image thanks to an uploaded file domain. THis add the image in the good storage and the project (if needed). This send too an email at the end to the uploader and the project admins.")
    @RestApiParams(params=[
    @RestApiParam(name="uploadedFile", type="long", paramType = RestApiParamType.PATH,description = "The uploaded file id")
    ])
    @RestApiResponseObject(objectIdentifier = "[abstractimage.|abstract image|]")
    def createImage () {
        long timestamp = new Date().getTime()
        def currentUser = cytomineService.currentUser
        securityACLService.checkUser(currentUser)
        UploadedFile uploadedFile = UploadedFile.read(params.long('uploadedFile'))
        Collection<Storage> storages = []
        uploadedFile.getStorages()?.each {
            storages << storageService.read(it)
        }

        Sample sample = new Sample(name : timestamp.toString() + "-" + uploadedFile.getOriginalFilename())

        def projects = []
        //create domains instance
        def mimeType = uploadedFile.getMimeType()
        def ext = uploadedFile.getExt()
        Mime mime = Mime.findByMimeType(mimeType)
        if (!mime) {
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            mimeType = mimeTypesMap.getContentType(uploadedFile.getAbsolutePath())
            mime = new Mime(extension: ext, mimeType : mimeType)
            mime.save(failOnError: true)
        }
        log.info "#################################################################"
        log.info "#################################################################"
        log.info "##############CREATE IMAGE#################"
        log.info "#################################################################"
        log.info "#################################################################"
        AbstractImage abstractImage = new AbstractImage(
                filename: uploadedFile.getFilename(),
                originalFilename:  uploadedFile.getOriginalFilename(),
                scanner: null,
                sample: sample,
                path: uploadedFile.getFilename(),
                mime: mime)

        if (sample.validate() && abstractImage.validate()) {
            sample.save(flush : true,failOnError: true)
            sample.refresh()
            abstractImage.setSample(sample)
            abstractImage.save(flush: true,failOnError: true)

            storages.each { storage ->
                storageAbstractImageService.add(JSON.parse(JSONUtils.toJSONString([storage : storage.id, abstractimage : abstractImage.id])))
            }

            uploadedFile.image = abstractImage
            uploadedFile.save(flush:true,failOnError: true)

            imagePropertiesService.clear(abstractImage)
            imagePropertiesService.populate(abstractImage)
            imagePropertiesService.extractUseful(abstractImage)
            abstractImage.save(flush: true,failOnError: true)


            log.info "Map image ${abstractImage.id} to uploaded file ${uploadedFile.id}"
            uploadedFile.image = abstractImage
            uploadedFile.save(flush:true,failOnError: true)
            log.info "Image = ${uploadedFile.image?.id}"

            uploadedFile.getProjects()?.each { project_id ->
                Project project = projectService.read(project_id)
                projects << project
                ImageInstance imageInstance = new ImageInstance( baseImage : abstractImage, project:  project, user :currentUser)
                imageInstanceService.add(JSON.parse(imageInstance.encodeAsJSON()))
            }



        } else {
            sample.errors?.each {
                log.info "Sample error : " + it
            }
            abstractImage.errors?.each {
                log.info "Sample error : " + it
            }
        }
        //notificationService.notifyNewImageAvailable(currentUser,abstractImage,projects)
        responseSuccess([abstractimage: abstractImage])
    }




}
