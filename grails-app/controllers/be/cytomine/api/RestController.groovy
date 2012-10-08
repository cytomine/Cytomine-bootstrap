package be.cytomine.api

import be.cytomine.Exception.CytomineException
import be.cytomine.test.HttpClient
import grails.converters.JSON
import grails.converters.XML
import org.codehaus.groovy.grails.web.json.JSONArray

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class RestController {

    def sessionFactory
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    def springSecurityService
    int idUser

    static int NOT_FOUND_CODE = 404

    def transactionService


    def add(def service, def json) {
        try {
            if (json instanceof JSONArray) {
                responseResult(addMultiple(service,json))
            } else {
                responseResult(addOne(service,json))
            }
        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def addOne(def service, def json) {
          return service.add(json)
    }

    def addMultiple(def service, def json) {
        def result = [:]
        result.data = []
        int i = 0
        json.each {
            def resp = addOne(service, it)  //TODO: when exception here, what should we do? For the time being, stop everything and response error
            result.data << resp
            if(i%100==0) cleanUpGorm()
            i++
        }
        cleanUpGorm()
        result.status = 200
        return result
    }

    def cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()

    }

    def update(def service, def json) {
        try {
            def domain = service.retrieve(json)
            def result = service.update(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def delete(def service, def json) {
        try {
            def domain = service.retrieve(json)
            def result = service.delete(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

  protected def response(data) {
        withFormat {
            json { render data as JSON }
            jsonp { 
				response.contentType = 'application/javascript'
				render "${params.callback}(${data as JSON})" 
			}
            xml { render data as XML}
        }
    }

  protected  def responseResult(result) {
        response.status = result.status
        withFormat {
            json { render result.data as JSON }
            xml { render result.data as XML}
        }
    }

  protected def responseSuccess(data, code) {
        response(data, code)
    }

  protected  def responseSuccess(data) {
        response(data)
    }

  protected  def response(data, code) {
        response.status = code
        response(data)
    }

  protected  def responseNotFound(className, id) {
        log.info "responseNotFound"
        log.error className + " Id " + id + " don't exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id : " + id)
        }
    }

  protected def responseNotFound(className, filter, id) {
        log.error className + ": " + filter + " " + id + " don't exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id " + filter + " : " + id)
        }
    }

  protected def responseNotFound(className, filter1, filter2, id1, id2) {
        log.error className + ": " + filter1 + " " + id1 + ", " + filter2 + " " + id2 + " don't exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id " + filter1 + " : " + id1 + " and  " + filter2 + " : " + id2)
        }
    }

  protected def responseNotFound(className, filter1, id1, filter2, id2, filter3, id3) {
        log.error className + ": " + filter1 + " " + id1 + ", " + filter2 + " " + id2 + " and " + filter3 + " " + id3 + " don't exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id " + filter1 + " : " + id1 + ",  " + filter2 + " : " + id2 + " and " + filter3 + " : " + id3)
        }
    }

//  protected def responseImage(String url) {
//        def out = new ByteArrayOutputStream()
//        withFormat {
//            png {
//                if (request.method == 'HEAD') {
//                    render(text: "", contentType: "image/png")
//                }
//                else {
//                    //IIP Send JPEG, so we have to convert to PNG
//                    BufferedImage bufferedImage = getImageFromURL(url)
//                    ImageIO.write(bufferedImage, "PNG", out)
//                    response.contentType = "image/png"
//                    response.getOutputStream() << out.toByteArray()
//                }
//            }
//            jpg {
//                if (request.method == 'HEAD') {
//                    render(text: "", contentType: "image/jpeg")
//                }
//                else {
//                    out << new URL(url).openStream()
//                    response.contentLength = out.size();
//                    response.contentType = "image/jpeg"
//                    response.getOutputStream() << out.toByteArray()
//                }
//            }
//        }
//    }

    protected def responseImage(String url) {
          withFormat {
              png {
                  if (request.method == 'HEAD') {
                      render(text: "", contentType: "image/png")
                  }
                  else {
                      HttpClient client = new HttpClient()
                      client.timeout = 60000;
                      client.connect(url,"","")
                      byte[] imageData = client.getData()
                      //IIP Send JPEG, so we have to convert to PNG
                      InputStream input = new ByteArrayInputStream(imageData);
                      BufferedImage bufferedImage = ImageIO.read(input);
                      def out = new ByteArrayOutputStream()
                      ImageIO.write(bufferedImage, "PNG", out)
                      response.contentType = "image/png"
                      response.getOutputStream() << out.toByteArray()
                  }
              }
              jpg {
                  if (request.method == 'HEAD') {
                      render(text: "", contentType: "image/jpeg")

                  }
                  redirect(url: url)
              }
          }

      }


  protected BufferedImage getImageFromURL(String url) {
        def out = new ByteArrayOutputStream()
        try {
            out << new URL(url).openStream()
        } catch(Exception e) {
            print e.printStackTrace()
            //out = IOUtils.toByteArray(new FileInputStream(url));
            //IOUtils.copy(new FileInputStream(url),out);

         //            InputStream input = new BufferedInputStream(new FileInputStream(url));
//            input.read(out)
        }
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray())
        return ImageIO.read(inputStream)
    }

  protected def responseBufferedImage(BufferedImage bufferedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        withFormat {

            png {
                if (request.method == 'HEAD') {
                    render(text: "", contentType: "image/png")
                }
                else {
                    ImageIO.write(bufferedImage, "png", baos);
                    byte[] bytesOut = baos.toByteArray();
                    response.contentLength = baos.size();
                    response.setHeader("Connection", "Keep-Alive")
                    response.setHeader("Accept-Ranges", "bytes")
                    response.setHeader("Content-Type", "image/png")
                    //response.contentType = "image/jpeg"
                    response.getOutputStream() << bytesOut
                    response.getOutputStream().flush()
                }
            }
            jpg {
                if (request.method == 'HEAD') {
                    render(text: "", contentType: "image/jpeg");
                }
                else {
                    ImageIO.write(bufferedImage, "jpg", baos);
                    byte[] bytesOut = baos.toByteArray();
                    response.contentLength = baos.size();
                    response.setHeader("Connection", "Keep-Alive")
                    response.setHeader("Accept-Ranges", "bytes")
                    response.setHeader("Content-Type", "image/jpeg")
                    //response.contentType = "image/jpeg"
                    response.getOutputStream() << bytesOut
                    response.getOutputStream().flush()
                }
            }
        }
    }
}
