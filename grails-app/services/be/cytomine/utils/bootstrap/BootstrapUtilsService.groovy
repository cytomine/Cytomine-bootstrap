package be.cytomine.utils.bootstrap

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.Mime
import be.cytomine.image.UploadedFile
import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.ImageServerStorage
import be.cytomine.image.server.MimeImageServer
import be.cytomine.image.server.RetrievalServer
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.middleware.MessageBrokerServer
import be.cytomine.ontology.Property
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.security.Group
import be.cytomine.security.SecRole
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.utils.JSONUtils
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.sql.Sql

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
    def grailsApplication
    def dataSource

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
            if(!Mime.findByMimeType(it.mimeType)) {
                log.info "*********************************"
                log.info "extension="+it.extension
                log.info "mimeType="+it.mimeType
                log.info "*********************************"
                log.info Mime.list().collect{it.extension + "=" + it.mimeType}.join("\n")
                log.info "*********************************"
                Mime mime = new Mime(extension : it.extension, mimeType: it.mimeType)
                if (mime.validate()) {
                    mime.save(flush:true)
                } else {
                    mime.errors?.each {
                        println it
                    }
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

    def addMimePyrTiff() {
        def mimeSamples = [
                [extension : 'tif', mimeType : 'image/pyrtiff']
        ]
        createMimes(mimeSamples)
        createMimeImageServers(ImageServer.findAll(), mimeSamples)
    }

    def addMimeVentanaTiff() {
        def mimeSamples = [
                [extension : 'tif', mimeType : 'openslide/ventana']
        ]
        createMimes(mimeSamples)
        createMimeImageServers(ImageServer.findAll(), mimeSamples)
    }

    def addMimePhilipsTiff() {
        def mimeSamples = [
                [extension : 'tif', mimeType : 'philips/tif']
        ]
        createMimes(mimeSamples)
        createMimeImageServers(ImageServer.findAll(), mimeSamples)
    }

    def createNewIS2() {
        if (ImageServer.count() > 1) return

        (1..10).each { id->
            log.info "cerate image server $id"
            def IIPImageServer = [className : 'IIPResolver', name : "IIP$id", service : '/image/tile', url : "http://image$id"+".cytomine.be", available : true]
            ImageServer imageServer = new ImageServer(
                    className: IIPImageServer.className,
                    name: IIPImageServer.name,
                    service : IIPImageServer.service,
                    url : IIPImageServer.url,
                    available : IIPImageServer.available
            )
            if (imageServer.validate()) {
                imageServer.save()
            } else {
                imageServer.errors?.each {
                    println it
                }
            }

            log.info "add all storage to IS $id"
            Storage.list().each {
                new ImageServerStorage(
                        storage : it,
                        imageServer: imageServer
                ).save()
            }

            Mime.list().each {
                new MimeImageServer(
                        mime : it,
                        imageServer: imageServer
                ).save()
            }
        }
    }

    def createMultipleRetrieval() {
        RetrievalServer.list().each { server ->
            if(!grailsApplication.config.grails.retrievalServerURL.contains(server.url)) {
                log.info server.url + " is not in config, drop it"
                log.info "delete Retrieval $server"
                server.delete()
            }

        }
        grailsApplication.config.grails.retrievalServerURL.eachWithIndex { it, index ->

            if(!RetrievalServer.findByUrl(it)) {
                RetrievalServer server = new RetrievalServer(description:"retrieval $index", url:"${it}",path:'/retrieval-web/api/resource.json')
                if (server.validate()) {
                    server.save()
                } else {
                    server.errors?.each {
                        println it
                    }
                }
            }

        }
    }

    def createMessageBrokerServer() {
        MessageBrokerServer.list().each { messageBroker ->
            if(!grailsApplication.config.grails.messageBrokerServerURL.contains(messageBroker.host)) {
                log.info messageBroker.host + "is not in config, drop it"
                log.info "delete Message Broker Server " + messageBroker
                messageBroker.delete()
            }
        }

        String messageBrokerURL = grailsApplication.config.grails.messageBrokerServerURL
        def splittedURL = messageBrokerURL.split(':')

        if(!MessageBrokerServer.findByHost(splittedURL[0])) {
            MessageBrokerServer mbs = new MessageBrokerServer(name: "MessageBrokerServer", host: splittedURL[0], port: splittedURL[1].toInteger())
            if (mbs.validate()) {
                mbs.save()
            } else {
                mbs.errors?.each {
                    println it
                }
            }
        }
        MessageBrokerServer.findByHost(splittedURL[0])
    }

    def createMultipleIS() {



        ImageServer.list().each { server ->
            if(!grailsApplication.config.grails.imageServerURL.contains(server.url)) {
                log.info server.url + " is not in config, drop it"
                MimeImageServer.findAllByImageServer(server).each {
                    log.info "delete $it"
                    it.delete()
                }

                ImageServerStorage.findAllByImageServer(server).each {
                    log.info "delete $it"
                    it.delete()
                }
                log.info "delete IS $server"
                server.delete()
            }

        }



        grailsApplication.config.grails.imageServerURL.eachWithIndex { it, index ->
            createNewIS(index+"",it)
        }
    }


    def createNewIS(String name = "", String url) {

        println "*************** createNewIS ********************"
        println name + "====> " + url

        if(!ImageServer.findByUrl(url)) {

//            MimeImageServer.list().each {
//                it.delete()
//            }
//
//            ImageServerStorage.list().each {
//                it.delete()
//            }
//
//            ImageServer.list().each {
//                it.delete()
//            }
            def IIPImageServer = [className : 'IIPResolver', name : 'IIP'+name, service : '/image/tile', url : url, available : true]
            ImageServer imageServer = new ImageServer(
                    className: IIPImageServer.className,
                    name: IIPImageServer.name,
                    service : IIPImageServer.service,
                    url : IIPImageServer.url,
                    available : IIPImageServer.available
            )

            if (imageServer.validate()) {
                imageServer.save()
            } else {
                imageServer.errors?.each {
                    println it
                }
            }

            Storage.list().each {
                new ImageServerStorage(
                        storage : it,
                        imageServer: imageServer
                ).save()
            }

            Mime.list().each {
                new MimeImageServer(
                        mime : it,
                        imageServer: imageServer
                ).save()
            }
        } else {
            println url + " already exist"
        }


    }

    def transfertProperty() {
        SpringSecurityUtils.reauthenticate "admin", null
        def ips = ImageProperty.list()
        ips.eachWithIndex { ip,index ->
            ip.attach()
            Property property = new Property(domainIdent: ip.image.id, domainClassName: AbstractImage.class.name,key:ip.key,value:ip.value)
            property.save(failOnError: true)
            ip.delete()
            if(index%500==0) {
                log.info "Image property ${(index/ips.size())*100}"
                cleanUpGorm()
            }
        }
    }

    def checkImages2() {
        SpringSecurityUtils.reauthenticate "admin", null
        def currentUser = cytomineService.getCurrentUser()

        def uploadedFiles = UploadedFile.findAllByPathLike("notfound").plus(UploadedFile.findAllByPathLike("/tmp/cytomine_buffer/")).plus(UploadedFile.findAllByPathLike("/tmp/imageserver_buffer"))

        uploadedFiles.eachWithIndex { uploadedFile,index->
            if(index%1==0) {
                log.info "Check ${(index/uploadedFiles.size())*100}"
                cleanUpGorm()
            }

            uploadedFile.attach()
            AbstractImage abstractImage = uploadedFile.image
            if (!abstractImage) { //
                UploadedFile parentUploadedFile = uploadedFile
                int max = 10
                while (parentUploadedFile.parent && !abstractImage && max <10) {
                    parentUploadedFile.attach()
                    parentUploadedFile.parent.attach()
                    parentUploadedFile = parentUploadedFile.parent
                    abstractImage = parentUploadedFile.image
                    max++
                }
            }
            if (!abstractImage) {
                log.error "DID NOT FIND AN ABSTRACT_IMAGE for uploadedFile $uploadedFile"
            } else {
                def data = StorageAbstractImage.findByAbstractImage(abstractImage)
                if(data) {
                    Storage storage = data.storage
                    uploadedFile.path = storage.getBasePath()
                    uploadedFile = uploadedFile.save()
                }

            }
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
                uploadedFile.mimeType = "image/pyrtiff"
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
