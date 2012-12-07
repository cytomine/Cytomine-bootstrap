package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import groovy.sql.Sql
import org.codehaus.groovy.grails.web.json.JSONArray

import java.text.SimpleDateFormat
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.security.UserJob
import be.cytomine.command.Task
import be.cytomine.processing.JobData
import org.hibernate.SessionFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import be.cytomine.utils.GeometryUtils
import com.vividsolutions.jts.io.WKTReader

class RestReviewedAnnotationController extends RestController {

    def exportService
    def algoAnnotationService
    def domainService
    def termService
    def imageInstanceService
    def userService
    def projectService
    def cytomineService
    def dataSource
    def reviewedAnnotationService
    def taskService

    //list all
    def list = {
        def annotations = []
        def projects = projectService.list()
        projects.each {
            annotations.addAll(reviewedAnnotationService.list(it))
        }
        responseSuccess(annotations)
    }

    //list all by image
    def listByImage = {
        ImageInstance image = imageInstanceService.read(params.long('idImage'))
        if (image && params.bbox) {
//            def list = reviewedAnnotationService.list(image,(String) params.bbox)
            Geometry boundingbox = GeometryUtils.createBoundingBox((String)params.bbox)

            println "boundingbox.toString()=" + boundingbox.toString()

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
             //ST_ExteriorRing(
            //TODO:: get zoom info from UI client, display with scaling only with hight zoom (< annotations)
            boolean zoomToLow = true
            String request
            if(zoomToLow) {
                request = "SELECT reviewed.id, reviewed.wkt_location, (SELECT SUM(ST_CoveredBy(ga.location,gb.location )::integer) FROM reviewed_annotation ga, reviewed_annotation gb WHERE ga.id=reviewed.id AND ga.id<>gb.id AND ga.image_id=gb.image_id AND ST_Intersects(gb.location,GeometryFromText('" + boundingbox.toString() + "',0))) as numberOfCoveringAnnotation\n" +
                      " FROM reviewed_annotation reviewed\n" +
                      " WHERE reviewed.image_id = $image.id\n" +
                      " AND ST_Intersects(reviewed.location,GeometryFromText('" + boundingbox.toString() + "',0))\n" +
                      " ORDER BY numberOfCoveringAnnotation asc, id asc"
            } else {
                request = "SELECT reviewed.id, reviewed.wkt_location, (SELECT SUM(ST_CoveredBy(ga.location,ST_Translate(ST_Scale(gb.location, $xfactor, $yfactor), ST_X(ST_Centroid(gb.location))*(1 - $xfactor), ST_Y(ST_Centroid(gb.location))*(1 - $yfactor) ))::integer) FROM reviewed_annotation ga, reviewed_annotation gb WHERE ga.id=reviewed.id AND ga.id<>gb.id AND ga.image_id=gb.image_id AND ST_Intersects(gb.location,GeometryFromText('" + boundingbox.toString() + "',0))) as numberOfCoveringAnnotation\n" +
                       " FROM reviewed_annotation reviewed\n" +
                       " WHERE reviewed.image_id = $image.id\n" +
                       " AND ST_Intersects(reviewed.location,GeometryFromText('" + boundingbox.toString() + "',0))\n" +
                       " ORDER BY numberOfCoveringAnnotation asc, id asc"
            }

            println "REQUEST=" + request
            def sql = new Sql(dataSource)

            def data = []
            sql.eachRow(request) {
                data << [id: it[0], location: it[1], term: []]
            }

            responseSuccess(data)
        }
        else if(image) responseSuccess(reviewedAnnotationService.list(image))
        else responseNotFound("Image", params.idImage)
    }

    //list all by image
    def listByImageAndTerm = {
        log.info "listByImageAndTerm"
        long start = System.currentTimeMillis()
        ImageInstance image = imageInstanceService.read(params.long('idImage'))
        Term term = termService.read(params.long('idTerm'))
        if (image && term) {
            def list = reviewedAnnotationService.list(image,term)
            responseSuccess(list)
        }
        else if(image) responseSuccess(reviewedAnnotationService.list(image))
        else responseNotFound("Image", params.idImage)
    }

//
//    //listByImageAndUser
//    def listByImageAndUser = {
//        def image = imageInstanceService.read(params.long('idImage'))
//        def user = userService.read(params.idUser)
//        if (image && user && params.bbox) {
//            responseSuccess(reviewedAnnotationService.list(image, user, (String) params.bbox))
//        }
//        else if (image && user) responseSuccess(reviewedAnnotationService.list(image, user))
//        else if (!user) responseNotFound("User", params.idUser)
//        else if (!image) responseNotFound("Image", params.idImage)
//    }


    //list all by project
    def listByProject = {
        log.info "listByProject"
        Project project = projectService.read(params.long('idProject'), new Project())
        if (project) responseSuccess(reviewedAnnotationService.list(project))
        else responseNotFound("Project", params.idProject)
    }

    //list all by project, term and user
    def listByProjectImageTermAndUser = {
        log.info "listByProjectImageTermAndUser"
        if ((params.users == null || params.users == "null") && (params.images == null || params.images == "null") && (params.terms == null || params.terms == "null"))
            forward(action: "listByProject")
        Project project = projectService.read(params.long('idProject'), new Project())
        if (project) {
            Integer offset = params.offset != null ? params.getInt('offset') : 0
            Integer max = params.max != null ? params.getInt('max') : Integer.MAX_VALUE
            Collection<SecUser> userList = []
            if (params.users != null && params.users != "null" && params.users != "") {
                userList = userService.list(project, params.users.split("_").collect { Long.parseLong(it)})
            }
            else {
                userList = userService.list(project)
            }
            Collection<ImageInstance> imageInstanceList = []
            if (params.images != null && params.images != "null" && params.images != "") {
                imageInstanceList = imageInstanceService.list(project, params.images.split("_").collect { Long.parseLong(it)})
            } else {
                imageInstanceList = imageInstanceService.list(project)
            }
            Collection<Term> termList = []
            if (params.terms != null && params.terms != "null" && params.terms != "") {
                termList = termService.list(project, params.terms.split("_").collect { Long.parseLong(it)})
            } else {
                termList = termService.list(project)
                log.info "termList=$termList"
            }

            if (userList.isEmpty()) {
                responseNotFound("User", params.users)
            } else if (imageInstanceList.isEmpty()) {
                responseNotFound("ImageInstance", params.images)
            } else if (termList.isEmpty()) {
                responseNotFound("Term", params.terms)
            } else {
                def list = reviewedAnnotationService.list(project, userList, imageInstanceList, termList)
                if (params.offset != null) responseSuccess([size: list.size(), collection: substract(list, offset, max)])
                else responseSuccess(list)
            }
        }
        else responseNotFound("Project", params.idProject)
    }

    //show
    def show = {
        log.info "show annotation = " + params.long('id')
        ReviewedAnnotation annotation = reviewedAnnotationService.read(params.long('id'))
        if (annotation) {
            reviewedAnnotationService.checkAuthorization(annotation.project)
            responseSuccess(annotation)
        }
        else responseNotFound("ReviewedAnnotation", params.id)
    }

    //add
    def add = {
        add(reviewedAnnotationService, request.JSON)
    }

    //update
    def update = {
        update(reviewedAnnotationService, request.JSON)
    }

    //delete
    def delete = {
        delete(reviewedAnnotationService, JSON.parse("{id : $params.id}"))
    }

    //listByImageAndUser
    def listByImageAndUser = {
        long start = System.currentTimeMillis()
        println "START:"+ start
        def image = imageInstanceService.read(params.long('idImage'))
        def user = userService.read(params.idUser)
        if (image && user && params.bbox) {
            responseSuccess(reviewedAnnotationService.list(image, user, (String) params.bbox))l
        }
        else if (image && user) responseSuccess(reviewedAnnotationService.list(image, user))
        else if (!user) responseNotFound("User", params.idUser)
        else if (!image) responseNotFound("Image", params.idImage)
    }

    private def substract(List collection, Integer offset, Integer max) {
        //TODO:: extract
        if (offset >= collection.size()) return []

        def maxForCollection = Math.min(collection.size() - offset, max)
        log.info "collection=${collection.size()} offset=$offset max=$max compute=${collection.size() - offset} maxForCollection=$maxForCollection"
        return collection.subList(offset, offset + maxForCollection)
    }

    def startImageInstanceReview = {
        try {
            def image = imageInstanceService.read(params.long("id"))
            def response = [:]

            if (image) {
                image.reviewStart = new Date()
                image.reviewUser = cytomineService.currentUser
                if (!image.validate()) throw new WrongArgumentException("Cannot review (validate) image instance:" + image.errors)
                if (image.save(flush: true) == null) throw new WrongArgumentException("Cannot review (add) image instance:" + image.errors)

                response.message = image.reviewUser.username + " start reviewing on " + image.baseImage.filename
                response.imageinstance = image
                responseSuccess(response)
            } else responseNotFound("Image", params.idImage)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def stopImageInstanceReview = {
        try {
            def image = imageInstanceService.read(params.long("id"))
            def response = [:]

            boolean isCancel = params.getBoolean("cancel")
            if (image) {
                if (image.reviewStart == null || image.reviewUser == null) throw new WrongArgumentException("Image is not in review mode: image.reviewStart=${image.reviewStart} and image.reviewUser=${image.reviewUser}")
                if (cytomineService.currentUser != image.reviewUser) throw new WrongArgumentException("Review can only be validate or stop by "+image.reviewUser.username)

                if(isCancel) {
                    if(!image.reviewStop) {
                        //cancel reviewing
                        image.reviewStart = null
                        image.reviewUser = null
                    } else {
                        //cancel finish reviewing (validate)
                        image.reviewStop = null
                    }

                } else {
                    image.reviewStop = new Date()
                }
                if (!image.validate()) throw new WrongArgumentException("Cannot stop review (validate) image instance:" + image.errors)
                if (image.save(flush: true) == null) throw new WrongArgumentException("Cannot stop review (add) image instance:" + image.errors)

                response.imageinstance = image

                if(isCancel)
                    response.message = cytomineService.currentUser.username + " cancel review or validate on " + image.baseImage.filename
                else response.message = cytomineService.currentUser.username + " validate reviewing on " + image.baseImage.filename
                responseSuccess(response)
            } else responseNotFound("Image", params.idImage)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def addAnnotationReview = {
        try {
            AnnotationDomain basedAnnotation = getAnnotationDomain(params.long('id'))
            if(!basedAnnotation.image.isInReviewMode()) throw new WrongArgumentException("Cannot accept annotation, enable image review mode!")
            if(basedAnnotation.image.reviewUser && basedAnnotation.image.reviewUser.id!=cytomineService.currentUser.id) throw new WrongArgumentException("You must be the image reviewer to accept annotation. Image reviewer is ${basedAnnotation.image.reviewUser?.username}.")
            if(ReviewedAnnotation.findByParentIdent(basedAnnotation.id)) throw new AlreadyExistException("Annotation is already accepted!")

            try {
                println "basedAnnotation="+basedAnnotation.location
                ReviewedAnnotation review = createReviewAnnotation(basedAnnotation, null)
                println "review="+review.location
                def result = reviewedAnnotationService.add(JSON.parse(review.encodeAsJSON()))
                responseResult(result)
            } catch (CytomineException e) {
                log.error(e)
                response([success: false, errors: e.msg], e.code)
            }

        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }



    def deleteAnnotationReview = {
        try {
            println "deleteAnnotationReview"
            AnnotationDomain annotation = getAnnotationDomain(params.long('id'))
            println "annotation="+annotation

            ReviewedAnnotation reviewedAnnotation = ReviewedAnnotation.findByParentIdent(annotation.id)
            println "reviewedAnnotation="+reviewedAnnotation
            if(!reviewedAnnotation)
                throw new WrongArgumentException("This annotation is not accepted, you cannot reject it!")
            if(reviewedAnnotation.image.reviewUser && reviewedAnnotation.image.reviewUser.id!=cytomineService.currentUser.id)
                throw new WrongArgumentException("You must be the image reviewer to reject annotation. Image reviewer is ${reviewedAnnotation.image.reviewUser?.username}.")

           def json = JSON.parse("{id : ${reviewedAnnotation.id}}")

            try {
                def domain = reviewedAnnotationService.retrieve(json)
                def result = reviewedAnnotationService.delete(domain, json)
                responseResult(result)
            } catch (CytomineException e) {
                log.error(e)
                response([success: false, errors: e.msg], e.code)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }


    def reviewLayer = {

        try {
            assert sessionFactory != null
            log.info "params.users="+params.users
            log.info("image="+params.image)
            Task task = taskService.read(params.long('task'))
            taskService.updateTask(task,2,"Extract parameters...")
            String[] layersParam = params.users.split(",")
            List<SecUser> users = layersParam.collect {
                SecUser.read(Long.parseLong(it))
            }
            taskService.updateTask(task,3,"Review ${layersParam.length} layers...")
            ImageInstance image = ImageInstance.read(params.long('image'))
            if(!image) throw new WrongArgumentException("Image ${params.image} was not found!")
            if(!image.isInReviewMode()) throw new WrongArgumentException("Cannot review annotation, enable image review mode!")
            if(image.reviewUser && image.reviewUser.id!=cytomineService.currentUser.id) throw new WrongArgumentException("You must be the image reviewer to review annotation. Image reviewer is ${image.reviewUser?.username}.")


            if(users.isEmpty())
                throw new WrongArgumentException("There is no layer:"+params.users)
            if(!image)
                responseNotFound("ImageInstance",params.image)
            else {
                def data = []

                List<AnnotationDomain> annotations = []
                taskService.updateTask(task,5,"Look for all annotations...")
                users.eachWithIndex { user, indexUser ->
                    if(user.algo())
                        annotations.addAll(AlgoAnnotation.findAllByUserAndImage(user,image))
                    else
                        annotations.addAll(UserAnnotation.findAllByUserAndImage(user,image))
                }
                taskService.updateTask(task,10,"${annotations.size()} annotations found...")
                int realReviewed = 0
                int taskRefresh =  annotations.size()>1000? 100 : 10
                annotations.eachWithIndex { annotation, indexAnnotation ->
                    if(indexAnnotation%taskRefresh==0) {
                        taskService.updateTask(task,10+(int)(((double)indexAnnotation/(double)annotations.size())*0.9d*100),"${realReviewed} new reviewed annotations...")
                        cleanUpGorm()

                    }
                    annotation.refresh()

                    if(!ReviewedAnnotation.findByParentIdent(annotation.id)) {
                        realReviewed++
                        def review = reviewAnnotation(annotation, null, false)
                        data << review.id
                    }
                }
                cleanUpGorm()
//                def hibSession = sessionFactory.getCurrentSession()
//                assert hibSession != null
//                hibSession.flush()
                taskService.finishTask(task)
                responseSuccess(data)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }

    }


    def unReviewLayer = {
        try {
            log.info "params.users="+params.users
            log.info("image="+params.image)
            Task task = taskService.read(params.long('task'))
            taskService.updateTask(task,2,"Extract parameters...")
            String[] layersParam = params.users.split(",")
            List<SecUser> users = layersParam.collect {
                SecUser.read(Long.parseLong(it))
            }
            taskService.updateTask(task,3,"Review ${layersParam.length} layers...")
            ImageInstance image = ImageInstance.read(params.long('image'))
            if(!image) throw new WrongArgumentException("Image ${params.image} was not found!")
            if(!image.isInReviewMode()) throw new WrongArgumentException("Cannot reject annotation, enable image review mode!")
            if(image.reviewUser && image.reviewUser.id!=cytomineService.currentUser.id) throw new WrongArgumentException("You must be the image reviewer to reject annotation. Image reviewer is ${image.reviewUser?.username}.")

            if(users.isEmpty())
                throw new WrongArgumentException("There is no layer:"+params.users)
            if(!image)
                responseNotFound("ImageInstance",params.image)
            else {
                def data = []

                List<AnnotationDomain> annotations = []
                taskService.updateTask(task,5,"Look for all annotations...")
                users.eachWithIndex { user, indexUser ->
                    if(user.algo())
                        annotations.addAll(AlgoAnnotation.findAllByUserAndImage(user,image))
                    else
                        annotations.addAll(UserAnnotation.findAllByUserAndImage(user,image))
                }
                println "NUmber of annotations="+annotations.size()
                taskService.updateTask(task,10,"${annotations.size()} annotations found...")
                int realUnReviewed = 0
                int taskRefresh =  annotations.size()>1000? 100 : 10
                annotations.eachWithIndex { annotation, indexAnnotation ->
                    if(indexAnnotation%taskRefresh==0) {
                        taskService.updateTask(task,10+(int)(((double)indexAnnotation/(double)annotations.size())*0.9d*100),"${realUnReviewed} new reviewed annotations...")
                        cleanUpGorm()
                    }
                    //annotation.refresh()
                    ReviewedAnnotation reviewed = ReviewedAnnotation.findByParentIdent(annotation.id)
                    if(reviewed) {
                        realUnReviewed++
                        data << reviewed.id
                        reviewed.delete()
                    }
                }
                cleanUpGorm()
                taskService.finishTask(task)
                responseSuccess(data)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }

    }

    private ReviewedAnnotation createReviewAnnotation(AnnotationDomain annotation, def terms) {
        ReviewedAnnotation review = new ReviewedAnnotation()
        review.parentIdent = annotation.id
        review.parentClassName = annotation.class.name
        review.status = 1
        review.user = cytomineService.currentUser
        review.location = annotation.location
        review.image = annotation.image
        review.project = annotation.project
        review.geometryCompression = annotation.geometryCompression

        if(terms!=null) {
            //terms in request param
            terms.each {
                review.addToTerm(Term.read(Long.parseLong(it+"")))
            }
        } else {
            //nothing in param, add term from annotation
            annotation.termsForReview().each {
                review.addToTerm(it)
            }
        }

        review.reviewUser = cytomineService.currentUser
        review
    }

    private ReviewedAnnotation reviewAnnotation(AnnotationDomain annotation, def terms, boolean flush) {
        ReviewedAnnotation review = createReviewAnnotation(annotation,terms)

        review.reviewUser = cytomineService.currentUser
        if(flush) domainService.saveDomain(review)
        else review.save()
        review
    }

    private AnnotationDomain getAnnotationDomain(long id) {
        AnnotationDomain basedAnnotation = UserAnnotation.read(id)
        if (!basedAnnotation)
            basedAnnotation = AlgoAnnotation.read(id)
        if (basedAnnotation) return basedAnnotation
        else throw new ObjectNotFoundException("Annotation ${id} not found")

    }
}
