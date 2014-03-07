package be.cytomine.web

import groovy.util.logging.Log
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest

import javax.servlet.http.HttpServletRequest

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 31/01/12
 * Time: 10:43
 */

@Log
public class CytomineMultipartHttpServletRequest extends CommonsMultipartResolver {

    static final String FILE_SIZE_EXCEEDED_ERROR = "fileSizeExceeded"

    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) {
        try {
            return super.resolveMultipart(request)
        } catch (MaxUploadSizeExceededException e) {
            log.info "!!! MaxUploadSizeExceededException Reached !!! ${e.getMaxUploadSize()}"
            request.setAttribute(FILE_SIZE_EXCEEDED_ERROR, true)
            return new DefaultMultipartHttpServletRequest(request)
        } catch (org.springframework.web.multipart.MultipartException e) {
            log.info "!!! MultipartException Reached !!! " + e
            //upload cancelled by client
        }
    }
}