package be.cytomine.api

import be.cytomine.project.Project
import be.cytomine.processing.ImageFilterProject
import be.cytomine.processing.ImageFilter

class RestImageFilterProjectController extends RestController {

    def imageFilterService
    def imageFilterProjectService
    def projectService


    def list = {
        def imagesFiltersProject = imageFilterProjectService.list(Project.read(params.project))
        if(imagesFiltersProject) responseSuccess(imagesFiltersProject.collect { it.imageFilter })
        else responseNotFound("ImageFilter","ImageFilter",params.project)
    }

    def add = {
        Project project = projectService.read(request.JSON.project)
        ImageFilter imageFilter = imageFilterService.read(request.JSON.imageFilter)
        imageFilterProjectService.add(project,imageFilter)
        responseSuccess(imageFilterProjectService.get(project,imageFilter).imageFilter)
    }

    def delete = {
        Project project = projectService.read(params.project)
        ImageFilter imageFilter = imageFilterService.read(params.imageFilter)
        imageFilterProjectService.delete(project,imageFilter)
        responseSuccess([])
    }

}
