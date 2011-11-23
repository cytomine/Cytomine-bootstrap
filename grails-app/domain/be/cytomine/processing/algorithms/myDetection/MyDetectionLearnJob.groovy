package be.cytomine.processing.algorithms.myDetection

import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.User

class MyDetectionLearnJob extends Job {

    Project project

    //Features
    Boolean doRGB
    Boolean doHSV
    Boolean doGRAY
    Boolean doEDGE
    Boolean doLBP

    int subwindowWidth = 21
    int subwindowHeight = 21
    int tree = 200
    int bound = 5
    int split = 10
    int ratio = 2

    static hasMany = [users: User, terms: Term]

    static constraints = {
    }

    static mapping = {
    }

    def execute() {
        print "Do someting with  " + this.class.getName() + " !!!"
    }
}
