package be.cytomine.processing.job

import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON
import be.cytomine.SecurityCheck

class RetrievalEvolutionJobService extends AbstractJobService{

    static transactional = false
    def springSecurityService
    def cytomineService
    def commandService
    def domainService
    def jobParameterService
    def algoAnnotationTermService

    def init(Job job, UserJob userJob) {
        jobParameterService.add(JSON.parse(createJobParameter("publicKey",job,userJob.publicKey).encodeAsJSON()),new SecurityCheck())
        jobParameterService.add(JSON.parse(createJobParameter("privateKey",job,userJob.privateKey).encodeAsJSON()),new SecurityCheck())
        //Execute Job
        log.info "Execute Job..."
    }

    def execute(Job job) {

        String applicPath = "algo/retrievalSuggest/ValidateAnnotationAlgo.jar"

        //get job params
        String[] jobParams = getParametersValues(job)
        String[] args = new String[jobParams.length+8]
        //build software params
        args[0] = "java"
        args[1] = "-Djava.library.path=/usr/local/lib"
        args[2] = "-Xmx2G"
        args[3] = "-cp"
        args[4] = applicPath
        args[5] = "retrieval.algo.suggestAnnotation.SuggestAnnotationEvolution"

        args[6] = job.software.id
        args[7] = UserJob.findByJob(job).user.id

        log.info "command="+ Arrays.toString(args)
        for(int i=0;i<jobParams.length;i++) {
            args[i+8] = jobParams[i]
        }


        printStartJobInfo(job,args)
        launchAndWaitSoftware(args,job)
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
    Double computeRate(Job job) {
        if(job.rate==-1 && job.status==Job.SUCCESS) {
            def result = listAVGEvolution(job)
            log.info "result="+result
            if(!result.isEmpty()) {
                job.rate = result.first().avg
//                job.save(flush: true)
            }
        }
        return job.rate
    }
}
