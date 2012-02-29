package be.cytomine.api.social

import be.cytomine.social.UserPosition
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser

class RestUserPositionController extends RestController {

    def cytomineService
    def imageInstanceService
    def userService

    def add = {
        UserPosition userPosition = new UserPosition(
                user : cytomineService.getCurrentUser(),
                longitude : request.JSON.lon,
                latitude : request.JSON.lat,
                zoom : request.JSON.zoom,
                image : imageInstanceService.read(request.JSON.image)
        ).save()
        responseSuccess(userPosition)
    }

    def list = {
        ImageInstance image = imageInstanceService.read(params.id)
        SecUser user = userService.read(params.user)
        def userPositions = UserPosition.createCriteria().list(sort : "created", order : "desc", max : 1) {
            eq("user", user)
            eq("image", image)
        }
        def result = (userPositions.size() > 0) ? userPositions[0] : []
        responseSuccess(result)
    }
}
