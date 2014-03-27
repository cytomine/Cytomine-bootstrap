package jsondoc

import grails.util.Holders
import groovy.util.logging.Log
import jsondoc.utils.JSONDocUtilsLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiObject

/**
 * Created by stevben on 16/12/13.
 */
@Log
class APIUtils {

    static String VERSION = "1.0"
    static String BASEPATH = "....TO REPLACE...."


    static void buildApiRegistry(grailsApplication,customDoc=null) {

        //Retrieve all controlers (for method doc)
        log.info "Retrieve Controller..."
        Set<Class> controllersClasses = new LinkedList<Class>()
        grailsApplication.controllerClasses.findAll {it.clazz.isAnnotationPresent(Api) }
                .each { controllerArtefact ->
            def controllerClass = controllerArtefact.getClazz()
            controllersClasses.add(controllerClass)
        }

        //Retrieve all domains (for object doc)
        log.info "Retrieve Domain..."
        Set<Class<?>> objectClasses = new LinkedList<Class<?>>()
        grailsApplication.domainClasses.findAll {it.clazz.isAnnotationPresent(ApiObject) }.each { domainArtefact ->
            def domainClass = domainArtefact.getClazz()
            objectClasses.add(domainClass)
        }

        //Generate doc
        def objectsDoc = JSONDocUtilsLight.getApiObjectDocs(objectClasses, customDoc)
        def controllerDoc = JSONDocUtilsLight.getApiDocs(controllersClasses)

        //Register doc
        ApiRegistry.jsondoc =
                ["version" : VERSION,
                 basePath : "${Holders.getGrailsApplication().config.grails.serverURL}/api",
                "apis" : controllerDoc,
                "objects" : objectsDoc]
    }
}
