package be.cytomine.api.processing

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.CytomineMethodNotYetImplementedException
import be.cytomine.api.RestController
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.utils.Task
import grails.converters.JSON
import org.restapidoc.annotation.*
import org.restapidoc.pojo.RestApiParamType

import static org.springframework.security.acls.domain.BasePermission.READ

/**
 * Controller for job request.
 * A job is a software instance that has been, is or will be running.
 */
@RestApi(name = "job services", description = "Methods for managing job. A job is a software instance that has been, is or will be running.")
class RestJobController extends RestController {

    def jobService
    def softwareService
    def projectService
    def secUserService
    def algoAnnotationService
    def algoAnnotationTermService
    def jobDataService
    def taskService
    def cytomineService
    def securityACLService

    /**
     * List all job
     */
    @RestApiMethod(description="Get an algo annotation", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="boolean", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional, default false) If true, get a light/quick listing (without job parameters,...)"),
        @RestApiParam(name="software", type="long", paramType = RestApiParamType.QUERY, description = "(Optional, default get all) A list of software id to filter"),
        @RestApiParam(name="project", type="long", paramType = RestApiParamType.QUERY, description = "(Optional, default get all) A list of project id to filter")
    ])
    def list() {
        Boolean light = params.boolean('light') ? params.boolean('light') : false;
        def softwares_id = params.software ? params.software.split(',').collect { Long.parseLong(it)} : null
        def projects_id = params.project ? params.project.split(',').collect { Long.parseLong(it)} : null

        Collection<Project> projects
        if (projects_id) {
            projects = projectService.readMany(projects_id)
        } else {
            projects = projectService.list(cytomineService.currentUser)
        }

        Collection<Software> softwares
        if (softwares_id) {
            softwares = softwareService.readMany(softwares_id)
        } else {
            softwares = Software.list() //implement security ?
        }

        responseSuccess(jobService.list(softwares, projects, light))
    }

    /**
     * Get a specific job
     */
    @RestApiMethod(description="Get a job", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The job id")
    ])
    def show() {
        Job job = jobService.read(params.long('id'))
        if (job) {
            responseSuccess(job)
        } else {
            responseNotFound("Job", params.id)
        }
    }

    /**
     * Add a new job and launch this new software instance
     */
    @RestApiMethod(description="Add a new job and create the corresponding user job")
    def add() {
        try {
            def result = jobService.add(request.JSON)
            long idJob = result?.data?.job?.id
            jobService.createUserJob(User.read(springSecurityService.principal.id), Job.read(idJob))
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Update a job
     */
    @RestApiMethod(description="Edit a job")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The job id")
    ])
    def update() {
        log.info "update"
        update(jobService, request.JSON)
    }

    /**
     * Delete a job
     */
    @RestApiMethod(description="Delete a job")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The job id")
    ])
    def delete() {
        delete(jobService, JSON.parse("{id : $params.id}"),null)
    }

    @RestApiMethod(description="Execute a job, launch the software")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The job id")
    ])
    def execute() {
        long idJob = params.long("id")
        Job job = Job.read(idJob)
        securityACLService.check(job.container(),READ)
        UserJob userJob = UserJob.findByJob(job)
        job.software.service.init(job, userJob)
        job.software.service.execute(job, userJob, false)
        responseSuccess(job)
    }

    //TODO:APIDOC
    def preview() {
        long idJob = params.long("id")
        Job job = Job.read(idJob)
        if (!job.software.service.previewAvailable()) {
            throw new CytomineMethodNotYetImplementedException("Preview is not available for $job.software" )
        }
        securityACLService.check(job.container(),READ)
        UserJob userJob = UserJob.findByJob(job)
        job.software.service.init(job, userJob)
        job.software.service.execute(job, userJob, true)
        responseSuccess(job)
    }

    //TODO:APIDOC
    def getPreviewRoi() {
        Job job = jobService.read(params.long('id'))
        byte[] data = job.software.service.getPreviewROI(job)
        if (data) {
            response.setHeader "Content-disposition", "inline"
            response.outputStream << data
            response.outputStream.flush()
        } else {
            responseNotFound("JobData", "getPreviewRoi")
        }
    }

    //TODO:APIDOC
    def getPreview() {
        Job job = jobService.read(params.long('id'))
        byte[] data = job.software.service.getPreview(job)
        if (data) {
            response.setHeader "Content-disposition", "inline"
            response.outputStream << data
            response.outputStream.flush()
        } else {
            responseNotFound("JobData", "getPreview")
        }
    }

    /**
     * Delete the full data set build by the job
     * This method will delete: annotation prediction, uploaded files,...
     * This method is heavy, so we use Task service to provide a progress status to the user interface
     */
    @RestApiMethod(description="Delete the full data set build by the job. This method will delete: annotation prediction, uploaded files,...")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The job id"),
        @RestApiParam(name="task", type="long", paramType = RestApiParamType.QUERY,description = "(Optional) The task id. This method is heavy, so we use Task service to provide a progress status to the user interface.")
    ])
    @RestApiResponseObject(objectIdentifier = "[message:x]")
    def deleteAllJobData() {
        Job job = jobService.read(params.long('id'));


        if (!job) {
            responseNotFound("Job",params.id)
        } else {
            securityACLService.checkReadOnly(job.container())
            securityACLService.checkIsAdminContainer(job.project,cytomineService.currentUser)
            Task task = taskService.read(params.long('task'))
            log.info "load all annotations..."
            //TODO:: Optim instead of loading all annotations to check if there are reviewed annotation => make a single SQL request to see if there are reviewed annotation
            taskService.updateTask(task,10,"Check if annotations are not reviewed...")
            List<AlgoAnnotation> annotations = algoAnnotationService.list(job)
            def reviewed = jobService.getReviewedAnnotation(annotations,job)

            if(!reviewed.isEmpty()) {
                taskService.finishTask(task)
                responseError(new ConstraintException("There are ${reviewed.size()} reviewed annotations. You cannot delete all job data!"))
            } else {

                taskService.updateTask(task,30,"Delete all terms from annotations...")
                jobService.deleteAllAlgoAnnotationsTerm(job)

                taskService.updateTask(task,60,"Delete all annotations...")
                jobService.deleteAllAlgoAnnotations(job)

                taskService.updateTask(task,90,"Delete all files...")
                jobService.deleteAllJobData(job)

                taskService.finishTask(task)
                job.dataDeleted = true;
                job.save(flush:true)
                responseSuccess([message:"All data from job launch "+ job.created + " are deleted!"])
            }
        }
    }

    /**
     * List all data build by the job
     * Job data are prediction (algoannotationterm, algoannotation,...) and uploaded files
     */
    @RestApiMethod(description="List all data build by the job. Job data are prediction (algoannotationterm, algoannotation,...) and uploaded files")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The job id"),
        @RestApiParam(name="task", type="long", paramType = RestApiParamType.QUERY,description = "(Optional) The task id. This method is heavy, so we use Task service to provide a progress status to the user interface.")
        ])
    @RestApiResponseObject(objectIdentifier = "[annotations:x,annotationsTerm:x,jobDatas:x,reviewed:x]")
    def listAllJobData () {
        Job job = jobService.read(params.long('id'))
        if (!job)
            responseNotFound("Job",params.id)
        else {
            Task task = taskService.read(params.long('task'))
            log.info "load all algo annotations..."
            taskService.updateTask(task,10,"Looking for algo annotations...")
            def annotations = algoAnnotationService.list(job,['basic'])
            def reviewed = jobService.getReviewedAnnotation(annotations,job)
            log.info "load all annotations...$reviewed"
            taskService.updateTask(task,50,"Looking for algo annotations term...")
            long annotationsTermNumber = algoAnnotationTermService.count(job)
            log.info "load all job data..."
            taskService.updateTask(task,75,"Looking for all job data...")
            List<JobData> jobDatas = jobDataService.list(job)
            taskService.finishTask(task)
            responseSuccess([annotations:annotations.size(),annotationsTerm:annotationsTermNumber,jobDatas:jobDatas.size(), reviewed:reviewed.size()])

        }
    }

    @RestApiMethod(description="For a project, delete all job data if the job has no reviewed annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id"),
        @RestApiParam(name="task", type="long", paramType = RestApiParamType.QUERY,description = "(Optional) The task id. This method is heavy, so we use Task service to provide a progress status to the user interface.")
    ])
    @RestApiResponseObject(objectIdentifier = "project")
    def purgeJobNotReviewed () {
        //retrieve project
        Project  project = projectService.read(params.long('id'))
        try {
            securityACLService.checkIsAdminContainer(project,cytomineService.currentUser)

            securityACLService.checkIsAdminContainer(project,cytomineService.currentUser)
               if (!project)
                   responseNotFound("Project",params.id)
               else {
                   //retrieve task
                   Task task = taskService.read(params.long('task'))
                   log.info "Looking for all jobs..."
                   taskService.updateTask(task,1,"Looking for all jobs...")
                   //get all jobs
                   def jobs = Job.findAllByProjectAndDataDeleted(project,false)

                   jobs.eachWithIndex { job, i ->
                       //check if job has reviewed annotation
                       boolean isReviewed = jobService.hasReviewedAnnotation(job)

                       //if job has no reviewed annotation deleteAllAlgoAnnotationsTerm / deleteAllAlgoAnnotations / deleteAllJobData
                       if(!isReviewed) {
                           removeJobData(job)
                           int pogress = (int)Math.floor((double)i/(double)jobs.size()*100)
                           taskService.updateTask(task,pogress,"Delete data for job: ${job.software.name} #${job.number}")
                       }

                   }
                   taskService.finishTask(task)
                   project.refresh()
                   responseSuccess(project)
               }

        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }


    private def removeJobData(Job job) {
        jobService.deleteAllAlgoAnnotationsTerm(job)
        jobService.deleteAllAlgoAnnotations(job)
        jobService.deleteAllJobData(job)
        job.dataDeleted = true;
        job.save(flush:true)
    }
}
