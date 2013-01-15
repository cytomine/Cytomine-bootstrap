package be.cytomine.social

import be.cytomine.CytomineDomain
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 18/04/12
 * Time: 11:29
 * TODOSTEVBEN: doc
 */
class FollowRequest extends CytomineDomain {

    static int REQUESTED = 0
    static int ACCEPTED  = 1
    static int REJECTED  = 2

    static belongsTo = [fromUser : SecUser, toUser : SecUser, image : ImageInstance]

    int status = REQUESTED
}
