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
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

import javax.activation.MimetypesFileTypeMap
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Controller that handle request on file uploading (when a file is uploaded, list uploaded files...)
 */
@Api(name = "uploaded file services", description = "Methods for managing an uploaded image file.")
class RestUploadedFileController extends RestController {

    def imageProcessingService
    def cytomineService
    def imagePropertiesService
    def projectService
    def cytomineMailService
    def storageService
    def grailsApplication
    def uploadedFileService
    def storageAbstractImageService
    def imageInstanceService
    def abstractImageService
    def renderService

    static allowedMethods = [image: 'POST']

    @ApiMethodLight(description="Get all uploaded file made by the current user")
    def list() {
        //get all uploaded file for this user
        def uploadedFiles = uploadedFileService.list(cytomineService.getCurrentUser())

        //if view is datatables, change way to store data
        if (params.dataTables) {
            uploadedFiles = ["aaData" : uploadedFiles]
        }
        responseSuccess(uploadedFiles)
    }

    @ApiMethodLight(description="Delete all file properties for an image")
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image id")
    ])
    @ApiResponseObject(objectIdentifier = "empty")
    def clearProperties () {
        AbstractImage abstractImage = abstractImageService.read(params.long('id'))
        imagePropertiesService.clear(abstractImage)
        responseSuccess([:])
    }

    @ApiMethodLight(description="Get all file properties for an image")
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image id")
    ])
    @ApiResponseObject(objectIdentifier = "empty")
    def populateProperties () {
        AbstractImage abstractImage = abstractImageService.read(params.long('id'))
        imagePropertiesService.populate(abstractImage)
        responseSuccess([:])
    }

    @ApiMethodLight(description="Fill image field (magn, width,...) with all file properties")
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image id")
    ])
    @ApiResponseObject(objectIdentifier = "empty")
    def extractProperties () {
        AbstractImage abstractImage = abstractImageService.read(params.long('id'))
        imagePropertiesService.extractUseful(abstractImage)
        responseSuccess([:])
    }


    @ApiMethodLight(description="Get an uploaded file")
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The uploaded file id")
    ])
    def show () {
        UploadedFile up = uploadedFileService.read(params.long('id'))
        if (up) {
            responseSuccess(up)
        } else {
            responseNotFound("UploadedFile", params.id)
        }
    }

    /**
     * Add a new image
     * TODO:: how to manage security here?
     *
     */
    @ApiMethodLight(description="Add a new uploaded file. This DOES NOT upload the file, just create the domain.")
    def add () {
        add(uploadedFileService, request.JSON)
    }

    /**
     * Update a new image
     * TODO:: how to manage security here?
     */
    @ApiMethodLight(description="Edit an uploaded file domain (usefull to edit its status during upload)")
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The uploaded file id")
    ])
    def update () {
        update(uploadedFileService, request.JSON)
    }

    /**
     * Delete a new image
     * TODO:: how to manage security here?
     */
    @ApiMethodLight(description="Delete an uploaded file domain. This do not delete the file on disk.")
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The uploaded file id")
    ])
    def delete () {
        delete(uploadedFileService, JSON.parse("{id : $params.id}"),null)
    }

    def upRedirect () {
        redirect(url: "http://localhost:9090/upload")
    }

    @ApiMethodLight(description="Create an image thanks to an uploaded file domain. THis add the image in the good storage and the project (if needed). This send too an email at the end to the uploader and the project admins.")
    @ApiParams(params=[
    @ApiParam(name="uploadedFile", type="long", paramType = ApiParamType.PATH,description = "The uploaded file id")
    ])
    @ApiResponseObject(objectIdentifier = "[abstractimage.|abstract image|]")
    def createImage () {
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

            storages.each { storage ->
                storageAbstractImageService.add(JSON.parse(JSONUtils.toJSONString([storage : storage.id, abstractimage : abstractImage.id])))
            }

            imagePropertiesService.clear(abstractImage)
            imagePropertiesService.populate(abstractImage)
            imagePropertiesService.extractUseful(abstractImage)
            abstractImage.save(flush: true,failOnError: true)

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
        notifyUsers(currentUser,abstractImage,projects)
        responseSuccess([abstractimage: abstractImage])
}

    def secUserService

    private def notifyUsers(SecUser currentUser, AbstractImage abstractImage, def projects) {
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

        String macroCID = UUID.randomUUID().toString()

        def attachments = []

        BufferedImage bufferedImage = imageProcessingService.getImageFromURL(abstractImage.getThumbURL())
        if (bufferedImage != null) {
            File macroFile = File.createTempFile("temp", ".jpg")
            macroFile.deleteOnExit()
            ImageIO.write(bufferedImage, "JPG", macroFile)
            attachments << [ cid : macroCID, file : macroFile]
        }

        def imagesInstances = []
        for (imageInstance in ImageInstance.findAllByBaseImage(abstractImage)) {
            String urlImageInstance = UrlApi.getBrowseImageInstanceURL(imageInstance.getProject().id, imageInstance.getId())
            imagesInstances << [urlImageInstance : urlImageInstance, projectName : imageInstance.project.getName()]

        }
        String message = renderService.createNewImagesAvailableMessage([
           abstractImageFilename : abstractImage.getOriginalFilename(),
           cid : macroCID,
           imagesInstances : imagesInstances,
            by: grailsApplication.config.grails.serverURL,
        ])

        cytomineMailService.send(
                null,
                (String[]) users.collect{it.getEmail()},
                null,
                "Cytomine : a new image is available",
                message.toString(),
                attachments)

    }

}
