Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Models');


Cytomine.Models.Project = {
    // Create a standard HttpProxy instance.
    proxy : function () {
        return new Ext.data.HttpProxy({
            url: '/cytomine-web/api/project.json'
        })},
    // Typical JsonReader.  Notice additional meta-data params for defining the core attributes of your json-response
    reader :  function () {
        return new Ext.data.JsonReader({
            totalProperty: 'total',
            successProperty: 'success',
            idProperty: 'id',
            root: "project",
            messageProperty: 'message'  // <-- New "messageProperty" meta-data
        }, [
            {name: 'id'},
            {name: 'name', allowBlank: true},
            {name: 'image'}
        ])},
    writer : function () { return new Ext.data.JsonWriter({
        encode: false,   // <-- don't return encoded JSON -- causes Ext.Ajax#request to send data using jsonData config rather than HTTP params
        writeAllFields: true
    })},
    store : function () {
        return new Ext.data.Store({
            id: 'project',
            autoLoad : true,
            restful: true,     // <-- This Store is RESTful
            proxy: this.proxy(),
            reader: this.reader(),
            writer: this.writer()   // <-- plug a DataWriter into the store just as you would a Reader
        })}
}