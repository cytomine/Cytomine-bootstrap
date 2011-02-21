Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Views');
Ext.namespace('Cytomine.Views.Browser');

/**
 * @class Cytomine.Views.Browser
 * @singleton
 */
Cytomine.Views.Browser = {
    /**
     * Retourne l'onglet correspondant au viewer de image
     * @return {Ext.Panel}
     */
    tab: function(idTab, idImage, url, tabTitle) {
        return new Ext.Panel({
            id: idTab,
            bodyCssClass: 'overflow-auto',
            iconCls: 'envelope-label',
            title: tabTitle,
            closable:true,
            autoLoad : {url:url,scripts:true},
            item : [
                /*{
                 title:'Project',
                 collapsible: true, //make this column collapsable

                 //contentEl: 'west', //Get our content from the "west" div
                 margins: '5 0 5 5',
                 cmargins: '5 5 5 5',
                 width: 175,
                 minSize: 100, //set the limits for resizing
                 maxSize: 250 //set the limits for resizing

                 }*/
            ],
            listeners: {
                show: function(p) {
                    console.log("SHOW");
                    console.log("LAYER NULL ? " +  Cytomine.annotationLayers[idImage] != undefined);
                    if (Cytomine.annotationLayers[idImage] != null) {
                        Cytomine.currentLayer = Cytomine.annotationLayers[idImage];
                        Cytomine.annotationLayers[idImage].loadToMap(Cytomine.scans[idImage]);
                    }

                }
            }
        });
    },
    openScan : function(idTab, idImage, url, title) {
        var tab = Cytomine.tabs.getItem(idTab);
        if(tab){
            Cytomine.tabs.remove(tab);
        }
        tab = Cytomine.tabs.add(this.tab(idTab, idImage, url, title)).show();
        Cytomine.tabs.setActiveTab(tab);
        if (Cytomine.toolbar != null) Cytomine.toolbar.show();
        if (Cytomine.overview != null) Cytomine.overview.show();
    }
};
