package be.cytomine.api.image

import be.cytomine.SecurityACL
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.image.Mime
import be.cytomine.image.UploadedFile
import be.cytomine.image.server.Storage
import be.cytomine.laboratory.Sample
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.utils.JSONUtils
import grails.converters.JSON

import javax.activation.MimetypesFileTypeMap

/**
 * Controller that handle request on file uploading (when a file is uploaded, list uploaded files...)
 * //TODO:: tester ce controller
 */
class RestUploadedFileController extends RestController {

    def cytomineService
    def imagePropertiesService
    def projectService

    def mailService
    def storageService
    def grailsApplication
    def uploadedFileService
    def storageAbstractImageService
    def imageInstanceService
    def abstractImageService


    static allowedMethods = [image: 'POST']


    def list = {
        //get all uploaded file for this user
        def uploadedFiles = uploadedFileService.list(cytomineService.getCurrentUser())

        //if view is datatables, change way to store data
        if (params.dataTables) {
            uploadedFiles = ["aaData" : uploadedFiles]
        }
        responseSuccess(uploadedFiles)
    }

    def clearProperties = {
        AbstractImage abstractImage = abstractImageService.read(params.long('id'))
       imagePropertiesService.clear(abstractImage)
        responseSuccess([:])
    }

    def populateProperties = {
        AbstractImage abstractImage = abstractImageService.read(params.long('id'))
        imagePropertiesService.populate(abstractImage)
        responseSuccess([:])
    }

    def extractProperties = {
        AbstractImage abstractImage = abstractImageService.read(params.long('id'))
        imagePropertiesService.extractUseful(abstractImage)
        responseSuccess([:])
    }

    def show = {

        UploadedFile up = uploadedFileService.read(params.long('id'))
        if (up) {
            responseSuccess(up)
        } else {
            responseNotFound("UploadedFile", params.id)
        }
    }

//    def upload = {
//        println "upload"
//        redirect(action:'add')
//    }
    /**
     * Add a new image
     * TODO:: how to manage security here?
     */
    def add = {
        add(uploadedFileService, request.JSON)
    }

    /**
     * Update a new image
     * TODO:: how to manage security here?
     */
    def update = {
        update(uploadedFileService, request.JSON)
    }

    /**
     * Delete a new image
     * TODO:: how to manage security here?
     */
    def delete = {
        delete(uploadedFileService, JSON.parse("{id : $params.id}"),null)
    }

    def upRedirect = {
        redirect(url: "http://localhost:9090/upload")
    }


    def createImage = {
        long timestamp = new Date().getTime()
        def currentUser = cytomineService.currentUser
        SecurityACL.checkUser(currentUser)
        UploadedFile uploadedFile = UploadedFile.read(params.long('uploadedFile'))
        Collection<Storage> storages = []
        uploadedFile.getStorages()?.each {
            storages << storageService.read(it)
        }

        Sample sample = new Sample(name : timestamp.toString() + "-" + uploadedFile.getOriginalFilename())

        def projects = []
         //create domains instance
         def ext = uploadedFile.getExt()
         Mime mime = Mime.findByExtension(ext)
         if (!mime) {
             MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
             String mimeType = mimeTypesMap.getContentType(uploadedFile.getAbsolutePath())
             mime = new Mime(extension: ext, mimeType : mimeType)
             mime.save(failOnError: true)
         }
        println "#################################################################"
        println "#################################################################"
        println "##############CREATE IMAGE#################"
        println "#################################################################"
        println "#################################################################"
         AbstractImage abstractImage = new AbstractImage(
                 filename: uploadedFile.getFilename(),
                 originalFilename:  uploadedFile.getOriginalFilename(),
                 scanner: null,
                 sample: sample,
                 path: uploadedFile.getFilename(),
                 mime: mime)

         if (sample.validate() && abstractImage.validate()) {
             sample.save(flush : true,failOnError: true)
             sample.refresh()
             abstractImage.setSample(sample)
             abstractImage.save(flush: true,failOnError: true)
//             abstractImage.save(flush : true,failOnError: true)

             storages.each { storage ->
                 storageAbstractImageService.add(JSON.parse(JSONUtils.toJSONString([storage : storage.id, abstractimage : abstractImage.id])))
             }



             uploadedFile.getProjects()?.each { project_id ->
                 Project project = projectService.read(project_id)
                 projects << project
                 ImageInstance imageInstance = new ImageInstance( baseImage : abstractImage, project:  project, user :currentUser)
                 imageInstanceService.add(JSON.parse(imageInstance.encodeAsJSON()))
             }

         } else {
             sample.errors?.each {
                 log.info "Sample error : " + it
             }
             abstractImage.errors?.each {
                 log.info "Sample error : " + it
             }
         }
         writeMail(currentUser,abstractImage,projects)
         responseSuccess([abstractimage: abstractImage])




    }

    def secUserService

    private def writeMail(SecUser currentUser, def abstractImage, def projects) {
     //send email


            User recipient = null
            if (currentUser instanceof User) {
                recipient = (User) currentUser
            } else if (currentUser instanceof UserJob) {
                UserJob userJob = (UserJob) currentUser
                recipient = userJob.getUser()
            }

        // send email to uploader + all project admin
            def users = [recipient]
            projects.each {
                users.addAll(secUserService.listAdmins(it))
            }
            users.unique()

            log.info "Send mail to $users"



            StringBuffer message = new StringBuffer()
            message.append("New images are available on Cytomine:<br/>")

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
                 if(projects.isEmpty()) {
                     message.append("This image is not in a project.")


                 } else {
                     message.append("You can see images in projects: "+projects.collect{it.name}.join(','))
                 }



                message.append("<br />")



            if (recipient) {
                String[] recipients = users.collect{it.getEmail()}
                mailService.send(null, recipients, null, "New images available on Cytomine", message.toString(), null)
            }

    }

}
