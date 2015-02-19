package be.cytomine.security

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.MessageBrokerServerAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON


/**
 * Created by julien 
 * Date : 10/02/15
 * Time : 13:37
 */
class MessageBrokerServerSecurityTests extends SecurityTestsAbstract{


    void testSecurityForCytomineAdmin() {

        def messageBrokerServer = BasicInstanceBuilder.getMessageBrokerServerNotExist()
        def result = MessageBrokerServerAPI.create(messageBrokerServer.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        Long id = result.data.id

        result = MessageBrokerServerAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = MessageBrokerServerAPI.containsInJSONList(id, JSON.parse(MessageBrokerServerAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD).data))
        assert result

        def data = UpdateData.createUpdateSet(messageBrokerServer, [name: ["OLDNAMEADMIN","NEWNAMEADMIN"], user: [BasicInstanceBuilder.user1, BasicInstanceBuilder.user2]])
        result = MessageBrokerServerAPI.update(messageBrokerServer.id, data.postData, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //MessageBrokerServerAPI.undo()

        result = MessageBrokerServerAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testSecurityForSimpleUser() {

        getUser1()
        // A simple user can't create a new message broker server. Only a cytomine admin can.
        def messageBrokerServer = BasicInstanceBuilder.getMessageBrokerServerNotExist()
        def result = MessageBrokerServerAPI.create(messageBrokerServer.encodeAsJSON(), USERNAME1, PASSWORD1)
        assert 403 == result.code

        // A message broker server is created to test all the simple user's rights
        messageBrokerServer = BasicInstanceBuilder.getMessageBrokerServerNotExist()
        result = MessageBrokerServerAPI.create(messageBrokerServer.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        Long id = result.data.id

        result = MessageBrokerServerAPI.show(id, USERNAME1, PASSWORD1)
        assert 200 == result.code

        result = MessageBrokerServerAPI.containsInJSONList(id, JSON.parse(MessageBrokerServerAPI.list(USERNAME1, PASSWORD1).data))
        assert result

        def data = UpdateData.createUpdateSet(messageBrokerServer, [name: ["OLDNAMESIMPLEUSER","NEWNAMESIMPLEUSER"], user: [BasicInstanceBuilder.user1, BasicInstanceBuilder.user2]])
        result = MessageBrokerServerAPI.update(messageBrokerServer.id, data.postData, USERNAME1, PASSWORD1)
        assert 403 == result.code

        result = MessageBrokerServerAPI.delete(id, USERNAME1, PASSWORD1)
        assert 403 == result.code
    }

    void testSecurityForAnonymous() {

        def messageBrokerServer = BasicInstanceBuilder.getMessageBrokerServerNotExist()
        def result = MessageBrokerServerAPI.create(messageBrokerServer.encodeAsJSON(), Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code

        messageBrokerServer = BasicInstanceBuilder.getMessageBrokerServerNotExist()
        result = MessageBrokerServerAPI.create(messageBrokerServer.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        Long id = result.data.id

        result = MessageBrokerServerAPI.show(id, Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code

        result = MessageBrokerServerAPI.containsInJSONList(id, JSON.parse(MessageBrokerServerAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD).data))
        assert !result

        def data = UpdateData.createUpdateSet(messageBrokerServer, [name: ["OLDNAMEANON","NEWNAMEANON"], user: [BasicInstanceBuilder.user1, BasicInstanceBuilder.user2]])
        result = MessageBrokerServerAPI.update(messageBrokerServer.id, data.postData, Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code

        result = MessageBrokerServerAPI.delete(id, Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code
    }
}
