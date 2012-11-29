package be.cytomine.api.ontology

import be.cytomine.Exception.CytomineException

import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotationTerm

import be.cytomine.ontology.Term

import be.cytomine.project.Project
import be.cytomine.security.SecUser

import grails.converters.JSON
import groovy.sql.Sql

import java.text.SimpleDateFormat

import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.AnnotationDomain
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.ontology.UserAnnotation
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions
import be.cytomine.utils.GeometryUtils

class RestAlgoAnnotationController extends RestController {

    def exportService
    def grailsApplication
    def algoAnnotationService
    def domainService
    def termService
    def imageInstanceService
    def userService
    def projectService
    def cytomineService
    def dataSource
    def algoAnnotationTermService

    def list = {
        def annotations = []
        def projects = projectService.list()
        projects.each {
            annotations.addAll(algoAnnotationService.list(it))
        }
        responseSuccess(annotations)
    }

    def show = {
        AlgoAnnotation annotation = algoAnnotationService.read(params.long('id'))
        if (annotation) {
            algoAnnotationService.checkAuthorization(annotation.project)
            responseSuccess(annotation)
        }
        else responseNotFound("Annotation", params.id)
    }


    def add = {
        def json = request.JSON
        try {
            if (json instanceof JSONArray) {
                responseResult(addMultiple(algoAnnotationService, json))
            } else {
                responseResult(addOne(algoAnnotationService, json))
            }
        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @Override
    public Object addOne(def service, def json) {
        if(!json.project || json.isNull('project')) {
            ImageInstance image = ImageInstance.read(json.image)
            if(image) json.project = image.project.id
        }
        if(json.isNull('project')) throw new WrongArgumentException("Annotation must have a valide project:"+json.project)
        if(json.isNull('location')) throw new WrongArgumentException("Annotation must have a valide geometry:"+json.location)
        algoAnnotationService.checkAuthorization(Long.parseLong(json.project.toString()), new UserAnnotation())
        def result = algoAnnotationService.add(json)
        return result
    }

    def update= {
        def json = request.JSON
        try {
            def domain = algoAnnotationService.retrieve(json)
            def result = algoAnnotationService.update(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def delete = {
        def json = JSON.parse("{id : $params.id}")
        delete(algoAnnotationService, json)
    }

    def listByImage = {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) responseSuccess(algoAnnotationService.list(image))
        else responseNotFound("Image", params.id)
    }

    def listByProject = {
        Project project = projectService.read(params.long('id'), new Project())

        if (project) {
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
            def list = algoAnnotationService.list(project, userList, imageInstanceList, (params.noTerm == "true"), (params.multipleTerm == "true"))
            if(params.offset!=null) responseSuccess([size:list.size(),collection:substract(list,offset,max)])
            else responseSuccess(list)
        }
        else responseNotFound("Project", params.id)
    }

    def listByImageAndUser = {
        println "listByImageAndUser"
        def image = imageInstanceService.read(params.long('idImage'))
        def user = userService.read(params.idUser)
        if (image && user && params.bbox) {
            //responseSuccess(algoAnnotationService.list(image, user, (String) params.bbox, params.getBoolean("notreviewed")))
             String bbox = params.bbox
             boolean notReviewedOnly = params.getBoolean("notreviewed")

            Geometry boundingbox = GeometryUtils.createBoundingBox(bbox)

            println "boundingbox.toString()=" + boundingbox.toString()
            String request

            if(!notReviewedOnly) {
                request = "SELECT annotation.id, AsText(annotation.location), at.term_id \n" +
                    " FROM algo_annotation annotation LEFT OUTER JOIN algo_annotation_term at ON annotation.id = at.annotation_ident\n" +
                    " WHERE annotation.image_id = $image.id\n" +
                    " AND annotation.user_id= $user.id\n" +
                    " AND ST_within(annotation.location,GeometryFromText('" + boundingbox.toString() + "',0)) " +
                    " ORDER BY annotation.id "

            } else {
                request = "SELECT annotation.id, AsText(annotation.location), at.term_id \n" +
                    " FROM algo_annotation annotation LEFT OUTER JOIN algo_annotation_term at ON annotation.id = at.annotation_ident\n" +
                    " WHERE annotation.image_id = $image.id\n" +
                    " AND annotation.user_id= $user.id\n" +
                    " AND annotation.count_reviewed_annotations = 0 " +
                    " AND ST_within(annotation.location,GeometryFromText('" + boundingbox.toString() + "',0)) " +
                    " ORDER BY annotation.id "
            }

            println "REQUEST=" + request
            def sql = new Sql(dataSource)

            def data = []
            long lastAnnotationId = -1
            sql.eachRow(request) {

                long idAnnotation = it[0]
                String location = it[1]
                def idTerm = it[2]


                if(idAnnotation!=lastAnnotationId) {
                    data << [id: idAnnotation, location: location, term:  idTerm? [idTerm]:[]]
                } else {
                    if(idTerm)
                        data.last().term.add(idTerm)
                }
                lastAnnotationId = idAnnotation
            }
            responseSuccess(data)
        }
        else if (image && user) responseSuccess(algoAnnotationService.list(image, user))
        else if (!user) responseNotFound("User", params.idUser)
        else if (!image) responseNotFound("Image", params.idImage)
    }

    //TODO:: a method that list annotation from algo by project, user and term: merge
    def listAnnotationByProjectAndTerm = {
        log.info "listAnnotationByProjectAndTerm"
        Term term = termService.read(params.long('idterm'))
        Project project = projectService.read(params.long('idproject'), new Project())

        if(project) {

            Integer offset = params.offset!=null? params.getInt('offset') : 0
            Integer max = params.max!=null? params.getInt('max') : Integer.MAX_VALUE

            log.info "offset=$offset max=$max"

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
            else if(!params.suggestTerm) {
                def list = algoAnnotationService.listForUserJob(project, term, userList, imageInstanceList)
                if(params.offset!=null) responseSuccess([size:list.size(),collection:mergeResults(substract(list,offset,max))])
                else responseSuccess(list)
            }
        } else {
            responseNotFound("Project", params.id)
        }
    }




       //return a list of annotation (if list = [[annotation1,rate1, term1, expectedTerm1],..], add rate value in annotation]
       private def mergeResults(def list) {
           //list = [ [a,b],...,[x,y]]  => [a.rate = b, x.rate = y...]
           if(list.isEmpty() || list[0] instanceof AlgoAnnotation) return list
           def result = []
           list.each {
               AnnotationDomain annotation = it[0]
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

            def annotations = AlgoAnnotation.createCriteria().list {
                eq("project", project)
                inList("image", imageInstances)
                inList("user.id", users)
            }

            def annotationsId = annotations.collect{it.id}

            def annotationTerms = AlgoAnnotationTerm.createCriteria().list {
                inList("annotationIdent", annotationsId)
                inList("term.id", terms)
                order("term.id", "asc")
            }

            def exportResult = []
            annotationTerms.each { annotationTerm ->
                AnnotationDomain annotation = annotationTerm.retrieveAnnotationDomain()
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


    private def substract(List collection, Integer offset, Integer max) {
        if (offset>=collection.size()) return []

        def maxForCollection = Math.min(collection.size()-offset,max)
        log.info "collection=${collection.size()} offset=$offset max=$max compute=${collection.size()-offset} maxForCollection=$maxForCollection"
        return collection.subList(offset,offset+maxForCollection)
    }


    def union = {
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
            List<AlgoAnnotation> annotations = AlgoAnnotation.findAllByImageAndUser(image, user)
            log.info "Buffer($bufferLength) annotations..."
            annotations.each {
                if(AlgoAnnotationTerm.findWhere(annotationIdent: it.id,userJob:user,term: term)) {
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
        log.info "#TIME#=" + (end - start)
    }

    private boolean unionPostgisSQL(ImageInstance image, SecUser user, Term term,Integer minIntersectLength,Integer bufferLength) {
        log.info "unionPostgisSQL"

        log.info "image=$image"
        log.info "user=$user"
        log.info "term=$term"
        log.info "minIntersectLength=$minIntersectLength"
        log.info "bufferLength=$bufferLength"

        boolean mustBeRestart = false
        //key = deleted annotation, value = annotation that take in the deleted annotation
        //If y is deleted and merge with x, we add an entry <y,x>. Further if y had intersection with z, we replace "y" (deleted) by "x" (has now intersection with z).
        HashMap<Long, Long> removedByUnion = new HashMap<Long, Long>(1024)

        List<AlgoAnnotation> annotations = AlgoAnnotation.findAllByImageAndUser(image, user)
        log.info "valide algoannotation..."
        annotations.each {
            if (!it.location.isValid()) {
                it.location = it.location.buffer(0)
                it.save(flush: true)
            }
        }

        String request
        if(bufferLength==null) {
            request = "SELECT annotation1.id as id1, annotation2.id as id2\n" +
                    " FROM algo_annotation annotation1, algo_annotation annotation2, algo_annotation_term at1, algo_annotation_term at2\n" +
                    " WHERE annotation1.image_id = $image.id\n" +
                    " AND annotation2.image_id = $image.id\n" +
                    " AND annotation2.created > annotation1.created\n" +
                    " AND annotation1.user_id = ${user.id}\n" +
                    " AND annotation2.user_id = ${user.id}\n" +
                    " AND annotation1.id = at1.annotation_ident\n" +
                    " AND annotation2.id = at2.annotation_ident\n" +
                    " AND at1.term_id = ${term.id}\n" +
                    " AND at2.term_id = ${term.id}\n" +
                    " AND ST_Perimeter(ST_Intersection(annotation1.location, annotation2.location))>=$minIntersectLength\n"



        } else {
            request = "SELECT annotation1.id as id1, annotation2.id as id2\n" +
                    " FROM algo_annotation annotation1, algo_annotation annotation2, algo_annotation_term at1, algo_annotation_term at2\n" +
                    " WHERE annotation1.image_id = $image.id\n" +
                    " AND annotation2.image_id = $image.id\n" +
                    " AND annotation2.created > annotation1.created\n" +
                    " AND annotation1.user_id = ${user.id}\n" +
                    " AND annotation2.user_id = ${user.id}\n" +
                    " AND annotation1.id = at1.annotation_ident\n" +
                    " AND annotation2.id = at2.annotation_ident\n" +
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

            AlgoAnnotation based = AlgoAnnotation.get(idBased)
            AlgoAnnotation compared = AlgoAnnotation.get(idCompared)

            if (based && compared && based.id != compared.id) {
                mustBeRestart = true
                based.location = based.location.union(compared.location)
                removedByUnion.put(compared.id, based.id)
                //save new annotation with union location

                domainService.saveDomain(based)
                //remove old annotation with data
                AlgoAnnotationTerm.executeUpdate("delete AlgoAnnotationTerm aat where aat.annotationIdent = :annotation", [annotation: compared.id])
                domainService.deleteDomain(compared)

            }
        }
        return mustBeRestart
    }
}
