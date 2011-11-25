package be.cytomine.project.laboratory

import be.cytomine.CytomineDomain

class Source extends CytomineDomain {

    String name

    static constraints = {
    }

    String toString() {
        name
    }
}
