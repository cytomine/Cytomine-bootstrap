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
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField

/**
 * Relation between a term 1 and a term 2
 */
//@RestApiObject(name = "relationTerm", description = "Relation Term description", show = true)
@RestApiObject(name = "relation", description = "Relation between a term 1 , a term 2 and a relantion domain (e.g. term1 PARENT term2)")
class RelationTerm extends CytomineDomain implements Serializable {

    static names = [PARENT: "parent", SYNONYM: "synonyme"]

    @RestApiObjectField(description = "The relation")
    Relation relation

    @RestApiObjectField(description = "The first term")
    Term term1

    @RestApiObjectField(description = "The second term")
    Term term2

    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
    }

    String toString() {
        "[" + this.id + " <" + relation + '(' + relation?.name + ')' + ":[" + term1 + '(' + term1?.name + ')' + "," + term2 + '(' + term2?.name + ')' + "]>]"
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static RelationTerm insertDataIntoDomain(def json, def domain = new RelationTerm()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.relation = JSONUtils.getJSONAttrDomain(json, "relation", new Relation(), true)
        domain.term1 = JSONUtils.getJSONAttrDomain(json, "term1", new Term(), true)
        domain.term2 = JSONUtils.getJSONAttrDomain(json, "term2", new Term(), true)
        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['relation'] = domain?.relation?.id
        returnArray['term1'] = domain?.term1?.id
        returnArray['term2'] = domain?.term2?.id
        return returnArray
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return term1.container();
    }

    void checkAlreadyExist() {
        RelationTerm.withNewSession {
            if(relation && term1 && term2) {
                RelationTerm rt = RelationTerm.findByRelationAndTerm1AndTerm2(relation,term1,term2)
                if(rt!=null && (rt.id!=id))  {
                    throw new AlreadyExistException("RelationTerm with relation=${relation.id} and term1 ${term1.id} and term2 ${term2.id} already exist!")
                }
            }
        }
    }


}
