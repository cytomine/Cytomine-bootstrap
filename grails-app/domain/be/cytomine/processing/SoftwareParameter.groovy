package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON
import org.apache.log4j.Logger

class SoftwareParameter extends CytomineDomain {

    Software software
    String name
    String type
    String defaultValue
    Boolean required = false
    Integer index=-1
    String uri
    String uriPrintAttribut
    String uriSortAttribut

    static belongsTo = [Software]

    static constraints = {
        name (nullable: false, blank : false)
        type (inList: ["String", "Boolean", "Number","Date","List","ListDomain","Domain"])
        defaultValue (nullable: true, blank : true)
        uri (nullable: true, blank : true)
        uriPrintAttribut (nullable: true, blank : true)
        uriSortAttribut (nullable: true, blank : true)
    }

    public beforeInsert() {
        super.beforeInsert()
        SoftwareParameter softwareParam = SoftwareParameter.findBySoftware(software,[max: 1,sort: "index",order: "desc"])
        if(this.index==-1) {
              if(softwareParam)
                this.index =  softwareParam.index
              else
                this.index = 0
        }
    }

   void checkAlreadyExist() {
        SoftwareParameter.withNewSession {
            SoftwareParameter softwareParamAlreadyExist=SoftwareParameter.findBySoftwareAndName(software,name)
            if(softwareParamAlreadyExist!=null && (softwareParamAlreadyExist.id!=id))
                throw new AlreadyExistException("Parameter " + softwareParamAlreadyExist?.name + " already exist for software " + softwareParamAlreadyExist?.software?.name)
        }
    }

    String toString() {
        return (this as JSON).toString()
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
     static void registerMarshaller(String cytomineBaseUrl) {
         Logger.getLogger(this).info("Register custom JSON renderer for " + SoftwareParameter.class)
        JSON.registerObjectMarshaller(SoftwareParameter) {
            def softwareParameter = [:]
            softwareParameter.id = it.id
            softwareParameter.name = it.name
            softwareParameter.type = it.type
            softwareParameter.defaultParamValue = it.defaultValue  //defaultValue & default are reserved
            softwareParameter.required = it.required
            softwareParameter.software = it.software?.id
            softwareParameter.index = it.index
            softwareParameter.uri = it.uri
            softwareParameter.uriPrintAttribut = it.uriPrintAttribut
            softwareParameter.uriSortAttribut = it.uriSortAttribut

            return softwareParameter
        }
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static SoftwareParameter createFromDataWithId(def json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static SoftwareParameter createFromData(def json) {
        def softwareParameter = new SoftwareParameter()
        insertDataIntoDomain(softwareParameter, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */    
    static SoftwareParameter insertDataIntoDomain(def domain, def json) {
        if (!json.name.toString().equals("null"))
            domain.name = json.name
        else throw new WrongArgumentException("domain name cannot be null")

        if (!json.software.toString().equals("null"))
            domain.software = Software.read(json.software)
        if(!domain.software) throw new WrongArgumentException("domain software cannot be null:"+json.software)

        if (!json.type.toString().equals("null"))
            domain.type = json.type
        else throw new WrongArgumentException("domain type cannot be null")

        if (!json.defaultValue.toString().equals("null"))
            domain.defaultValue = json.defaultValue

        if (!json.required.toString().equals("null"))
            domain.required = Boolean.parseBoolean(json.required.toString())

        if (!json.index.toString().equals("null"))
            domain.index = Integer.parseInt(json.index.toString())

        domain.uri = json.uri
        domain.uriPrintAttribut = json.uriPrintAttribut
        domain.uriSortAttribut = json.uriSortAttribut

        return domain;
    }
}
