package be.cytomine.social

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.security.SecUser

/**
 * Info on last user connection for a project
 * User x connect to poject y the 2013/01/01 at xxhyymin
 */
class LastConnection extends CytomineDomain{

    static mapWith = "mongo"

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
        sort "id"
        compoundIndex date:1, indexAttributes:['expireAfterSeconds':60]
    }
}
