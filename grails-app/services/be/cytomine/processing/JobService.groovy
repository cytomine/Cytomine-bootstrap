package be.cytomine.processing

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import org.codehaus.groovy.grails.web.json.JSONObject

class JobService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def domainService
    def transactionService
    def jobParameterService

    def list() {
        Job.list()
    }

    def read(def id) {
        Job.read(id)
    }

    def list(Software software, boolean light, def max) {
        def jobs = Job.findAllBySoftware(software, [max: max, sort: "created", order: "desc"])
        if(!light) {
            jobs.each {
                if(it.rate==-1 && it.status==Job.SUCCESS) {
                    it.rate = it.software?.service?.computeRate(it)
                    it.save(flush: true)
                }
            }
        }
        else getJOBResponseList(jobs)
    }

    def list(Project project, boolean light, def max) {
        def jobs = Job.findAllByProject(project, [max: max, sort: "created", order: "desc"])
        if(!light) return jobs
        else getJOBResponseList(jobs)
    }

    def list(Software software, Project project, boolean light, def max) {
        def jobs = Job.findAllBySoftwareAndProject(software, project, [max: max, sort: "created", order: "desc"])
        if(!light) {
            jobs.each {
                if(it.rate==-1 && it.status==Job.SUCCESS) {
                    it.rate = it.software?.service?.computeRate(it)
                    it.save(flush: true)
                }
            }
        }
        else getJOBResponseList(jobs)
    }

    private def getJOBResponseList(List<Job> jobs) {
        def data = []
        jobs.each {
           def job = [:]
            job.id = it.id
            job.status = it.status
            job.number = it.number
            job.created = it.created ? it.created.time.toString() : null
            data << job
        }
        return data
    }

    def add(def json) {
        log.info "json="+json
        log.info "cytomineService="+cytomineService
        SecUser currentUser = cytomineService.getCurrentUser()

        //Start transaction
        Transaction transaction = transactionService.start()

        //Synchronzed this part of code, prevent two job to be add at the same time
        synchronized (this.getClass()) {
            //Add Job
            log.debug this.toString()
            def result = executeCommand(new AddCommand(user: currentUser, transaction: transaction), json)
            def job = result?.data?.job?.id
            log.info "job=" + job + " json.params=" + json.params
            //Add annotation-term if term
            if (job) {
                def params = json.params;
                if (params) {
                    params.each { param ->
                        log.info "add param = " + param
                        jobParameterService.addJobParameter(job,param.softwareParameter,param.value, currentUser, transaction)
                    }
                }
            }

        //Stop transaction
        transactionService.stop()

        return result
        }
    }

    def update(def domain, def json) {
        log.info "update job:"+json
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    def delete(def domain, Object json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        //TODO: delete job-parameters
        //TODO: delete job-data
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(Job.createFromDataWithId(json), printMessage)
    }

    def create(Job domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, Job], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Job.get(json.id), printMessage)
    }

    def destroy(Job domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, Job], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Job(), json), printMessage)
    }

    def edit(Job domain, boolean printMessage) {
        log.info "edit="+domain
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, Job], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
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

     List<UserJob> getAllLastUserJob(Project project, Software software, int max) {
         //List<Job> jobs = Job.findAllBySoftwareAndSuccessful(software,true)
         List<Job> jobs = Job.findAllWhere('software':software,'status':Job.SUCCESS, 'project':project)
         //TODO: inlist bad performance
         List<UserJob> userJob = UserJob.findAllByJobInList(jobs,[max:max,sort:'created', order:"desc"])
         return userJob
    }

     List<UserJob> getAllLastUserJob(Project project, Software software) {
         //TODO: inlist bad performance
         List<Job> jobs = Job.findAllWhere('software':software,'status':Job.SUCCESS, 'project':project)
         List<UserJob>  userJob = UserJob.findAllByJobInList(jobs,[sort:'created', order:"desc"])
         return userJob
    }

     UserJob getLastUserJob(Project project, Software software) {
         List<UserJob> userJobs = getAllLastUserJob(project,software)
         return userJobs.isEmpty()? null : userJobs.first()
    }
}
