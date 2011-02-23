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
            iconCls: 'magnifier-medium',
            title: tabTitle,
            closable:true,
            tbar : {
                items: [
                    {name : 'none',tooltip: ULg.lang.Viewer.annotations.toolMove, iconCls:'hand', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', pressed:true, handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);Cytomine.currentLayer.toggleControl(this);}},
                    {name : 'select',tooltip: ULg.lang.Viewer.annotations.toolSelect, iconCls:'cursor', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);Cytomine.currentLayer.toggleControl(this);}},
                    {name : 'regular',tooltip: ULg.lang.Viewer.annotations.toolSelect, iconCls:'layer-shape', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);Cytomine.currentLayer.setSides(4);Cytomine.currentLayer.toggleControl(this);}},
                    {name : 'regular',tooltip: ULg.lang.Viewer.annotations.toolSelect, iconCls:'layer-ellipse', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);Cytomine.currentLayer.setSides(30);Cytomine.currentLayer.toggleControl(this);}},
                    {name : 'polygon',tooltip: ULg.lang.Viewer.annotations.toolPolygon, iconCls:'layer-polygon', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);Cytomine.currentLayer.toggleControl(this);}},
                    {name : 'modify',tooltip: ULg.lang.Viewer.annotations.toolPolygon, iconCls:'ruler-crop', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);Cytomine.currentLayer.toggleControl(this);}},
                    {
                        xtype: 'tbsplit',
                        text: 'Options',
                        menu: [{
                            text: 'Allow Rotate',
                            checked: false,
                            id : 'rotate',
                            name : 'rotate',
                            handler: function() {console.log("Toolbar toggle : " + this.id);Cytomine.currentLayer.toggleRotate();}
                            //group: 'quality'
                        }, {
                            text: 'Allow Resize',
                            checked: false,
                            id : 'resize',
                            name : 'resize',
                            handler: function() {console.log("Toolbar toggle : " + this.id);Cytomine.currentLayer.toggleResize();}
                            //group: 'quality'
                        }, {
                            text: 'Allow Drag',
                            checked: false,
                            id : 'drag',
                            name : 'drag',
                            handler: function() {console.log("Toolbar toggle : " + this.id);Cytomine.currentLayer.toggleDrag();}
                            //group: 'quality'
                        },{
                            text: 'Allow irregular',
                            checked: false,
                            id : 'irregular',
                            name : 'irregular',
                            handler: function() {console.log("Toolbar toggle : " + this.id);Cytomine.currentLayer.toggleIrregular();}
                            //group: 'quality'
                        }]
                    },
                    '-',
                    '->',
                    '-'
                ]
            },
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
        if (Cytomine.toolbar != null) Cytomine.toolbar.show().syncSize();
        if (Cytomine.overview != null) Cytomine.overview.show().syncSize();
    }
};
