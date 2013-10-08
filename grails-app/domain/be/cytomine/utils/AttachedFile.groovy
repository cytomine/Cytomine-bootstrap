package be.cytomine.utils

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.processing.JobData
import grails.converters.JSON

/**
 * A file to attach to any Cytomine domain
 */
class AttachedFile extends CytomineDomain {

    /**
     * File data
     */
    byte[] data
    String domainClassName
    Long domainIdent
    String filename

    static constraints = {
        domainClassName(nullable: false, blank:  false)
    }
    static mapping = {
        id generator: "assigned"
        sort "id"
    }

    /**
     * Set annotation (storing class + id)
     * With groovy, you can do: this.annotation = ...
     * @param domain to add
     */
    public void setDomain(CytomineDomain domain) {
        domainClassName = domain.class.getName()
        domainIdent = domain.id
    }

    /**
     * Get annotation thanks to domainClassName and domainIdent
     * @return Annotation concerned with this prediction
     */
    public CytomineDomain retrieveCytomineDomain() {
        CytomineDomain domain = Class.forName(domainClassName, false, Thread.currentThread().contextClassLoader).read(domainIdent)
        domain
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller(){
        JSON.registerObjectMarshaller(AttachedFile){
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['domainIdent'] = it.domainIdent
            returnArray['domainClassName'] = it.domainClassName
            returnArray['url'] = "/api/attachedfile/${it.id}/download"
            returnArray['filename'] = it.domainClassName
            returnArray['created'] = it.created?.time
            returnArray['updated'] = it.updated?.time
            return returnArray
        }
    }
}
