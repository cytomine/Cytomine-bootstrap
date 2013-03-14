package be.cytomine.utils.bootstrap

import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.security.Group
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import be.cytomine.test.Infos

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/03/13
 * Time: 11:59
 */
class BootstrapUtilsService {

    public def createUsers(def usersSamples) {

        SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority: "ROLE_USER").save(flush: true)
        SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority: "ROLE_ADMIN").save(flush: true)

        usersSamples.each { item ->
            User user = User.findByUsername(item.username)
            if (user)  return
            user = new User(
                    username: item.username,
                    firstname: item.firstname,
                    lastname: item.lastname,
                    email: item.email,
                    color: item.color,
                    password: item.password,
                    enabled: true)
            user.generateKeys()


            log.info "Before validating ${user.username}..."
            if (user.validate()) {
                log.info "Creating user ${user.username}..."

                try {user.save(flush: true) } catch(Exception e) {println e}
                log.info "Save ${user.username}..."

                /* Create a special group the user */
                def userGroupName = item.username
                def userGroup = [
                        [name: userGroupName]
                ]
                createGroups(userGroup)
                Group group = Group.findByName(userGroupName)
                UserGroup ug = new UserGroup(user:user, group:group)
                ug.save(flush:true,failOnError: true)

                /* Handle groups */
                item.group.each { elem ->
                    def newGroup = [
                            [name: elem.name]
                    ]
                    createGroups(newGroup)
                    log.info "Fetch group " + elem.name
                    group = Group.findByName(elem.name)
                    ug = new UserGroup(user:user, group:group)
                    ug.save(flush:true,failOnError: true)
                }

                /* Add Roles */
                item.roles.each { authority ->
                    log.info "Add SecRole " + authority + " for user " + user.username
                    SecRole secRole = SecRole.findByAuthority(authority)
                    if (secRole) SecUserSecRole.create(user, secRole)
                }

            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                user.errors.each {
                    err -> log.info(err)
                }
            }
        }
    }

    public def createGroups(groupsSamples) {
        groupsSamples.each { item ->
            if (Group.findByName(item.name)) return
            def group = new Group(name: item.name)
            if (group.validate()) {
                log.info("Creating group ${group.name}...")
                group.save(flush: true)
                log.info("Creating group ${group.name}... OK")
            }
            else {
                log.info("\n\n\n Errors in group boostrap for ${item.name}!\n\n\n")
                group.errors.each {
                    err -> log.info err
                }
            }
        }
    }

    public def createRelation() {
        def relationSamples = [
                [name: RelationTerm.names.PARENT],
                [name: RelationTerm.names.SYNONYM]
        ]

        log.info("createRelation")
        relationSamples.each { item ->
            if (Relation.findByName(item.name)) return
            def relation = new Relation(name: item.name)
            log.info("create relation=" + relation.name)

            if (relation.validate()) {
                log.info("Creating relation : ${relation.name}...")
                relation.save(flush: true)

            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                relation.errors.each {
                    err -> log.info err
                }

            }
        }
    }
}
