package be.cytomine.processing.algorithms.suggestedannotationterm

import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.User

class SuggestedAnnotationTermIndexJob extends Job {

    Project project
    int N = 500
    int indexCompressThreshold = 0
    int maxPercentageSimilarWord = 0
    String kyotoCacheGiga = 3

    static constraints = {
    }

    static mapping = {
    }

    def execute() {
        print "Do someting with  " + this.class.getName() + " !!!"
    }
}
