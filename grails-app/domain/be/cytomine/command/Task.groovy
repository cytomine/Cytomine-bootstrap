package be.cytomine.command

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A task provide info about a command.
 * The main info is the progress status
 */
class Task extends CytomineDomain{

    /**
     * Request progress between 0 and 100
     */
    int progress

    /**
     * Project updated by the command task
     */
    Project project

    /**
     * User that ask the task
     */
    SecUser user

    /**
     * Task order comments
     * It can be something like this: ["start request","adding annotation",..]
     */
    List comments

    static hasMany = [comments:String]

    static constraints = {
        progress(range: 0..100)
    }

    static mapping = {
          id generator: "assigned"
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Task.class)
        JSON.registerObjectMarshaller(Task) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['progress'] = it.progress
            returnArray['comments'] = it.comments
            returnArray['project'] = it.project.id
            returnArray['user'] = it.user.id
            returnArray['created'] = it.created
            returnArray['updated'] = it.updated
            return returnArray
        }
    }
}
