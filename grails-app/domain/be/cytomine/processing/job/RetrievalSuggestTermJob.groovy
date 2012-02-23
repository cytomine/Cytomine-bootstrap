package be.cytomine.processing.job

import be.cytomine.processing.Job
import be.cytomine.processing.JobParameter
import be.cytomine.security.UserJob
import be.cytomine.processing.SoftwareParameter
import be.cytomine.Exception.WrongArgumentException

class RetrievalSuggestTermJob extends Job {


    def execute() {
        String applicPath = "/home/lrollus/Cytomine/subversion/wiki/cytomine/algo/retrieval/ValidateAnnotationAlgo/out/artifacts/ValidateAnnotationAlgo_jar7/ValidateAnnotationAlgo.jar"

        println "*******************************"
        println "****** Execute Retrieval ******"
        println "*******************************"
        println "Parameter="
        def params = SoftwareParameter.findAllBySoftware(this.software,[sort: "index",order: "asc"])
        String[] args = new String[params.size()+4]

        args[0] = "java"
        args[1] = "-Xmx2G"
        args[2] = "-jar"
        args[3] = applicPath

        params.eachWithIndex {it, i->
            SoftwareParameter softParam = it
            JobParameter jobParam =  JobParameter.findByJobAndSoftwareParameter(this,softParam)

            String value = softParam.defaultValue
            if(jobParam) value = jobParam.value
            else if(softParam.required) throw new WrongArgumentException("Argument "+softParam.name+" is required!")
            args[i+4] = value
            println softParam.name + "=" + value
        }

        println "*******************************"
        println "****** Launch Applic"
        for(int i=0;i<args.length;i++) {
            println "args["+(i-1)+"]="+args[i]
        }
        println "*******************************"
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

      println "###################################################################################"
        println "###################################################################################"
        println "################## ALGO IS FINISH ######################"
        println "###################################################################################"
        println "###################################################################################"

    }

  static RetrievalSuggestTermJob createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static RetrievalSuggestTermJob createFromData(jsonJob) {
        def job = new RetrievalSuggestTermJob()
        getFromData(job, jsonJob)
    }

    static def getFromData(job, jsonJob) {
        (RetrievalSuggestTermJob)Job.getFromData(job,jsonJob)
    }
}
