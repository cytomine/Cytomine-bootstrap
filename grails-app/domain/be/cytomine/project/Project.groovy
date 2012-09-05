package be.cytomine.project

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.command.Command
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Ontology
import be.cytomine.processing.ImageFilterProject
import be.cytomine.processing.SoftwareProject
import be.cytomine.security.SecUser
import grails.converters.JSON

class Project extends CytomineDomain {

    def securityService

    String name
    Ontology ontology
    Discipline discipline
    boolean privateLayer = false
    long countAnnotations
	long countJobAnnotations
    long countImages

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
        Annotation.createCriteria().list {
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

    def slides() {
        ImageInstance.createCriteria().list {
            join 'slide'
            projections {
                groupProperty('slide')
            }
            eq("project", this)
        }
    }

    def countSlides() {
        def query = ImageInstance.createCriteria().list {
            join 'slide'
            projections {
                countDistinct('slide.id')
            }
            eq("project", this)
        }
        query[0]
    }

    def groups() {
        projectGroup.collect { it.group }
    }

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

    static Project createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static Project createFromData(jsonProject) {
        def project = new Project()
        getFromData(project, jsonProject)
    }

    static Project getFromData(project, jsonProject) {
        String name = jsonProject.name.toString()
        if (!name.equals("null"))
            project.name = jsonProject.name.toUpperCase()
        else throw new WrongArgumentException("Project name cannot be null")

        if (jsonProject.ontology)
            project.ontology = Ontology.read(jsonProject.ontology)

        if (!jsonProject.discipline.toString().equals("null"))
            project.discipline = Discipline.read(jsonProject.discipline)

        try {project.countAnnotations = Long.parseLong(jsonProject.numberOfAnnotations.toString()) } catch (Exception e) {
            project.countAnnotations = 0
        }
        try {project.countImages = Long.parseLong(jsonProject.numberOfImages.toString()) } catch (Exception e) {
            project.countImages = 0
        }
        if(!jsonProject.retrievalDisable.toString().equals("null")) project.retrievalDisable = Boolean.parseBoolean(jsonProject.retrievalDisable.toString())
        if(!jsonProject.retrievalAllOntology.toString().equals("null")) project.retrievalAllOntology = Boolean.parseBoolean(jsonProject.retrievalAllOntology.toString())

        return project;
    }

    def creator() {
        securityService.getCreator(this)
    }

    def admins() {
        securityService.getAdminList(this)
    }

    def users() {
        securityService.getUserList(this)
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + Project.class
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
            try {returnArray['creator'] = project.creator().id} catch (Exception e) {println "creator:"+e}
            try {returnArray['admins'] = project.admins().collect { it.id }} catch (Exception e) {println "admins:"+e}
            try {returnArray['users'] = project.users().collect { it.id } } catch (Exception e) {println "users:"+e}
            try {returnArray['userLayers'] = project.userLayers().collect {it.id} } catch (Exception e) {println "userLayers:"+e}
            try {returnArray['numberOfSlides'] = project.countSlides()} catch (Exception e) {returnArray['numberOfSlides'] = -1}
            try {returnArray['numberOfImages'] = project.countImageInstance()} catch (Exception e) {returnArray['numberOfImages'] = -1}
            try {returnArray['numberOfAnnotations'] = project.countAnnotations()} catch (Exception e) {e.printStackTrace(); returnArray['numberOfAnnotations'] = -1}
			try {returnArray['numberOfJobAnnotations'] = project.countJobAnnotations()} catch (Exception e) {e.printStackTrace(); returnArray['numberOfJobAnnotations'] = -1}
            try {returnArray['retrievalProjects'] = project.retrievalProjects.collect { it.id } } catch (Exception e) {println "users:"+e}

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
