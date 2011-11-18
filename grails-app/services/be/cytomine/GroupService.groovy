package be.cytomine

import be.cytomine.image.AbstractImage

class GroupService {

    static transactional = true


        def list(AbstractImage abstractimage) {
               return abstractimage.groups()
            }

}
