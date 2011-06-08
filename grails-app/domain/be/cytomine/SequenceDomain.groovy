package be.cytomine

abstract class SequenceDomain {

  def sequenceService
  Long id
  Date created
  Date updated

  static mapping = {
    id generator : "assigned"
  }

  static constraints = {
    created nullable:true
    updated nullable:true
  }

  public beforeInsert() {
    created = new Date()
    println "beforeInsert in " + this.class.getName()
    if (id == null)
      id = sequenceService.generateID(this)
  }

  public beforeUpdate() {
    updated = new Date()
  }
}
