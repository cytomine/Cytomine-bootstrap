package be.cytomine.utils

import be.cytomine.SecurityACL

import static org.springframework.security.acls.domain.BasePermission.READ

class AttachedFileService extends ModelService {

    static transactional = true
    def springSecurityService
    def transactionService
    def commandService
    def cytomineService

    def currentDomain() {
        return AttachedFile
    }

    /**
     * List all description, Only for admin
     */
    def list() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        return AttachedFile.list()
    }

    def list(Long domainIdent,String domainClassName) {
        SecurityACL.check(domainIdent,domainClassName,"container",READ)
        return AttachedFile.findAllByDomainIdentAndDomainClassName(domainIdent,domainClassName)
    }


    def read(def id) {
        AttachedFile file = AttachedFile.read(id)
        if(file) {
            //TODO: TEMPORARY disable security. There is an issue:
            //if we copy layers from image x - project 1 to image x - project 2, users may not have the right to download the file
            //SecurityACL.check(file.domainIdent,file.domainClassName,"container",READ)
        }
        file
    }

    def add(String filename,byte[] data,Long domainIdent,String domainClassName) {
        SecurityACL.check(domainIdent,domainClassName,"container",READ)
        AttachedFile file = new AttachedFile()
        file.domainIdent =  domainIdent
        file.domainClassName = domainClassName
        file.filename = filename
        file.data = data
        saveDomain(file)
        file
    }


}
