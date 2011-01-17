Ext.namespace('Cytomine');

/**
 * @class Cytomine.Project
 * @singleton
 */
Cytomine.Project = {
    /**
     * Retourne l'onglet correspondant au gestionnaire de projets
     * @return {Ext.Panel}
     */
    tab: function() {
        var view = new Ext.DataView({
            itemSelector: 'div.thumb-wrap',
            store: new Ext.data.JsonStore({
                url: '/cytomine-web/api/scan.json',
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
                    '<span class="x-editable">{filename}</span></div>',
                    '</tpl>'
                    ),
            listeners: {
                click: function(dataview, index, node, e) {
                    var data = dataview.getStore().getAt(index);
                    //Cytomine.Browser.openScan(data.get('id'),data.get('id'), data.get('filename')); //multiple tabs
                    Cytomine.Browser.openScan('browser', data.get('id'), data.get('filename')); //unique tabs
                }
            }
        });

        return new Ext.Panel({
            id: 'Project',
            bodyCssClass: 'overflow-auto',
            iconCls: 'envelope-label',
            title: 'Project',
            items: [view]
        });
    }
};