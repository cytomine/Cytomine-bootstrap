package be.cytomine.api.social

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import be.cytomine.social.UserPosition
import groovy.sql.Sql
import org.joda.time.DateTime

/**
 * Controller for user position
 * Position of the user (x,y) on an image for a time
 */
class RestUserPositionController extends RestController {

    def cytomineService
    def imageInstanceService
    def secUserService
    def dataSource
    def projectService

    /**
     * Add new position for user
     */
    def add = {
        SecUser user = cytomineService.getCurrentUser()

        //check if user has moved its position. If not, only update the date
        DateTime tenSecondsAgo = new DateTime().minusSeconds(10)
        def json = request.JSON

        def data = [user.id, json.image, json.lon, json.lat,json.zoom, tenSecondsAgo.millis / 1000, tenSecondsAgo.millis / 1000]
        def reqcreate = "UPDATE user_position SET updated = '" + new Date() + "' WHERE user_id = ? AND image_id = ? AND longitude = ? AND latitude = ? AND zoom = ? AND (extract(epoch from created) > ? OR extract(epoch from updated)> ?)"

        //synchronized (this.getClass()) { //may be not synchronized for perf reasons (but table content will not be consistent)
            def sql = new Sql(dataSource)
            //execute update, if 1 row is affected, the user was still this postion on the image
            int affectedRow = sql.executeUpdate(reqcreate, data)
            def image = imageInstanceService.read(json.image)
            if (affectedRow == 0) {
                Date now = new Date()
                def reqinsert = "INSERT INTO user_position" +
                        "(id,version,user_id,longitude,latitude,zoom,image_id,updated,project_id,created) VALUES " +
                        "(nextval('hibernate_sequence'),0," + user.id + "," + json.lon + "," + json.lat + "," + json.zoom + "," + image.id + ",'" + now + "',"+image.project.id+",'" + now + "')"
                sql.execute(reqinsert)
            }
            sql.close()
        //}

        responseSuccess([:])

    }

    /**
     * Get the last position for a user and an image
     */
    def lastPositionByUser = {
        ImageInstance image = imageInstanceService.read(params.id)
        SecUser user = secUserService.read(params.user)
        def userPositions = UserPosition.createCriteria().list(sort: "created", order: "desc", max: 1) {
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
        def userPositions = UserPosition.createCriteria().list(sort: "created", order: "desc") {
            eq("image", image)
            or {
                gt("created", thirtySecondsAgo.toDate())
                gt("updated", thirtySecondsAgo.toDate())
            }
        }.collect { it.user.id }.unique()
        def result = ["users": userPositions.join(",")]
        responseSuccess(result)
    }

    /**
     * Get online users
     */
    def listLastUserPositionsByProject = {
        DateTime thirtySecondsAgo = new DateTime().minusSeconds(30)
        def userPositions = UserPosition.createCriteria().list() {
            eq("project", projectService.read(params.id))
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
