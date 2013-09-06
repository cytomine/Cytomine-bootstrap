package be.cytomine.utils

import be.cytomine.CytomineDomain
import be.cytomine.security.User
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Cytomine new
 * Ex: "2013/08/10: Data from job can now be deleted"
 */
class News extends CytomineDomain {

    Date added
    String text
    User user

    static constraints = {
        text(type: 'text',nullable: false)
    }

    static mapping = {
        id generator: "assigned"
        text type: 'text'
        sort "id"
    }

    static void registerMarshaller() {
         Logger.getLogger(this).info("Register custom JSON renderer for " + News.class)
         JSON.registerObjectMarshaller(News) { news ->
             def returnArray = [:]
             returnArray['class'] = news.class
             returnArray['text'] = news.text
             returnArray['user'] = news.user.id
             returnArray['added'] = news.added?.time?.toString()
             returnArray['created'] = news.created?.time?.toString()
             returnArray['updated'] = news.updated?.time?.toString()
             return returnArray
         }
     }
}
