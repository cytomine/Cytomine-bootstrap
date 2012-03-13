package be.cytomine.processing.job

import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON
import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareParameter

class RetrievalSuggestedTermJobService extends AbstractJobService {

    static transactional = false
    def cytomineService
    def commandService
    def domainService
    def jobParameterService

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
//        jobParameterService.add(JSON.parse(createJobParameter("N",job, "500").encodeAsJSON()))
//        jobParameterService.add(JSON.parse(createJobParameter("T",job, "5").encodeAsJSON()))
//        jobParameterService.add(JSON.parse(createJobParameter("workingDir",job, "algo/retrievalSuggest/suggest/").encodeAsJSON()))
//        jobParameterService.add(JSON.parse(createJobParameter("cytomineHost",job, "http://localhost:8080").encodeAsJSON()))
//        jobParameterService.add(JSON.parse(createJobParameter("forceDownloadCrop",job, "false").encodeAsJSON()))
//        jobParameterService.add(JSON.parse(createJobParameter("storeName",job, "KYOTOSINGLEFILE").encodeAsJSON()))
//        jobParameterService.add(JSON.parse(createJobParameter("indexProject",job, "57").encodeAsJSON()))
//        jobParameterService.add(JSON.parse(createJobParameter("searchProject",job, "57").encodeAsJSON()))
        //Execute Job
        log.info "Execute Job..."
    }

    def execute(Job job) {

        String applicPath = "algo/retrievalSuggest/ValidateAnnotationAlgo.jar"

        //get job params
        String[] jobParams = getParametersValues(job)
        String[] args = new String[jobParams.length+4]
        //build software params
        args[0] = "java"
        args[1] = "-Xmx2G"
        args[2] = "-jar"
        args[3] = applicPath

        for(int i=0;i<jobParams.length;i++) {
            args[i+4] = jobParams[i]
        }


        printStartJobInfo(job,args)
        launchAndWaitSoftware(args)
        printStopJobInfo(job,args)
    }
}
