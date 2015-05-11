package be.cytomine.utils

import be.cytomine.CytomineDomain
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields

/**
 * A file to attach to any Cytomine domain
 */
@RestApiObject(name = "attached file", description = "A file that may be attached to any Cytomine domain. Usefull to include file into description.")
class AttachedFile extends CytomineDomain {

    /**
     * File data
     */
    byte[] data

    @RestApiObjectField(description = "Domain class name")
    String domainClassName

    @RestApiObjectField(description = "Domain id")
    Long domainIdent

    @RestApiObjectField(description = "File name with ext")
    String filename

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "url", description = "URL to get this file",allowedType = "string",useForCreation = false)
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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
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
