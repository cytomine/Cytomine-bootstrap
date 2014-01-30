package be.cytomine.image.multidim

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.project.Project
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 8:33
 * A group of image with diff dimension
 */
@ApiObject(name = "image group", description = "A group of image from the same source with different dimension")
class ImageGroup extends CytomineDomain implements Serializable {

    @ApiObjectFieldLight(description = "The name of the project")
    String name

    @ApiObjectFieldLight(description = "The image group project")
    Project project

    static constraints = {
    }

    static mapping = {
        id generator: "assigned"
        sort "id"
    }

    def beforeValidate() {
        super.beforeValidate()
        if (!name) {
            name = id
        }
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        ImageGroup.withNewSession {
            ImageGroup imageAlreadyExist = ImageGroup.findByNameAndProject(name,project)
            if (imageAlreadyExist != null && (imageAlreadyExist.id != id)) {
                throw new AlreadyExistException("ImageGroup with name=" + name + " and project=" + project + "  already exists")
            }
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static ImageGroup insertDataIntoDomain(def json, def domain = new ImageGroup()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.created = JSONUtils.getJSONAttrDate(json, "created")
        domain.updated = JSONUtils.getJSONAttrDate(json, "updated")
        domain.project = JSONUtils.getJSONAttrDomain(json, "project", new Project(), true)
        domain.name = domain.id
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + ImageGroup.class)
        JSON.registerObjectMarshaller(ImageGroup) {
            getDataFromDomain(it)
        }
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        returnArray['project'] = domain?.project?.id
        return returnArray
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return project.container();
    }
}
