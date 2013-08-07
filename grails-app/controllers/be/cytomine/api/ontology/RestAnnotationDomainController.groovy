package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.SecurityACL
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.sql.AlgoAnnotationListing
import be.cytomine.sql.AnnotationListing
import be.cytomine.sql.ReviewedAnnotationListing
import be.cytomine.sql.UserAnnotationListing
import be.cytomine.utils.GeometryUtils
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import groovy.sql.Sql

import java.text.SimpleDateFormat
import static org.springframework.security.acls.domain.BasePermission.ADMINISTRATION
/**
 * Controller that handle request on annotation.
 * It's main utility is to redirect request to the correct controller: user/algo/reviewed
 */
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



    /**
     * Search service for all annotation type
     * see AnnotationListing for all filters available
     */
    def search = {
         try {
             responseSuccess(doSearch(params).result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def downloadSearched = {
        println "downloadSearched"
        def lists = doSearch(params)
        downloadDocument(lists.result,lists.project)
    }


    private doSearch(def params) {
        AnnotationListing al
        def result = []

        if(isReviewedAnnotationAsked(params)) {
            al = new ReviewedAnnotationListing()
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
        println "a=${al.kmeansValue}"

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
        println "kmeans=${al.kmeans}"
        annotationListingService.listGeneric(al)
    }

    /**
     * Download report for an annotation listing
     */
    def downloadDocumentByProject = {

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

    def listIncludedAnnotation = {
        responseSuccess(getIncludedAnnotation(params))
    }

    def downloadIncludedAnnotation = {
        println "downloadIncludedAnnotation"
        def image = imageInstanceService.read(params.long('idImage'))
        def lists = getIncludedAnnotation(params,['basic','meta','gis','image','term'])
        downloadPdf(lists,image.project)
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
    def show = {
        AnnotationDomain annotation = userAnnotationService.read(params.long('id'))
        if (annotation) {
            forward(controller: "restUserAnnotation", action: "show")
        }
        else {
            annotation = algoAnnotationService.read(params.long('id'))
            if (annotation) {
                forward(controller: "restAlgoAnnotation", action: "show")
            } else {
                forward(controller: "restReviewedAnnotation", action: "show")
            }
        }
    }

    /**
     * Add an annotation
     * Redirect to the controller depending on the user type
     */
    def add = {
        SecUser user = cytomineService.currentUser
        if (user.algo()) {
            forward(controller: "restAlgoAnnotation", action: "add")
        } else {
            forward(controller: "restUserAnnotation", action: "add")
        }
    }

    /**
     * Update an annotation
     * Redirect to the good controller with the annotation type
     */
    def update = {
        if (params.getBoolean('fill'))
        //if fill param is set, annotation will be filled (removed empty area inside geometry)
            forward(action: "fillAnnotation")
        else {
            try {
                SecUser user = cytomineService.currentUser
                if (user.algo()) {
                    //if user is algo, redirect to the correct controller
                    forward(controller: "restAlgoAnnotation", action: "update")
                } else {
                    //if user is human, check if its a user annotation or a reviewed annotation
                    AnnotationDomain annotation = UserAnnotation.read(params.getLong("id"))

                    if (annotation)
                        forward(controller: "restUserAnnotation", action: "update")
                    else {
                        annotation = ReviewedAnnotation.read(params.getLong("id"))

                        if (annotation) {
                            if (annotation.user != user) {
                                throw new ForbiddenException("You cannot update this annotation! Only ${annotation.user.username} can do that!")
                            }
                            forward(controller: "restReviewedAnnotation", action: "update")
                        }
                        else {
                            throw new ObjectNotFoundException("Annotation not found with id " + params.id)
                        }
                    }
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
    def delete = {
        try {
            if (cytomineService.currentUser.algo()) {
                forward(controller: "restAlgoAnnotation", action: "delete")
            } else {
                forward(controller: "restUserAnnotation", action: "delete")
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def simplify = {

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

    /**
     * Fill an annotation.
     * Remove empty space in the polygon
     */
    def fillAnnotation = {
        log.info "fillAnnotation"
        try {
            AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(params.long('id'))
            if (!annotation) {
                throw new ObjectNotFoundException("Review Annotation ${params.long('id')} not found!")
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
    def addCorrection = {
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
        boolean projectAdmin = image.project.checkPermission(ADMINISTRATION)
        if(!projectAdmin) {
            layers = layers.findAll{(it+"")==(cytomineService.currentUser.id+"")}
        }


        String request = "SELECT annotation.id,user_id\n" +
                "FROM $table annotation\n" +
                "WHERE annotation.image_id = $idImage\n" +
                "AND user_id IN (${layers.join(',')})\n" +
                "AND ST_Intersects(annotation.location,ST_GeometryFromText('" + location + "',0));"

        def sql = new Sql(dataSource)
        List<Long> ids = []
        List<Long> users = []
        sql.eachRow(request) {
            ids << it[0]
            users << it[1]
        }
        users.unique()
        if(users.size()>1) {
            throw new WrongArgumentException("Annotations from multiple users are under this area. You can correct only annotation from 1 user (hide layer if necessary)")
        }
        return ids
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
