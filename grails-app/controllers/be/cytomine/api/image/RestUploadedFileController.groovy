package be.cytomine.api.image

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
import be.cytomine.image.UploadedFile

class RestUploadedFileController {


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

    def add = {
        def destPath = "/tmp/cytominebuffer"
        println "UPLOAD REQUESTED"
        User currentUser = User.read(springSecurityService.principal.id)
        String errorMessage = ""
        def f = request.getFile('files[]')
        if (!f.empty) {

            def ext = getExtensionFromFilename(f.originalFilename)
            /*def tmpFile = File.createTempFile(f.originalFilename, ext)
            tmpFile.deleteOnExit()
            f.transferTo(tmpFile) */
            long timestamp = new Date().getTime()
            println "createDirectory :"
            def fullDestPath = destPath + "/" + timestamp.toString()
            def mkdirCommand = "mkdir -p " + fullDestPath
            mkdirCommand.execute()
            String pathFile = fullDestPath + "/" + f.originalFilename
            File destFile = new File(pathFile)
            f.transferTo(destFile)
                     println "pathFile="+pathFile
            new UploadedFile(
                    originalFilename: f.originalFilename,
                    filename : f.originalFilename,
                    path : pathFile,
                    project : params.project,
                    user : currentUser
            )

        }
        else {
            response.status = 400;
            render errorMessage
        }

        def content = [:]
        content.status = 200;
        content.name = f.originalFilename
        content.size = f.size
        content.type = f.contentType
        def response = [content]
        render response as JSON
    }

    def deploy = {
        Slide slide = new Slide(name : timestamp.toString()+"_"+f.originalFilename, index : 0)
        slide.save()

        String finalPath = timestamp.toString() + "_" + f.originalFilename

        AbstractImage aimage = new AbstractImage(
                filename: timestamp+"_"+f.originalFilename,
                scanner: Scanner.list().first(),
                slide: slide,
                path: finalPath,
                mime: Mime.findByExtension(ext))

        if (aimage.validate()) {
            aimage.save(flush: true)
            Group group = Group.findByName(currentUser.getUsername())
            AbstractImageGroup.link(aimage, group)
            Storage.list().each { storage ->
                try {
                    remoteCopyService.copy(storage, aimage, tmpFile.getPath()) //TO DO : iterate on all servers (not accessibles from here)
                    StorageAbstractImage.link(storage, aimage)
                } catch (java.net.ConnectException e) {
                    errorMessage = "Operation timed out"
                }
            }
            imagePropertiesService.extractUseful(aimage)
        }
    }
}
