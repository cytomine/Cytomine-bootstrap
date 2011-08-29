package be.cytomine.upload

import grails.converters.JSON
import be.cytomine.image.server.Storage
import be.cytomine.image.AbstractImage
import be.cytomine.project.Slide
import be.cytomine.image.acquisition.Scanner
import be.cytomine.image.Mime

class UploadController {

    def storageService
    static allowedMethods = [image:'POST']

    def image = {
        def f = request.getFile('file')

        if(!f.empty) {
            println "not empty"
            def tmpFile = File.createTempFile(f.originalFilename, ".svs")
            tmpFile.deleteOnExit()
            f.transferTo(tmpFile)
            println "Tmp file created " + tmpFile.getPath()
            def aimage = new AbstractImage(
                    filename: f.originalFilename,
                    scanner : Scanner.list().first(),
                    slide : Slide.list().first(),
                    path : tmpFile.getPath(),
                    mime : Mime.findByExtension("svs"))
            if (aimage.validate()) {
                aimage.save(flush : true)
                Storage.list().each { storage->
                    storageService.copy(storage, aimage)
                }
            } else {
                aimage.errors.each {
                    println it
                }
            }
        }
        else {
            response.status = 400;
            render ""
        }

        def response = [:]
        response.status = 200;
        response.name = f.originalFilename
        response.size = f.size
        response.type = f.contentType
        render response as JSON
    }
}
