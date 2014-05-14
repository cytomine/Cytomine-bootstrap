package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.processing.Job
import be.cytomine.processing.RoiAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.sql.*
import be.cytomine.utils.GeometryUtils
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import groovy.sql.Sql
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.annotation.RestApiResponseObject
import org.restapidoc.pojo.RestApiParamType

import java.text.SimpleDateFormat

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION

/**
 * Controller that handle request on annotation.
 * It's main utility is to redirect request to the correct controller: user/algo/reviewed
 */
@RestApi(name = "generic annotation services", description = "Methods for managing an annotation created by a software")
class RestAnnotationDomainController extends RestController {

    def userAnnotationService
    def termService
    def imageInstanceService
    def secUserService
    def projectService
    def cytomineService
    def dataSource
    def algoAnnotationService
    def reviewedAnnotationService
    def paramsService
    def exportService
    def annotationListingService
    def simplifyGeometryService
    def imageProcessingService
    def currentRoleServiceProxy

    def currentDomainName() {
        return "generic annotation" //needed because not RestAbstractImageController...
    }
    /**
     * Search service for all annotation type
     * see AnnotationListing for all filters available
     */
    @RestApiMethod(description="Search service for all annotation type. By default All fields are not visible (optim), you need to select fields using show/hideXXX query parameters.", listing = true)
    @RestApiResponseObject(objectIdentifier = "[annotation listing]")
    @RestApiParams(params=[
        @RestApiParam(name="showDefault", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, show 'basic', 'meta', and 'term' properties group. See showBasic/Meta/... for more information (default: true ONLY IF NO OTHER show/hideXXX are set)"),
        @RestApiParam(name="showBasic", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, show basic properties group (id, class...)"),
        @RestApiParam(name="showMeta", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, show meta properties group (urls, image id, project id,...)"),
        @RestApiParam(name="showWKT", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, show the location WKT properties. This may slow down the request!."),
        @RestApiParam(name="showGIS", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, show the form GIS field (area, centroid,...). This may slow down the request!."),
        @RestApiParam(name="showTerm", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, show the term properties (id, user who add the term,...). This may slow down the request."),
        @RestApiParam(name="showAlgo", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, show the algo details (job,...). This may slow down the request."),
        @RestApiParam(name="showUser", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, show the annotation user details (username,...). This may slow down the request."),
        @RestApiParam(name="showImage", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, show the annotation image details (filename,...). This may slow down the request."),
        @RestApiParam(name="hideBasic", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, hide basic properties group (id, class...)"),
        @RestApiParam(name="hideMeta", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, hide meta properties group (urls, image id, project id,...)"),
        @RestApiParam(name="hideWKT", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, hide the location WKT properties. This may slow down the request!."),
        @RestApiParam(name="hideGIS", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, hide the form GIS field (area, centroid,...). This may slow down the request!."),
        @RestApiParam(name="hideTerm", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, hide the term properties (id, user who add the term,...). This may slow down the request."),
        @RestApiParam(name="hideAlgo", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, hide the algo details (job,...). This may slow down the request."),
        @RestApiParam(name="hideUser", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, hide the annotation user details (username,...). This may slow down the request."),
        @RestApiParam(name="hideImage", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If true, hide the annotation image details (filename,...). This may slow down the request."),
        @RestApiParam(name="project", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotation for this project id"),
        @RestApiParam(name="job", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotation for this job id"),
        @RestApiParam(name="user", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotation for this user id"),
        @RestApiParam(name="jobForTermAlgo", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotation link with a term added by this job id"),
        @RestApiParam(name="term", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotation link with this term id"),
        @RestApiParam(name="image", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotation for this image id"),
        @RestApiParam(name="suggestedTerm", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotation suggested by for this term by a job"),
        @RestApiParam(name="userForTermAlgo", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) Get only user annotation link with a term added by this job id"),
        @RestApiParam(name="kmeansValue", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) Only used for GUI "),
        @RestApiParam(name="users", type="list", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotation for these users id"),
        @RestApiParam(name="images", type="list", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotation for these images id"),
        @RestApiParam(name="terms", type="list", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotation for these terms id"),
        @RestApiParam(name="notReviewedOnly", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) Only get annotation not reviewed"),
        @RestApiParam(name="noTerm", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) Only get annotation with no term"),
        @RestApiParam(name="noAlgoTerm", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) Only get annotation with no term from a job"),
        @RestApiParam(name="multipleTerm", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) Only get annotation with multiple terms"),
        @RestApiParam(name="kmeans", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) Enable or not kmeans (only for GUI)"),
        @RestApiParam(name="bbox", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotations having intersection with the bbox (WKT)"),
        @RestApiParam(name="bboxAnnotation", type="Long", paramType = RestApiParamType.QUERY, description = "(Optional) Get only annotations having intersection with this annotation")
    ])
    def search() {
         try {
             responseSuccess(doSearch(params).result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @RestApiMethod(description="Download report for annotation. !!! See doc for /annotation/search to filter annotations!!!", listing = true)
    @RestApiResponseObject(objectIdentifier =  "file")
    @RestApiParams(params=[
        @RestApiParam(name="format", type="string", paramType = RestApiParamType.QUERY, description = "(Optional) Output file format (pdf, xls,...)")
    ])
    def downloadSearched() {
        def lists = doSearch(params)
        downloadDocument(lists.result,lists.project)
    }

    /**
     * Get annotation crop (image area that frame annotation)
     * This work for all kinds of annotations
     */

    @RestApiMethod(description="Get annotation crop  (image area that frame annotation). This work for all kinds of annotations.")
    @RestApiResponseObject(objectIdentifier =  "file")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
        @RestApiParam(name="max_size", type="int", paramType = RestApiParamType.PATH,description = "Maximum size of the crop image (w and h)"),
        @RestApiParam(name="zoom", type="int", paramType = RestApiParamType.PATH,description = "Zoom level"),
        @RestApiParam(name="draw", type="boolean", paramType = RestApiParamType.PATH,description = "Draw annotation form border on the image")
    ])
    def crop () {
        try {
            def annotation = AnnotationDomain.getAnnotationDomain(params.long("id"))
            def image = imageProcessingService.crop(annotation, params)
            responseBufferedImage(image)
        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    /**
     * Get annotation crop (image area that frame annotation)
     * This work for all kinds of annotations
     */

    @RestApiMethod(description="Get annotation crop with minimal size (256*256max)  (image area that frame annotation). This work for all kinds of annotations.")
    @RestApiResponseObject(objectIdentifier =  "file")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
        @RestApiParam(name="zoom", type="int", paramType = RestApiParamType.PATH,description = "Zoom level"),
        @RestApiParam(name="draw", type="boolean", paramType = RestApiParamType.PATH,description = "Draw annotation form border on the image")
    ])
    def cropMin () {
        try {
            params.max_size = 256
            def annotation = AnnotationDomain.getAnnotationDomain(params.long("id"))
            def image = imageProcessingService.crop(annotation, params)
            responseBufferedImage(image)
        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    private doSearch(def params) {
        AnnotationListing al
        def result = []

        if(isReviewedAnnotationAsked(params)) {
            al = new ReviewedAnnotationListing()
            result = createRequest(al, params)
        } else if(isRoiAnnotationAsked(params)) {
            al = new RoiAnnotationListing()
            result = createRequest(al, params)
        } else if(isAlgoAnnotationAsked(params)) {
            al = new AlgoAnnotationListing()
            result.addAll(createRequest(al, params))
            params.suggestedTerm = params.term
            params.term = null
            params.usersForTermAlgo = params.users
            params.users = null
            al = new UserAnnotationListing() //if algo, we look for user_annotation JOIN algo_annotation_term  too
            result.addAll(createRequest(al, params))
        } else {
            al = new UserAnnotationListing()
            result = createRequest(al, params)
        }
        [result: result, project: al.container().container()]
    }

    private downloadDocument(def annotations, Project project) {
        def ignoredField = ['class','image','project','user','container','userByTerm']

        if (params?.format && params.format != "html") {
            def exporterIdentifier = params.format;
            if (exporterIdentifier == "xls") {
                exporterIdentifier = "excel"
            }
            response.contentType = grailsApplication.config.grails.mime.types[params.format]
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
            String datePrefix = simpleFormat.format(new Date())
            response.setHeader("Content-disposition", "attachment; filename=${datePrefix}_annotations.${params.format}")

            def terms = termService.list(project)
            def termsIndex = [:]
            terms.each {
                termsIndex.put(it.id,it)
            }

            def exportResult = []

            def fields = ['indice']


            if(!annotations.isEmpty()) {
                annotations.first().each {
                    if(!ignoredField.contains(it.key)) {
                        fields << it.key
                    }
                }
            }
            annotations.eachWithIndex { annotation, i ->

                def data = annotation
                annotation.indice = i+1
                annotation.eachWithIndex {
                    if(it.key.equals("updated") || it.key.equals("created")) {
                        it.value = it.value? new Date((long)it.value) : null
                    }
                    if(it.key.equals("area") || it.key.equals("perimeter") || it.key.equals("x") || it.key.equals("y")) {
                        it.value = (int)Math.floor(it.value)
                    }

                    if(it.key.equals("term")) {
                        def termList = []
                        it.value.each { termId ->
                            termList << termsIndex[termId]
                        }
                        it.value = termList
                    }
                }

                exportResult.add(data)
            }

            def labels = [:]
            fields.each {
                labels[it]=it
            }

            exportService.export(exporterIdentifier, response.outputStream, exportResult,fields,labels,[:],[:])
        }
    }

    /**
     * Check if we ask reviewed annotation
     */
    private boolean isReviewedAnnotationAsked(def params) {
        return params.getBoolean('reviewed')
    }

    /**
     * Check if we ask reviewed annotation
     */
    private boolean isRoiAnnotationAsked(def params) {
        return params.getBoolean('roi')
    }

    /**
     * Check if we ask algo annotation
     */
    private boolean isAlgoAnnotationAsked(def params) {
        def idUser = params.getLong('user')
        if(idUser) {
           def user = SecUser.read(idUser)
           if(!user) {
               throw new ObjectNotFoundException("User $user not exist!")
           }
           return user.algo()
        } else {
           def idUsers = params.get('users')
            if(idUsers) {
                def ids= idUsers.replace("_",",").split(",").collect{Long.parseLong(it)}
                def user = SecUser.read(ids.first())
                if(!user) {
                    throw new ObjectNotFoundException("User $user not exist!")
                }
                return !ids.isEmpty() && user.algo()
            }
        }
        //if no other filter, just take user annotation
        return false
    }

    /**
     * Fill AnnotationListing al thanks to params data
     */
    private def createRequest(AnnotationListing al, def params) {

        al.columnToPrint = paramsService.getPropertyGroupToShow(params)
        al.project = params.getLong('project')
        al.user = params.getLong('user')
        if(params.getLong("job")) {
            al.user = UserJob.findByJob(Job.read(params.getLong("job")))?.id
        }
        if(params.getLong("jobForTermAlgo")) {
            al.userForTermAlgo = UserJob.findByJob(Job.read(params.getLong("jobForTermAlgo")))?.id
        }

        al.term = params.getLong('term')
        al.image = params.getLong('image')
        al.suggestedTerm = params.getLong('suggestedTerm')
        al.userForTermAlgo = params.getLong('userForTermAlgo')

        al.kmeansValue = params.getLong('kmeansValue')
        al.excludedAnnotation = params.getLong('excludedAnnotation')

        def users = params.get('users')
        if(users) {
            al.users = params.get('users').replace("_",",").split(",").collect{Long.parseLong(it)}
        }

        def images = params.get('images')
        if(images) {
            al.images = params.get('images').replace("_",",").split(",").collect{Long.parseLong(it)}
        }

        def terms = params.get('terms')
        if(terms) {
            al.terms = params.get('terms').replace("_",",").split(",").collect{Long.parseLong(it)}
        }

        def usersForTerm = params.get('usersForTerm')
        if(usersForTerm) {
            al.usersForTerm = params.get('usersForTerm').split(",").collect{Long.parseLong(it)}
        }

        def suggestedTerms = params.get('suggestedTerms')
        if(suggestedTerms) {
            al.suggestedTerms = params.get('suggestedTerms').split(",").collect{Long.parseLong(it)}
        }

        def usersForTermAlgo = params.get('usersForTermAlgo')
        if(usersForTermAlgo) {
            al.usersForTermAlgo = params.get('usersForTermAlgo').split(",").collect{Long.parseLong(it)}
        }

        al.notReviewedOnly = params.getBoolean('notReviewedOnly')
        al.noTerm = params.getBoolean('noTerm')
        al.noAlgoTerm = params.getBoolean('noAlgoTerm')
        al.multipleTerm = params.getBoolean('multipleTerm')
        al.kmeans = params.getBoolean('kmeans')

        if(params.get('bbox')) {
            al.bbox = GeometryUtils.createBoundingBox(params.get('bbox')).toText()
        }

        if(params.get('bboxAnnotation')) {
            al.bboxAnnotation = AnnotationDomain.getAnnotationDomain(params.getLong('bboxAnnotation')).wktLocation
        }

        annotationListingService.listGeneric(al)
    }

    /**
     * Download report for an annotation listing
     */
    @RestApiMethod(description="Download a report (pdf, xls,...) with software annotation data from a specific project.")
    @RestApiResponseObject(objectIdentifier = "file")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id"),
        @RestApiParam(name="reviewed", type="boolean", paramType = RestApiParamType.QUERY,description = "Get only reviewed annotation"),
        @RestApiParam(name="terms", type="list", paramType = RestApiParamType.QUERY,description = "The annotation terms id (if empty: all terms)"),
        @RestApiParam(name="users", type="list", paramType = RestApiParamType.QUERY,description = "The annotation users id (if empty: all users). If reviewed flag is false then if first user is software, get algo annotation otherwise if first user is human, get user annotation. "),
        @RestApiParam(name="images", type="list", paramType = RestApiParamType.QUERY,description = "The annotation images id (if empty: all images)"),
        @RestApiParam(name="format", type="string", paramType = RestApiParamType.QUERY,description = "The report format (pdf, xls,...)")
    ])
    def downloadDocumentByProject() {

        def users = []
        if (params.users != null && params.users != "") {
            params.users.split(",").each { id ->
                users << Long.parseLong(id)
            }
        }

        if(params.getBoolean('reviewed')) {
            forward(controller: "restReviewedAnnotation", action: "downloadDocumentByProject")
        } else {
            if (!users.isEmpty() && SecUser.read(users.first()).algo()) {
                forward(controller: "restAlgoAnnotation", action: "downloadDocumentByProject")
            } else {
                forward(controller: "restUserAnnotation", action: "downloadDocumentByProject")
            }
        }
    }

    @RestApiMethod(description="Get all annotation that intersect a geometry or another annotation. See /annotation/search for extra parameter (show/hide). ", listing=true)
    @RestApiResponseObject(objectIdentifier = "file")
    @RestApiParams(params=[
        @RestApiParam(name="idImage", type="long", paramType = RestApiParamType.QUERY,description = "The image id"),
        @RestApiParam(name="geometry", type="string", paramType = RestApiParamType.QUERY,description = "(Optional) WKT form of the geometry (if not set, set annotation param)"),
        @RestApiParam(name="annotation", type="long", paramType = RestApiParamType.QUERY,description = "(Optional) The annotation id for the geometry (if not set, set geometry param)"),
        @RestApiParam(name="user", type="long", paramType = RestApiParamType.QUERY,description = "The annotation user id (may be an algo) "),
        @RestApiParam(name="terms", type="list", paramType = RestApiParamType.QUERY,description = "The annotation terms id")
    ])
    def listIncludedAnnotation() {
        responseSuccess(getIncludedAnnotation(params))
    }

    @RestApiMethod(description="Get all annotation that intersect a geometry or another annotation. Unlike the simple list, extra parameter (show/hide) are not available. ")
    @RestApiResponseObject(objectIdentifier = "file")
    @RestApiParams(params=[
        @RestApiParam(name="idImage", type="long", paramType = RestApiParamType.QUERY,description = "The image id"),
        @RestApiParam(name="geometry", type="string", paramType = RestApiParamType.QUERY,description = "(Optional) WKT form of the geometry (if not set, set annotation param)"),
        @RestApiParam(name="annotation", type="long", paramType = RestApiParamType.QUERY,description = "(Optional) The annotation id for the geometry (if not set, set geometry param)"),
        @RestApiParam(name="user", type="long", paramType = RestApiParamType.QUERY,description = "The annotation user id (may be an algo) "),
        @RestApiParam(name="terms", type="list", paramType = RestApiParamType.QUERY,description = "The annotation terms id")
    ])
    def downloadIncludedAnnotation() {
        ImageInstance image = imageInstanceService.read(params.long('idImage'))
        def lists = getIncludedAnnotation(params,['basic','meta','gis','image','term'])
        downloadPdf(lists, image.project)
    }



    private downloadPdf(def annotations, Project project) {
        if (params?.format && params.format != "html") {
            def exporterIdentifier = params.format;
            if (exporterIdentifier == "xls") exporterIdentifier = "excel"
            response.contentType = grailsApplication.config.grails.mime.types[params.format]
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
            String datePrefix = simpleFormat.format(new Date())
            response.setHeader("Content-disposition", "attachment; filename=${datePrefix}_annotations_included.${params.format}")

            def users = secUserService.listAll(project)
            def terms = termService.list(project)

            def usersIndex = [:]
            users.each {
                usersIndex.put(it.id,it)
            }
            def termsIndex = [:]
            terms.each {
                termsIndex.put(it.id,it)
            }


            def exportResult = []
            annotations.each { annotation ->
                def data = [:]
                data.id = annotation.id
                data.area = (int) Math.floor(annotation.area)
                data.perimeter = (int) Math.floor(annotation.perimeter)
                data.XCentroid = (int) Math.floor(annotation.x)
                data.YCentroid = (int) Math.floor(annotation.y)

                data.image = annotation.image
                data.filename = annotation.originalfilename
                data.user = usersIndex.get(annotation.user)
                data.term = annotation.term.collect {termsIndex.get(it).name}.join(", ")
                data.cropURL = annotation.cropURL
                data.cropGOTO = UrlApi.getAnnotationURL(project.id, data.image, data.id)
                exportResult.add(data)
            }

            List fields = ["id", "area", "perimeter", "XCentroid", "YCentroid", "image", "filename", "user", "term", "cropURL", "cropGOTO"]
            Map labels = ["id": "Id", "area": "Area (microns²)", "perimeter": "Perimeter (mm)", "XCentroid": "X", "YCentroid": "Y", "image": "Image Id", "filename": "Image Filename", "user": "User", "term": "Term", "cropURL": "View userannotation picture", "cropGOTO": "View userannotation on image"]
            String title = "Annotations included"

            exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.04, 0.06, 0.06, 0.04, 0.04, 0.04, 0.08, 0.06, 0.06, 0.25, 0.25], "title": title, "csv.encoding": "UTF-8", "separator": ";"])
        }
    }


    private def getIncludedAnnotation(params, def propertiesToShow = null){

        def image = imageInstanceService.read(params.long('idImage'))

        //get area
        def geometry = params.geometry
        AnnotationDomain annotation = null
        if(!geometry) {
            annotation = AnnotationDomain.getAnnotationDomain(params.long('annotation'))
            geometry = annotation.location.toText()
        }

        //get user
        def idUser = params.long('user')
        def user
        if (idUser!=0) {
            user = secUserService.read(params.long('user'))
        }

        //get term
        def terms = paramsService.getParamsTermList(params.terms,image.project)

        def response
        if(!user) {
            //goto reviewed
            response = reviewedAnnotationService.listIncluded(image,geometry,terms,annotation,propertiesToShow)
        } else if (user.algo()) {
            //goto algo
            response = algoAnnotationService.listIncluded(image,geometry,user,terms,annotation,propertiesToShow)
        }  else {
            //goto user annotation
            response = userAnnotationService.listIncluded(image,geometry,user,terms,annotation,propertiesToShow)
        }
        response

    }

    /**
     * Read a specific annotation
     * It's better to avoid the user of this method if we know the correct type of an annotation id
     * Annotation x => annotation/x.json is slower than userannotation/x.json or algoannotation/x.json
     */
    @RestApiMethod(description="Get an annotation, this works for all kind of annotation (user/algo/reviewed). It's better to avoid the user of this method if we know the correct type of an annotation id. Annotation x => annotation/x.json is slower than userannotation/x.json or algoannotation/x.json")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The annotation id")
    ])
    def show() {
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(params.long('id'))
        if(!annotation) {
            responseNotFound("Annotation",params.id)
        } else if(annotation instanceof UserAnnotation) {
            forward(controller: "restUserAnnotation", action: "show")
        } else if(annotation instanceof AlgoAnnotation) {
            forward(controller: "restAlgoAnnotation", action: "show")
        } else  if(annotation instanceof ReviewedAnnotation) {
            forward(controller: "restReviewedAnnotation", action: "show")
        }else  if(annotation instanceof RoiAnnotation) {
            forward(controller: "restRoiAnnotation", action: "show")
        }
    }

    /**
     * Add an annotation
     * Redirect to the controller depending on the user type
     */
    @RestApiMethod(description="Add an annotation (only available for user/algo). If current user is algo, an algo annotation will be created. Otherwise, an user annotation")
    def add() {
        SecUser user = cytomineService.currentUser
        if(params.getBoolean('roi')) {
            forward(controller: "restRoiAnnotation", action: "add")
        } else if (user.algo()) {
            forward(controller: "restAlgoAnnotation", action: "add")
        } else {
            forward(controller: "restUserAnnotation", action: "add")
        }
    }

    /**
     * Update an annotation
     * Redirect to the good controller with the annotation type
     */
    @RestApiMethod(description="Update an annotation. This works for all kind of annotation (user/algo/reviewed)")
        @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
        @RestApiParam(name="fill", type="boolean", paramType = RestApiParamType.QUERY,description = "(Optional, default: false) If true, fill holes in annotation")
    ])
    def update() {
        if (params.getBoolean('fill'))
        //if fill param is set, annotation will be filled (removed empty area inside geometry)
            forward(action: "fillAnnotation")
        else {
            try {
                SecUser user = cytomineService.currentUser

                def annotation = AnnotationDomain.getAnnotationDomain(params.getLong("id"))
                if(!annotation) {
                    responseNotFound("Annotation",params.id)
                } else if(annotation instanceof UserAnnotation) {
                    forward(controller: "restUserAnnotation", action: "update")
                } else if(annotation instanceof AlgoAnnotation) {
                    forward(controller: "restAlgoAnnotation", action: "update")
                } else  if(annotation instanceof ReviewedAnnotation) {
                    if (annotation.reviewUser != user) {
                        throw new ForbiddenException("You cannot update this annotation! Only ${annotation.user.username} can do that!")
                    }
                    forward(controller: "restReviewedAnnotation", action: "update")
                }else  if(annotation instanceof RoiAnnotation) {
                    forward(controller: "restRoiAnnotation", action: "update")
                }
            } catch (CytomineException e) {
                log.error(e)
                response([success: false, errors: e.msg], e.code)
            }
        }
    }

    /**
     * Delete an annotation
     * Redirect to the good controller with the current user type
     */
    @RestApiMethod(description="Delete an annotation (only user/algo)")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
    ])
    def delete() {
        try {
            def annotation = AnnotationDomain.getAnnotationDomain(params.getLong("id"))
            if(!annotation) {
                responseNotFound("Annotation",params.id)
            } else if(annotation instanceof UserAnnotation) {
                forward(controller: "restUserAnnotation", action: "delete")
            } else if(annotation instanceof AlgoAnnotation) {
                forward(controller: "restAlgoAnnotation", action: "delete")
            }else  if(annotation instanceof RoiAnnotation) {
                forward(controller: "restRoiAnnotation", action: "delete")
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @RestApiMethod(description="Simplify an existing annotation form (reducing the number of point). The number of points of the resulting form is not garantee to be between minPoint and maxPoint (best effort)")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
        @RestApiParam(name="minPoint", type="int", paramType = RestApiParamType.QUERY,description = "Minimum number of point"),
        @RestApiParam(name="maxPoint", type="int", paramType = RestApiParamType.QUERY,description = "Maximum number of point")
    ])
    def simplify() {
        try {
            //extract params
            def minPoint = params.getLong('minPoint')
            def maxPoint = params.getLong('maxPoint')
            def idAnnotation = params.getLong('id')

            //retrieve annotation
            AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(idAnnotation)

            //apply simplify
            def result = simplifyGeometryService.simplifyPolygon(annotation.location.toText(),minPoint,maxPoint)
            annotation.location = result.geometry
            annotation.geometryCompression = result.rate
            userAnnotationService.saveDomain(annotation)  //saveDomain is same method in algo/reviewedannotationservice
            //update geom
            responseSuccess(annotation)


        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @RestApiMethod(description="Simplify and return a form. The number of points of the resulting form is not garantee to be between minPoint and maxPoint (best effort)")
    @RestApiParams(params=[
        @RestApiParam(name="minPoint", type="int", paramType = RestApiParamType.QUERY,description = "Minimum number of point"),
        @RestApiParam(name="maxPoint", type="int", paramType = RestApiParamType.QUERY,description = "Maximum number of point"),
        @RestApiParam(name="JSON POST DATA: wkt", type="string", paramType = RestApiParamType.QUERY,description = "WKT form to return simplify. This may be big so must be in post data (not query param)")
    ])
    def retrieveSimplify() {
        def minPoint = params.getLong('minPoint')
        def maxPoint = params.getLong('maxPoint')
        def json = request.JSON
        def wkt = json.wkt
        def result = simplifyGeometryService.simplifyPolygon(wkt,minPoint,maxPoint)
        responseSuccess([wkt:result.geometry.toText()])
    }


    /**
     * Fill an annotation.
     * Remove empty space in the polygon
     */
    @RestApiMethod(description="Fill an annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
    ])
    def fillAnnotation() {
        log.info "fillAnnotation"
        try {
            AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(params.long('id'))
            if (!annotation) {
                throw new ObjectNotFoundException("Annotation ${params.long('id')} not found!")
            }

            //Is the first polygon always the big 'boundary' polygon?
            String newGeom = fillPolygon(annotation.location.toText())
            def json = JSON.parse(annotation.encodeAsJSON())
            json.location = newGeom

            if (annotation.algoAnnotation) {
                responseSuccess(algoAnnotationService.update(annotation,json))
            }
            else if (annotation.reviewedAnnotation) {
                responseSuccess(reviewedAnnotationService.update(annotation,json))
            }
            else {
                responseSuccess(userAnnotationService.update(annotation,json))
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Fill polygon to complete empty space inside polygon/mulypolygon
     * @param polygon A polygon or multipolygon wkt polygon
     * @return A polygon or multipolygon filled points
     */
    private String fillPolygon(String polygon) {
        if (polygon.startsWith("POLYGON")) return "POLYGON(" + getFirstPolygonLocation(polygon) + ")";
        else if (polygon.startsWith("MULTIPOLYGON")) return "MULTIPOLYGON(" + getFirstPolygonLocationForEachItem(polygon) + ")";
        else throw new WrongArgumentException("Form cannot be filled:" + polygon)
    }

    /**
     * Fill all polygon inside a Multipolygon WKT polygon
     * @param form Multipolygon WKT polygon
     * @return Multipolygon with all its polygon filled
     */
    private String getFirstPolygonLocationForEachItem(String form) {
        //e.g: "MULTIPOLYGON (((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)) , ((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)) , ((6 3,9 2,9 4,6 3)))";
        String workingForm = form.replaceAll("\\) ", ")");
        //"MULTIPOLYGON(((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)),((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)),((6 3,9 2,9 4,6 3)))";
        workingForm = workingForm.replaceAll(" \\(", "(")
        workingForm = workingForm.replace("MULTIPOLYGON(", "");
        //"((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)),((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)),((6 3,9 2,9 4,6 3)))";
        workingForm = workingForm.substring(0, workingForm.length() - 1);
        //"((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)),((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2)),((6 3,9 2,9 4,6 3))";
        String[] polygons = workingForm.split("\\)\\)\\,\\(\\(");
        //"[ ((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2] [1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2] [6 3,9 2,9 4,6 3)) ]";
        List<String> fixedPolygon = new ArrayList<String>();
        for (int i = 0; i < polygons.length; i++) {
            if (i == 0) {
                fixedPolygon.add(polygons[i] + "))");
            } else if (i == polygons.length - 1) {
                fixedPolygon.add("((" + polygons[i] + "");
            } else {
                fixedPolygon.add("((" + polygons[i] + "))");
            }
            //"[ ((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2))] [((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2))] [((6 3,9 2,9 4,6 3)) ]";
        }

        List<String> filledPolygon = new ArrayList<String>();
        for (int i = 0; i < fixedPolygon.size(); i++) {
            filledPolygon.add("(" + getFirstPolygonLocation(fixedPolygon.get(i)) + ")");
            //"[ ((1 1,5 1,5 5,1 5,1 1))] [((1 1,5 1,5 5,1 5,1 1))] [((6 3,9 2,9 4,6 3)) ]";
        }

        String multiPolygon = filledPolygon.join(",")
        //"((1 1,5 1,5 5,1 5,1 1)),((1 1,5 1,5 5,1 5,1 1)),((6 3,9 2,9 4,6 3))";
        return multiPolygon;
    }

    /**
     * Fill a polygon
     * @param polygon Polygon as wkt
     * @return Polygon filled points
     */
    private String getFirstPolygonLocation(String polygon) {
        int i = 0;
        int start, stop;
        while (polygon.charAt(i) != '(') i++;
        while (polygon.charAt(i + 1) == '(') i++;
        start = i;
        while (polygon.charAt(i) != ')') i++;
        stop = i;
        return polygon.substring(start, stop + 1);
    }

    /**
     * Add/Remove a geometry Y to/from the annotation geometry X.
     * Y must have intersection with X
     */
    @RestApiMethod(description="Add/Remove a geometry Y to/from all annotations that intersects Y")
    @RestApiParams(params=[
        @RestApiParam(name="minPoint", type="int", paramType = RestApiParamType.QUERY,description = "Minimum number of point"),
        @RestApiParam(name="maxPoint", type="int", paramType = RestApiParamType.QUERY,description = "Maximum number of point"),
        @RestApiParam(name="JSON POST DATA: location", type="string", paramType = RestApiParamType.QUERY,description = "WKT form of Y"),
        @RestApiParam(name="JSON POST DATA: review", type="boolean", paramType = RestApiParamType.QUERY,description = "Only get reviewed annotation"),
        @RestApiParam(name="JSON POST DATA: image", type="long", paramType = RestApiParamType.QUERY,description = "The image id"),
        @RestApiParam(name="JSON POST DATA: remove", type="boolean", paramType = RestApiParamType.QUERY,description = "Add or remove Y"),
        @RestApiParam(name="JSON POST DATA: layers", type="list", paramType = RestApiParamType.QUERY,description = "List of layers id")
    ])
    def addCorrection() {
        def json = request.JSON
        String location = json.location
        boolean review = json.review
        long idImage = json.image
        boolean remove = json.remove
        def layers = json.layers
        try {
            List<Long> idsReviewedAnnotation = []
            List<Long> idsUserAnnotation = []

            //if review mode, priority is done to reviewed annotation correction
            if (review) {
                idsReviewedAnnotation = findAnnotationIdThatTouch(location, layers,idImage, "reviewed_annotation")
            }

            //there is no reviewed intersect annotation or user is not in review mode
            if (idsReviewedAnnotation.isEmpty()) {
                idsUserAnnotation = findAnnotationIdThatTouch(location, layers, idImage, "user_annotation")
            }

            log.info "idsReviewedAnnotation=$idsReviewedAnnotation"
            log.info "idsUserAnnotation=$idsUserAnnotation"

            //there is no user/reviewed intersect
            if (idsUserAnnotation.isEmpty() && idsReviewedAnnotation.isEmpty()) {
                throw new WrongArgumentException("There is no intersect annotation!")
            }

            if (!idsUserAnnotation.isEmpty()) {
                responseResult(doCorrectUserAnnotation(idsUserAnnotation,location, remove))
            } else {
                responseResult(doCorrectReviewedAnnotation(idsReviewedAnnotation,location, remove))
            }

        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Find all annotation id from a specific table created by a user that touch location geometry
     * @param location WKT Location that must touch result annotation
     * @param idImage Annotation image
     * @param idUser Annotation User
     * @param table Table that store annotation (user, algo, reviewed)
     * @return List of annotation id from idImage and idUser that touch location
     */
    def findAnnotationIdThatTouch(String location, def layers, long idImage, String table) {
        ImageInstance image = ImageInstance.read(idImage)
        boolean projectAdmin = image.project.checkPermission(ADMINISTRATION,currentRoleServiceProxy.isAdminByNow())
        if(!projectAdmin) {
            layers = layers.findAll{(it+"")==(cytomineService.currentUser.id+"")}
        }

        String userColumnName = "user_id"
        if(table.equals("reviewed_annotation")) {
            userColumnName = "review_user_id"
        }

        String request = "SELECT annotation.id,user_id\n" +
                "FROM $table annotation\n" +
                "WHERE annotation.image_id = $idImage\n" +
                (userColumnName.equals("user_id")? "AND $userColumnName IN (${layers.join(',')})\n" : "") +
                "AND ST_Intersects(annotation.location,ST_GeometryFromText('" + location + "',0));"

        def sql = new Sql(dataSource)
        List<Long> ids = []
        List<Long> users = []
        sql.eachRow(request) {
            ids << it[0]
            users << it[1]
        }
        sql.close()
        users.unique()
        if(users.size()>1 && userColumnName.equals("user_id")) { //if more annotation from more than 1 user NOT IN REVIEW MODE!
            throw new WrongArgumentException("Annotations from multiple users are under this area. You can correct only annotation from 1 user (hide layer if necessary)")
        }

        def annotations = []
        if(table.equals("user_annotation")) {
            ids.each {
                annotations << UserAnnotation.read(it)
            }
        } else if(table.equals("reviewed_annotation")) {
            ids.each {
                annotations << ReviewedAnnotation.read(it)
            }
        }

        def termSizes = [:]
        annotations.each { annotation ->
            def terms = annotation.termsId()
            terms.each { term->
                def value = termSizes.get(term)?:0
                 termSizes.put(term,value+annotation.area)

            }
        }

        Double min = Double.MAX_VALUE
        Long goodTerm = null

        if(!termSizes.isEmpty()) {
            termSizes.each {
               if(min>it.value) {
                   min=it.value
                   goodTerm = it.key
               }
            }

            ids = []
            annotations.each { annotation ->
                def terms = annotation.termsId()
                if(terms.contains(goodTerm)) {
                    ids << annotation.id
                }
            }
        }

        return ids.unique()
    }

    /**
     * Find all reviewed annotation domain instance with ids and exactly the same term
     * All these annotation must have this single term
     * @param ids List of reviewed annotation id
     * @param term Term that must have all reviewed annotation (
     * @return Reviewed Annotation list
     */
    def findReviewedAnnotationWithTerm(def ids, def termsId) {
        List<ReviewedAnnotation> annotationsWithSameTerm = []
        ids.each { id ->
            ReviewedAnnotation compared = ReviewedAnnotation.read(id)
            List<Long> idTerms = compared.termsId()
            if (idTerms.size() != termsId.size()) {
                throw new WrongArgumentException("Annotations have not the same term!")
            }

            idTerms.each { idTerm ->
                if (!termsId.contains(idTerm)) {
                    throw new WrongArgumentException("Annotations have not the same term!")
                }
            }
            annotationsWithSameTerm << compared
        }
        annotationsWithSameTerm
    }

    /**
     * Find all user annotation domain instance with ids and exactly the same term
     * All these annotation must have this single term
     * @param ids List of user annotation id
     * @param term Term that must have all user annotation (
     * @return user Annotation list
     */
    def findUserAnnotationWithTerm(def ids, def termsId) {
        List<UserAnnotation> annotationsWithSameTerm = []

        ids.each { id ->
             UserAnnotation compared = UserAnnotation.read(id)
             List<Long> idTerms = compared.termsId()
             if (idTerms.size() != termsId.size()) {
                 throw new WrongArgumentException("Annotations have not the same term!")
             }

             idTerms.each { idTerm ->
                 if (!termsId.contains(idTerm)) {
                     throw new WrongArgumentException("Annotations have not the same term!")
                 }
             }
             annotationsWithSameTerm << compared
         }
         annotationsWithSameTerm

    }

    /**
     * Apply a union or a diff on all covering annotations list with the newLocation geometry
     * @param coveringAnnotations List of reviewed annotations id that are covering by newLocation geometry
     * @param newLocation A geometry (wkt format)
     * @param remove Flag that tell to extend or substract part of geometry from  coveringAnnotations list
     * @return The first annotation data
     */
    def doCorrectReviewedAnnotation(def coveringAnnotations, String newLocation, boolean remove) {
        if (coveringAnnotations.isEmpty()) return

        //Get the based annotation
        ReviewedAnnotation based = ReviewedAnnotation.read(coveringAnnotations.first())

        //Get the term of the based annotation, it will be the main term
        def basedTerms = based.termsId()

        //Get all other annotation with same term
        List<Long> allOtherAnnotationId = coveringAnnotations.subList(1, coveringAnnotations.size())
        List<ReviewedAnnotation> allAnnotationWithSameTerm = findReviewedAnnotationWithTerm(allOtherAnnotationId, basedTerms)

        //Create the new geometry
        Geometry newGeometry = new WKTReader().read(newLocation)
        if(!newGeometry.isValid()) {
            throw new WrongArgumentException("Your annotation cannot be self-intersected.")
        }

        def result
        if (!remove) {
            //union will be made:
            // -add the new geometry to the based annotation location.
            // -add all other annotation geometry to the based annotation location (and delete other annotation)
            based.location = based.location.union(newGeometry)
            allAnnotationWithSameTerm.eachWithIndex { other, i ->
                based.location = based.location.union(other.location)
                reviewedAnnotationService.delete(other)
            }
            result = reviewedAnnotationService.update(based,JSON.parse(based.encodeAsJSON()))
        } else {
            //diff will be made
            //-remove the new geometry from the based annotation location
            //-remove the new geometry from all other annotation location
            based.location = based.location.difference(newGeometry)
            if (based.location.getNumPoints() < 2) throw new WrongArgumentException("You cannot delete an annotation with substract! Use reject or delete tool.")
            result = reviewedAnnotationService.update(based,JSON.parse(based.encodeAsJSON()))
            allAnnotationWithSameTerm.eachWithIndex { other, i ->
                other.location = other.location.difference(newGeometry)
                reviewedAnnotationService.update(other,JSON.parse(other.encodeAsJSON()))
            }
        }
        return result
    }


    def doCorrectUserAnnotation(def coveringAnnotations, String newLocation, boolean remove) {
        if (coveringAnnotations.isEmpty()) return

        //Get the based annotation
        UserAnnotation based = UserAnnotation.read(coveringAnnotations.first())

        //Get the term of the based annotation, it will be the main term
        def basedTerms = based.termsId()
        //if(basedTerms.isEmpty() || basedTerms.size()>1) throw new WrongArgumentException("Annotations have not the same term!")
        //Long basedTerm = basedTerms.first()

        //Get all other annotation with same term
        List<Long> allOtherAnnotationId = coveringAnnotations.subList(1, coveringAnnotations.size())
        List<UserAnnotation> allAnnotationWithSameTerm = findUserAnnotationWithTerm(allOtherAnnotationId, basedTerms)

        //Create the new geometry
        Geometry newGeometry = new WKTReader().read(newLocation)
        if(!newGeometry.isValid()) {
            throw new WrongArgumentException("Your annotation cannot be self-intersected.")
        }

        def result
        if (!remove) {
            //union will be made:
            // -add the new geometry to the based annotation location.
            // -add all other annotation geometry to the based annotation location (and delete other annotation)
            based.location = based.location.union(newGeometry)
            allAnnotationWithSameTerm.eachWithIndex { other, i ->
                based.location = based.location.union(other.location)
                userAnnotationService.delete(other)
            }
            result = userAnnotationService.update(based,JSON.parse(based.encodeAsJSON()))
        } else {
            //diff will be made
            //-remove the new geometry from the based annotation location
            //-remove the new geometry from all other annotation location
            based.location = based.location.difference(newGeometry)
            if (based.location.getNumPoints() < 2) throw new WrongArgumentException("You cannot delete an annotation with substract! Use reject or delete tool.")
            result = userAnnotationService.update(based,JSON.parse(based.encodeAsJSON()))
            allAnnotationWithSameTerm.eachWithIndex { other, i ->
                other.location = other.location.difference(newGeometry)
                userAnnotationService.update(other,JSON.parse(other.encodeAsJSON()))
            }
        }
        return result
    }
}
