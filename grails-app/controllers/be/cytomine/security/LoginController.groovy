package be.cytomine.security

import grails.converters.JSON
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter

import javax.servlet.http.HttpServletResponse

class LoginController {

    /**
     * Dependency injection for the authenticationTrustResolver.
     */
    def authenticationTrustResolver

    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService

    def loginWithoutLDAP = {
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
    def index = {
        if (springSecurityService.isLoggedIn()) {
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        }
        else {
            redirect action: auth, params: params
        }
    }

    /**
     * Show the login page.
     */
    def auth = {
        def config = SpringSecurityUtils.securityConfig

        if (springSecurityService.isLoggedIn()) {
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }
        println "render"
        String view = 'auth'
        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
        render view: view, model: [postUrl: postUrl,
                rememberMeParameter: config.rememberMe.parameter]
    }

    /**
     * Show denied page.
     */
    def denied = {
        if (springSecurityService.isLoggedIn() &&
                authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
            // have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
            redirect action: full, params: params
        }
    }

    /**
     * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
     */
    def full = {
        def config = SpringSecurityUtils.securityConfig
        render view: 'auth', params: params,
                model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
                        postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
    }

    /**
     * Callback after a failed login. Redirects to the auth page with a warning message.
     */
    def authfail = {

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

    }

    /**
     * The Ajax success redirect url.
     */
    /* def ajaxSuccess = {
      response.status = 200
      render([success: true, username: springSecurityService.authentication.name, followUrl : grailsApplication.config.grails.serverURL] as JSON)
    }*/


    def authAjax = {
        response.sendError HttpServletResponse.SC_UNAUTHORIZED
    }

    /**
     * The Ajax success redirect url.
     */
    def ajaxSuccess = {
        User user = User.read(springSecurityService.principal.id)
        render([success: true, id: user.id, fullname: user.firstname + " " + user.lastname] as JSON)
    }

    /**
     * The Ajax denied redirect url.
     */
    def ajaxDenied = {
        render([error: 'access denied'] as JSON)
    }

}
