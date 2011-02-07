Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Project');


Cytomine.scans = [];
Cytomine.annotationLayers = [];
Cytomine.currentLayer = null;


/**
 * @class Cytomine.Project
 * @singleton
 */
Cytomine.Project = {
    /**
     * Retourne l'onglet correspondant au gestionnaire de projets
     * @return {Ext.Panel}
     */
    tab: function(id) {
        var view = this.getView(id);

        return new Ext.Panel({
            id: 'Project',
            bodyCssClass: 'overflow-auto',
            iconCls: 'envelope-label',
            title: 'Project',
            items: [view],
            listeners: {
                show: function(p) {
                    if (Cytomine.toolbar != null) Cytomine.toolbar.hide();
                    if (Cytomine.overview != null) Cytomine.overview.hide();
                }
            }
        });
    },
    getView : function(id) {
        return new Ext.DataView({
            itemSelector: 'div.thumb-wrap',
            store: new Ext.data.JsonStore({
                //url: '/cytomine-web/api/scan.json',
                url : '/cytomine-web/api/project/scan/'+id+'.json',
                autoLoad: true,
                root: 'scan',
                fields:[
                    'id','filename'
                ]
            }),
            tpl: new Ext.XTemplate(
                    '<tpl for=".">',
                    '<div class="thumb-wrap" id="{id}">',
                    '<div class="thumb"><img src="/cytomine-web/api/image/thumb/{id}" title="{filename}"></div>',
                    '<span class="x-editable">{filename} {id}</span></div>',
                    '</tpl>'
                    ),
            listeners: {
                click: function(dataview, index, node, e) {
                    var data = dataview.getStore().getAt(index);

                    Cytomine.Browser.openScan(data.get('id'),data.get('id'), data.get('filename')); //multiple tabs

                    //Cytomine.Retrieval.showSimilarities(data.get('id'),data.get('id'), data.get('filename')); //multiple tabs
                    //Cytomine.Browser.openScan('browser', data.get('id'), data.get('filename')); //unique tabs
                }
            }
        });
    }
};