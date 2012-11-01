package be.cytomine.security

import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.ontology.Ontology
import be.cytomine.processing.SoftwareProject
import grails.converters.JSON
import org.apache.log4j.Logger

class User extends SecUser {

    transient springSecurityService

    def projectService

    String firstname
    String lastname
    String email
    String color
    String skypeAccount
    String sipAccount

    int transaction

    static constraints = {
        firstname blank: false
        lastname blank: false
        skypeAccount(nullable: true, blank:false)
        sipAccount(nullable: true, blank:false)
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
        //add ontology from project which can be view by this user
        def project = this.projects();
        project.each { proj ->
            Ontology ontology = proj.ontology
            if (!ontologies.contains(ontology))
                ontologies << ontology
        }
        ontologies
    }

    def projects() {
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

    def samples() {
        def userGroup = userGroups()
        AbstractImage.createCriteria().list {
            inList("id", AbstractImageGroup.createCriteria().list {
                inList("group.id", userGroup.collect {it.group.id})
                projections {
                    groupProperty('abstractimage.id')
                }
            })
            projections {
                groupProperty('sample')
            }
        }
    }

    def samples(int max, int first, String col, String order) {
        def userGroup = userGroups()
        AbstractImage.createCriteria().list(offset: first, max: max, sort: col, order: order) {
            inList("id", AbstractImageGroup.createCriteria().list {
                inList("group.id", userGroup.collect {it.group.id})
                projections {
                    groupProperty('abstractimage.id')
                }
            })
            projections {
                groupProperty('sample')
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
        user.skypeAccount = jsonUser.skypeAccount != null ? jsonUser.skypeAccount : null
        user.sipAccount = jsonUser.sipAccount != null ? jsonUser.sipAccount : null
        if (jsonUser.password && user.password != null) {
            user.newPassword = jsonUser.password //user updated
        } else if (jsonUser.password) {
            user.password = jsonUser.password //user created
        }
        user.enabled = true
        if (user.getPublicKey() == null || user.getPrivateKey() == null || jsonUser.publicKey == "" || jsonUser.privateKey == "") {
            user.generateKeys()
        }
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
        Logger.getLogger(this).info("Register custom JSON renderer for " + User.class)
        JSON.registerObjectMarshaller(User) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['username'] = it.username
            returnArray['firstname'] = it.firstname
            returnArray['lastname'] = it.lastname
            returnArray['email'] = it.email
            returnArray['sipAccount'] = it.sipAccount
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
        println "beforeInsert.user"
        super.beforeInsert()
    }

    def beforeUpdate() {
        super.beforeUpdate()
    }




}
