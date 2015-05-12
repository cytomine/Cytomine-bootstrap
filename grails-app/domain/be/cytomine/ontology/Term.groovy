package be.cytomine.ontology

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields

/**
 * A term is a class that can be link to an annotation
 * A term is a part of ontology (list/tree of terms)
 */
@RestApiObject(name = "term", description = "Term description")
class Term extends CytomineDomain implements Serializable, Comparable {

    @RestApiObjectField(description = "The term name")
    String name

    @RestApiObjectField(description = "A comment about the term", mandatory = false)
    String comment

    @RestApiObjectField(description = "The ontology that store the term")
    Ontology ontology

    @RestApiObjectField(description = "The color associated, in HTML format (e.g : RED = #FF0000)")
    String color

    Double rate // ?

    static belongsTo = [ontology: Ontology]

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "parent", description = "The parent term id of this annotation",allowedType = "long",useForCreation = false)
    ])
    static transients = ["rate"]

    static constraints = {
        comment(blank: true, nullable: true)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Term.withNewSession {
            Term termAlreadyExist=Term.findByNameAndOntology(name, ontology)
            if(termAlreadyExist!=null && (termAlreadyExist.id!=id))  {
                throw new AlreadyExistException("Term " + termAlreadyExist?.name + " already exist!")
            }
        }
    }

    /**
     * Check if this term has children
     * @return True if this term has children, otherwise false
     */
    def hasChildren() {
        boolean hasChildren = false
        RelationTerm.findAllByTerm1(this).each {
            if (it.getRelation().getName().equals(RelationTerm.names.PARENT)) {
                hasChildren = true
                return
            }
        }
        return hasChildren
    }

    /**
     * Check if this term has no parent
     * @return True if term has no parent
     */
    def isRoot() {
        def isRoot = true;
        RelationTerm.findAllByTerm2(this).each {
            isRoot &= (it.getRelation().getName() != RelationTerm.names.PARENT)
        }
        return isRoot
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Term insertDataIntoDomain(def json, def domain = new Term()) throws CytomineException {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json,'name')
        domain.created = JSONUtils.getJSONAttrDate(json,'created')
        domain.updated = JSONUtils.getJSONAttrDate(json,'updated')
        domain.comment = JSONUtils.getJSONAttrStr(json,'comment')
        domain.color = JSONUtils.getJSONAttrStr(json,'color')
        domain.ontology = JSONUtils.getJSONAttrDomain(json, "ontology", new Ontology(), true)

        if (!domain.name) {
            throw new WrongArgumentException("Term name cannot be null")
        }
        return domain;
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    def getCallBack() {
        return [ontologyID: this?.ontology?.id]
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        returnArray['comment'] = domain?.comment
        returnArray['ontology'] = domain?.ontology?.id
        try {returnArray['rate'] = domain?.rate} catch (Exception e) {}
        try {
            RelationTerm rt = RelationTerm.findByRelationAndTerm2(Relation.findByName(RelationTerm.names.PARENT), Term.read(domain?.id))
            returnArray['parent'] = rt?.term1?.id
        } catch (Exception e) {}

        if (domain?.color) returnArray['color'] = domain?.color
        return returnArray
    }

    public boolean equals(Object o) {
        if (!o) {
            return false
        } else if (!o instanceof Term) {
            return false
        } else {
            try {return ((Term) o).getId() == this.getId()} catch (Exception e) { return false}
        }

    }
    
    String toString() {
        name
    }

    int compareTo(Object t) {
        return this.name.compareTo(((Term)t).name)
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return ontology.container();
    }
}
