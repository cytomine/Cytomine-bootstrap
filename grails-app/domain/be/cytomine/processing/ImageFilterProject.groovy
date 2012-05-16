package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import grails.converters.JSON

class ImageFilterProject extends CytomineDomain implements Serializable{

    ImageFilter imageFilter
    Project project

    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    static ImageFilterProject link(def id , ImageFilter imageFilter, Project project) {
        def imageFilterProject = ImageFilterProject.findByImageFilterAndProject(imageFilter, project)
        if (!imageFilterProject) {
            imageFilterProject = new ImageFilterProject()
            imageFilterProject.id = id
            imageFilterProject.imageFilter = imageFilter
            imageFilterProject.project = project
            imageFilter?.addToImageFilterProjects(imageFilterProject)
            project?.addToImageFilterProjects(imageFilterProject)
            project.refresh()
            imageFilter.refresh()
            imageFilterProject.save(flush: true)
        } else throw new AlreadyExistException("Image Filter " + imageFilter?.name + " already map with project " + project?.name)
        imageFilterProject
    }

    static ImageFilterProject link(ImageFilter imageFilter, Project project) {
        link(null, imageFilter, project)
    }

    static void unlink(ImageFilter imageFilter, Project project) {
        def imageFilterProject = ImageFilterProject.findByImageFilterAndProject(imageFilter, project)
        if (imageFilterProject) {
            imageFilter?.removeFromImageFilterProjects(imageFilterProject)
            project?.removeFromImageFilterProjects(imageFilterProject)
            imageFilter.refresh()
            project.refresh()
            imageFilterProject.delete(flush: true)
        } else {println "no link between " + imageFilter + " " + project}
    }

    static ImageFilterProject createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static ImageFilterProject createFromData(jsonSoftwareParameter) {
        def imageFilterProject = new ImageFilterProject()
        getFromData(imageFilterProject, jsonSoftwareParameter)
    }

    static ImageFilterProject getFromData(ImageFilterProject imageFilterProject, jsonSoftwareParameter) {
        println "jsonSoftwareParameter=" + jsonSoftwareParameter.toString()
        try {
            imageFilterProject.imageFilter = ImageFilter.get(jsonSoftwareParameter.imageFilter.id)
            imageFilterProject.project = Project.get(jsonSoftwareParameter.project.id)
        }
        catch (Exception e) {
            imageFilterProject.imageFilter = ImageFilter.get(jsonSoftwareParameter.imageFilter)
            imageFilterProject.project = Project.get(jsonSoftwareParameter.project)
        }
        if (!imageFilterProject.imageFilter) throw new WrongArgumentException("Software ${jsonSoftwareParameter.imageFilter.toString()} doesn't exist!")
        if (!imageFilterProject.project) throw new WrongArgumentException("Project ${jsonSoftwareParameter.project.toString()} doesn't exist!")
        return imageFilterProject;
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + ImageFilterProject.class
        JSON.registerObjectMarshaller(ImageFilterProject) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['imageFilter'] = it.imageFilter?.id
            returnArray['baseUrl'] = it.imageFilter?.baseUrl
            returnArray['name'] = it.imageFilter?.name
            returnArray['project'] = it.project?.id
            return returnArray
        }
    }
}

