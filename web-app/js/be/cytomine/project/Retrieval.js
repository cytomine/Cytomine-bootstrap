/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 19/01/11
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */
Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Retrieval');
/**
 * @class Cytomine.Retrieval
 * @singleton
 */

Cytomine.Retrieval.tabPrefix = "retrieval";

Cytomine.Retrieval = {
    /**
     * Retourne l'onglet correspondant au gestionnaire de projets
     * @return {Ext.Panel}
     */
    tab: function(idTab, idScan, tabTitle) {
        var view = new Ext.DataView({
            itemSelector: 'div.thumb-wrap',
            store: new Ext.data.JsonStore({
                url: '/cytomine-web/api/image/retrieval/'+idScan+'/100',
                autoLoad: true,
                title: tabTitle,
                closable:true,
                root: '',
                fields:[
                    'path','sim'
                ]
            }),
            tpl: new Ext.XTemplate(
                    '<tpl for=".">',
                    '<div class="thumb-wrap" id="{sim}">',
                    '<div class="thumb"><img src="{path}" title=""></div>',
                    '<span class="x-editable">{sim}</span></div>',
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
            id: idTab,
            bodyCssClass: 'overflow-auto',
            iconCls: 'envelope-label',
            title: 'Retrieval',
            items: [view]
        });
    }
};


// Update the contents of a tab if it exists, otherwise create a new one
Cytomine.Retrieval.showSimilarities = function(idTab, idScan, title) {
    var tabName = Cytomine.Retrieval.tabPrefix + idScan;
    var tab = Cytomine.tabs.getItem(tabName);
    if(tab){
        Cytomine.tabs.remove(tab);
    }
    tab = Cytomine.tabs.add(Cytomine.Retrieval.tab(tabName, idScan, title)).show();
    Cytomine.tabs.setActiveTab(tab);
}
