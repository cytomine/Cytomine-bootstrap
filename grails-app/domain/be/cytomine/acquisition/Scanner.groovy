package be.cytomine.acquisition

class Scanner {

    String brand
    String model

    static constraints = {
    }

    String toString() {
      brand + "-" + model
    }
}
