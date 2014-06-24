package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.security.SecUser
import be.cytomine.utils.Task
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.annotation.RestApiResponseObject
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for reviewed annotation
 * A reviewed annotation is an annotation that is validate by a user with its term
 */
@RestApi(name = "reviewed annotation services", description = "Methods for managing reviewed annotation, annotation validated by human")
class RestReviewedAnnotationController extends RestController {

    def paramsService
    def algoAnnotationService
    def termService
    def imageInstanceService
    def secUserService
    def projectService
    def cytomineService
    def dataSource
    def reviewedAnnotationService
    def taskService
    def reportService
    def imageProcessingService
    def securityACLService

    /**
     * List all reviewed annotation available for the user
     */
    @RestApiMethod(description="List all reviewed annotation available for the user", listing=true)
    def list() {
        def annotations = []
        def projects = projectService.list()
        projects.each {
            annotations.addAll(reviewedAnnotationService.list(it,paramsService.getPropertyGroupToShow(params)))
        }
        responseSuccess(annotations)
    }

    @RestApiMethod(description="Count the number of reviewed annotation for the current user")
    @RestApiResponseObject(objectIdentifier = "[total:x]")
    def countByUser() {
        responseSuccess([total:reviewedAnnotationService.count(cytomineService.currentUser)])
    }

    @RestApiMethod(description="Get the number of review for each user in an image")
    @RestApiParams(params=[
        @RestApiParam(name="image", type="long", paramType = RestApiParamType.PATH,description = "The image instance id")
    ])
    def stats() {
        ImageInstance image = imageInstanceService.read(params.long('image'))
        if(image) {
            responseSuccess(reviewedAnnotationService.stats(image))
        }
        else {
            responseNotFound("Image", params.image)
        }
    }

    /**
     * Get a single reviewed annotation
     */
    @RestApiMethod(description="Get a reviewed annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The annotation id")
    ])
    def show() {
        ReviewedAnnotation annotation = reviewedAnnotationService.read(params.long('id'))
        if (annotation) {
            responseSuccess(annotation)
        }
        else {
            responseNotFound("ReviewedAnnotation", params.id)
        }
    }

    /**
     * Add reviewed annotation
     * Only use to create a reviewed annotation with all json data.
     * Its better to use 'addAnnotationReview' that needs only the annotation id and a list of term
     */
    @RestApiMethod(description="Add reviewed annotation. Only use to create a reviewed annotation with all json data. Use /annotation/x/review")
    def add () {
        add(reviewedAnnotationService, request.JSON)
    }

    /**
     * Update reviewed annotation
     */
    @RestApiMethod(description="Update a reviewed annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
    ])
    def update() {
        update(reviewedAnnotationService, request.JSON)
    }

    /**
     * Delete reviewed annotation
     */
    @RestApiMethod(description="Delete a reviewed annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
    ])
    def delete() {
        delete(reviewedAnnotationService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Start the review mode on an image
     * To review annotation, a user must enable review mode in the current image
     */
    @RestApiMethod(description="Start to review an image. The current user will be the reviewer.")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image instance id")
    ])
    def startImageInstanceReview() {
        try {
            def image = imageInstanceService.read(params.long("id"))
            def response = [:]

            if (image) {
                securityACLService.checkReadOnly(image.container())
                image.reviewStart = new Date()
                image.reviewUser = cytomineService.currentUser
                reviewedAnnotationService.saveDomain(image)

                response.message = image.reviewUser.username + " start reviewing on " + image.baseImage.filename
                response.imageinstance = image
                responseSuccess(response)
            } else {
                responseNotFound("Image", params.idImage)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Stop the review mode on the current image
     * It can be:
     * -cancel (no reviewed annotation must be done)
     * -validate
     */
    @RestApiMethod(description="Stop to review an image. ")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image instance id"),
        @RestApiParam(name="cancel", type="boolean", paramType = RestApiParamType.QUERY,description = "If false, the image will be validated. If true, the image return in unreview status (only if no reviewed annotation) "),
    ])
    def stopImageInstanceReview() {
        try {

            def image = imageInstanceService.read(params.long("id"))
            boolean isCancel = params.getBoolean("cancel")

            if (image) {
                if (image.reviewStart == null || image.reviewUser == null) {
                    throw new WrongArgumentException("Image is not in review mode: image.reviewStart=${image.reviewStart} and image.reviewUser=${image.reviewUser}")
                }
                if (cytomineService.currentUser != image.reviewUser) {
                    throw new WrongArgumentException("Review can only be validate or stop by "+image.reviewUser.username)
                }

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
                reviewedAnnotationService.saveDomain(image)

                def response = [:]
                response.imageinstance = image

                if(isCancel) {
                    response.message = cytomineService.currentUser.username + " cancel review or validate on " + image.baseImage.filename
                }
                else {
                    response.message = cytomineService.currentUser.username + " validate reviewing on " + image.baseImage.filename
                }
                responseSuccess(response)
            } else {
                responseNotFound("Image", params.idImage)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Review annotation
     */
    @RestApiMethod(description="Review a user or algo annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
        @RestApiParam(name="POST JSON (terms)", type="list", paramType = RestApiParamType.QUERY,description = "List of annotation terms (if null, keep term already map with this annotation by the annotation creator)"),
    ])
    def addAnnotationReview() {
        try {
            AnnotationDomain basedAnnotation = AnnotationDomain.getAnnotationDomain(params.long('id'))
            if(!basedAnnotation.image.isInReviewMode()) {
                throw new WrongArgumentException("Cannot accept annotation, enable image review mode!")
            }
            if(basedAnnotation.image.reviewUser && basedAnnotation.image.reviewUser.id!=cytomineService.currentUser.id) {
                throw new WrongArgumentException("You must be the image reviewer to accept annotation. Image reviewer is ${basedAnnotation.image.reviewUser?.username}.")
            }
            if(ReviewedAnnotation.findByParentIdent(basedAnnotation.id)) {
                throw new AlreadyExistException("Annotation is already accepted!")
            }

            ReviewedAnnotation review = createReviewAnnotation(basedAnnotation, null)

            def json = JSON.parse(review.encodeAsJSON())
            def jsonRequest = request.JSON.terms
            if(jsonRequest) {
                json.terms = jsonRequest
            }
            def result = reviewedAnnotationService.add(json)
            responseResult(result)

        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Unreview annotation
     */
    @RestApiMethod(description="Unreview (cancel the review) a user or algo annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
     ])
    def deleteAnnotationReview() {
        try {
            ReviewedAnnotation reviewedAnnotation = ReviewedAnnotation.findByParentIdent(params.long('id'))

            if(!reviewedAnnotation) {
                throw new WrongArgumentException("This annotation is not accepted, you cannot reject it!")
            }
            if(reviewedAnnotation.image.reviewUser && reviewedAnnotation.image.reviewUser.id!=cytomineService.currentUser.id) {
                throw new WrongArgumentException("You must be the image reviewer to reject annotation. Image reviewer is ${reviewedAnnotation.image.reviewUser?.username}.")
            }
            def json = JSON.parse("{id : ${reviewedAnnotation.id}}")
            def result = reviewedAnnotationService.delete(reviewedAnnotationService.retrieve(json))
            responseResult(result)

        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Review all annotation in image for a user
     * It support the task functionnality, if task param is set,
     * this method will update its progress status to the task.
     * User can access task status by getting the task info
     */
    @RestApiMethod(description="Review all annotation in image for a list of user")
    @RestApiParams(params=[
        @RestApiParam(name="image", type="long", paramType = RestApiParamType.PATH,description = "The image id"),
        @RestApiParam(name="users", type="list", paramType = RestApiParamType.PATH,description = "The list of users id")
    ])
    def reviewLayer() {

        try {
            Task task = taskService.read(params.long('task'))

            //Read all users to review
            taskService.updateTask(task,2,"Extract parameters...")
            String[] layersParam = params.users.split(",")
            List<SecUser> users = layersParam.collect {
                SecUser.read(Long.parseLong(it))
            }
            ImageInstance image = ImageInstance.read(params.long('image'))

            if(!image) {
                throw new WrongArgumentException("Image ${params.image} was not found!")
            } else if(!image.isInReviewMode()) {
                throw new WrongArgumentException("Cannot review annotation, enable image review mode!")
            } else if(image.reviewUser && image.reviewUser.id!=cytomineService.currentUser.id) {
                throw new WrongArgumentException("You must be the image reviewer to review annotation. Image reviewer is ${image.reviewUser?.username}.")
            } else if(users.isEmpty())
                throw new WrongArgumentException("There is no layer:"+params.users)
            if(!image)
                responseNotFound("ImageInstance",params.image)
            else {
                def data = []
                taskService.updateTask(task,3,"Review ${layersParam.length} layers...")
                List<AnnotationDomain> annotations = []

                //get all annotations for each user
                taskService.updateTask(task,5,"Look for all annotations...")
                users.eachWithIndex { user, indexUser ->
                    if(user.algo()) {
                        annotations.addAll(AlgoAnnotation.findAllByUserAndImage(user,image))
                    }
                    else {
                        annotations.addAll(UserAnnotation.findAllByUserAndImage(user,image))
                    }
                }

                //review each annotation
                taskService.updateTask(task,10,"${annotations.size()} annotations found...")
                int realReviewed = 0
                int taskRefresh =  annotations.size()>1000? 100 : 10
                annotations.eachWithIndex { annotation, indexAnnotation ->
                    if(indexAnnotation%taskRefresh==0) {
                        //update the task and clean the gorm cache (optim)
                        taskService.updateTask(task,10+(int)(((double)indexAnnotation/(double)annotations.size())*0.9d*100),"${realReviewed} new reviewed annotations...")
                        cleanUpGorm()

                    }
                    annotation.refresh()
                    if(!ReviewedAnnotation.findByParentIdent(annotation.id)) {
                        //if not yet reviewed, review it
                        realReviewed++
                        def review = reviewAnnotation(annotation, null, false)
                        data << review.id
                    }
                }
                cleanUpGorm()
                //finish task
                taskService.finishTask(task)
                responseSuccess(data)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }

    }

    /**
     * Unreview all annotation for all layers in params
     */
    @RestApiMethod(description="Cancel the review of all annotation in image for a list of user")
    @RestApiParams(params=[
        @RestApiParam(name="image", type="long", paramType = RestApiParamType.PATH,description = "The image id"),
        @RestApiParam(name="users", type="list", paramType = RestApiParamType.PATH,description = "The list of users id")
    ])
    def unReviewLayer(){
        try {
            Task task = taskService.read(params.long('task'))

            //extract params info
            taskService.updateTask(task,2,"Extract parameters...")
            String[] layersParam = params.users.split(",")
            List<SecUser> users = layersParam.collect {
                SecUser.read(Long.parseLong(it))
            }
            ImageInstance image = ImageInstance.read(params.long('image'))

            //check constraint
            taskService.updateTask(task,3,"Review ${layersParam.length} layers...")
            if(!image) {
                responseNotFound("ImageInstance",params.image)
            } else if(!image.isInReviewMode()) {
                throw new WrongArgumentException("Cannot reject annotation, enable image review mode!")
            } else if(image.reviewUser && image.reviewUser.id!=cytomineService.currentUser.id) {
                throw new WrongArgumentException("You must be the image reviewer to reject annotation. Image reviewer is ${image.reviewUser?.username}.")
            } else if(users.isEmpty()) {
                throw new WrongArgumentException("There is no layer:"+params.users)
            } else {
                def data = []

                //get all annotations for each user
                List<AnnotationDomain> annotations = []
                taskService.updateTask(task,5,"Look for all annotations...")
                users.eachWithIndex { user, indexUser ->
                    if(user.algo())
                        annotations.addAll(AlgoAnnotation.findAllByUserAndImage(user,image))
                    else
                        annotations.addAll(UserAnnotation.findAllByUserAndImage(user,image))
                }

                //unreview each one
                taskService.updateTask(task,10,"${annotations.size()} annotations found...")
                int realUnReviewed = 0
                int taskRefresh =  annotations.size()>1000? 100 : 10
                annotations.eachWithIndex { annotation, indexAnnotation ->
                    if(indexAnnotation%taskRefresh==0) {
                        taskService.updateTask(task,10+(int)(((double)indexAnnotation/(double)annotations.size())*0.9d*100),"${realUnReviewed} new reviewed annotations...")
                        cleanUpGorm()
                    }
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

    /**
     * Review annotation with the specified terms
     * @param annotation Annotation to review
     * @param terms Terms to add to the annotation
     * @return The reviewed annotation
     */
    private ReviewedAnnotation createReviewAnnotation(AnnotationDomain annotation, def terms) {
        ReviewedAnnotation review = new ReviewedAnnotation()
        review.parentIdent = annotation.id
        review.parentClassName = annotation.class.name
        review.status = 1
        review.user = annotation.user
        review.location = annotation.location
        review.image = annotation.image
        review.project = annotation.project
        review.geometryCompression = annotation.geometryCompression

        if(terms!=null) {
            //terms in request param
            terms.each {
                review.addToTerms(Term.read(Long.parseLong(it+"")))
            }
        } else {
            //nothing in param, add term from annotation
            annotation.termsForReview().each {
                review.addToTerms(it)
            }
        }
        review.reviewUser = cytomineService.currentUser
        review
    }

    /**
     * Review annotation with the specified terms
     * @param annotation Annotation to review
     * @param terms Terms to add to the annotation
     * @return The reviewed annotation
     */
    private ReviewedAnnotation reviewAnnotation(AnnotationDomain annotation, def terms, boolean flush) {
        ReviewedAnnotation review = createReviewAnnotation(annotation,terms)
        review.reviewUser = cytomineService.currentUser
        if(flush) {
            reviewedAnnotationService.saveDomain(review)
        }
        else {
            review.save()
        }
        review
    }


    @RestApiMethod(description="Download a report (pdf, xls,...) with reviewed annotation data from a specific project")
    @RestApiResponseObject(objectIdentifier = "file")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id"),
        @RestApiParam(name="terms", type="list", paramType = RestApiParamType.QUERY,description = "The annotation terms id (if empty: all terms)"),
        @RestApiParam(name="users", type="list", paramType = RestApiParamType.QUERY,description = "The annotation users id (if empty: all users)"),
        @RestApiParam(name="images", type="list", paramType = RestApiParamType.QUERY,description = "The annotation images id (if empty: all images)"),
        @RestApiParam(name="format", type="string", paramType = RestApiParamType.QUERY,description = "The report format (pdf, xls,...)")
    ])
    def downloadDocumentByProject() {
        reportService.createAnnotationDocuments(params.long('id'),params.terms,params.users,params.images,params.format,response,"REVIEWEDANNOTATION")
    }



    /**
     * Get annotation review crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    @RestApiMethod(description="Get annotation reviewed crop (image area that frame annotation)")
    @RestApiResponseObject(objectIdentifier = "file")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
        @RestApiParam(name="max_size", type="int", paramType = RestApiParamType.PATH,description = "Maximum size of the crop image (w and h)"),
        @RestApiParam(name="zoom", type="int", paramType = RestApiParamType.PATH,description = "Zoom level"),
        @RestApiParam(name="draw", type="boolean", paramType = RestApiParamType.PATH,description = "Draw annotation form border on the image")
    ])
    def crop() {
        ReviewedAnnotation annotation = ReviewedAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("ReviewedAnnotation", params.id)
        } else {
            redirect (url : annotation.toCropURL(params))
        }

    }

    //TODO:APIDOC
    def cropMask () {
        ReviewedAnnotation annotation = ReviewedAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("ReviewedAnnotation", params.id)
        } else {
            params.mask = true
            redirect (url : annotation.toCropURL(params))
        }

    }

    //TODO:APIDOC
    def cropAlphaMask () {
        ReviewedAnnotation annotation = ReviewedAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("ReviewedAnnotation", params.id)
        } else {
            params.alphaMask = true
            redirect (url : annotation.toCropURL(params))
        }

    }
}
