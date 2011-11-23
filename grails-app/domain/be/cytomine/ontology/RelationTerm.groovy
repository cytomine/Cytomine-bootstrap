package be.cytomine.ontology

import be.cytomine.Exception.WrongArgumentException
import be.cytomine.SequenceDomain
import grails.converters.JSON

class RelationTerm extends SequenceDomain implements Serializable {

    static names = [PARENT: "parent", SYNONYM: "synonyme"]

    Relation relation
    Term term1
    Term term2
    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    def getIdTerm1() {
        if (this.term1Id) return this.term1Id
        else return this.term1?.id
    }

    String toString() {
        "[" + this.id + " <" + relation + '(' + relation?.name + ')' + ":[" + term1 + '(' + term1?.name + ')' + "," + term2 + '(' + term2?.name + ')' + "]>]"
    }

    static RelationTerm link(long id, Relation relation, Term term1, Term term2) {
        if (!relation) throw new WrongArgumentException("Relation cannot be null");
        if (!term1) throw new WrongArgumentException("Term 1 cannot be null");
        if (!term2) throw new WrongArgumentException("Term 2 cannot be null");

        println "Link Term " + term1.id + " with Term " + term2.id + " with relation " + relation.id
        def relationTerm = RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
        if (!relationTerm) {
            relationTerm = new RelationTerm()
            if (id != -1) relationTerm.id = id
            term1?.addToRelationTerm1(relationTerm)
            term2?.addToRelationTerm2(relationTerm)
            relation?.addToRelationTerm(relationTerm)
            term1.refresh()
            term2.refresh()
            relation.refresh()
            println"relationTerm.id="+relationTerm.id
            relationTerm.save(flush: true)
            println "relationTerm.id="+relationTerm.id
        } else throw new WrongArgumentException("Term1 " + term1.id + " and " + term2.id + " are already mapped with relation " + relation.id)
        return relationTerm
    }

    static RelationTerm link(Relation relation, Term term1, Term term2) {
        link(-1, relation, term1, term2)
    }

    static void unlink(Relation relation, Term term1, Term term2) {
        println "Unlink Term " + term1.id + " with Term " + term2.id + " with relation " + relation.id
        def relationTerm = RelationTerm.findWhere('relation': relation, 'term1': term1, 'term2': term2)
        if (relationTerm) {
            term1?.removeFromRelationTerm1(relationTerm)
            term2?.removeFromRelationTerm2(relationTerm)
            relation?.removeFromRelationTerm(relationTerm)
            relationTerm.delete(flush: true)
        }

    }

    static RelationTerm createFromDataWithId(json)  {
        def domain = createFromData(json)
        domain.id = json.id
        return domain
    }

    static RelationTerm createFromData(jsonRelationTerm) {
        def relationTerm = new RelationTerm()
        getFromData(relationTerm, jsonRelationTerm)
    }

    static RelationTerm getFromData(relationTerm, jsonRelationTerm) {
        println "jsonRelationTerm=" + jsonRelationTerm.toString()
        try {
            println "jsonRelationTerm.xxx.id"
            relationTerm.relation = Relation.get(jsonRelationTerm.relation.id)
            relationTerm.term1 = Term.get(jsonRelationTerm.term1.id)
            relationTerm.term2 = Term.get(jsonRelationTerm.term2.id)
        }
        catch (Exception e) {
            println "jsonRelationTerm.idXXX"
            relationTerm.relation = Relation.get(jsonRelationTerm.relation)
            relationTerm.term1 = Term.get(jsonRelationTerm.term1)
            relationTerm.term2 = Term.get(jsonRelationTerm.term2)
        }
        return relationTerm;
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + RelationTerm.class
        JSON.registerObjectMarshaller(RelationTerm) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['relation'] = it.relation
            returnArray['term1'] = it.term1
            returnArray['term2'] = it.term2

            return returnArray
        }
    }

}
