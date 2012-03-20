package be.cytomine.api.image

import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.image.UploadedFile
import be.cytomine.api.RestController

class RestUploadedFileController extends RestController {

    def cytomineService
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

    def list = {
        def uploadedFiles = UploadedFile.createCriteria().list(sort : "created", order : "desc") {
            eq("user", cytomineService.getCurrentUser())
        }
        responseSuccess(uploadedFiles)
    }

    def show = {
        def uploadedFile = UploadedFile.findById(params.id)
        responseSuccess(uploadedFile)
    }

    def add = {
        def destPath = "/tmp/cytominebuffer"
        User currentUser = User.read(springSecurityService.principal.id)
        String errorMessage = ""
        def f = request.getFile('files[]')

        def uploadedFile = null
        if (!f.empty) {

             def ext = getExtensionFromFilename(f.originalFilename)
/*           def tmpFile = File.createTempFile(f.originalFilename, ext)
            tmpFile.deleteOnExit()
            f.transferTo(tmpFile) */
            long timestamp = new Date().getTime()

            def fullDestPath = destPath + "/" + timestamp.toString()

            def mkdirCommand = "mkdir -p " + fullDestPath
                        println "mkdirCommand = " + mkdirCommand
            mkdirCommand.execute()
            String pathFile = fullDestPath + "/" + f.originalFilename
            File destFile = new File(pathFile)
            f.transferTo(destFile)
            println "transferTo = " + destFile
            uploadedFile = new UploadedFile(
                    originalFilename: f.originalFilename,
                    filename : f.originalFilename,
                    path : pathFile,
                    ext : ext,
                    size : f.size,
                    contentType : f.contentType,
                    project : params.project,
                    user : currentUser
            )
            println "save uploadedFile = " + destFile
            uploadedFile.save()
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
        content.uploadFile = uploadedFile
        def response = [content]
        render response as JSON
    }

    def deploy = {
        /*Slide slide = new Slide(name : timestamp.toString()+"_"+f.originalFilename, index : 0)
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
        } */
    }
}
