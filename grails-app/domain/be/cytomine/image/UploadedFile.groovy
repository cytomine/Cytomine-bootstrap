package be.cytomine.image

import be.cytomine.security.SecUser
import be.cytomine.project.Project
import be.cytomine.CytomineDomain

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
}
