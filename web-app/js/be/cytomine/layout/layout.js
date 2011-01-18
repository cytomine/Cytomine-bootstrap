Ext.namespace('Cytomine');

Cytomine.tabs = null;

Ext.onReady(function() {
    setTimeout(function(){
        Ext.get('loading').remove();
        Ext.get('loading-mask').fadeOut({remove:true});
    }, 250);

    //Create our centre panel with tabs
    Cytomine.tabs = new Ext.TabPanel({
        region:'center',
        activeTab:0,
        //autoScroll:true,
        margins: '5 5 5 0',
        resizeTabs:true, // turn on tab resizing
        minTabWidth: 115,
        items:[
            Cytomine.Project.tab()
            ]
    });

    //Create our layout
    var viewport = new Ext.Viewport({
        layout:'border', //set the layout style. Check the Ext JS API for more styles
        defaults: {autoScroll: true},
        defaults: {
            collapsible: false,
            split: true
        },
        items: [
            Cytomine.tabs,
            {
                cls: 'docs-header',
                height: 30,
                region:'north',
                xtype:'box',
                el:'header',
                border:false,
                margins: '0 0 5 0'
            }
        ]
    });
});

