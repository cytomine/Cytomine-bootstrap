package be.cytomine.api.processing

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.security.UserJob
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.Exception.ConstraintException
import be.cytomine.ontology.AlgoAnnotationTerm

import be.cytomine.processing.JobData

class RestJobController extends RestController {

    def jobService
    def softwareService
    def projectService
    def jobParameterService
    def retrievalSuggestTermJobService
    def userService
    def backgroundService
    def algoAnnotationService
    def annotationTermService
    def algoAnnotationTermService
    def jobDataService

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        log.info "list all job"
        if(params.software!=null) {
            Software software = Software.read(params.software)
            if(software) responseSuccess(jobService.list(software,false,Integer.MAX_VALUE))
            else responseNotFound("Job", "Software", params.software)
        } else responseSuccess(jobService.list())
    }

    def listByProject = {
        log.info "list all job by project"
        boolean light = params.light==null ? false : params.boolean('light')
        int max = params.max==null? Integer.MAX_VALUE : params.int('max')

        Project project = projectService.read(params.long('id'), new Project())
        if(project) {
            log.info "project="+project.id + " software="+params.software
            if(params.software!=null) {
                Software software = Software.read(params.software)
                if(software) responseSuccess(jobService.list(software,project,light,max))
                else responseNotFound("Job", "Software", params.software)
            }
            else responseSuccess(jobService.list(project,light,max))
        } else responseNotFound("Job", "Project", params.id)
    }

    def listBySoftware = {
        Software software = softwareService.read(params.long('id'));
        if (software) responseSuccess(jobService.list(software,false,Integer.MAX_VALUE))
        else responseNotFound("Job", "Software", params.id)
    }

    def listBySoftwareAndProject = {
        Software software = softwareService.read(params.long('idSoftware'));
        Project project = projectService.read(params.long('idProject'), new Project());
        if (!software) responseNotFound("Job", "Software", params.idSoftware)
        if (!project) responseNotFound("Job", "Project", params.idProject)
        else responseSuccess(jobService.list(software, project,false,Integer.MAX_VALUE))
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        Job job = jobService.read(params.long('id'))
        if (job) responseSuccess(job)
        else responseNotFound("Job", params.id)
    }

    def add = {
        try {
            log.debug("add")
            def result = jobService.add(request.JSON)
            log.debug("result="+result)
            def idJob = result?.data?.job?.id
            log.debug("idJob="+idJob)
            executeJob(Job.get(idJob))
            responseResult(result)
        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    private def executeJob(Job job) {
        log.info "Create UserJob..."
        UserJob userJob = createUserJob(User.read(springSecurityService.principal.id), job)
        job.software.service.init(job, userJob)

        log.info "Launch async..."
        backgroundService.execute("RunJobAsynchronously", {
            log.info "Launch thread";
            job.software.service.execute(job)
        })
        job
    }

    def update = {
        log.info "update job controller"
        update(jobService, request.JSON)
    }

    def delete = {
        delete(jobService, JSON.parse("{id : $params.id}"))
    }

    public UserJob createUserJob(User user, Job job) {
        UserJob userJob = new UserJob()
        userJob.job = job
        userJob.username = "JOB[" + user.username + " ], " + new Date().toString()
        userJob.password = user.password
        userJob.generateKeys()
        userJob.enabled = user.enabled
        userJob.accountExpired = user.accountExpired
        userJob.accountLocked = user.accountLocked
        userJob.passwordExpired = user.passwordExpired
        userJob.user = user
        userJob = userJob.save(flush: true)
        user.getAuthorities().each { secRole ->
            SecUserSecRole.create(userJob, secRole)
        }
        return userJob
    }


    def deleteAllJobData = {
        //TODO:: secure this method!
        Job job = jobService.read(params.long('id'));
        if (!job)
            responseNotFound("Job",params.id)
        else {
            log.info "load all annotations..."
            //TODO:: inseatd of loading all annotations to check if there are reviewed annotation => make a single SQL request to see if there are reviewed annotation
            List<AlgoAnnotation> annotations = algoAnnotationService.list(job)
            List<ReviewedAnnotation> reviewed = hasReviewedAnnotation(annotations)

            if(!reviewed.isEmpty())
                responseError(new ConstraintException("There are ${reviewed.size()} reviewed annotations. You cannot delete all job data!"))
            else {
                List<UserJob> users = UserJob.findAllByJob(job)
                log.info "delete all algo annotation term..."
                deleteAllAlgoAnnotationsTerm(users)
                log.info "delete all annotation..."
                deleteAllAlgoAnnotations(users)
                log.info "delete all data..."
                deleteAllJobData(JobData.findAllByJob(job))
                log.info "End..."
                job.dataDeleted = true;
                job.save(flush:true)
                responseSuccess([])
            }
        }
    }

    def listAllJobData = {
        Job job = jobService.read(params.long('id'));
        if (!job)
            responseNotFound("Job",params.id)
        else {
            log.info "load all algo annotations..."
            List<AlgoAnnotation> annotations = algoAnnotationService.list(job)
            log.info "load all annotations..."
            long annotationsTermNumber = algoAnnotationTermService.count(job)
            log.info "load all job datas..."
            List<JobData> jobDatas = jobDataService.list(job)

            responseSuccess([annotations:annotations.size(),annotationsTerm:annotationsTermNumber,jobDatas:jobDatas.size(), reviewed:hasReviewedAnnotation(annotations).size()])

        }
    }

    private def hasReviewedAnnotation(List<AlgoAnnotation> annotations) {
        List<Long> annotationsId = annotations.collect{ it.id }
        if (annotationsId.isEmpty()) []
        return ReviewedAnnotation.findAllByParentIdentInList(annotationsId)
    }

    private void deleteAllAlgoAnnotations(List<UserJob> users) {
        List<Long> usersId = users.collect{ it.id }
        if (usersId.isEmpty()) return
        AlgoAnnotation.executeUpdate("delete from AlgoAnnotation a where a.user.id in (:list)",[list:usersId])
    }

    private void deleteAllAlgoAnnotationsTerm(List<UserJob> users) {
        List<Long> usersId = users.collect{ it.id }
        if (usersId.isEmpty()) return
        AlgoAnnotationTerm.executeUpdate("delete from AlgoAnnotationTerm a where a.userJob.id IN (:list)",[list:usersId])
    }

    private void deleteAllJobData(List<JobData> jobDatas) {
        List<Long> jobDatasId = jobDatas.collect{ it.id }
        if (jobDatasId.isEmpty()) return
        JobData.executeUpdate("delete from JobData a where a.id IN (:list)",[list:jobDatasId])
    }
}
