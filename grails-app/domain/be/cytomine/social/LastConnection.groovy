package be.cytomine.social

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.security.SecUser

class LastConnection extends CytomineDomain{

    SecUser user
    Date date
    Project project

    static constraints = {
        user (nullable:false)
        date (nullable: true)
        project (nullable: true)
    }

    static mapping = {
        id(generator: 'assigned', unique: true)
    }
}
