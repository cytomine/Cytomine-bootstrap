package be.cytomine.acquisition

import be.cytomine.SequenceDomain

abstract class Instrument extends SequenceDomain {

  String brand
  String model

  static constraints = {
    id (generator:'assigned' , unique : true)
  }

  String toString() {
    brand + "-" + model
  }


}
