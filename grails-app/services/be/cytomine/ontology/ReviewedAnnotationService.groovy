package be.cytomine.ontology

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.GeometryUtils
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import grails.converters.JSON
import groovy.sql.Sql
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions
import org.springframework.security.access.prepost.PreAuthorize

class ReviewedAnnotationService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def annotationTermService
    def retrievalService
    def algoAnnotationTermService
    def responseService
    def domainService
    def securityService
    def simplifyGeometryService
    def dataSource

    boolean saveOnUndoRedoStack = true

    ReviewedAnnotation get(def id) {
        def annotation = ReviewedAnnotation.get(id)
        if (annotation) {
            SecurityCheck.checkReadAuthorization(annotation.project)
        }
        annotation
    }

    ReviewedAnnotation read(def id) {
        def annotation = ReviewedAnnotation.read(id)
        if (annotation) {
            SecurityCheck.checkReadAuthorization(annotation.project)
        }
        annotation
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        ReviewedAnnotation.findAllByProject(project)
    }

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image) {
        ReviewedAnnotation.findAllByImage(image)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, List<Long> userList, List<Long> imageList, List<Long> termList) {
        def reviewed = ReviewedAnnotation.createCriteria().list {
            eq("project", project)
            inList("user.id", userList)
            inList("image.id", imageList)
            order("created", "desc")
        }
        def annotationWithThisTerm = []
        reviewed.each { review ->
            boolean hasTerm = false
            review.terms().each { term ->
                if (termList.contains(term.id)) hasTerm = true
            }
            if (hasTerm) annotationWithThisTerm << review
        }
        return annotationWithThisTerm
    }

    /**
     * List validate annotation
     * @param image Image filter
     * @param bbox Boundary area filter
     * @return Reviewed Annotation list
     */
    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, String bbox) {
        Geometry boundingbox = GeometryUtils.createBoundingBox(bbox)
        /**
         * We will sort annotation so that big annotation that covers a lot of annotation comes first (appear behind little annotation so we can select annotation behind other)
         * We compute in 'gc' the set of all other annotation that must be list
         * For each review annotation, we compute the number of other annotation that cover it (ST_CoveredBy => t or f => 0 or 1)
         *
         * ST_CoveredBy will return false if the annotation is not perfectly "under" the compare annotation (if some points are outside)
         * So in gc, we increase the size of each compare annotation just for the check
         * So if an annotation x is under y but x has some point next outside y, x will appear top (if no resize, it will appear top or behind).
         */
        def xfactor = "1.08"
        def yfactor = "1.08"
        //TODO:: get zoom info from UI client, display with scaling only with hight zoom (< annotations)
        boolean zoomToLow = true
        String request
        if (zoomToLow) {
            request = "SELECT reviewed.id, reviewed.wkt_location, (SELECT SUM(ST_CoveredBy(ga.location,gb.location )::integer) FROM reviewed_annotation ga, reviewed_annotation gb WHERE ga.id=reviewed.id AND ga.id<>gb.id AND ga.image_id=gb.image_id AND ST_Intersects(gb.location,GeometryFromText('" + boundingbox.toString() + "',0))) as numberOfCoveringAnnotation\n" +
                    " FROM reviewed_annotation reviewed\n" +
                    " WHERE reviewed.image_id = $image.id\n" +
                    " AND ST_Intersects(reviewed.location,GeometryFromText('" + boundingbox.toString() + "',0))\n" +
                    " ORDER BY numberOfCoveringAnnotation asc, id asc"
        } else {
            //too heavy to use with little zoom
            request = "SELECT reviewed.id, reviewed.wkt_location, (SELECT SUM(ST_CoveredBy(ga.location,ST_Translate(ST_Scale(gb.location, $xfactor, $yfactor), ST_X(ST_Centroid(gb.location))*(1 - $xfactor), ST_Y(ST_Centroid(gb.location))*(1 - $yfactor) ))::integer) FROM reviewed_annotation ga, reviewed_annotation gb WHERE ga.id=reviewed.id AND ga.id<>gb.id AND ga.image_id=gb.image_id AND ST_Intersects(gb.location,GeometryFromText('" + boundingbox.toString() + "',0))) as numberOfCoveringAnnotation\n" +
                    " FROM reviewed_annotation reviewed\n" +
                    " WHERE reviewed.image_id = $image.id\n" +
                    " AND ST_Intersects(reviewed.location,GeometryFromText('" + boundingbox.toString() + "',0))\n" +
                    " ORDER BY numberOfCoveringAnnotation asc, id asc"
        }

        def sql = new Sql(dataSource)

        def data = []
        sql.eachRow(request) {
            data << [id: it[0], location: it[1], term: []]
        }
        data
    }

    /**
     * List validate annotation
     * @param image Image filter
     * @param user User Job that created annotation filter
     * @param bbox Boundary area filter
     * @return Reviewed Annotation list
     */
    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, SecUser user, String bbox) {
        String[] coordinates = bbox.split(",")
        double bottomX = Double.parseDouble(coordinates[0])
        double bottomY = Double.parseDouble(coordinates[1])
        double topX = Double.parseDouble(coordinates[2])
        double topY = Double.parseDouble(coordinates[3])
        Coordinate[] boundingBoxCoordinates = [new Coordinate(bottomX, bottomY), new Coordinate(bottomX, topY), new Coordinate(topX, topY), new Coordinate(topX, bottomY), new Coordinate(bottomX, bottomY)]
        Geometry boundingbox = new GeometryFactory().createPolygon(new GeometryFactory().createLinearRing(boundingBoxCoordinates), null)
        ReviewedAnnotation.createCriteria()
                .add(Restrictions.eq("user", user))
                .add(Restrictions.eq("image", image))
                .add(SpatialRestrictions.within("location", boundingbox))
                .list()
    }

    //reviewedAnnotationService.list(image, user, (String) params.bbox (optional))
    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, Term term) {
        def reviewed = ReviewedAnnotation.createCriteria().list {
            createAlias "term", "t"
            eq("image", image)
            eq("t.id", term.id)
            order("created", "desc")
        }
        reviewed
    }

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, SecUser user) {
        ReviewedAnnotation.createCriteria()
                .add(Restrictions.eq("user", user))
                .add(Restrictions.eq("image", image))
                .list()
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        Transaction transaction = transactionService.start()
        //Synchronzed this part of code, prevent two annotation to be add at the same time
        synchronized (this.getClass()) {
            //Add annotation user
            json.user = currentUser.id
            //Add Annotation
            log.debug this.toString()
            def result = executeCommand(new AddCommand(user: currentUser, transaction: transaction), json)
            def annotationID = result?.data?.reviewedannotation?.id
            //Stop transaction
            transactionService.stop()
            return result
        }
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkCurrentUserCreator(principal.id) or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def result = executeCommand(new EditCommand(user: currentUser), json)
        return result
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkCurrentUserCreator(principal.id) or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        //Start transaction
        Transaction transaction = transactionService.start()
        //Delete annotation (+cascade)
        def result = deleteAnnotation(ReviewedAnnotation.read(json.id), currentUser, true, transaction)
        //Stop transaction
        transactionService.stop()
        return result
    }

    /**
     * Delete a reviewed annotation from database
     * @param annotation Annotation to delete
     * @param currentUser User that will be the deleter user
     * @param printMessage Flag to tell the client to print confirmation message or not
     * @param transaction Transaction that will pack the delete command
     * @return Result structure
     */
    def deleteAnnotation(ReviewedAnnotation annotation, SecUser currentUser, boolean printMessage, Transaction transaction) {
        if (annotation) {
            //remove annotation term
            annotation.term.clear()
            annotation.save(flush: true)
        }
        //Delete annotation
        def json = JSON.parse("{id: $annotation.id}")
        def result = executeCommand(new DeleteCommand(user: currentUser, transaction: transaction), json)
        return result
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(ReviewedAnnotation.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(ReviewedAnnotation domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Add", domain.getCallBack())

        return response
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(ReviewedAnnotation.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(ReviewedAnnotation domain, boolean printMessage) {
        //Build response message
        log.info "destroy remove " + domain.id
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain from database
     * @param json domain data in json
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new ReviewedAnnotation(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(ReviewedAnnotation domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    ReviewedAnnotation createFromJSON(def json) {
        return ReviewedAnnotation.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        ReviewedAnnotation annotation = ReviewedAnnotation.get(json.id)
        if (!annotation) {
            throw new ObjectNotFoundException("ReviewedAnnotation " + json.id + " not found")
        }
        return annotation
    }

}
