package be.cytomine.utils

import be.cytomine.security.SecUser
import org.springframework.security.core.codec.Base64

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * User: lrollus
 * Date: 25/09/13
 * GIGA-ULg
 *
 */
class SecurityUtils {

    public static String generateKeys(String method, String content_md5, String content_type, String date, String queryString, String path,SecUser user) {
        String canonicalHeaders = method + "\n" + content_md5 + "\n" + content_type + "\n" + date + "\n"
//            println "canonicalHeaders="+canonicalHeaders
            String canonicalExtensionHeaders = ""
        String canonicalResource = path + queryString
        String messageToSign = canonicalHeaders + canonicalExtensionHeaders + canonicalResource

        String key = user.getPrivateKey()
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1")
        // get an hmac_sha1 Mac instance and initialize with the signing key
        Mac mac = Mac.getInstance("HmacSHA1")
        mac.init(signingKey)
        // compute the hmac on input data bytes
        byte[] rawHmac = mac.doFinal(new String(messageToSign.getBytes(), "UTF-8").getBytes())

        // base64-encode the hmac
        byte[] signatureBytes = Base64.encode(rawHmac)
        def signature = new String(signatureBytes)
        return signature
    }
}
