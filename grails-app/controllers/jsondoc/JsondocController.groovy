package jsondoc

import grails.converters.JSON
import jsondoc.utils.BuildPathMap
import jsondoc.utils.RulesLight
import org.reflections.Reflections

class JsondocController {

    def grailsApplication

    def index() {
    }

    def api() {
        render(ApiRegistry.jsondoc as JSON)
    }

    def apiprod() {
        File docFile = new File("doc.json")
        render(docFile.text)
    }

    def build() {
        BuildPathMap buildPathMap = new BuildPathMap()
        RulesLight rules = buildPathMap.build()
        render rules.rules
        APIUtils.buildApiRegistry(grailsApplication.mainContext, grailsApplication)
        File docFile = new File("doc.json")
        docFile.write((ApiRegistry.jsondoc as JSON).toString(true))
    }

    def listdocClass() {
        Reflections reflections = new Reflections("be.cytomine.api.doc");

        reflections.get

        Set<Class<? extends Object>> allClasses =
                reflections.getSubTypesOf(Object.class);
    }

}
