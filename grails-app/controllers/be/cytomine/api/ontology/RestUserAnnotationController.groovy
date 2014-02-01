package be.cytomine.api.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.SecurityACL
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.UserAnnotation
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.social.SharedAnnotation
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiBodyObject
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Controller for annotation created by user
 */
@Api(name = "user annotation services", description = "Methods for managing an annotation created by a human user")
class RestUserAnnotationController extends RestController {

    def exportService
    def userAnnotationService
    def termService
    def imageInstanceService
    def secUserService
    def projectService
    def cytomineService
    def cytomineMailService
    def dataSource
    def paramsService
    def annotationListingService
    def reportService
    def imageProcessingService

    /**
     * List all annotation with light format
     */
    @ApiMethodLight(description="List all annotation (very light format)", listing = true)
    def list() {
        responseSuccess(userAnnotationService.listLightForRetrieval())
    }

    @ApiMethodLight(description="Count the number of annotation for the current user")
    @ApiResponseObject(objectIdentifier = "[total:x]")
    def countByUser() {
        responseSuccess([total:userAnnotationService.count(cytomineService.currentUser)])
    }

    /**
     * Download report with annotation
     */
    @ApiMethodLight(description="Download a report (pdf, xls,...) with user annotation data from a specific project")
    @ApiResponseObject(objectIdentifier =  "file")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The project id"),
        @ApiParam(name="terms", type="list", paramType = ApiParamType.QUERY,description = "The annotation terms id (if empty: all terms)"),
        @ApiParam(name="users", type="list", paramType = ApiParamType.QUERY,description = "The annotation users id (if empty: all users)"),
        @ApiParam(name="images", type="list", paramType = ApiParamType.QUERY,description = "The annotation images id (if empty: all images)"),
        @ApiParam(name="format", type="string", paramType = ApiParamType.QUERY,description = "The report format (pdf, xls,...)")
    ])
    def downloadDocumentByProject() {
        reportService.createAnnotationDocuments(params.long('id'),params.terms,params.users,params.images,params.format,response,"USERANNOTATION")
    }

    def bootstrapUtilsService

    /**
     * Add comment on an annotation to other user
     */
    @ApiMethodLight(description="Add comment on an annotation to other user and send a mail to users")
    @ApiResponseObject(objectIdentifier = "empty")
    @ApiParams(params=[
        @ApiParam(name="userannotation", type="long", paramType = ApiParamType.PATH,description = "The annotation id"),
        @ApiParam(name="POST JSON: subject", type="string", paramType = ApiParamType.PATH,description = "The subject"),
        @ApiParam(name="POST JSON: message", type="string", paramType = ApiParamType.PATH,description = "TODO:APIDOC, DIFF WITH COMMENT?"),
        @ApiParam(name="POST JSON: users", type="list", paramType = ApiParamType.PATH,description = "The list of user (id) to send the mail"),
        @ApiParam(name="POST JSON: comment", type="string", paramType = ApiParamType.PATH,description = "TODO:APIDOC, DIFF WITH MESSAGE?"),
    ])
    def addComment() {

        User sender = User.read(springSecurityService.principal.id)
        SecurityACL.checkUser(sender)
        UserAnnotation annotation = userAnnotationService.read(params.getLong('userannotation'))
        log.info "add comment from " + sender + " and userannotation " + annotation

        //create annotation crop (will be send with comment)
        File annnotationCrop = null
        try {
            String cropURL = annotation.toCropURL()
            if (cropURL != null) {
                BufferedImage bufferedImage = imageProcessingService.getImageFromURL(annotation.toCropURL())
                if (bufferedImage != null) {
                    annnotationCrop = File.createTempFile("temp", ".jpg")
                    annnotationCrop.deleteOnExit()
                    ImageIO.write(bufferedImage, "JPG", annnotationCrop)
                }
            }
        } catch (FileNotFoundException e) {
            annnotationCrop = null
        }
        def attachments = []
        if (annnotationCrop != null) {
            attachments << [cid: "annotation", file: annnotationCrop]
        }

        //do receivers email list
        String[] receiversEmail
        List<User> receivers = []
        println "params.users $request.JSON.users"
        println "params.emails $request.JSON.emails"
        if (request.JSON.users) {
            receivers = JSONUtils.getJSONList(request.JSON.users).collect { userID ->
                println "userID=$userID"
                User.read(userID)
            }
            receiversEmail = receivers.collect { it.getEmail() }
        } else if (request.JSON.emails) {
            receiversEmail = request.JSON.emails.split(",")
            println "emails to send $receiversEmail"
            receiversEmail.each { email ->
                //to do use addCommandUser

                def guestUser = [
                        username : email.split("@")[0],
                        firstname : '___',
                        lastname : '---',
                        email : email,
                        group : [],
                        password : 'guest',
                        color : "#FF0000",
                        roles : ["ROLE_GUEST"]
                ]

                def usersCreated = bootstrapUtilsService.createUsers([guestUser])
                usersCreated.each {
                    SecUser user = (SecUser) it
                    user.setPasswordExpired(true)
                    user.save()
                }

            }
        }

        log.info "send mail to " + receiversEmail

        //create shared annotation domain
        def sharedAnnotation = new SharedAnnotation(
                sender: sender,
                receivers: receivers,
                comment: request.JSON.comment,
                userAnnotation: annotation
        )
        if (sharedAnnotation.save()) {
            cytomineMailService.send("cytomine.ulg@gmail.com", receiversEmail, sender.getEmail(), request.JSON.subject, request.JSON.message, attachments)
            response([success: true, message: "Annotation shared to " + receivers.toString()], 200)
        } else {
            response([success: false, message: "Error"], 400)
        }
    }

    /**
     * Show a single comment for an annotation
     */
    @ApiMethodLight(description="Get a specific comment")
    @ApiParams(params=[
        @ApiParam(name="userannotation", type="long", paramType = ApiParamType.PATH,description = "The annotation id"),
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The comment id"),
    ])
    def showComment() {
        UserAnnotation annotation = userAnnotationService.read(params.long('userannotation'))
        if (!annotation) {
            responseNotFound("Annotation", params.annotation)
        }
        def sharedAnnotation = SharedAnnotation.findById(params.long('id'))
        if (!sharedAnnotation) {
            responseNotFound("SharedAnnotation", params.id)
        } else {
            responseSuccess(sharedAnnotation)
        }
    }

    /**
     * List all comments for an annotation
     */
    @ApiMethodLight(description="Get all comments on annotation", listing=true)
    @ApiParams(params=[
        @ApiParam(name="userannotation", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
    ])
    def listComments() {
        UserAnnotation annotation = userAnnotationService.read(params.long('userannotation'))
        User user = User.read(springSecurityService.principal.id)
        if (annotation) {
            def sharedAnnotations = SharedAnnotation.createCriteria().list {
                eq("userAnnotation", annotation)
                or {
                    eq("sender", user)
                    receivers {
                        eq("id", user.id)
                    }
                }
                order("created", "desc")
            }
            responseSuccess(sharedAnnotations.unique())
        } else {
            responseNotFound("Annotation", params.id)
        }
    }

    /**
     * Get a single annotation
     */
    @ApiMethodLight(description="Get a user annotation")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
    ])
    def show() {
        UserAnnotation annotation = userAnnotationService.read(params.long('id'))
        if (annotation) {
            responseSuccess(annotation)
        }
        else responseNotFound("Annotation", params.id)
    }


    /**
     * Add annotation created by user
     */
    @ApiMethodLight(description="Add an annotation created by user")
    def add(){
        add(userAnnotationService, request.JSON)
    }


    /**
     * Get annotation user crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    @ApiMethodLight(description="Get annotation user crop (image area that frame annotation)")
    @ApiResponseObject(objectIdentifier = "file")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id"),
        @ApiParam(name="max_size", type="int", paramType = ApiParamType.PATH,description = "Maximum size of the crop image (w and h)"),
        @ApiParam(name="zoom", type="int", paramType = ApiParamType.PATH,description = "Zoom level"),
        @ApiParam(name="draw", type="boolean", paramType = ApiParamType.PATH,description = "Draw annotation form border on the image")
    ])
    def crop() {
        UserAnnotation annotation = UserAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("Annotation", params.id)
        } else {
            responseBufferedImage(imageProcessingService.crop(annotation, params))
        }

    }

    //TODO:APIDOC
    def cropMask () {
        UserAnnotation annotation = UserAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("UserAnnotation", params.id)
        } else {
            responseBufferedImage(imageProcessingService.getMaskImage(annotation, params, false))
        }

    }

    //TODO:APIDOC
    def cropAlphaMask () {
        UserAnnotation annotation = UserAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("UserAnnotation", params.id)
        } else {
            responseBufferedImage(imageProcessingService.getMaskImage(annotation, params, true))
        }

    }



    @Override
    public Object addOne(def service, def json) {
        if (!json.project || json.isNull('project')) {
            ImageInstance image = ImageInstance.read(json.image)
            if (image) json.project = image.project.id
        }
        if (json.isNull('project')) {
            throw new WrongArgumentException("Annotation must have a valide project:" + json.project)
        }
        if (json.isNull('location')) {
            throw new WrongArgumentException("Annotation must have a valide geometry:" + json.location)
        }
        def minPoint = params.getLong('minPoint')
        def maxPoint = params.getLong('maxPoint')

        def result = userAnnotationService.add(json,minPoint,maxPoint)
        return result
    }

    /**
     * Update annotation created by user
     */
    @ApiMethodLight(description="Update an annotation")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
    ])
    def update() {
        def json = request.JSON
        try {
            def domain = userAnnotationService.retrieve(json)
            def result = userAnnotationService.update(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Delete annotation created by user
     */
    @ApiMethodLight(description="Delete an annotation")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
    ])
    def delete() {
        def json = JSON.parse("{id : $params.id}")
        delete(userAnnotationService, json,null)
    }

    /**
     * Return a list of annotation (if list = [[annotation1,rate1, term1, expectedTerm1],..], add rate,term1,expected value in annotation]
     * @param list annotation list
     * @return annotation list with all info
     */
    private def mergeResults(def list) {
        //list = [ [a,b],...,[x,y]]  => [a.rate = b, x.rate = y...]
        if (list.isEmpty() || list[0] instanceof UserAnnotation || list[0].class.equals("be.cytomine.ontology.UserAnnotation")) return list
//        def result = []
//        list.each {
//            UserAnnotation annotation = it[0]
//            annotation.rate = it[1]
//            annotation.idTerm = it[2]
//            annotation.idExpectedTerm = it[3]
//            result << annotation
//        }
//        return result
        return list
    }
}
