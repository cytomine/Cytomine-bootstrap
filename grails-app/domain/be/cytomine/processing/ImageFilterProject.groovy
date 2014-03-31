package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.project.Project
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields
import org.restapidoc.annotation.RestApiObject

/**
 * An image filter can be link to many projects
 */
@RestApiObject(name = "image filter project", description = "An image filter can be link to many projects")
class ImageFilterProject extends CytomineDomain implements Serializable {

    @RestApiObjectField(description = "The filter")
    ImageFilter imageFilter

    @RestApiObjectField(description = "The project")
    Project project

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "processingServer", description = "The URL of the processing server",allowedType = "string",useForCreation = false),
        @RestApiObjectField(apiFieldName = "baseUrl", description = "The URL path of the filter on the processing server",allowedType = "string",useForCreation = false),
        @RestApiObjectField(apiFieldName = "name", description = "The filter name",allowedType = "string",useForCreation = false)
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
    static ImageFilterProject insertDataIntoDomain(def json, def domain = new ImageFilterProject()) {
        try {
            domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
            domain.imageFilter = JSONUtils.getJSONAttrDomain(json, "imageFilter", new ImageFilter(), true)
            domain.project = JSONUtils.getJSONAttrDomain(json, "project", new Project(), true)
        }
        catch (Exception e) {
            domain.imageFilter = JSONUtils.getJSONAttrDomain(json.imageFilter, "id", new ImageFilter(), true)
            domain.project = JSONUtils.getJSONAttrDomain(json.project, "id", new Project(), true)
        }
        return domain;
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        ImageFilterProject.withNewSession {
            if(imageFilter && project)  {
                ImageFilterProject ifp = ImageFilterProject.findByImageFilterAndProject(imageFilter,project)
                   if(ifp!=null && (ifp.id!=id))  {
                       throw new AlreadyExistException("Filter ${imageFilter?.name} is already map with project ${project?.name}")
                   }
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
        returnArray['imageFilter'] = domain?.imageFilter?.id
        returnArray['project'] = domain?.project?.id

        returnArray['processingServer'] = domain?.imageFilter?.processingServer?.url
        returnArray['baseUrl'] = domain?.imageFilter?.baseUrl
        returnArray['name'] = domain?.imageFilter?.name
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

