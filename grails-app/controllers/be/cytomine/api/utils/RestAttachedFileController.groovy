package be.cytomine.api.utils

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

import be.cytomine.api.RestController
import org.restapidoc.annotation.*
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for a description (big text data/with html format) on a specific domain
 */
@RestApi(name = "attached services", description = "Methods for managing attached file on a specific domain")
class RestAttachedFileController extends RestController {

    def springSecurityService
    def attachedFileService

    @RestApiMethod(description="List all attached file available", listing=true)
    def list() {
        responseSuccess(attachedFileService.list())
    }

    @RestApiMethod(description="List all attached file for a given domain", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="domainIdent", type="long", paramType = RestApiParamType.PATH, description = "The domain id"),
        @RestApiParam(name="domainClassName", type="string", paramType = RestApiParamType.PATH, description = "The domain class")
    ])
    def listByDomain() {
        Long domainIdent = params.long("domainIdent")
        String domainClassName = params.get("domainClassName")
        responseSuccess(attachedFileService.list(domainIdent,domainClassName))
    }

    @RestApiMethod(description="Get a specific attached file")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The attached file id")
    ])
    def show() {
        def file = attachedFileService.read(params.get('id'))
        if(file) {
            responseSuccess(file)
        } else {
            responseNotFound("AttachedFile",params.get('id'))
        }

    }

    @RestApiMethod(description="Download a file for a given attached file")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The attached file id")
    ])
    @RestApiResponseObject(objectIdentifier = "file")
    def download() {
       def attached = attachedFileService.read(params.get('id'))
        if(!attached) {
            responseNotFound("AttachedFile",params.get('id'))
        } else {
            response.setContentType "application/octet-stream"
            response.setHeader "Content-disposition", "attachment; filename=${attached.filename}"
            response.outputStream << attached.data
            response.outputStream.flush()
        }
    }

    @RestApiMethod(description="Upload a file for a domain")
    @RestApiParams(params=[
        @RestApiParam(name="domainIdent", type="long", paramType = RestApiParamType.PATH, description = "The domain id"),
        @RestApiParam(name="domainClassName", type="string", paramType = RestApiParamType.PATH, description = "The domain class")
    ])
    def upload() {
        log.info "Upload attached file"
        Long domainIdent = params.long("domainIdent")
        String domainClassName = params.get("domainClassName")
        def f = request.getFile('files[]')


        String filename = f.originalFilename
        log.info "Upload $filename for domain $domainClassName $domainIdent"
        log.info "File size = ${f.size}"

        def result = attachedFileService.add(filename,f.getBytes(),domainIdent,domainClassName)
        responseSuccess(result)
    }

    @RestApiMethod(description="Upload a file for a domain. Decode params filled by CKEditor")
    @RestApiParams(params=[
    @RestApiParam(name="domainIdent", type="long", paramType = RestApiParamType.PATH, description = "The domain id"),
    @RestApiParam(name="domainClassName", type="string", paramType = RestApiParamType.PATH, description = "The domain class")
    ])
    def uploadFromCKEditor() {
        log.info "Upload attached file"
        Long domainIdent = params.long("domainIdent")
        String domainClassName = params.get("domainClassName")
        def upload = params.upload
        String filename = upload.getOriginalFilename()
        log.info "Upload $filename for domain $domainClassName $domainIdent"

        def result = attachedFileService.add(filename,upload.getBytes(),domainIdent,domainClassName)

        //tricky :-) difficult to interact with ckeeditor
       // String resp = '<div><script>$("span:contains(\'Send it to the Server\')").hide();</script>' +
         //       '<div style="width:400px;height:400px">Image was saved correctly. Copy this url: api/attachedfile/'+result.id+'/download, click on "image info" tabs and copy the content in the URL field.</div></div>'
         //: api/attachedfile/'+result.domain.id+'/download" />'

        String resp = "<script type='text/javascript'>window.parent.CKEDITOR.tools.callFunction(${params.CKEditorFuncNum}, 'api/attachedfile/${result.id}/download', '');</script>"

        render(resp)
    }
}

