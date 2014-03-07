package be.cytomine.utils

import be.cytomine.image.NestedImageInstance
import be.cytomine.processing.JobTemplate
import be.cytomine.security.User
import be.cytomine.security.UserJob
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
                if (method.name.equals("getDataFromDomain")) {
                    def domainFullName = domain.packageName + "." + domain.name
                    log.info "Init Marshaller for domain class : " + domainFullName
                    def domainInstance = grailsApplication.getDomainClass(domainFullName).newInstance()
                    log.info("Register custom JSON renderer for " + this.class)
                    JSON.registerObjectMarshaller(domain.clazz) { it ->
                        return domainInstance.getDataFromDomain(it)
                    }
                }
            }
        }
        //if ImageInstance.registerMarshaller is call after NestedImageInstance..registerMarshaller, it override it
        JSON.registerObjectMarshaller(NestedImageInstance) { it ->
            return NestedImageInstance.getDataFromDomain(it)
        }
        JSON.registerObjectMarshaller(User) { it ->
            return User.getDataFromDomain(it)
        }
        JSON.registerObjectMarshaller(UserJob) { it ->
            return UserJob.getDataFromDomain(it)
        }
        JSON.registerObjectMarshaller(JobTemplate) { it ->
            return JobTemplate.getDataFromDomain(it)
        }
    }
}
