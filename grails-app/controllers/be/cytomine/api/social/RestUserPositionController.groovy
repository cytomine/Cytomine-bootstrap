package be.cytomine.api.social

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import be.cytomine.social.UserPosition
import org.joda.time.DateTime

class RestUserPositionController extends RestController {

    def cytomineService
    def imageInstanceService
    def userService
    def dataSource

    def add = {
        synchronized (this.getClass()) {
            SecUser user = cytomineService.getCurrentUser()
            //check if user has moved its position. If not, only update the date
            DateTime tenSecondsAgo = new DateTime()
            tenSecondsAgo = tenSecondsAgo.minusSeconds(10)
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
            //user is online but don't move
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
//                def sql = new Sql(dataSource)
//                long nexId = sequenceService.generateID(user)
//                String req = "insert into user_position (id , version, user_id, longitude , latitude, zoom,image_id) values( ${nexId},0,${user.id},${request.JSON.lon}, ${request.JSON.lat},${request.JSON.zoom},${request.JSON.image})"
//                sql.execute(req)

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
