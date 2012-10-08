package be.cytomine.api.image

import be.cytomine.api.RestController
import be.cytomine.image.UploadedFile
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON

class RestUploadedFileController extends RestController {

    def backgroundService
    def remoteCopyService
    def cytomineService
    def storageService
    def imagePropertiesService
    def projectService
    def convertImagesService
    def deployImagesService

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
        if (params.dataTables) {
            uploadedFiles = ["aaData" : uploadedFiles]
        }
        responseSuccess(uploadedFiles)
    }

    def show = {
        def uploadedFile = UploadedFile.findById(params.id)
        responseSuccess(uploadedFile)
    }

    def add = {
        def destPath = "/tmp/cytominebuffer"
        SecUser currentUser = cytomineService.getCurrentUser()
        String errorMessage = ""
        String projectParam = request.getParameter("idProject")
        Project project = null
        if (projectParam != null && projectParam != "undefined" && projectParam != "null") {
            project = projectService.read(Integer.parseInt(projectParam), new Project())
        }
        def f = request.getFile('files[]')

        UploadedFile uploadedFile = null
        if (!f.empty) {

            def ext = getExtensionFromFilename(f.originalFilename)
/*           def tmpFile = File.createTempFile(f.originalFilename, ext)
            tmpFile.deleteOnExit()
            f.transferTo(tmpFile) */
            long timestamp = new Date().getTime()

            String fullDestPath = destPath + "/" + currentUser.getId() + "/" + timestamp.toString()
            String newFilename = f.originalFilename
            newFilename = newFilename.replace(" ", "_")
            newFilename = newFilename.replace("(", "_")
            newFilename = newFilename.replace(")", "_")
            newFilename = newFilename.replace("+", "_")
            newFilename = newFilename.replace("*", "_")
            newFilename = newFilename.replace("/", "_")
            newFilename = newFilename.replace("@", "_")
            String pathFile = fullDestPath + "/" + newFilename
            def mkdirCommand = "mkdir -p " + fullDestPath
            def proc = mkdirCommand.execute()
            proc.waitFor()
            File destFile = new File(pathFile)
            f.transferTo(destFile)

            uploadedFile = new UploadedFile(
                    originalFilename: f.originalFilename,
                    filename : currentUser.getId() + "/" + timestamp.toString() + "/" + newFilename,
                    path : destPath.toString(),
                    ext : ext.toLowerCase(),
                    size : f.size,
                    contentType : f.contentType,
                    project : project,
                    user : currentUser
            )
            uploadedFile.save(flush : true)
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

        //Convert and deploy
        backgroundService.execute("convertAndDeployImage", {
            boolean success = convertImagesService.convertUploadedFile(uploadedFile, currentUser)
            if (success) deployImagesService.deployUploadedFile(uploadedFile, currentUser)
        })

        def response = [content]
        render response as JSON
    }

}
