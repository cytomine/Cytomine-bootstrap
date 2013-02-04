package be.cytomine.processing

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.utils.ModelService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import grails.converters.JSON

class JobService extends ModelService {

    static transactional = true
    def cytomineService
    def modelService
    def transactionService
    def jobParameterService
    def springSecurityService
    def backgroundService
    def jobDataService
    def secUserService

    def read(def id) {
        def job = Job.read(id)
        if(job) {
            SecurityCheck.checkReadAuthorization(job.project)
        }
        job
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def list() {
        Job.list()
    }

    /**
     * List max job for a project
     * Light flag allow to get a light list with only main job properties
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, boolean light, def max) {
        def jobs = Job.findAllByProject(project, [max: max, sort: "created", order: "desc"])
        if(!light) {
            return jobs
        } else {
            getJOBResponseList(jobs)
        }
    }

    /**
     * List max job for a project and a software
     * Light flag allow to get a light list with only main job properties
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Software software, Project project, boolean light, def max) {
        def jobs = Job.findAllBySoftwareAndProject(software, project, [max: max, sort: "created", order: "desc"])
        if(!light) {
            jobs.each {
                //compute success rate if not yet done
                //TODO: this may be heavy...computeRate just after job running?
                if(it.rate==-1 && it.status==Job.SUCCESS) {
                    it.rate = it.software?.service?.computeRate(it)
                    it.save(flush: true)
                }
            }
            jobs
        } else {
            getJOBResponseList(jobs)
        }
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess(#json['project']) or hasRole('ROLE_ADMIN')")
    def add(def json,SecurityCheck security) {

        SecUser currentUser = cytomineService.getCurrentUser()

        //Start transaction
        Transaction transaction = transactionService.start()

        //Synchronzed this part of code, prevent two job to be add at the same time
        synchronized (this.getClass()) {
            //Add Job
            log.debug this.toString()
            def result = executeCommand(new AddCommand(user: currentUser, transaction: transaction), json)
            def job = result?.data?.job?.id

            //add all job params
            def params = json.params;
            if (params) {
                params.each { param ->
                    log.info "add param = " + param
                    jobParameterService.addJobParameter(job,param.softwareParameter,param.value, currentUser, transaction)
                }
            }

        return result
        }
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkProjectAccess() or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) {
        log.info "update job:"+json
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) {
//        SecUser currentUser = cytomineService.getCurrentUser()
//        def userJob = UserJob.findByJob(Job.read(json.id))
//        if(userJob) {
//            //TODO:: move this in a methode deleteUser in userService
//            SecUserSecRole.findAllBySecUser(userJob).each{
//                it.delete(flush:true)
//            }
//            userJob.delete(flish:true)
//        }
//        //TODO: delete job-parameters
//        //TODO: delete job-data
//        return executeCommand(new DeleteCommand(user: currentUser), json)
        delete(retrieve(json))
    }


    def delete(Job job, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${job.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(Job.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(Job domain, boolean printMessage) {
        //Save new object
        saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, Job], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Job.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(Job domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, Job], printMessage, "Delete", domain.getCallBack())
        //Delete object
        deleteDomain(domain)
        return response
    }

    /**
     * Edit domain from database
     * @param json domain data in json
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Job(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(Job domain, boolean printMessage) {
        log.info "edit="+domain
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, Job], printMessage, "Edit", domain.getCallBack())
        //Save update
        saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    Job createFromJSON(def json) {
        return Job.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        log.info "retrieve="+json
        Job job = Job.get(json.id)
        log.info "job="+job
        if (!job) throw new ObjectNotFoundException("Job " + json.id + " not found")
        return job
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
     List<UserJob> getAllLastUserJob(Project project, Software software) {
         //TODO: inlist bad performance
         List<Job> jobs = Job.findAllWhere('software':software,'status':Job.SUCCESS, 'project':project)
         List<UserJob>  userJob = UserJob.findAllByJobInList(jobs,[sort:'created', order:"desc"])
         return userJob
    }

     private UserJob getLastUserJob(Project project, Software software) {
         List<UserJob> userJobs = getAllLastUserJob(project,software)
         return userJobs.isEmpty()? null : userJobs.first()
    }


    /**
     * If params.project && params.software, get the last userJob from this software from this project
     * If params.job, get userjob with job
     * @param params
     * @return
     */
    public UserJob retrieveUserJobFromParams(def params) {
        log.info "retrieveUserJobFromParams:" + params
        SecUser userJob = null
        if (params.project != null && params.software != null) {
            Project project = Project.read(params.project)
            Software software = Software.read(params.software)
            if(project && software) userJob = getLastUserJob(project, software)
        } else if (params.job != null) {
            Job job = Job.read(params.long('job'))
            if(job) {
                userJob = UserJob.findByJob(job)
            }
        }
        return userJob
    }

    /**
     * Chek if job has reviewed annotation
     */
    public def hasReviewedAnnotation(List<AlgoAnnotation> annotations) {
        List<Long> annotationsId = annotations.collect{ it.id }
        if (annotationsId.isEmpty()) []
        return ReviewedAnnotation.findAllByParentIdentInList(annotationsId)
    }

    /**
     * Delete all annotation created by a user job from argument
     */
    @PreAuthorize("#job.project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    public void deleteAllAlgoAnnotations(Job job) {
        List<Long> usersId = UserJob.findAllByJob(job).collect{ it.id }
        if (usersId.isEmpty()) return
        AlgoAnnotation.executeUpdate("delete from AlgoAnnotation a where a.user.id in (:list)",[list:usersId])
    }

    /**
     * Delete all algo-annotation-term created by a user job from argument
     */
    @PreAuthorize("#job.project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    public void deleteAllAlgoAnnotationsTerm(Job job) {
        List<Long> usersId = UserJob.findAllByJob(job).collect{ it.id }
        if (usersId.isEmpty()) return
        AlgoAnnotationTerm.executeUpdate("delete from AlgoAnnotationTerm a where a.userJob.id IN (:list)",[list:usersId])
    }

    /**
     * Delete all data filescreated by a user job from argument
     */
    @PreAuthorize("#job.project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    public void deleteAllJobData(Job job) {
        List<JobData> jobDatas = JobData.findAllByJob(job)
        List<Long> jobDatasId = jobDatas.collect{ it.id }
        if (jobDatasId.isEmpty()) return
        JobData.executeUpdate("delete from JobData a where a.id IN (:list)",[list:jobDatasId])
    }


    /**
     * Create a new user that will be link with the job and launch the exe with parameters
     * @param job Job to launch
     * @return The job
     */
    @PreAuthorize("#job.project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    public def executeJob(Job job) {
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

    @PreAuthorize("#job.project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
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

    /**
     * Convert jobs list to a simple list with json object and main job properties
     */
    private def getJOBResponseList(List<Job> jobs) {
        def data = []
        jobs.each {
           def job = [:]
            job.id = it.id
            job.status = it.status
            job.number = it.number
            job.created = it.created?.time?.toString()
            job.dataDeleted = it.dataDeleted
            data << job
        }
        return data
    }


    def deleteDependentJobParameter(Job job, Transaction transaction) {
        JobParameter.findAllByJob(job).each {
            jobParameterService.delete(it, transaction, false)
        }
    }

    def deleteDependentJobData(Job job, Transaction transaction) {
        JobData.findAllByJob(job).each {
            jobDataService.delete(it, transaction, false)
        }
    }

    def deleteDependentUserJob(Job job, Transaction transaction) {
        UserJob.findAllByJob(job).each {
            secUserService.delete(it, transaction, false)
        }
    }

}
