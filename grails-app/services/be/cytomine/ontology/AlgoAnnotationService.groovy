package be.cytomine.ontology

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.image.ImageInstance

import be.cytomine.project.Project
import be.cytomine.security.SecUser

import com.vividsolutions.jts.geom.Geometry

import com.vividsolutions.jts.geom.Polygon
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.io.WKTWriter
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.FetchMode

import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.AnnotationDomain
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions
import be.cytomine.processing.Job
import be.cytomine.security.UserJob

class AlgoAnnotationService extends ModelService {

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

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image) {
        AlgoAnnotation.findAllByImage(image)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        AlgoAnnotation.findAllByProject(project)
    }

    @PreAuthorize("#job.hasPermission(#job.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(Job job) {
        List<UserJob> user = UserJob.findAllByJob(job);
        List<AlgoAnnotation> algoAnnotations = []
        user.each {
            algoAnnotations.addAll(AlgoAnnotation.findAllByUser(it))
        }
        return algoAnnotations
    }

    @PreAuthorize("#job.hasPermission(#job.project,'READ') or hasRole('ROLE_ADMIN')")
    def count(Job job) {
        List<UserJob> user = UserJob.findAllByJob(job);
        long total = 0
        user.each {
            total = total + AlgoAnnotation.countByUser(it)
        }
        return total
    }


    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, SecUser user) {
        return AlgoAnnotation.findAllByImageAndUser(image, user)
    }

    @PreAuthorize("#image.hasPermission(#image.project,'READ') or hasRole('ROLE_ADMIN')")
    def list(ImageInstance image, SecUser user, String bbox, Boolean notReviewedOnly) {
        String[] coordinates = bbox.split(",")
        double bottomX = Double.parseDouble(coordinates[0])
        double bottomY = Double.parseDouble(coordinates[1])
        double topX = Double.parseDouble(coordinates[2])
        double topY = Double.parseDouble(coordinates[3])
        Coordinate[] boundingBoxCoordinates = [new Coordinate(bottomX, bottomY), new Coordinate(bottomX, topY), new Coordinate(topX, topY), new Coordinate(topX, bottomY), new Coordinate(bottomX, bottomY)]
        Geometry boundingbox = new GeometryFactory().createPolygon(new GeometryFactory().createLinearRing(boundingBoxCoordinates), null)

        if(!notReviewedOnly) {
            AlgoAnnotation.createCriteria()
                    .add(Restrictions.eq("user", user))
                    .add(Restrictions.eq("image", image))
                    .add(SpatialRestrictions.within("location",boundingbox))
                    .list()
        } else {
            AlgoAnnotation.createCriteria()
                    .add(Restrictions.eq("user", user))
                    .add(Restrictions.eq("image", image))
                    .add(Restrictions.eq("countReviewedAnnotations", 0))
                    .add(SpatialRestrictions.within("location",boundingbox))
                    .list()
        }


    }

    AlgoAnnotation get(def id) {
        AlgoAnnotation.get(id)
    }

    AlgoAnnotation read(def id) {
        AlgoAnnotation.read(id)
    }

    /**
     * List all algoAnnotation
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, List<Long> userList, List<Long> imageInstanceList, boolean noTerm, boolean multipleTerm) {
        log.info("project/userList/noTerm/multipleTerm project=$project.id userList=$userList imageInstanceList=${imageInstanceList.size()} noTerm=$noTerm multipleTerm=$multipleTerm")
        if (userList.isEmpty()) return []
        if (imageInstanceList.isEmpty()) return []
        else if (multipleTerm) {
            log.info "multipleTerm"
            //get all algoannotationterm where annotation id is twice

            def data = []

            def annotationsWithTerms = AlgoAnnotationTerm.withCriteria() {
                eq("project", project)
                inList("userJob.id", userList)
                projections {
                    groupProperty("annotationIdent")
                    groupProperty("annotationClassName")
                    countDistinct("term")
                    countDistinct('created', 'createdSort')
                }
                order('createdSort','desc')
            }
            annotationsWithTerms.each {
                String id = it[0]
                String className = it[1]
                Long termNumber = (Long)it[2]

                if(termNumber>1) {
                    AnnotationDomain annotation = AlgoAnnotationTerm.retrieveAnnotationDomain(id,className)
                    if(imageInstanceList.contains(annotation.image.id)) {
                        data << annotation
                    }
                }
            }
            return data
        }
        else if (noTerm) {
            log.info "noTerm"

            def annotationsWithTerms = AlgoAnnotationTerm.withCriteria() {
                eq("project", project)
                inList("userJob.id", userList)
                projections {
                    groupProperty("annotationIdent")
                }
            }

            println "annotationsWithTerms="+annotationsWithTerms

            //annotationsWithTerms = annotationsWithTerms.collect{it[0]}

            //inList crash is argument is an empty list so we have to use if/else at this time
            def annotations = []


            if (annotationsWithTerms.size() == 0) {
                annotations.addAll(UserAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image.id", imageInstanceList)
                    inList("user.id", userList)
                    order 'created', 'desc'
                })
                annotations.addAll(AlgoAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image.id", imageInstanceList)
                    inList("user.id", userList)
                    order 'created', 'desc'
                })
            } else {
                annotations.addAll(UserAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image.id", imageInstanceList)
                    inList("user.id", userList)
                    not {
                        inList("id", annotationsWithTerms)
                    }
                    order 'created', 'desc'
                })
                annotations.addAll(AlgoAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("image.id", imageInstanceList)
                    inList("user.id", userList)
                    not {
                        inList("id", annotationsWithTerms)
                    }
                    order 'created', 'desc'
                })
            }

            return annotations
        } else {
            log.info "findAllByProjectAndUserInList=" + project + " users=" + userList
            long start = new Date().time
            def annotations = AlgoAnnotation.createCriteria().list {
                eq("project", project)
                inList("user.id", userList)
                inList("image.id", imageInstanceList)
                fetchMode 'image', FetchMode.JOIN
                fetchMode 'image.baseImage', FetchMode.JOIN
                order 'created', 'desc'
            }
            long end = new Date().time
            log.info "time = " + (end - start) + "ms"
            return annotations
        }
    }


    def listForUserJob(Project project, Term term, List<Long> userList, List<Long> imageInstanceList) {
         if (userList.isEmpty()) return []
         if (imageInstanceList.isEmpty()) return []
         if (imageInstanceList.size() == project.countImages) {
             //TODO:: May be speedup without using hibernate (direct SQL request)
             List annotationsUsers = AlgoAnnotation.executeQuery(
                     "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM UserAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc",[project:project,term:term,userList:userList])

             List annotationsAlgo = AlgoAnnotation.executeQuery(
                     "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM AlgoAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc",[project:project,term:term,userList:userList])

             annotationsUsers.addAll(annotationsAlgo)
             return annotationsUsers
         } else {

             List annotationsUsers = AlgoAnnotation.executeQuery(
                     "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM UserAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND a.image.id IN (:imageInstanceList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc",[project:project,term:term,userList:userList,imageInstanceList:imageInstanceList])

             List annotationsAlgo = AlgoAnnotation.executeQuery(
                     "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM AlgoAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND a.image.id IN (:imageInstanceList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc",[project:project,term:term,userList:userList,imageInstanceList:imageInstanceList])

             annotationsUsers.addAll(annotationsAlgo)
             return annotationsUsers
         }
     }


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
            //Stop transaction
            transactionService.stop()

            return result
        }
    }

    @PreAuthorize("#domain.user.id == principal.id  or hasRole('ROLE_ADMIN')")
    def update(def domain, def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        //simplify annotation
        try {
            def annotation = AlgoAnnotation.read(json.id)
            def data = simplifyGeometryService.simplifyPolygon(json.location, annotation?.geometryCompression)
            json.location = new WKTWriter().write(data.geometry)
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        def result = executeCommand(new EditCommand(user: currentUser), json)

        return result
    }

    @PreAuthorize("#domain.user.id == principal.id  or hasRole('ROLE_ADMIN')")
    def delete(def domain, def json) {

        SecUser currentUser = cytomineService.getCurrentUser()

        //Start transaction
        Transaction transaction = transactionService.start()

        //Delete annotation (+cascade)
        def result = deleteAnnotation(json.id, currentUser, transaction)

        //Stop transaction
        transactionService.stop()

        //Remove annotation from retrieval
        log.info "Remove " + json.id + " from retrieval"
        return result
    }


    def deleteAnnotation(Long idAnnotation, SecUser currentUser, Transaction transaction) {
        return deleteAnnotation(idAnnotation, currentUser, true, transaction)
    }

    def deleteAnnotation(AnnotationDomain annotation, SecUser currentUser, boolean printMessage, Transaction transaction) {

        if (annotation) {
           //TODO:: delete all domain that reference the deleted domain
        }
        //Delete annotation
        def json = JSON.parse("{id: $annotation.id}")
        def result = executeCommand(new DeleteCommand(user: currentUser, transaction: transaction), json)
        return result
    }

    def deleteAnnotation(Long idAnnotation, SecUser currentUser, boolean printMessage, Transaction transaction) {
        log.info "Delete algoannotation: " + idAnnotation
        AlgoAnnotation annotation = AlgoAnnotation.read(idAnnotation)
        return deleteAnnotation(annotation, currentUser, printMessage, transaction)
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(AlgoAnnotation.createFromDataWithId(json), printMessage)
    }

    def create(AlgoAnnotation domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Add", domain.getCallBack())

        //we store data into annotation instead of algoannotation
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')

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
        destroy(AlgoAnnotation.get(json.id), printMessage)
    }

    def destroy(AlgoAnnotation domain, boolean printMessage) {
        //Build response message
        log.info "destroy remove " + domain.id
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Delete", domain.getCallBack())

        //we store data into annotation instead of algoannotation
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')

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
        edit(fillDomainWithData(new AlgoAnnotation(), json), printMessage)
    }

    def edit(AlgoAnnotation domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.user.toString(), domain.image?.baseImage?.filename], printMessage, "Edit", domain.getCallBack())

        //we store data into annotation instead of algoannotation
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')

        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    AlgoAnnotation createFromJSON(def json) {
        return AlgoAnnotation.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        AlgoAnnotation annotation = AlgoAnnotation.get(json.id)
        if (!annotation) throw new ObjectNotFoundException("AlgoAnnotation " + json.id + " not found")
        return annotation
    }

}
