package be.cytomine.utils

import be.cytomine.image.NestedImageInstance
import grails.converters.JSON

/**
 * Service to manage marshaller
 * Marshaller provide json/xml/... response structure for web request
 */
class MarshallersService {

    def grailsApplication
    static transactional = false

    /**
     * Init marshaller for all cytomine domain
     */
    def initMarshallers() {
        JSON.registerObjectMarshaller(Date) {
            return it?.time?.toString()
        }
        grailsApplication.getDomainClasses().each { domain ->
            domain.metaClass.methods.each { method ->
                if (method.name.equals("registerMarshaller")) {
                    def domainFullName = domain.packageName + "." + domain.name
                    log.info "Init Marshaller for domain class : " + domainFullName
                    def domainInstance = grailsApplication.getDomainClass(domainFullName).newInstance()
                    domainInstance.registerMarshaller()
                }

            }

        }
        //if ImageInstance.registerMarshaller is call after NestedImageInstance..registerMarshaller, it override it
        NestedImageInstance.registerMarshaller()
    }
}
