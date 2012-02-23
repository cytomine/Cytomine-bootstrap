package be.cytomine.processing

import grails.converters.JSON
import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException

class JobParameter extends CytomineDomain{

    String value

    static belongsTo = [job: Job, softwareParameter : SoftwareParameter]

    static constraints = {
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + JobParameter.class
        JSON.registerObjectMarshaller(JobParameter) {
            def jobParameter = [:]
            jobParameter.id = it.id
            jobParameter.value = it.value
            jobParameter.job = it.job.id
            SoftwareParameter softwareParam =  it.softwareParameter
            jobParameter.softwareParameter = softwareParam.id
            jobParameter.name = softwareParam.name
            jobParameter.type = softwareParam.type
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
}
