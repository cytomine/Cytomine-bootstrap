package be.cytomine.api.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.social.SharedAnnotation
import be.cytomine.sql.AnnotationListing
import be.cytomine.sql.UserAnnotationListing
import be.cytomine.utils.GeometryUtils
import com.vividsolutions.jts.geom.Geometry
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

    /**
     * List user annotation by image
     */
    def listByImage = {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) {
            responseSuccess(userAnnotationService.listLight(image,paramsService.getPropertyGroupToShow(params)))
        } else {
            responseNotFound("Image", params.id)
        }
    }

    /**
     * List user annotation by project
     */
    def listByProject = {
        Project project = projectService.read(params.long('id'))
        if (project) {
            boolean notReviewedOnly = params.getBoolean("notreviewed")
            List<Long> userList = paramsService.getParamsUserList(params.users, project)
            List<Long> imageInstanceList = paramsService.getParamsImageInstanceList(params.images, project)

            def list = userAnnotationService.listLight(project, userList, imageInstanceList, (params.noTerm == "true"), (params.multipleTerm == "true"),notReviewedOnly,paramsService.getPropertyGroupToShow(params))
            responseList(list)
        } else {
            responseNotFound("Project", params.id)
        }
    }

    /**
     * List by image and user
     * Bbbx params can be provide to select only annotation in a area
     */
    def listByImageAndUser = {
        println "listByImageAndUser"
        def image = imageInstanceService.read(params.long('idImage'))
        def user = secUserService.read(params.idUser)
        if (image && user && params.bbox) {
            boolean notReviewedOnly = params.getBoolean("notreviewed")
            Integer force = params.getInt('force')
            Geometry boundingbox = GeometryUtils.createBoundingBox(params.bbox)

            def data = userAnnotationService.listLight(image, user, boundingbox, notReviewedOnly,force,['basic','wkt','term'])
            responseSuccess(data)
        } else if (image && user) {
            responseSuccess(userAnnotationService.listLight(image, user,paramsService.getPropertyGroupToShow(params)))
        } else if (!user) {
            responseNotFound("User", params.idUser)
        } else if (!image) {
            responseNotFound("Image", params.idImage)
        }
    }

    /**
     * List annotation by project and term
     */
    def listAnnotationByProjectAndTerm = {
        Term term = termService.read(params.long('idterm'))
        Project project = projectService.read(params.long('idproject'))
        boolean notReviewedOnly = params.getBoolean("notreviewed")
        if (term == null) {
            responseNotFound("Term", params.idterm)
        } else if (project == null) {
            responseNotFound("Project", params.idproject)
        }
        else {
            List<Long> userList = paramsService.getParamsUserList(params.users, project)
            List<Long> imageInstanceList = paramsService.getParamsImageInstanceList(params.images, project)
            println "imageInstanceList="+imageInstanceList
            if (!params.suggestTerm) {
                def list
                if (userList.isEmpty() || imageInstanceList.isEmpty()) {
                    list = []
                } else {
                    list = userAnnotationService.list(project, term, userList, imageInstanceList,notReviewedOnly,paramsService.getPropertyGroupToShow(params))
                }
                list = mergeResults(list)
                responseList(list)
            }
            else {
                Term suggestedTerm = termService.read(params.suggestTerm)
                def list = userAnnotationService.list(project, userList, term, suggestedTerm, Job.read(params.long('job')),paramsService.getPropertyGroupToShow(params))
                responseSuccess(list)
            }
        }
    }


    /**
     * List all annotation with light format
     */
    def list = {
        responseSuccess(userAnnotationService.listLightForRetrieval())
    }


    def search = {

        AnnotationListing al = new UserAnnotationListing()
        al.columnToPrint = paramsService.getPropertyGroupToShow(params)
        al.project = params.getLong('project')
        al.user = params.getLong('user')
        al.term = params.getLong('term')
        al.image = params.getLong('image')
        al.suggestedTerm = params.getLong('suggestedTerm')

        def users = params.get('users')
        if(users) {
            al.users = params.get('users').split(",").collect{Long.parseLong(it)}
        }

        def images = params.get('images')
        if(images) {
            al.images = params.get('images').split(",").collect{Long.parseLong(it)}
        }

        def terms = params.get('terms')
        if(terms) {
            al.terms = params.get('terms').split(",").collect{Long.parseLong(it)}
        }

        def usersForTerm = params.get('usersForTerm')
        if(usersForTerm) {
            al.usersForTerm = params.get('usersForTerm').split(",").collect{Long.parseLong(it)}
        }

        def userForTermAlgo = params.get('userForTermAlgo')
        if(userForTermAlgo) {
            al.userForTermAlgo = params.get('userForTermAlgo').split(",").collect{Long.parseLong(it)}
        }

        al.notReviewedOnly = params.getBoolean('notReviewedOnly')
        al.noTerm = params.getBoolean('noTerm')
        al.multipleTerm = params.getBoolean('multipleTerm')
        al.bbox = params.get('bbox')

        responseSuccess(userAnnotationService.listGeneric(al))
    }

    /**
     * Download report with annotation
     * TODO:: this should be extract in a specific service (and use lot of same code as algo annotation
     * TODO:: request must be in secure service
     */
    def downloadDocumentByProject = {
        // Export service provided by Export plugin

        Project project = projectService.read(params.long('id'))
        if (!project) {
            responseNotFound("Project", params.long('id'))
        }

        //users=9331125,16&terms=-1,-2,9331340,9331346&images=9331299

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
        def termsName = Term.findAllByIdInList(terms).collect { it.toString() }
        def usersName = SecUser.findAllByIdInList(users).collect { it.toString() }
        def imageInstances = ImageInstance.findAllByIdInList(images)


        if (params?.format && params.format != "html") {
            def exporterIdentifier = params.format;
            if (exporterIdentifier == "xls") exporterIdentifier = "excel"
            response.contentType = grailsApplication.config.grails.mime.types[params.format]
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
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
                data.perimeterUnit = annotation.getPerimeterUnit()
                data.areaUnit = annotation.getAreaUnit()
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
                data.cropURL = UrlApi.getUserAnnotationCropWithAnnotationId(annotation.id)
                data.cropGOTO = UrlApi.getAnnotationURL(annotation?.image?.project?.id, annotation.image.id, annotation.id)
                exportResult.add(data)
            }

            List fields = ["id", "area", "perimeter", "XCentroid", "YCentroid", "image", "filename", "user", "term", "cropURL", "cropGOTO"]
            Map labels = ["id": "Id", "area": "Area (microns²)", "perimeter": "Perimeter (mm)", "XCentroid": "X", "YCentroid": "Y", "image": "Image Id", "filename": "Image Filename", "user": "User", "term": "Term", "cropURL": "View userannotation picture", "cropGOTO": "View userannotation on image"]
            String title = "Annotations in " + project.getName() + " created by " + usersName.join(" or ") + " and associated with " + termsName.join(" or ") + " @ " + (new Date()).toLocaleString()

            exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.04, 0.06, 0.06, 0.04, 0.04, 0.04, 0.08, 0.06, 0.06, 0.25, 0.25], "title": title, "csv.encoding": "UTF-8", "separator": ";"])
        }
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
        def result = userAnnotationService.add(json)
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
