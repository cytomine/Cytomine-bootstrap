package be.cytomine.project

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException

import be.cytomine.command.Command
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Ontology
import be.cytomine.processing.ImageFilterProject
import be.cytomine.processing.SoftwareProject

import grails.converters.JSON
import be.cytomine.Exception.AlreadyExistException

class Project extends CytomineDomain {

    def securityService
    def aclUtilService

    String name
    Ontology ontology
    Discipline discipline

    long countAnnotations
    long countImages

    static belongsTo = [ontology: Ontology]
    static hasMany = [projectGroup: ProjectGroup, commands: Command, softwareProjects: SoftwareProject, imageFilterProjects: ImageFilterProject]


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
        //ImageInstance.countByProject(this)
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
        Annotation.findAllByProject(this)
    }

    def countAnnotations() {
        countAnnotations  //may return null
        //def images = this.imagesinstance()
        //images.size() > 0 ? Annotation.countByImageInList(images) : 0
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

    def users() {
        securityService.getUserList(this)
        //UserGroup.findAllByGroupInList(this.groups()).collect { it.user }.unique() //not optimal but okay for users&groups

//        List<User> users = []
//        println "1"
//        try {
//            println "2"
//            def acl = aclUtilService.readAcl(this)
//            println "3"
//            acl.entries.each { entry ->
//                users.add(User.findByUsername(entry.sid.getPrincipal()))
//            }
//        }
//        catch (org.springframework.security.acls.model.NotFoundException e) {
//            println "4"
//            println e
//            println "5"
//        }
//        catch (RuntimeException e) {
//            println "6"
//            println e
//            println "7"
//        }
//        println "8"
//        return users
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

        return project;
    }

    def getIdOntology() {
//        if (this.ontologyId) return this.ontologyId
//        else return this.ontology?.id
        return this.ontology?.id
    }

    def getIdDiscipline() {
//        if (this.disciplineId) return this.disciplineId
//        else return this.discipline?.id
        return this.discipline?.id
    }

    def creator() {
       securityService.getCreator(this)
//        User user
//        try {
//        Acl acl = aclUtilService.readAcl(domain)
//        def owner = acl.getOwner()
//            user = User.findByUsername(owner.getPrincipal())
//         } catch (org.springframework.security.acls.model.NotFoundException e) {e.printStackTrace()}
//        return user

    }

    def admins() {
        securityService.getAdminList(this)
//        List<User> users = []
//        try {
//            def acl = aclUtilService.readAcl(domain)
//            acl.entries.each { entry ->
//                if (entry.permission.equals(ADMINISTRATION))
//                    users.add(User.findByUsername(entry.sid.getPrincipal()))
//            }
//        } catch (org.springframework.security.acls.model.NotFoundException e) {e.printStackTrace()}
//        return users
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + Project.class
        JSON.registerObjectMarshaller(Project) { project ->
            def returnArray = [:]
            returnArray['class'] = project.class
            returnArray['id'] = project.id
            returnArray['name'] = project.name
            returnArray['ontology'] = project.getIdOntology()
            returnArray['ontologyName'] = project.ontology ? project.ontology.name : null
            returnArray['discipline'] = project.getIdDiscipline()
            returnArray['disciplineName'] = project.discipline ? project.discipline.name : null
            try {returnArray['creator'] = project.creator().id} catch (Exception e) {println "creator:"+e}
            try {returnArray['admins'] = project.admins().collect { it.id }} catch (Exception e) {println "admins:"+e}
            try {returnArray['users'] = project.users().collect { it.id } } catch (Exception e) {println "users:"+e}
            try {returnArray['numberOfSlides'] = project.countSlides()} catch (Exception e) {returnArray['numberOfSlides'] = -1}
            try {returnArray['numberOfImages'] = project.countImageInstance()} catch (Exception e) {returnArray['numberOfImages'] = -1}
            try {returnArray['numberOfAnnotations'] = project.countAnnotations()} catch (Exception e) {e.printStackTrace(); returnArray['numberOfAnnotations'] = -1}
            returnArray['created'] = project.created ? project.created.time.toString() : null
            returnArray['updated'] = project.updated ? project.updated.time.toString() : null
            return returnArray
        }
    }
}
