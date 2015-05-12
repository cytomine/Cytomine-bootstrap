package be.cytomine.utils

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.security.SecUser

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
        def signature = rawHmac.encodeBase64().toString()
        return signature
    }
}
