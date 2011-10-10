package be.cytomine.api

import be.cytomine.project.Project
import be.cytomine.processing.ImageFilterProject
import be.cytomine.processing.ImageFilter

class RestImageFilterProjectController extends RestController {

    def list = {
        def imagesFiltersProject = ImageFilterProject.findAllByProject(Project.read(params.idProject))
        if(imagesFiltersProject!=null) responseSuccess(imagesFiltersProject.collect { it.imageFilter })
        else responseNotFound("ImageFilter","ImageFilter",params.id)
    }

    def add = {
        Project project = Project.read(request.JSON.idProject)
        ImageFilter imageFilter = ImageFilter.read(request.JSON.idImageFilter)
        ImageFilterProject.link(imageFilter,project)
        responseSuccess([])
    }

    def delete = {
        Project project = Project.read(params.idProject)
        ImageFilter imageFilter = ImageFilter.read(params.idImageFilter)
        ImageFilterProject.unlink(imageFilter,project)
        responseSuccess([])
    }

}
