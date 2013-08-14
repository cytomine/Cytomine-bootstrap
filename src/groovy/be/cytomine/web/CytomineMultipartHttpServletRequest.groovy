package be.cytomine.web

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

public class CytomineMultipartHttpServletRequest extends CommonsMultipartResolver {

    static final String FILE_SIZE_EXCEEDED_ERROR = "fileSizeExceeded"

    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) {
        try {
            return super.resolveMultipart(request)
        } catch (MaxUploadSizeExceededException e) {
            println "!!! MaxUploadSizeExceededException Reached !!!"
            request.setAttribute(FILE_SIZE_EXCEEDED_ERROR, true)
            return new DefaultMultipartHttpServletRequest(request)
        } catch (org.springframework.web.multipart.MultipartException e) {
            println "!!! MultipartException Reached !!!"
            //upload cancelled by client
        }
    }
}