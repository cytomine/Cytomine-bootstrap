package be.cytomine.ontology

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.SecurityACL
import be.cytomine.api.UrlApi
import be.cytomine.command.*
import be.cytomine.image.ImageInstance
import be.cytomine.image.server.RetrievalServer
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.social.SharedAnnotation
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.io.WKTWriter
import groovy.sql.Sql
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions

import static org.springframework.security.acls.domain.BasePermission.READ

class UserAnnotationService extends ModelService {

    static transactional = true
    def cytomineService
    def transactionService
    def annotationTermService
    def retrievalService
    def algoAnnotationTermService
    def modelService
    def simplifyGeometryService
    def dataSource
    def reviewedAnnotationService
    def annotationPropertyService
    def kmeansGeometryService


    def currentDomain() {
        return UserAnnotation
    }

    UserAnnotation get(def id) {
        def annotation = UserAnnotation.get(id)
        if (annotation) {
            SecurityACL.check(annotation.container(),READ)
        }
        annotation
    }

    UserAnnotation read(def id) {
        def annotation = UserAnnotation.read(id)
        if (annotation) {
            SecurityACL.check(annotation.container(),READ)
        }
        annotation
    }

    def list(Project project) {
        SecurityACL.check(project.container(),READ)
        UserAnnotation.findAllByProject(project)
    }

    /**
     * List annotation where a user from 'userList' has added term 'realTerm' and for which a specific job has predicted 'suggestedTerm'
     * @param project Annotation project
     * @param userList Annotation user list filter
     * @param realTerm Annotation term (add by user)
     * @param suggestedTerm Annotation predicted term (from job)
     * @param job Job that make prediction
     * @return
     */
    def list(Project project, List<Long> userList, Term realTerm, Term suggestedTerm, Job job) {
        SecurityACL.check(project.container(),READ)
        log.info "list with suggestedTerm"
        if (userList.isEmpty()) {
            return []
        }
        //Get last userjob
        SecUser user = UserJob.findByJob(job)

        //Get all annotation from this project with this term
        def annotationFromProjectWithTerm = UserAnnotation.executeQuery(
                "SELECT ua " +
                "FROM UserAnnotation ua, AnnotationTerm at " +
                "WHERE ua.id = at.userAnnotation.id " +
                "AND ua.project = :project " +
                "AND at.term = :realTerm " +
                "AND at.user.id IN (:userList)", [userList: userList, realTerm: realTerm, project: project])

        def algoAnnotationsTerm = AnnotationTerm.executeQuery("SELECT ua " +
                "FROM AlgoAnnotationTerm aat, UserAnnotation ua " +
                "WHERE aat.userJob = :user " +
                "AND aat.term = :suggestedTerm " +
                "AND aat.annotationIdent = ua.id " +
                "AND aat.annotationIdent IN (:annotations)", [user: user, suggestedTerm: suggestedTerm, annotations: annotationFromProjectWithTerm.collect {it.id}.unique()])

        return algoAnnotationsTerm
    }

    /**
     * List annotation created by user
     * Rem1: We use SQL request (instead of using GORM/hibernate), this improve a lot perf.
     * @param project Project annotation filter
     * @param userList Annotation user filter
     * @param imageInstanceList Image filter
     * @param noTerm Only get annotation with no term
     * @param multipleTerm Only get annotation with multiple (diff) term
     * @return Annotation listing (light)
     */
    def listLight(Project project, List<Long> userList, List<Long> imageInstanceList, boolean noTerm, boolean multipleTerm) {
        SecurityACL.check(project.container(),READ)
        if (!userList.isEmpty() && userList.getAt(0) instanceof UserJob) {
            throw new IllegalArgumentException("Method not supported for this type of data!!!")
        } else {
            String request
            if (multipleTerm)
                request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerms,at2.user_id as userTerm,a.wkt_location as location  \n" +
                        " FROM user_annotation a, annotation_term at2, annotation_term at3\n" +
                        " WHERE a.project_id = " + project.id + "\n" +
                        " AND a.id = at2.user_annotation_id\n" +
                        " AND a.id = at3.user_annotation_id\n" +
                        " AND at2.id <> at3.id \n" +
                        " AND at2.term_id <> at3.term_id \n" +
                        " AND at2.user_id IN (" + userList.join(",") + ") \n" +
                        (imageInstanceList.size() == project.countImageInstance() ? "" : "AND a.image_id IN(" + imageInstanceList.join(",") + ") \n") +
                        " ORDER BY id desc, term"
            else if (noTerm)
                request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,null as term, null as annotationTerms,null as userTerm,a.wkt_location as location  \n" +
                        " FROM user_annotation a LEFT JOIN (SELECT * from annotation_term x where x.user_id IN (" + userList.join(",") + ")) at ON a.id = at.user_annotation_id \n" +
                        " WHERE a.project_id = " + project.id + "\n" +
                        " AND at.id IS NULL\n" +
                        " AND a.user_id IN (" + userList.join(",") + ") \n" +
                        (imageInstanceList.size() == project.countImageInstance() ? "" : "AND a.image_id IN(" + imageInstanceList.join(",") + ") \n") +
                        " ORDER BY id desc, term"
            else
                request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerms,at2.user_id as userTerm,a.wkt_location as location  \n" +
                        " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id \n" +
                        " WHERE a.project_id = " + project.id + "\n" +
                        " AND a.user_id IN (" + userList.join(",") + ") \n" +
                        (imageInstanceList.size() == project.countImageInstance() ? "" : "AND a.image_id IN(" + imageInstanceList.join(",") + ") \n") +
                        " ORDER BY id desc, term"

            selectUserAnnotationFull(request)
        }
    }

    /**
     * List annotation created by user
     * Rem1: We use SQL request (instead of using GORM/hibernate), this improve a lot perf.
     * @param image Image list
     * @param user Annotation user filter
     * @return Annotation listing (light)
     */
    def listLight(ImageInstance image, SecUser user) {
        SecurityACL.check(image.container(),READ)
//
//        def rule = kmeansGeometryService.mustBeReduce(image,user)
//
//        if(rule==kmeansGeometryService.FULL) {
            String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerms,at2.user_id as userTerm,a.wkt_location as location  \n" +
                    " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id\n" +
                    " WHERE a.image_id = " + image.id + "\n" +
                    " AND a.user_id = " + user.id + "\n" +
                    " ORDER BY id desc, term"
            return selectUserAnnotationFull(request)
//        } else if(rule==kmeansGeometryService.KMEANSFULL){
//            println "mustBeReduce"
//            String request =  "select kmeans(ARRAY[ST_X(st_centroid(location)), ST_Y(st_centroid(location))], 5) OVER (), location\n " +
//                              "from user_annotation \n " +
//                              "where image_id = ${image.id} " +
//                              "and user_id = ${user.id} " +
//                              "and ST_IsEmpty(st_centroid(location))=false "
//             kmeansGeometryService.doKeamsFullRequest(request)
//        } else {
//            println "mustBeReduce"
//            String request =  "select kmeans(ARRAY[ST_X(st_centroid(location)), ST_Y(st_centroid(location))], 5) OVER (), location\n " +
//                              "from user_annotation \n " +
//                              "where image_id = ${image.id} and user_id = ${user.id} and ST_IsEmpty(st_centroid(location))=false \n "
//             kmeansGeometryService.doKeamsSoftRequest(request)
//        }
    }

    /**
     * List annotations according to some filters parameters (rem : use list light if you only need the response, not
     * the objects)
     * @param image the image instance
     * @param bbox Geometry restricted Area
     * @param termsIDS filter terms ids
     * @param userIDS filter user ids
     * @return Annotation listing
     */
    def list(ImageInstance image, Geometry bbox, List<Long> termsIDS, List<Long> userIDS) {
        //:to do use listlight and parse WKT instead ?
        Collection<UserAnnotation> annotations_in_roi = []

        annotations_in_roi = UserAnnotation.createCriteria()
                .add(Restrictions.in("user.id", userIDS))
                .add(Restrictions.eq("image.id", image.id))
                .add(SpatialRestrictions.intersects("location",bbox))
                .list()

        Collection<UserAnnotation> annotations = []

        if (!annotations_in_roi.isEmpty()) {
            annotations = (Collection<UserAnnotation>) AnnotationTerm.createCriteria().list {
                inList("term.id", termsIDS)
                join("userAnnotation")
                createAlias("userAnnotation", "a")
                projections {
                    inList("a.id", annotations_in_roi.collect{it.id})
                    groupProperty("userAnnotation")
                }
            }
        }

        return annotations
    }

    /**
     * List annotation created by user
     * Rem1: We use SQL request (instead of using GORM/hibernate), this improve a lot perf.
     * @param image Image list
     * @param user Annotation user filter
     * @param bbox Geometry restricted Area
     * @param notReviewedOnly Don't get annotation that have been reviewed
     * @return Annotation listing (light)
     */
    def listLight(ImageInstance image, SecUser user, Geometry bbox, Boolean notReviewedOnly) {
        SecurityACL.check(image.container(),READ)
        println "listLight"

        SecurityACL.check(image.container(),READ)

        def rule = kmeansGeometryService.mustBeReduce(image,user,bbox)
        if(rule==kmeansGeometryService.FULL) {
            String request = "SELECT DISTINCT annotation.id, annotation.wkt_location, at.term_id \n" +
                                " FROM user_annotation annotation LEFT OUTER JOIN annotation_term at ON annotation.id = at.user_annotation_id\n" +
                                " WHERE annotation.image_id = $image.id\n" +
                                " AND annotation.user_id= $user.id\n" +
                                (notReviewedOnly ? " AND annotation.count_reviewed_annotations = 0\n" : "") +
                                " AND ST_Intersects(annotation.location,ST_GeometryFromText('" + bbox.toString() + "',0)) \n" +
                                " ORDER BY annotation.id desc"
            return selectUserAnnotationLight(request)
        } else if(rule==kmeansGeometryService.KMEANSFULL){
            String request =  "select kmeans(ARRAY[ST_X(st_centroid(location)), ST_Y(st_centroid(location))], 5) OVER (), location\n " +
                              "from user_annotation \n " +
                              "where image_id = ${image.id} " +
                              "and user_id = ${user.id} " +
                              "and ST_IsEmpty(st_centroid(location))=false " +
                              "and ST_Intersects(user_annotation.location,ST_GeometryFromText('" + bbox.toString() + "',0)) \n"
             kmeansGeometryService.doKeamsFullRequest(request)
        } else {
            String request =  "select kmeans(ARRAY[ST_X(st_centroid(location)), ST_Y(st_centroid(location))], 5) OVER (), location\n " +
                              "from user_annotation \n " +
                              "where image_id = ${image.id} and user_id = ${user.id} and ST_IsEmpty(st_centroid(location))=false \n " +
                              "and ST_Intersects(user_annotation.location,ST_GeometryFromText('" + bbox.toString() + "',0)) \n"
             kmeansGeometryService.doKeamsSoftRequest(request)
        }


    }

    /**
     * List annotation created by user
     * Rem1: We use SQL request (instead of using GORM/hibernate), this improve a lot perf.
     * @param project Project annotation filter
     * @param term Term filter
     * @param userList User filter
     * @param imageInstanceList Image filter
     * @return Annotation listing (light)
     */
    def list(Project project, Term term, List<Long> userList, List<Long> imageInstanceList) {
        SecurityACL.check(project.container(),READ)
        if (!userList.isEmpty() && userList.getAt(0) instanceof UserJob) {
            listForUserJob(project, term, userList, imageInstanceList)
        } else {
            boolean allImages = ImageInstance.countByProject(project)==imageInstanceList.size()
            String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerms,at2.user_id as userTerm,a.wkt_location as location  \n" +
                    " FROM user_annotation a, annotation_term at,annotation_term at2,annotation_term at3\n" +
                    " WHERE a.id = at.user_annotation_id \n" +
                    " AND a.project_id = " + project.id + "\n" +
                    " AND at3.term_id = " + term.id + "\n" +
                    " AND a.id = at2.user_annotation_id\n" +
                    " AND a.id = at3.user_annotation_id\n" +
                    " AND at.user_id IN (" + userList.collect {it}.join(",") + ") \n" +
                    (allImages? " AND a.image_id IN (" + imageInstanceList.collect {it}.join(",") + ") \n" : "") +
                    " ORDER BY id desc, term"
            selectUserAnnotationFull(request)
        }
    }

//
//    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
//    def listLight(Project project) {
//        String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerms,at2.user_id as userTerm,a.wkt_location as location  \n" +
//                " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id\n" +
//                " WHERE a.project_id = " + project.id + "\n"+
//                " ORDER BY id desc, term"
//        selectUserAnnotationFull(request)
//    }

    /**
     * List all annotation with a very light strcuture: id, project and crop url
     * Use for retrieval server (suggest term)
     */
    def listLightForRetrieval() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        String request = "SELECT a.id as id, a.project_id as project\n" +
                " FROM user_annotation a\n" +
                " WHERE GeometryType(a.location) != 'POINT'\n"
        " ORDER BY id desc"
        selectUserAnnotationLightForRetrieval(request)
    }

    /**
     * List annotation created by user
     * @param image Image filter
     */
    def listLight(ImageInstance image) {
        SecurityACL.check(image.project,READ)
        String request = "SELECT a.id as id, a.image_id as image, a.geometry_compression as geometryCompression, a.project_id as project, a.user_id as user,a.count_comments as nbComments,extract(epoch from a.created)*1000 as created, extract(epoch from a.updated)*1000 as updated, a.count_reviewed_annotations as countReviewedAnnotations,at2.term_id as term, at2.id as annotationTerms,at2.user_id as userTerm,a.wkt_location as location  \n" +
                " FROM user_annotation a LEFT OUTER JOIN annotation_term at2 ON a.id = at2.user_annotation_id\n" +
                " WHERE a.image_id = " + image.id + "\n" +
                " ORDER BY id desc, term"
        selectUserAnnotationFull(request)
    }


    private def listForUserJob(Project project, Term term, List<Long> userList, List<Long> imageInstanceList) {
        //TODO: must be improved!!!!!!!!!!
        if (userList.isEmpty()) return []
        if (imageInstanceList.isEmpty()) return []
        if (imageInstanceList.size() == project.countImages) {
            def criteria = AlgoAnnotationTerm.withCriteria() {
                createAlias("userAnnotation", "a")
                eq('project', project)
                eq('term', term)
                inList('userJob.id', userList)
                projections {
                    groupProperty("userAnnotation")
                    groupProperty("rate")
                    groupProperty("term.id")
                    groupProperty("expectedTerm.id")
                }
                order 'rate', 'desc'
            }
            return criteria
        } else {
            def criteria = AlgoAnnotationTerm.withCriteria() {
                createAlias("userAnnotation", "a")
                eq('project', project)
                eq('term', term)
                inList('userJob.id', userList)
                inList("a.image.id", imageInstanceList)
                projections {
                    groupProperty("userAnnotation")
                    groupProperty("rate")
                    groupProperty("term.id")
                    groupProperty("expectedTerm.id")
                }
                order 'rate', 'desc'
            }
            return criteria
        }
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecurityACL.check(json.project, Project,READ)
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
            def result = executeCommand(new AddCommand(user: currentUser, transaction: transaction),null,json)
            def annotationID = result?.data?.annotation?.id
            log.info "userAnnotation=" + annotationID + " json.term=" + json.term
            //Add annotation-term if term
            if (annotationID) {
                def term = json.term;
                if (term) {
                    term.each { idTerm ->
                        annotationTermService.addAnnotationTerm(annotationID, idTerm, null, currentUser.id, currentUser, transaction)
                    }
                }
            }

            //add annotation on the retrieval
            if (annotationID && UserAnnotation.read(annotationID).location.getNumPoints() >= 3) {
                if (!currentUser.algo()) {
                    try {
                        if (annotationID) indexRetrievalAnnotation(annotationID)
                    } catch (CytomineException ex) {
                        log.error "CytomineException index in retrieval:" + ex.toString()
                    } catch (Exception e) {
                        log.error "Exception index in retrieval:" + e.toString()
                    }
                }
            }

            return result
        }
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(UserAnnotation annotation, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkIsSameUser(annotation.user,currentUser)
        //simplify annotation
        try {
            def data = simplifyGeometryService.simplifyPolygon(json.location, annotation?.geometryCompression)
            json.location = new WKTWriter().write(data.geometry)
        } catch (Exception e) {
            log.error("Cannot simplify:" + e)
        }

        def result = executeCommand(new EditCommand(user: currentUser),annotation,jsonNewData)

        if (result.success) {
            Long id = result.userannotation.id
            try {
                updateRetrievalAnnotation(id)
            } catch (Exception e) {
                log.error "Cannot update in retrieval:" + e.toString()
            }
        }
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
    def delete(UserAnnotation domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkIsSameUser(domain.user,currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    /**
     * Add annotation to retrieval server for similar annotation listing and term suggestion
     */
    private indexRetrievalAnnotation(Long id) {
        //index in retrieval (asynchronous)
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "userAnnotation.id=" + id + " stevben-server=" + retrieval
        if (id && retrieval) {
            log.info "index userAnnotation " + id + " on  " + retrieval.url
            retrievalService.indexAnnotationAsynchronous(UserAnnotation.read(id), RetrievalServer.findByDescription("retrieval"))

        }
    }

    /**
     * Add annotation from retrieval server
     */
    private deleteRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "userAnnotation.id=" + id + " retrieval-server=" + retrieval
        if (id && retrieval) {
            log.info "delete userAnnotation " + id + " on  " + retrieval.url
            retrievalService.deleteAnnotationAsynchronous(id)
        }
    }

    /**
     *  Update annotation in retrieval server
     */
    private updateRetrievalAnnotation(Long id) {
        RetrievalServer retrieval = RetrievalServer.findByDescription("retrieval")
        log.info "userAnnotation.id=" + id + " retrieval-server=" + retrieval
        if (id && retrieval) {
            log.info "update userAnnotation " + id + " on  " + retrieval.url
            retrievalService.updateAnnotationAsynchronous(id)
        }
    }

    def afterAdd(def domain, def response) {
        response.data['annotation'] = response.data.userannotation
        response.data.remove('userannotation')
    }

    def afterDelete(def domain, def response) {
        response.data['annotation'] = response.data.userannotation
        response.data.remove('userannotation')
    }

    def afterUpdate(def domain, def response) {
        response.data['annotation'] = response.data.userannotation
        response.data.remove('userannotation')
    }

    def getStringParamsI18n(def domain) {
        return [domain.user.toString(), domain.image?.baseImage?.filename]
    }



    /**
     * Execute request and format result into a list of map
     */
    private def selectUserAnnotationFull(String request) {
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
                        'class': 'be.cytomine.ontology.UserAnnotation',
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
                        smallCropURL: UrlApi.getUserAnnotationCropWithAnnotationIdWithMaxWithOrHeight(it.id, 256),
                        url: UrlApi.getUserAnnotationCropWithAnnotationId(it.id),
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
     * Execute request and format result into a list of map
     */
    private def selectUserAnnotationLight(String request) {
        def data = []
        long lastAnnotationId = -1
        new Sql(dataSource).eachRow(request) {

            long idAnnotation = it[0]
            String location = it[1]
            def idTerm = it[2]

            if (idAnnotation != lastAnnotationId) {
                data << [id: idAnnotation, location: location, term: idTerm ? [idTerm] : []]
            } else {
                if (idTerm)
                    data.last().term.add(idTerm)
            }
            lastAnnotationId = idAnnotation
        }
        data
    }

    /**
     * Execute request and format result into a list of map
     */
    private def selectUserAnnotationLightForRetrieval(String request) {
        def data = []
        new Sql(dataSource).eachRow(request) {

            long idAnnotation = it[0]
            long idContainer = it[1]
            def url = UrlApi.getAnnotationMinCropWithAnnotationId(idAnnotation)
            data << [id: idAnnotation, container: idContainer, url: url]
        }
        data
    }

    def deleteDependentAlgoAnnotationTerm(UserAnnotation ua, Transaction transaction, Task task = null) {
        AlgoAnnotationTerm.findAllByAnnotationIdent(ua.id).each {
            algoAnnotationTermService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentAnnotationTerm(UserAnnotation ua, Transaction transaction, Task task = null) {
        AnnotationTerm.findAllByUserAnnotation(ua).each {
            annotationTermService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentReviewedAnnotation(UserAnnotation ua, Transaction transaction, Task task = null) {
        ReviewedAnnotation.findAllByParentIdent(ua.id).each {
            reviewedAnnotationService.delete(it,transaction,null,false)
        }
     }

    def deleteDependentSharedAnnotation(UserAnnotation ua, Transaction transaction, Task task = null) {
        //TODO: we should implement a full service for sharedannotation and delete them if annotation is deleted
        if(SharedAnnotation.findByUserAnnotation(ua)) {
            throw new ConstraintException("There are some comments on this annotation. Cannot delete it!")
        }
    }

    def deleteDependentAnnotationProperty(UserAnnotation ua, Transaction transaction, Task task = null) {
        AnnotationProperty.findAllByAnnotationIdent(ua.id).each {
            annotationPropertyService.delete(it,transaction,null,false)
        }

    }

}
