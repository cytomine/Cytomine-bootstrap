package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.utils.JSONUtils
import jsondoc.annotation.ApiObjectFieldLight
import org.jsondoc.core.annotation.ApiObject

/**
 * A parameter for a software.
 * It's a template to create job parameter.
 */
@ApiObject(name = "software parameter", description = "A parameter for a software. It's a template to create job parameter. When job is init, we create job parameter list based on software parameter list.")
class SoftwareParameter extends CytomineDomain {

//    returnArray['name'] = domain?.name
//    returnArray['type'] = domain?.type
//    returnArray['defaultParamValue'] = domain?.defaultValue  //defaultValue & default are reserved
//    returnArray['required'] = domain?.required
//    returnArray['software'] = domain?.software?.id
//    returnArray['index'] = domain?.index
//    returnArray['uri'] = domain?.uri
//    returnArray['uriPrintAttribut'] = domain?.uriPrintAttribut
//    returnArray['uriSortAttribut'] = domain?.uriSortAttribut
//    returnArray['setByServer'] = domain?.setByServer



    /**
     * Software for parameter
     */
    @ApiObjectFieldLight(description = "The software of the parameter")
    Software software

    /**
     * Parameter name
     */
    @ApiObjectFieldLight(description = "The parameter name")
    String name

    /**
     * Parameter type
     */
    @ApiObjectFieldLight(description = "The parameter data type (Number, String, Date, Boolean, Domain (e.g: image instance id,...), ListDomain )")
    String type

    /**
     * Default value when creating job parameter
     * All value are stored in (generic) String
     */
    @ApiObjectFieldLight(description = "Default value when creating job parameter", mandatory = false, apiFieldName = "defaultParamValue")
    String defaultValue

    /**
     * Flag if value is mandatory
     */
    @ApiObjectFieldLight(description = "Flag if value is mandatory", mandatory = false)
    Boolean required = false

    /**
     * Index for parameter position.
     * When launching software, parameter will be send ordered by index (asc)
     */
    @ApiObjectFieldLight(description = "Index for parameter position. When launching software, parameter will be send ordered by index (asc).", mandatory = false, defaultValue="-1")
    Integer index=-1

    /**
     * Used for UI
     * If parameter has "Domain" type, the URI will provide a list of choice.
     *
     */
    @ApiObjectFieldLight(description = "Used for UI. If parameter has '(List)Domain' type, the URI will provide a list of choice. E.g. if uri is 'api/project.json', the choice list will be cytomine project list", mandatory = false)
    String uri

    /**
     * JSON Fields to print in choice list
     * E.g. if uri is api/project.json and uriPrintAttribut is "name", the choice list will contains project name
     */
    @ApiObjectFieldLight(description = "Used for UI. JSON Fields to print in choice list. E.g. if uri is api/project.json and uriPrintAttribut is 'name', the choice list will contains project name ", mandatory = false)
    String uriPrintAttribut

    /**
     * JSON Fields used to sort choice list
     */
    @ApiObjectFieldLight(description = "Used for UI. JSON Fields used to sort choice list. E.g. if uri is api/project.json and uriSortAttribut is 'id', projects will be sort by id (not by name) ", mandatory = false)
    String uriSortAttribut

    /**
     * Indicated if the field is autofilled by the server
     */
    @ApiObjectFieldLight(description = "Indicated if the field is autofilled by the server", mandatory = false)
    Boolean setByServer = false


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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        returnArray['type'] = domain?.type
        returnArray['defaultParamValue'] = domain?.defaultValue  //defaultValue & default are reserved
        returnArray['required'] = domain?.required
        returnArray['software'] = domain?.software?.id
        returnArray['index'] = domain?.index
        returnArray['uri'] = domain?.uri
        returnArray['uriPrintAttribut'] = domain?.uriPrintAttribut
        returnArray['uriSortAttribut'] = domain?.uriSortAttribut
        returnArray['setByServer'] = domain?.setByServer
        return returnArray
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
        if(!domain.defaultValue) {
            domain.defaultValue = JSONUtils.getJSONAttrStr(json, 'defaultParamValue')
        }
        domain.required = JSONUtils.getJSONAttrBoolean(json, 'required',false)
        domain.index = JSONUtils.getJSONAttrInteger(json, 'index', -1)
        domain.uri = JSONUtils.getJSONAttrStr(json,'uri')
        domain.uriPrintAttribut = JSONUtils.getJSONAttrStr(json,'uriPrintAttribut')
        domain.uriSortAttribut = JSONUtils.getJSONAttrStr(json,'uriSortAttribut')
        domain.setByServer = JSONUtils.getJSONAttrBoolean(json,'setByServer', false)
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
