package be.cytomine.web

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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