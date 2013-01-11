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

    //TODO: ro remove!
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





    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static User createFromDataWithId(def json) {
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
    static User createFromData(def json) {
        insertDataIntoDomain(new User(), json)
    }
    
    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */         
    static User insertDataIntoDomain(def domain, def json) {
        domain.username = json.username
        domain.firstname = json.firstname
        domain.lastname = json.lastname
        domain.email = json.email
        domain.color = json.color
        domain.skypeAccount = json.skypeAccount != null ? json.skypeAccount : null
        domain.sipAccount = json.sipAccount != null ? json.sipAccount : null
        if (json.password && domain.password != null) {
            domain.newPassword = json.password //user updated
        } else if (json.password) {
            domain.password = json.password //user created
        }
        domain.enabled = true
        if (domain.getPublicKey() == null || domain.getPrivateKey() == null || json.publicKey == "" || json.privateKey == "") {
            domain.generateKeys()
        }
        domain.created = (!json.created.toString().equals("null"))  ? new Date(Long.parseLong(json.created.toString())) : null
        domain.updated = (!json.updated.toString().equals("null"))  ? new Date(Long.parseLong(json.updated.toString())) : null
        return domain;
    }    

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
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

            returnArray['algo'] = it.algo()
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
