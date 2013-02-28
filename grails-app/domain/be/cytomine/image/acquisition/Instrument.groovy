package be.cytomine.image.acquisition

import be.cytomine.CytomineDomain

/**
 * @author lrollus
 * Device that allow to digitalized a picture (Scanner, Camera,...)
 */
class Instrument extends CytomineDomain {

    String brand
    String model

    static mapping = {
        id generator: "assigned"
        sort "id"
    }

    String toString() {
        brand + "-" + model
    }

    def beforeInsert() {
        super.beforeInsert()
    }

}
