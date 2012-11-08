package be.cytomine.command

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import org.apache.log4j.Logger
import grails.converters.JSON

class Task extends CytomineDomain{

    int progress

    Project project
    SecUser user

    List comments
    static hasMany = [comments:String]

    static constraints = {
        progress(range: 0..100)
    }

    static mapping = {
          id generator: "assigned"
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Task.class)
        JSON.registerObjectMarshaller(Task) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['progress'] = it.progress
            println "marshaller:"+ it.comments
            returnArray['comments'] = it.comments

            returnArray['project'] = it.project.id

            returnArray['user'] = it.user.id

            returnArray['created'] = it.created
            returnArray['updated'] = it.updated

            return returnArray
        }
    }
}
