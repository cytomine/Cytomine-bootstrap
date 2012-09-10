package be.cytomine.api.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Annotation
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
import groovy.sql.Sql
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.security.UserJob

class RestAnnotationController extends RestController {

    def exportService
    def grailsApplication
    def annotationService
    def domainService
    def termService
    def imageInstanceService
    def userService
    def projectService
    def cytomineService
    def mailService
    def dataSource



    def list = {
        def annotations = []
        def projects = projectService.list()
        projects.each {
            annotations.addAll(annotationService.list(it))
        }
        responseSuccess(annotations)
    }

    def listByImage = {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) responseSuccess(annotationService.list(image))
        else responseNotFound("Image", params.id)
    }

    def listByProject = {
        Project project = projectService.read(params.long('id'), new Project())
        Integer offset = params.offset!=null? params.getInt('offset') : 0
        Integer max = params.max!=null? params.getInt('max') : Integer.MAX_VALUE
        Collection<SecUser> userList = []
        if (params.users != null && params.users != "null") {
            if (params.users != "") userList = userService.list(project, params.users.split("_").collect{ Long.parseLong(it)})
        }
        else {
            userList = userService.list(project)
        }
        Collection<ImageInstance> imageInstanceList = []
        if (params.images != null && params.images != "null") {
            if (params.images != "") imageInstanceList = imageInstanceService.list(project, params.images.split("_").collect{ Long.parseLong(it)})
        } else {
            imageInstanceList = imageInstanceService.list(project)
        }
        def sessionFactory

        if (project) {
            def list = annotationService.list(project, userList, imageInstanceList, (params.noTerm == "true"), (params.multipleTerm == "true"))
            if(params.offset!=null) responseSuccess([size:list.size(),collection:substract(list,offset,max)])
            else responseSuccess(list)
        }
        else responseNotFound("Project", params.id)
    }

    def listByImageAndUser = {
        def image = imageInstanceService.read(params.long('idImage'))
        def user = userService.read(params.idUser)
        println "xxxlistByImageAndUser="+image.project
        if (image && user && params.bbox) {
            responseSuccess(annotationService.list(image, user, (String) params.bbox))
        }
        else if (image && user) responseSuccess(annotationService.list(image, user))
        else if (!user) responseNotFound("User", params.idUser)
        else if (!image) responseNotFound("Image", params.idImage)
    }

    def listAnnotationByTerm = {
        Term term = termService.read(params.long('idterm'))
        if (term) responseSuccess(annotationService.list(term))
        else responseNotFound("Annotation Term", "Term", params.idterm)
    }

    def listAnnotationByProjectAndTerm = {
        log.info "listAnnotationByProjectAndTerm"
        Term term = termService.read(params.long('idterm'))
        Project project = projectService.read(params.long('idproject'), new Project())

        Integer offset = params.offset!=null? params.getInt('offset') : 0
        Integer max = params.max!=null? params.getInt('max') : Integer.MAX_VALUE


        println "offset=$offset max=$max"

        Collection<SecUser> userList = []
        if (params.users != null && params.users != "null") {
            if (params.users != "") userList = userService.list(project, params.users.split("_").collect{ Long.parseLong(it)})
        }
        else {
            userList = userService.list(project)
        }

        log.info "userList="+userList
        Collection<ImageInstance> imageInstanceList = []
        if (params.images != null && params.images != "null") {
            if (params.images != "") imageInstanceList = imageInstanceService.list(project, params.images.split("_").collect{ Long.parseLong(it)})
        } else {
            imageInstanceList = imageInstanceService.list(project)
        }

        if (term == null) responseNotFound("Term", params.idterm)
        else if (project == null) responseNotFound("Project", params.idproject)
        /*else if (userList.isEmpty()) responseNotFound("Users", params.users)
        else if (imageInstanceList.isEmpty()) responseNotFound("ImageInstance", params.images)*/
        else if(!params.suggestTerm) {
            def list = annotationService.list(project, term, userList, imageInstanceList)
            if(params.offset!=null) responseSuccess([size:list.size(),collection:substract(mergeResults(list),offset,max)])
            else responseSuccess(list)
        }
        else {
            Term suggestedTerm = termService.read(params.suggestTerm)
            def list = annotationService.list(project, userList, term, suggestedTerm, Job.read(params.long('job')))
            if(params.offset!=null) responseSuccess([size:list.size(),collection:substract(mergeResults(list),offset,max)])
            else responseSuccess(list)
        }
    }


       //return a list of annotation (if list = [[annotation1,rate1],..], add rate value in annotation]
       private def mergeResults(def list) {
           //list = [ [a,b],...,[x,y]]  => [a.rate = b, x.rate = y...]
           if(list.isEmpty() || list[0] instanceof Annotation) return list
           def result = []
           list.each {
               Annotation annotation = it[0]
               annotation.rate = it[1]
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
        if (params.users != null) {
            params.users.split(",").each { id ->
                users << Long.parseLong(id)
            }
        }
        def terms = []
        if (params.terms != null) {
            params.terms.split(",").each {  id ->
                terms << Long.parseLong(id)
            }
        }
        def images = []
        if (params.images != null) {
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

            def annotations = Annotation.createCriteria().list {
                eq("project", project)
                inList("image", imageInstances)
                inList("user.id", users)
            }

            def annotationTerms = AnnotationTerm.createCriteria().list {
                inList("annotation", annotations)
                inList("term.id", terms)
                order("term.id", "asc")
            }

            def exportResult = []
            annotationTerms.each { annotationTerm ->
                Annotation annotation = annotationTerm.annotation
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
                data.cropURL =UrlApi.getAnnotationCropWithAnnotationId(grailsApplication.config.grails.serverURL,annotation.id)
                data.cropGOTO = UrlApi.getAnnotationURL(grailsApplication.config.grails.serverURL,annotation?.image?.project?.id, annotation.image.id, annotation.id)
                exportResult.add(data)
            }

            List fields = ["id", "area", "perimeter", "XCentroid", "YCentroid", "image", "filename", "user", "term", "cropURL", "cropGOTO"]
            Map labels = ["id": "Id", "area": "Area (µm²)", "perimeter": "Perimeter (µm)", "XCentroid" : "X", "YCentroid" : "Y", "image": "Image Id", "filename": "Image Filename", "user": "User", "term": "Term", "cropURL": "View annotation picture", "cropGOTO": "View annotation on image"]
            String title = "Annotations in " + project.getName() + " created by " + usersName.join(" or ") + " and associated with " + termsName.join(" or ") + " @ " + (new Date()).toLocaleString()

            exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.04,0.06,0.06,0.04, 0.04, 0.04,0.08,0.06,0.06,0.25,0.25], "title": title, "csv.encoding": "UTF-8", "separator": ";"])
        }
    }


    def addComment = {
        //try {
        User sender = User.read(springSecurityService.principal.id)
        Annotation annotation = Annotation.read(request.JSON.annotation)
        log.info "add comment from " + sender + " and annotation " + annotation
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
                annotation: annotation
        )
        def attachments = []
        if (annnotationCrop != null) attachments << [cid : "annotation", file : annnotationCrop]
        if (sharedAnnotation.save()) {
            mailService.send("cytomine.ulg@gmail.com", receiversEmail, sender.getEmail(), request.JSON.subject, request.JSON.message, attachments)
            response([success: true, message: "Annotation shared to " + receivers.toString()], 200)
        } else {
            response([success: false, message: "Error"], 400)
        }
        /* } catch (Exception e) {
            response([success: false, message: e.toString()], 400)
        }*/

    }

    def showComment = {
        Annotation annotation = annotationService.read(params.long('annotation'))
        User user = User.read(springSecurityService.principal.id)
        if (!annotation)  responseNotFound("Annotation", params.annotation)
        annotationService.checkAuthorization(annotation.project)
        def sharedAnnotation = SharedAnnotation.findById(params.long('id'))
        if (!sharedAnnotation) responseNotFound("SharedAnnotation", params.id)
        responseSuccess(sharedAnnotation)
    }

    def listComments = {
        Annotation annotation = annotationService.read(params.long('annotation'))
        User user = User.read(springSecurityService.principal.id)
        if (annotation) {
            annotationService.checkAuthorization(annotation.project)
            def sharedAnnotations = SharedAnnotation.createCriteria().list {
                eq("annotation", annotation)
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

        Annotation annotation = annotationService.read(params.long('id'))

        if (annotation) {
            annotationService.checkAuthorization(annotation.project)
            responseSuccess(annotation)
        }
        else responseNotFound("Annotation", params.id)
    }

    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()

    }

    def add = {
        add(annotationService, request.JSON)
    }

    @Override
    def addOne(def service, def json) {
        if(!json.project || json.isNull('project')) {
            ImageInstance image = ImageInstance.read(json.image)
            if(image) json.project = image.project.id
        }
        if(json.isNull('project')) throw new WrongArgumentException("Annotation must have a valide project:"+json.project)
        if(json.isNull('location')) throw new WrongArgumentException("Annotation must have a valide geometry:"+json.location)
        annotationService.checkAuthorization(Long.parseLong(json.project.toString()), new Annotation())
        def result = annotationService.add(json)
        return result
    }

    def update= {
        def json = request.JSON
        try {
            def domain = annotationService.retrieve(json)
            def result = annotationService.update(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }


    def delete = {
        def json = JSON.parse("{id : $params.id}")
        delete(annotationService, json)
    }

    def copy = {
        Long idAnnotation = params.getLong('id')
        Annotation annotation = Annotation.read(idAnnotation)

        if(!annotation) responseNotFound("Annotation", params.id)
        else {
            try {
                annotationService.checkAuthorization(annotation.project)
                if(cytomineService.currentUser.id==annotation.user.id) throw new WrongArgumentException("You cannot copy your own annotation!")
                def json = JSON.parse(annotation.encodeAsJSON())
                json.id = null
                json.user = cytomineService.currentUser.id
                json.parent = idAnnotation
                def result = annotationService.add(json)
                responseResult(result)
            } catch (CytomineException e) {
                log.error(e)
                response([success: false, errors: e.msg], e.code)
            }
        }
    }


    private def substract(List collection, Integer offset, Integer max) {
        if (offset>=collection.size()) return []

        def maxForCollection = Math.min(collection.size()-offset,max)
        println "collection=${collection.size()} offset=$offset max=$max compute=${collection.size()-offset} maxForCollection=$maxForCollection"
        return collection.subList(offset,offset+maxForCollection)
    }


    def union() {
        ImageInstance image = ImageInstance.read(params.getLong('idImage'))
        SecUser user = SecUser.read(params.getLong('idUser'))
        Term term = Term.read(params.getLong('idTerm'))
        Integer minIntersectLength = params.getInt('minIntersectionLength')
        Integer bufferLength = params.getInt('bufferLength')
        if(!image) responseNotFound("ImageInstance",params.getLong('idImage'))
        else if(!term) responseNotFound("Term",params.getLong('idTerm'))
        else if(!user) responseNotFound("User",params.getLong('idUser'))
        else {
            unionAnnotations(image, user,term,minIntersectLength,bufferLength)
            def data = [:]
            data.annotationunion = [:]
            data.annotationunion.status = "ok"
            responseSuccess(data)
        }
    }

    private def unionAnnotations(ImageInstance image, SecUser user, Term term, Integer minIntersectLength,Integer bufferLength) {
        long start = System.currentTimeMillis()
        int i = 0

        if(bufferLength) {
            List<Annotation> annotations = Annotation.findAllByImageAndUser(image, user)
            println "Buffer($bufferLength) annotations..."
            annotations.each {
                if(AlgoAnnotationTerm.findWhere(annotation: it,userJob:user,term: term)) {
                    it.location = it.location.buffer(bufferLength)
                    it.save(flush: true)
                }
            }
        }

        boolean restart = unionPostgisSQL(image, user,term,minIntersectLength,bufferLength)
        while(restart && i<100) {
            restart = unionPostgisSQL(image, user,term,minIntersectLength,bufferLength)
            i++
        }

        long end = System.currentTimeMillis()
        println "#TIME#=" + (end - start)
    }

    private boolean unionPostgisSQL(ImageInstance image, SecUser user, Term term,Integer minIntersectLength,Integer bufferLength) {
        println "unionPostgisSQL"

        println "********************\n********************\n********************\n********************\n"
        println "image=$image"
        println "user=$user"
        println "term=$term"
        println "minIntersectLength=$minIntersectLength"
        println "bufferLength=$bufferLength"

        boolean mustBeRestart = false
        //key = deleted annotation, value = annotation that take in the deleted annotation
        //If y is deleted and merge with x, we add an entry <y,x>. Further if y had intersection with z, we replace "y" (deleted) by "x" (has now intersection with z).
        HashMap<Long, Long> removedByUnion = new HashMap<Long, Long>(1024)

        List<Annotation> annotations = Annotation.findAllByImageAndUser(image, user)
        println "valide annotation..."
        annotations.each {
            if (!it.location.isValid()) {
                it.location = it.location.buffer(0)
                it.save(flush: true)
            }
        }

        String request
        if(bufferLength==null) {
            request = "SELECT annotation1.id as id1, annotation2.id as id2\n" +
                    " FROM annotation annotation1, annotation annotation2, algo_annotation_term at1, algo_annotation_term at2\n" +
                    " WHERE annotation1.image_id = $image.id\n" +
                    " AND annotation2.image_id = $image.id\n" +
                    " AND annotation2.created > annotation1.created\n" +
                    " AND annotation1.user_id = ${user.id}\n" +
                    " AND annotation2.user_id = ${user.id}\n" +
                    " AND annotation1.id = at1.annotation_id\n" +
                    " AND annotation2.id = at2.annotation_id\n" +
                    " AND at1.term_id = ${term.id}\n" +
                    " AND at2.term_id = ${term.id}\n" +
                    " AND ST_length2d(ST_Intersection(annotation1.location, annotation2.location))>=$minIntersectLength\n"


        } else {
            request = "SELECT annotation1.id as id1, annotation2.id as id2\n" +
                    " FROM annotation annotation1, annotation annotation2, algo_annotation_term at1, algo_annotation_term at2\n" +
                    " WHERE annotation1.image_id = $image.id\n" +
                    " AND annotation2.image_id = $image.id\n" +
                    " AND annotation2.created > annotation1.created\n" +
                    " AND annotation1.user_id = ${user.id}\n" +
                    " AND annotation2.user_id = ${user.id}\n" +
                    " AND annotation1.id = at1.annotation_id\n" +
                    " AND annotation2.id = at2.annotation_id\n" +
                    " AND at1.term_id = ${term.id}\n" +
                    " AND at2.term_id = ${term.id}\n" +
                    " AND ST_Perimeter(ST_Intersection(ST_Buffer(annotation1.location,$bufferLength), ST_Buffer(annotation2.location,$bufferLength)))>=$minIntersectLength\n"
        }

        def sql = new Sql(dataSource)


        sql.eachRow(request) {

            long idBased = it[0]
            //check if annotation has be deleted (because merge), if true get the union annotation
            if (removedByUnion.containsKey(it[0]))
                idBased = removedByUnion.get(it[0])

            long idCompared = it[1]
            //check if annotation has be deleted (because merge), if true get the union annotation
            if (removedByUnion.containsKey(it[1]))
                idCompared = removedByUnion.get(it[1])

            Annotation based = Annotation.get(idBased)
            Annotation compared = Annotation.get(idCompared)

            if (based && compared && based.id != compared.id) {
                mustBeRestart = true
                based.location = based.location.union(compared.location)
                removedByUnion.put(compared.id, based.id)
                //save new annotation with union location

                domainService.saveDomain(based)
                //remove old annotation with data
                AlgoAnnotationTerm.executeUpdate("delete AlgoAnnotationTerm aat where aat.annotation = :annotation", [annotation: compared])
                domainService.deleteDomain(compared)

            }
        }
        return mustBeRestart
    }
}
