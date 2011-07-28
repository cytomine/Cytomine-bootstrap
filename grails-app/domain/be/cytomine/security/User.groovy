package be.cytomine.security

import grails.converters.JSON
import be.cytomine.ontology.Ontology
import be.cytomine.image.AbstractImage
import be.cytomine.project.Slide
import be.cytomine.project.Project
import be.cytomine.project.ProjectGroup
import be.cytomine.image.AbstractImageGroup
import org.perf4j.StopWatch
import org.perf4j.LoggingStopWatch

class User extends SecUser {

  def springSecurityService

  String firstname
  String lastname
  String email
  String color

  int transaction

  static constraints = {
    firstname blank : false
    lastname blank : false
    email (blank : false , email : true)
    color (blank : false, nullable : true)
  }

  String toString() {
    firstname + " " + lastname + " (" + username + ")"
  }

  def groups() {
    return userGroup.collect{
      it.group
    }
  }

  def ontologies() {
    def ontologies = []
    //add ontology created by this user
    if(this.version!=null) ontologies.addAll(Ontology.findAllByUser(this))
    //add ontology from project which can be view by this user
    def project = this.projects();

    project.each { proj ->
      Ontology ontology = proj.ontology
      if(!ontologies.contains(ontology))
        ontologies << ontology
    }
    ontologies
  }


  def projects() {
    def c = ProjectGroup.createCriteria()

    if(userGroup==null || userGroup.size()==0) return []
    def projects = c {
      inList("group.id", userGroup.collect {it.groupId})
      projections {
        groupProperty("project")
      }
    }
    projects
  }

  def abstractimages() {
    def abstractImages = []
    if (userGroup.size() > 0) {
      abstractImages = AbstractImageGroup.createCriteria().list {
        inList("group.id", userGroup.collect{it.group.id})
        projections {
          groupProperty('abstractimage')
        }
      }
    }
    abstractImages

  }


  def abstractimage(int max, int first, String col, String order, String filename, Date dateAddedStart, Date dateAddedStop) {

    AbstractImage.createCriteria().list(offset:first, max:max ,sort:col, order:order){
      inList("id", AbstractImageGroup.createCriteria().list {
        inList("group.id", userGroup.collect{it.group.id})
        projections {
          groupProperty('abstractimage.id')
        }
      })
      projections {
        groupProperty('abstractimage')
      }
      ilike("filename","%"+filename+"%")
      between('created',dateAddedStart, dateAddedStop)

    }

  }

  def slides() {
    AbstractImage.createCriteria().list{
      inList("id", AbstractImageGroup.createCriteria().list {
        inList("group.id", userGroup.collect{it.group.id})
        projections {
          groupProperty('abstractimage.id')
        }
      })
      projections {
        groupProperty('slide')
      }
    }
  }

  def slides(int max, int first, String col, String order) {
    AbstractImage.createCriteria().list(offset:first, max:max ,sort:col, order:order){
      inList("id", AbstractImageGroup.createCriteria().list {
        inList("group.id", userGroup.collect{it.group.id})
        projections {
          groupProperty('abstractimage.id')
        }
      })
      projections {
        groupProperty('slide')
      }
    }
  }

  static User getFromData(User user, jsonUser) {
    user.username = jsonUser.username
    user.firstname = jsonUser.firstname
    user.lastname = jsonUser.lastname
    user.email = jsonUser.email
    user.password = user.springSecurityService.encodePassword(jsonUser.password)
    user.enabled = true
//    user.created = (!jsonUser.created.toString().equals("null"))  ? new Date(Long.parseLong(jsonUser.created)) : null
    //    user.updated = (!jsonUser.updated.toString().equals("null"))  ? new Date(Long.parseLong(jsonUser.updated)) : null
    return user;
  }

  static User createFromData(data) {
    getFromData(new User(), data)
  }

  static void registerMarshaller() {
    println "Register custom JSON renderer for " + User.class
    JSON.registerObjectMarshaller(User) {
      def returnArray = [:]
      returnArray['id'] = it.id
      returnArray['username'] = it.username
      returnArray['firstname'] = it.firstname
      returnArray['lastname'] = it.lastname
      returnArray['email'] = it.email
      returnArray['password'] = "******"
      returnArray['color'] = it.color

      returnArray['created'] = it.created? it.created.time.toString() : null
      returnArray['updated'] = it.updated? it.updated.time.toString() : null

      return returnArray
    }
  }


}
