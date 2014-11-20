package be.cytomine.image.server

import be.cytomine.CytomineDomain

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 27/01/11
 * Time: 15:21
 * Retrieval server provide similar images to an images.
 * It can be used to suggest class for images (=> terms for annotation)
 */
class RetrievalServer extends CytomineDomain {

    String description
    String url
    String path
    int port = 0

    String toString() { return getFullURL(); }

    public String getFullURL() {
        return url + (path?:"")
    }

    static constraints = {
        path nullable: true
    }


    def beforeInsert() {
        super.beforeInsert()
    }

    def beforeValidate() {
        super.beforeValidate()
    }

}
