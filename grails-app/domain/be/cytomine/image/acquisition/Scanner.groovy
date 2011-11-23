package be.cytomine.image.acquisition

class Scanner extends Instrument {

    String maxResolution

    static constraints = {
        maxResolution nullable: true
        id(generator: 'assigned', unique: true)
    }

    String toString() {
        brand + "-" + model + " with maxResolution = " + maxResolution
    }

}
