package be.cytomine.security

import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.ontology.Ontology
import be.cytomine.processing.SoftwareProject
import grails.converters.JSON

class User extends SecUser {

    transient springSecurityService

    def projectService

    String firstname
    String lastname
    String email
    String color
    String skypeAccount

    int transaction

    static constraints = {
        firstname blank: false
        lastname blank: false
        skypeAccount(nullable: true, blank:false)
        email(blank: false, email: true)
        color(blank: false, nullable: true)
    }

  static mapping = {
      id(generator: 'assigned', unique: true)
  }


    static hasMany = [softwareProjects: SoftwareProject]


    String realUsername() {
        return username
    }

    String toString() {
        firstname + " " + lastname
    }

    def userGroups() {
        UserGroup.findAllByUser(this)
    }

    def groups() {
        return userGroups().collect {
            it.group
        }
    }

    def ontologies() {
        def ontologies = []
        //add ontology created by this user
        
        if (this.version != null) ontologies.addAll(Ontology.findAllByUser(this))
        
        println "User ontolgy =" + ontologies
        //add ontology from project which can be view by this user
        def project = this.projects();
        println "User project =" + project
        project.each { proj ->
            Ontology ontology = proj.ontology
            if (!ontologies.contains(ontology))
                ontologies << ontology
        }
        ontologies
    }

    //TODO: change this by security!!!!
    def projects() {
//        def c = ProjectGroup.createCriteria()
//        def userGroup = userGroups()
//        if (userGroup == null || userGroup.size() == 0) return []
//        def projects = c {
//            inList("group.id", userGroup.collect {it.groupId})
//            projections {
//                groupProperty("project")
//            }
//        }
        projectService.list()
    }

    def abstractimages() {
        def abstractImages = []
        def userGroup = userGroups()
        if (userGroup.size() > 0) {
            abstractImages = AbstractImageGroup.createCriteria().list {
                inList("group.id", userGroup.collect {it.group.id})
                projections {
                    groupProperty('abstractimage')
                }
            }
        }
        abstractImages

    }


    def abstractimage(int max, int first, String col, String order, String filename, Date dateAddedStart, Date dateAddedStop) {
        def userGroup = userGroups()
        AbstractImage.createCriteria().list(offset: first, max: max, sort: col, order: order) {
            inList("id", AbstractImageGroup.createCriteria().list {
                inList("group.id", userGroup.collect {it.group.id})
                projections {
                    groupProperty('abstractimage.id')
                }
            })
            projections {
                groupProperty('abstractimage')
            }
            ilike("filename", "%" + filename + "%")
            between('created', dateAddedStart, dateAddedStop)

        }

    }

    def slides() {
        def userGroup = userGroups()
        AbstractImage.createCriteria().list {
            inList("id", AbstractImageGroup.createCriteria().list {
                inList("group.id", userGroup.collect {it.group.id})
                projections {
                    groupProperty('abstractimage.id')
                }
            })
            projections {
                groupProperty('slide')
            }
        }
    }

    def slides(int max, int first, String col, String order) {
        def userGroup = userGroups()
        AbstractImage.createCriteria().list(offset: first, max: max, sort: col, order: order) {
            inList("id", AbstractImageGroup.createCriteria().list {
                inList("group.id", userGroup.collect {it.group.id})
                projections {
                    groupProperty('abstractimage.id')
                }
            })
            projections {
                groupProperty('slide')
            }
        }
    }

    boolean algo() {
        return false
    }

    static User getFromData(User user, jsonUser) {
        user.username = jsonUser.username
        user.firstname = jsonUser.firstname
        user.lastname = jsonUser.lastname
        user.email = jsonUser.email
        user.color = jsonUser.color
        user.password = jsonUser.password
        user.enabled = true
        user.generateKeys()
         user.created = (!jsonUser.created.toString().equals("null"))  ? new Date(Long.parseLong(jsonUser.created.toString())) : null
         user.updated = (!jsonUser.updated.toString().equals("null"))  ? new Date(Long.parseLong(jsonUser.updated.toString())) : null
        return user;
    }

    static User createFromData(data) {
        getFromData(new User(), data)
    }

    static User createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + User.class
        JSON.registerObjectMarshaller(User) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['username'] = it.username
            returnArray['firstname'] = it.firstname
            returnArray['lastname'] = it.lastname
            returnArray['email'] = it.email
            returnArray['password'] = "******"
            if (it.id == it.springSecurityService.principal?.id) {
                returnArray['publicKey'] = it.publicKey
                returnArray['privateKey'] = it.privateKey
            }
            returnArray['color'] = it.color
            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null
            return returnArray
        }
    }

    def beforeInsert() {
      println "User.beforeInsert()"
        super.beforeInsert()
    }

    def beforeUpdate() {
        super.beforeUpdate()
    }




}
