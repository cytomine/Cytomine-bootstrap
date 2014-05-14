package cytomine.web

class SecurityFilters {
    def springSecurityService
    def currentRoleServiceProxy

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
//
//        api(uri:'/monitoring/**') {
//            before = {
//                if(!currentRoleServiceProxy.isAdminByNow()) {
//                    redirect(uri:'/')
//                    return false
//                }
//            }
//        }
    }


//    grails.plugins.springsecurity.interceptUrlMap = [
//    '/admin/**':    ['ROLE_ADMIN','ROLE_SUPER_ADMIN'],
//    '/monitoring/**':    ['ROLE_ADMIN','ROLE_SUPER_ADMIN'],
//    '/j_spring_security_switch_user': ['ROLE_ADMIN','ROLE_SUPER_ADMIN'],
//    '/securityInfo/**': ['ROLE_ADMIN','ROLE_SUPER_ADMIN'],
//    '/api/**':      ['IS_AUTHENTICATED_REMEMBERED'],
//    '/lib/**':      ['IS_AUTHENTICATED_ANONYMOUSLY'],
//    '/css/**':      ['IS_AUTHENTICATED_ANONYMOUSLY'],
//    '/images/**':   ['IS_AUTHENTICATED_ANONYMOUSLY'],
//    '/*':           ['IS_AUTHENTICATED_REMEMBERED'], //if cas authentication, active this      //beta comment
//    '/login/**':    ['IS_AUTHENTICATED_ANONYMOUSLY'],
//    '/logout/**':   ['IS_AUTHENTICATED_ANONYMOUSLY'],
//    ]

}


