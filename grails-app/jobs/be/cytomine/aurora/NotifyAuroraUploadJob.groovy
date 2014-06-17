package be.cytomine.aurora

import be.cytomine.ontology.Property

class NotifyAuroraUploadJob {

    public static String PATIENT_ID = "patient_id"
    public static String SAMPLE_ID = "sample_id"
    public static String IMAGE_TYPE = "image_type"
    public static String VERSION = "version"
    public static String NOTIFICATION = "notification"

    static triggers = {
        //simple name: 'notifyAuroraUpload', startDelay: 1000, repeatInterval: 1000
    }

    def group = "MyGroup"

    def execute() {
        println "hello!"

        //Keys: PATIENT_ID, SAMPLE_ID, IMAGE_TYPE, VERSION, NOTIFICATION


        //Get all images with PATIENT_ID, SAMPLE_ID, IMAGE_TYPE and VERSION WITH NO NOTIFICATION

        //SEND HTTP POST TO AURORA
            //REQUEST: [ {PATIENT_ID: xxx, SAMPLE_ID: xxx, ..., image: {xxx}}, {....}   ]

            //RESPONSE: [ {IMAGE_ID: image, NOTIFICATION: now()} ]

        //FOR EACH RESPONSE, ADD NOTIFICATION property


    }




}
