package be.cytomine.api.social

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import be.cytomine.social.LastUserPosition
import be.cytomine.social.PersistentUserPosition
import be.cytomine.social.UserPosition
import be.cytomine.utils.JSONUtils
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
    def mongo
    def noSQLCollectionService


    /**
     * Add new position for user
     */
    def add = {
        SecUser user = cytomineService.getCurrentUser()
        def json = request.JSON
        ImageInstance image = ImageInstance.read(JSONUtils.getJSONAttrLong(json,"image",0))
        PersistentUserPosition position = new PersistentUserPosition()
        position.user = user
        position.image = image
        position.project = image.project
        def polygon = [
                [JSONUtils.getJSONAttrDouble(json,"topLeftX",-1),JSONUtils.getJSONAttrDouble(json,"topLeftY",-1)],
                [JSONUtils.getJSONAttrDouble(json,"topRightX",-1),JSONUtils.getJSONAttrDouble(json,"topRightY",-1)],
                [JSONUtils.getJSONAttrDouble(json,"bottomRightX",-1),JSONUtils.getJSONAttrDouble(json,"bottomRightY",-1)],
                [JSONUtils.getJSONAttrDouble(json,"bottomLeftX",-1),JSONUtils.getJSONAttrDouble(json,"bottomLeftY",-1)]
        ]
        position.location = polygon
        position.zoom = JSONUtils.getJSONAttrInteger(json,"zoom",-1)
        position.created = new Date()
        position.updated = position.created
        position.imageName = image.getFileName()
        position.insert(flush:true) //don't use save (stateless collection)

        LastUserPosition lastUserPosition = new LastUserPosition()
        UserPosition.copyProperties(position,lastUserPosition)
        lastUserPosition.insert(flush:true)

        responseSuccess([:])
    }


    /**
     * Get the last position for a user and an image
     */
    def lastPositionByUser = {
        ImageInstance image = imageInstanceService.read(params.id)
        SecUser user = secUserService.read(params.user)
        def userPositions = LastUserPosition.createCriteria().list(sort: "created", order: "desc", max: 1) {
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
        def userPositions = LastUserPosition.createCriteria().list(sort: "created", order: "desc") {
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
        def db = mongo.getDB(noSQLCollectionService.getDatabaseName())
        DateTime thirtySecondsAgo = new DateTime().minusSeconds(30)

        def result = db.lastUserPosition.aggregate(
                [$match : [ project : projectService.read(params.id).id, created:[$gt:thirtySecondsAgo.toDate()]]],
                [$project:[user:1,"image":1]],
                [$group : [_id : [ user: '$user', image: '$image']]]
        )
        responseSuccess(result.results().collect{it['_id']}.collect{[it["image"],it["user"]]})
    }


}
