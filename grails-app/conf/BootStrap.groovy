import be.cytomine.security.User
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole

class BootStrap {
    def springSecurityService
    def init = { servletContext ->
      def usersSamples = [
              'rmaree' : [ firstname : 'Raphaël', lastname : 'Marée', email : 'rmaree@ulg.ac.be'],
              'rlrollus' : [  firstname : 'Loïc', lastname : 'Rollus', email : 'lrollus@ulg.ac.be'],
              'stevben' : [  firstname : 'Benjamin', lastname : 'Stévens', email : 'bstevens@ulg.ac.be'],
      ]

      def userRole = SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority : "ROLE_USER").save()
      def adminRole = SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority : "ROLE_ADMIN").save()

      def users = User.list() ?: []
      if (!users) {
        usersSamples.each { username, profilteAttrs ->
          def user = new User(
                  username : username,
                  firstname : profilteAttrs.firstname,
                  lastname : profilteAttrs.lastname,
                  email : profilteAttrs.email,
                  password : springSecurityService.encodePassword("password"),
                  dateCreated : new Date(),
                  enabled : true)
          if (user.validate()) {
            println "Creating user ${username}..."

            user.save(flush : true)

            SecUserSecRole.create(user, userRole)
            SecUserSecRole.create(user, adminRole)

            users << user
          } else {
            println("\n\n\n Errors in account boostrap for ${username}!\n\n\n")
            user.errors.each {
              err -> println err
            }
          }
        }
      }


    }
    def destroy = {
    }
}
