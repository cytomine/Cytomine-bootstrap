Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Models');

Cytomine.Models.User = {
    // Create a standard HttpProxy instance.
    proxy : function () {
        return new Ext.data.HttpProxy({
            url: '/cytomine-web/api/user.json'
        })},
    // Typical JsonReader.  Notice additional meta-data params for defining the core attributes of your json-response
    reader :  function () {
        return new Ext.data.JsonReader({
            totalProperty: 'total',
            successProperty: 'success',
            idProperty: 'id',
            root: "user",
            messageProperty: 'message'  // <-- New "messageProperty" meta-data
        }, [
            {name: 'id'},
            {name: 'username', allowBlank: false},
            {name: 'firstname', allowBlank: false},
            {name: 'lastname', allowBlank: false},
            {name: 'email', allowBlank: false},
            {name: 'password', allowBlank: false}
        ])},
    writer : function () { return new Ext.data.JsonWriter({
        encode: false,   // <-- don't return encoded JSON -- causes Ext.Ajax#request to send data using jsonData config rather than HTTP params
        writeAllFields: true
    })},
    store : function () {
        return new Ext.data.Store({
            id: 'user',
            autoLoad : true,
            restful: true,     // <-- This Store is RESTful
            proxy: Cytomine.Models.User.proxy(),
            reader: Cytomine.Models.User.reader(),
            writer: Cytomine.Models.User.writer()   // <-- plug a DataWriter into the store just as you would a Reader
        })}
}