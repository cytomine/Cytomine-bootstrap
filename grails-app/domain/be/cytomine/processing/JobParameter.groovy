package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON
import org.apache.log4j.Logger

class JobParameter  extends CytomineDomain implements Comparable{

    String value

    static belongsTo = [job: Job, softwareParameter : SoftwareParameter]

    static constraints = {
    }

    static mapping = {
        id generator: "assigned"
        softwareParameter fetch: 'join'
    }

     void checkAlreadyExist() {
        JobParameter.withNewSession {
            JobParameter jobParamAlreadyExist=JobParameter.findByJobAndSoftwareParameter(job,softwareParameter)
            if(jobParamAlreadyExist!=null && (jobParamAlreadyExist.id!=id))  throw new AlreadyExistException("Parameter " + softwareParameter?.name + " already exist fro job " + job?.id)
        }
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + JobParameter.class)
        JSON.registerObjectMarshaller(JobParameter) {
            def jobParameter = [:]
            jobParameter.id = it.id
            jobParameter.value = it.value
            jobParameter.job = it.job.id
            SoftwareParameter softwareParam =  it.softwareParameter
            jobParameter.softwareParameter = softwareParam.id
            jobParameter.name = softwareParam.name
            jobParameter.type = softwareParam.type
            jobParameter.index = softwareParam.index
            jobParameter.uri = softwareParam.uri
            jobParameter.uriPrintAttribut = softwareParam.uriPrintAttribut
            jobParameter.uriSortAttribut = softwareParam.uriSortAttribut
            return jobParameter
        }
    }

    static JobParameter createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static JobParameter createFromData(jsonJobParameter) {
        def jobParameter = new JobParameter()
        getFromData(jobParameter, jsonJobParameter)
    }

    static JobParameter getFromData(jobParameter, jsonJobParameter) {
        if (!jsonJobParameter.value.toString().equals("null"))
            jobParameter.value = jsonJobParameter.value

        jobParameter.job = Job.get(jsonJobParameter.job.toString())
        jobParameter.softwareParameter = SoftwareParameter.get(jsonJobParameter.softwareParameter.toString())

        if(!jobParameter.job) throw new WrongArgumentException("Job ${jsonJobParameter.job.toString()} doesn't exist!")
        if(!jobParameter.softwareParameter) throw new WrongArgumentException("SoftwareParameter ${jsonJobParameter.softwareParameter.toString()} doesn't exist!")
        return jobParameter;
    }

    int compareTo(Object t) {
        if(this.softwareParameter.index<t.softwareParameter.index) return -1
        else if(this.softwareParameter.index>t.softwareParameter.index) return 1
        else return 0
    }
}
