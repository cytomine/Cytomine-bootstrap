package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.project.Project
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * An image filter can be link to many projects
 */
class ImageFilterProject extends CytomineDomain implements Serializable{

    ImageFilter imageFilter
    Project project

    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static ImageFilterProject insertDataIntoDomain(def json, def domain = new ImageFilterProject()) {
        try {
            domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
            domain.imageFilter = JSONUtils.getJSONAttrDomain(json, "imageFilter", new ImageFilter(), true)
            domain.project = JSONUtils.getJSONAttrDomain(json, "project", new Project(), true)
        }
        catch (Exception e) {
            domain.imageFilter = JSONUtils.getJSONAttrDomain(json.imageFilter, "id", new ImageFilter(), true)
            domain.project = JSONUtils.getJSONAttrDomain(json.project, "id", new Project(), true)
        }
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + ImageFilterProject.class)
        JSON.registerObjectMarshaller(ImageFilterProject) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['imageFilter'] = it.imageFilter?.id
            returnArray['processingServer'] = it.imageFilter?.processingServer
            returnArray['baseUrl'] = it.imageFilter?.baseUrl
            returnArray['name'] = it.imageFilter?.name
            returnArray['project'] = it.project?.id
            return returnArray
        }
    }

    /**
     * Return domain project (annotation project, image project...)
     * By default, a domain has no project.
     * You need to override projectDomain() in domain class
     * @return Domain project
     */
    public Project projectDomain() {
        return project;
    }
}

