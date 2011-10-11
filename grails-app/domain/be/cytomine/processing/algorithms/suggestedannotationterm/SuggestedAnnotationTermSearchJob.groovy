package be.cytomine.processing.algorithms.suggestedannotationterm

import be.cytomine.processing.Job
import be.cytomine.project.Project

class SuggestedAnnotationTermSearchJob extends Job {

    Project project
    int N = 500
    int indexCompressThreshold = 0
    int maxPercentageSimilarWord = 0
    String kyotoCacheGiga = "3G"

    be.cytomine.processing.algorithms.suggestedannotationterm.SuggestedAnnotationTermIndexJob suggestedAnnotationTermIndexJob

    static constraints = {
        suggestedAnnotationTermIndexJob (nullable: false)
    }

    def execute() {
        if (!suggestedAnnotationTermIndexJob.successful) {
            this.successful = false
            this.save()
            print "Can't launch " + this.class.getName() + "(job : "+ this.id +") because " + suggestedAnnotationTermIndexJob.class.getName() + "(job : "+ this.id +") was not successful"
        }
        print "Do something with  " + this.class.getName() + " !!!"
    }

}
