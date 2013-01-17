package be.cytomine

import be.cytomine.image.server.MimeImageServer
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.laboratory.Sample
import be.cytomine.security.Group
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import be.cytomine.image.*
import be.cytomine.project.Project

class DeployImagesService {

    def imagePropertiesService
    def remoteCopyService
    def cytomineService
    def abstractImageService
    def imageInstanceService
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
            AbstractImageGroup.link(abstractImage, group)
            storages.each { storage ->
                StorageAbstractImage.link(storage, abstractImage)
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
