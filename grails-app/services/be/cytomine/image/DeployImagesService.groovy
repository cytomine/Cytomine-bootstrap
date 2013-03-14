package be.cytomine.image

import be.cytomine.image.server.Storage
import be.cytomine.laboratory.Sample
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import javax.activation.MimetypesFileTypeMap

/**
 * TODOSTEVEBEN: Doc + refactoring + security?
 */
class DeployImagesService {

    def remoteCopyService
    def cytomineService
    def imageInstanceService
    def storageAbstractImageService
    def projectService

    static transactional = true

    UploadedFile copyUploadedFile(UploadedFile uploadedFile, Collection<Storage> storages, SecUser currentUser) {
        def localFile = uploadedFile.getPath() + "/" + uploadedFile.getFilename()

        storages.each { storage ->
            try {
                def remoteFile = storage.getBasePath() + "/" + uploadedFile.getFilename()
                log.info "REMOTE FILE = " + remoteFile
                def remotePath = new File(remoteFile).getParent()
                log.info "REMOTE PATH = " + remotePath
                remoteCopyService.copy(localFile, remotePath, remoteFile, storage, true)
            } catch (java.net.ConnectException e) {
                //errorMessage = "Operation timed out"
            }
        }
        uploadedFile.setStatus(UploadedFile.DEPLOYED)
        uploadedFile.save()
        return uploadedFile
    }


    AbstractImage deployUploadedFile(UploadedFile uploadedFile,  Collection<Storage> storages, SecUser currentUser) {

        SpringSecurityUtils.reauthenticate currentUser.getUsername(), null
        uploadedFile.refresh()

        //copy it
        uploadedFile = copyUploadedFile(uploadedFile, storages, currentUser)

        long timestamp = new Date().getTime()
        Sample sample = new Sample(name : timestamp.toString() + "-" + uploadedFile.getOriginalFilename())


        //create domains instance
        def ext = uploadedFile.getExt()
        Mime mime = Mime.findByExtension(ext)
        if (!mime) {
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            String mimeType = mimeTypesMap.getContentType(uploadedFile.getAbsolutePath())
            mime = new Mime(extension: ext, mimeType : mimeType)
            mime.save()
        }
        AbstractImage abstractImage = new AbstractImage(
                filename: uploadedFile.getFilename(),
                originalFilename:  uploadedFile.getOriginalFilename(),
                scanner: null,
                sample: sample,
                path: uploadedFile.getFilename(),
                mime: mime)

        if (sample.validate() && abstractImage.validate()) {

            sample.save(flush : true)
            sample.refresh()
            abstractImage.setSample(sample)
            abstractImage.save(flush: true)


            abstractImage.save(flush : true)

/*Group group = Group.findByName(currentUser.getUsername())

  AbstractImageGroup aig = new AbstractImageGroup(abstractImage:abstractImage, group:group)
            aig.save(flush: true,failOnError: true)*/

            storages.each { storage ->
                storageAbstractImageService.add(JSON.parse([storage : storage.id, abstractimage : abstractImage.id].encodeAsJSON()))
                /*StorageAbstractImage sai = new StorageAbstractImage(storage:storage,abstractImage:abstractImage)
                sai.save(flush: true,failOnError: true)*/
            }

            uploadedFile.getProjects()?.each { project_id ->
                Project project = projectService.read(project_id)
                ImageInstance imageInstance = new ImageInstance( baseImage : abstractImage, project:  project, user :currentUser)
                imageInstanceService.add(JSON.parse(imageInstance.encodeAsJSON()))
            }

            /*imagePropertiesService.clear(abstractImage)
            imagePropertiesService.populate(abstractImage)
            imagePropertiesService.extractUseful(abstractImage)*/


        } else {
            sample.errors?.each {
                log.info "Sample error : " + it
            }
            abstractImage.errors?.each {
                log.info "Sample error : " + it
            }
        }

        return abstractImage
    }
}
