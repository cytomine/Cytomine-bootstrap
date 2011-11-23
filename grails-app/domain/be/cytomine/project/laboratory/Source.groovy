package be.cytomine.project.laboratory

import be.cytomine.SequenceDomain

class Source extends SequenceDomain {

    String name

    static constraints = {
    }

    String toString() {
        name
    }
}
