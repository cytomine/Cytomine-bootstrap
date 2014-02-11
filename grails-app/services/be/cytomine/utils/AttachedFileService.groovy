package be.cytomine.utils

import be.cytomine.SecurityACL
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.security.SecUser

import static org.springframework.security.acls.domain.BasePermission.READ
import static org.springframework.security.acls.domain.BasePermission.WRITE

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
        SecurityACL.checkAtLeastOne(domainIdent,domainClassName,"containers",READ)
        AttachedFile file = new AttachedFile()
        file.domainIdent =  domainIdent
        file.domainClassName = domainClassName
        file.filename = filename
        file.data = data
        saveDomain(file)
        file
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(AttachedFile domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecurityACL.checkAtLeastOne(domain.domainIdent, domain.domainClassName, "containers", WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }


}
