package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.GeometryUtils
import com.vividsolutions.jts.geom.Geometry
import grails.converters.JSON
import groovy.sql.Sql
import org.codehaus.groovy.grails.web.json.JSONArray

import java.text.SimpleDateFormat
import be.cytomine.SecurityCheck

/**
 * Controller that handle request on annotation created by software (job)
 * Annotation my be created by humain (RestUserAnnotationController).
 */
class RestAlgoAnnotationController extends RestController {

    def exportService
    def algoAnnotationService
    def domainService
    def termService
    def imageInstanceService
    def userService
    def projectService
    def cytomineService
    def dataSource
    def algoAnnotationTermService
    def paramsService

    /**
     * List all annotation (created by algo) visible for the current user
     */
    def list = {
        def annotations = []
        //get all user's project and list all algo annotation
        def projects = projectService.list()
        projects.each {
            annotations.addAll(algoAnnotationService.list(it))
        }
        responseSuccess(annotations)
    }

    /**
     * Read a single algo annotation
     */
    def show = {
        AlgoAnnotation annotation = algoAnnotationService.read(params.long('id'))
        if (annotation) {
            responseSuccess(annotation)
        }
        else {
            responseNotFound("Annotation", params.id)
        }
    }

    /**
     * Add an annotation created by an algo
     * If JSON request params is an object, create a new annotation
     * If its a json array, create multiple annotation
     */
    def add = {
        def json = request.JSON
        try {
            if (json instanceof JSONArray) {
                responseResult(addMultiple(algoAnnotationService, json))
            } else {
                responseResult(addOne(algoAnnotationService, json))
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @Override
    public Object addOne(def service, def json) {
        if ((!json.project || json.isNull('project'))) {
            //fill project id thanks to image info
            ImageInstance image = ImageInstance.read(json.image)
            if (image) {
                json.project = image.project.id
            }
        }
        if (json.isNull('project')) {
            throw new WrongArgumentException("Annotation must have a valide project:" + json.project)
        }
        if (json.isNull('location')) {
            throw new WrongArgumentException("Annotation must have a valide geometry:" + json.location)
        }
        def result = algoAnnotationService.add(json,new SecurityCheck())
        return result
    }

    /**
     * Update a single annotation created by algo
     */
    def update = {
        def json = request.JSON
        try {
            //get annotation from DB
            def domain = algoAnnotationService.retrieve(json)
            //update it thanks to JSON in request
            def result = algoAnnotationService.update(json,new SecurityCheck(domain))
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Delete a single annotation created by algo
     */
    def delete = {
        def json = JSON.parse("{id : $params.id}")
        delete(algoAnnotationService, json)
    }

    /**
     * List annotation created by algo for a specific image.
     * This image must be accessible for the current user
     */
    def listByImage = {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) {
            responseSuccess(algoAnnotationService.list(image))
        }
        else {
            responseNotFound("Image", params.id)
        }
    }

    /**
     * List annotation created by algo for a specific project
     */
    def listByProject = {
        Project project = projectService.read(params.long('id'), new Project())

        if (project) {
            Integer offset = params.offset != null ? params.getInt('offset') : 0
            Integer max = params.max != null ? params.getInt('max') : Integer.MAX_VALUE
            //retrieve all image/user for the filter
            List<Long> userList = paramsService.getParamsSecUserList(params.users,project)
            List<Long> imagesList = paramsService.getParamsImageInstanceList(params.images,project)

            def list = algoAnnotationService.list(project, userList, imagesList, (params.noTerm == "true"), (params.multipleTerm == "true"))
            if (params.offset != null) {
                responseSuccess([size: list.size(), collection: substract(list, offset, max)])
            }
            else {
                responseSuccess(list)
            }
        }
        else {
            responseNotFound("Project", params.id)
        }
    }

    /**
     * List all annotation created by algo for a specific image and user.
     * If bbox params is set, it will retrieve annotation in the bbox specified area
     */
    def listByImageAndUser = {

        def image = imageInstanceService.read(params.long('idImage'))
        def user = userService.read(params.idUser)
        String bbox = params.bbox
        boolean notReviewedOnly = params.getBoolean("notreviewed")

        if (image && user && bbox) {
            responseSuccess(algoAnnotationService.list(image,user,bbox,notReviewedOnly))
        } else if (image && user) {
            responseSuccess(algoAnnotationService.list(image, user))
        } else if (!user) {
            responseNotFound("User", params.idUser)
        } else if (!image) {
            responseNotFound("Image", params.idImage)
        }
    }

    /**
     * List all annotation created by algo for specific project and a term
     */
    def listAnnotationByProjectAndTerm = {

        Term term = termService.read(params.long('idterm'))
        Project project = projectService.read(params.long('idproject'), new Project())
        Integer offset = params.offset != null ? params.getInt('offset') : 0
        Integer max = params.max != null ? params.getInt('max') : Integer.MAX_VALUE

        if (project) {

            List<Long> userList = paramsService.getParamsUserList(params.users,project)
            List<Long> imagesList = paramsService.getParamsImageInstanceList(params.images,project)

            if (term == null) {
                responseNotFound("Term", params.idterm)
            } else if (!params.suggestTerm) {
                def list = algoAnnotationService.listForUserJob(project, term, userList, imagesList)
                if (params.offset != null) {
                    responseSuccess([size: list.size(), collection: mergeResults(substract(list, offset, max))])
                }
                else {
                    responseSuccess(list)
                }
            }
        } else {
            responseNotFound("Project", params.id)
        }
    }


    def downloadDocumentByProject = {
        //TODO:: should be refactor! We should have a specific service
        //Export service provided by Export plugin

        Project project = projectService.read(params.long('id'), new Project())
        if (!project) {
            responseNotFound("Project", params.long('id'))
        }

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

            def annotations = AlgoAnnotation.createCriteria().list {
                eq("project", project)
                inList("image", imageInstances)
                inList("user.id", users)
            }

            def annotationsId = annotations.collect {it.id}

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
                data.cropURL = UrlApi.getUserAnnotationCropWithAnnotationId(grailsApplication.config.grails.serverURL, annotation.id)
                data.cropGOTO = UrlApi.getAnnotationURL(grailsApplication.config.grails.serverURL, annotation?.image?.project?.id, annotation.image.id, annotation.id)
                exportResult.add(data)
            }

            List fields = ["id", "area", "perimeter", "XCentroid", "YCentroid", "image", "filename", "user", "term", "cropURL", "cropGOTO"]
            Map labels = ["id": "Id", "area": "Area (µm²)", "perimeter": "Perimeter (µm)", "XCentroid": "X", "YCentroid": "Y", "image": "Image Id", "filename": "Image Filename", "user": "User", "term": "Term", "cropURL": "View userannotation picture", "cropGOTO": "View userannotation on image"]
            String title = "Annotations in " + project.getName() + " created by " + usersName.join(" or ") + " and associated with " + termsName.join(" or ") + " @ " + (new Date()).toLocaleString()

            exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.04, 0.06, 0.06, 0.04, 0.04, 0.04, 0.08, 0.06, 0.06, 0.25, 0.25], "title": title, "csv.encoding": "UTF-8", "separator": ";"])
        }
    }

    /**
     * Do union operation between annotation from the same image, user and term.
     * Params are:
     * -minIntersectionLength: size of the intersection geometry between two annotation to merge them
     * -bufferLength: tolerance threshold for two annotation (if they are very close but not intersect)
     */
    def union = {
        ImageInstance image = ImageInstance.read(params.getLong('idImage'))
        SecUser user = SecUser.read(params.getLong('idUser'))
        Term term = Term.read(params.getLong('idTerm'))
        Integer minIntersectLength = params.getInt('minIntersectionLength')
        Integer bufferLength = params.getInt('bufferLength')
        if (!image) {
            responseNotFound("ImageInstance", params.getLong('idImage'))
        }
        else if (!term) {
            responseNotFound("Term", params.getLong('idTerm'))
        }
        else if (!user) {
            responseNotFound("User", params.getLong('idUser'))
        }
        else {
            unionAnnotations(image, user, term, minIntersectLength, bufferLength)
            def data = [:]
            data.annotationunion = [:]
            data.annotationunion.status = "ok"
            responseSuccess(data)
        }
    }

    /**
     * Merge all annotation from the image, user and term that touch with min minIntersectLength size and with a tolerance threshold bufferLength
     * @param image Image
     * @param user User
     * @param term Term
     * @param minIntersectLength  size of the intersection geometry between two annotation to merge them
     * @param bufferLength tolerance threshold for two annotation (if they are very close but not intersect)
     */
    private def unionAnnotations(ImageInstance image, SecUser user, Term term, Integer minIntersectLength, Integer bufferLength) {
        int i = 0
        if (bufferLength) {
            //first, bufferize all annotations, so that we have "valid polygon"
            List<AlgoAnnotation> annotations = AlgoAnnotation.findAllByImageAndUser(image, user)
            annotations.each {
                if (AlgoAnnotationTerm.findWhere(annotationIdent: it.id, userJob: user, term: term)) {
                    it.location = it.location.buffer(bufferLength)
                    it.save(flush: true)
                }
            }
        }

        boolean restart = unionPostgisSQL(image, user, term, minIntersectLength, bufferLength)
        /**
         * Do union while previous union has merge something.
         * If a touch b and b touch c (but a don't touch c), with a single union, we may have
         * a merge with b
         * c merge with b
         * => a & c should be merge
         */
        while (restart && i < 100) {
            restart = unionPostgisSQL(image, user, term, minIntersectLength, bufferLength)
            i++
        }
    }

    private boolean unionPostgisSQL(ImageInstance image, SecUser user, Term term, Integer minIntersectLength, Integer bufferLength) {
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
        annotations.each {
            if (!it.location.isValid()) {
                it.location = it.location.buffer(0)
                it.save(flush: true)
            }
        }

        String request
        if (bufferLength == null) {
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



    /**
     * Return a list of annotation
     *  (if list = [[annotation1,rate1, term1, expectedTerm1],..], add rate,term &exo term value in annotation]
     * @param list
     * @return
     */
    private def mergeResults(def list) {
        //list = [ [a,b],...,[x,y]]  => [a.rate = b, x.rate = y...]
        if (list.isEmpty() || list[0] instanceof AlgoAnnotation) return list
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
}
