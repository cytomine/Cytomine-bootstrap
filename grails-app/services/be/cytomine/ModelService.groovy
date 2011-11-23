package be.cytomine

abstract class ModelService {

    static transactional = true

    abstract def add(def json)

    ;

    abstract def update(def json)

    ;

    abstract def delete(def json)

    ;
}
