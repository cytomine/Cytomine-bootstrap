package be.cytomine.processing.algorithms.myDetection

import be.cytomine.processing.Job

class MyDetectionPredictJob  extends Job {

    MyDetectionLearnJob myDetectionLearnJob

    static constraints = {
        myDetectionLearnJob (nullable: false)
    }

    def execute() {
        if (!myDetectionLearnJob.successful) {
            this.successful = false
            this.save()
            print "Can't launch " + this.class.getName() + "(job : "+ this.id +") because " + myDetectionLearnJob.class.getName() + "(job : "+ this.id +") was not successful"
        }
        print "Do something with  " + this.class.getName() + " !!!"
    }

}
