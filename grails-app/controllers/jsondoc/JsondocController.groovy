package jsondoc

import be.cytomine.api.doc.CustomResponseDoc
import grails.converters.JSON
import jsondoc.utils.BuildPathMap
import jsondoc.utils.MappingRules
import org.reflections.Reflections

class JsondocController {

    static String JSONFILE = "doc.json"

    def grailsApplication

    def index() {
    }

    def api() {
        render(ApiRegistry.jsondoc as JSON)
    }

    def apiprod() {
        File docFile = new File(JSONFILE)
        render(docFile.text)
    }

    def build() {
        BuildPathMap buildPathMap = new BuildPathMap()
        MappingRules rules = buildPathMap.build()
        render rules.rules
        APIUtils.buildApiRegistry(grailsApplication,new  CustomResponseDoc())
        File docFile = new File(JSONFILE)
        docFile.write((ApiRegistry.jsondoc as JSON).toString(true))
    }

}
