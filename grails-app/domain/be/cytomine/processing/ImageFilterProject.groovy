package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import grails.converters.JSON
import org.apache.log4j.Logger

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
        } else {
            Logger.getLogger(this).info("no link between " + imageFilter + " " + project)
        }
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static ImageFilterProject createFromDataWithId(def json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static ImageFilterProject createFromData(def json) {
        def imageFilterProject = new ImageFilterProject()
        insertDataIntoDomain(imageFilterProject, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static ImageFilterProject insertDataIntoDomain(def domain, def json) {
        try {
            domain.imageFilter = ImageFilter.get(json.imageFilter.id)
            domain.project = Project.get(json.project.id)
        }
        catch (Exception e) {
            domain.imageFilter = ImageFilter.get(json.imageFilter)
            domain.project = Project.get(json.project)
        }
        if (!domain.imageFilter) throw new WrongArgumentException("Software ${json.imageFilter.toString()} doesn't exist!")
        if (!domain.project) throw new WrongArgumentException("Project ${json.project.toString()} doesn't exist!")
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + ImageFilterProject.class)
        JSON.registerObjectMarshaller(ImageFilterProject) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['imageFilter'] = it.imageFilter?.id
            returnArray['processingServer'] = it.imageFilter?.processingServer?.url
            returnArray['baseUrl'] = it.imageFilter?.baseUrl
            returnArray['name'] = it.imageFilter?.name
            returnArray['project'] = it.project?.id
            return returnArray
        }
    }
}

