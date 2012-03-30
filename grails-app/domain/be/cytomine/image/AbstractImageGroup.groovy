package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.security.Group
import grails.converters.JSON
import be.cytomine.Exception.AlreadyExistException

class AbstractImageGroup extends CytomineDomain implements Serializable {

    AbstractImage abstractimage
    Group group

    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    String toString() {
        "[" + this.id + " <" + abstractimage + "," + group + ">]"
    }

    static AbstractImageGroup link(AbstractImage abstractimage, Group group) {
        if (!abstractimage) throw new WrongArgumentException("AbstractImage cannot be null")
        if (!group) throw new WrongArgumentException("Group cannot be null")
        def abstractimageGroup = AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group)
        if (abstractimageGroup) throw new AlreadyExistException("AbstractImage - group already exist")
        if (!abstractimageGroup) {
            abstractimageGroup = new AbstractImageGroup()
            abstractimage?.addToAbstractimagegroup(abstractimageGroup)
            group?.addToAbstractimagegroup(abstractimageGroup)
            abstractimage.refresh()
            group.refresh()
            abstractimageGroup.save(flush: true)
        } else throw new WrongArgumentException("AbstractImage " + abstractimage.id + " and group " + group.id + " are already mapped")
        return abstractimageGroup
    }

    static void unlink(AbstractImage abstractimage, Group group) {

        if (!abstractimage) throw new WrongArgumentException("AbstractImage cannot be null")
        if (!group) throw new WrongArgumentException("Group cannot be null")
        def abstractimageGroup = AbstractImageGroup.findByAbstractimageAndGroup(abstractimage, group)
        if (!abstractimageGroup) return

        if (abstractimageGroup) {
            abstractimage?.removeFromAbstractimagegroup(abstractimageGroup)
            group?.removeFromAbstractimagegroup(abstractimageGroup)
            abstractimage.refresh()
            group.refresh()
            abstractimageGroup.delete(flush: true)
        }
    }

    static AbstractImageGroup createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static AbstractImageGroup createFromData(jsonAbstractImageGroup) {
        def abstractimageGroup = new AbstractImageGroup()
        getFromData(abstractimageGroup, jsonAbstractImageGroup)
    }

    static AbstractImageGroup getFromData(abstractimageGroup, jsonAbstractImageGroup) {
        println "jsonAbstractImageGroup from getAbstractImageGroupFromData = " + jsonAbstractImageGroup
        abstractimageGroup.abstractimage = AbstractImage.get(jsonAbstractImageGroup.abstractimage.toString())
        abstractimageGroup.group = Group.get(jsonAbstractImageGroup.group.toString())
        return abstractimageGroup;
    }

    def getCallBack() {
        HashMap<String, Object> callback = new HashMap<String, Object>();
        callback.put("abstractimageID", this.abstractimage.id)
        callback.put("groupID", this.group.id)
        callback.put("imageID", this.abstractimage.id)
        return callback
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + AbstractImageGroup.class
        JSON.registerObjectMarshaller(AbstractImageGroup) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['abstractimage'] = it.abstractimage?.id
            returnArray['group'] = it.group?.id
            return returnArray
        }
    }
}
