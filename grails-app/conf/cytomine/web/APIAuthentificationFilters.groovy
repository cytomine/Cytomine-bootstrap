package cytomine.web

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import org.springframework.security.core.codec.Base64
import be.cytomine.security.SecUser
import javax.servlet.FilterConfig
import javax.servlet.FilterChain
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class APIAuthentificationFilters implements javax.servlet.Filter {

    void init(FilterConfig filterConfig) {

    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        tryAPIAuthentification(request, response)
        chain.doFilter(request, response);
    }

    void destroy() {}

    /**
     * http://code.google.com/apis/storage/docs/reference/v1/developer-guidev1.html#authentication
     */
    private void tryAPIAuthentification(HttpServletRequest request, HttpServletResponse response) {

        String authorization = request.getHeader("authorization")

        if (request.getHeader("date") == null) {
            //println "Date Header Must be set"
            return
        }
        if (request.getHeader("host") == null) {
            println "Host Header Must be set"
            return
        }
        if (authorization == null) {
            println "Authorization Header must be set"
            return
        }
        if (!authorization.startsWith("CYTOMINE") || !authorization.indexOf(" ") == -1 || !authorization.indexOf(":") == -1) {
            println "Authorization Header is not valid"
            return
        }
        try {

            String content_md5 = (request.getHeader("content-MD5") != null) ? request.getHeader("content-MD5") : ""
            String content_type = (request.getHeader("content-type") != null) ? request.getHeader("content-type") : ""
            String date = (request.getHeader("date") != null) ? request.getHeader("date") : ""
            String canonicalHeaders = request.getMethod() + "\n" + content_md5 + "\n" + content_type + "\n" + date + "\n"
            String canonicalExtensionHeaders = ""
            String queryString = (request.getQueryString() != null) ? "?" + request.getQueryString() : ""
            String path = request.forwardURI //original URI Request
            String canonicalResource = path + queryString
            String messageToSign = canonicalHeaders + canonicalExtensionHeaders + canonicalResource
            String signature = ""
            String method = authorization.substring(0, authorization.indexOf(" "))
            String accessKey = authorization.substring(authorization.indexOf(" ") + 1, authorization.indexOf(":"))
            String authorizationSign = authorization.substring(authorization.indexOf(":") + 1)
            SecUser user = SecUser.findByPublicKey(accessKey)
            if (!user) {
                println "No private key associated with this public key"
                return
            }
            String key = user.getPrivateKey()
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1")
            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1")
            mac.init(signingKey)
            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(new String(messageToSign.getBytes(), "UTF-8").getBytes())
            // base64-encode the hmac
            byte[] signatureBytes = Base64.encode(rawHmac)
            signature = new String(signatureBytes)
            if (authorizationSign == signature) {
                SpringSecurityUtils.reauthenticate user.getUsername(), null
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    def filters = {
        all(uri:'/api/**') {
            before = {

            }
            after = {

            }
            afterView = {

            }
        }
    }

}
