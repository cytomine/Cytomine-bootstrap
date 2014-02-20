package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import jsondoc.annotation.ApiObjectFieldLight
import org.jsondoc.core.annotation.ApiObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 19/02/14
 * Time: 7:33
 * A job template is a job with pre-filled parameters. It can be used to init a new "real" job on the basis of this template.
 */
@ApiObject(name = "job template", description = "A job template is a job with pre-filled parameters. It can be used to init a new 'real' job on the basis of this template.")
class JobTemplate extends Job implements Serializable {

    @ApiObjectFieldLight(description = "The template name")
    String name

    static constraints = {
        name nullable: false
    }

    static mapping = {
        id generator: "assigned"
        sort "id"
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        JobTemplate.withNewSession {
            JobTemplate job = JobTemplate.findByName(name)
            if (job != null && (job.id != id)) {
                throw new AlreadyExistException("Job template" + name + " already exist")
            }
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static JobTemplate insertDataIntoDomain(def json, def domain = new JobTemplate()) {
        domain = (JobTemplate)Job.insertDataIntoDomain(json,domain)
        domain.name = JSONUtils.getJSONAttrStr(json,"name",true)
        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = Job.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        return returnArray
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return project.container()
    }

}
