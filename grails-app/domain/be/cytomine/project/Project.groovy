package be.cytomine.project

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.ontology.Ontology
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.annotation.ApiObjectField


/**
 * A project is the main cytomine domain
 * It structure user data
 */
@ApiObject(name = "project")
class Project extends CytomineDomain implements Serializable {

    /**
     * Project name
     */
    @ApiObjectField(description = "The name of the project")
    String name

    /**
     * Project ontology link
     */
    @ApiObjectField(
            description = "The ontology identifier of the project",
            allowedType = "integer",
            apiFieldName = "ontology",
            apiValueAccessor = "ontologyID")
    Ontology ontology

    /**
     * Project discipline link
     */
    @ApiObjectField(
            description = "The discipline identifier of the project",
            allowedType = "integer",
            apiFieldName = "discipline",
            apiValueAccessor = "disciplineID")
    Discipline discipline


    @ApiObjectField(
            description = "Blind mode (if true, image filename are hidden)",
            allowedType = "boolean")
    boolean blindMode = false

    /**
     * Number of projects user annotations
     */
    long countAnnotations

    /**
     * Number of projects algo annotations
     */
    long countJobAnnotations

    /**
     * Number of projects images
     */
    long countImages

    /**
     * Number of projects reviewed annotations
     */
    long countReviewedAnnotations

    /**
     * Flag if retrieval is disable
     * If true, don't suggest similar annotations
     */
    @ApiObjectField(
            description = "If true, don't suggest similar annotations",
            allowedType = "boolean")
    boolean retrievalDisable = false

    /**
     * Flag for retrieval search on all ontologies
     * If true, search similar annotations on all project that share the same ontology
     */
    @ApiObjectField(
            description = "If true, search similar annotations on all project that share the same ontology",
            allowedType = "boolean")
    boolean retrievalAllOntology = true

    @ApiObjectField(
            description = "If true, project is closed",
            allowedType = "boolean")
    boolean isClosed = false

    @ApiObjectField(
            description = "If true, project is in read only mode",
            allowedType = "boolean")
    boolean isReadOnly = false
    /**
     * Flag if project has private layer
     * A project user only see its layer
     */

    @ApiObjectField(
            description = "If true, an user ( which is not an administrator of the project) see only its own annotations layer",
            allowedType = "boolean")
    boolean hideUsersLayers = false

    @ApiObjectField(
            description = "If true, a user (including the administrators) see only its own annotations layer",
            allowedType = "boolean")
    boolean hideAdminsLayers = false


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

    private static Integer ontologyID(project) {
        return project.getOntology()?.id
    }

    private static Integer disciplineID(project) {
        return project.getDiscipline()?.id
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



//        int nbreAnnotation = jsondoc.JSONUtils.getJSONAttrLong(json, 'countAnnotations', -1)
//        domain.countAnnotations = (nbreAnnotation!=-1? nbreAnnotation : domain.countAnnotations)
//
//        int nbreImage = jsondoc.JSONUtils.getJSONAttrLong(json, 'countImages', -1)
//        domain.countImages = jsondoc.JSONUtils.getJSONAttrLong(json, 'countImages', 0)
//
//        domain.countJobAnnotations = jsondoc.JSONUtils.getJSONAttrLong(json, 'countJobAnnotations', 0)

        domain.retrievalDisable = JSONUtils.getJSONAttrBoolean(json, 'retrievalDisable', false)
        domain.retrievalAllOntology = JSONUtils.getJSONAttrBoolean(json, 'retrievalAllOntology', true)

        domain.blindMode = JSONUtils.getJSONAttrBoolean(json, 'blindMode', false)
        domain.created = JSONUtils.getJSONAttrDate(json, 'created')
        domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')

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
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + this.class)
        println "<<< mapping from Project <<< " + getMappingFromAnnotation(Project)
        JSON.registerObjectMarshaller(Project) { domain ->
            return getDataFromDomain(domain, getMappingFromAnnotation(Project))
        }
    }

    static def getDataFromDomain(def domain, LinkedHashMap<String, Object> mapFields = null) {

        /* base fields + api fields */
        def json = getAPIBaseFields(domain) + getAPIDomainFields(domain, mapFields)

        /* supplementary fields : which are NOT used in insertDataIntoDomain !
        * Typically, these fields are shortcuts or supplementary information
        * from other domains
        * ::to do : hide these fields if not GUI ?
        * */

        json['ontologyName'] = domain.ontology?.name
        json['disciplineName'] = domain.discipline?.name
        json['numberOfSlides'] = domain.countSamples()
        json['numberOfImages'] = domain.countImageInstance()
        json['numberOfAnnotations'] = domain.countAnnotations()
        json['numberOfJobAnnotations'] = domain.countJobAnnotations()
        json['retrievalProjects'] = domain.retrievalProjects.collect { it.id }
        json['numberOfReviewedAnnotations'] = domain.countReviewedAnnotations

        return json
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
