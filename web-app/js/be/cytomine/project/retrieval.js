/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 19/01/11
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
Ext.namespace('Cytomine');

/**
 * @class Cytomine.Retrieval
 * @singleton
 */
Cytomine.Retrieval = {
    /**
     * Retourne l'onglet correspondant au gestionnaire de projets
     * @return {Ext.Panel}
     */
    tab: function() {
        var view = new Ext.DataView({
            itemSelector: 'div.thumb-wrap',
            store: new Ext.data.JsonStore({
                url: '/cytomine-web/api/image/retrieval/2/25',
                autoLoad: true,
                root: '',
                fields:[
                    'path','sim'
                ]
            }),
            tpl: new Ext.XTemplate(
                    '<tpl for=".">',
                    '<div class="thumb-wrap" id="{sim}">',
                    '<div class="thumb"><img src="{path}" title=""></div>',
                    '<span class="x-editable">{path}</span></div>',
                    '</tpl>'
                    ),
            listeners: {
                click: function(dataview, index, node, e) {
                    var data = dataview.getStore().getAt(index);
                    //Cytomine.Browser.openScan(data.get('id'),data.get('id'), data.get('filename')); //multiple tabs
                    //Cytomine.Browser.openScan('browser', data.get('id'), data.get('filename')); //unique tabs
                }
            }
        });

        return new Ext.Panel({
            id: 'Retrieval',
            bodyCssClass: 'overflow-auto',
            iconCls: 'envelope-label',
            title: 'Retrieval',
            items: [view]
        });
    }
};