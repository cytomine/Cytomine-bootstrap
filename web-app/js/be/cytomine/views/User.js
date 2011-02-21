Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Views');
Ext.namespace('Cytomine.Views.User');

Cytomine.Views.User.Grid = {
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
    init : function() {
        var store = Cytomine.Models.User.store();
        var editor = this.editor();

        var grid =  new Ext.grid.GridPanel({
            iconCls: 'icon-grid',
            frame: true,
            title: 'Users',
            height: 300,
            store: store,
            plugins: [editor],
            columns : this.userColumns,
            viewConfig: {
                forceFit: true
            }
        });
        var tbar = new Ext.Toolbar({
            items: [{
                text: 'Add',
                iconCls: 'silk-add',
                handler: this.onAdd.createDelegate(this, [grid, editor])

            }, '-', {
                text: 'Delete',
                iconCls: 'silk-delete',
                handler: this.onDelete.createDelegate(this, [grid, editor])

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
