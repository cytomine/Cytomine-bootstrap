package be.cytomine.image

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import be.cytomine.SecurityCheck
import be.cytomine.image.server.MimeImageServer
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.laboratory.Sample
import be.cytomine.security.Group
import be.cytomine.security.SecUser
import grails.converters.JSON

/**
 * TODOSTEVEBEN: Doc + refactoring + security?
 */
class DeployImagesService {

    def imagePropertiesService
    def remoteCopyService
    def cytomineService
    def abstractImageService
    def imageInstanceService
    def storageService
    static transactional = true

    void deployUploadedFile(UploadedFile uploadedFile, SecUser currentUser) {
        SpringSecurityUtils.reauthenticate currentUser.getUsername(), null
        uploadedFile.refresh()

        long timestamp = new Date().getTime()
        Sample sample = new Sample(name : timestamp.toString() + "-" + uploadedFile.getOriginalFilename())
        def _ext = uploadedFile.getConvertedExt()
        Mime mime = Mime.findByExtension(uploadedFile.getConvertedExt())
        AbstractImage abstractImage = new AbstractImage(
                filename: uploadedFile.getConvertedFilename(),
                originalFilename:  uploadedFile.getOriginalFilename(),
                scanner: null,
                sample: sample,
                path: uploadedFile.getConvertedFilename(),
                mime: mime)

        if (sample.validate() && abstractImage.validate()) {

            Collection<Storage> storages = MimeImageServer.findAllByMime(mime).collect {it.imageServer.storage}.unique()
            storages.each { storage ->
                try {
                    remoteCopyService.copy(storage, abstractImage, uploadedFile, true) //TO DO : iterate on all servers (not accessibles from here)

                } catch (java.net.ConnectException e) {
                    //errorMessage = "Operation timed out"
                }
            }

            uploadedFile.setStatus(UploadedFile.DEPLOYED)
            uploadedFile.save()
            //slideService.add(JSON.parse(sample.encodeAsJSON()))
            sample.save(flush : true)
            sample.refresh()
            abstractImage.setSample(sample)
            //abstractImageService.add(JSON.parse(abstractImage.encodeAsJSON()))
            //abstractImage.refresh()
            abstractImage.save(flush: true)
            Group group = Group.findByName(currentUser.getUsername())
            AbstractImageGroup aig = new AbstractImageGroup(abstractImage:abstractImage, group:group)
            aig.save(flush: true,failOnError: true)
            storages.each { storage ->
                StorageAbstractImage sai = new StorageAbstractImage(storage:storage,abstractImage:abstractImage)
                sai.save(flush: true,failOnError: true)
            }

            StorageAbstractImage.findAllByAbstractImage(abstractImage).each { storageAbstractImage ->
               def sai = StorageAbstractImage.findByStorageAndAbstractImage(storageAbstractImage.storage, abstractImage)
               sai.delete(flush:true)
           }
           json.storage.each { storageID ->
               Storage storage = storageService.read(storageID)
               StorageAbstractImage sai = new StorageAbstractImage(storage:storage,abstractImage:abstractImage)
               sai.save(flush:true,failOnError: true)
           }

            if (uploadedFile.getProject() != null) {

                ImageInstance imageInstance = new ImageInstance( baseImage : abstractImage, project:  uploadedFile.getProject(), user :currentUser)
                imageInstanceService.add(JSON.parse(imageInstance.encodeAsJSON()), new SecurityCheck())
                //imageInstance.save()
            }

            imagePropertiesService.clear(abstractImage)
            imagePropertiesService.populate(abstractImage)
            imagePropertiesService.extractUseful(abstractImage)
            abstractImage.save(flush : true)

        } else {
            sample.errors?.each {
                log.info "Sample error : " + it
            }
            abstractImage.errors?.each {
                log.info "Sample error : " + it
            }
        }
    }
}
