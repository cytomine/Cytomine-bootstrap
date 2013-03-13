package be.cytomine.ontology

import be.cytomine.SecurityACL
import be.cytomine.api.UrlApi
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.GeometryUtils
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import groovy.sql.Sql
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions

import static org.springframework.security.acls.domain.BasePermission.READ

class ReviewedAnnotationService extends ModelService {

    static transactional = true
    def annotationPropertyService
    def cytomineService
    def transactionService
    def annotationTermService
    def retrievalService
    def algoAnnotationTermService
    def modelService
    def simplifyGeometryService
    def dataSource

    def currentDomain() {
        return ReviewedAnnotation
    }

    ReviewedAnnotation get(def id) {
        def annotation = ReviewedAnnotation.get(id)
        if (annotation) {
            SecurityACL.check(annotation.container(),READ)
        }
        annotation
    }

    ReviewedAnnotation read(def id) {
        def annotation = ReviewedAnnotation.read(id)
        if (annotation) {
            SecurityACL.check(annotation.container(),READ)
        }
        annotation
    }

    def list(Project project) {
        SecurityACL.check(project.container(),READ)
        ReviewedAnnotation.findAllByProject(project)
    }

    def list(ImageInstance image) {
        SecurityACL.check(image.container(),READ)
        ReviewedAnnotation.findAllByImage(image)
    }

    def list(Project project, List<Long> userList, List<Long> imageList, List<Long> termList) {
        SecurityACL.check(project.container(),READ)
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
    def list(ImageInstance image, String bbox) {
        SecurityACL.check(image.container(),READ)
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
    def list(ImageInstance image, SecUser user, String bbox) {
        SecurityACL.check(image.container(),READ)
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

    def list(ImageInstance image, Term term) {
        SecurityACL.check(image.container(),READ)
        def reviewed = ReviewedAnnotation.createCriteria().list {
            createAlias "terms", "t"
            eq("image", image)
            eq("t.id", term.id)
            order("created", "desc")
        }
        reviewed
    }

    def list(ImageInstance image, SecUser user) {
        SecurityACL.check(image.container(),READ)
        ReviewedAnnotation.createCriteria()
                .add(Restrictions.eq("user", user))
                .add(Restrictions.eq("image", image))
                .list()
    }

    def list(Project project, Term term, List<Long> userList, List<Long> imageInstanceList) {
        SecurityACL.check(project.container(),READ)
        boolean allImages = ImageInstance.countByProject(project)==imageInstanceList.size()
        String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, 1 as countReviewedAnnotations,at2.term_id as term, at2.reviewed_annotation_terms_id as annotationTerms,a.user_id as userTerm,a.wkt_location as location  \n" +
                " FROM reviewed_annotation a, reviewed_annotation_term at,reviewed_annotation_term at2,reviewed_annotation_term at3\n" +
                " WHERE a.id = at.reviewed_annotation_terms_id \n" +
                " AND a.project_id = " + project.id + "\n" +
                " AND at3.term_id = " + term.id + "\n" +
                " AND a.id = at2.reviewed_annotation_terms_id\n" +
                " AND a.id = at3.reviewed_annotation_terms_id\n" +
                " AND a.user_id IN (" + userList.collect {it}.join(",") + ") \n" +
                (allImages? " AND a.image_id IN (" + imageInstanceList.collect {it}.join(",") + ") \n" : "") +
                " ORDER BY id desc, term"
        selectReviewedAnnotationFull(request)
    }

    /**
     * Execute request and format result into a list of map
     */
    private def selectReviewedAnnotationFull(String request) {
        log.info "REQUEST=" + request
        def data = []
        long lastAnnotationId = -1
        long lastTermId = -1

        new Sql(dataSource).eachRow(request) {
            /**
             * If an annotation has n multiple term, it will be on "n" lines.
             * For the first line for this annotation (it.id!=lastAnnotationId), add the annotation data,
             * For the other lines, we add term data to the last annotation
             */
            if (it.id != lastAnnotationId) {
                data << [
                        'class': 'be.cytomine.ontology.ReviewedAnnotation',
                        id: it.id,
                        image: it.image,
                        geometryCompression: it.geometryCompression,
                        project: it.project,
                        container: it.project,
                        user: it.user,
                        nbComments: it.nbComments,
                        created: it.created,
                        updated: it.updated,
                        reviewed: (it.countReviewedAnnotations > 0),
                        cropURL: UrlApi.getUserAnnotationCropWithAnnotationId(it.id),
                        smallCropURL: UrlApi.getReviewedAnnotationCropWithAnnotationIdWithMaxWithOrHeight(it.id, 256),
                        url: UrlApi.getReviewedAnnotationCropWithAnnotationId(it.id),
                        imageURL: UrlApi.getAnnotationURL(it.project, it.image, it.id),
                        term: (it.term ? [it.term] : []),
                        userByTerm: (it.term ? [[id: it.annotationTerms, term: it.term, user: [it.userTerm]]] : []),
                        location: it.location
                ]
            } else {
                if (it.term) {
                    data.last().term.add(it.term)
                    data.last().term.unique()
                    if (it.term == lastTermId) {
                        data.last().userByTerm.last().user.add(it.userTerm)
                        data.last().userByTerm.last().user.unique()
                    } else {
                        data.last().userByTerm.add([id: it.annotationTerms, term: it.term, user: [it.userTerm]])
                    }
                }
            }
            lastTermId = it.term
            lastAnnotationId = it.id
        }
        data
    }


    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        println "json=$json"
        //read annotation (annotation or annotationIdent)

        SecurityACL.check(json.project,Project,READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        Transaction transaction = transactionService.start()
        //Synchronzed this part of code, prevent two annotation to be add at the same time
        synchronized (this.getClass()) {
            //Add annotation user
            json.user = currentUser.id
            //Add Annotation
            log.debug this.toString()
            def result = executeCommand(new AddCommand(user: currentUser, transaction: transaction),null,json)
            def annotationID = result?.data?.reviewedannotation?.id
            return result
        }
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(ReviewedAnnotation annotation, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.isCreator(annotation,currentUser)
        def result = executeCommand(new EditCommand(user: currentUser),annotation,jsonNewData)
        return result
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(ReviewedAnnotation domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.isCreator(domain,currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.user.toString(), domain.image?.baseImage?.filename]
    }

    def deleteDependentAlgoAnnotationTerm(ReviewedAnnotation annotation, Transaction transaction, Task task = null) {
        AlgoAnnotationTerm.findAllByAnnotationIdent(annotation.id).each {
            algoAnnotationTermService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentHasManyTerm(ReviewedAnnotation annotation, Transaction transaction, Task task = null) {
        annotation.terms?.clear()
     }

    def deleteDependentAnnotationProperty(ReviewedAnnotation ra, Transaction transaction, Task task = null) {
        AnnotationProperty.findAllByAnnotationIdent(ra.id).each {
            annotationPropertyService.delete(it,transaction,null,false)
        }

    }

}
