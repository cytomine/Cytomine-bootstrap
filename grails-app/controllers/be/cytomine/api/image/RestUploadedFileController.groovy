package be.cytomine.api.image

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
import be.cytomine.utils.FilesUtils
import grails.converters.JSON

import javax.activation.MimetypesFileTypeMap

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
        AbstractImage abstractImage = abstractImageService.read(params.long('idImage'))
       imagePropertiesService.clear(abstractImage)
        responseSuccess([:])
    }

    def populateProperties = {
        AbstractImage abstractImage = abstractImageService.read(params.long('idImage'))
        imagePropertiesService.populate(abstractImage)
        responseSuccess([:])
    }

    def extractProperties = {
        AbstractImage abstractImage = abstractImageService.read(params.long('idImage'))
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


    def createImage = {
        long timestamp = new Date().getTime()
        def currentUser = cytomineService.currentUser
        UploadedFile uploadedFile = UploadedFile.read(params.long('uploadedFile'))
        Collection<Storage> storages = []
        uploadedFile.getStorages()?.each {
            storages << storageService.read(it)
        }

        Sample sample = new Sample(name : timestamp.toString() + "-" + uploadedFile.getOriginalFilename())


         //create domains instance
         def ext = uploadedFile.getExt()
         Mime mime = Mime.findByExtension(ext)
         if (!mime) {
             MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
             String mimeType = mimeTypesMap.getContentType(uploadedFile.getAbsolutePath())
             mime = new Mime(extension: ext, mimeType : mimeType)
             mime.save(failOnError: true)
         }
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
             abstractImage.save(flush : true,failOnError: true)

             storages.each { storage ->
                 storageAbstractImageService.add(JSON.parse([storage : storage.id, abstractimage : abstractImage.id].encodeAsJSON()))
             }

             uploadedFile.getProjects()?.each { project_id ->
                 Project project = projectService.read(project_id)
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
         writeMail(currentUser,abstractImage)
         responseSuccess([abstractimage: abstractImage])




    }

    private def writeMail(SecUser currentUser, def abstractImage) {
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

            if (recipient) {
                String[] recipients = [recipient.getEmail()]
                mailService.send(null, recipients, null, "New images available on Cytomine", message.toString(), null)
            }

        }

//    def pwet = {
//        //TODO:: document this method
//
//        String storageBufferPath = grailsApplication.config.storage_buffer
//        SecUser currentUser = cytomineService.getCurrentUser()
//        String errorMessage = ""
//        println "params=$params"
//        Integer idStorage = Integer.parseInt(params['idStorage']+"")
//        Integer idProject
//
//        try {
//            idProject = Integer.parseInt(params['idProject']+"")
//        } catch(Exception e) {
//            idProject = null
//        }
//
//        println "idProject : $idProject"
//        println "idStorage : $idStorage"
//
//
////        println "request=${request.queryString}"
////        println "request=${request['idStorage']}"
//
//        //params=[files[].size:172455, files[]:[size:172455, name:test01.jpg, path:/tmp/uploaded/2/0000000002, md5:5f411d67bf1b2ae420b39aea8902b1f3, content_type:application/octet-stream], files[].name:test01.jpg, files[].path:/tmp/uploaded/2/0000000002, files[].md5:5f411d67bf1b2ae420b39aea8902b1f3, files[].content_type:application/octet-stream, action:[POST:add], controller:restUploadedFile]
//
//
//        params.each {
//            println it
//            println it.class
//            println "|"+it?.key+"|"
//            println it?.key?.class
//            println params['files[].name']
//        }
//
//
//        //get file to upload
////        def f = request.getFile('files[]')
////        println f
//
//        def filename = params['files[].name']
//        File uploadedFilePath = new File(params['files[].path'])
//        long size = uploadedFilePath.size()
//        def contentType = params['files[].content_type']
//
//        println "filename=$filename"
//        println "uploadedFilePath=${uploadedFilePath.absolutePath}"
//        println "uploadedFilePath.exist=${uploadedFilePath.exists()}"
//
//        UploadedFile uploadedFile = null
//        if (uploadedFilePath.exists()) {
//
//            long timestamp = new Date().getTime()
//
//            //compute path/filename info
//            String fullDestPath = storageBufferPath + "/" + currentUser.getId() + "/" + timestamp.toString()
//            String newFilename = FilesUtils.correctFileName(filename)
//            String pathFile = fullDestPath + "/" + newFilename
//            String extension = FilesUtils.getExtensionFromFilename(filename).toLowerCase()
//
//            //create dir and transfer file
//            fileSystemService.makeLocalDirectory(fullDestPath)
//
//            println "SRC="+ uploadedFilePath.absolutePath
//            println "DEST="+ new File(pathFile).absolutePath
//            uploadedFilePath.renameTo(new File(pathFile))
//            println new File(pathFile).exists()
//            //create domain
//            uploadedFile = new UploadedFile(
//                    originalFilename: filename,
//                    filename : currentUser.getId() + "/" + timestamp.toString() + "/" + newFilename,
//                    path : storageBufferPath.toString(),
//                    ext : extension,
//                    size : size,
//                    contentType : contentType,
//                    projects : (idProject ? [idProject]: []),
//                    storages : [idStorage],
//                    user : currentUser
//            )
//
//            if(!uploadedFile.validate()) {
//                log.error uploadedFile.errors
//                response.status = 400;
//                render errorMessage
//                return
//            }
//
//            uploadedFile.save(flush : true, failOnError: true)
//            println "uploadedFile.getStorages()=" + uploadedFile.getStorages()
//
//        }
//        else {
//            response.status = 400;
//            render errorMessage
//            return
//        }
//
//        def content = [:]
//        content.status = 200;
//        content.name = filename
//        content.size = size
//        content.type = contentType
//        content.uploadFile = uploadedFile

//        Collection<Storage> storages = []
//        uploadedFile.getStorages()?.each {
//            storages << storageService.read(it)
//        }
//
//        //Convert and deploy
//        println "Convert and deploy"
//        backgroundService.execute("convertAndDeployImage", {
//            println "uploadedFile_copy"
//            UploadedFile uploadedFile_copy = UploadedFile.get(uploadedFile.id)
//            println "uploadedFile=$uploadedFile"
//            println "uploadedFile_copy=$uploadedFile_copy"
//            def uploadedFiles = convertImagesService.convertUploadedFile(uploadedFile_copy, currentUser)
//
//            Collection<AbstractImage> abstractImagesCreated = []
//            Collection<UploadedFile> deployedFiles = []
//
//
//            uploadedFiles.each {
//                UploadedFile new_uploadedFile = (UploadedFile) it
//
//
//                if (new_uploadedFile.status == UploadedFile.TO_DEPLOY)
//                    abstractImagesCreated << deployImagesService.deployUploadedFile(new_uploadedFile, storages, currentUser)
//                if (new_uploadedFile.status == UploadedFile.CONVERTED)
//                    deployImagesService.copyUploadedFile(new_uploadedFile, storages, currentUser)
//
//                deployedFiles << new_uploadedFile
//            }
//
//            //Check result
//            log.info "deployed files"
//            deployedFiles.each { deployedFile ->
//                log.info deployedFile
//            }
//            log.info "abstract image created"
//            abstractImagesCreated.each { abstractImage ->
//                log.info abstractImage
//            }
//
//
//            //delete main uploaded file
//            if (!deployedFiles.contains(uploadedFile_copy)) {
//                fileSystemService.deleteFile(uploadedFile_copy.absolutePath)
//                //uploadedFile_copy.delete()
//            }
//            //delete nested uploaded file
//            deployedFiles.each {
//                log.info "delete local files"
//                fileSystemService.deleteFile(it.absolutePath)
//                //it.delete()
//            }
//
//            //try to discover size & metadata
//
//            try {
//                abstractImagesCreated.each { abstractImage ->
//                    imagePropertiesService.clear(abstractImage)
//                    imagePropertiesService.populate(abstractImage)
//                    imagePropertiesService.extractUseful(abstractImage)
//                    abstractImage.save(flush : true)
//                }
//            } catch (Exception  e){
//
//            }
//
//            //send email
//            User recipient = null
//            if (currentUser instanceof User) {
//                recipient = (User) currentUser
//            } else if (currentUser instanceof UserJob) {
//                UserJob userJob = (UserJob) currentUser
//                recipient = userJob.getUser()
//            }
//            StringBuffer message = new StringBuffer()
//            message.append("images:<br/>")
//            abstractImagesCreated.each { abstractImage ->
//                for (imageInstance in ImageInstance.findAllByBaseImage(abstractImage)) {
//                    String url = UrlApi.getBrowseImageInstanceURL(imageInstance.getProject().id, imageInstance.getId())
//                    message.append(url)
//                    message.append("<br />")
//                    url = UrlApi.getAbstractImageThumbURL(abstractImage.id)
//                    message.append(url)
//                    message.append("<br />")
//
//                }
//                //UrlApi.getBrowseImageInstanceURL(grailsApplication.config.grails.serverURL, )
//                message.append(abstractImage.getFilename())
//                message.append("<br />")
//            }
//            message.append("files:<br/>")
//            deployedFiles.each { deployedFile ->
//                message.append(deployedFile.getFilename())
//                message.append("<br />")
//            }
//            if (recipient) {
//                String[] recipients = [recipient.getEmail()]
//                mailService.send(null, recipients, null, "New images available on Cytomine", message.toString(), null)
//            }
//
//        })
//
//        def response = [content]
//        render response as JSON
//    }

}
