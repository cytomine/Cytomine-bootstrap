Ext.BLANK_IMAGE_URL = 'images/default/s.gif';

Ext.onReady(function(){   
    var viewport = new Ext.Viewport(
    {
        layout: 'fit',
        items: [ new Ext.TabPanel({
            activeTab: 0,
            items: [
                ULg.Projects.tab()
                /*,
                ULg.Viewer.tab(),
                ULg.ontologies.Manager.tab()*/
            ]
            })
        ]
    });
    Ext.QuickTips.init();
});
