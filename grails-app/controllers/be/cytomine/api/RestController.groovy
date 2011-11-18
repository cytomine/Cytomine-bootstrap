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

  static int SUCCESS_ADD_CODE = 200
  static int SUCCESS_EDIT_CODE = 200
  static int SUCCESS_DELETE_CODE = 200

  static int NOT_FOUND_CODE = 404
  static int TOO_LONG_REQUEST = 413

  static User getCurrentUser(idUser) {
    return User.read(idUser)
  }

  def processCommand(AddCommand c,JSONElement json)  throws NullPointerException, BackingStoreException, SQLException
  {
    processCommand(c,json,SUCCESS_ADD_CODE)
  }

  def processCommand(EditCommand c, JSONElement json)  throws NullPointerException, BackingStoreException, SQLException
  {
    processCommand(c,json,SUCCESS_EDIT_CODE)
  }

  def processCommand(DeleteCommand c,JSONElement json)   throws NullPointerException, BackingStoreException, SQLException
  {
    processCommand(c,json,SUCCESS_DELETE_CODE)
  }

  def processCommand(Command c, JSONElement json, int successCode)  throws NullPointerException, BackingStoreException, SQLException{
    def result
    c.setJson(json)
    c.postData = json.toString()

    log.debug "c.postData.size()=" + c.postData.size() + " Command.MAXSIZEREQUEST=" + Command.MAXSIZEREQUEST
    if(c.postData.size()>=Command.MAXSIZEREQUEST) {
         response.status = TOO_LONG_REQUEST
        log.error "Request too long: " +  c.postData.size() + "character (max="+ Command.MAXSIZEREQUEST+")"
         return [object : null , errors : ["Request too long:"+c.postData.size() + "character"]]
    }

    result = c.execute()
    if (result.status == successCode) {
      c.save()
      CommandHistory ch = new CommandHistory(command:c,prefixAction:"", project: c.project)
      ch.save();
      if(c.saveOnUndoRedoStack) {
        User user = c.user
        new UndoStackItem(command : c, user: user, transactionInProgress:  user.transactionInProgress, transaction : user.transaction).save(flush:true)
      }
    }
    response.status = result.status
    log.debug "result.status="+result.status+" result.data=" + result.data
    return result.data
  }
  def response(data) {
    withFormat {
      json { render data as JSON }
      xml { render data as XML}
    }
  }
  def responseOK(result) {
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

    boolean isSuccess() {
        try {
        return response.status==SUCCESS_ADD_CODE || response.status==SUCCESS_EDIT_CODE || response.status==SUCCESS_DELETE_CODE;
        } catch(Exception ex) {
             return false;
        }
    }

    boolean isSuccess(Integer code) {
        try {
        return code==SUCCESS_ADD_CODE || code==SUCCESS_EDIT_CODE || code==SUCCESS_DELETE_CODE;
        } catch(Exception ex) {
             return false;
        }
    }
}
