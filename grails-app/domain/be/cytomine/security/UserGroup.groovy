package be.cytomine.security

class UserGroup {

  User user
  Group group

  static mapping = {
    version false
  }

  static UserGroup link(User user, Group group) {
    def userGroup = UserGroup.findByUserAndGroup(user, group)
    if (!userGroup) {
      userGroup = new UserGroup()
      user?.addToUserGroup(userGroup)
      group?.addToUserGroup(userGroup)
      userGroup.save(flush : true)
    }
  }

  static void unlink(User user, Group group) {
    def userGroup = UserGroup.findByUserAndGroup(user, group)
    if (userGroup) {
      user?.removeFromUserGroup(userGroup)
      group?.removeFromUserGroup(userGroup)
      userGroup.delete(flush : true)
    }
  }
}
