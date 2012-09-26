package be.cytomine.social

import be.cytomine.security.SecUser
import be.cytomine.CytomineDomain

class LastConnection extends CytomineDomain{

    SecUser user
    Date date

    static constraints = {
        user (nullable:false)
        date (nullable: true)
    }

    static mapping = {
        id(generator: 'assigned', unique: true)
    }
}
