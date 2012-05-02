package be.cytomine.processing.job

import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON
import be.cytomine.project.Project
import be.cytomine.ontology.Term

class RetrievalEvolutionJobService extends AbstractJobService{

    static transactional = false
    def springSecurityService
    def cytomineService
    def commandService
    def domainService
    def jobParameterService
    def algoAnnotationTermService

    def init(Job job, UserJob userJob) {
        jobParameterService.add(JSON.parse(createJobParameter("publicKey",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("privateKey",job,userJob.privateKey).encodeAsJSON()))
        //Execute Job
        log.info "Execute Job..."
    }

    def execute(Job job) {

        String applicPath = "algo/retrievalSuggest/ValidateAnnotationAlgo.jar"

        //get job params
        String[] jobParams = getParametersValues(job)
        String[] args = new String[jobParams.length+7]
        //build software params
        args[0] = "java"
        args[1] = "-Xmx2G"
        args[2] = "-cp"
        args[3] = applicPath
        args[4] = "retrieval.algo.suggestAnnotation.SuggestAnnotationEvolution"

        args[5] = job.software.id
        args[6] = UserJob.findByJob(job).user.id


        for(int i=0;i<jobParams.length;i++) {
            args[i+7] = jobParams[i]
        }


        printStartJobInfo(job,args)
        launchAndWaitSoftware(args)
        printStopJobInfo(job,args)
    }
    
    def listAVGEvolution(Job job) {
        List<UserJob> userJobs = UserJob.findAllByJob(job, [sort : "created", order: "desc"])
        return algoAnnotationTermService.listAVGEvolution(userJobs, job.project)
    }

    def listAVGEvolution(Job job, Term term) {
        List<UserJob> userJobs = UserJob.findAllByJob(job, [sort : "created", order: "desc"])
        return algoAnnotationTermService.listAVGEvolution(userJobs, job.project,term)
    }

    @Override
    double computeRate(Job job) {
        if(job.rate==-1) {
            def result = listAVGEvolution(job)
            println "result="+result
            if(!result.isEmpty()) {
                job.rate = result.first().avg
//                job.save(flush: true)
            }
        }
        return job.rate
    }
}
