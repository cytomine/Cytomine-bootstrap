package be.cytomine.project

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.command.Command
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.UserAnnotation
import be.cytomine.processing.ImageFilterProject
import be.cytomine.processing.SoftwareProject
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.apache.log4j.Logger

class Project extends CytomineDomain implements Serializable {

    def securityService

    String name
    Ontology ontology
    Discipline discipline
    boolean privateLayer = false
    long countAnnotations
	long countJobAnnotations
    long countImages
    long countReviewedAnnotations

    boolean retrievalDisable = false
    boolean retrievalAllOntology = true

    static belongsTo = [ontology: Ontology]
    static hasMany = [commands: Command, softwareProjects: SoftwareProject, imageFilterProjects: ImageFilterProject, retrievalProjects : Project]


    static constraints = {
        name(maxSize: 150, unique: true, blank: false) //, validator: {
        discipline(nullable: true)
        //  return !Project.findByNameIlike(it)
        //})
    }

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

    def imagesinstance() {
        ImageInstance.findAllByProject(this)
    }

    def countImageInstance() {
        countImages//may return null
    }

    def abstractimages() {
        ImageInstance.createCriteria().list {
            eq("project", this)
            projections {
                groupProperty("baseImage")
            }
        }
    }

    def annotations() {
        UserAnnotation.createCriteria().list {
            eq("project", this)
            inList("user", this.userLayers())
        }
    }

    def countAnnotations() {
        countAnnotations  //may return null
    }

	def countJobAnnotations() {
        countJobAnnotations  //may return null
    }

    def samples() {
        ImageInstance.createCriteria().list {
            join 'sample'
            projections {
                groupProperty('sample')
            }
            eq("project", this)
        }
    }

    def countSlides() {
        def query = ImageInstance.createCriteria().list {
            join 'sample'
            projections {
                countDistinct('sample.id')
            }
            eq("project", this)
        }
        query[0]
    }

    def groups() {
        projectGroup.collect { it.group }
    }

    //TODO:: remove (move in userService)
    def userLayers() {
        Collection<SecUser> users = securityService.getUserList(this)
        SecUser currentUser = cytomineService.getCurrentUser()
        if (this.privateLayer && users.contains(currentUser)) {
            return [currentUser]
        } else if (!this.privateLayer) {
            return  users
        } else { //should no arrive but possible if user is admin and not in project
            []
        }
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static Project createFromDataWithId(def json) {
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
    static Project createFromData(def json) {
        def project = new Project()
        insertDataIntoDomain(project, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */           
    static Project insertDataIntoDomain(def domain, def json) {
        String name = json.name.toString()
        if (!name.equals("null"))
            domain.name = json.name.toUpperCase()
        else throw new WrongArgumentException("Project name cannot be null")

        if (json.ontology)
            domain.ontology = Ontology.read(json.ontology)

        if (!json.discipline.toString().equals("null"))
            domain.discipline = Discipline.read(json.discipline)

        try {domain.countAnnotations = Long.parseLong(json.numberOfAnnotations.toString()) } catch (Exception e) {
            domain.countAnnotations = 0
        }
        try {domain.countImages = Long.parseLong(json.numberOfImages.toString()) } catch (Exception e) {
            domain.countImages = 0
        }
        if(!json.retrievalDisable.toString().equals("null")) domain.retrievalDisable = Boolean.parseBoolean(json.retrievalDisable.toString())
        if(!json.retrievalAllOntology.toString().equals("null")) domain.retrievalAllOntology = Boolean.parseBoolean(json.retrievalAllOntology.toString())

        return domain;
    }

    //TODO:: remove (move in userService)
    def creator() {
        securityService.getCreator(this)
    }

    //TODO:: remove (move in userService)
    def admins() {
        securityService.getAdminList(this)
    }

    //TODO:: remove (move in userService)
    def users() {
        securityService.getUserList(this)
    }


    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Project.class)
        JSON.registerObjectMarshaller(Project) { project ->
            def returnArray = [:]
            returnArray['class'] = project.class
            returnArray['id'] = project.id
            returnArray['name'] = project.name
            returnArray['ontology'] = project.ontology?.id
            returnArray['ontologyName'] = project.ontology ? project.ontology.name : null
            returnArray['discipline'] = project.discipline?.id
            returnArray['privateLayer'] = (project.privateLayer != null &&  project.privateLayer)
            returnArray['disciplineName'] = project.discipline ? project.discipline.name : null
            try {returnArray['numberOfSlides'] = project.countSlides()} catch (Exception e) {returnArray['numberOfSlides'] = -1}
            try {returnArray['numberOfImages'] = project.countImageInstance()} catch (Exception e) {returnArray['numberOfImages'] = -1}
            try {returnArray['numberOfAnnotations'] = project.countAnnotations()} catch (Exception e) { returnArray['numberOfAnnotations'] = -1}
			try {returnArray['numberOfJobAnnotations'] = project.countJobAnnotations()} catch (Exception e) { returnArray['numberOfJobAnnotations'] = -1}
            try {returnArray['retrievalProjects'] = project.retrievalProjects.collect { it.id } } catch (Exception e) {log.info "users:"+e}

            returnArray['numberOfReviewedAnnotations'] = project.countReviewedAnnotations
            returnArray['retrievalDisable'] = project.retrievalDisable
            returnArray['retrievalAllOntology'] = project.retrievalAllOntology

            returnArray['created'] = project.created ? project.created.time.toString() : null
            returnArray['updated'] = project.updated ? project.updated.time.toString() : null
            return returnArray
        }
    }

    public boolean equals(Object o) {
        if (!o) return false
        try {return ((Project) o).getId() == this.getId()} catch (Exception e) { return false}
        //if no try/catch, when getting term from ontology => GroovyCastException: Cannot cast object 'null' with class 'org.codehaus.groovy.grails.web.json.JSONObject$Null' to class 'be.cytomine.ontology.Term'
    }
}
