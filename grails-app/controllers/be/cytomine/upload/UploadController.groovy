package be.cytomine.upload

import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.image.Mime
import be.cytomine.image.acquisition.Scanner
import be.cytomine.image.server.Storage
import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.project.Slide
import be.cytomine.security.Group
import be.cytomine.security.User
import grails.converters.JSON

class UploadController {

    def remoteCopyService
    def storageService
    def imagePropertiesService
    def springSecurityService

    static allowedMethods = [image: 'POST']

    private def getExtensionFromFilename = {filename ->
        def returned_value = ""
        def m = (filename =~ /(\.[^\.]*)$/)
        if (m.size() > 0) returned_value = ((m[0][0].size() > 0) ? m[0][0].substring(1).trim().toLowerCase() : "");
        return returned_value
    }

    def image = {
        println "UPLOAD REQUESTED"
        User currentUser = User.read(springSecurityService.principal.id)
        def f = request.getFile('files[]')
        if (!f.empty) {
            println "not empty"
            def ext = getExtensionFromFilename(f.originalFilename)
            def tmpFile = File.createTempFile(f.originalFilename, ext)
            tmpFile.deleteOnExit()
            f.transferTo(tmpFile)            
            println "Tmp file created " + tmpFile.getPath()

            /*def aimage = new AbstractImage(
                    filename: f.originalFilename,
                    scanner: Scanner.list().first(),
                    slide: Slide.list().first(),
                    path: f.originalFilename,
                    mime: Mime.findByExtension(ext))

            if (aimage.validate()) {
                aimage.save(flush: true)
                Group group = Group.findByName(currentUser.getUsername())
                AbstractImageGroup.link(aimage, group)
                Storage.list().each { storage ->
                    remoteCopyService.copy(storage, aimage, tmpFile.getPath()) //TO DO : iterate on all servers (not accessibles from here)
                    StorageAbstractImage.link(storage, aimage)
                }
                imagePropertiesService.extractUseful(aimage)
            } else {
                aimage.errors.each {
                    println it
                }
            }   */
        }
        else {
            response.status = 400;
            render ""
        }

        def content = [:]
        content.status = 200;
        content.name = f.originalFilename
        content.size = f.size
        content.type = f.contentType
        def response = [content]
        render response as JSON
    }
}
