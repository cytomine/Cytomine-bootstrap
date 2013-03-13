package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A parameter for a software.
 * It's a template to create job parameter.
 */
class SoftwareParameter extends CytomineDomain {

    /**
     * Software for parameter
     */
    Software software

    /**
     * Parameter name
     */
    String name

    /**
     * Parameter type (Number, String, other domain...)
     */
    String type

    /**
     * Default value when creating job parameter
     * All value are stored in (generic) String
     */
    String defaultValue

    /**
     * Flag if value is mandatory
     */
    Boolean required = false

    /**
     * Index for parameter position.
     * When launching software, parameter will be send ordered by index (asc)
     */
    Integer index=-1

    /**
     * Used for UI
     * If parameter has "Domain" type, the URI will provide a list of choice.
     * E.g. if uri is api/project.json, the choice list will be cytomine project list
     */
    String uri

    /**
     * JSON Fields to print in choice list
     * E.g. if uri is api/project.json and uriPrintAttribut is "name", the choice list will contains project name
     */
    String uriPrintAttribut

    /**
     * JSON Fields used to sort choice list
     */
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
        //if index is not set, automaticaly set it to lastIndex+1
        SoftwareParameter softwareParam = SoftwareParameter.findBySoftware(software,[max: 1,sort: "index",order: "desc"])
        if(this.index==-1) {
              if(softwareParam)
                this.index =  softwareParam.index
              else
                this.index = 0
        }
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
   void checkAlreadyExist() {
        SoftwareParameter.withNewSession {
            SoftwareParameter softwareParamAlreadyExist=SoftwareParameter.findBySoftwareAndName(software,name)
            if(softwareParamAlreadyExist!=null && (softwareParamAlreadyExist.id!=id)) {
                throw new AlreadyExistException("Parameter " + softwareParamAlreadyExist?.name + " already exist for software " + softwareParamAlreadyExist?.software?.name)
            }
        }
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
     static void registerMarshaller() {
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
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */    
    static SoftwareParameter insertDataIntoDomain(def json, def domain = new SoftwareParameter()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name', true)
        domain.software = JSONUtils.getJSONAttrDomain(json, "software", new Software(), true)
        domain.type = JSONUtils.getJSONAttrStr(json, 'type', true)
        domain.defaultValue = JSONUtils.getJSONAttrStr(json, 'defaultValue')
        domain.required = JSONUtils.getJSONAttrBoolean(json, 'required',false)
        domain.defaultValue = JSONUtils.getJSONAttrInteger(json, 'index', -1)
        domain.uri = JSONUtils.getJSONAttrStr(json,'uri')
        domain.uriPrintAttribut = JSONUtils.getJSONAttrStr(json,'uriPrintAttribut')
        domain.uriSortAttribut = JSONUtils.getJSONAttrStr(json,'uriSortAttribut')
        return domain;
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return software.container();
    }

}
