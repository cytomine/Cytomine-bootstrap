package be.cytomine.processing

import be.cytomine.security.User

class SoftwareUsers {

    Software software
    User user

    static mapping = {
        version false
    }

    static SoftwareUsers link(Software software, User user) {
        def softwareUsers = SoftwareUsers.findBySoftwareAndUser(software, user)
        if (!softwareUsers) {
            softwareUsers = new SoftwareProject()
            software?.addToSoftwareProjects(softwareUsers)
            user?.addToSoftwareProjects(softwareUsers)
            softwareUsers.save(flush: true)
        }
    }

    static void unlink(Software software, User user) {
        def softwareUsers = SoftwareUsers.findBySoftwareAndUser(software, user)
        if (softwareUsers) {
            software?.removeFromSoftwareProjects(softwareUsers)
            user?.removeFromSoftwareProjects(softwareUsers)
            softwareUsers.delete(flush: true)
        } else {println "no link between " + software + " " + user}
    }
}
