package be.cytomine.processing.job

import be.cytomine.processing.Job
import be.cytomine.processing.SoftwareParameter
import be.cytomine.processing.JobParameter
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.security.UserJob

abstract class AbstractJobService {

    static transactional = true

    public JobParameter createJobParameter(String name, Job job, String value) {
                SoftwareParameter softwareParameter = SoftwareParameter.findBySoftwareAndName(job.software, name)
            JobParameter jobParam = new JobParameter(value: value, job: job, softwareParameter: softwareParameter)
                return  jobParam
 	}

    String[] getParameters(Job job) throws WrongArgumentException{

         Collection<SoftwareParameter> parameters = SoftwareParameter.findAllBySoftware(job.software,[sort: "index",order: "asc"])
         String[] args = new String[parameters.size()*2]
         parameters.eachWithIndex {softParam, i->
             JobParameter jobParam =  JobParameter.findByJobAndSoftwareParameter(job,softParam)
             String value = softParam.defaultValue
             if(jobParam) value = jobParam.value
             else if(softParam.required) throw new WrongArgumentException("Argument "+softParam.name+" is required!")
             args[i*2] = "--"+softParam.getName()
             args[i*2+1] = value
         }
        return args
    }

    String[] getParametersValues(Job job) throws WrongArgumentException{

         Collection<SoftwareParameter> parameters = SoftwareParameter.findAllBySoftware(job.software,[sort: "index",order: "asc"])
         String[] args = new String[parameters.size()]

         parameters.eachWithIndex {softParam, i->
             JobParameter jobParam =  JobParameter.findByJobAndSoftwareParameter(job,softParam)
             String value = softParam.defaultValue
             if(jobParam) value = jobParam.value
             else if(softParam.required) throw new WrongArgumentException("Argument "+softParam.name+" is required!")
             args[i] = value
             println softParam.name + "=" + value
         }
        return args
    }


    void launchAndWaitSoftware(String[] args, Job job) {
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
                                 println "############# JOB:" + line
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
//        job.refresh()
//        job.rate = job.software.service.computeRate(job)
//        job.save(flush: true)
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

    public abstract double computeRate(Job job);

}
