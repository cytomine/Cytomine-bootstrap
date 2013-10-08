package be.cytomine.utils

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.security.SecUser

import static org.springframework.security.acls.domain.BasePermission.READ

class AttachedFileService extends ModelService {

    static transactional = true
    def springSecurityService
    def transactionService
    def commandService
    def cytomineService

    def currentDomain() {
        return Description
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
            SecurityACL.check(file.domainIdent,file.domainClassName,"container",READ)
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
