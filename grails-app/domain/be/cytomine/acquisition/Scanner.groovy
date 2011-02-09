package be.cytomine.acquisition

class Scanner extends Instrument {

  String maxResolution

  static constraints = {
    maxResolution nullable : true
  }

  String toString() {
    brand + "-" + model
  }

}
