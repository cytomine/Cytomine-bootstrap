package jsondoc

import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.util.JSONDocUtils

/**
 * Created by stevben on 16/12/13.
 */
class APIUtils {
    static void buildApiRegistry(applicationContext, grailsApplication) {
        Set<Class> controllersClasses = new LinkedList<Class>()
        grailsApplication.controllerClasses.findAll {it.clazz.isAnnotationPresent(Api) }
                .each { controllerArtefact ->
            def controllerClass = controllerArtefact.getClazz()
            controllersClasses.add(controllerClass)
        }


        Set<Class<?>> objectClasses = new LinkedList<Class<?>>()
        grailsApplication.domainClasses.findAll {it.clazz.isAnnotationPresent(ApiObject) }.each { domainArtefact ->
            def domainClass = domainArtefact.getClazz()
            println "$domainArtefact, $domainClass"
            objectClasses.add(domainClass)
        }


        def objectsDoc = JSONDocUtils.getApiObjectDocs(objectClasses)
        def controllerDoc = JSONDocUtils.getApiDocs(controllersClasses)

        ApiRegistry.jsondoc =
                ["version" : "1.0",
                 basePath : "http://localhost:8080/api",
                "apis" : controllerDoc,
                "objects" : objectsDoc]
    }
}
