Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Views');
Ext.namespace('Cytomine.Views.User');

Cytomine.Views.User = (function() {
    var userColumns = [
        /*{header: "ID", width: 40, sortable: true, dataIndex: 'id'},*/
        {header: "Username", width: 100, sortable: true, dataIndex: 'username', editor: new Ext.form.TextField({})},
        {header: "First", width: 50, sortable: true, dataIndex: 'firstname', editor: new Ext.form.TextField({})},
        {header: "Last", width: 50, sortable: true, dataIndex: 'lastname', editor: new Ext.form.TextField({})},
        {header: "Email", width: 50, sortable: true, dataIndex: 'email', editor: new Ext.form.TextField({})},
        {header: "Password", width: 50, sortable: true, dataIndex: 'password', editor: new Ext.form.TextField({})}
    ];
    var editor = new Ext.ux.grid.RowEditor({
        saveText: 'Update'
    });
    var store;
    var grid = null;
    var onAdd = function (grid, editor) {
        var u = new grid.store.recordType({
            username : '',
            firstname: 'aze',
            lastname : 'aze',
            email    : 'aze@aze.aze',
            password : 'aze'
        });

        //var index = grid.store.data.length;
        index = 0;
        editor.stopEditing();
        grid.store.insert(index, u);
        editor.startEditing(index);
    }
    var onDelete = function (grid, editor) {
        var rec = grid.getSelectionModel().getSelected();
        if (!rec) {
            return false;
        }
        grid.store.remove(rec);
    }
    var init = function() {
        store = Cytomine.Models.User.store();
        //var store = Cytomine.Models.User.store();
        grid =  new Ext.grid.GridPanel({
            iconCls: 'icon-grid',
            frame: true,
            //title: 'Users',
            height: 300,
            store: store,
            plugins: [editor],
            columns : userColumns,
            viewConfig: {
                forceFit: true
            }
        });
        var tbar = new Ext.Toolbar({
            items: [{
                text: 'Add',
                iconCls: 'add',
                handler: onAdd.createDelegate(this, [grid, editor])

            }, '-', {
                text: 'Delete',
                iconCls: 'delete',
                handler: onDelete.createDelegate(this, [grid, editor])

            }]
        });
        grid.elements += ',tbar';
        grid.add(tbar);
        grid.doLayout();
    }

    return {
        grid : function () {
            init();
            return grid;
        },
        reload : function(url) {
            store.reload();
        }
    }
})();
