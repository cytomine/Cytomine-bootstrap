package ais

import be.cytomine.image.AbstractImage
import be.cytomine.image.server.Storage
import grails.converters.JSON

class StorageService {

    def ais = """scripts/ais/storage/aisStorageClientRuby/ais"""

    static transactional = true

    def copy(Storage storage, AbstractImage image) {
        def remotePath = getRemotePath(storage, image)
        def command = ais + " -s " + storage.getName() + " cp " + image.getPath() + " " +remotePath// Create the String
        println command
        def proc = command.execute()                 // Call *execute* on the string
        proc.waitFor()                               // Wait for the command to finish

        // Obtain status and output
        println "return code: ${ proc.exitValue()}"
        println "stderr: ${proc.err.text}"
        println "stdout: ${proc.in.text}" // *out* from the external program is *in* for groovy
    }


    def remove(Storage storage, AbstractImage) {

    }

    def metadata(Storage storage, AbstractImage image) {
        def remotePath = getRemotePath(storage, image)
        def command = ais + " --raw -s " + storage.getName() + " metadata " + image.getPath() + " " + remotePath// Create the String
        println command
        def proc = command.execute()                 // Call *execute* on the string
        proc.waitFor()                               // Wait for the command to finish

        def exitValue = proc.exitValue()
        def stdderr = proc.err.text
        def stdout = proc.in.text

        return JSON.parse(stdout)
    }

    def getRemotePath (Storage storage, AbstractImage image){
        storage.getServiceUrl() + storage.getName() + "/" + image.getFilename()
    }


}
