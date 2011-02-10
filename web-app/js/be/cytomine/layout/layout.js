Ext.namespace('Cytomine');

Cytomine.tabs = null;

var brol = '<p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Sed metus nibh, sodales a, porta at, vulputate eget, dui. Pellentesque ut nisl. Maecenas tortor turpis, interdum non, sodales non, iaculis ac, lacus. Vestibulum auctor, tortor quis iaculis malesuada, libero lectus bibendum purus, sit amet tincidunt quam turpis vel lacus. In pellentesque nisl non sem. Suspendisse nunc sem, pretium eget, cursus a, fringilla vel, urna.';


Ext.onReady(function() {

    setTimeout(function(){
        Ext.get('loading').remove();
        Ext.get('loading-mask').fadeOut({remove:true});
    }, 250);

    //Create our centre panel with tabs
    Cytomine.tabs = new Ext.TabPanel({
        region:'center',
        activeTab:0,
        autoScroll:true,
        enableTabScroll:true,
        margins: '0 0 0 0',
        resizeTabs:true, // turn on tab resizing
        minTabWidth: 115,
        items:[
            Cytomine.Dashboard.tab()
        ],
        defaults: {
            autoScroll:true
        },
        plugins: new Ext.ux.TabCloseMenu(),
        listeners : {
            tabchange: function(p) {
                console.log("EVENT");
                //Cytomine.currentLayer = Cytomine.annotationLayers[idTab];
            }
        }
    });

    Cytomine.toolbar = new Ext.Window({
        id : 'toolbarPanel',
        layout : 'fit',
        title  : 'Annotations controls',
        el : 'controls',
        x : 400,
        y : 65
    });


    Cytomine.overview = null;//created dynamically by browse.gsp

    //Create our layout
    var viewport = new Ext.Viewport({
        layout:'border', //set the layout style. Check the Ext JS API for more styles
        title : 'Cytomine',
        items: [
            {
                xtype: 'box',
                region: 'north',
                applyTo: 'header',
                height: 30
            },
            Cytomine.tabs
        ],
        renderTo: Ext.getBody()
    });
});

