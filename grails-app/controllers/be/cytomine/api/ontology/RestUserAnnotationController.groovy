package be.cytomine.api.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance

import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.social.SharedAnnotation
import grails.converters.JSON

import java.awt.image.BufferedImage
import java.text.SimpleDateFormat
import javax.imageio.ImageIO
import be.cytomine.ontology.UserAnnotation

import com.vividsolutions.jts.geom.Geometry

import groovy.sql.Sql

import be.cytomine.security.UserJob

import be.cytomine.utils.GeometryUtils

class RestUserAnnotationController extends RestController {

    def exportService
    def grailsApplication
    def userAnnotationService
    def domainService
    def termService
    def imageInstanceService
    def userService
    def projectService
    def cytomineService
    def mailService
    def dataSource
    def paramsService

    def list = {
        def annotations = []
        def projects = projectService.list()
        projects.each {
            annotations.addAll(userAnnotationService.listMap(it))
        }
        responseSuccess(annotations)
    }

    def listByImage = {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) responseSuccess(userAnnotationService.listMap(image))
        else responseNotFound("Image", params.id)
    }

    def listByProject = {
        Project project = projectService.read(params.long('id'), new Project())

        if (project) {
            Integer offset = params.offset!=null? params.getInt('offset') : 0
            Integer max = params.max!=null? params.getInt('max') : Integer.MAX_VALUE

            List<Long> userList = paramsService.getParamsUserList(params.users,project)
            List<Long> imageInstanceList = paramsService.getParamsImageInstanceList(params.images,project)

            def list = userAnnotationService.listMap(project, userList, imageInstanceList, (params.noTerm == "true"), (params.multipleTerm == "true"))
            if(params.offset!=null) responseSuccess([size:list.size(),collection:substract(list,offset,max)])
            else responseSuccess(list)
        }
        else responseNotFound("Project", params.id)
    }

    def listByImageAndUser = {
         def image = imageInstanceService.read(params.long('idImage'))
         def user = userService.read(params.idUser)
         if (image && user && params.bbox) {
            boolean notReviewedOnly = params.getBoolean("notreviewed")
            Geometry boundingbox = GeometryUtils.createBoundingBox(params.bbox)
            def data = userAnnotationService.listMap(image,user,boundingbox,notReviewedOnly)
            responseSuccess(data)
        }
         else if (image && user) responseSuccess(userAnnotationService.listMap(image, user))
         else if (!user) responseNotFound("User", params.idUser)
         else if (!image) responseNotFound("Image", params.idImage)
     }

    def listAnnotationByTerm = {
        Term term = termService.read(params.long('idterm'))
        //TODO:: improve this with a single SQL request
        if (term) responseSuccess(userAnnotationService.list(term))
        else responseNotFound("Annotation Term", "Term", params.idterm)
    }

    def listAnnotationByProjectAndTerm = {
        log.info "listAnnotationByProjectAndTerm"
        Term term = termService.read(params.long('idterm'))
        Project project = projectService.read(params.long('idproject'), new Project())
        Integer offset = params.offset!=null? params.getInt('offset') : 0
        Integer max = params.max!=null? params.getInt('max') : Integer.MAX_VALUE

        List<Long> userList = paramsService.getParamsUserList(params.users,project)
        List<Long> imageInstanceList = paramsService.getParamsImageInstanceList(params.images,project)

        if (term == null) responseNotFound("Term", params.idterm)
        else if (project == null) responseNotFound("Project", params.idproject)
        else if(!params.suggestTerm) {
            def list = []
            if (userList.isEmpty()) list = []
            else if (imageInstanceList.isEmpty()) list = []
            else {
                list = userAnnotationService.list(project,term,userList,imageInstanceList)
            }
            if(params.offset!=null) responseSuccess([size:list.size(),collection:mergeResults(substract(list,offset,max))])
            else responseSuccess(list)
        }
        else {
            Term suggestedTerm = termService.read(params.suggestTerm)
            //TODO:: improve this with a single SQL request
            def list = userAnnotationService.list(project, userList, term, suggestedTerm, Job.read(params.long('job')))
            if(params.offset!=null) responseSuccess([size:list.size(),collection:mergeResults(substract(list,offset,max))])
            else responseSuccess(list)
        }
    }

       //return a list of annotation (if list = [[annotation1,rate1, term1, expectedTerm1],..], add rate value in annotation]
       private def mergeResults(def list) {
           //list = [ [a,b],...,[x,y]]  => [a.rate = b, x.rate = y...]
           if(list.isEmpty() || list[0] instanceof UserAnnotation || list[0].class.equals("be.cytomine.ontology.UserAnnotation")) return list
           def result = []
           list.each {
               UserAnnotation annotation = it[0]
               annotation.rate = it[1]
               annotation.idTerm = it[2]
               annotation.idExpectedTerm = it[3]
               result << annotation
           }
           return result
       }


    def downloadDocumentByProject = {  //and filter by users and terms !
        // Export service provided by Export plugin

        Project project = projectService.read(params.long('id'),new Project())
        if (!project) responseNotFound("Project", params.long('id'))

        projectService.checkAuthorization(project)
        def users = []
        if (params.users != null && params.users != "") {
            params.users.split(",").each { id ->
                users << Long.parseLong(id)
            }
        }
        def terms = []
        if (params.terms != null && params.terms != "") {
            params.terms.split(",").each {  id ->
                terms << Long.parseLong(id)
            }
        }
        def images = []
        if (params.images != null && params.images != "") {
            params.images.split(",").each {  id ->
                images << Long.parseLong(id)
            }
        }
        def termsName = Term.findAllByIdInList(terms).collect{ it.toString() }
        def usersName = SecUser.findAllByIdInList(users).collect{ it.toString() }
        def imageInstances = ImageInstance.findAllByIdInList(images)

        if (params?.format && params.format != "html") {
            def exporterIdentifier = params.format;
            if (exporterIdentifier == "xls") exporterIdentifier = "excel"
            response.contentType = grailsApplication.config.grails.mime.types[params.format]
            SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
            String datePrefix = simpleFormat.format(new Date())
            response.setHeader("Content-disposition", "attachment; filename=${datePrefix}_annotations_project${project.id}.${params.format}")

            def annotations = UserAnnotation.createCriteria().list {
                eq("project", project)
                inList("image", imageInstances)
                inList("user.id", users)
            }

            def annotationTerms = AnnotationTerm.createCriteria().list {
                inList("userAnnotation", annotations)
                inList("term.id", terms)
                order("term.id", "asc")
            }

            def exportResult = []
            annotationTerms.each { annotationTerm ->
                UserAnnotation annotation = annotationTerm.userAnnotation
                def centroid = annotation.getCentroid()
                Term term = annotationTerm.term
                def data = [:]
                data.id = annotation.id
                data.area = annotation.computeArea()
                data.perimeter = annotation.computePerimeter()
                if (centroid != null) {
                    data.XCentroid = (int) Math.floor(centroid.x)
                    data.YCentroid = (int) Math.floor(centroid.y)
                } else {
                    data.XCentroid = "undefined"
                    data.YCentroid = "undefined"
                }
                data.image = annotation.image.id
                data.filename = annotation.getFilename()
                data.user = annotation.user.toString()
                data.term = term.name
                data.cropURL =UrlApi.getUserAnnotationCropWithAnnotationId(grailsApplication.config.grails.serverURL,annotation.id)
                data.cropGOTO = UrlApi.getAnnotationURL(grailsApplication.config.grails.serverURL,annotation?.image?.project?.id, annotation.image.id, annotation.id)
                exportResult.add(data)
            }

            List fields = ["id", "area", "perimeter", "XCentroid", "YCentroid", "image", "filename", "user", "term", "cropURL", "cropGOTO"]
            Map labels = ["id": "Id", "area": "Area (µm²)", "perimeter": "Perimeter (µm)", "XCentroid" : "X", "YCentroid" : "Y", "image": "Image Id", "filename": "Image Filename", "user": "User", "term": "Term", "cropURL": "View userannotation picture", "cropGOTO": "View userannotation on image"]
            String title = "Annotations in " + project.getName() + " created by " + usersName.join(" or ") + " and associated with " + termsName.join(" or ") + " @ " + (new Date()).toLocaleString()

            exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.04,0.06,0.06,0.04, 0.04, 0.04,0.08,0.06,0.06,0.25,0.25], "title": title, "csv.encoding": "UTF-8", "separator": ";"])
        }
    }


    def addComment = {
        //try {
        User sender = User.read(springSecurityService.principal.id)
        UserAnnotation annotation = UserAnnotation.read(params.getLong('userannotation'))
        log.info "add comment from " + sender + " and userannotation " + annotation
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
        List<User> receivers = request.JSON.users.collect { userID ->
            User.read(userID)
        }
        String[] receiversEmail = new String[receivers.size()]
        for (int i = 0; i < receivers.size(); i++) {
            receiversEmail[i] = receivers[i].getEmail();
        }
        log.info "send mail to " + receiversEmail
        def sharedAnnotation = new SharedAnnotation(
                sender : sender,
                receiver : receivers,
                comment : request.JSON.comment,
                userAnnotation: annotation
        )
        def attachments = []
        if (annnotationCrop != null) attachments << [cid : "annotation", file : annnotationCrop]
        if (sharedAnnotation.save()) {
            mailService.send("cytomine.ulg@gmail.com", receiversEmail, sender.getEmail(), request.JSON.subject, request.JSON.message, attachments)
            response([success: true, message: "Annotation shared to " + receivers.toString()], 200)
        } else {
            response([success: false, message: "Error"], 400)
        }
    }

    def showComment = {
        UserAnnotation annotation = userAnnotationService.read(params.long('userannotation'))
        if (!annotation)  responseNotFound("Annotation", params.annotation)
        userAnnotationService.checkAuthorization(annotation.project)
        def sharedAnnotation = SharedAnnotation.findById(params.long('id'))
        if (!sharedAnnotation) responseNotFound("SharedAnnotation", params.id)
        responseSuccess(sharedAnnotation)
    }

    def listComments = {
        UserAnnotation annotation = userAnnotationService.read(params.long('userannotation'))
        User user = User.read(springSecurityService.principal.id)
        if (annotation) {
            userAnnotationService.checkAuthorization(annotation.project)
            def sharedAnnotations = SharedAnnotation.createCriteria().list {
                eq("userAnnotation", annotation)
                or {
                    eq("sender", user)
                    receiver {
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

    def show = {
        UserAnnotation annotation = userAnnotationService.read(params.long('id'))

        if (annotation) {
            userAnnotationService.checkAuthorization(annotation.project)
            responseSuccess(annotation)
        }
        else responseNotFound("Annotation", params.id)
    }

    public void cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()

    }

    def add = {
        add(userAnnotationService, request.JSON)
    }

    @Override
    public Object addOne(def service, def json) {
        if(!json.project || json.isNull('project')) {
            ImageInstance image = ImageInstance.read(json.image)
            if(image) json.project = image.project.id
        }
        if(json.isNull('project')) throw new WrongArgumentException("Annotation must have a valide project:"+json.project)
        if(json.isNull('location')) throw new WrongArgumentException("Annotation must have a valide geometry:"+json.location)
        userAnnotationService.checkAuthorization(Long.parseLong(json.project.toString()), new UserAnnotation())
        def result = userAnnotationService.add(json)
        return result
    }

    def update= {
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


    def delete = {
        def json = JSON.parse("{id : $params.id}")
        delete(userAnnotationService, json)
    }

    private def substract(List collection, Integer offset, Integer max) {
        //TODO:: extract
        if (offset>=collection.size()) return []

        def maxForCollection = Math.min(collection.size()-offset,max)
        log.info "collection=${collection.size()} offset=$offset max=$max compute=${collection.size()-offset} maxForCollection=$maxForCollection"
        return collection.subList(offset,offset+maxForCollection)
    }
}
