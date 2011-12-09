package be.cytomine.api

import be.cytomine.Exception.CytomineException
import grails.converters.JSON
import grails.converters.XML
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class RestController {

    def springSecurityService
    int idUser

    static int NOT_FOUND_CODE = 404

    def transactionService

    def add(def service, def json) {
        try {
            log.debug("add")
            def result = service.add(json)
            log.debug("result")
            responseResult(result)
        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        } finally {
            transactionService?.stopIfTransactionInProgress()
        }
    }

    def update(def service, def json) {
        try {
            def domain = service.retrieve(json)
            log.debug "json="+json
            log.debug "domain="+domain
            def result = service.update(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        } finally {
            transactionService?.stopIfTransactionInProgress()
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
        } finally {
            transactionService?.stopIfTransactionInProgress()
        }
    }

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
        response(data, code)
    }

    def responseSuccess(data) {
        response(data)
    }

    def response(data, code) {
        response.status = code
        response(data)
    }

    def responseNotFound(className, id) {
        log.error className + " Id " + id + " don't exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id : " + id)
        }
    }

    def responseNotFound(className, filter, id) {
        log.error className + ": " + filter + " " + id + " don't exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id " + filter + " : " + id)
        }
    }

    def responseNotFound(className, filter1, filter2, id1, id2) {
        log.error className + ": " + filter1 + " " + id1 + ", " + filter2 + " " + id2 + " don't exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id " + filter1 + " : " + id1 + " and  " + filter2 + " : " + id2)
        }
    }

    def responseNotFound(className, filter1, id1, filter2, id2, filter3, id3) {
        log.error className + ": " + filter1 + " " + id1 + ", " + filter2 + " " + id2 + " and " + filter3 + " " + id3 + " don't exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id " + filter1 + " : " + id1 + ",  " + filter2 + " : " + id2 + " and " + filter3 + " : " + id3)
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

    BufferedImage getImageFromURL(String url) {
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
        response.setHeader("Connection", "Keep-Alive")
        response.setHeader("Accept-Ranges", "bytes")
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
