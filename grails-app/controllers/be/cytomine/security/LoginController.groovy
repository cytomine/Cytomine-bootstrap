package be.cytomine.security

import be.cytomine.api.RestController
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter

import javax.servlet.http.HttpServletResponse

class LoginController extends RestController {

    def secUserService
    def currentRoleServiceProxy
    def cytomineService

    static final long ONE_MINUTE_IN_MILLIS=60000;//millisecs

    /**
     * Dependency injection for the authenticationTrustResolver.
     */
    def authenticationTrustResolver

    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService
    def notificationService

    def loginWithoutLDAP () {
        if (springSecurityService.isLoggedIn()) {
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        }
        else {
            render(view:'login')
        }

        //render view: "index", model: [postUrl: postUrl,
        //   rememberMeParameter: config.rememberMe.parameter]
    }

    /**
     * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
     */
    def index () {
        if (springSecurityService.isLoggedIn()) {
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        }
        else {
            redirect action: "auth", params: params
        }
    }

    /**
     * Show the login page.
     */
    def auth () {
        def config = SpringSecurityUtils.securityConfig

        if (springSecurityService.isLoggedIn()) {
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }
        String view = 'auth'
        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
        render view: view, model: [postUrl: postUrl,
                rememberMeParameter: config.rememberMe.parameter]
    }

    /**
     * Show denied page.
     */
    def denied () {
        if (springSecurityService.isLoggedIn() &&
                authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
            // have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
            redirect action: "full", params: params
        }
    }

    /**
     * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
     */
    def full () {
        def config = SpringSecurityUtils.securityConfig
        render view: 'auth', params: params,
                model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
                        postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
    }

    /**
     * Callback after a failed login. Redirects to the auth page with a warning message.
     */
    def authfail () {
        log.info "springSecurityService.isLoggedIn()="+springSecurityService.isLoggedIn()
        def msg = ''
        def exception = session[AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY]
        if (exception) {
            //:todo put error messages in i18n
            if (exception instanceof AccountExpiredException) {
                msg = "Account expired"
            }
            else if (exception instanceof CredentialsExpiredException) {
                msg = "Password expired"
            }
            else if (exception instanceof DisabledException) {
                msg = "Account disabled"
            }
            else if (exception instanceof LockedException) {
                msg = "Account locked"
            }
            else {
                msg = "Bad login or password"
            }
        }

        if (springSecurityService.isAjax(request)) {
            response.status = 403
            render([success: false, message: msg] as JSON)
        }

        log.info "CAS="+grailsApplication.config.grails?.plugins?.springsecurity?.cas?.active
        if(grailsApplication.config.grails?.plugins?.springsecurity?.cas?.active) {
            //if cas, first page loaded after subscription redirect here, a refresh will redirect on cytomine web page
            def string = """
            <html>
            <head></head>
            <body>
                Redirection...
                <script>

                      window.setTimeout(function(){
                        window.location = '/';
                      }, 1500);

                </script>
            </body>
            </html>
            """
            response.status = 200
            render string

        }

    }

    /**
     * The Ajax success redirect url.
     */
    /* def ajaxSuccess = {
      response.status = 200
      render([success: true, username: springSecurityService.authentication.name, followUrl : grailsApplication.config.grails.serverURL] as JSON)
    }*/


    def authAjax () {
        response.sendError HttpServletResponse.SC_UNAUTHORIZED
    }

    /**
     * The Ajax success redirect url.
     */
    def ajaxSuccess () {
        User user = User.read(springSecurityService.principal.id)
        render([success: true, id: user.id, fullname: user.firstname + " " + user.lastname] as JSON)
    }

    /**
     * The Ajax denied redirect url.
     */
    def ajaxDenied () {
        render([error: 'access denied'] as JSON)
    }

    def forgotUsername () {
        User user = User.findByEmail(params.j_email)
        if (user) {
            notificationService.notifyForgotUsername(user)
            response([success: true, message: "Check your inbox"], 200)
        } else {
            response([success: false, message: "User not found with email $params.j_email"], 400)
        }
    }

    def loginWithToken() {
        String username = params.username
        String tokenKey = params.tokenKey
        User user = User.findByUsername(username) //we are not logged, we bypass the service


        AuthWithToken authToken = AuthWithToken.findByTokenKeyAndUser(tokenKey, user)
        ForgotPasswordToken forgotPasswordToken = ForgotPasswordToken.findByTokenKeyAndUser(tokenKey, user)

        //check first if a entry is made for this token
        if (authToken && authToken.isValid())  {
            user = authToken.user
            SpringSecurityUtils.reauthenticate user.username, null
            if (params.redirect) {
                redirect (uri : params.redirect)
            } else {
                redirect (uri : "/")
            }
        } else if (forgotPasswordToken && forgotPasswordToken.isValid())  {
            user = forgotPasswordToken.user
            user.setPasswordExpired(true)
            user.save(flush :  true)
            SpringSecurityUtils.reauthenticate user.username, null
            if (params.redirect) {
                redirect (uri : params.redirect)
            } else {
                redirect (uri : "/")
            }

        } else {
            response([success: false, message: "Error : token invalid"], 400)
        }
    }

    def buildToken() {
        String username = params.username
        Double validityMin = params.double('validity',60d)
        User user = User.findByUsername(username)

        if(currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser)) {
            String tokenKey = UUID.randomUUID().toString()
            AuthWithToken token = new AuthWithToken(
                    user : user,
                    expiryDate: new Date((long)new Date().getTime() + (validityMin * ONE_MINUTE_IN_MILLIS)),
                    tokenKey: tokenKey
            ).save(flush : true)
            response([success: true, token:token], 200)
        } else {
            response([success: false, message: "You must be an admin/superadmin!"], 403)
        }

    }

    def forgotPassword () {
        String username = params.j_username
        if (username) {
            User user = User.findByUsername(username) //we are not logged, so we bypass the service
            if (user) {
                String tokenKey = UUID.randomUUID().toString()
                ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(
                        user : user,
                        expiryDate: new Date() + 1, //tomorrow
                        tokenKey: tokenKey
                ).save(flush : true)

                notificationService.notifyForgotPassword(user, forgotPasswordToken)

                response([success: true, message: "Check your inbox"], 200)
            }

        }
    }

}
