package be.cytomine.project

class RelationTerm {

  Relation relation
  Term term1
  Term term2

  static mapping = {
    version false
  }

  static RelationTerm link(Term term1, Term term2, Relation relation) {
    println "Link Term " + term1 + " with Term " + term2 + " with relation " + relation
    //def relationTerm = RelationTerm.findByTerm1AndTerm2AndRelation(term1, term2,relation)
    def rel = Relation.findWhere('name':'Parent')
    def relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)
   // def relationTerm = RelationTerm.findWhere('relation': relation.id,'term1':term1.id, 'term2':term2.id)
    if (!relationTerm) {
      println "LINKED"
      relationTerm = new RelationTerm()
      term1?.addToRelationTerm1(relationTerm)
      term2?.addToRelationTerm2(relationTerm)
      relation?.addToRelationTerm(relationTerm)
      relationTerm.save(flush : true)
    }
    return relationTerm
  }

  static void unlink(Term term1, Term term2, Relation relation) {
    def relationTerm = RelationTerm.findWhere('relation': relation,'term1':term1, 'term2':term2)
    if (relationTerm) {
      term1?.addToRelationTerm1(relationTerm)
      term2?.addToRelationTerm2(relationTerm)
      relation?.addToRelationTerm(relationTerm)
      relationTerm.delete(flush : true)
    }

  }
}
