package cytomine.web

import be.cytomine.security.AuthWithToken
import be.cytomine.security.ForgotPasswordToken
import be.cytomine.security.User
import grails.plugin.springsecurity.SpringSecurityUtils

class SecurityFilters {
    def springSecurityService

    def dependsOn = [APIAuthentificationFilters]

    def filters = {

        api(uri:'/api/**') {
            before = {

                if(!springSecurityService.isLoggedIn()) {
                    redirect(uri:'/')
                    return false
                }
            }
            after = {

            }
            afterView = {

            }
        }
    }

}


