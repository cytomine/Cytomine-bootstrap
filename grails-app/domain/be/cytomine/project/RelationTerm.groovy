package be.cytomine.project
import grails.converters.JSON
class RelationTerm {

  Relation relation
  Term term1
  Term term2

  static mapping = {
    version false
  }

  static RelationTerm link(Relation relation, Term term1, Term term2) {
    def relationTerm = RelationTerm.linkNoSave(relation,term1,term2)
    relationTerm.save(flush : true)
    println "link=" +  relationTerm.errors
    return relationTerm
  }

  static RelationTerm link(long id,Relation relation, Term term1, Term term2) {
    def relationTerm = RelationTerm.linkNoSave(relation,term1,term2)
    relationTerm.id = id
    println "link id=" + id
    RelationTerm.list().each {println it.id}


    relationTerm.save(flush : true)
    println "link=" +  relationTerm.errors
    return relationTerm
  }

  static RelationTerm linkNoSave(Relation relation, Term term1, Term term2) {
    println "Link Term " + term1 + " with Term " + term2 + " with relation " + relation
    def relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)
    if (!relationTerm) {
      println "LINKED"
      relationTerm = new RelationTerm()
      term1?.addToRelationTerm1(relationTerm)
      term2?.addToRelationTerm2(relationTerm)
      relation?.addToRelationTerm(relationTerm)
    }
    return relationTerm
  }

  static void unlink(Relation relation, Term term1, Term term2) {
    def relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)
    if (relationTerm) {
      term1?.removeFromRelationTerm1(relationTerm)
      term2?.removeFromRelationTerm2(relationTerm)
      relation?.removeFromRelationTerm(relationTerm)
      relationTerm.delete(flush : true)
    }

  }

  static void unlink(long id) {
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

  }

  static RelationTerm createRelationTermFromData(jsonRelationTerm) {
    def relationTerm = new RelationTerm()
    getRelationTermFromData(relationTerm,jsonRelationTerm)
  }

  static RelationTerm getRelationTermFromData(relationTerm,jsonRelationTerm) {
    //TODO: check constraint
    println "jsonRelationTerm from getRelationTermFromData = " + jsonRelationTerm
    def relation = Relation.get(jsonRelationTerm.relation.id)
    def term1 = Term.get(jsonRelationTerm.term1.id)
    def term2 = Term.get(jsonRelationTerm.term2.id)

    relationTerm.relation = relation
    relationTerm.term1 = term1
    relationTerm.term2 = term2

    //relationTerm = RelationTerm.linkNoSave(relation,term1,term2)

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
