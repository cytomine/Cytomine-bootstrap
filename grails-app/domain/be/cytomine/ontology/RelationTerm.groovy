package be.cytomine.ontology

import grails.converters.JSON

class RelationTerm implements Serializable{

  Relation relation
  Term term1
  Term term2


  String toString()
  {
    "[" + this.id + " <" + relation + ":[" + term1 + ","+ term2 +"]>]"
  }


  static RelationTerm link(long id,Relation relation,Term term1,Term term2) {
    println "Link Term " + term1.id + " with Term " + term2.id + " with relation " + relation.id
    def relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)
    if (!relationTerm) {
      println "LINKED"
      relationTerm = new RelationTerm()
      relationTerm.id = id!=-1? id : null
      term1?.addToRelationTerm1(relationTerm)
      term2?.addToRelationTerm2(relationTerm)
      relation?.addToRelationTerm(relationTerm)
      println "save relationTerm"
      relationTerm.save(flush : true)
    } else throw new IllegalArgumentException("Term1 " + term1.id + " and " + term2.id + " are already mapped with relation " + relation.id)
    return relationTerm
  }

  static RelationTerm link(Relation relation, Term term1, Term term2) {
       link(-1,relation,term1,term2)
  }

  static void unlink(Relation relation, Term term1, Term term2) {
    println "Unlink Term " + term1.id + " with Term " + term2.id + " with relation " + relation.id
    def relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)
    println "unlink relationTerm ="+ relationTerm
    if (relationTerm) {
      term1?.removeFromRelationTerm1(relationTerm)
      term2?.removeFromRelationTerm2(relationTerm)
      relation?.removeFromRelationTerm(relationTerm)
      println "relationTerm delete"
      println "***"
      RelationTerm.list().each{println it}
      println "***"
      Term.list().each{println it}
      relationTerm.delete(flush : true)
      println "***"
      RelationTerm.list().each{println it}
      println "***"
      Term.list().each{println it}
    }

  }

  static RelationTerm createRelationTermFromData(jsonRelationTerm) {
    def relationTerm = new RelationTerm()
    getRelationTermFromData(relationTerm,jsonRelationTerm)
  }

  static RelationTerm getRelationTermFromData(relationTerm,jsonRelationTerm) {
    relationTerm.relation = Relation.get(jsonRelationTerm.relation.id)
    relationTerm.term1 = Term.get(jsonRelationTerm.term1.id)
    relationTerm.term2 = Term.get(jsonRelationTerm.term2.id)
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

 /* static void unlink(long id) {
    def relationTerm = RelationTerm.get(id)
    def term1 = relationTerm.term1
    def term2 = relationTerm.term2
    def relation =relationTerm.relation
    if (relationTerm) {
      term1?.removeFromRelationTerm1(relationTerm)
      term2?.removeFromRelationTerm2(relationTerm)
      relation?.removeFromRelationTerm(relationTerm)
      relationTerm.delete(flush : true)
    }

  }  */

}
