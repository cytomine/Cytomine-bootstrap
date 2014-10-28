package be.cytomine.api.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.UserAnnotation
import be.cytomine.security.ForgotPasswordToken
import be.cytomine.security.SecRole
import be.cytomine.security.User
import be.cytomine.ontology.SharedAnnotation
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.annotation.RestApiResponseObject
import org.restapidoc.pojo.RestApiParamType

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Controller for annotation created by user
 */
@RestApi(name = "user annotation services", description = "Methods for managing an annotation created by a human user")
class RestUserAnnotationController extends RestController {

    def exportService
    def userAnnotationService
    def termService
    def imageInstanceService
    def secUserService
    def secUserSecRoleService
    def secRoleService
    def projectService
    def cytomineService
    def notificationService
    def paramsService
    def annotationListingService
    def reportService
    def imageProcessingService
    def securityACLService

    /**
     * List all annotation with light format
     */
    @RestApiMethod(description="List all annotation (very light format)", listing = true)
    def list() {
        responseSuccess(userAnnotationService.listLightForRetrieval())
    }

    @RestApiMethod(description="Count the number of annotation for the current user")
    @RestApiResponseObject(objectIdentifier = "[total:x]")
    def countByUser() {
        responseSuccess([total:userAnnotationService.count(cytomineService.currentUser)])
    }

    /**
     * Download report with annotation
     */
    @RestApiMethod(description="Download a report (pdf, xls,...) with user annotation data from a specific project")
    @RestApiResponseObject(objectIdentifier =  "file")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id"),
    @RestApiParam(name="terms", type="list", paramType = RestApiParamType.QUERY,description = "The annotation terms id (if empty: all terms)"),
    @RestApiParam(name="users", type="list", paramType = RestApiParamType.QUERY,description = "The annotation users id (if empty: all users)"),
    @RestApiParam(name="images", type="list", paramType = RestApiParamType.QUERY,description = "The annotation images id (if empty: all images)"),
    @RestApiParam(name="format", type="string", paramType = RestApiParamType.QUERY,description = "The report format (pdf, xls,...)")
    ])
    def downloadDocumentByProject() {
        reportService.createAnnotationDocuments(params.long('id'),params.terms,params.users,params.images,params.format,response,"USERANNOTATION")
    }

    def bootstrapUtilsService

    /**
     * Add comment on an annotation to other user
     */
    @RestApiMethod(description="Add comment on an annotation to other user and send a mail to users")
    @RestApiResponseObject(objectIdentifier = "empty")
    @RestApiParams(params=[
    @RestApiParam(name="userannotation", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
    @RestApiParam(name="POST JSON: subject", type="string", paramType = RestApiParamType.PATH,description = "The subject"),
    @RestApiParam(name="POST JSON: message", type="string", paramType = RestApiParamType.PATH,description = "TODO:APIDOC, DIFF WITH COMMENT?"),
    @RestApiParam(name="POST JSON: users", type="list", paramType = RestApiParamType.PATH,description = "The list of user (id) to send the mail"),
    @RestApiParam(name="POST JSON: comment", type="string", paramType = RestApiParamType.PATH,description = "TODO:APIDOC, DIFF WITH MESSAGE?"),
    ])
    def addComment() {

        User sender = User.read(springSecurityService.principal.id)
        securityACLService.checkUser(sender)
        UserAnnotation annotation = userAnnotationService.read(params.getLong('userannotation'))
        String cid = UUID.randomUUID().toString()

        //create annotation crop (will be send with comment)
        File annnotationCrop = null
        try {
            String cropURL = annotation.toCropURL(params)
            if (cropURL != null) {
                log.info "Load image from " + annotation.toCropURL(params)
                log.info "Load image from " + annotation.toCropURL(params)

                BufferedImage bufferedImage = imageProcessingService.getImageFromURL(annotation.toCropURL(params))

                log.info "Image " + bufferedImage

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
            attachments << [cid: cid, file: annnotationCrop]
        }

        //do receivers email list
        String[] receiversEmail
        List<User> receivers = []

        if (request.JSON.users) {
            receivers = JSONUtils.getJSONList(request.JSON.users).collect { userID ->
                User.read(userID)
            }
            receiversEmail = receivers.collect { it.getEmail() }
        } else if (request.JSON.emails) {
            receiversEmail = request.JSON.emails.split(",")
            receiversEmail.each { email ->
                if (!secUserService.findByEmail(email)) {

                    def guestUser = [username : email, firstname : 'firstname',
                            lastname : 'lastname', email : email,
                            password : 'passwordExpired', color : "#FF0000"]
                    secUserService.add(JSON.parse(JSONUtils.toJSONString(guestUser)))
                    User user = (User) secUserService.findByUsername(guestUser.username)
                    SecRole secRole = secRoleService.findByAuthority("ROLE_GUEST")
                    secUserSecRoleService.add(JSON.parse(JSONUtils.toJSONString([ user : user.id, role : secRole.id])))
                    secUserService.addUserToProject(user, annotation.getProject(), false)

                    if (user) {
                        user.passwordExpired = true
                        user.save()
                        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(
                                user : user,
                                tokenKey: UUID.randomUUID().toString(),
                                expiryDate: new Date() + 1
                        ).save()
                        notificationService.notifyWelcome(sender, user, forgotPasswordToken)
                    } else { //error
                    }

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
            notificationService.notifyShareAnnotation(sender, receiversEmail, request, attachments, cid)
            response([success: true, message: "Annotation shared to " + receiversEmail], 200)
        } else {
            response([success: false, message: "Error"], 400)
        }
    }

    /**
     * Show a single comment for an annotation
     */
    @RestApiMethod(description="Get a specific comment")
    @RestApiParams(params=[
    @RestApiParam(name="userannotation", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The comment id"),
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
    @RestApiMethod(description="Get all comments on annotation", listing=true)
    @RestApiParams(params=[
    @RestApiParam(name="userannotation", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
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
    @RestApiMethod(description="Get a user annotation")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
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
    @RestApiMethod(description="Add an annotation created by user")
    def add(){
        add(userAnnotationService, request.JSON)
    }


    /**
     * Get annotation user crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    @RestApiMethod(description="Get annotation user crop (image area that frame annotation)")
    @RestApiResponseObject(objectIdentifier = "file")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
    @RestApiParam(name="maxSize", type="int", paramType = RestApiParamType.PATH,description = "Maximum size of the crop image (w and h)"),
    @RestApiParam(name="zoom", type="int", paramType = RestApiParamType.PATH,description = "Zoom level"),
    @RestApiParam(name="draw", type="boolean", paramType = RestApiParamType.PATH,description = "Draw annotation form border on the image")
    ])
    def crop() {
        UserAnnotation annotation = UserAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("Annotation", params.id)
        } else {
            String url = annotation.toCropURL(params)
            log.info "redirect to ${url}"
            redirect (url : url)
        }

    }

    //TODO:APIDOC
    def cropMask () {
        UserAnnotation annotation = UserAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("UserAnnotation", params.id)
        } else {
            params.mask = true
            redirect (url : annotation.toCropURL(params))
            //responseBufferedImage(imageProcessingService.getMaskImage(annotation, params, false))
        }

    }

    //TODO:APIDOC
    def cropAlphaMask () {
        UserAnnotation annotation = UserAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("UserAnnotation", params.id)
        } else {
            params.alphaMask = true
            redirect (url : annotation.toCropURL(params))
            //responseBufferedImage(imageProcessingService.getMaskImage(annotation, params, true))
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
    @RestApiMethod(description="Update an annotation")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
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
    @RestApiMethod(description="Delete an annotation")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
    ])
    def delete() {
        def json = JSON.parse("{id : $params.id}")
        delete(userAnnotationService, json,null)
    }
}
