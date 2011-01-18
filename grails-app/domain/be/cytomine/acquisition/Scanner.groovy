package be.cytomine.acquisition

class Scanner extends Digitizer {

  String maxResolution

  static constraints = {
    maxResolution nullable : true
  }

  String toString() {
    brand + "-" + model
  }
}
