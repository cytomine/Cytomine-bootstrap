package be.cytomine

import be.cytomine.image.UploadedFile
import be.cytomine.security.SecUser
import be.cytomine.project.Slide
import be.cytomine.image.AbstractImage
import be.cytomine.image.Mime
import be.cytomine.image.server.Storage
import be.cytomine.image.acquisition.Scanner
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.security.Group
import be.cytomine.image.ImageInstance
import grails.converters.JSON
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils


class DeployImagesService {

    def imagePropertiesService
    def remoteCopyService
    def cytomineService
    def abstractImageService
    def slideService
    def imageInstanceService
    static transactional = true

    void deployUploadedFile(UploadedFile uploadedFile, SecUser currentUser) {
        SpringSecurityUtils.reauthenticate currentUser.getUsername(), null
        uploadedFile.refresh()

        long timestamp = new Date().getTime()
        Slide slide = new Slide(name : timestamp.toString() + "-" + uploadedFile.getOriginalFilename(), index : 0)

        AbstractImage abstractImage = new AbstractImage(
                filename: uploadedFile.getConvertedFilename(),
                originalFilename:  uploadedFile.getOriginalFilename(),
                scanner: Scanner.list().first(),
                slide: slide,
                path: uploadedFile.getConvertedFilename(),
                mime: Mime.findByExtension(uploadedFile.getConvertedExt()))

        if (slide.validate() && abstractImage.validate()) {


            Storage.list().each { storage ->
                try {
                    remoteCopyService.copy(storage, abstractImage, uploadedFile, true) //TO DO : iterate on all servers (not accessibles from here)

                } catch (java.net.ConnectException e) {
                    //errorMessage = "Operation timed out"
                }
            }

            uploadedFile.setStatus(UploadedFile.DEPLOYED)
            uploadedFile.save()
            //slideService.add(JSON.parse(slide.encodeAsJSON()))
            slide.save(flush : true)
            slide.refresh()
            abstractImage.setSlide(slide)
            //abstractImageService.add(JSON.parse(abstractImage.encodeAsJSON()))
            //abstractImage.refresh()
            abstractImage.save(flush: true)
            Group group = Group.findByName(currentUser.getUsername())
            AbstractImageGroup.link(abstractImage, group)
            Storage.list().each { storage ->
                StorageAbstractImage.link(storage, abstractImage)
            }
            if (uploadedFile.getProject() != null) {

                ImageInstance imageInstance = new ImageInstance( baseImage : abstractImage, project:  uploadedFile.getProject(), slide:  slide, user :currentUser)
                imageInstanceService.add(JSON.parse(imageInstance.encodeAsJSON()))
                //imageInstance.save()
            }

            imagePropertiesService.extractUseful(abstractImage)


        } else {
            slide.errors?.each {
                println "Slide error : " + it
            }
            abstractImage.errors?.each {
                println "Slide error : " + it
            }
        }
    }
}
