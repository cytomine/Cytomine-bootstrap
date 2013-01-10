package be.cytomine.api.social

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.social.UserPosition
import org.joda.time.DateTime

/**
 * Controller for user position
 * Position of the user (x,y) on an image for a time
 */
class RestUserPositionController extends RestController {

    def cytomineService
    def imageInstanceService
    def userService
    def dataSource
    def projectService

    /**
     * Add new position for user
     */
    def add = {
        synchronized (this.getClass()) {
            SecUser user = cytomineService.getCurrentUser()

            //check if user has moved its position. If not, only update the date
            DateTime tenSecondsAgo = new DateTime().minusSeconds(10)

            //Get the last user position ONLY if user has not moved
            def userPositions = UserPosition.createCriteria().list(sort : "created", order : "desc", max : 1) {
                eq("image.id", Long.parseLong(request.JSON.image+""))
                eq("user.id", user.id)
                eq("longitude", (double) request.JSON.lon)
                eq("latitude", (double) request.JSON.lat)
                or {
                    gt("created", tenSecondsAgo.toDate())
                    gt("updated", tenSecondsAgo.toDate())
                }
            }

            def userPosition
            //user is online but don't move, just update the date
            if (userPositions.size() == 1) {
                userPositions[0].setUpdated(new Date())
                userPositions[0].save(flush : true)
                userPosition = userPositions[0]
            } else {
                //create the new position
                ImageInstance image = imageInstanceService.read(request.JSON.image)
                userPosition = new UserPosition(
                        user : user,
                        longitude : request.JSON.lon,
                        latitude : request.JSON.lat,
                        zoom : request.JSON.zoom,
                        image : image,
                        updated: new Date(),
                        project : image.project
                )
                userPosition.save(flush: true)
            }
            responseSuccess(userPosition)
        }
    }

    /**
     * Get the last position for a user and an image
     */
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

    /**
     * Get users that have opened an image now
     */
    def listOnlineUsersByImage = {
        ImageInstance image = imageInstanceService.read(params.id)
        DateTime thirtySecondsAgo = new DateTime().minusSeconds(30)
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

    /**
     * Get online users
     */
    def listLastUserPositionsByProject = {
        DateTime thirtySecondsAgo = new DateTime().minusSeconds(30)
        def userPositions = UserPosition.createCriteria().list() {
            eq("project", projectService.read(params.id, new Project()))
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
