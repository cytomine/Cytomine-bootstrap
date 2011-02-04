package be.cytomine.warehouse

class Data {

  String path
  Mime mime

  static constraints = {
    path (maxSize : 255, blank : false)
    mime blank : false
  }

  String toString() {
    path
  }

  static Data createOrGetBasicData() {

    println "createOrGetBasicData()"
    Mime mime = Mime.createOrGetBasicMime()
    def data = new Data(path:"pathpathpath",mime:mime)
    data.save(flush : true)
    data

  }
}
