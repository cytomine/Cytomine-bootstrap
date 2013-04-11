package be.cytomine.utils.bootstrap

import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.image.Mime
import be.cytomine.image.NestedFile
import be.cytomine.image.server.ImageProperty
import be.cytomine.image.server.ImageServer
import be.cytomine.image.server.ImageServerStorage
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/03/13
 * Time: 11:31
 */
class BootstrapProdDataService {


    def grailsApplication

    def termService
    def secUserService
    def permissionService
    def imagePropertiesService
    def abstractImageService
    def fileSystemService

    public def toVersion1() {
        addPermissionsOnOntologyAndSoftware()
        getAbstractImageNestedFiles()
        initUserIntoAbstractImage()
        initUserStorages()
        generateCopyToStorageScript()
    }


    private def  initUserIntoAbstractImage() {
        SecUser defaultUser = SecUser.findByUsername("rmaree")
        AbstractImage.list().each {
            if (!it.user) {
                log.info "abstractImage $it.filename"
                AbstractImageGroup abstractImageGroup = AbstractImageGroup.findByAbstractImage(it)
                if (!abstractImageGroup) {
                    it.user = defaultUser
                } else {
                    ArrayList<UserGroup> userGroups = UserGroup.findAllByGroup(abstractImageGroup.group)
                    if (!userGroups || userGroups.size() < 1) {
                        log.error ("can't find user fom group $abstractImageGroup.group.name")
                        it.user = defaultUser
                    }
                    else {
                        it.user = userGroups.first().user
                    }
                }
                it.save()
            }
        }
    }

    private def generateCopyToStorageScript() {
        String scriptFilename = "/tmp/generateCopyToStorageScript.sh"
        File f = new File(scriptFilename)
        if (f.exists()) {
            f.delete()
            f = new File(scriptFilename)
        }
        //Storage originStorage = Storage.findByName("cytomine")
        String oldStorageBasePath = "/opt/cytomine/storage/beta.cytomine.be/"
        String originPath = null
        Storage destStorage = null
        String destPath = null
        String cmd = null
        String dirParent = null
        AbstractImage.list().each {
            new StorageAbstractImage(storage: Storage.findByUser(it.user), abstractImage: it).save()
        }
        AbstractImage.list().each {
            destStorage = Storage.findByUser(it.user)
            originPath = [oldStorageBasePath, it.getPath()].join(File.separator)
            destPath = [destStorage.getBasePath(), it.getPath()].join(File.separator)
            dirParent = new File(destPath).getParent()
            cmd = "mkdir -p \"$dirParent\";cp \"$originPath\" \"$destPath\";"
            f << cmd
            NestedFile.findAllByAbstractImage(it).each { nestedFile ->
                originPath = [oldStorageBasePath, nestedFile.getFilename()].join(File.separator)
                destPath = [destStorage.getBasePath(), nestedFile.getFilename()].join(File.separator)
                dirParent = new File(destPath).getParent()
                cmd = "mkdir -p \"$dirParent\";cp \"$originPath\" \"$destPath\";"
                f << cmd
            }
        }


    }


    private def getAbstractImageNestedFiles() {

        //VMS files
        AbstractImage.findAllByMime(Mime.findByExtension("vms")).each {
            if (NestedFile.findAllByAbstractImage(it)?.size() > 0) return //already done for this image

            ArrayList<StorageAbstractImage> storageAbstractImage = StorageAbstractImage.findAllByAbstractImage(it)
            if (!storageAbstractImage || storageAbstractImage.size() < 1) {
                log.error "cannot get storage for $it.filename"
                abstractImageService.delete(AbstractImage.read(it.id), null, false)
                return
            }

            log.info "extract nested files of $it.filename"
            ArrayList<ImageProperty> properties = ImageProperty.findAllByKeyLike("%Error%")
            if (properties && properties.size() > 0) {
                imagePropertiesService.clear(it)
                imagePropertiesService.populate(it)
            }
            properties = ImageProperty.findAllByKeyLikeAndImage("hamamatsu.ImageFile%", it)
            String parent = new File(it.getPath()).getParent()
            if (!parent) parent = ""

            properties.each { property ->
                String path = [parent, property.value].join(File.separator)
                log.info "add nested file $path"
                new NestedFile(originalFilename: path, filename: path, abstractImage: it, data: null).save(flush : true)
            }
            //hamamatsu.MacroImage
            ImageProperty property = ImageProperty.findByKeyLikeAndImage("hamamatsu.MacroImage%", it)
            if (property) {
                String path = [parent, property.value].join(File.separator)
                log.info "add nested file $path"
                new NestedFile(originalFilename: path, filename: path, abstractImage: it, data: null).save(flush : true)
            } else {
                log.error "can't file hamamatsu.MacroImage for $it.filename"
            }
            //hamamatsu.MapFile
            property = ImageProperty.findByKeyLikeAndImage("hamamatsu.MapFile%", it)
            if (property) {
                String path = [parent, property.value].join(File.separator)
                log.info "add nested file $path"
                new NestedFile(originalFilename: path, filename: path, abstractImage: it, data: null).save(flush : true)
            } else {
                log.error "can't file hamamatsu.MapFile for $it.filename"
            }
            //hamamatsu.OptimisationFile
            property = ImageProperty.findByKeyLikeAndImage("hamamatsu.OptimisationFile%", it)
            if (property) {
                String path = [parent, property.value].join(File.separator)
                log.info "add nested file $path"
                new NestedFile(originalFilename: path, filename: path, abstractImage: it, data: null).save(flush : true)
            } else {
                log.error "can't file hamamatsu.OptimisationFile for $it.filename"
            }
        }

        //mrxs files
        AbstractImage.findAllByMime(Mime.findByExtension("mrxs")).each {
            if (NestedFile.findAllByAbstractImage(it)?.size() > 0) return //already done for this image

            ArrayList<StorageAbstractImage> storageAbstractImage = StorageAbstractImage.findAllByAbstractImage(it)
            if (!storageAbstractImage || storageAbstractImage.size() < 1) {
                log.error "cannot get storage for $it.filename"
                return
            }

            log.info "extract nested files of $it.filename"
            ArrayList<ImageProperty> properties = ImageProperty.findAllByKeyLike("Error")
            if (properties && properties.size() > 0) {
                imagePropertiesService.clear(it)
                imagePropertiesService.populate(it)
            }
            properties = ImageProperty.findAllByKeyLikeAndImage("mirax.DATAFILE.FILE%", it)

            String fileWithoutExtension = it.getPath().substring(0, it.getPath().length()-5)
            properties.each { property ->
                if (property.key != "mirax.DATAFILE.FILE_COUNT") {
                    String path = [fileWithoutExtension, property.value].join(File.separator)
                    log.info "add nested file -> $path"
                    new NestedFile(originalFilename: path, filename: path, abstractImage: it, data: null).save(flush : true)
                }
            }
            //Slidedat.ini
            String slidedatPath = [fileWithoutExtension, "Slidedat.ini"].join(File.separator)
            log.info "add nested file $slidedatPath"
            new NestedFile(originalFilename: slidedatPath, filename: slidedatPath, abstractImage: it, data: null).save(flush : true)
            //Index.dat
            String index = [fileWithoutExtension, "Index.dat"].join(File.separator)
            log.info "add nested file $index"
            new NestedFile(originalFilename: index, filename: index, abstractImage: it, data: null).save(flush : true)

        }

    }

    private def initUserStorages() {
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken("lrollus", "lR\$2011", AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
        println "initUserStorages"
        //create storage for each user
        for (user in User.findAll()) {
            String storage_base_path = grailsApplication.config.storage_path
            String remotePath = [storage_base_path, user.id.toString()].join(File.separator)
            if (!Storage.findByUser(user)) {

                Storage storage = new Storage(
                        name: "$user.username storage",
                        basePath: remotePath,
                        ip: "10.1.0.106",
                        username: "storage",
                        password: "bioinfo;3u54",
                        keyFile: null,
                        port: 22,
                        user: user
                )

                if (storage.validate()) {
                    storage.save()
                    permissionService.addPermission(storage,user.username,BasePermission.ADMINISTRATION)
                    /*fileSystemService.makeRemoteDirectory(
                            storage.getIp(),
                            storage.getPort(),
                            storage.getUsername(),
                            storage.getPassword(),
                            storage.getKeyFile(),
                            storage.getBasePath())*/

                    for (imageServer in ImageServer.findAll()) {
                        ImageServerStorage imageServerStorage = new ImageServerStorage(imageServer : imageServer, storage : storage)
                        imageServerStorage.save(flush : true)
                    }
                } else {
                    storage.errors.each {
                        log.error it
                    }
                }
            }  else { //update the storage
                Storage storage = Storage.findByUser(user)
                storage.basePath = remotePath
                storage.save()
                permissionService.addPermission(storage,user.username,BasePermission.ADMINISTRATION)
                for (imageServer in ImageServer.findAll()) {
                    ImageServerStorage imageServerStorage = new ImageServerStorage(imageServer : imageServer, storage : storage)
                    imageServerStorage.save(flush : true)
                }
            }

        }
        println "initUserStorages end"
    }

    private def addPermissionsOnOntologyAndSoftware() {
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken("lrollus", "lR\$2011", AuthorityUtils.createAuthorityList('ROLE_ADMIN'))

        Project.withTransaction {
            Project.list().each { project ->
                def users = secUserService.listUsers(project)
                //Ajouter le droit de read a l'ontologie du projet
                users.each { user ->
                    permissionService.addPermission(project.ontology,user.username,BasePermission.READ)
                }
            }
            //Ajouter le doit d'admin au créateur de l'ontologie
            Ontology.list().each { ontology ->
                permissionService.addPermission(ontology,ontology.user.username,BasePermission.ADMINISTRATION)
            }
            //Ajouter un droit de créateur/admin a qqun
            Software.list().each { software ->
                permissionService.addPermission(software,User.findByUsername("lrollus").username,BasePermission.ADMINISTRATION)
                permissionService.addPermission(software,User.findByUsername("rmaree").username,BasePermission.ADMINISTRATION)
                permissionService.addPermission(software,User.findByUsername("stevben").username,BasePermission.ADMINISTRATION)
            }
        }
    }

}
