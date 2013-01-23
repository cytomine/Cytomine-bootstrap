package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ForbiddenException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import groovy.sql.Sql
import be.cytomine.SecurityCheck

/**
 * Controller that handle request on annotation.
 * It's main utility is to redirect request to the correct controller: user/algo/reviewed
 */
class RestAnnotationDomainController extends RestController {

    def exportService
    def userAnnotationService
    def domainService
    def termService
    def imageInstanceService
    def userService
    def projectService
    def cytomineService
    def dataSource
    def algoAnnotationService
    def reviewedAnnotationService
    def paramsService

    /**
     * List annotation by project
     * If user filter is set, redirect request to the user type controller
     */
    def listByProject = {

        Project project = projectService.read(params.long('id'))

        if (project) {
            List<SecUser> userList = paramsService.getParamsSecUserDomainList(params.users, project)
            if (!userList.isEmpty() && userList.get(0)?.algo()) {
                forward(controller: "restAlgoAnnotation", action: "listByProject")
            } else {
                forward(controller: "restUserAnnotation", action: "listByProject")
            }
        }
        else {
            responseNotFound("Project", params.id)
        }
    }

    /**
     * List annotation by image and user
     * Redirect to the user corresponding controller
     */
    def listByImageAndUser = {
        def user = SecUser.read(params.idUser)
        if (user) {
            if (user.algo()) {
                forward(controller: "restAlgoAnnotation", action: "listByImageAndUser")
            } else {
                forward(controller: "restUserAnnotation", action: "listByImageAndUser")
            }
        } else {
            responseNotFound("User", params.idUser)
        }
    }

    /**
     * List annotation by project and term
     */
    def listAnnotationByProjectAndTerm = {

        Project project = projectService.read(params.long('idproject'))

        List<SecUser> userList = paramsService.getParamsSecUserDomainList(params.users, project)
        if (!userList.isEmpty() && userList.get(0)?.algo()) {
            forward(controller: "restAlgoAnnotation", action: "listAnnotationByProjectAndTerm")
        } else {
            forward(controller: "restUserAnnotation", action: "listAnnotationByProjectAndTerm")
        }
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

        if (!users.isEmpty() && SecUser.read(users.first()).algo()) {
            forward(controller: "restAlgoAnnotation", action: "downloadDocumentByProject")
        } else {
            forward(controller: "restUserAnnotation", action: "downloadDocumentByProject")
        }
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
        def result
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
                def result
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
                //responseResult(result)
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

            def response = [:]

            //Is the first polygon always the big 'boundary' polygon?
            String newGeom = fillForm(annotation.location.toText())
            def json = JSON.parse(annotation.encodeAsJSON())
            json.location = newGeom

            if (annotation.algoAnnotation) {
                responseSuccess(algoAnnotationService.update(json,new SecurityCheck(annotation)))
            }
            else if (annotation.reviewedAnnotation) {
                responseSuccess(reviewedAnnotationService.update(json,new SecurityCheck(annotation)))
            }
            else {
                responseSuccess(userAnnotationService.update(json,new SecurityCheck(annotation)))
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Fill form to complete empty space inside polygon/mulypolygon
     * @param form A polygon or multipolygon wkt form
     * @return A polygon or multipolygon filled points
     */
    private String fillForm(String form) {
        if (form.startsWith("POLYGON")) return "POLYGON(" + getFirstPolygonLocation(form) + ")";
        else if (form.startsWith("MULTIPOLYGON")) return "MULTIPOLYGON(" + getFirstPolygonLocationForEachItem(form) + ")";
        else throw new WrongArgumentException("Form cannot be filled:" + form)
    }

    /**
     * Fill all polygon inside a Multipolygon WKT form
     * @param form Multipolygon WKT form
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
     * @param form Polygon as wkt
     * @return Polygon filled points
     */
    private String getFirstPolygonLocation(String form) {
        int i = 0;
        int start, stop;
        while (form.charAt(i) != '(') i++;
        while (form.charAt(i + 1) == '(') i++;
        start = i;
        while (form.charAt(i) != ')') i++;
        stop = i;
        return form.substring(start, stop + 1);
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
        try {
            List<Long> idsReviewedAnnotation = []
            List<Long> idsUserAnnotation = []

            //if review mode, priority is done to reviewed annotation correction
            if (review) {
                idsReviewedAnnotation = findAnnotationIdThatTouch(location, idImage, cytomineService.currentUser.id, "reviewed_annotation")
            }

            //there is no reviewed intersect annotation or user is not in review mode
            if (idsReviewedAnnotation.isEmpty()) {
                idsUserAnnotation = findAnnotationIdThatTouch(location, idImage, cytomineService.currentUser.id, "user_annotation")
            }

            log.info "idsReviewedAnnotation=$idsReviewedAnnotation"
            log.info "idsUserAnnotation=$idsUserAnnotation"

            //there is no user/reviewed intersect
            if (idsUserAnnotation.isEmpty() && idsReviewedAnnotation.isEmpty()) {
                throw new WrongArgumentException("There is no intersect annotation!")
            }

            if (!idsUserAnnotation.isEmpty()) {
                responseResult(doCorrectUserAnnotation(idsUserAnnotation, location, remove))
            } else {
                responseResult(doCorrectReviewedAnnotation(idsReviewedAnnotation, location, remove))
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
    def findAnnotationIdThatTouch(String location, long idImage, long idUser, String table) {

        String request = "SELECT annotation.id\n" +
                "FROM $table annotation\n" +
                "WHERE annotation.image_id = $idImage\n" +
                "AND user_id = $idUser\n" +
                "AND ST_Intersects(annotation.location,GeometryFromText('" + location + "',0));"

        def sql = new Sql(dataSource)
        List<Long> ids = []
        sql.eachRow(request) {
            ids << it[0]
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

        def result
        if (!remove) {
            //union will be made:
            // -add the new geometry to the based annotation location.
            // -add all other annotation geometry to the based annotation location (and delete other annotation)
            based.location = based.location.union(newGeometry)
            allAnnotationWithSameTerm.eachWithIndex { other, i ->
                based.location = based.location.union(other.location)
                reviewedAnnotationService.delete(JSON.parse(other.encodeAsJSON()),new SecurityCheck(other))
            }
            result = reviewedAnnotationService.update(JSON.parse(based.encodeAsJSON()),new SecurityCheck(based))
        } else {
            //diff will be made
            //-remove the new geometry from the based annotation location
            //-remove the new geometry from all other annotation location
            based.location = based.location.difference(newGeometry)
            if (based.location.getNumPoints() < 2) throw new WrongArgumentException("You cannot delete an annotation with substract! Use reject or delete tool.")
            result = reviewedAnnotationService.update(JSON.parse(based.encodeAsJSON()),new SecurityCheck(based))
            allAnnotationWithSameTerm.eachWithIndex { other, i ->
                other.location = other.location.difference(newGeometry)
                reviewedAnnotationService.update(JSON.parse(other.encodeAsJSON()),new SecurityCheck(other))
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

        def result
        if (!remove) {
            //union will be made:
            // -add the new geometry to the based annotation location.
            // -add all other annotation geometry to the based annotation location (and delete other annotation)
            based.location = based.location.union(newGeometry)
            allAnnotationWithSameTerm.eachWithIndex { other, i ->
                based.location = based.location.union(other.location)
                userAnnotationService.delete(JSON.parse(other.encodeAsJSON()),new SecurityCheck(other))
            }
            result = userAnnotationService.update(JSON.parse(based.encodeAsJSON()),new SecurityCheck(based))
        } else {
            //diff will be made
            //-remove the new geometry from the based annotation location
            //-remove the new geometry from all other annotation location
            based.location = based.location.difference(newGeometry)
            if (based.location.getNumPoints() < 2) throw new WrongArgumentException("You cannot delete an annotation with substract! Use reject or delete tool.")
            result = userAnnotationService.update(JSON.parse(based.encodeAsJSON()),new SecurityCheck(based))
            allAnnotationWithSameTerm.eachWithIndex { other, i ->
                other.location = other.location.difference(newGeometry)
                userAnnotationService.update(JSON.parse(other.encodeAsJSON()),new SecurityCheck(other))
            }
        }
        return result
    }
}
