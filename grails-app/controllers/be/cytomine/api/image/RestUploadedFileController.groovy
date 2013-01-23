package be.cytomine.api.image

import be.cytomine.api.RestController
import be.cytomine.image.UploadedFile
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.FilesUtils
import grails.converters.JSON

/**
 * Controller that handle request on file uploading (when a file is uploaded, list uploaded files...)
 * //TODO:: tester ce controller
 */
class RestUploadedFileController extends RestController {

    def backgroundService
    def cytomineService
    def imagePropertiesService
    def projectService
    def convertImagesService
    def deployImagesService

    static allowedMethods = [image: 'POST']


    def list = {
        //get all uploaded file for this user
        def uploadedFiles = UploadedFile.createCriteria().list(sort : "created", order : "desc") {
            eq("user", cytomineService.getCurrentUser())
        }

        //if view is datatables, change way to store data
        if (params.dataTables) {
            uploadedFiles = ["aaData" : uploadedFiles]
        }
        responseSuccess(uploadedFiles)
    }

    def show = {
        responseSuccess(UploadedFile.findById(params.id))
    }

    def download = {
        //TODO:: remove this action or must be impl?

    }

    def add = {
        //TODO:: document this method

        //TODO:: externalized config
        def destPath = "/tmp/cytominebuffer"
        SecUser currentUser = cytomineService.getCurrentUser()
        String errorMessage = ""

        //read project
        String projectParam = request.getParameter("idProject")
        Project project = null
        if (projectParam != null && projectParam != "undefined" && projectParam != "null") {
            project = projectService.read(Integer.parseInt(projectParam))
        }

        //get file to upload
        def f = request.getFile('files[]')

        UploadedFile uploadedFile = null
        if (!f.empty) {

            long timestamp = new Date().getTime()

            //compute path/filename info
            String fullDestPath = destPath + "/" + currentUser.getId() + "/" + timestamp.toString()
            String newFilename = FilesUtils.correctFileName(f.originalFilename)
            String pathFile = fullDestPath + "/" + newFilename
            String extension = FilesUtils.getExtensionFromFilename(f.originalFilename).toLowerCase()
            //create dir and transfer file
            def mkdirCommand = "mkdir -p " + fullDestPath
            def proc = mkdirCommand.execute()
            proc.waitFor()
            File destFile = new File(pathFile)
            f.transferTo(destFile)

            //create domain
            uploadedFile = new UploadedFile(
                    originalFilename: f.originalFilename,
                    filename : currentUser.getId() + "/" + timestamp.toString() + "/" + newFilename,
                    path : destPath.toString(),
                    ext : extension,
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
            return
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
