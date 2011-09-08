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

    private def getExtensionFromFilename(filename) {
        def returned_value = ""
        def m = (filename =~ /(\.[^\.]*)$/)
        if (m.size()>0) returned_value = ((m[0][0].size()>0) ? m[0][0].substring(1).trim().toLowerCase() : "");
        return returned_value
    }

    def image = {
        def f = request.getFile('file')
        if(!f.empty) {
            println "not empty"
            def ext =  getExtensionFromFilename(f.originalFilename)
            def tmpFile = File.createTempFile(f.originalFilename, ext)
            tmpFile.deleteOnExit()
            f.transferTo(tmpFile)
            println "Tmp file created " + tmpFile.getPath()
            def aimage = new AbstractImage(
                    filename: f.originalFilename,
                    scanner : Scanner.list().first(),
                    slide : Slide.list().first(),
                    path : tmpFile.getPath(),
                    mime : Mime.findByExtension(ext))
            if (aimage.validate()) {
                aimage.save(flush : true)
                Storage.list().each { storage->
                    println "#################### " + storage
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
