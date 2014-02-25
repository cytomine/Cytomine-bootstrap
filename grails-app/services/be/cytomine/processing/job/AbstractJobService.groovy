package be.cytomine.processing.job

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.processing.Job
import be.cytomine.processing.JobParameter
import be.cytomine.processing.SoftwareParameter
import be.cytomine.security.UserJob

/**
 * Each software should be linked with a 'job service'.
 * This class provide usefull method for these services.
 */
abstract class AbstractJobService {

    static transactional = true

    /**
     * Add a value for a parameter from a job
     * @param name Parameter key
     * @param job Job parameter
     * @param value Value for the parameter key of this job
     * @return The new parameter
     */
    public JobParameter createJobParameter(String name, Job job, String value) {
        SoftwareParameter softwareParameter = SoftwareParameter.findBySoftwareAndName(job.software, name)
        JobParameter jobParam = new JobParameter(value: value, job: job, softwareParameter: softwareParameter)
        return jobParam
    }

    /**
     *
     * @param job
     * @return
     * @throws WrongArgumentException
     */
    String[] getParameters(Job job) throws WrongArgumentException {

        Collection<SoftwareParameter> parameters = SoftwareParameter.findAllBySoftware(job.software, [sort: "index", order: "asc"])
        String[] args = new String[parameters.size() * 2]
        parameters.eachWithIndex {softParam, i ->
            JobParameter jobParam = JobParameter.findByJobAndSoftwareParameter(job, softParam)
            String value = softParam.defaultValue
            if (jobParam) value = jobParam.value
            else if (softParam.required) throw new WrongArgumentException("Argument " + softParam.name + " is required!")
            args[i * 2] = "--" + softParam.getName()
            args[i * 2 + 1] = value
        }
        return args
    }

    /**
     * Get all parameters value for a specific software instance
     * Check if all job parameter are well defined and fill default value if necessary
     * @param job Software instance
     * @return Parameters array (in software parameter order)
     */
    String[] getParametersValues(Job job) throws WrongArgumentException {

        //Get all parameter template ordered by indx
        Collection<SoftwareParameter> parameters = SoftwareParameter.findAllBySoftware(job.software, [sort: "index", order: "asc"])
        String[] args = new String[parameters.size()]

        //Fill default value if no value specified, check if mandatory value are well defined
        parameters.eachWithIndex {softParam, i ->
            JobParameter jobParam = JobParameter.findByJobAndSoftwareParameter(job, softParam)
            String value = softParam.defaultValue
            if (jobParam) value = jobParam.value
            else if (softParam.required) throw new WrongArgumentException("Argument " + softParam.name + " is required!")
            args[i] = value
            log.info softParam.name + "=" + value
        }
        return args
    }

    /**
     * Launch a software instance with all arguments
     * Only works if software is on the same computer
     * First argument must be software path
     * @param args Job argument
     * @param job Software instance that will be launch
     */
    void launchSoftware(String[] args, Job job) {
        Runtime runtime = Runtime.getRuntime();
        final Process process = runtime.exec(args);

        // See algo log in Cytomine log (for debug!)
        new Thread() {
            public void run() {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = "";
                    try {
                        while ((line = reader.readLine()) != null) {
                            log.info "############# JOB:" + line
                        }
                    } finally {
                        reader.close();
                    }
                } catch (IOException ioe) {
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
                        while ((line = reader.readLine()) != null) {
                            log.error "############# JOB ERROR:" + line
                        }
                    } finally {
                        reader.close();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }.start();
//        process.waitFor();
//        job.refresh()
//        job.rate = job.software.service.computeRate(job)
//        job.save(flush: true)
    }

    void printStartJobInfo(Job job, String[] args) {
        log.info "*******************************"
        log.info "****** Execute " + job?.software?.name
        log.info "*******************************"
        log.info "*******************************"
        log.info "****** Params:"
        for (int i = 0; i < args.length; i++) {
            log.info "args[" + (i - 1) + "]=" + args[i]
        }
        log.info "*******************************"

    }

    void printStopJobInfo(Job job, String[] args) {
        log.info "###################################################################################"
        log.info "###################################################################################"
        log.info "################## ALGO IS FINISH: " + job?.software?.name
        log.info "###################################################################################"
        log.info "###################################################################################"

        log.info "****** Params:"
        for (int i = 0; i < args.length; i++) {
            log.info "args[" + (i - 1) + "]=" + args[i]
        }
        log.info "###################################################################################"
        log.info "###################################################################################"
        log.info "###################################################################################"
    }

    /**
     * Generic method that will compute rate for a software
     * A rate is a 'success value' specific for this software
     * E.g. If software suggest term for annotation rate could be (correct annotation prediction / annotation number)
     */
    public abstract Double computeRate(Job job);

    /**
     * Init job thx to user job keys (keys,...)
     * We may create add generic parameters
     */
    public abstract def init(Job job, UserJob userJob);

    /**
     * Launch the job
     * This method must retrieve all parameters, format them and execute job
     */
    public abstract def execute(Job job, UserJob userJob, boolean preview);

    /**
     * Indicates if a preview is available for the software
     * @return
     */
    public boolean previewAvailable() {
        return false
    }

    /**
     * Return the ROI used for the preview
     * @param job
     * @return
     */
    public def getPreviewROI(Job job) {
        throw new ObjectNotFoundException("Preview roi not available")
    }

    /**
     * Return the ROI used for the preview
     * @param job
     * @return
     */
    public def getPreview(Job job) {
        throw new ObjectNotFoundException("Preview not available")
    }

    protected def createArgsArray(Job job) {
        String[] args = job.software.executeCommand.split(" ")
        return args
    }

}
