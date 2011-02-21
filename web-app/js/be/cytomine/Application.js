
Ext.namespace('Cytomine');



Cytomine.overview = null;//created dynamically by browse.gsp
Cytomine.scans = [];
Cytomine.annotationLayers = [];
Cytomine.currentLayer = null;

Cytomine.Application = function() {
    return {
        init : function() {

            console.log("Init Application");

            Ext.BLANK_IMAGE_URL = 'http://extjs.cachefly.net/ext-3.1.0/resources/images/default/s.gif';

            //Stores



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

            // This is the app UI layout code.  All of the calendar views are subcomponents of
            // CalendarPanel, but the app title bar and sidebar/navigation calendar are separate
            // pieces that are composed in app-specific layout code since they could be ommitted
            // or placed elsewhere within the application.
            new Ext.Viewport({
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
        },

        // The edit popup window is not part of the CalendarPanel itself -- it is a separate component.
        // This makes it very easy to swap it out with a different type of window or custom view, or omit
        // it altogether. Because of this, it's up to the application code to tie the pieces together.
        // Note that this function is called from various event handlers in the CalendarPanel above.
        showEditWindow : function(rec, animateTarget){
        },

        // The CalendarPanel itself supports the standard Panel title config, but that title
        // only spans the calendar views.  For a title that spans the entire width of the app
        // we added a title to the layout's outer center region that is app-specific. This code
        // updates that outer title based on the currently-selected view range anytime the view changes.
        updateTitle: function(startDt, endDt){

        },

        // This is an application-specific way to communicate CalendarPanel event messages back to the user.
        // This could be replaced with a function to do "toast" style messages, growl messages, etc. This will
        // vary based on application requirements, which is why it's not baked into the CalendarPanel.
        showMsg: function(msg){
            Ext.fly('app-msg').update(msg).removeClass('x-hidden');
        },

        clearMsg: function(){
            Ext.fly('app-msg').update('').addClass('x-hidden');
        }
    }
}();

setTimeout(function(){
    Ext.get('loading').remove();
    Ext.get('loading-mask').fadeOut({remove:true});
}, 250);


Ext.onReady(Cytomine.Application.init, Cytomine.Application);

var App = new Ext.App({});

Ext.data.DataProxy.addListener('beforewrite', function(proxy, action) {
    //App.setAlert(App.STATUS_NOTICE, "Before " + action);
});

////
// all write events
//
Ext.data.DataProxy.addListener('write', function(proxy, action, result, res, rs) {
    App.setAlert(true, res.message);
});

////
// all exception events
//
Ext.data.DataProxy.addListener('exception', function(proxy, type, action, options, res) {
    var jsonData = Ext.util.JSON.decode(res.responseText);
    App.setAlert(false, ""+ jsonData.errors);
});