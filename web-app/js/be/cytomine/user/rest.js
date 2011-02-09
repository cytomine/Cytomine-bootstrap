Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Rest');
Ext.namespace('Cytomine.Security');
Ext.namespace('Cytomine.Security.User');

Cytomine.Rest.Project = {
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
            {name: 'name', allowBlank: false},
            {name: 'image', allowBlank: false}
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
            proxy: Cytomine.Rest.Project.proxy(),
            reader: Cytomine.Rest.Project.reader(),
            writer: Cytomine.Rest.Project.writer()   // <-- plug a DataWriter into the store just as you would a Reader
        })},
    userColumns : [
        /*{header: "ID", width: 40, sortable: true, dataIndex: 'id'},*/
        {header: "Name", width: 100, sortable: true, dataIndex: 'name', editor: new Ext.form.TextField({})}
    ],
    editor :  function () {
        return new Ext.ux.grid.RowEditor({
            saveText: 'Update'
        })},
    grid : function() {
        var store = Cytomine.Rest.Project.store();
        var editor = Cytomine.Rest.Project.editor();
        var detailsPanel = new Ext.Panel({
            id: 'detailsPanel',
            region: 'center',
            title: 'Details',
            bodyStyle: {
                background: '#ffffff',
                padding: '7px'
            },
            html: ''
        });
        var grid =  new Ext.grid.GridPanel({
            iconCls: 'icon-grid',
            title: 'Projects',
            store: store,
            plugins: [editor],
            columns : Cytomine.Rest.Project.userColumns,
            viewConfig: {
                forceFit: true
            },
            sm: new Ext.grid.RowSelectionModel({
                singleSelect: true,
                listeners: {
                    rowselect: function(smObj, rowIndex, record) {
                        var project = store.getById(record.id);
                        detailsPanel.removeAll();
                        detailsPanel.add(Cytomine.Project.getView(project.get("image")));
                        detailsPanel.doLayout();
                    }
                }
            }),
            split : true,
            autoSizeColumns: true,
            trackMouseOver: true
        });
        var tbar = new Ext.Toolbar({
            items: [{
                text: 'Add',
                iconCls: 'silk-add',
                handler: Cytomine.Rest.Project.onAdd.createDelegate(this, [grid, editor])

            }, '-', {
                text: 'Delete',
                iconCls: 'silk-delete',
                handler: Cytomine.Rest.Project.onDelete.createDelegate(this, [grid, editor])

            }]
        });
        grid.elements += ',tbar';
        grid.add(tbar);
        grid.doLayout();
        var ct = new Ext.Panel({
            layout:'fit',
            items: [
                grid,
                detailsPanel
            ]
        })
        return ct;
    },
    beforewrite : function () {
        return new Ext.data.DataProxy.addListener('beforewrite', function(proxy, action) {
            //App.setAlert(App.STATUS_NOTICE, "Before " + action);
        })
    },
    write : function () {
        return new Ext.data.DataProxy.addListener('write', function(proxy, action, result, res, rs) {
            //App.setAlert(true, action + ':' + res.message);
        })
    },
    exception : function () {
        return new Ext.data.DataProxy.addListener('exception', function(proxy, type, action, options, res) {
            //App.setAlert(false, "Something bad happend while executing " + action);
        })},
    onAdd : function (grid, editor) {
        var u = new grid.store.recordType({
            name : ''
        });

        //var index = grid.store.data.length;
        index = 0;
        editor.stopEditing();
        grid.store.insert(index, u);
        editor.startEditing(index);
    },
    onDelete : function (grid, editor) {
        var rec = grid.getSelectionModel().getSelected();
        if (!rec) {
            return false;
        }
        grid.store.remove(rec);
    }
}

Cytomine.Security.User = {

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
            proxy: Cytomine.Security.User.proxy(),
            reader: Cytomine.Security.User.reader(),
            writer: Cytomine.Security.User.writer()   // <-- plug a DataWriter into the store just as you would a Reader
        })},
    userColumns : [
        /*{header: "ID", width: 40, sortable: true, dataIndex: 'id'},*/
        {header: "Username", width: 100, sortable: true, dataIndex: 'username', editor: new Ext.form.TextField({})},
        {header: "First", width: 50, sortable: true, dataIndex: 'firstname', editor: new Ext.form.TextField({})},
        {header: "Last", width: 50, sortable: true, dataIndex: 'lastname', editor: new Ext.form.TextField({})},
        {header: "Email", width: 50, sortable: true, dataIndex: 'email', editor: new Ext.form.TextField({})},
        {header: "Password", width: 50, sortable: true, dataIndex: 'password', editor: new Ext.form.TextField({})}
    ],
    editor :  function () {
        return new Ext.ux.grid.RowEditor({
            saveText: 'Update'
        })},
    grid : function() {
        var store = Cytomine.Security.User.store();
        var editor = Cytomine.Security.User.editor();

        var grid =  new Ext.grid.GridPanel({
            iconCls: 'icon-grid',
            frame: true,
            title: 'Users',
            height: 300,
            store: store,
            plugins: [editor],
            columns : Cytomine.Security.User.userColumns,
            viewConfig: {
                forceFit: true
            }
        });
        var tbar = new Ext.Toolbar({
            items: [{
                text: 'Add',
                iconCls: 'silk-add',
                handler: Cytomine.Security.User.onAdd.createDelegate(this, [grid, editor])

            }, '-', {
                text: 'Delete',
                iconCls: 'silk-delete',
                handler: Cytomine.Security.User.onDelete.createDelegate(this, [grid, editor])

            }]
        });
        grid.elements += ',tbar';
        grid.add(tbar);
        grid.doLayout();
        return grid;
    },
    beforewrite : function () {
        return new Ext.data.DataProxy.addListener('beforewrite', function(proxy, action) {
            //App.setAlert(App.STATUS_NOTICE, "Before " + action);
        })
    },
    write : function () {
        return new Ext.data.DataProxy.addListener('write', function(proxy, action, result, res, rs) {
            //App.setAlert(true, action + ':' + res.message);
        })
    },
    exception : function () {
        return new Ext.data.DataProxy.addListener('exception', function(proxy, type, action, options, res) {
            //App.setAlert(false, "Something bad happend while executing " + action);
        })},
    onAdd : function (grid, editor) {
        var u = new grid.store.recordType({
            username : '',
            firstname: '',
            lastname : '',
            email    : '',
            password : ''
        });

        //var index = grid.store.data.length;
        index = 0;
        editor.stopEditing();
        grid.store.insert(index, u);
        editor.startEditing(index);
    },
    onDelete : function (grid, editor) {
        var rec = grid.getSelectionModel().getSelected();
        if (!rec) {
            return false;
        }
        grid.store.remove(rec);
    }
}


Ext.onReady(function() {
    //Cytomine.Security.User.grid = Cytomine.Security.User.userGrid();
});



