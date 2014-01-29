package be.cytomine.laboratory

import be.cytomine.CytomineDomain
import org.jsondoc.core.annotation.ApiObject

/**
 * A source is a real thing that provide sample.
 * E.g. a patient, a mouse,...
 * It is defined to connect cytomine to "outside".
 */
class Source extends CytomineDomain {

    String name
    //TODO: change with has Many
    Sample sample

    static constraints = {
    }

    String toString() {
        name
    }
}
