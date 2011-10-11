package be.cytomine.api

import be.cytomine.project.Project
import be.cytomine.processing.ImageFilterProject
import be.cytomine.processing.ImageFilter

class RestImageFilterProjectController extends RestController {

    def list = {
        def imagesFiltersProject = ImageFilterProject.findAllByProject(Project.read(params.project))
        if(imagesFiltersProject!=null) responseSuccess(imagesFiltersProject.collect { it.imageFilter })
        else responseNotFound("ImageFilter","ImageFilter",params.project)
    }

    def add = {
        Project project = Project.read(request.JSON.project)
        ImageFilter imageFilter = ImageFilter.read(request.JSON.imageFilter)
        ImageFilterProject.link(imageFilter,project)
        responseSuccess(ImageFilterProject.findByImageFilterAndProject(imageFilter, project).imageFilter)
    }

    def delete = {
        Project project = Project.read(params.project)
        ImageFilter imageFilter = ImageFilter.read(params.imageFilter)
        ImageFilterProject.unlink(imageFilter,project)
        responseSuccess([])
    }

}
