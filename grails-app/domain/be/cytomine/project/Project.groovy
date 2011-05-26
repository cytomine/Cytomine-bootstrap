package be.cytomine.project

import grails.converters.JSON
import be.cytomine.SequenceDomain
import be.cytomine.rest.UrlApi
import be.cytomine.ontology.Ontology
import be.cytomine.image.ImageInstance
import be.cytomine.image.AbstractImage
import be.cytomine.ontology.Annotation

class Project extends SequenceDomain {

  String name
  Ontology ontology

  static hasMany = [projectGroup:ProjectGroup]

  static constraints = {
    name ( maxSize : 100, unique : true, blank : false)
  }

  String toString() {
    name
  }

  def imagesinstance() {
    return ImageInstance.findAllByProject(this)
  }

  def abstractimages() {
    def images = []
    def imagesinstance = this.imagesinstance()
    imagesinstance.each { imageinstance ->
      images << imageinstance.baseImage
    }
    return images
  }

  def annotations() {
    def annotations = []
    this.imagesinstance().each { img ->
      def imageAnnotations = Annotation.findAllByImage(img)
      imageAnnotations.each { annotation ->
        annotations << annotation
      }
    }
    return annotations
  }
  def slides() {
    def slides = []
    this.abstractimages().each { img ->
      if(!slides.contains(img.slide))
          slides << img.slide
    }
    return slides
  }


  def groups() {
    return projectGroup.collect{
      it.group
    }
  }



  def users() {
    def users = []
    projectGroup.each { projGroup ->
      projGroup.group.users().each { user ->
        if(!users.contains(user))
          users << user
      }
    }
    users
  }

  static Project createProjectFromData(jsonProject) {
    def project = new Project()
    getProjectFromData(project,jsonProject)
  }

  static Project getProjectFromData(project,jsonProject) {
    String name = jsonProject.name.toString()
    /*println "jsonProject.name=" + jsonProject.name
    println "jsonProject.name==null" + (jsonProject.name==null)
    println "jsonProject.name.type" + jsonProject.name.class
    println "jsonProject.name.equals(null)" + jsonProject.name.equals("null")
    println "jsonProject.name.toString().equals(null)" + jsonProject.name.toString().equals("null")
    println "isNull(String key) " + jsonProject.isNull(jsonProject.name)
    println "isNull(String key) " + jsonProject.isNull(name)
    println "isNull(String key) " + jsonProject.isNull("name")  */
    if(!name.equals("null"))
      project.name = jsonProject.name.toUpperCase()
    else throw new IllegalArgumentException("Project name cannot be null")
    if (jsonProject.ontology)
      project.ontology = Ontology.read(jsonProject.ontology)
    return project;
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + Project.class
    JSON.registerObjectMarshaller(Project) {
      def returnArray = [:]
      returnArray['class'] = it.class
      returnArray['id'] = it.id
      returnArray['name'] = it.name
      returnArray['ontology'] = it.ontology? it.ontology.id : null
      returnArray['ontologyURL'] = UrlApi.getOntologyURLWithOntologyId(it.ontology?.id)
      returnArray['abstractimageURL'] = UrlApi.getAbstractImageURLWithProjectId(it.id)
      returnArray['imageinstanceURL'] = UrlApi.getImageInstanceURLWithProjectId(it.id)
      returnArray['termURL'] = UrlApi.getTermsURLWithOntologyId(it.ontology?.id)
      returnArray['userURL'] = UrlApi.getUsersURLWithProjectId(it.id)
      returnArray['users'] = it.users().collect { it.id }


      try {returnArray['numberOfSlides'] = it.slides().size()}catch(Exception e){returnArray['numberOfSlides']=-1}
      try {returnArray['numberOfImages'] = it.imagesinstance().size()}catch(Exception e){returnArray['numberOfImages']=-1}
      try {returnArray['numberOfAnnotations'] = it.annotations().size()}catch(Exception e){returnArray['numberOfAnnotations']=-1}

      returnArray['created'] = it.created? it.created.time.toString() : null
      returnArray['updated'] = it.updated? it.updated.time.toString() : null
      return returnArray
    }
  }
}
