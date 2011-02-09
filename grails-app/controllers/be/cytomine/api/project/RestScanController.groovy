package be.cytomine.api.project

import be.cytomine.project.Image
import grails.converters.*
import be.cytomine.project.Project

class RestScanController {

  def index = {
    redirect(controller: "image")
  }

  def springSecurityService

  /* REST API */

  def list = {
    def data = [:]
    data.scan = Image.list()
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }

  def show = {
    if(params.id && Image.exists(params.id)) {
      def data = Image.findById(params.id)
      withFormat {
        json { render data as JSON }
        xml { render data as XML}
      }
    } else {
      response.status = 404
      render ""
    }


  }

  def showByProject = {
    if(params.id && Project.exists(params.id)) {
      def scan = []
      Project.findAllById(params.id).each {
        it.projectSlide.each { ps ->
          ps.slide.scan.each { sc ->
            scan << sc
          }
        }
      }
      def resp = [ scan : scan]

      withFormat {
        json { render resp as JSON }
        xml { render resp as XML}
      }
    } else {
      response.status = 404
      render ""
    }
  }


}
