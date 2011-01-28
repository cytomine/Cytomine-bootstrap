package be.cytomine.api.project

import grails.converters.*
import be.cytomine.project.Annotation
import be.cytomine.project.Scan
import com.vividsolutions.jts.io.WKTReader
import com.vividsolutions.jts.geom.Geometry

class RestAnnotationController {

  def list = {
    println params.idscan
    List<Annotation> data = (params.idscan == null) ? Annotation.list() : (Annotation.findAllByScan(Scan.findById(params.idscan)))
    HashMap jsonMap = getAnnotationsMap(data)
    println data
    withFormat {
      json { render jsonMap as JSON }
      xml { render jsonMap as XML}
    }

  }

  def show = {
    Annotation annotation = Annotation.get(params.idannotation)
    def data = []
    data.add(annotation)
    HashMap jsonMap = getAnnotationsMap(data)

    withFormat {
      json { render jsonMap as JSON }
      xml { render jsonMap as XML}
    }

  }

  def add = {
    println params.location
    Scan scan =  Scan.get(params.idscan)
    if((scan==null)) println "Scan is null"
    else println "Scan is not null"

    Geometry geom = new WKTReader().read(params.location);
    Annotation annotation = new Annotation(name: "toto", location:geom, scan:scan)

    if(annotation.validate())
    {
      annotation.save(flush:true)

      def annotationList = []
      annotationList.add(annotation)
      HashMap jsonMap = getAnnotationsMap(annotationList)

      withFormat {
        json { render jsonMap as JSON }
        xml { render jsonMap as XML}
      }

    }
    else
    {
      println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
      scan.errors.each {
        err -> println err
      }
    }
  }


  def update = {
    println params.location
    Annotation annotation =  Annotation.get(params.idannotation)
    if((annotation==null)) println "Annotation is null"
    else println "Annotation is not null"

    Geometry geom = new WKTReader().read(params.location);
    annotation.location = geom

    if(annotation.validate())
    {
      annotation.save(flush:true)

      def annotationList = []
      annotationList.add(annotation)
      HashMap jsonMap = getAnnotationsMap(annotationList)

      withFormat {
        json { render jsonMap as JSON }
        xml { render jsonMap as XML}
      }

    }
    else
    {
      println("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
      scan.errors.each {
        err -> println err
      }
    }
  }

  def delete = {
    println params.idannotation
    Annotation annotation =  Annotation.get(params.idannotation)
    if((annotation==null)) println "Annotation is null"
    else println "Annotation is not null"

     annotation.delete()

     def annotationList = []
     annotationList.add(annotation)
     HashMap jsonMap = getAnnotationsMap(annotationList)

      withFormat {
        json { render jsonMap as JSON }
        xml { render jsonMap as XML}
      }
  }

  /* Take a List of annotation(s) and return a Map of annotation with only some attribute.
  *  Avoid that the converter go into the geometry object.
  * */
  def getAnnotationsMap(annotationList) {
    if(annotationList==null || annotationList.size()==0)
    {
      HashMap jsonMap = new HashMap()
      jsonMap.annotations = []
      jsonMap

    }
    else
    {
      HashMap jsonMap = new HashMap()
      jsonMap.annotations = annotationList.collect {ann ->
        return [id: ann.id, name: ann.name, location: ann.location.toString(), scan: ann.scan.id]
      }
      jsonMap
    }
  }

}
