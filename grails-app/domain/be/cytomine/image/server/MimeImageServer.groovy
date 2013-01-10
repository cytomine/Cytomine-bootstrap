package be.cytomine.image.server

import be.cytomine.image.Mime

/**
 * Association between mime and image server
 */
class MimeImageServer {

    ImageServer imageServer
    Mime mime

    static mapping = {
        version false
    }

    static MimeImageServer link(ImageServer imageServer, Mime mime) {
        def mis = MimeImageServer.findByImageServerAndMime(imageServer, mime)
        if (!mis) {
            mis = new MimeImageServer()
            imageServer?.addToMis(mis)
            mime?.addToMis(mis)
            mis.save(flush: true)
        }
        return mis
    }

    static void unlink(ImageServer imageServer, Mime mime) {
        def mis = MimeImageServer.findByImageServerAndMime(imageServer, mime)
        if (mis) {
            imageServer?.removeFromMis(mis)
            mime?.removeFromMis(mis)
            mis.delete(flush: true)
        }
    }


}
