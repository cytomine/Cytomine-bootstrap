package be.cytomine.processing.job

import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareParameter
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.processing.structure.ConfusionMatrix
import be.cytomine.ontology.Term

class RetrievalSuggestedTermJobService extends AbstractJobService {

    static transactional = false
    def cytomineService
    def commandService
    def domainService
    def jobParameterService
    def jobService
    def algoAnnotationTermService

    def init(Job job, UserJob userJob) {

        /*
           * 0: type (=> cytomine) or standalone if execute with ide/java -jar  => STRING
           * 1: public key
           * 2: private key
           * 3: N value
           * 4: T value
           * 5: Working dir
           * 6: Cytomine Host
           * 7: Force download crop (even if already exist) => BOOLEAN
           * 8: storeName (KYOTOSINGLEFILE)
           * 9: index project (list: x,y,z)
           * 10: search project (only one)
        */
        //Create Job-parameter
//        jobParameterService.add(JSON.parse(createJobParameter("execType",job,"cytomine").encodeAsJSON()))
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
        args[4] = "retrieval.algo.suggestAnnotation.SuggestAnnotationSimple"

        args[5] = job.software.id
        args[6] = UserJob.findByJob(job).user.id

        for(int i=0;i<jobParams.length;i++) {
            args[i+7] = jobParams[i]
        }


        printStartJobInfo(job,args)
        launchAndWaitSoftware(args,job)
        printStopJobInfo(job,args)
    }

    def listAVGEvolution(UserJob userJob) {
        //Get all project userJob
        List userJobs = jobService.getAllLastUserJob(userJob?.job?.project,userJob?.job?.software)
        return algoAnnotationTermService.listAVGEvolution(userJobs, userJob?.job?.project)
    }

    double computeAVG(def userJob) {
       return algoAnnotationTermService.computeAVG(userJob)
   }

   double computeAVGAveragePerClass(def userJob) {
        return  algoAnnotationTermService.computeAVGAveragePerClass(userJob)
  }

    ConfusionMatrix computeConfusionMatrix(List<Term> projectTerms, def userJob) {
       return algoAnnotationTermService.computeConfusionMatrix(projectTerms,userJob)
   }

    double computeRate(Job job) {
        if(job.rate==-1 && job.status==Job.SUCCESS) {
            try {
            job.rate = computeAVG(UserJob.findByJob(job))
            }catch(Exception e) {
                log.warn "computeRate is null:"+e.toString()
                job.rate = 0
            }
        }
        return job.rate
    }       
}
