package be.cytomine.api.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.social.SharedAnnotation
import grails.converters.JSON

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.text.SimpleDateFormat

/**
 * Controller for annotation created by user
 */
class RestUserAnnotationController extends RestController {

    def exportService
    def userAnnotationService
    def termService
    def imageInstanceService
    def secUserService
    def projectService
    def cytomineService
    def mailService
    def dataSource
    def paramsService
    def annotationListingService
    def reportService

    /**
     * List all annotation with light format
     */
    def list = {
        responseSuccess(userAnnotationService.listLightForRetrieval())
    }

    def countByUser = {
        responseSuccess([total:userAnnotationService.count(cytomineService.currentUser)])
    }

    /**
     * Download report with annotation
     */
    def downloadDocumentByProject = {
        reportService.createAnnotationDocuments(params.long('id'),params.terms,params.users,params.images,params.format,response,"USERANNOTATION")
    }

    /**
     * Add comment on an annotation to other user
     */
    def addComment = {

        User sender = User.read(springSecurityService.principal.id)
        UserAnnotation annotation = userAnnotationService.read(params.getLong('userannotation'))
        log.info "add comment from " + sender + " and userannotation " + annotation

        //create annotation crop (will be send with comment)
        File annnotationCrop = null
        try {
            String cropURL = annotation.toCropURL()
            if (cropURL != null) {
                BufferedImage bufferedImage = getImageFromURL(annotation.toCropURL())
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
        List<User> receivers = request.JSON.users.collect { userID ->
            User.read(userID)
        }
        String[] receiversEmail = new String[receivers.size()]
        for (int i = 0; i < receivers.size(); i++) {
            receiversEmail[i] = receivers[i].getEmail();
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
            mailService.send("cytomine.ulg@gmail.com", receiversEmail, sender.getEmail(), request.JSON.subject, request.JSON.message, attachments)
            response([success: true, message: "Annotation shared to " + receivers.toString()], 200)
        } else {
            response([success: false, message: "Error"], 400)
        }
    }

    /**
     * Show a single comment for an annotation
     */
    def showComment = {
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
    def listComments = {
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
    def show = {
        UserAnnotation annotation = userAnnotationService.read(params.long('id'))
        if (annotation) {
            responseSuccess(annotation)
        }
        else responseNotFound("Annotation", params.id)
    }

    /**
     * Add annotation created by user
     */
    def add = {
        add(userAnnotationService, request.JSON)
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
    def update = {
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
    def delete = {
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
