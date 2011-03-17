Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Views');
Ext.namespace('Cytomine.Views.Term');

Cytomine.Views.Term = (function() {
    var userColumns = [
        /*{header: "ID", width: 40, sortable: true, dataIndex: 'id'},*/
        {header: "Name", width: 100, sortable: true, dataIndex: 'name', editor: new Ext.form.TextField({})}  //name is a bad index???
    ];
    var editor = new Ext.ux.grid.RowEditor({
        saveText: 'Update'
    });
    var store;
    var grid = null;
    var onAdd = function (grid, editor) {
        var u = new grid.store.recordType({    ////??????????
            name : ''
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
        store = Cytomine.Models.Term.store();
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