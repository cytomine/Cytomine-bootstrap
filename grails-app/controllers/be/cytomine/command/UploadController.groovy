package be.cytomine.command

import grails.converters.JSON

class UploadController {

  static allowedMethods = [image:'POST']

  def image = {
    def f = request.getFile('file')

    if(!f.empty) {
      println "not empty"
      f.transferTo( new File('/tmp/' + f.originalFilename))
    }
    else {
      response.status = 400;
      render ""
    }

    def response = [:]
    response.status = 200;
    response.name = f.originalFilename
    response.size = f.size
    response.type = f.contentType
    render response as JSON
  }
}
