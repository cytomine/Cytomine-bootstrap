package be.cytomine.api.image

import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import grails.converters.JSON

/**
 * Controller that handle operation on security access to abstractImage domain.
 * An abstract image may be visible by some groups.
 */
class RestAbstractImageGroupController extends RestController {

    def abstractImageService
    def abstractImageGroupService
    def groupService

    /**
     * Show a link between an abstract image and a group
     */
    def show = {
        AbstractImage abstractImage = abstractImageService.read(params.long('idabstractimage'))
        Group group = groupService.read(params.long('idgroup'))
        if (abstractImage && group) {
            def abstractImageGroup = abstractImageGroupService.get(abstractImage, group)
            if (abstractImageGroup) responseSuccess(abstractImageGroup)
            else responseNotFound("AbstractImageGroup", "Group", "AbstractImage", params.idgroup, params.idabstractimage)
        }
        else {
            responseNotFound("AbstractImageGroup", "Group", "AbstractImage", params.idgroup, params.idabstractimage)
        }
    }

    /**
     * Add a new group to an abstract image
     */
    def add = {
        add(abstractImageGroupService, request.JSON)
    }

    /**
     * Remove a group from an abstract image
     */
    def delete = {
        delete(abstractImageGroupService, JSON.parse("{abstractimage: $params.idabstractimage, group: $params.idgroup}"))
    }
}