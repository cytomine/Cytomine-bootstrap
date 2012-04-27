package be.cytomine.api.social

import be.cytomine.social.UserPosition
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import org.joda.time.DateTime

class RestUserPositionController extends RestController {

    def cytomineService
    def imageInstanceService
    def userService

    def add = {
        synchronized (this.getClass()) {
            //check if user has moved its position. If not, only update the date
            DateTime tenSecondsAgo = new DateTime()
            tenSecondsAgo = tenSecondsAgo.minusSeconds(10)
            def userPositions = UserPosition.createCriteria().list(sort : "created", order : "desc", max : 1) {
                eq("image", imageInstanceService.read(request.JSON.image))
                eq("user", userService.read(cytomineService.getCurrentUser().id))
                eq("longitude", (double) request.JSON.lon)
                eq("latitude", (double) request.JSON.lat)
                or {
                    gt("created", tenSecondsAgo.toDate())
                    gt("updated", tenSecondsAgo.toDate())
                }
            }

            def userPosition
            //user is online but don't move
            if (userPositions.size() == 1) {
                userPositions[0].setUpdated(new Date())
                userPositions[0].save(flush : true)
                userPosition = userPositions[0]
            } else {
                //create the new position
                userPosition = new UserPosition(
                        user : userService.read(cytomineService.getCurrentUser().id),
                        longitude : request.JSON.lon,
                        latitude : request.JSON.lat,
                        zoom : request.JSON.zoom,
                        image : imageInstanceService.read(request.JSON.image)
                ).save(flush : true)
            }
            responseSuccess(userPosition)
        }
    }

    def lastPositionByUser = {
        ImageInstance image = imageInstanceService.read(params.id)
        SecUser user = userService.read(params.user)
        def userPositions = UserPosition.createCriteria().list(sort : "created", order : "desc", max : 1) {
            eq("user", user)
            eq("image", image)
        }
        def result = (userPositions.size() > 0) ? userPositions[0] : []
        responseSuccess(result)
    }

    def listOnlineUsersByImage = {
        ImageInstance image = imageInstanceService.read(params.id)
        DateTime thirtySecondsAgo = new DateTime()
        thirtySecondsAgo = thirtySecondsAgo.minusSeconds(30)
        def userPositions = UserPosition.createCriteria().list(sort : "created", order : "desc") {
            eq("image", image)
            or {
                gt("created", thirtySecondsAgo.toDate())
                gt("updated", thirtySecondsAgo.toDate())
            }
        }.collect { it.user.id }.unique()
        def result = [ "users" : userPositions.join(",")]
        responseSuccess(result)
    }

    def listLastUserPositionsByProject = {
        DateTime thirtySecondsAgo = new DateTime()
        thirtySecondsAgo = thirtySecondsAgo.minusSeconds(30)
        def userPositions = UserPosition.createCriteria().list() {
            or {
                gt("created", thirtySecondsAgo.toDate())
                gt("updated", thirtySecondsAgo.toDate())
            }

            projections {
                groupProperty("image.id")
                groupProperty("user.id")
            }
        }
        responseSuccess(userPositions)
    }

}
