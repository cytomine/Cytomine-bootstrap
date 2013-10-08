package be.cytomine.api.utils

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.api.RestController

/**
 * Controller for a description (big text data/with html format) on a specific domain
 */
class RestAttachedFileController extends RestController {

    def springSecurityService
    def attachedFileService

    def list = {
        responseSuccess(attachedFileService.list())
    }

    def listByDomain = {
        Long domainIdent = params.long("domainIdent")
        String domainClassName = params.get("domainClassName")
        responseSuccess(attachedFileService.list(domainIdent,domainClassName))
    }

    def show = {
        responseSuccess(attachedFileService.read(params.get('id')))
    }

    def download = {
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

    def upload = {
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
}

