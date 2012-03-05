package be.cytomine.processing.job

import be.cytomine.processing.Job

class RetrievalSuggestedTermJobService extends AbstractJobService {

    static transactional = true
    def cytomineService
    def commandService
    def domainService

    def execute(Job job) {

        String applicPath = "algo/retrievalSuggest/ValidateAnnotationAlgo.jar"

        File dir1 = new File (".");
        System.out.println ("Current dir : " + dir1.getCanonicalPath());
        //get job params
        String[] jobParams = getParams(job)
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
