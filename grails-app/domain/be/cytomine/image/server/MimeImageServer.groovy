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

    /**
     * Add a link between the imageServer and the mime
     * @param imageServer Image server to link with this mime
     * @param mime Mime to link with this image server
     * @return Relation between Image server and mime
     */
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

    /**
     * Remove the link between the imageServer and the mime
     * @param imageServer Image server that will not support mime
     * @param mime Mime to remove from image server
     */
    static void unlink(ImageServer imageServer, Mime mime) {
        def mis = MimeImageServer.findByImageServerAndMime(imageServer, mime)
        if (mis) {
            imageServer?.removeFromMis(mis)
            mime?.removeFromMis(mis)
            mis.delete(flush: true)
        }
    }


}
