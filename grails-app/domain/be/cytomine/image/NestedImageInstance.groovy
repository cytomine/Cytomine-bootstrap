package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 8:33
 * An ImageInstance is an image map with a project
 */
class NestedImageInstance extends ImageInstance implements Serializable {

    //stack stuff
    ImageInstance parent
    Integer x
    Integer y

    static belongsTo = [AbstractImage, Project, User]

    static constraints = {
        parent nullable: false
        x nullable: false
        y nullable: false
    }

    static mapping = {
        id generator: "assigned"
        baseImage fetch: 'join'
        sort "id"
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        NestedImageInstance.withNewSession {
            NestedImageInstance imageAlreadyExist = NestedImageInstance.findByBaseImageAndParentAndProject(baseImage,parent,project)
            if (imageAlreadyExist != null && (imageAlreadyExist.id != id)) {
                throw new AlreadyExistException("Nested Image " + baseImage?.filename + " already map with image " + parent?.baseImage?.filename + "in project " + project?.name)
            }
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static NestedImageInstance insertDataIntoDomain(def json, def domain = new NestedImageInstance()) {

        domain = (NestedImageInstance)ImageInstance.insertDataIntoDomain(json,domain)
        domain.parent = JSONUtils.getJSONAttrDomain(json, "parent", new ImageInstance(), false)
        domain.x =  JSONUtils.getJSONAttrInteger(json,"x",0)
        domain.y =  JSONUtils.getJSONAttrInteger(json,"y",0)
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + NestedImageInstance.class)
        JSON.registerObjectMarshaller(NestedImageInstance) { nested ->
            def returnArray = ImageInstance.getDataFromDomain(nested)
            returnArray['parent'] = nested.parent.id
            returnArray['x'] = nested.x
            returnArray['y'] = nested.y
            return returnArray
        }
    }

}
