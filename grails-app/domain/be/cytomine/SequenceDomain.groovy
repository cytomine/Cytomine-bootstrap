package be.cytomine

abstract class SequenceDomain {

  def sequenceService

  Date created
  Date updated

  static constraints = {
    created nullable:true
    updated nullable:true
  }

  public beforeInsert() {
    created = new Date()
    if (id == null)
      id = sequenceService.generateID(this)
  }

  public beforeUpdate() {
    updated = new Date()
  }
}
