package be.cytomine.acquisition

class Scanner extends Instrument {

  String maxResolution

  static constraints = {
    maxResolution nullable : true
  }

  String toString() {
    brand + "-" + model
  }

  static Scanner createOrGetBasicScanner() {

    println "createOrGetBasicScanner()"
    def scanner = new Scanner(maxResolution:"x40",brand:"brand", model:"model")
    scanner.save(flush : true)
    scanner

  }
}
