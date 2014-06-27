package be.cytomine.utils.bootstrap

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.Mime
import be.cytomine.image.UploadedFile
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.MimeImageServer
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.security.Group
import be.cytomine.security.SecRole
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/03/13
 * Time: 11:59
 */
class BootstrapUtilsService {

    def cytomineService
    def sessionFactory
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    public def createUsers(def usersSamples) {

        SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority: "ROLE_USER").save(flush: true)
        SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority: "ROLE_ADMIN").save(flush: true)
        SecRole.findByAuthority("ROLE_SUPER_ADMIN") ?: new SecRole(authority: "ROLE_SUPER_ADMIN").save(flush: true)
        SecRole.findByAuthority("ROLE_GUEST") ?: new SecRole(authority: "ROLE_GUEST").save(flush: true)

        def usersCreated = []
        usersSamples.each { item ->
            User user = User.findByUsername(item.username)
            if (user)  return
            user = new User(
                    username: item.username,
                    firstname: item.firstname,
                    lastname: item.lastname,
                    email: item.email,
                    color: item.color,
                    password: item.password,
                    enabled: true)
            user.generateKeys()


            log.info "Before validating ${user.username}..."
            if (user.validate()) {
                log.info "Creating user ${user.username}..."

                try {user.save(flush: true) } catch(Exception e) {println e}
                log.info "Save ${user.username}..."

                usersCreated << user

                /* Add Roles */
                item.roles.each { authority ->
                    log.info "Add SecRole " + authority + " for user " + user.username
                    SecRole secRole = SecRole.findByAuthority(authority)
                    if (secRole) SecUserSecRole.create(user, secRole)
                }

            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                user.errors.each {
                    err -> log.info(err)
                }
            }
        }
        return usersCreated
    }

    public def createGroups(groupsSamples) {
        groupsSamples.each { item ->
            if (Group.findByName(item.name)) return
            def group = new Group(name: item.name)
            if (group.validate()) {
                log.info("Creating group ${group.name}...")
                group.save(flush: true)
                log.info("Creating group ${group.name}... OK")
            }
            else {
                log.info("\n\n\n Errors in group boostrap for ${item.name}!\n\n\n")
                group.errors.each {
                    err -> log.info err
                }
            }
        }
    }

    public def createRelation() {
        def relationSamples = [
                [name: RelationTerm.names.PARENT],
                [name: RelationTerm.names.SYNONYM]
        ]

        log.info("createRelation")
        relationSamples.each { item ->
            if (Relation.findByName(item.name)) return
            def relation = new Relation(name: item.name)
            log.info("create relation=" + relation.name)

            if (relation.validate()) {
                log.info("Creating relation : ${relation.name}...")
                relation.save(flush: true)

            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                relation.errors.each {
                    err -> log.info err
                }

            }
        }
    }

    public def createImageServers(def imageServersSamples) {
        imageServersSamples.each {
            ImageServer imageServer = new ImageServer(
                    className: it.className,
                    name: it.name,
                    service : it.service,
                    url : it.url,
                    available : it.available
            )

            if (imageServer.validate()) {
                imageServer.save()
            } else {
                imageServer.errors?.each {
                    println it
                }
            }

        }

    }

    public def createMimes(def mimeSamples) {
        mimeSamples.each {
            Mime mime = new Mime(extension : it.extension, mimeType: it.mimeType)
            if (mime.validate()) {
                mime.save()
            } else {
                mime.errors?.each {
                    println it
                }
            }
        }
    }

    public def createMimeImageServers(def imageServerCollection, def mimeCollection) {
        println imageServerCollection
        println ImageServer.list()
        imageServerCollection.each {
            ImageServer imageServer = ImageServer.findByName(it.name)
            if (imageServer) {
                mimeCollection.each {
                    Mime mime = Mime.findByMimeType(it.mimeType)
                    if (mime) {
                        new MimeImageServer(
                                mime : mime,
                                imageServer: imageServer
                        ).save()
                    }
                }
            }
        }
    }

    def saveDomain(def newObject, boolean flush = true) {
        if (!newObject.validate()) {
            log.error newObject.errors
            log.error newObject.retrieveErrors().toString()
            throw new WrongArgumentException(newObject.retrieveErrors().toString())
        }
        if (!newObject.save(flush: flush)) {
            throw new InvalidRequestException(newObject.retrieveErrors().toString())
        }
    }

    def checkImages() {
        SpringSecurityUtils.reauthenticate "admin", null
        def currentUser = cytomineService.getCurrentUser()

        List<AbstractImage> ok = []
        List<AbstractImage> notok = []
        def list = AbstractImage.findAll()
        list.eachWithIndex { abstractImage,index->
            if(index%500==0) {
                log.info "Check ${(index/list.size())*100}"
                cleanUpGorm()
            }

            if (UploadedFile.findByImage(abstractImage)) {
                ok << abstractImage
            } else {
                notok << abstractImage
            }
        }

        notok.eachWithIndex { abstractImage, index ->
            abstractImage.attach()
            UploadedFile uploadedFile = UploadedFile.findByFilename(abstractImage.getPath())
            SecUser user = abstractImage.user ? abstractImage.user : currentUser
            if (!uploadedFile) {
                def imageServerStorage = abstractImage.imageServersStorage
                uploadedFile = new UploadedFile(
                        user : user,
                        filename : abstractImage.getPath(),
                        projects: ImageInstance.findAllByBaseImage(abstractImage).collect { it.project.id}.unique(),
                        storages : abstractImage.getImageServersStorage().collect { it.storage.id},
                        originalFilename: abstractImage.getOriginalFilename(),
                        convertedExt: abstractImage.mime.extension,
                        ext: abstractImage.mime.extension,
                        size : 0,
                        path : (imageServerStorage.isEmpty()? "notfound" : imageServerStorage.first().storage.getBasePath()),
                        contentType: abstractImage.mime.mimeType)

                if (!uploadedFile.validate()) {
                    uploadedFile.errors.each {
                        println it
                    }
                } else {
                    uploadedFile = uploadedFile.save()
                }

            }

            uploadedFile.image = abstractImage
            String extension = abstractImage.mime.extension
            if (extension == "tiff" || extension == "tif") {
                uploadedFile.mimeType = "image/tiff"
                uploadedFile.downloadParent = uploadedFile
            }
            else if (extension == "mrxs") {
                uploadedFile.mimeType = "openslide/mrxs"
            }
            else if (extension == "svs") {
                uploadedFile.mimeType = "openslide/svs"
                uploadedFile.downloadParent = uploadedFile
            }
            else if (extension == "scn") {
                uploadedFile.mimeType = "openslide/scn"
                uploadedFile.downloadParent = uploadedFile
            }
            else if (extension == "jp2") {
                uploadedFile.mimeType = "image/jp2"
                uploadedFile.downloadParent = uploadedFile
            }
            else if (extension == "vms") {
                uploadedFile.mimeType = "openslide/vms"
            }
            else if (extension == "ndpi") {
                uploadedFile.mimeType = "openslide/ndpi"
                uploadedFile.downloadParent = uploadedFile
            }
            uploadedFile.save()
            if(index%100==0) {
                log.info "Create upload ${(index/notok.size())*100}"
                cleanUpGorm()
            }
        }
    }

    public void cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }
}
