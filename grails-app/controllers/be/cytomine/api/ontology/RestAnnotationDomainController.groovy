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
import be.cytomine.sql.AlgoAnnotationListing
import be.cytomine.sql.AnnotationListing
import be.cytomine.sql.ReviewedAnnotationListing
import be.cytomine.sql.RoiAnnotationListing
import be.cytomine.sql.UserAnnotationListing
import be.cytomine.utils.GeometryUtils
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import groovy.sql.Sql
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiBodyObject
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

import java.text.SimpleDateFormat

import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION

/**
 * Controller that handle request on annotation.
 * It's main utility is to redirect request to the correct controller: user/algo/reviewed
 */
@Api(name = "generic annotation services", description = "Methods for managing an annotation created by a software")
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

    def currentDomainName() {
        return "generic annotation" //needed because not RestAbstractImageController...
    }
    /**
     * Search service for all annotation type
     * see AnnotationListing for all filters available
     */
    @ApiMethodLight(description="Search service for all annotation type. By default All fields are not visible (optim), you need to select fields using show/hideXXX query parameters.", listing = true)
    @ApiResponseObject(objectIdentifier = "[annotation listing]")
    @ApiParams(params=[
        @ApiParam(name="showDefault", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, show 'basic', 'meta', and 'term' properties group. See showBasic/Meta/... for more information (default: true ONLY IF NO OTHER show/hideXXX are set)"),
        @ApiParam(name="showBasic", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, show basic properties group (id, class...)"),
        @ApiParam(name="showMeta", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, show meta properties group (urls, image id, project id,...)"),
        @ApiParam(name="showWKT", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, show the location WKT properties. This may slow down the request!."),
        @ApiParam(name="showGIS", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, show the form GIS field (area, centroid,...). This may slow down the request!."),
        @ApiParam(name="showTerm", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, show the term properties (id, user who add the term,...). This may slow down the request."),
        @ApiParam(name="showAlgo", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, show the algo details (job,...). This may slow down the request."),
        @ApiParam(name="showUser", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, show the annotation user details (username,...). This may slow down the request."),
        @ApiParam(name="showImage", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, show the annotation image details (filename,...). This may slow down the request."),
        @ApiParam(name="hideBasic", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, hide basic properties group (id, class...)"),
        @ApiParam(name="hideMeta", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, hide meta properties group (urls, image id, project id,...)"),
        @ApiParam(name="hideWKT", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, hide the location WKT properties. This may slow down the request!."),
        @ApiParam(name="hideGIS", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, hide the form GIS field (area, centroid,...). This may slow down the request!."),
        @ApiParam(name="hideTerm", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, hide the term properties (id, user who add the term,...). This may slow down the request."),
        @ApiParam(name="hideAlgo", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, hide the algo details (job,...). This may slow down the request."),
        @ApiParam(name="hideUser", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, hide the annotation user details (username,...). This may slow down the request."),
        @ApiParam(name="hideImage", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) If true, hide the annotation image details (filename,...). This may slow down the request."),
        @ApiParam(name="project", type="long", paramType = ApiParamType.QUERY, description = "(Optional) Get only annotation for this project id"),
        @ApiParam(name="job", type="long", paramType = ApiParamType.QUERY, description = "(Optional) Get only annotation for this job id"),
        @ApiParam(name="user", type="long", paramType = ApiParamType.QUERY, description = "(Optional) Get only annotation for this user id"),
        @ApiParam(name="jobForTermAlgo", type="long", paramType = ApiParamType.QUERY, description = "(Optional) Get only annotation link with a term added by this job id"),
        @ApiParam(name="term", type="long", paramType = ApiParamType.QUERY, description = "(Optional) Get only annotation link with this term id"),
        @ApiParam(name="image", type="long", paramType = ApiParamType.QUERY, description = "(Optional) Get only annotation for this image id"),
        @ApiParam(name="suggestedTerm", type="long", paramType = ApiParamType.QUERY, description = "(Optional) Get only annotation suggested by for this term by a job"),
        @ApiParam(name="userForTermAlgo", type="long", paramType = ApiParamType.QUERY, description = "(Optional) Get only user annotation link with a term added by this job id"),
        @ApiParam(name="kmeansValue", type="long", paramType = ApiParamType.QUERY, description = "(Optional) Only used for GUI "),
        @ApiParam(name="users", type="list", paramType = ApiParamType.QUERY, description = "(Optional) Get only annotation for these users id"),
        @ApiParam(name="images", type="list", paramType = ApiParamType.QUERY, description = "(Optional) Get only annotation for these images id"),
        @ApiParam(name="terms", type="list", paramType = ApiParamType.QUERY, description = "(Optional) Get only annotation for these terms id"),
        @ApiParam(name="notReviewedOnly", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) Only get annotation not reviewed"),
        @ApiParam(name="noTerm", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) Only get annotation with no term"),
        @ApiParam(name="noAlgoTerm", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) Only get annotation with no term from a job"),
        @ApiParam(name="multipleTerm", type="long", paramType = ApiParamType.QUERY, description = "(Optional) Only get annotation with multiple terms"),
        @ApiParam(name="kmeans", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional) Enable or not kmeans (only for GUI)"),
        @ApiParam(name="bbox", type="string", paramType = ApiParamType.QUERY, description = "(Optional) Get only annotations having intersection with the bbox (WKT)")
    ])
    def search() {
         try {
             responseSuccess(doSearch(params).result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @ApiMethodLight(description="Download report for annotation. !!! See doc for /annotation/search to filter annotations!!!", listing = true)
    @ApiResponseObject(objectIdentifier =  "file")
    @ApiParams(params=[
        @ApiParam(name="format", type="string", paramType = ApiParamType.QUERY, description = "(Optional) Output file format (pdf, xls,...)")
    ])
    def downloadSearched() {
        println "downloadSearched"
        def lists = doSearch(params)
        downloadDocument(lists.result,lists.project)
    }

    /**
     * Get annotation crop (image area that frame annotation)
     * This work for all kinds of annotations
     */

    @ApiMethodLight(description="Get annotation crop  (image area that frame annotation). This work for all kinds of annotations.")
    @ApiResponseObject(objectIdentifier =  "file")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id"),
        @ApiParam(name="max_size", type="int", paramType = ApiParamType.PATH,description = "Maximum size of the crop image (w and h)"),
        @ApiParam(name="zoom", type="int", paramType = ApiParamType.PATH,description = "Zoom level"),
        @ApiParam(name="draw", type="boolean", paramType = ApiParamType.PATH,description = "Draw annotation form border on the image")
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

    @ApiMethodLight(description="Get annotation crop with minimal size (256*256max)  (image area that frame annotation). This work for all kinds of annotations.")
    @ApiResponseObject(objectIdentifier =  "file")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id"),
        @ApiParam(name="zoom", type="int", paramType = ApiParamType.PATH,description = "Zoom level"),
        @ApiParam(name="draw", type="boolean", paramType = ApiParamType.PATH,description = "Draw annotation form border on the image")
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
        println "downloadDocument"
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
                    println it.key + "=" + it.value
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

                    //annotation[it.key]=it.value
                }

                exportResult.add(data)



//                data.id = annotation.id
//                data.area = (int) Math.floor(annotation.area)
//                data.perimeter = (int) Math.floor(annotation.perimeter)
//                data.XCentroid = (int) Math.floor(annotation.x)
//                data.YCentroid = (int) Math.floor(annotation.y)
//
//                data.image = annotation.image
//                data.filename = annotation.originalfilename
//                data.user = usersIndex.get(annotation.user)
//                data.term = annotation.term.collect {termsIndex.get(it).name}.join(", ")
//                data.cropURL = annotation.cropURL
//                data.cropGOTO = UrlApi.getAnnotationURL(project.id, data.image, data.id)
//                exportResult.add(data)
            }




            def labels = [:]
            fields.each {
                labels[it]=it
            }
            println "exporterIdentifier=$exporterIdentifier"
            println  "exportResult=${exportResult.size()}"
            println "fields=${fields}"
            println "labels=${labels}"
//
//            Map formatters = [author: upperCase]
//         	Map parameters = [title: "Cool books", "column.widths": [0.2, 0.3, 0.5]]

            exportService.export(exporterIdentifier, response.outputStream, exportResult,fields,labels,[:],[:])
//            exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.04, 0.06, 0.06, 0.04, 0.04, 0.04, 0.08, 0.06, 0.06, 0.25, 0.25], "title": title, "csv.encoding": "UTF-8", "separator": ";"])
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
        annotationListingService.listGeneric(al)
    }

    /**
     * Download report for an annotation listing
     */
    @ApiMethodLight(description="Download a report (pdf, xls,...) with software annotation data from a specific project.")
    @ApiResponseObject(objectIdentifier = "file")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The project id"),
        @ApiParam(name="reviewed", type="boolean", paramType = ApiParamType.QUERY,description = "Get only reviewed annotation"),
        @ApiParam(name="terms", type="list", paramType = ApiParamType.QUERY,description = "The annotation terms id (if empty: all terms)"),
        @ApiParam(name="users", type="list", paramType = ApiParamType.QUERY,description = "The annotation users id (if empty: all users). If reviewed flag is false then if first user is software, get algo annotation otherwise if first user is human, get user annotation. "),
        @ApiParam(name="images", type="list", paramType = ApiParamType.QUERY,description = "The annotation images id (if empty: all images)"),
        @ApiParam(name="format", type="string", paramType = ApiParamType.QUERY,description = "The report format (pdf, xls,...)")
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

    @ApiMethodLight(description="Get all annotation that intersect a geometry or another annotation. See /annotation/search for extra parameter (show/hide). ", listing=true)
    @ApiResponseObject(objectIdentifier = "file")
    @ApiParams(params=[
        @ApiParam(name="idImage", type="long", paramType = ApiParamType.QUERY,description = "The image id"),
        @ApiParam(name="geometry", type="string", paramType = ApiParamType.QUERY,description = "(Optional) WKT form of the geometry (if not set, set annotation param)"),
        @ApiParam(name="annotation", type="long", paramType = ApiParamType.QUERY,description = "(Optional) The annotation id for the geometry (if not set, set geometry param)"),
        @ApiParam(name="user", type="long", paramType = ApiParamType.QUERY,description = "The annotation user id (may be an algo) "),
        @ApiParam(name="terms", type="list", paramType = ApiParamType.QUERY,description = "The annotation terms id")
    ])
    def listIncludedAnnotation() {
        responseSuccess(getIncludedAnnotation(params))
    }

    @ApiMethodLight(description="Get all annotation that intersect a geometry or another annotation. Unlike the simple list, extra parameter (show/hide) are not available. ")
    @ApiResponseObject(objectIdentifier = "file")
    @ApiParams(params=[
        @ApiParam(name="idImage", type="long", paramType = ApiParamType.QUERY,description = "The image id"),
        @ApiParam(name="geometry", type="string", paramType = ApiParamType.QUERY,description = "(Optional) WKT form of the geometry (if not set, set annotation param)"),
        @ApiParam(name="annotation", type="long", paramType = ApiParamType.QUERY,description = "(Optional) The annotation id for the geometry (if not set, set geometry param)"),
        @ApiParam(name="user", type="long", paramType = ApiParamType.QUERY,description = "The annotation user id (may be an algo) "),
        @ApiParam(name="terms", type="list", paramType = ApiParamType.QUERY,description = "The annotation terms id")
    ])
    def downloadIncludedAnnotation() {
        println "downloadIncludedAnnotation"
        ImageInstance image = imageInstanceService.read(params.long('idImage'))
        def lists = getIncludedAnnotation(params,['basic','meta','gis','image','term'])
        downloadPdf(lists, image.project)
    }



    private downloadPdf(def annotations, Project project) {
        println "downloadPdf"
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
            println params.long('annotation')
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
        println "END" + AnnotationListing.availableColumnDefault
        response

    }

    /**
     * Read a specific annotation
     * It's better to avoid the user of this method if we know the correct type of an annotation id
     * Annotation x => annotation/x.json is slower than userannotation/x.json or algoannotation/x.json
     */
    @ApiMethodLight(description="Get an annotation, this works for all kind of annotation (user/algo/reviewed). It's better to avoid the user of this method if we know the correct type of an annotation id. Annotation x => annotation/x.json is slower than userannotation/x.json or algoannotation/x.json")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The annotation id")
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
    @ApiMethodLight(description="Add an annotation (only available for user/algo). If current user is algo, an algo annotation will be created. Otherwise, an user annotation")
    def add() {
        println params
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
    @ApiMethodLight(description="Update an annotation. This works for all kind of annotation (user/algo/reviewed)")
        @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id"),
        @ApiParam(name="fill", type="boolean", paramType = ApiParamType.QUERY,description = "(Optional, default: false) If true, fill holes in annotation")
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
    @ApiMethodLight(description="Delete an annotation (only user/algo)")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
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

    @ApiMethodLight(description="Simplify an existing annotation form (reducing the number of point). The number of points of the resulting form is not garantee to be between minPoint and maxPoint (best effort)")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id"),
        @ApiParam(name="minPoint", type="int", paramType = ApiParamType.QUERY,description = "Minimum number of point"),
        @ApiParam(name="maxPoint", type="int", paramType = ApiParamType.QUERY,description = "Maximum number of point")
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

    @ApiMethodLight(description="Simplify and return a form. The number of points of the resulting form is not garantee to be between minPoint and maxPoint (best effort)")
    @ApiParams(params=[
        @ApiParam(name="minPoint", type="int", paramType = ApiParamType.QUERY,description = "Minimum number of point"),
        @ApiParam(name="maxPoint", type="int", paramType = ApiParamType.QUERY,description = "Maximum number of point"),
        @ApiParam(name="JSON POST DATA: wkt", type="string", paramType = ApiParamType.QUERY,description = "WKT form to return simplify. This may be big so must be in post data (not query param)")
    ])
    def retrieveSimplify() {
        def minPoint = params.getLong('minPoint')
        def maxPoint = params.getLong('maxPoint')
        def json = request.JSON
        def wkt = json.wkt
        println wkt
        def result = simplifyGeometryService.simplifyPolygon(wkt,minPoint,maxPoint)
        println result.geometry
        responseSuccess([wkt:result.geometry.toText()])
    }


    /**
     * Fill an annotation.
     * Remove empty space in the polygon
     */
    @ApiMethodLight(description="Fill an annotation")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id"),
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
    @ApiMethodLight(description="Add/Remove a geometry Y to/from all annotations that intersects Y")
    @ApiParams(params=[
        @ApiParam(name="minPoint", type="int", paramType = ApiParamType.QUERY,description = "Minimum number of point"),
        @ApiParam(name="maxPoint", type="int", paramType = ApiParamType.QUERY,description = "Maximum number of point"),
        @ApiParam(name="JSON POST DATA: location", type="string", paramType = ApiParamType.QUERY,description = "WKT form of Y"),
        @ApiParam(name="JSON POST DATA: review", type="boolean", paramType = ApiParamType.QUERY,description = "Only get reviewed annotation"),
        @ApiParam(name="JSON POST DATA: image", type="long", paramType = ApiParamType.QUERY,description = "The image id"),
        @ApiParam(name="JSON POST DATA: remove", type="boolean", paramType = ApiParamType.QUERY,description = "Add or remove Y"),
        @ApiParam(name="JSON POST DATA: layers", type="list", paramType = ApiParamType.QUERY,description = "List of layers id")
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
        println "findAnnotationIdThatTouch"
        ImageInstance image = ImageInstance.read(idImage)
        boolean projectAdmin = image.project.checkPermission(ADMINISTRATION)
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
                "AND $userColumnName IN (${layers.join(',')})\n" +
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
        if(users.size()>1) {
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

        println "annotations="+annotations

        def termSizes = [:]
        annotations.each { annotation ->
            def terms = annotation.termsId()
            println "terms="+terms
            terms.each { term->
                def value = termSizes.get(term)?:0
                 println "add term="+value + "+"+annotation.area
                 termSizes.put(term,value+annotation.area)

            }
        }

        println "termSizes="+termSizes

        Double min = Double.MAX_VALUE
        Long goodTerm = null

        if(!termSizes.isEmpty()) {
            termSizes.each {
               if(min>it.value) {
                   min=it.value
                   goodTerm = it.key
               }
            }

            println "goodTerm="+goodTerm
            println "min="+min

            ids = []
            annotations.each { annotation ->
                def terms = annotation.termsId()
                if(terms.contains(goodTerm)) {
                    ids << annotation.id
                }
            }
        }

        println "ids="+ids

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
        boolean multipleTerm
        def termSize = [:]
        List<UserAnnotation> annotations = []

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
