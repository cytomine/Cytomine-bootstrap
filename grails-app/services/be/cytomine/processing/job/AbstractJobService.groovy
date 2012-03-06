package be.cytomine.processing.job

import be.cytomine.processing.Job
import be.cytomine.processing.SoftwareParameter
import be.cytomine.processing.JobParameter
import be.cytomine.Exception.WrongArgumentException

abstract class AbstractJobService {

    static transactional = true

    String[] getParams(Job job) throws WrongArgumentException{

         def params = SoftwareParameter.findAllBySoftware(job.software,[sort: "index",order: "asc"])
         String[] args = new String[params.size()]

         params.eachWithIndex {it, i->
             SoftwareParameter softParam = it
             JobParameter jobParam =  JobParameter.findByJobAndSoftwareParameter(job,softParam)

             String value = softParam.defaultValue
             if(jobParam) value = jobParam.value
             else if(softParam.required) throw new WrongArgumentException("Argument "+softParam.name+" is required!")
             args[i] = value
             println softParam.name + "=" + value
         }

        return args
    }


    void launchAndWaitSoftware(String[] args) {
         Runtime runtime = Runtime.getRuntime();
         final Process process = runtime.exec(args);


             // See algo log in Cytomine log (for debug!)
             new Thread() {
                 public void run() {
                     try {
                         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                         String line = "";
                         try {
                             while((line = reader.readLine()) != null) {
                                 //println line
                             }
                         } finally {
                             reader.close();
                         }
                     } catch(IOException ioe) {
                         ioe.printStackTrace();
                     }
                 }
             }.start();

             new Thread() {
                 public void run() {
                     try {
                         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                         String line = "";
                         try {
                             while((line = reader.readLine()) != null) {
                                 println line
                             }
                         } finally {
                             reader.close();
                         }
                     } catch(IOException ioe) {
                         ioe.printStackTrace();
                     }
                 }
             }.start();
            process.waitFor();
    }




    void printStartJobInfo(Job job, String[] args) {
         println "*******************************"
         println "****** Execute " + job?.software?.name
         println "*******************************"


         println "*******************************"
         println "****** Params:"
         for(int i=0;i<args.length;i++) {
             println "args["+(i-1)+"]="+args[i]
         }
         println "*******************************"

    }

    void printStopJobInfo(Job job, String[] args) {
         println "###################################################################################"
         println "###################################################################################"
         println "################## ALGO IS FINISH: " + job?.software?.name
         println "###################################################################################"
         println "###################################################################################"

         println "****** Params:"
         for(int i=0;i<args.length;i++) {
             println "args["+(i-1)+"]="+args[i]
         }
        println "###################################################################################"
        println "###################################################################################"
        println "###################################################################################"
    }

}
