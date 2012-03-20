package be.cytomine.image

import be.cytomine.security.SecUser
import be.cytomine.project.Project
import be.cytomine.CytomineDomain
import grails.converters.JSON

class UploadedFile extends CytomineDomain {

    public static int UPLOADED = 0
    public static int CONVERTED = 1
    public static int DEPLOYED = 2

    SecUser user
    Project project
    String filename
    String originalFilename
    String ext
    String path
    String contentType
    int size
    int status = 0

    static constraints = {
          project (nullable : true)
    }

     static void registerMarshaller() {
        println "Register custom JSON renderer for " + UploadedFile.class
        JSON.registerObjectMarshaller(UploadedFile) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['created'] = it.created
            returnArray['id'] = it.id
            returnArray['user'] = it.user.id
            returnArray['project'] = it.project?.id
            returnArray['filename'] = it.filename
            returnArray['originalFilename'] = it.originalFilename
            returnArray['ext'] = it.ext
            returnArray['contentType'] = it.contentType
            returnArray['size'] = it.size
            returnArray['uploaded'] = (it.status == 0)
            returnArray['deployed'] = (it.status == 2)
            return returnArray
        }
    }
}
