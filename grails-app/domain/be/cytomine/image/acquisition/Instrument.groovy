package be.cytomine.image.acquisition

import be.cytomine.CytomineDomain

class Instrument extends CytomineDomain {

    String brand
    String model

    static constraints = {
        id(generator: 'assigned', unique: true)
    }

    static mapping = {
        id generator: "assigned"
    }

    String toString() {
        brand + "-" + model
    }

  def beforeInsert() {
      super.beforeInsert()
  }

}
