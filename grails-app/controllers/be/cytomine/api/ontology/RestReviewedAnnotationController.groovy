package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.Task
import grails.converters.JSON

import java.text.SimpleDateFormat

/**
 * Controller for reviewed annotation
 * A reviewed annotation is an annotation that is validate by a user with its term
 */
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
    def exportService


    /**
     * List all reviewed annotation available for the user
     */
    def list = {
        def annotations = []
        def projects = projectService.list()
        projects.each {
            annotations.addAll(reviewedAnnotationService.list(it,paramsService.getPropertyGroupToShow(params)))
        }
        responseSuccess(annotations)
    }

    /**
     * List reviewed annotation for an image
     * bbox param can be use to select only annotation inside bbox
     */
    def listByImage = {
        ImageInstance image = imageInstanceService.read(params.long('idImage'))
        if (image && params.bbox) {
            def data = reviewedAnnotationService.list(image,(String)params.bbox,['basic','wkt','term'])
            responseSuccess(data)
        }
        else if(image) {
            responseSuccess(reviewedAnnotationService.list(image,paramsService.getPropertyGroupToShow(params)))
        }
        else {
            responseNotFound("Image", params.idImage)
        }
    }

    /**
     * List all by image and user
     */
    def listByImageAndUser = {
        def image = imageInstanceService.read(params.long('idImage'))
        def user = secUserService.read(params.idUser)
        if (image && user && params.bbox) {
            responseSuccess(reviewedAnnotationService.list(image, user, (String) params.bbox,['basic','wkt','term']))
        } else if (image && user) {
            responseSuccess(reviewedAnnotationService.list(image, user,paramsService.getPropertyGroupToShow(params)))
        }
        else if (!user) {
            responseNotFound("User", params.idUser)
        }
        else if (!image) {
            responseNotFound("Image", params.idImage)
        }
    }

    /**
     * List reviewed annotation by image and by term
     */
    def listByImageAndTerm = {
        ImageInstance image = imageInstanceService.read(params.long('idImage'))
        Term term = termService.read(params.long('idTerm'))
        if (image && term) {
            def list = reviewedAnnotationService.list(image,term,paramsService.getPropertyGroupToShow(params))
            responseSuccess(list)
        }
        else if(image) {
            responseSuccess(reviewedAnnotationService.list(image,paramsService.getPropertyGroupToShow(params)))
        }
        else {
            responseNotFound("Image", params.idImage)
        }
    }

    /**
     * List all reviewed annotation by project
     */
    def listByProject = {
        Project project = projectService.read(params.long('idproject'))
        if (project) {
            responseSuccess(reviewedAnnotationService.list(project,paramsService.getPropertyGroupToShow(params)))
        }
        else {
            responseNotFound("Project", params.idproject)
        }
    }

    /**
     *  List all reviewed annotation by project, term and user filter
     */
    def listByProjectImageTermAndUser = {
        if ((params.users == null || params.users == "null") && (params.images == null || params.images == "null") && (params.terms == null || params.terms == "null")) {
            //if no filter, simply forward to listByproject method
            forward(action: "listByProject")
        } else {
           Project project = projectService.read(params.long('idproject'))
           if (project) {

               List<Long> userList = paramsService.getParamsSecUserList(params.users,project)
               List<Long> imageInstanceList = paramsService.getParamsImageInstanceList(params.images,project)
               List<Long> termList = paramsService.getParamsTermList(params.terms, project)

               if (userList.isEmpty()) {
                   responseNotFound("User", params.users)
               } else if (imageInstanceList.isEmpty()) {
                   responseNotFound("ImageInstance", params.images)
               } else if (termList.isEmpty()) {
                   responseNotFound("Term", params.terms)
               } else {
                   def list = reviewedAnnotationService.list(project, termList, userList, imageInstanceList ,null, paramsService.getPropertyGroupToShow(params))
                   responseSuccess(list)
               }
           } else {
               responseNotFound("Project", params.idproject)
           }
        }
    }


    def listAnnotationByProjectAndTerm = {
        Term term = termService.read(params.long('idterm'))
        Project project = projectService.read(params.long('idproject'))

        if (term == null) {
            responseNotFound("Term", params.idterm)
        } else if (project == null) {
            responseNotFound("Project", params.idproject)
        }
        else {
            List<Long> userList = paramsService.getParamsSecUserList(params.users, project)
            List<Long> imageInstanceList = paramsService.getParamsImageInstanceList(params.images, project)

            def list
            if (userList.isEmpty() || imageInstanceList.isEmpty()) {
                list = []
            } else {
                list = reviewedAnnotationService.list(project, [term.id], userList, imageInstanceList,null,paramsService.getPropertyGroupToShow(params))
            }
            responseSuccess(list)
        }
    }

    def stats = {
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
    def show = {
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
    def add = {
        add(reviewedAnnotationService, request.JSON)
    }

    /**
     * Update reviewed annotation
     */
    def update = {
        update(reviewedAnnotationService, request.JSON)
    }

    /**
     * Delete reviewed annotation
     */
    def delete = {
        delete(reviewedAnnotationService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Start the review mode on an image
     * To review annotation, a user must enable review mode in the current image
     */
    def startImageInstanceReview = {
        try {
            def image = imageInstanceService.read(params.long("id"))
            def response = [:]

            if (image) {
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
    def stopImageInstanceReview = {
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
    def addAnnotationReview = {
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
    def deleteAnnotationReview = {
        try {
            AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(params.long('id'))
            ReviewedAnnotation reviewedAnnotation = ReviewedAnnotation.findByParentIdent(annotation.id)

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
    def reviewLayer = {

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
    def unReviewLayer = {
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
        review.user = cytomineService.currentUser
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



    def downloadDocumentByProject = {
        // Export service provided by Export plugin

        Project project = projectService.read(params.long('id'))
        if (!project) {
            responseNotFound("Project", params.long('id'))
        }

        //users=9331125,16&terms=-1,-2,9331340,9331346&images=9331299

        def users = []
        if (params.users != null && params.users != "") {
            params.users.split(",").each { id ->
                users << Long.parseLong(id)
            }
        }
        def terms = []
        if (params.terms != null && params.terms != "") {
            params.terms.split(",").each {  id ->
                terms << Long.parseLong(id)
            }
        }
        def images = []
        if (params.images != null && params.images != "") {
            params.images.split(",").each {  id ->
                images << Long.parseLong(id)
            }
        }
        def termsName = Term.findAllByIdInList(terms).collect { it.toString() }
        def usersName = SecUser.findAllByIdInList(users).collect { it.toString() }

        if (params?.format && params.format != "html") {
            def exporterIdentifier = params.format;
            if (exporterIdentifier == "xls") exporterIdentifier = "excel"
            response.contentType = grailsApplication.config.grails.mime.types[params.format]
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
            String datePrefix = simpleFormat.format(new Date())
            response.setHeader("Content-disposition", "attachment; filename=${datePrefix}_annotations_project${project.id}.${params.format}")


            def annotations = ReviewedAnnotation.executeQuery(
               'select distinct a ' +
               'from ReviewedAnnotation a inner join a.terms terms ' +
               'where a.project = :project and a.image.id in (:images) and a.user.id in (:users) and terms.id in (:terms)',
            [project: project, images: images, users: users, terms: terms])


            def exportResult = []
            annotations.each { annotation ->
                def centroid = annotation.getCentroid()
                def data = [:]
                data.id = annotation.id
                data.perimeterUnit = annotation.getPerimeterUnit()
                data.areaUnit = annotation.getAreaUnit()
                data.area = annotation.computeArea()
                data.perimeter = annotation.computePerimeter()
                if (centroid != null) {
                    data.XCentroid = (int) Math.floor(centroid.x)
                    data.YCentroid = (int) Math.floor(centroid.y)
                } else {
                    data.XCentroid = "undefined"
                    data.YCentroid = "undefined"
                }
                data.image = annotation.image.id
                data.filename = annotation.getFilename()
                data.user = annotation.user.toString()
                data.term = annotation.terms().join(", ")
                data.cropURL = UrlApi.getUserAnnotationCropWithAnnotationId(annotation.id)
                data.cropGOTO = UrlApi.getAnnotationURL(annotation?.image?.project?.id, annotation.image.id, annotation.id)
                exportResult.add(data)
            }

            List fields = ["id", "area", "perimeter", "XCentroid", "YCentroid", "image", "filename", "user", "term", "cropURL", "cropGOTO"]
            Map labels = ["id": "Id", "area": "Area (microns²)", "perimeter": "Perimeter (mm)", "XCentroid": "X", "YCentroid": "Y", "image": "Image Id", "filename": "Image Filename", "user": "User", "term": "Term", "cropURL": "View userannotation picture", "cropGOTO": "View userannotation on image"]
            String title = "Annotations in " + project.getName() + " created by " + usersName.join(" or ") + " and associated with " + termsName.join(" or ") + " @ " + (new Date()).toLocaleString()

            exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.04, 0.06, 0.06, 0.04, 0.04, 0.04, 0.08, 0.06, 0.06, 0.25, 0.25], "title": title, "csv.encoding": "UTF-8", "separator": ";"])
        }
    }

}
