package be.cytomine.ontology

import be.cytomine.CytomineDomain
import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.annotation.ApiObjectField

/**
 * A relation between terms (e.g. term1 PARENT term2)
 */
@ApiObject(name = "relation", description = "Type of relation between two terms (e.g. term1 PARENT term2)")
class Relation extends CytomineDomain implements Serializable {

    @ApiObjectField(description = "The name of the relation")
    String name

    static constraints = {
        name(unique: true, nullable: false)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
    }

    String toString() {
        return name
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        return returnArray
    }

}
