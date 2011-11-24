package be.cytomine

abstract class ModelService {

    static transactional = true

    def responseService

    abstract def add(def json)

    abstract def update(def json)

    abstract def delete(def json)

    protected def fillDomainWithData(def object, def json)
    {
        def domain = object.get(json.id)
        domain = object.getFromData(domain,json)
        domain.id = json.id
        return domain
    }
}
