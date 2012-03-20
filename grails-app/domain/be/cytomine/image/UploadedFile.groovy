package be.cytomine.image

import be.cytomine.security.SecUser
import be.cytomine.project.Project

class UploadedFile {

    SecUser user
    Project project
    String filename
    String originalFilename
    String path

    static constraints = {
          project (nullable : true)
    }
}
