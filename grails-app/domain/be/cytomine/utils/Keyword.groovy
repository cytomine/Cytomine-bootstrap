package be.cytomine.utils
/**
 * Keywords may be used to suggest a term in an input
 */
class Keyword {

    String key

    static mapping = {
        version false
        id generator: 'identity', column: 'nid'
        key(unique:true)
    }
}
