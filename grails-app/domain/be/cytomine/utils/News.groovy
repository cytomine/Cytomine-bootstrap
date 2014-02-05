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

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['text'] = domain?.text
        returnArray['user'] = domain?.user?.id
        returnArray['added'] = domain?.added?.time?.toString()
        returnArray
    }
}
