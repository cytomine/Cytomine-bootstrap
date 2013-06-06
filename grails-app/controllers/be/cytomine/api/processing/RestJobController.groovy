package be.cytomine.api.processing

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.utils.Task
import grails.converters.JSON

/**
 * Controller for job request.
 * A job is a software instance that has been, is or will be running.
 */
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
    def list = {
        def projects = projectService.list(cytomineService.currentUser)
        responseSuccess(jobService.list(projects))
    }

    /**
     * List all job for a project
     */
    def listByProject = {
        boolean light = params.light==null ? false : params.boolean('light')

        Project project = projectService.read(params.long('id'))
        if(project) {
            log.info "project="+project.id + " software="+params.software
            if(params.software!=null) {
                Software software = Software.read(params.software)
                if(software) {
                    responseSuccess(jobService.list(software,project,light))
                } else {
                    responseNotFound("Job", "Software", params.software)
                }
            } else {
                responseSuccess(jobService.list(project,light))
            }
        } else {
            responseNotFound("Job", "Project", params.id)
        }
    }

    /**
     * List all job for a software and a project
     */
    def listBySoftwareAndProject = {
        Software software = softwareService.read(params.long('idSoftware'));
        Project project = projectService.read(params.long('idProject'));
        if (!software) {
            responseNotFound("Job", "Software", params.idSoftware)
        } else if (!project) {
            responseNotFound("Job", "Project", params.idProject)
        } else {
            responseSuccess(jobService.list(software, project,false))
        }
    }

    /**
     * Get a specific job
     */
    def show = {
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
    def add = {
        try {
            def result = jobService.add(request.JSON)
            def idJob = result?.data?.job?.id
            jobService.executeJob(Job.get(idJob))
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Update a job
     */
    def update = {
        update(jobService, request.JSON)
    }

    /**
     * Delete a job
     */
    def delete = {
        delete(jobService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Delete the full data set build by the job
     * This method will delete: annotation prediction, uploaded files,...
     * This method is heavy, so we use Task service to provide a progress status to the user interface
     */
    def deleteAllJobData = {
        Job job = jobService.read(params.long('id'));

        if (!job) {
            responseNotFound("Job",params.id)
        } else {
            println "1project=${job.project.id}"
            Task task = taskService.read(params.long('task'))
            log.info "load all annotations..."
            //TODO:: Optim instead of loading all annotations to check if there are reviewed annotation => make a single SQL request to see if there are reviewed annotation
            taskService.updateTask(task,10,"Check if annotations are not reviewed...")
            List<AlgoAnnotation> annotations = algoAnnotationService.list(job)
            println "2project=${job.project.id}"
            List<ReviewedAnnotation> reviewed = jobService.hasReviewedAnnotation(annotations,job)

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
    def listAllJobData = {
        Job job = jobService.read(params.long('id'));
        if (!job)
            responseNotFound("Job",params.id)
        else {
            Task task = taskService.read(params.long('task'))
            log.info "load all algo annotations..."
            taskService.updateTask(task,10,"Looking for algo annotations...")
            def annotations = algoAnnotationService.list(job,['basic'])
            boolean reviewed = jobService.hasReviewedAnnotation(annotations,job).size()
            log.info "load all annotations..."
            taskService.updateTask(task,50,"Looking for algo annotations term...")
            long annotationsTermNumber = algoAnnotationTermService.count(job)
            log.info "load all job data..."
            taskService.updateTask(task,75,"Looking for all job data...")
            List<JobData> jobDatas = jobDataService.list(job)
            taskService.finishTask(task)
            responseSuccess([annotations:annotations.size(),annotationsTerm:annotationsTermNumber,jobDatas:jobDatas.size(), reviewed:reviewed])

        }
    }
}
