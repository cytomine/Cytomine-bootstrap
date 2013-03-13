package be.cytomine.image.server

import be.cytomine.CytomineDomain
import be.cytomine.image.Mime

/**
 * Association between mime and image server
 */
class MimeImageServer extends CytomineDomain {

    ImageServer imageServer
    Mime mime

}
