package be.cytomine.api.processing

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.SecurityACL
import be.cytomine.api.RestController
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.utils.Task
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for job request.
 * A job is a software instance that has been, is or will be running.
 */
@Api(name = "job services", description = "Methods for managing job. A job is a software instance that has been, is or will be running.")
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

    /**
     * List all job
     */
    @ApiMethodLight(description="Get an algo annotation", listing = true)
    @ApiParams(params=[
        @ApiParam(name="boolean", type="boolean", paramType = ApiParamType.QUERY, description = "(Optional, default false) If true, get a light/quick listing (without job parameters,...)"),
        @ApiParam(name="software", type="long", paramType = ApiParamType.QUERY, description = "(Optional, default get all) A list of software id to filter"),
        @ApiParam(name="project", type="long", paramType = ApiParamType.QUERY, description = "(Optional, default get all) A list of project id to filter")
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
    @ApiMethodLight(description="Get a job", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The job id")
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
    @ApiMethodLight(description="Add a new job and create the corresponding user job")
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
    @ApiMethodLight(description="Edit a job")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The job id")
    ])
    def update() {
        log.info "update"
        update(jobService, request.JSON)
    }

    /**
     * Delete a job
     */
    @ApiMethodLight(description="Delete a job")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The job id")
    ])
    def delete() {
        delete(jobService, JSON.parse("{id : $params.id}"),null)
    }

    @ApiMethodLight(description="Execute a job, launch the software")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The job id")
    ])
    def execute() {
        long idJob = params.long("id")
        Job job = Job.read(idJob)
        jobService.executeJob(job, false)
        responseSuccess(job)
    }

    //TODO:APIDOC
    def preview() {
        long idJob = params.long("id")
        Job job = Job.read(idJob)
        jobService.executeJob(job, true)
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
    @ApiMethodLight(description="Delete the full data set build by the job. This method will delete: annotation prediction, uploaded files,...")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The job id"),
        @ApiParam(name="task", type="long", paramType = ApiParamType.QUERY,description = "(Optional) The task id. This method is heavy, so we use Task service to provide a progress status to the user interface.")
    ])
    @ApiResponseObject(objectIdentifier = "[message:x]")
    def deleteAllJobData() {
        Job job = jobService.read(params.long('id'));


        if (!job) {
            responseNotFound("Job",params.id)
        } else {
            SecurityACL.checkReadOnly(job.container())
            SecurityACL.checkIsAdminContainer(job.project,cytomineService.currentUser)
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
    @ApiMethodLight(description="List all data build by the job. Job data are prediction (algoannotationterm, algoannotation,...) and uploaded files")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The job id"),
        @ApiParam(name="task", type="long", paramType = ApiParamType.QUERY,description = "(Optional) The task id. This method is heavy, so we use Task service to provide a progress status to the user interface.")
        ])
    @ApiResponseObject(objectIdentifier = "[annotations:x,annotationsTerm:x,jobDatas:x,reviewed:x]")
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

    @ApiMethodLight(description="For a project, delete all job data if the job has no reviewed annotation")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The project id"),
        @ApiParam(name="task", type="long", paramType = ApiParamType.QUERY,description = "(Optional) The task id. This method is heavy, so we use Task service to provide a progress status to the user interface.")
    ])
    @ApiResponseObject(objectIdentifier = "project")
    def purgeJobNotReviewed () {
        //retrieve project
        Project  project = projectService.read(params.long('id'))
        try {
            SecurityACL.checkIsAdminContainer(project,cytomineService.currentUser)

            SecurityACL.checkIsAdminContainer(project,cytomineService.currentUser)
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
