package be.cytomine.image.server

import be.cytomine.SequenceDomain

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 27/01/11
 * Time: 15:21
 */
class RetrievalServer extends SequenceDomain {

    String description
    String url
    int port = 0

    String toString() { return url; }

}
