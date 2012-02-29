package be.cytomine.processing.job

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.processing.SoftwareParameter
import be.cytomine.processing.JobParameter
import be.cytomine.Exception.WrongArgumentException

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
