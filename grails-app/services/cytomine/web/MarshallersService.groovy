package cytomine.web

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
        String baseUrl = grailsApplication.config.grails.serverURL
        JSON.registerObjectMarshaller(Date) {
            return it?.time?.toString()
        }
        grailsApplication.getDomainClasses().each { domain ->
            domain.metaClass.methods.each { method ->
                if (method.name.equals("registerMarshaller")) {
                    def domainFullName = domain.packageName + "." + domain.name
                    log.info "Init Marshaller for domain class : " + domainFullName
                    def domainInstance = grailsApplication.getDomainClass(domainFullName).newInstance()
                    domainInstance.registerMarshaller(baseUrl)
                }

            }

        }
    }
}
