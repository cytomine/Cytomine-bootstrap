package be.cytomine.integration

import be.cytomine.image.AbstractImage
import be.cytomine.ontology.Property
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.DomainAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 17/02/11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
class AuroraTests {


    void testAuroraNotification() {
        def json,result,post


        //add IMAGE1
        AbstractImage image1 = BasicInstanceBuilder.getAbstractImageNotExist(true)

        //add IMAGE2
        AbstractImage image2 = BasicInstanceBuilder.getAbstractImageNotExist(true)

        //add IMAGE3
        AbstractImage image3 = BasicInstanceBuilder.getAbstractImageNotExist(true)

        //Get /aurora/retrieveAurora.json, assert empty
        result = DomainAPI.doGET(Infos.CYTOMINEURL,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert 0==json.size()

        //add property to IMAGE1
        addProperties(image1)

        //add property to IMAGE2
        addProperties(image2)

        //Get /aurora/retrieveAurora.json, assert == 2
        result = DomainAPI.doGET(Infos.CYTOMINEURL+"aurora/retrieveAurora",Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection
        assert 2==json.size()
        assert json[0].abstractImage.id == image1.id
        assert json[1].abstractImage.id == image2.id

        //add property to IMAGE3
        addProperties(image3)

        //Check property notif for <>Image 3
        result = DomainAPI.doGET(Infos.CYTOMINEURL+"aurora/retrieveAurora",Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection
        assert 3==json.size()
        assert json[2].abstractImage.id == image3.id

        //Notification for IMAGE1 AND 2
        post = [[image_id:image1.id,notification:new Date().toString()],[image_id:image2.id,notification:new Date().toString()]]
        result = DomainAPI.doPOST(Infos.CYTOMINEURL+"aurora/markNotifyAurora",(post as JSON).toString(true),Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //Get /aurora/retrieveAurora.json, assert == 1
        result = DomainAPI.doGET(Infos.CYTOMINEURL+"aurora/retrieveAurora",Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection
        assert 1==json.size()
        assert json[0].abstractImage.id == image3.id

        //Notification for  3
        post = [[image_id:image3.id,notification:new Date().toString()]]
        result = DomainAPI.doPOST(Infos.CYTOMINEURL+"aurora/markNotifyAurora",(post as JSON).toString(true),Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //Get /aurora/retrieveAurora.json, assert == 1
        result = DomainAPI.doGET(Infos.CYTOMINEURL+"aurora/retrieveAurora",Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection
        assert 0==json.size()

    }


    private void addProperties(AbstractImage image) {
        new Property(domainIdent: image.id, domainClassName: image.class.name, key: AuroraService.PATIENT_ID,value: "123456789").save(flush: true,failOnError: true)
        new Property(domainIdent: image.id, domainClassName: image.class.name, key: AuroraService.SAMPLE_ID,value: "123456789").save(flush: true,failOnError: true)
        new Property(domainIdent: image.id, domainClassName: image.class.name, key: AuroraService.IMAGE_TYPE,value: "123456789").save(flush: true,failOnError: true)
        new Property(domainIdent: image.id, domainClassName: image.class.name, key: AuroraService.VERSION,value: "123456789").save(flush: true,failOnError: true)
    }

}
