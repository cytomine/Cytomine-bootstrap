package be.cytomine.processing

import grails.converters.JSON
import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.Exception.AlreadyExistException

class SoftwareParameter extends CytomineDomain {

    Software software
    String name
    String type
    String defaultValue
    Boolean required = false
    Integer index=-1

    static belongsTo = [Software]

    static constraints = {
        name (nullable: false, blank : false)
        type (inList: ["String", "Boolean", "Number","List","ListProject","Project"])
        defaultValue (nullable: true, blank : true)
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

    def getIdSoftware() {
//        if (this.softwareId) return this.softwareId
//        else return this.software?.id
        return this.software?.id
    }

     static void registerMarshaller() {
        println "Register custom JSON renderer for " + SoftwareParameter.class
        JSON.registerObjectMarshaller(SoftwareParameter) {
            def softwareParameter = [:]
            softwareParameter.id = it.id
            softwareParameter.name = it.name
            softwareParameter.type = it.type
            softwareParameter.defaultParamValue = it.defaultValue  //defaultValue & default are reserved
            softwareParameter.required = it.required
            softwareParameter.software = it.getIdSoftware()
            softwareParameter.index = it.index
            return softwareParameter
        }
    }

    static SoftwareParameter createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static SoftwareParameter createFromData(jsonSoftwareParameter) {
        def softwareParameter = new SoftwareParameter()
        getFromData(softwareParameter, jsonSoftwareParameter)
    }

    static SoftwareParameter getFromData(softwareParameter, jsonSoftwareParameter) {
        if (!jsonSoftwareParameter.name.toString().equals("null"))
            softwareParameter.name = jsonSoftwareParameter.name
        else throw new WrongArgumentException("SoftwareParameter name cannot be null")

        if (!jsonSoftwareParameter.software.toString().equals("null"))
            softwareParameter.software = Software.read(jsonSoftwareParameter.software)
        if(!softwareParameter.software) throw new WrongArgumentException("SoftwareParameter software cannot be null:"+jsonSoftwareParameter.software)

        if (!jsonSoftwareParameter.type.toString().equals("null"))
            softwareParameter.type = jsonSoftwareParameter.type
        else throw new WrongArgumentException("SoftwareParameter type cannot be null")

        if (!jsonSoftwareParameter.defaultValue.toString().equals("null"))
            softwareParameter.defaultValue = jsonSoftwareParameter.defaultValue

        if (!jsonSoftwareParameter.required.toString().equals("null"))
            softwareParameter.required = Boolean.parseBoolean(jsonSoftwareParameter.required.toString())

        if (!jsonSoftwareParameter.index.toString().equals("null"))
            softwareParameter.index = Integer.parseInt(jsonSoftwareParameter.index.toString())

        return softwareParameter;
    }
}
