package be.cytomine.processing

import be.cytomine.CytomineDomain

/**
 * TODOSTEVBEN: doc
 */
class ProcessingServer extends CytomineDomain {

    String url

    static constraints = {
        url nullable: false
    }
}
