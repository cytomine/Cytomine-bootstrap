package be.cytomine.utils

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A project is the main cytomine domain
 * It structure user data
 */
class Description extends CytomineDomain implements Serializable {

    /**
     * text data
     */
    String data

    /**
     * Domain class Name
     */
    String domainClassName

    /**
     * Domain id
     */
    Long domainIdent

    static constraints = {
        data(type: 'text',nullable: false)
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Description.withNewSession {
            Description descriptionAlreadyExist = Description.findByDomainIdentAndDomainClassName(domainIdent,domainClassName)
            if(descriptionAlreadyExist && (descriptionAlreadyExist.id!=id))
                throw new AlreadyExistException("Domain $domainClassName with id $domainIdent already has description!")
        }
    }

    static mapping = {
        id generator: "assigned"
        data type: 'text'
        sort "id"
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */           
    static Description insertDataIntoDomain(def json,def domain = new Description()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.domainClassName = JSONUtils.getJSONAttrStr(json, 'domainClassName',true)
        domain.domainIdent = JSONUtils.getJSONAttrLong(json,'domainIdent',null)
        domain.data = JSONUtils.getJSONAttrStr(json, 'data',true)

        domain.created = JSONUtils.getJSONAttrDate(json, 'created')
        domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')


        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Description.class)
        JSON.registerObjectMarshaller(Description) { description ->
            def returnArray = [:]
            returnArray['class'] = description.class
            returnArray['id'] = description.id
            returnArray['domainClassName'] = description.domainClassName
            returnArray['domainIdent'] = description.domainIdent

            returnArray['data'] = description.data //'<br/><img src="http://localhost:8080/api/attachedfile/8527848/download.png" align="left"><br/>' //description.data

            returnArray['created'] = description.created
            returnArray['updated'] = description.updated
            return returnArray
        }
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return getDomain()?.container()
    }

    public setDomain(CytomineDomain domain) {
        domainClassName = domain.class.name
        domainIdent = domain.id
    }


    public getDomain() {
        Class.forName(domainClassName, false, Thread.currentThread().contextClassLoader).read(domainIdent)
    }

}
