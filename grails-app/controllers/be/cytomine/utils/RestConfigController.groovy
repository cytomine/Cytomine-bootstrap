package be.cytomine.utils

import grails.converters.JSON
import grails.converters.XML

class RestConfigController {

    def ldap() {
        def data = [:]
        data['enabled'] = true
        withFormat {
            json { render data as JSON }
            xml { render data as XML}
        }
    }

}