package be.cytomine.utils

import be.cytomine.CytomineDomain
import be.cytomine.command.ResponseService
import be.cytomine.security.UserJob
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

/**
 * A file to attach to any Cytomine domain
 */
@ApiObject(name = "attached file", description = "A file that may be attached to any Cytomine domain. Usefull to include file into description.")
class AttachedFile extends CytomineDomain {

    /**
     * File data
     */
    byte[] data

    @ApiObjectFieldLight(description = "Domain class name")
    String domainClassName

    @ApiObjectFieldLight(description = "Domain id")
    Long domainIdent

    @ApiObjectFieldLight(description = "File name with ext")
    String filename

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "url", description = "URL to get this file",allowedType = "string",useForCreation = false)
    ])
    static transients = []

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
            getDataFromDomain(it)
        }
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['domainIdent'] = domain?.domainIdent
        returnArray['domainClassName'] = domain?.domainClassName
        returnArray['url'] = "/api/attachedfile/${domain?.id}/download"
        returnArray['filename'] = domain?.domainClassName
        return returnArray
    }
}
