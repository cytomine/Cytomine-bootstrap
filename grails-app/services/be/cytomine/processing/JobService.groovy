package be.cytomine.processing

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.command.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.sql.AlgoAnnotationListing
import be.cytomine.sql.ReviewedAnnotationListing
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import groovy.sql.Sql

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
    def annotationListingService
    def dataSource
    def currentRoleServiceProxy
    def securityACLService
    //def softwareService

    def currentDomain() {
        return Job
    }

    def read(def id) {
        def job = Job.read(id)
        if(job) {
            securityACLService.check(job.container(),READ)
        }
        job
    }

    /**
     * List max job for a project and a software
     * Light flag allow to get a light list with only main job properties
     */
    def list(def softwares, def projects, boolean light) {

        def jobs = Job.findAllBySoftwareInListAndProjectInList(softwares, projects, [sort : "created", order : "desc"])

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
        securityACLService.check(json.project,Project, READ)
        securityACLService.checkReadOnly(json.project,Project)
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
        log.info "update"
        securityACLService.check(job.container(),READ)
        securityACLService.checkReadOnly(job.container())
        log.info "securityACLService.check"
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
        securityACLService.check(domain.container(),READ)
        securityACLService.checkReadOnly(domain.container())

        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.software.name]
    }

    List<UserJob> getAllLastUserJob(Project project, Software software) {
        securityACLService.check(project,READ)
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
    public def getReviewedAnnotation(def annotations, def job) {
        List<Long> annotationsId = annotations.collect{ it.id }
        if (annotationsId.isEmpty()) {
            return  []
        }
        ReviewedAnnotationListing al = new ReviewedAnnotationListing(project:job.project.id, parents:annotationsId)


        return annotationListingService.listGeneric(al)
    }

    public boolean hasReviewedAnnotation(def job) {
        def user = UserJob.findByJob(job)
        if(!user) {
           return true
        }

        def annotations = annotationListingService.listGeneric(new AlgoAnnotationListing(project:job.project.id,user:user.id))
        log.info "Job ${job.id} has ${annotations.size()} annotations"
        if (annotations.isEmpty()) return false
        ReviewedAnnotationListing al = new ReviewedAnnotationListing(project:job.project.id, parents:annotations.collect{ it.id })
        def list = annotationListingService.listGeneric(al)
        log.info "Job ${job.id} has ${list.size()} annotations reviewed"
        return !list.isEmpty()
    }

    /**
     * Delete all annotation created by a user job from argument
     */
    public void deleteAllAlgoAnnotations(Job job) {
        securityACLService.check(job.container(),READ)
        List<Long> usersId = UserJob.findAllByJob(job).collect{ it.id }
        if (usersId.isEmpty()) return
        def request = "delete from algo_annotation where user_id in (" + usersId.join(',') +")"
        def sql = new Sql(dataSource)
         sql.execute(request,[])
        try {
            sql.close()
        }catch (Exception e) {}
    }

    /**
     * Delete all algo-annotation-term created by a user job from argument
     */
    public void deleteAllAlgoAnnotationsTerm(Job job) {
        securityACLService.check(job.container(),READ)
        List<Long> usersId = UserJob.findAllByJob(job).collect{ it.id }
        if (usersId.isEmpty()) return
        def request = "delete from algo_annotation_term where user_job_id in ("+ usersId.join(',')+")"
        def sql = new Sql(dataSource)
        sql.execute(request,[])
        try {
            sql.close()
        }catch (Exception e) {}

    }

    /**
     * Delete all data filescreated by a user job from argument
     */
    public void deleteAllJobData(Job job) {
        securityACLService.check(job.container(),READ)
        List<JobData> jobDatas = JobData.findAllByJob(job)
        List<Long> jobDatasId = jobDatas.collect{ it.id }
        if (jobDatasId.isEmpty()) return
        JobData.executeUpdate("delete from JobData a where a.id IN (:list)",[list:jobDatasId])
    }

    public UserJob createUserJob(User user, Job job) {
        securityACLService.check(job.container(),READ)
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

        currentRoleServiceProxy.findCurrentRole(user).each { secRole ->
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
