package be.cytomine.image.acquisition

import be.cytomine.CytomineDomain

abstract class Instrument extends CytomineDomain {

    String brand
    String model

    static constraints = {
        id(generator: 'assigned', unique: true)
    }

    String toString() {
        brand + "-" + model
    }

  def beforeInsert() {
      super.beforeInsert()
  }

}
