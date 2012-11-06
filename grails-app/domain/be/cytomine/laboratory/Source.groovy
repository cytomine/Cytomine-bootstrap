package be.cytomine.laboratory

import be.cytomine.CytomineDomain

class Source extends CytomineDomain {

    String name
    Sample sample

    static constraints = {
    }

    String toString() {
        name
    }
}
