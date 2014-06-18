package be.cytomine.integration

import be.cytomine.image.AbstractImage
import be.cytomine.ontology.Property
import be.cytomine.test.HttpClient
import grails.converters.JSON
import groovy.sql.Sql

/**
 * Created by lrollus on 6/17/14.
 */
class AuroraService {

    def dataSource
    def propertyService
    def grailsApplication

    public static String PATIENT_ID = "patient_id"
    public static String SAMPLE_ID = "sample_id"
    public static String IMAGE_TYPE = "image_type"
    public static String VERSION = "version"
    public static String NOTIFICATION = "notification"
    public static String IMAGE = "image_id"

    public void notifyImage() {
        println "notify Aurora!"

        //Get all images with PATIENT_ID, SAMPLE_ID, IMAGE_TYPE and VERSION and with NO NOTIFICATION


        //SEND HTTP POST TO AURORA
        //REQUEST: [ {PATIENT_ID: xxx, SAMPLE_ID: xxx, ..., image: {xxx}}, {....}   ]
        try {

            def json = doHTTPRequestToAurora()
            processResponse(json)
        } catch(Exception e) {
            log.error "Aurora cannot be notify!"
            log.error e
            e.printStackTrace()
        }
    }

    public String doRequestContent() {
        List<AbstractImage> imagesToNotify = getAuroraImageNotYetNotificated()
        String req = (buildRequest(imagesToNotify) as JSON).toString(true)
        return req
    }

    public def doHTTPRequestToAurora() {

        HttpClient client = new HttpClient()
        client.connect(grailsApplication.config.grails.integration.aurora.url,grailsApplication.config.grails.integration.aurora.username,grailsApplication.config.grails.integration.aurora.password)

        client.post(doRequestContent(), "application/json")
        int code = client.getResponseCode()
        log.info "Aurora code = $code"
        String response = client.getResponseData()
        client.disconnect()

        //RESPONSE: [ {image_id: image, notification: now()} ]
        def json = JSON.parse(response)
        return json
    }

    public def processResponse(def json) {
        json.each { item ->
            try {
                println item
                AbstractImage image = AbstractImage.read(item[IMAGE])
                println "ADD NOTIFICATION ${item[NOTIFICATION]} TO IMAGE $image"
                setImageNotification(image,item[NOTIFICATION])
            } catch(Exception e) {
                log.error e
            }
        }
    }

    private def buildRequest(List<AbstractImage> images) {
        def data = []
        images.each { image ->
            def item = [:]
            item.abstractImage = JSON.parse(image.encodeAsJSON())
            item[PATIENT_ID] = Property.findByDomainIdentAndKey(image.id,PATIENT_ID)?.value
            item[SAMPLE_ID] = Property.findByDomainIdentAndKey(image.id,SAMPLE_ID)?.value
            item[IMAGE_TYPE] = Property.findByDomainIdentAndKey(image.id,IMAGE_TYPE)?.value
            item[VERSION] = Property.findByDomainIdentAndKey(image.id,VERSION)?.value
            log.info "item=$item"
            data << item
        }
        data
    }

    private List<AbstractImage> getAuroraImageNotYetNotificated() {
        List<AbstractImage> images = AbstractImage.findAllByIdInList(getAuroraImageIdNotYetNotificated())
        log.info "getAuroraImageNotYetNotificated=$images"
        return images
    }

    private List<Long> getAuroraImageIdNotYetNotificated() {
        String request = """
            SELECT abstract_image.id
            FROM abstract_image
            WHERE abstract_image.id IN (SELECT domain_ident FROM property WHERE key like 'patient_id')
            AND abstract_image.id IN (SELECT domain_ident FROM property WHERE key like 'sample_id')
            AND abstract_image.id IN (SELECT domain_ident FROM property WHERE key like 'image_type')
            AND abstract_image.id IN (SELECT domain_ident FROM property WHERE key like 'version')
            AND abstract_image.id NOT IN (SELECT domain_ident FROM property WHERE key like 'notification');"""

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
