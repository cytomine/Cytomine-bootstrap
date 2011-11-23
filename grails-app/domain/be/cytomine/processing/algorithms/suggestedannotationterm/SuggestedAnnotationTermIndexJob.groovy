package be.cytomine.processing.algorithms.suggestedannotationterm

import be.cytomine.processing.Job
import be.cytomine.project.Project

class SuggestedAnnotationTermIndexJob extends Job {


    int N = 500
    int indexCompressThreshold = 0
    int maxPercentageSimilarWord = 0
    String kyotoCacheGiga = "3G"

    //We can index annotation from multiple project (from same ontology)
    static hasMany = [projects: Project]

    static constraints = {
    }

    static mapping = {
    }

    def execute() {
        print "Do someting with  " + this.class.getName() + " !!!"
    }
}
