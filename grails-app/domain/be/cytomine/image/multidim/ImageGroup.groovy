package be.cytomine.image.multidim

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.project.Project
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 8:33
 * A group of image with diff dimension
 */
class ImageGroup extends CytomineDomain implements Serializable {

    String name
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
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['name'] = it.name
            returnArray['project'] = it.project?.id
            returnArray['created'] = it.created?.time?.toString()
            returnArray['updated'] = it.updated?.time?.toString()
            return returnArray
        }
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return project.container();
    }
}
