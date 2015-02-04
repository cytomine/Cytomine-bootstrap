package be.cytomine.project

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.ontology.Ontology
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields
import org.restapidoc.annotation.RestApiObject

/**
 * A project is the main cytomine domain
 * It structure user data
 */
@RestApiObject(name = "project", description="A project is the main cytomine domain, its a workspace to store images, annotations,...")
class Project extends CytomineDomain implements Serializable {

    /**
     * Project name
     */
    @RestApiObjectField(description = "The name of the project")
    String name

    /**
     * Project ontology link
     */
    @RestApiObjectField(description = "The ontology identifier of the project")
    Ontology ontology

    /**
     * Project discipline link
     */
    @RestApiObjectField(description = "The discipline identifier of the project", mandatory = false)
    Discipline discipline


    @RestApiObjectField(description = "Blind mode (if true, image filename are hidden)",mandatory = false)
    boolean blindMode = false

    /**
     * Number of projects user annotations
     */
    @RestApiObjectField(description = "Number of annotations created by human user in the project", apiFieldName="numberOfAnnotations", useForCreation = false)
    long countAnnotations

    /**
     * Number of projects algo annotations
     */
    @RestApiObjectField(description = "Number of annotations created by software in the project", apiFieldName="numberOfJobAnnotations",useForCreation = false)
    long countJobAnnotations

    /**
     * Number of projects images
     */
    @RestApiObjectField(description = "Number of image in the project", apiFieldName="numberOfImages",useForCreation = false)
    long countImages

    /**
     * Number of projects reviewed annotations
     */
    @RestApiObjectField(description = "Number of annotations validated in the project", apiFieldName="numberOfReviewedAnnotations",useForCreation = false)
    long countReviewedAnnotations

    /**
     * Flag if retrieval is disable
     * If true, don't suggest similar annotations
     */
    @RestApiObjectField(description = "If true, don't suggest similar annotations")
    boolean retrievalDisable = false

    /**
     * Flag for retrieval search on all ontologies
     * If true, search similar annotations on all project that share the same ontology
     */
    @RestApiObjectField(description = "If true, search similar annotations on all project that share the same ontology",defaultValue = "true")
    boolean retrievalAllOntology = true

    @RestApiObjectField(description = "If true, project is closed",mandatory = false)
    boolean isClosed = false

    @RestApiObjectField(description = "If true, project is in read only mode",mandatory = false)
    boolean isReadOnly = false

    @RestApiObjectField(description = "If true, an user (which is not an administrator of the project) cannot see others users layers",mandatory = false)
    boolean hideUsersLayers = false

    @RestApiObjectField(description = "If true, an user (which is not an administrator of the project) cannot see admins layers", mandatory = false)
    boolean hideAdminsLayers = false

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "users", description = "Users id that will be in the project",allowedType = "list",useForCreation = true, presentInResponse = false),
        @RestApiObjectField(apiFieldName = "admins", description = "Admins id that will be in the project",allowedType = "list",useForCreation = true, presentInResponse = false),
        @RestApiObjectField(apiFieldName = "ontologyName", description = "The ontology name for the project",allowedType = "string",useForCreation = false),
        @RestApiObjectField(apiFieldName = "disciplineName", description = "The discipline name for the project",allowedType = "string",useForCreation = false),
        @RestApiObjectField(apiFieldName = "numberOfSlides", description = "The number of samples in the project", allowedType = "long",useForCreation = false),
        @RestApiObjectField(apiFieldName = "retrievalProjects", description = "List all projects id that are used for retrieval search (if retrievalDisable = false and retrievalAllOntology = false)",allowedType = "list",mandatory = false)
    ])


    static transients = ["isAdmin"]


    static belongsTo = [ontology: Ontology]

    static hasMany = [retrievalProjects : Project]

    static constraints = {
        name(maxSize: 150, unique: true, blank: false)
        discipline(nullable: true)
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Project.withNewSession {
            Project projectAlreadyExist = Project.findByName(name)
            if(projectAlreadyExist && (projectAlreadyExist.id!=id))  throw new AlreadyExistException("Project "+projectAlreadyExist?.name + " already exist!")
        }
    }

    static mapping = {
        id generator: "assigned"
        ontology fetch: 'join'
        discipline fetch: 'join'
        sort "id"
        cache true
    }

    String toString() {
        name
    }

    def countImageInstance() {
        countImages
    }

    def countAnnotations() {
        countAnnotations
    }

    def countJobAnnotations() {
        countJobAnnotations
    }


    def countSamples() {
        //TODO::implement
        return 0
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Project insertDataIntoDomain(def json,def domain = new Project()) {

        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name',true)
        domain.ontology = JSONUtils.getJSONAttrDomain(json, "ontology", new Ontology(), true)
        domain.discipline = JSONUtils.getJSONAttrDomain(json, "discipline", new Discipline(), false)

        domain.retrievalDisable = JSONUtils.getJSONAttrBoolean(json, 'retrievalDisable', false)
        domain.retrievalAllOntology = JSONUtils.getJSONAttrBoolean(json, 'retrievalAllOntology', true)

        domain.blindMode = JSONUtils.getJSONAttrBoolean(json, 'blindMode', false)
        domain.created = JSONUtils.getJSONAttrDate(json, 'created')
        domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')
        domain.deleted = JSONUtils.getJSONAttrDate(json, "deleted")
        domain.isClosed = JSONUtils.getJSONAttrBoolean(json, 'isClosed', false)
        domain.isReadOnly = JSONUtils.getJSONAttrBoolean(json, 'isReadOnly', false)

        domain.hideUsersLayers = JSONUtils.getJSONAttrBoolean(json, 'hideUsersLayers', false)
        domain.hideAdminsLayers = JSONUtils.getJSONAttrBoolean(json, 'hideAdminsLayers', false)

        if(!json.retrievalProjects.toString().equals("null")) {
            domain.retrievalProjects?.clear()
            json.retrievalProjects.each { idProject ->
                Long proj = Long.parseLong(idProject.toString())
                //-1 = project himself, project has no id when client send request
                Project projectRetrieval = (proj==-1 ? domain : Project.read(proj))
                if(projectRetrieval) {
                    ((Project)domain).addToRetrievalProjects(projectRetrieval)
                }
            }
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
        returnArray['name'] = domain?.name
        returnArray['ontology'] = domain?.ontology?.id
        returnArray['ontologyName'] = domain?.ontology?.name
        returnArray['discipline'] = domain?.discipline?.id
        returnArray['blindMode'] = (domain?.blindMode != null &&  domain?.blindMode)
        returnArray['disciplineName'] = domain?.discipline?.name
        returnArray['numberOfSlides'] = domain?.countSamples()
        returnArray['numberOfImages'] = domain?.countImageInstance()
        returnArray['numberOfAnnotations'] = domain?.countAnnotations()
        returnArray['numberOfJobAnnotations'] = domain?.countJobAnnotations()
        returnArray['retrievalProjects'] = domain?.retrievalProjects.collect { it.id }
        returnArray['numberOfReviewedAnnotations'] = domain?.countReviewedAnnotations
        returnArray['retrievalDisable'] = domain?.retrievalDisable
        returnArray['retrievalAllOntology'] = domain?.retrievalAllOntology
        returnArray['isClosed'] = domain?.isClosed
        returnArray['isReadOnly'] = domain?.isReadOnly
        returnArray['hideUsersLayers'] = domain?.hideUsersLayers
        returnArray['hideAdminsLayers'] = domain?.hideAdminsLayers

        return returnArray
    }


    public boolean equals(Object o) {
        if (!o) {
            return false
        } else {
            try {
                return ((Project) o).getId() == this.getId()
            } catch (Exception e) {
                return false
            }
        }
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return this;
    }


    boolean canUpdateContent() {
        return !isReadOnly
    }



}
