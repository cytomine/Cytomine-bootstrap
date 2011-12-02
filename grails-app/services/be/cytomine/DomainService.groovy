package be.cytomine

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.Exception.CytomineException

class DomainService {

    static transactional = true

    def saveDomain(def newObject) {
        if (!newObject.validate()) {
            println newObject.retrieveErrors().toString()
            CytomineException cyto = new WrongArgumentException(newObject.retrieveErrors().toString())
            log.debug "cyto="+cyto.msg
            log.debug "code="+cyto.code
            throw new WrongArgumentException(newObject.retrieveErrors().toString())
        }
        if (!newObject.save(flush: true)) throw new InvalidRequestException(newObject.retrieveErrors().toString())
    }

    def deleteDomain(def oldObject) {
        try {
            oldObject.delete(flush: true, failOnError: true)
        } catch (Exception e) {
            log.error e.toString()
            throw new InvalidRequestException(e.toString())
        }

    }

    def editDomain(def newObject, def postData) {
        if (postData.id instanceof String) {
            newObject.id = Long.parseLong(postData.id)
        } else {
            newObject.id = postData.id
        }
        if (!newObject.validate()) throw new WrongArgumentException(newObject.errors.toString())
        if (!newObject.save(flush: true)) throw new InvalidRequestException(newObject.errors.toString())
    }
}
