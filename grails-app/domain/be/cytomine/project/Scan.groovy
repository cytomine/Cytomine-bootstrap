package be.cytomine.project

import be.cytomine.warehouse.Data
import be.cytomine.acquisition.Scanner

class Scan {
    String filename
    Data data
    Scanner scanner

    static constraints = {
      filename blank : false
      data blank : false
      scanner blank : false
    }
}
