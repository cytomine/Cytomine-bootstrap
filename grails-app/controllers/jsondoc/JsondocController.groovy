package jsondoc

import grails.converters.JSON
import org.codehaus.groovy.grails.commons.UrlMappingsArtefactHandler
import org.codehaus.groovy.grails.web.mapping.ResponseCodeMappingData
import org.codehaus.groovy.grails.web.mapping.ResponseCodeUrlMapping
import org.codehaus.groovy.grails.web.mapping.UrlMapping

class JsondocController {

    def grailsApplication

    def index() {
    }

    def api() {
        render(ApiRegistry.jsondoc as JSON)
    }


    def build() {
        BuildPathMap buildPathMap = new BuildPathMap()
        RulesLight rules = buildPathMap.build()
        println rules
        render rules.rules
        APIUtils.buildApiRegistry(grailsApplication.mainContext, grailsApplication)
    }













}
