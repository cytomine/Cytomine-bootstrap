package be.cytomine.utils

import be.cytomine.CytomineDomain
import be.cytomine.security.User
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Keywords may be used to suggest a term in an input
 */
class Keyword {

    String key

    static mapping = {
        version false
        id generator: 'identity', column: 'nid'
        key(unique:true)
    }
}
