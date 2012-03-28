package be.cytomine.image.server

import be.cytomine.CytomineDomain

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 27/01/11
 * Time: 15:21
 */
class RetrievalServer extends CytomineDomain {

    String description
    String url
    int port = 0

    String toString() { return url; }


    def beforeInsert() {
      super.beforeInsert()
    }

  def beforeValidate() {
    super.beforeValidate()
  }

}
