package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.utils.GeometryUtils
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTWriter
import groovy.sql.Sql
import org.hibernate.FetchMode

import static org.springframework.security.acls.domain.BasePermission.READ

class AlgoAnnotationService extends ModelService {

    static transactional = true
    def annotationPropertyService

    def cytomineService
    def transactionService
    def annotationTermService
    def algoAnnotationTermService
    def simplifyGeometryService
    def dataSource
    def reviewedAnnotationService

    def currentDomain() {
        return AlgoAnnotation
    }

    AlgoAnnotation get(def id) {
        def annotation = AlgoAnnotation.get(id)
        if (annotation) {
            SecurityACL.check(annotation.container(),READ)
        }
        annotation
    }

    AlgoAnnotation read(def id) {
        def annotation = AlgoAnnotation.read(id)
        if (annotation) {
            SecurityACL.check(annotation.container(),READ)
        }
        annotation
    }

    def list(ImageInstance image) {
        SecurityACL.check(image.container(),READ)
        AlgoAnnotation.findAllByImage(image)
    }

    def list(Project project) {
        SecurityACL.check(project,READ)
        AlgoAnnotation.findAllByProject(project)
    }

    def list(Job job) {
        SecurityACL.check(job.container(),READ)
        List<UserJob> user = UserJob.findAllByJob(job);
        List<AlgoAnnotation> algoAnnotations = []
        user.each {
            algoAnnotations.addAll(AlgoAnnotation.findAllByUser(it))
        }
        return algoAnnotations
    }

    def list(ImageInstance image, SecUser user) {
        SecurityACL.check(image.container(),READ)
        return AlgoAnnotation.findAllByImageAndUser(image, user)
    }

    /**
     * List annotation created by algorithm
     * @param image Image filter
     * @param user User Job that created annotation filter
     * @param bbox Boundary area filter
     * @param notReviewedOnly Flag to get only annotation that are not reviewed
     * @return Algo Annotation list
     */
    def list(ImageInstance image, SecUser user, String bbox, Boolean notReviewedOnly) {
        SecurityACL.check(image.container(),READ)

        Geometry boundingbox = GeometryUtils.createBoundingBox(bbox)

        //we use SQL request (not hibernate) to speedup time request
        String request

        if (!notReviewedOnly) {
            request = "SELECT annotation.id, annotation.wkt_location, at.term_id \n" +
                    " FROM algo_annotation annotation LEFT OUTER JOIN algo_annotation_term at ON annotation.id = at.annotation_ident\n" +
                    " WHERE annotation.image_id = $image.id\n" +
                    " AND annotation.user_id= $user.id\n" +
                    " AND ST_Intersects(annotation.location,GeometryFromText('" + boundingbox.toString() + "',0)) " +
                    " ORDER BY annotation.id "

        } else {
            //show only annotation that are not reviewed (use in review mode)
            request = "SELECT annotation.id, annotation.wkt_location, at.term_id \n" +
                    " FROM algo_annotation annotation LEFT OUTER JOIN algo_annotation_term at ON annotation.id = at.annotation_ident\n" +
                    " WHERE annotation.image_id = $image.id\n" +
                    " AND annotation.user_id= $user.id\n" +
                    " AND annotation.count_reviewed_annotations = 0 " +
                    " AND ST_Intersects(annotation.location,GeometryFromText('" + boundingbox.toString() + "',0)) " +
                    " ORDER BY annotation.id "
        }

        def sql = new Sql(dataSource)

        def data = []

        /*
        Request result will come like this if an annotation ahs multiple term;
        -annotation A - Term 1
        -annotation B - Term 1
        -annotation B - Term 2
        ...
        So during the sql result loop, we will group term by annotation like this:
        -annotation A - Term 1
        -annotation B - Term 1 & Term 2
        */

        long lastAnnotationId = -1

        sql.eachRow(request) {

            long idAnnotation = it[0]
            String location = it[1]
            def idTerm = it[2]

            if (idAnnotation != lastAnnotationId) {
                //if its a new annotation, create a new data line
                data << [id: idAnnotation, location: location, term: idTerm ? [idTerm] : []]
            } else {
                //annotation id is the same as the previous iteration, so, just add term
                if (idTerm)
                    data.last().term.add(idTerm)
            }
            lastAnnotationId = idAnnotation
        }
        data
    }

    /**
     * List Annotation created by algorithm
     * @param project Annotation project
     * @param userList Annotation user job
     * @param imageInstanceList Annotation image
     * @param noTerm Flag to get only annotation with no term
     * @param multipleTerm Flag to get only annotation with many terms
     * @return Algo annotation list
     */
    def list(Project project, List<Long> userList, List<Long> imageInstanceList, boolean noTerm, boolean multipleTerm) {
        SecurityACL.check(project,READ)
        log.info("project/userList/noTerm/multipleTerm project=$project.id userList=$userList imageInstanceList=${imageInstanceList.size()} noTerm=$noTerm multipleTerm=$multipleTerm")
        if (userList.isEmpty()) {
            return []
        } else if (imageInstanceList.isEmpty()) {
            return []
        } else if (multipleTerm) {
            log.info "multipleTerm"
            //TODO: could be improve with a single SQL Request
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
                order('createdSort', 'desc')
            }
            annotationsWithTerms.each {
                String id = it[0]
                String className = it[1]
                Long termNumber = (Long) it[2]

                if (termNumber > 1) {
                    AnnotationDomain annotation = AlgoAnnotationTerm.retrieveAnnotationDomain(id, className)
                    if (imageInstanceList.contains(annotation.image.id)) {
                        data << annotation
                    }
                }
            }
            return data
        }
        else if (noTerm) {
            log.info "noTerm"
            //TODO: could be improve with a single SQL Request
            def annotationsWithTerms = AlgoAnnotationTerm.withCriteria() {
                eq("project", project)
                inList("userJob.id", userList)
                projections {
                    groupProperty("annotationIdent")
                }
            }

            println "annotationsWithTerms=" + annotationsWithTerms

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

    /**
     * List all annotation created by algo
     * @param project Annotation project
     * @param term Term map with this algo annotation from at least an AlgoAnnotationTerm from the same AlgoAnnotation user
     * @param userList Annotation user job
     * @param imageInstanceList Annotation Imageinstance
     * @return Algo Annotation List
     */
    def listForUserJob(Project project, Term term, List<Long> userList, List<Long> imageInstanceList) {
        SecurityACL.check(project,READ)
        if (userList.isEmpty()) {
            return []
        } else if (imageInstanceList.isEmpty()) {
            return []
        } else if (imageInstanceList.size() == project.countImages) {
            //Get all images
            //TODO:: May be speedup without using hibernate (direct SQL request)
            List annotationsUsers = AlgoAnnotation.executeQuery(
                    "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM UserAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc", [project: project, term: term, userList: userList])

            List annotationsAlgo = AlgoAnnotation.executeQuery(
                    "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM AlgoAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc", [project: project, term: term, userList: userList])

            annotationsUsers.addAll(annotationsAlgo)
            return annotationsUsers
        } else {

            List annotationsUsers = AlgoAnnotation.executeQuery(
                    "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM UserAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND a.image.id IN (:imageInstanceList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc", [project: project, term: term, userList: userList, imageInstanceList: imageInstanceList])

            List annotationsAlgo = AlgoAnnotation.executeQuery(
                    "SELECT a, aat.rate, aat.term.id,aat.expectedTerm.id FROM AlgoAnnotation a, AlgoAnnotationTerm aat WHERE aat.project = :project AND aat.term = :term AND aat.userJob.id IN (:userList) AND a.image.id IN (:imageInstanceList) AND aat.annotationIdent=a.id ORDER BY aat.rate desc", [project: project, term: term, userList: userList, imageInstanceList: imageInstanceList])

            annotationsUsers.addAll(annotationsAlgo)
            return annotationsUsers
        }
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecurityACL.check(json.project, Project, READ)
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
            Command command = new AddCommand(user: currentUser, transaction: transaction)
            def result = executeCommand(command,null,json)

            return result
        }
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(AlgoAnnotation annotation, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.isCreator(annotation,currentUser)
        //simplify annotation
        try {
            def data = simplifyGeometryService.simplifyPolygon(jsonNewData.location, annotation?.geometryCompression)
            jsonNewData.location = new WKTWriter().write(data.geometry)
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

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
    def delete(AlgoAnnotation domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.isCreator(domain,currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }


    def getStringParamsI18n(def domain) {
        return [domain.user.toString(), domain.image?.baseImage?.filename]
    }

    def afterAdd(def domain, def response) {
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')
    }

    def afterDelete(def domain, def response) {
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')
    }

    def afterUpdate(def domain, def response) {
        response.data['annotation'] = response.data.algoannotation
        response.data.remove('algoannotation')
    }



    def deleteDependentAlgoAnnotationTerm(AlgoAnnotation ao, Transaction transaction, Task task = null) {
        AlgoAnnotationTerm.findAllByAnnotationIdent(ao.id).each {
            algoAnnotationTermService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentReviewedAnnotation(AlgoAnnotation aa, Transaction transaction, Task task = null) {
        ReviewedAnnotation.findAllByParentIdent(aa.id).each {
            reviewedAnnotationService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentAnnotationProperty(AlgoAnnotation aa, Transaction transaction, Task task = null) {
        AnnotationProperty.findAllByAnnotationIdent(aa.id).each {
            annotationPropertyService.delete(it,transaction,null,false)
        }

    }
}
