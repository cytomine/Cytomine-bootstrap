package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.SecurityACL
import be.cytomine.api.UrlApi
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.sql.AlgoAnnotationListing
import be.cytomine.sql.AnnotationListing
import be.cytomine.sql.UserAnnotationListing
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
    def propertyService

    def cytomineService
    def transactionService
    def annotationTermService
    def algoAnnotationTermService
    def simplifyGeometryService
    def dataSource
    def reviewedAnnotationService
    def kmeansGeometryService
    def annotationListingService

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

    def list(ImageInstance image, def propertiesToShow = null) {
        SecurityACL.check(image.container(),READ)
        AnnotationListing al = new AlgoAnnotationListing(columnToPrint: propertiesToShow,image : image.id)
        annotationListingService.executeRequest(al)
    }

    def list(Project project,def propertiesToShow = null) {
        SecurityACL.check(project,READ)
        AnnotationListing al = new AlgoAnnotationListing(columnToPrint: propertiesToShow,project : project.id)
        annotationListingService.executeRequest(al)
    }

    def list(Job job,def propertiesToShow = null) {
        SecurityACL.check(job.container(),READ)
        List<UserJob> users = UserJob.findAllByJob(job);
        List algoAnnotations = []
        users.each { user ->
            AnnotationListing al = new AlgoAnnotationListing(columnToPrint: propertiesToShow,user : user.id)
            algoAnnotations.addAll(annotationListingService.executeRequest(al))
        }
        return algoAnnotations
    }

    def list(ImageInstance image, SecUser user,def propertiesToShow = null) {
        AnnotationListing al = new AlgoAnnotationListing(columnToPrint: propertiesToShow,image : image.id,user:user.id)
        annotationListingService.executeRequest(al)
    }

    /**
     * List annotation created by algorithm
     * @param image Image filter
     * @param user User Job that created annotation filter
     * @param bbox Boundary area filter (String)
     * @param notReviewedOnly Flag to get only annotation that are not reviewed
     * @return Algo Annotation list
     */
    def list(ImageInstance image, SecUser user, String bbox, Boolean notReviewedOnly,Integer force = null,def propertiesToShow = null) {
        list(image, user, GeometryUtils.createBoundingBox(bbox), notReviewedOnly,force)
    }

    /**
     * List annotation created by algorithm
     * @param image Image filter
     * @param user User Job that created annotation filter
     * @param bbox Boundary area filter (Geometry)
     * @param notReviewedOnly Flag to get only annotation that are not reviewed
     * @return Algo Annotation list
     */
    def list(ImageInstance image, SecUser user, Geometry bbox, Boolean notReviewedOnly,Integer force = null,def propertiesToShow = null) {
        SecurityACL.check(image.container(),READ)

        //we use SQL request (not hibernate) to speedup time request

        def rule = force
        if (!force) {
            rule = kmeansGeometryService.mustBeReduce(image,user,bbox)
        }
        println "RULEX=$rule"
        if(rule==kmeansGeometryService.FULL) {
//            String request = "SELECT annotation.id, annotation.wkt_location, at.term_id \n" +
//                                " FROM algo_annotation annotation LEFT OUTER JOIN algo_annotation_term at ON annotation.id = at.annotation_ident\n" +
//                                " WHERE annotation.image_id = $image.id\n" +
//                                " AND annotation.user_id= $user.id\n" +
//                                (notReviewedOnly ? " AND annotation.count_reviewed_annotations = 0\n" : "") +
//                                " AND ST_Intersects(annotation.location,ST_GeometryFromText('" + bbox.toString() + "',0)) " +
//                                " ORDER BY annotation.id "

            AnnotationListing al = new AlgoAnnotationListing(
                    columnToPrint: propertiesToShow,
                    image : image.id,
                    user:user.id,
                    notReviewedOnly:notReviewedOnly,
                    bbox:bbox
            )
            annotationListingService.executeRequest(al)

        } else if(rule==kmeansGeometryService.KMEANSFULL){
            println "mustBeReduce"
//            String request =  " select kmeans(ARRAY[ST_X(st_centroid(location)), ST_Y(st_centroid(location))], 15) OVER (), location\n" +
//                           " from algo_annotation \n" +
//                           " where image_id = ${image.id} " +
//                           " and user_id = ${user.id} " +
//                            (notReviewedOnly ? " AND algo_annotation.count_reviewed_annotations = 0\n" : " ") +
//                            "and ST_Intersects(algo_annotation.location,ST_GeometryFromText('" + bbox.toString() + "',0)) \n"
//

            AnnotationListing al = new AlgoAnnotationListing(
                    columnToPrint: propertiesToShow,
                    image : image.id,
                    user:user.id,
                    notReviewedOnly:notReviewedOnly,
                    bbox:bbox.toString(),
                    kmeans: true
            )
             kmeansGeometryService.doKeamsFullRequest(al.getAnnotationsRequest())
        } else {
            println "mustBeReduce"
//            String request =  " select kmeans(ARRAY[ST_X(st_centroid(location)), ST_Y(st_centroid(location))], 5) OVER (), location\n" +
//                           " from algo_annotation \n" +
//                           " where image_id = ${image.id} " +
//                           " and user_id = ${user.id} " +
//                            (notReviewedOnly ? " AND algo_annotation.count_reviewed_annotations = 0\n" : " ") +
//                            "and ST_Intersects(algo_annotation.location,ST_GeometryFromText('" + bbox.toString() + "',0)) \n"
//                           //" and ST_IsEmpty(st_centroid(location))=false \n"
            AnnotationListing al = new AlgoAnnotationListing(
                    columnToPrint: propertiesToShow,
                    image : image.id,
                    user:user.id,
                    notReviewedOnly:notReviewedOnly,
                    bbox:bbox.toString(),
                    kmeans: true
            )
             kmeansGeometryService.doKeamsSoftRequest(al.getAnnotationsRequest())
        }


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
    def list(Project project, List<Long> userList, List<Long> imageInstanceList, boolean noTerm, boolean multipleTerm,boolean notReviewedOnly = false,def propertiesToShow = null) {
        SecurityACL.check(project,READ)
        log.info("project/userList/noTerm/multipleTerm project=$project.id userList=$userList imageInstanceList=${imageInstanceList.size()} noTerm=$noTerm multipleTerm=$multipleTerm notReviewedOnly=$notReviewedOnly")
        if (userList.isEmpty()) {
            return []
        } else if (imageInstanceList.isEmpty()) {
            return []
        } else if (multipleTerm) {
            log.info "multipleTerm"
            //TODO: could be improve with a single SQL Request
            //get all algoannotationterm where annotation id is twice

            def annotations = []
            AnnotationListing al = new AlgoAnnotationListing(
                    columnToPrint: propertiesToShow,
                    project : project.id,
                    usersForTermAlgo: userList,
                    multipleTerm:  true,
                    images:  imageInstanceList,
                    notReviewedOnly : notReviewedOnly

            )
            annotations.addAll(annotationListingService.executeRequest(al))


            AnnotationListing al2 = new UserAnnotationListing(
                    columnToPrint: propertiesToShow,
                    project : project.id,
                    usersForTermAlgo: userList,
                    multipleTerm: true,
                    images:  imageInstanceList,
                    notReviewedOnly : notReviewedOnly

            )
            annotations.addAll(annotationListingService.executeRequest(al2))

            return annotations


//            def annotationsWithTerms = AlgoAnnotationTerm.withCriteria() {
//                eq("project", project)
//                inList("userJob.id", userList)
//                projections {
//                    groupProperty("annotationIdent")
//                    groupProperty("annotationClassName")
//                    countDistinct("term")
//                    countDistinct('created', 'createdSort')
//                }
//                order('createdSort', 'desc')
//            }
//            annotationsWithTerms.each {
//                String id = it[0]
//                String className = it[1]
//                Long termNumber = (Long) it[2]
//
//                if (termNumber > 1) {
//                    AnnotationDomain annotation = AlgoAnnotationTerm.retrieveAnnotationDomain(id, className)
//
//                    def avoid = (notReviewedOnly && annotation.hasReviewedAnnotation())
//
//                    if (!avoid && imageInstanceList.contains(annotation.image.id)) {
//                        data << annotation
//                    }
//                }
//            }
//            return data
        }
        else if (noTerm) {
            log.info "noTerm"
            def annotations = []
            AnnotationListing al = new AlgoAnnotationListing(
                    columnToPrint: propertiesToShow,
                    project : project.id,
                    users: userList,
                    noAlgoTerm: true,
                    images:  imageInstanceList,
                    notReviewedOnly : notReviewedOnly

            )
            annotations.addAll(annotationListingService.executeRequest(al))


//            AnnotationListing al2 = new UserAnnotationListing(
//                    columnToPrint: propertiesToShow,
//                    project : project.id,
//                    usersForTermAlgo: userList,
//                    noTerm: true,
//                    images:  imageInstanceList,
//                    notReviewedOnly : notReviewedOnly
//
//            )
//            annotations.add(annotationListingService.executeRequest(al2))



//            //TODO: could be improve with a single SQL Request
//            def annotationsWithTerms = AlgoAnnotationTerm.withCriteria() {
//                eq("project", project)
//                inList("userJob.id", userList)
//                projections {
//                    groupProperty("annotationIdent")
//                }
//            }
//
//            //annotationsWithTerms = annotationsWithTerms.collect{it[0]}
//
//            //inList crash is argument is an empty list so we have to use if/else at this time
//            def annotations = []
//
//            int maxReviewed = (notReviewedOnly? 0 : Integer.MAX_VALUE)
//
//            if (annotationsWithTerms.size() == 0) {
//                annotations.addAll(UserAnnotation.createCriteria().list {
//                    eq("project", project)
//                    inList("image.id", imageInstanceList)
//                    inList("user.id", userList)
//                    lt('countReviewedAnnotations',maxReviewed)
//                    order 'created', 'desc'
//                })
//                annotations.addAll(AlgoAnnotation.createCriteria().list {
//                    eq("project", project)
//                    inList("image.id", imageInstanceList)
//                    inList("user.id", userList)
//                    lt('countReviewedAnnotations',maxReviewed)
//                    order 'created', 'desc'
//                })
//            } else {
//                annotations.addAll(UserAnnotation.createCriteria().list {
//                    eq("project", project)
//                    inList("image.id", imageInstanceList)
//                    inList("user.id", userList)
//                    lt('countReviewedAnnotations',maxReviewed)
//                    not {
//                        inList("id", annotationsWithTerms)
//                    }
//                    order 'created', 'desc'
//                })
//                annotations.addAll(AlgoAnnotation.createCriteria().list {
//                    eq("project", project)
//                    inList("image.id", imageInstanceList)
//                    inList("user.id", userList)
//                    lt('countReviewedAnnotations',maxReviewed)
//                    not {
//                        inList("id", annotationsWithTerms)
//                    }
//                    order 'created', 'desc'
//                })
//            }

            return annotations
        } else {
            log.info "findAllByProjectAndUserInList=" + project + " users=" + userList
//            int maxReviewed = (notReviewedOnly? 0 : Integer.MAX_VALUE)
//
//            String request = "" +
//                    "SELECT a.id as id, count_reviewed_annotations as countReviewedAnnotations, at.rate as rate, at.term_id as term, at.expected_term_id as expterm, a.image_id as image, true as algo, a.created as created, a.project_id as project, at.user_job_id as user\n" +
//                    "FROM algo_annotation a, algo_annotation_term at\n" +
//                    "WHERE a.id = at.annotation_ident\n" +
//                    "AND a.project_id = ${project.id}\n" +
//                    "AND a.count_reviewed_annotations = 0\n" +
//                    "AND a.user_id IN (${userList.join(",")})\n" +
//                    "AND a.image_id IN (${imageInstanceList.join(",")})\n" +
//                    "ORDER BY id desc"
//
//             println request
//            return selecAlgoAnnotationLight(request)

            AnnotationListing al = new AlgoAnnotationListing(
                    columnToPrint: propertiesToShow,
                    project : project.id,
                    users: userList,
                    images:  imageInstanceList,
                    notReviewedOnly : notReviewedOnly

            )
            annotationListingService.executeRequest(al)


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
    def listForUserJob(Project project, Term term, List<Long> userList, List<Long> imageInstanceList,Boolean notReviewedOnly = false,def propertiesToShow = null) {
        SecurityACL.check(project,READ)
        if (userList.isEmpty()) {
            return []
        } else if (imageInstanceList.isEmpty()) {
            return []
        } else {
            //Get all images
            println "listForUserJob"

            def annotations = []
            AnnotationListing al = new AlgoAnnotationListing(
                    columnToPrint: propertiesToShow,
                    project : project.id,
                    suggestedTerm : term.id,
                    usersForTermAlgo: userList,
                    images : imageInstanceList,
                    notReviewedOnly : notReviewedOnly
            )
            annotations.addAll(annotationListingService.executeRequest(al))


            AnnotationListing al2 = new UserAnnotationListing(
                    columnToPrint: propertiesToShow,
                    project : project.id,
                    suggestedTerm : term.id,
                    usersForTermAlgo: userList,
                    images : imageInstanceList,
                    notReviewedOnly : notReviewedOnly
            )
            annotations.addAll(annotationListingService.executeRequest(al2))

            return annotations



//            String request = "" +
//                    "SELECT a.id as id, count_reviewed_annotations as countReviewedAnnotations, at.rate as rate, at.term_id as term, at.expected_term_id as expterm, a.image_id as image, true as algo, a.created as created, a.project_id as project, at.user_job_id as user \n" +
//                    "FROM algo_annotation a, algo_annotation_term at \n" +
//                    "WHERE at.project_id = ${project.id} \n" +
//                    "AND at.term_id = ${term.id} \n" +
//                    "AND at.user_job_id IN (${userList.join(',')}) \n" +
//                    (imageInstanceList.size() != project.countImages? "AND a.image_id IN (${imageInstanceList.join(',')})\n " :" ") +
//                    "AND at.annotation_ident = a.id \n" +
//                    (notReviewedOnly? "AND a.count_reviewed_annotations=0\n" : "" ) +
//                    "UNION \n" +
//                    "SELECT a.id as id, count_reviewed_annotations as countReviewedAnnotations, at.rate as rate, at.term_id as term, at.expected_term_id as expterm, a.image_id as image , false as algo, a.created as created, a.project_id as project, at.user_job_id as user \n" +
//                    "FROM user_annotation a, algo_annotation_term at \n" +
//                    "WHERE at.project_id = ${project.id} \n" +
//                    "AND at.term_id = ${term.id} \n" +
//                    "AND at.user_job_id IN (${userList.join(',')}) \n" +
//                    (imageInstanceList.size() != project.countImages? "AND a.image_id IN (${imageInstanceList.join(',')}) \n" :" ") +
//                    "AND at.annotation_ident = a.id \n" +
//                    (notReviewedOnly? "AND a.count_reviewed_annotations=0\n" : "" ) +
//                    "ORDER BY rate desc \n"
//
////             println request
//            return selecAlgoAnnotationLight(request)
        }
    }

    def list(ImageInstance image, String geometry, SecUser user,  List<Long> terms, AnnotationDomain annotation = null,def propertiesToShow = null) {
        SecurityACL.check(image.container(),READ)

        def annotations = []
        AnnotationListing al = new AlgoAnnotationListing(
                columnToPrint: propertiesToShow,
                image : image.id,
                user : user.id,
                suggestedTerms : terms,
                excludedAnnotation : annotation?.id,
                bbox: geometry
        )
        annotations.addAll(annotationListingService.executeRequest(al))
        return annotations

//         String request = "SELECT a.id as id, count_reviewed_annotations as countReviewedAnnotations, at.rate as rate, at.term_id as term, at.expected_term_id as expterm, a.image_id as image, true as algo, a.created as created, a.project_id as project, at.user_job_id as user,ST_area(location) as area, ST_perimeter(location) as perimeter, ST_X(ST_centroid(location)) as x,ST_Y(ST_centroid(location)) as y, ai.original_filename as originalfilename \n" +
//                 "FROM algo_annotation a, algo_annotation_term at, abstract_image ai, image_instance ii \n" +
//                 "WHERE a.id = at.annotation_ident \n" +
//                 "AND a.image_id = ${image.id} \n" +
//                 "AND a.user_id = ${user.id} \n" +
//                 "AND a.image_id = ii.id  \n" +
//                 "AND ii.base_image_id = ai.id  \n" +
//                 "AND at.term_id IN (${terms.join(',')})\n" +
//                 (annotation? "AND a.id <> ${annotation.id} \n" :"")+
//                 "AND ST_Intersects(a.location,ST_GeometryFromText('${geometry}',0));"
//
//        println request
//        selecAlgoAnnotationLight(request)
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
        SecurityACL.checkIsCreator(annotation,currentUser)
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
        SecurityACL.checkIsCreator(domain,currentUser)
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

    def deleteDependentProperty(AlgoAnnotation aa, Transaction transaction, Task task = null) {
        Property.findAllByDomainIdent(aa.id).each {
            propertyService.delete(it,transaction,null,false)
        }

    }


}
