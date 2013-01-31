package be.cytomine.image.server

import be.cytomine.image.Mime
import be.cytomine.CytomineDomain

/**
 * Association between mime and image server
 */
class MimeImageServer extends CytomineDomain {

    ImageServer imageServer
    Mime mime

    static mapping = {
        id generator: "assigned"
    }
}
