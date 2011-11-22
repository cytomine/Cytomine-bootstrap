package be.cytomine.api
import grails.converters.*
import be.cytomine.security.User
import be.cytomine.command.AddCommand
import be.cytomine.command.UndoStackItem
import be.cytomine.command.EditCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Command
import be.cytomine.command.CommandHistory
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.util.prefs.BackingStoreException
import org.hibernate.exception.ConstraintViolationException
import java.sql.SQLException
import org.codehaus.groovy.grails.web.json.JSONElement

class RestController {

  def springSecurityService
  int idUser

  static int NOT_FOUND_CODE = 404

  def response(data) {
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }
  def responseResult(result) {
      response.status = result.status
    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML}
    }
  }

  def responseSuccess(data, code) {
    response(data,code)
  }
  def responseSuccess(data) {
    response(data)
  }

  def response(data, code) {
    response.status = code
    response(data)
  }

  def responseNotFound(className,id) {
    log.error className + " Id " + id+ " don't exist"
    response.status = NOT_FOUND_CODE
    render(contentType:'text/json'){
      errors(message:className+" not found with id : " +id)
    }
  }

  def responseNotFound(className,filter,id) {
    log.error className + ": " + filter + " " + id+ " don't exist"
    response.status = NOT_FOUND_CODE
    render(contentType:'text/json'){
      errors(message:className+" not found with id " + filter + " : " +id)
    }
  }

  def responseNotFound(className,filter1,filter2,id1,id2) {
    log.error className + ": " + filter1 + " " + id1 + ", "+ filter2 + " " + id2+ " don't exist"
    response.status = NOT_FOUND_CODE
    render(contentType:'text/json'){
      errors(message:className+" not found with id " + filter1 + " : " +id1 + " and  " + filter2 + " : " +id2)
    }
  }

  def responseNotFound(className,filter1,id1,filter2,id2,filter3,id3) {
    log.error className + ": " + filter1 + " " + id1 + ", "+ filter2 + " " + id2+ " and " + filter3 + " " + id3+ " don't exist"
    response.status = NOT_FOUND_CODE
    render(contentType:'text/json'){
      errors(message:className+" not found with id " + filter1 + " : " +id1 + ",  " + filter2 + " : " +id2+ " and "+ filter3 + " : " +id3)
    }
  }

  def responseImage(String url) {
      def out = new ByteArrayOutputStream()
      out << new URL(url).openStream()
      response.contentLength = out.size();
      withFormat {
        jpg {
          if (request.method == 'HEAD') {
            render(text: "", contentType: "image/jpeg");
          }
          else {
            response.contentType = "image/jpeg"; response.getOutputStream() << out.toByteArray()
          }
        }
      }
  }

    BufferedImage getImageFromURL(String url)  {
        def out = new ByteArrayOutputStream()
        out << new URL(url).openStream()
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        return ImageIO.read(inputStream);
    }

    def responseBufferedImage(BufferedImage bufferedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", baos);
        byte[] bytesOut = baos.toByteArray();
        response.contentLength = baos.size();
        response.setHeader("Connection","Keep-Alive")
        response.setHeader("Accept-Ranges","bytes")
        response.setHeader("Content-Type", "image/jpeg")
        withFormat {
            jpg {
                if (request.method == 'HEAD') {
                    render(text: "", contentType: "image/jpeg");
                }
                else {
                    response.contentType = "image/jpeg";
                    response.getOutputStream() << bytesOut
                    response.getOutputStream().flush()
                }
            }
        }
    }
}
