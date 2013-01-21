package be.cytomine.processing.job

import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON
import be.cytomine.SecurityCheck

class ComputeTermAreaJobService extends AbstractJobService{

    static transactional = false

    def jobParameterService

    def init(Job job, UserJob userJob) {
        jobParameterService.add(JSON.parse(createJobParameter("publicKey",job,userJob.publicKey).encodeAsJSON()),new SecurityCheck())
        jobParameterService.add(JSON.parse(createJobParameter("privateKey",job,userJob.privateKey).encodeAsJSON()),new SecurityCheck())
        //Execute Job
        log.info "Execute Job..."
    }

    def execute(Job job) {

        String applicPath = "algo/computeTermArea/computeTermArea.jar"

        //get job params
        String[] jobParams = getParametersValues(job)
        String[] args = new String[jobParams.length+7]
        //build software params
        args[0] = "java"
        args[1] = "-Xmx1G"
        args[2] = "-cp"
        args[3] = applicPath
        args[4] = "ComputeArea"

        args[5] = job.id
        args[6] = UserJob.findByJob(job).id


        for(int i=0;i<jobParams.length;i++) {
            args[i+7] = jobParams[i]
        }


        printStartJobInfo(job,args)
        launchAndWaitSoftware(args,job)
        printStopJobInfo(job,args)
    }

    @Override
    Double computeRate(Job job) {
        return null;
    }
}
