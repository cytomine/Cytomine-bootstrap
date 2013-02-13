package be.cytomine.project

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.command.Command
import be.cytomine.ontology.Ontology
import be.cytomine.processing.ImageFilterProject
import be.cytomine.processing.SoftwareProject
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A project is the main cytomine domain
 * It structure user data
 */
class Project extends CytomineDomain implements Serializable {

    /**
     * Project name
     */
    String name

    /**
     * Project ontology link
     */
    Ontology ontology

    /**
     * Project discipline link
     */
    Discipline discipline

    /**
     * Flag if project has private layer
     * A project user only see its layer
     */
    boolean privateLayer = false

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
    boolean retrievalDisable = false

    /**
     * Flag for retrieval search on all ontologies
     * If true, search similar annotations on all project that share the same ontology
     */
    boolean retrievalAllOntology = true

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
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static Project createFromData(def json) {
        def project = new Project()
        try {project.id = json.id} catch (Exception e) {}
        insertDataIntoDomain(project, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */           
    static Project insertDataIntoDomain(def domain, def json) {
        domain.name = JSONUtils.getJSONAttrStr(json, 'name',true)
        domain.ontology = JSONUtils.getJSONAttrDomain(json, "ontology", new Ontology(), true)
        domain.discipline = JSONUtils.getJSONAttrDomain(json, "discipline", new Discipline(), false)

        domain.countAnnotations = JSONUtils.getJSONAttrLong(json, 'countAnnotations', 0)
        domain.countImages = JSONUtils.getJSONAttrLong(json, 'countImages', 0)
        domain.countJobAnnotations = JSONUtils.getJSONAttrLong(json, 'countJobAnnotations', 0)

        domain.retrievalDisable = JSONUtils.getJSONAttrBoolean(json, 'retrievalDisable', false)
        domain.retrievalAllOntology = JSONUtils.getJSONAttrBoolean(json, 'retrievalAllOntology', true)
        domain.privateLayer = JSONUtils.getJSONAttrBoolean(json, 'privateLayer', false)
        domain.created = JSONUtils.getJSONAttrDate(json, 'created')
        domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')

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
        Logger.getLogger(this).info("Register custom JSON renderer for " + Project.class)
        JSON.registerObjectMarshaller(Project) { project ->
            def returnArray = [:]
            returnArray['class'] = project.class
            returnArray['id'] = project.id
            returnArray['name'] = project.name
            returnArray['ontology'] = project.ontology?.id
            returnArray['ontologyName'] = project.ontology?.name
            returnArray['discipline'] = project.discipline?.id
            returnArray['privateLayer'] = (project.privateLayer != null &&  project.privateLayer)
            returnArray['disciplineName'] = project.discipline?.name
            returnArray['numberOfSlides'] = project.countSamples()
            returnArray['numberOfImages'] = project.countImageInstance()
            returnArray['numberOfAnnotations'] = project.countAnnotations()
			returnArray['numberOfJobAnnotations'] = project.countJobAnnotations()
            returnArray['retrievalProjects'] = project.retrievalProjects.collect { it.id }
            returnArray['numberOfReviewedAnnotations'] = project.countReviewedAnnotations
            returnArray['retrievalDisable'] = project.retrievalDisable
            returnArray['retrievalAllOntology'] = project.retrievalAllOntology
            returnArray['created'] = project.created?.time?.toString()
            returnArray['updated'] = project.updated?.time?.toString()
            return returnArray
        }
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
     * Get the project link with this domain type
     * @return Project of this domain
     */
     public Project projectDomain() {
        return this
    }
}
