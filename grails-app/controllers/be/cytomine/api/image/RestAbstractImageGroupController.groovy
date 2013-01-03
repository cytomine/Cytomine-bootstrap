package be.cytomine.api.image

import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group
import grails.converters.JSON

/**
 * Controller that handle operation on security access to abstractimage domain.
 * An abstract image may be visible by some groups.
 */
class RestAbstractImageGroupController extends RestController {

    def abstractImageService
    def abstractImageGroupService
    def groupService

    def show = {
        AbstractImage abstractimage = abstractImageService.read(params.long('idabstractimage'))
        Group group = groupService.read(params.long('idgroup'))
        if (abstractimage && group) {
            def abstractimageGroup = abstractImageGroupService.get(abstractimage, group)
            if (abstractimageGroup) responseSuccess(abstractimageGroup)
            else responseNotFound("AbstractImageGroup", "Group", "AbstractImage", params.idgroup, params.idabstractimage)
        }
        else responseNotFound("AbstractImageGroup", "Group", "AbstractImage", params.idgroup, params.idabstractimage)
    }

    def add = {
        add(abstractImageGroupService, request.JSON)
    }

    def delete = {
        delete(abstractImageGroupService, JSON.parse("{abstractimage: $params.idabstractimage, group: $params.idgroup}"))
    }
}