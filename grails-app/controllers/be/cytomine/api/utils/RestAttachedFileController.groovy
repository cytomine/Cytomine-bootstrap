package be.cytomine.api.utils

import be.cytomine.api.RestController
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import jsondoc.annotation.ApiResponseObjectLight
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for a description (big text data/with html format) on a specific domain
 */
@Api(name = "attached services", description = "Methods for managing attached file on a specific domain")
class RestAttachedFileController extends RestController {

    def springSecurityService
    def attachedFileService

    @ApiMethodLight(description="List all attached file available", listing=true)
    def list() {
        responseSuccess(attachedFileService.list())
    }

    @ApiMethodLight(description="List all attached file for a given domain", listing=true)
    @ApiParamsLight(params=[
        @ApiParamLight(name="domainIdent", type="long", paramType = ApiParamType.PATH, description = "The domain id"),
        @ApiParamLight(name="domainClassName", type="string", paramType = ApiParamType.PATH, description = "The domain class")
    ])
    def listByDomain() {
        Long domainIdent = params.long("domainIdent")
        String domainClassName = params.get("domainClassName")
        responseSuccess(attachedFileService.list(domainIdent,domainClassName))
    }

    @ApiMethodLight(description="Get a specific attached file")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The attached file id")
    ])
    def show() {
        def file = attachedFileService.read(params.get('id'))
        if(file) {
            responseSuccess(file)
        } else {
            responseNotFound("AttachedFile",params.get('id'))
        }

    }

    @ApiMethodLight(description="Download a file for a given attached file")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The attached file id")
    ])
    @ApiResponseObjectLight(objectIdentifier = "file")
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

    @ApiMethodLight(description="Upload a file for a domain")
    @ApiParamsLight(params=[
        @ApiParamLight(name="domainIdent", type="long", paramType = ApiParamType.PATH, description = "The domain id"),
        @ApiParamLight(name="domainClassName", type="string", paramType = ApiParamType.PATH, description = "The domain class")
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

    @ApiMethodLight(description="Upload a file for a domain. Decode params filled by CKEditor")
    @ApiParamsLight(params=[
    @ApiParamLight(name="domainIdent", type="long", paramType = ApiParamType.PATH, description = "The domain id"),
    @ApiParamLight(name="domainClassName", type="string", paramType = ApiParamType.PATH, description = "The domain class")
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

