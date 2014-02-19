package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.utils.JSONUtils
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.jsondoc.core.annotation.ApiObject

/**
 * A link between a software and a project
 * We can add a software to many projects
 */
@ApiObject(name = "software project", description = "A link between a software and a project. We can add a software to many projects")
class SoftwareProject extends CytomineDomain implements Serializable{

    @ApiObjectFieldLight(description = "The software")
    Software software

    @ApiObjectFieldLight(description = "The project")
    Project project

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "name", description = "The name of the software",allowedType = "string",useForCreation = false)
    ])
    static transients = []

    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static SoftwareProject insertDataIntoDomain(def json,def domain=new SoftwareProject()) {
        try {
            domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
            domain.software = JSONUtils.getJSONAttrDomain(json.software, "id", new Software(), true)
            domain.project = JSONUtils.getJSONAttrDomain(json.project, "id", new Project(), true)
        }
        catch (Exception e) {
            domain.software = JSONUtils.getJSONAttrDomain(json, "software", new Software(), true)
            domain.project = JSONUtils.getJSONAttrDomain(json, "project", new Project(), true)
        }
        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['software'] = domain?.software?.id
        returnArray['name'] = domain?.software?.name
        returnArray['project'] = domain?.project?.id
        return returnArray
    }


    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return project.container();
    }
}
