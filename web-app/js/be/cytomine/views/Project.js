Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Views');
Ext.namespace('Cytomine.Views.Project');

Cytomine.Views.Project.Grid = {
    userColumns : [
        /*{header: "ID", width: 40, sortable: true, dataIndex: 'id'},*/
        {header: "Name", width: 100, sortable: true, dataIndex: 'name', editor: new Ext.form.TextField({})},
        /*{header: "Image", width: 100, sortable: true, dataIndex: 'image', editor: new Ext.form.TextField({})}*/
    ],
    editor :  function () {
        return new Ext.ux.grid.RowEditor({
            saveText: 'Update'
        })},
    init : function() {
        var store = Cytomine.Models.Project.store();
        var editor = this.editor();
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
            columns : this.userColumns,
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
