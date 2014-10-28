package be.cytomine.social
/**
 *  User position on an image at a time
 *  2 sub class:
 *  -persistentUserPosition: usefull store the position for a long time (analyze,...)
 *  -lastuserposition: usefull to get the last position (remove if > 1 min)
 */
class UserPosition {//extends CytomineDomain {

    //All field from LastUserPosition and PersistentUserPosition should be here!!!

    //but mongodb plugin doesn't support tablePerHierarchy config.
    //so data from lastuserp (no persistent, remove after x sec) and persistuserp (persistent) go into the same collection

    static def copyProperties(PersistentUserPosition source, LastUserPosition target) {
        target.user = source.user
        target.image = source.image
        target.project = source.project
        target.location = source.location
        target.zoom = source.zoom
        target.created = source.created
        target.imageName = source.imageName
    }
}
