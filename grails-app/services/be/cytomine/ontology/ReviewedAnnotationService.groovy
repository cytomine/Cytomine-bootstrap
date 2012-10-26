package be.cytomine.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.image.ImageInstance
import be.cytomine.image.server.RetrievalServer
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.social.SharedAnnotation
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.io.WKTWriter
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.FetchMode
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions
import org.springframework.security.access.prepost.PostFilter
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

    boolean saveOnUndoRedoStack = true

    //reviewedAnnotationService.list(Project)
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        ReviewedAnnotation.findAllByProject(project)
    }

    //reviewedAnnotationService.list(ImageInstance)
    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image) {
        ReviewedAnnotation.findAllByImage(image)
    }

    //reviewedAnnotationService.list(Project, List<SecUser>, Lis<ImageInstance>, List<Term>, boolean duplicate)
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, List<SecUser> userList, List<ImageInstance> imageList, List<Term> termList, boolean conflict) {
        //TODO:: improve perf by query duplication (if imageList.size = project.image.size then don't query with inList)
        log.info "userList="+userList
        log.info "imageList="+imageList
        log.info "termList="+termList
        if(!conflict) {
            def reviewed = ReviewedAnnotation.createCriteria().list {
                eq("project",project)
                inList("user", userList)
                inList("image", imageList)
                order("created", "desc")
            }
            def annotationWithThisTerm = []
            def termListId = termList.collect {it.id}
            reviewed.each { review ->
                boolean hasTerm = false
                review.terms().each { term ->
                    if(termListId.contains(term.id)) hasTerm = true
                }
                if(hasTerm) annotationWithThisTerm << review
            }
            return annotationWithThisTerm
        } else {
            def reviewed = ReviewedAnnotation.createCriteria().list {
                eq("project",project)
                inList("user", userList)
                inList("image", imageList)
                order("created", "desc")
            }
            def annotationWithThisTerm = []
            def termListId = termList.collect {it.id}
            reviewed.each { review ->
                boolean hasTerm = false
                review.terms().each { term ->
                    if(termListId.contains(term.id)) hasTerm = true
                }
                if(hasTerm) annotationWithThisTerm << review
            }

            def data = [:]
            def skipId = new HashMap<Long,Long>()
            annotationWithThisTerm.each { reviewedAnnotation ->
                if(!skipId.containsKey(reviewedAnnotation.id)) {
                    List<ReviewedAnnotation> alreadyExistReviewedAnnotation = ReviewedAnnotation.findAllByParentIdent(reviewedAnnotation.parentIdent)
                    boolean initialAnnotationAdded = false;
                    alreadyExistReviewedAnnotation.each { alreadyExist ->
                        //add to skip list to ignore next time
                        skipId.put(alreadyExist.id,0)
                        if(!shareCommonTerm(reviewedAnnotation,alreadyExist)) {
                            def item = data.get(reviewedAnnotation.parentIdent)
                            if(item==null) item = []
                            item << alreadyExist
                            //if first iteration, include based review annotation
                            if(!initialAnnotationAdded) {
                                initialAnnotationAdded = true;
                                skipId.put(reviewedAnnotation.id,0)
                                item << reviewedAnnotation
                            }
                            data.put(reviewedAnnotation.parentIdent,item)
                        }

                    }
                }


            }
            return data

        }
    }

    boolean shareCommonTerm(ReviewedAnnotation annotation1,ReviewedAnnotation annotation2) {

       return differenceTerm(annotation1.term,annotation2.term).isEmpty();
   }

    def differenceTerm(Set<Term> terms1,Set<Term> terms2) {
        Collection result = union(terms1, terms2);
        result.removeAll(intersect(terms1, terms2));
        return result;
    }

    public static Collection union(Collection coll1, Collection coll2)
    {
        Set union = new HashSet(coll1);
        union.addAll(new HashSet(coll2));
        return new ArrayList(union);
    }

    public static Set intersect(Set set1, Set set2)
    {
        Set intersection = new HashSet(set1);
        intersection.retainAll(new HashSet(set2));
        return intersection;
    }


    //reviewedAnnotationService.list(image, user, (String) params.bbox (optional))
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
                .add(SpatialRestrictions.within("location",boundingbox))
                .list()

    }

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, SecUser user) {
        ReviewedAnnotation.createCriteria()
                .add(Restrictions.eq("user", user))
                .add(Restrictions.eq("image", image))
                .list()

    }

    //reviewedAnnotationService.read
    ReviewedAnnotation get(def id) {
        ReviewedAnnotation.get(id)
    }

    ReviewedAnnotation read(def id) {
        ReviewedAnnotation.read(id)
    }

    //reviewedAnnotationService.add
    @PreAuthorize("hasRole('ROLE_USER')")
      def add(def json) {

          SecUser currentUser = cytomineService.getCurrentUser()

          //simplify annotation
          try {
              def data = simplifyGeometryService.simplifyPolygon(json.location)
              json.location = new WKTWriter().write(data.geometry)
              json.geometryCompression = data.rate
          } catch (Exception e) {
              log.error("Cannot simplify:" + e)
          }

          //Start transaction
          Transaction transaction = transactionService.start()

          //Synchronzed this part of code, prevent two annotation to be add at the same time
          synchronized (this.getClass()) {
              //Add annotation user
              json.user = currentUser.id
              //Add Annotation
              log.debug this.toString()
              def result = executeCommand(new AddCommand(user: currentUser, transaction: transaction), json)
              def annotationID = result?.data?.reviewedannotation?.id
              log.info "reviewedannotation=" + annotationID + " json.term=" + json.term

              //Stop transaction
              transactionService.stop()

              return result
          }
      }

    //reviewedAnnotationService.update
    @PreAuthorize("#domain.user.id == principal.id  or hasRole('ROLE_ADMIN')")
    def update(def domain, def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        //simplify annotation
        try {
            def annotation = UserAnnotation.read(json.id)
            def data = simplifyGeometryService.simplifyPolygon(json.location, annotation?.geometryCompression)
            json.location = new WKTWriter().write(data.geometry)
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        def result = executeCommand(new EditCommand(user: currentUser), json)
        return result
    }

    //reviewedAnnotationService.delete
    @PreAuthorize("#domain.user.id == principal.id  or hasRole('ROLE_ADMIN')")
    def delete(def domain, def json) {

        SecUser currentUser = cytomineService.getCurrentUser()

        //Start transaction
        Transaction transaction = transactionService.start()

        //Delete annotation (+cascade)
        def result = deleteAnnotation(ReviewedAnnotation.read(json.id), currentUser, true, transaction)

        //Stop transaction
        transactionService.stop()
        return result
    }

    def deleteAnnotation(ReviewedAnnotation annotation, SecUser currentUser, boolean printMessage, Transaction transaction) {

        if (annotation) {
            annotation.term.clear()
            annotation.save(flush:true)
        }
        //Delete annotation
        def json = JSON.parse("{id: $annotation.id}")
        def result = executeCommand(new DeleteCommand(user: currentUser, transaction: transaction), json)
        return result
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(ReviewedAnnotation.createFromDataWithId(json), printMessage)
    }

    def create(ReviewedAnnotation domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Add", domain.getCallBack())

        return response
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(ReviewedAnnotation.get(json.id), printMessage)
    }

    def destroy(ReviewedAnnotation domain, boolean printMessage) {
        //Build response message
        log.info "destroy remove " + domain.id
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new ReviewedAnnotation(), json), printMessage)
    }

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
        if (!annotation) throw new ObjectNotFoundException("ReviewedAnnotation " + json.id + " not found")
        return annotation
    }

}
