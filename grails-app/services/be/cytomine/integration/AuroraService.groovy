package be.cytomine.integration

import be.cytomine.image.AbstractImage
import be.cytomine.ontology.Property
import groovy.sql.Sql
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseException

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.InvalidKeyException

import static groovyx.net.http.ContentType.JSON

/**
 * Created by lrollus on 6/17/14.
 */
class AuroraService {

    def dataSource
    def propertyService
    def grailsApplication

    public static String PATIENT_ID = "patient_id"
    public static String SAMPLE_ID = "sample_type"
    public static String IMAGE_TYPE = "image_type"
    public static String VERSION = "version"
    public static String NOTIFICATION = "notification"
    public static String IMAGE = "image_id"

    public void notifyImage() {
        println "notify Aurora!"
        //Get all images with PATIENT_ID, SAMPLE_ID, IMAGE_TYPE and VERSION and with NO NOTIFICATION

        //SEND HTTP POST TO AURORA
        //REQUEST: [ {PATIENT_ID: xxx, SAMPLE_ID: xxx, ..., image: {xxx}}, {....}   ]
            List<AbstractImage> imagesToNotify = getAuroraImageNotYetNotificated()
            if(!imagesToNotify.isEmpty()) {
                imagesToNotify.each {
                    try {
                        def json = doHTTPRequestToAurora(it)
                    } catch(Exception e) {
                        log.error "Aurora cannot be notify!"
                        log.error e
                        e.printStackTrace()
                    }
                }
            }
    }

    public def doHTTPRequestToAurora(AbstractImage imageToNotify) {
        println "doHTTPRequestToAurora > ${imageToNotify.id}"

        def postData = createPostBody(imageToNotify) //'{"image_type": "HER2", "abstract_image_id": 122, "sample_type": "Primary", "patient_id": "0002"}'

        long timeComp = System.currentTimeMillis() / 1000;
        String endpoint = "${grailsApplication.config.grails.integration.aurora.path}$timeComp/${grailsApplication.config.grails.integration.aurora.pub}/"
        def hash = hmac_sha256(grailsApplication.config.grails.integration.aurora.priv, endpoint+postData)
        String request_signature = bytesToHex(hash)//hash.encodeBase64().toString()
        String signed_endpoint = endpoint + request_signature + "/"

        println grailsApplication.config.grails.integration.aurora.url
        println "postData=$postData"
        println "signed_endpoint=$signed_endpoint"
        def http = new HTTPBuilder( grailsApplication.config.grails.integration.aurora.url )
        try {
            http.post(
                    path: signed_endpoint,
                    body: postData,
                    requestContentType: groovyx.net.http.ContentType.JSON) { resp ->
                resp.properties.each {
                    println it.key + "=" + it.value
                }
                println "POST Success: ${resp.status}"
                int code = resp.status
                if(code==200 || code==201 || code==202 || code==203 || code==204) {
                    log.info "RESPONSE: $code => $resp"
                    setImageNotification(imageToNotify,new Date().getTime().toString())
                } else {
                    log.error "RESPONSE: $code => $resp"
                }
            }
        }
        catch ( HttpResponseException ex ) {
            // default failure handler throws an exception:
            println "Unexpected response error: ${ex.statusCode}"
            ex.response.properties.each {
                println it.key + "=" + it.value
            }
            println "Unexpected response error: ${ex.message}"
        }
    }


    def hmac_sha256(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1")
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1")
            mac.init(secretKeySpec)
            byte[] digest = mac.doFinal(data.getBytes())
            return digest
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key exception while converting to HMac SHA256")
        }
    }

    public String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public String createPostBody(AbstractImage image) {
        return '{"image_type": "'+Property.findByDomainIdentAndKey(image.id,IMAGE_TYPE)?.value+'", ' +
                '"abstract_image_id": '+image.id+', ' +
                '"sample_type": "'+Property.findByDomainIdentAndKey(image.id,SAMPLE_ID)?.value+'", ' +
                '"patient_id": "'+Property.findByDomainIdentAndKey(image.id,PATIENT_ID)?.value+'"}'
    }

    public List<AbstractImage> getAuroraImageNotYetNotificated() {
        List<AbstractImage> images = AbstractImage.findAllByIdInList(getAuroraImageIdNotYetNotificated())
        return images
    }

    private List<Long> getAuroraImageIdNotYetNotificated() {
        String request = """
            SELECT abstract_image.id
            FROM abstract_image
            WHERE abstract_image.id IN (SELECT domain_ident FROM property WHERE key like '$PATIENT_ID')
            AND abstract_image.id IN (SELECT domain_ident FROM property WHERE key like '$SAMPLE_ID')
            AND abstract_image.id IN (SELECT domain_ident FROM property WHERE key like '$IMAGE_TYPE')
            AND abstract_image.id NOT IN (SELECT domain_ident FROM property WHERE key like '$NOTIFICATION');"""

        def data = []
        def sql = new Sql(dataSource)
        sql.eachRow(request) {
            data << it[0]
        }
        try {
            sql.close()
        }catch (Exception e) {}
        log.info "getAuroraImageIdNotYetNotificated=$data"
        data.sort()
    }

    private void setImageNotification(AbstractImage image, String notificationValue) {
        Property property = new Property()
        property.setDomain(image)
        property.key = NOTIFICATION
        property.value = notificationValue
        propertyService.add(JSON.parse(property.encodeAsJSON()))
    }
}
