package be.cytomine.image.server

import be.cytomine.image.AbstractImage
import grails.converters.JSON

class StorageService {

    def ais = """scripts/ais/storage/aisStorageClientRuby/ais"""

    static transactional = true


    def copy(Storage storage, AbstractImage image) {
        def remotePath = getRemotePath(storage, image)
        def command = ais + " -s " + storage.getName() + " cp " + image.getPath() + " " + remotePath// Create the String
        log.info command
        def proc = command.execute()                 // Call *execute* on the string
        proc.waitFor()                               // Wait for the command to finish

        // Obtain status and output
        log.info "return code: ${ proc.exitValue()}"
        log.info "stderr: ${proc.err.text}"
        log.info "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy
    }


    def remove(Storage storage, AbstractImage) {

    }

    def metadata(Storage storage, AbstractImage image) {
        def remotePath = getRemotePath(storage, image)
        def command = ais + " --raw -s " + storage.getName() + " metadata " + image.getPath() + " " + remotePath// Create the String
        log.info command
        def proc = command.execute()                 // Call *execute* on the string
        proc.waitFor()                               // Wait for the command to finish

        def exitValue = proc.exitValue()
        def stdderr = proc.err.text
        def stdout = proc.in.text

        return JSON.parse(stdout)
    }

    def getRemotePath(Storage storage, AbstractImage image) {
        storage.getServiceUrl() + storage.getName() + "/" + image.getFilename()
    }


    def get(def id) {
        Storage.get(id)
    }

    def read(def id) {
        Storage.read(id)
    }
}
