package be.cytomine.processing

import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

import static org.springframework.security.acls.domain.BasePermission.READ

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

    def currentDomain() {
        return Job
    }

    def read(def id) {
        def job = Job.read(id)
        if(job) {
            SecurityACL.check(job.container(),READ)
        }
        job
    }

    def list(List<Project> projects) {
        projects.each { project ->
            SecurityACL.check(project,READ)
        }
        Job.findAllByProjectInList(projects,[sort: "created", order: "desc"])
    }

    /**
     * List max job for a project
     * Light flag allow to get a light list with only main job properties
     */
    def list(Project project, boolean light) {
        SecurityACL.check(project,READ)
        def jobs = Job.findAllByProject(project, [sort: "created", order: "desc"])
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
    def list(Software software, Project project, boolean light) {
        SecurityACL.check(project,READ)
        def jobs = Job.findAllBySoftwareAndProject(software, project, [sort: "created", order: "desc"])
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
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecurityACL.check(json.project,Project, READ)
        SecUser currentUser = cytomineService.getCurrentUser()

        //Start transaction
        Transaction transaction = transactionService.start()

        //Synchronzed this part of code, prevent two job to be add at the same time
        synchronized (this.getClass()) {
            //Add Job
            log.debug this.toString()
            def result = executeCommand(new AddCommand(user: currentUser, transaction: transaction),null,json)
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
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Job job, def jsonNewData) {
        SecurityACL.check(job.container(),READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser),job, jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Job domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(domain.container(),READ)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.software.name]
    }

     List<UserJob> getAllLastUserJob(Project project, Software software) {
        SecurityACL.check(project,READ)
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
    public void deleteAllAlgoAnnotations(Job job) {
        SecurityACL.check(job.container(),READ)
        List<Long> usersId = UserJob.findAllByJob(job).collect{ it.id }
        if (usersId.isEmpty()) return
        AlgoAnnotation.executeUpdate("delete from AlgoAnnotation a where a.user.id in (:list)",[list:usersId])
    }

    /**
     * Delete all algo-annotation-term created by a user job from argument
     */
    public void deleteAllAlgoAnnotationsTerm(Job job) {
        SecurityACL.check(job.container(),READ)
        List<Long> usersId = UserJob.findAllByJob(job).collect{ it.id }
        if (usersId.isEmpty()) return
        AlgoAnnotationTerm.executeUpdate("delete from AlgoAnnotationTerm a where a.userJob.id IN (:list)",[list:usersId])
    }

    /**
     * Delete all data filescreated by a user job from argument
     */
    public void deleteAllJobData(Job job) {
        SecurityACL.check(job.container(),READ)
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
    public def executeJob(Job job) {
        SecurityACL.check(job.container(),READ)
        log.info "Create UserJob..."
        UserJob userJob = createUserJob(User.read(springSecurityService.principal.id), job)
        job.software.service.init(job, userJob)

        log.info "Launch async..."
        //backgroundService.execute("RunJobAsynchronously", {
            log.info "Launch thread";
            job.software.service.execute(job)
        //})
        job
    }

    public UserJob createUserJob(User user, Job job) {
        SecurityACL.check(job.container(),READ)
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


    def deleteDependentJobParameter(Job job, Transaction transaction, Task task = null) {
        JobParameter.findAllByJob(job).each {
            jobParameterService.delete(it, transaction, null,false)
        }
    }

    def deleteDependentJobData(Job job, Transaction transaction, Task task = null) {
        JobData.findAllByJob(job).each {
            jobDataService.delete(it, transaction, null,false)
        }
    }

    def deleteDependentUserJob(Job job, Transaction transaction, Task task = null) {
        UserJob.findAllByJob(job).each {
            secUserService.delete(it, transaction,null, false)
        }
    }

}
