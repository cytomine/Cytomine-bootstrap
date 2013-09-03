package be.cytomine.api.image

import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.UploadedFile
import be.cytomine.image.server.Storage
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.security.UserJob
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
    def fileSystemService
    def mailService
    def storageService
    def grailsApplication

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

    def add = {
        //TODO:: document this method

        String storageBufferPath = grailsApplication.config.storage_buffer
        SecUser currentUser = cytomineService.getCurrentUser()
        String errorMessage = ""

        Integer idProject = params.int("idProject")
        Integer idStorage = params.int("idStorage")

        println "idProject : $idProject"
        println "idStorage : $idStorage"

        //get file to upload
        def f = request.getFile('files[]')

        UploadedFile uploadedFile = null
        if (!f.empty) {

            long timestamp = new Date().getTime()

            //compute path/filename info
            String fullDestPath = storageBufferPath + "/" + currentUser.getId() + "/" + timestamp.toString()
            String newFilename = FilesUtils.correctFileName(f.originalFilename)
            String pathFile = fullDestPath + "/" + newFilename
            String extension = FilesUtils.getExtensionFromFilename(f.originalFilename).toLowerCase()

            //create dir and transfer file
            fileSystemService.makeLocalDirectory(fullDestPath)
            f.transferTo(new File(pathFile))

            //create domain
            uploadedFile = new UploadedFile(
                    originalFilename: f.originalFilename,
                    filename : currentUser.getId() + "/" + timestamp.toString() + "/" + newFilename,
                    path : storageBufferPath.toString(),
                    ext : extension,
                    size : f.size,
                    contentType : f.contentType,
                    projects : (idProject ? [idProject]: []),
                    storages : [idStorage],
                    user : currentUser
            )
            if(!uploadedFile.validate()) {
                log.error uploadedFile.errors
                response.status = 400;
                render errorMessage
                return
            }

            uploadedFile.save(flush : true, failOnError: true)
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

        Collection<Storage> storages = []
        uploadedFile.getStorages()?.each {
            storages << storageService.read(it)
        }

        //Convert and deploy
        println "Convert and deploy"
        backgroundService.execute("convertAndDeployImage", {
            println "uploadedFile_copy"
            UploadedFile uploadedFile_copy = UploadedFile.get(uploadedFile.id)
            println "uploadedFile=$uploadedFile"
            println "uploadedFile_copy=$uploadedFile_copy"
            def uploadedFiles = convertImagesService.convertUploadedFile(uploadedFile_copy, currentUser)
            Collection<AbstractImage> abstractImagesCreated = []
            Collection<UploadedFile> deployedFiles = []


            uploadedFiles.each {
                UploadedFile new_uploadedFile = (UploadedFile) it


                if (new_uploadedFile.status == UploadedFile.TO_DEPLOY)
                    abstractImagesCreated << deployImagesService.deployUploadedFile(new_uploadedFile, storages, currentUser)
                if (new_uploadedFile.status == UploadedFile.CONVERTED)
                    deployImagesService.copyUploadedFile(new_uploadedFile, storages, currentUser)

                deployedFiles << new_uploadedFile
            }

            //Check result
            log.info "deployed files"
            deployedFiles.each { deployedFile ->
                log.info deployedFile
            }
            log.info "abstract image created"
            abstractImagesCreated.each { abstractImage ->
                log.info abstractImage
            }


            //delete main uploaded file
            if (!deployedFiles.contains(uploadedFile_copy)) {
                fileSystemService.deleteFile(uploadedFile_copy.absolutePath)
                //uploadedFile_copy.delete()
            }
            //delete nested uploaded file
            deployedFiles.each {
                log.info "delete local files"
                fileSystemService.deleteFile(it.absolutePath)
                //it.delete()
            }

            //try to discover size & metadata

            try {
                abstractImagesCreated.each { abstractImage ->
                    imagePropertiesService.clear(abstractImage)
                    imagePropertiesService.populate(abstractImage)
                    imagePropertiesService.extractUseful(abstractImage)
                    abstractImage.save(flush : true)
                }
            } catch (Exception  e){

            }

            //send email
            User recipient = null
            if (currentUser instanceof User) {
                recipient = (User) currentUser
            } else if (currentUser instanceof UserJob) {
                UserJob userJob = (UserJob) currentUser
                recipient = userJob.getUser()
            }
            StringBuffer message = new StringBuffer()
            message.append("images:<br/>")
            abstractImagesCreated.each { abstractImage ->
                for (imageInstance in ImageInstance.findAllByBaseImage(abstractImage)) {
                    String url = UrlApi.getBrowseImageInstanceURL(imageInstance.getProject().id, imageInstance.getId())
                    message.append(url)
                    message.append("<br />")
                    url = UrlApi.getAbstractImageThumbURL(abstractImage.id)
                    message.append(url)
                    message.append("<br />")

                }
                //UrlApi.getBrowseImageInstanceURL(grailsApplication.config.grails.serverURL, )
                message.append(abstractImage.getFilename())
                message.append("<br />")
            }
            message.append("files:<br/>")
            deployedFiles.each { deployedFile ->
                message.append(deployedFile.getFilename())
                message.append("<br />")
            }
            if (recipient) {
                String[] recipients = [recipient.getEmail()]
                mailService.send(null, recipients, null, "New images available on Cytomine", message.toString(), null)
            }

        })

        def response = [content]
        render response as JSON
    }

}
