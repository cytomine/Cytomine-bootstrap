Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Views');
Ext.namespace('Cytomine.Views.Project');


Cytomine.gridSelectedProject = null;
Cytomine.currentProject = null;

Cytomine.Views.Project = {
    userColumns : [
        /*{header: "ID", width: 40, sortable: true, dataIndex: 'id'},*/
        {header: "Name", width: 100, sortable: true, dataIndex: 'name', editor: new Ext.form.TextField({})}
        /*{header: "Image", width: 100, sortable: true, dataIndex: 'image', editor: new Ext.form.TextField({})}*/
    ],
    editor :  function () {
        return new Ext.ux.grid.RowEditor({
            saveText: 'Update'
        })},
    grid : function() {
        var alias = this;
        var store = Cytomine.Models.Project.store();
        var editor = this.editor();

        var grid =  new Ext.grid.GridPanel({
            iconCls: 'icon-grid',
            //title: 'Projects',
            store: store,
            plugins: [editor],
            columns : this.userColumns,
            autoWidth : true,
            autoHeight : true,
            viewConfig: {
                forceFit: true
            },
            sm: new Ext.grid.RowSelectionModel({
                singleSelect: true,
                listeners: {
                    rowselect: function(smObj, rowIndex, record) {
                        var project = store.getById(record.id);
                        Cytomine.gridSelectedProject = project.get("id");
                        Cytomine.Views.Project.detailPanel.removeAll();
                        Cytomine.Views.Project.detailPanel.add(alias.getView(project.get("imageURL")));
                        Cytomine.Views.Project.detailPanel.doLayout();
                        Cytomine.Views.Project.detailPanel.show();
                    }
                }
            }),
            split : true,
            autoSizeColumns: true,
            trackMouseOver: true
        });
        //select first row
        store.on('load', function(){
            grid.getSelectionModel().selectRow(0);
            grid.fireEvent('rowclick', grid, 0)
        }, this, {
            single: true
        });
        var tbar = new Ext.Toolbar({
            items: [{
                text: 'Add',
                iconCls: 'add',
                handler: this.onAdd.createDelegate(this, [grid, editor])

            }, '-', {
                text: 'Delete',
                iconCls: 'delete',
                handler: this.onDelete.createDelegate(this, [grid, editor])

            }]
        });
        grid.elements += ',tbar';
        grid.add(tbar);
        grid.doLayout();

        return grid;
    },
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
    },
    getView : function(url) {
        console.log("Getview " + url);
        return new Ext.DataView({
            itemSelector: 'div.thumb-wrap',
            store: new Ext.data.JsonStore({
                url : url,
                autoLoad: true,
                root: 'image',
                fields:[
                    'id','filename', 'thumb'
                ]
            }),
            tpl: new Ext.XTemplate(
                    '<tpl for=".">',
                    '<div class="thumb-wrap" id="{id}">',
                    '<div class="thumb"><img src="{thumb}" title="{filename}"></div>',
                    '<span class="x-editable">{filename} {id}</span></div>',
                    '</tpl>'
                    ),
            listeners: {
                click: function(dataview, index, node, e) {
                    var data = dataview.getStore().getAt(index);
                    var closeBrowser = function () {
                        for (imageID in Cytomine.images) {
                            var tab = Cytomine.tabs.getItem(imageID);
                            if(tab){
                                Cytomine.tabs.remove(tab);
                            }
                        }
                        openImage();
                    }
                    var openImage = function() {
                        Cytomine.currentProject = Cytomine.gridSelectedProject;
                        Cytomine.Views.Browser.create(data.get('id')); //multiple tabs

                    }
                    if (Cytomine.currentProject != null && Cytomine.gridSelectedProject != Cytomine.currentProject) {
                        Cytomine.Notifications.confirm("Do you want to close the active project ?", "OpenProject", closeBrowser,"question", openImage);
                    } else {
                        openImage();
                    }

                    //Cytomine.Retrieval.showSimilarities(data.get('id'),data.get('id'), data.get('filename')); //multiple tabs
                }
            }
        });
    }
}

Cytomine.Views.Project.detailPanel = new Ext.Panel({
    id: 'detailsPanel',
    region: 'center',
    title: 'Details',
    html: ''
}).hide();