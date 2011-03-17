
Ext.namespace('Cytomine');
Ext.namespace('Cytomine.Application');

Cytomine.images = [];
Cytomine.currentProject = null;

Cytomine.Application = function() {


    var models = [];

    return {
        dispatch : function(callback) {
            //basic dispatcher

            //Annotations
            if (callback.method == "be.cytomine.AddAnnotationCommand") {
                var tab = Cytomine.tabs.getItem(callback.imageID);
                if (tab) Cytomine.tabs.setActiveTab(tab);
                Cytomine.Views.Browser.addAnnotation(callback.annotationID, callback.imageID);
            } else if (callback.method == "be.cytomine.EditAnnotationCommand") {
                var tab = Cytomine.tabs.getItem(callback.imageID);
                if (tab) Cytomine.tabs.setActiveTab(tab);
                Cytomine.Views.Browser.updateAnnotation(callback.annotationID, callback.imageID);
            } else if (callback.method == "be.cytomine.DeleteAnnotationCommand") {
                var tab = Cytomine.tabs.getItem(callback.imageID);
                if (tab) Cytomine.tabs.setActiveTab(tab);
                Cytomine.Views.Browser.removeAnnotation(callback.annotationID, callback.imageID);
            }
            //Users
            else if (callback.method == "be.cytomine.AddUserCommand"){
                var usersTab = Cytomine.tabs.getItem("Users");
                if (usersTab) Cytomine.tabs.setActiveTab(usersTab);
                Cytomine.Views.User.reload();
            }
            else if (callback.method == "be.cytomine.EditUserCommand"){
                var usersTab = Cytomine.tabs.getItem("Users");
                if (usersTab) Cytomine.tabs.setActiveTab(usersTab);
                Cytomine.Views.User.reload();
            }
            else if (callback.method == "be.cytomine.DeleteUserCommand"){
                var usersTab = Cytomine.tabs.getItem("Users");
                if (usersTab) Cytomine.tabs.setActiveTab(usersTab);
                Cytomine.Views.User.reload();
            }

        },
        storeImage : function(imageID, image) {
            Cytomine.images[imageID] = image;
        },
        addTab : function(idTab, tab) {
            var existingTab = Cytomine.tabs.getItem(idTab);
            if(existingTab){
                Cytomine.tabs.remove(tab);
            }
            tab = Cytomine.tabs.add(tab);
            tab.show();
            Cytomine.tabs.setActiveTab(tab);
        },
        received : function(response) {
            console.log("undo/redo resp : " + response.responseText);
            var jsonData = Ext.util.JSON.decode(response.responseText);
            if (jsonData.callback != undefined) {
                Cytomine.Application.dispatch(jsonData.callback);
                //eval(jsonData.callback);
            }

            if (jsonData.message != undefined) App.setAlert(true, jsonData.message);

        },
        undo : function () {
            Ext.Ajax.request({
                url: '/cytomine-web/command/undo',
                success: this.received,
                failure: function () { console.log('failure');}
            });
        },
        redo  : function () {
            Ext.Ajax.request({
                url: '/cytomine-web/command/redo',
                success: this.received,
                failure: function () { console.log('failure');}
            });
        },
        init : function() {
            this.regModels();
            var alias = this;
            var session = Cytomine.Application.getModel('session');
            console.log("-------");
            session.on('load', function(){
                session.each(function(user) {
                    console.log("logged user is : " + user.data.id);
                    Cytomine.Application.userID = user.data.id;
                    alias.initComponents();
                });

            });
        },
        regModels : function() {
            models['image'] = Cytomine.Models.Image.store();
            models['project'] = Cytomine.Models.Project.store();
            models['session'] = Cytomine.Models.Session.store();
            models['user'] = Cytomine.Models.User.store();
            models['term'] = Cytomine.Models.Term.store();
        },
        getModel : function (name) {
            return models[name]
        },
        initComponents : function() {

            Ext.QuickTips.init();

            console.log("Init Application");

            var welcomePanel = new Ext.Panel({
                title : "Welcome",
                html : "welcome"
            });

            var faqPanel = new Ext.Panel({
                title : "FAQ",
                html : "faq"
            });

            var screencastsPanel = new Ext.Panel({
                title : "Screencasts",
                html : "content"
            });

            Cytomine.Application.HelpWindow = new Ext.Window({
                title:'Help',
                width:500,
                height:400,
                plain:true,
                closable:true,
                closeAction:'hide',
                layoutConfig:{animate:true},
                border:false,
                items:[{
                    defaults:{border:false, activeTab:0}
                    ,items:[{
                        defaults:{layout:'fit'}
                        ,xtype:'tabpanel'
                        ,items:[welcomePanel, screencastsPanel, faqPanel]
                    }]
                }]
            });

            //Cytomine.Application.HelpWindow.show();

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

            /*Cytomine.toolbar = new Ext.Window({
             id : 'toolbarPanel',
             title  : 'Controls',
             layout :'fit',
             el : 'toolbar',
             tbar : {
             items: [
             {id: 'noneToggle', name : 'none',tooltip: ULg.lang.Viewer.annotations.toolMove, iconCls:'hand', enableToggle: true, toggleGroup:'controlToggle', pressed:true, handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);Cytomine.currentLayer.toggleControl(this);}},
             {id: 'selectToggle', name : 'select',tooltip: ULg.lang.Viewer.annotations.toolSelect, iconCls:'cursor', enableToggle: true, toggleGroup:'controlToggle', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);Cytomine.currentLayer.toggleControl(this);}},
             //{id:'tool-rect', tooltip: ULg.lang.Viewer.annotations.toolRectangle, iconCls:'layer-shape', enableToggle: true, toggleGroup:'tool', handler: function() {Cytomine.currentLayer.toggleControl(this);}},
             //{id:'tool-circle', tooltip: ULg.lang.Viewer.annotations.toolCircle, iconCls:'layer-ellipse', enableToggle: true, toggleGroup:'tool', handler: function() {Cytomine.currentLayer.toggleControl(this);}},
             {id:'polygonToggle', name : 'polygon',tooltip: ULg.lang.Viewer.annotations.toolPolygon, iconCls:'layer-polygon', enableToggle: true, toggleGroup:'controlToggle', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);Cytomine.currentLayer.toggleControl(this);}},
             '-',
             '->',
             '-'
             ]
             },
             x : 400,
             y : 65,
             autoHeight: true,
             autoWidth: true
             });*/

            // This is the app UI layout code.  All of the calendar views are subcomponents of
            // CalendarPanel, but the app title bar and sidebar/navigation calendar are separate
            // pieces that are composed in app-specific layout code since they could be ommitted
            // or placed elsewhere within the application.
            new Ext.Viewport({
                layout:'border', //set the layout style. Check the Ext JS API for more styles
                title : 'Cytomine',
                items: [
                    {
                        region: 'north',
                        border: false,

                        tbar: [{
                            xtype:'tbtext',
                            text: '<h1>Cytomine</h1>'
                        },'-',

                            {
                                text: 'Undo',
                                iconCls : 'control_rewind',
                                handler : function () {
                                    Cytomine.Application.undo();
                                }
                            }, {
                                text: 'Redo',
                                iconCls : 'control_forward',
                                handler : function () {
                                    Cytomine.Application.redo();
                                }
                            }, '->',
                            {
                                text: 'Administration',
                                iconCls : 'folder_wrench',
                                menu: [{
                                    text: 'Users',
                                    iconCls: 'user_gray',
                                    handler : function () {
                                        Cytomine.Application.addTab("users",Cytomine.Admin.tab());
                                    }
                                }/*, {
                                 text: 'Slide'
                                 }*/
                                ]
                            } , '-',{
                                text: 'Options',
                                iconCls: 'cog-edit',
                                menu: [{
                                    text: 'User Info',
                                    handler : function () {
                                        App.setAlert(true,"Work in progress");
                                    }
                                }, {
                                    text: 'Settings',
                                    handler : function () {
                                        App.setAlert(true,"Work in progress");
                                    }
                                }]
                            }, {
                                text: 'Help',
                                iconCls : 'question',
                                handler : function () {
                                    Cytomine.Application.HelpWindow.show();
                                }
                            }, '-', {
                                text: 'Logout',
                                handler : function () {
                                    var logoutFunction = function() {
                                        window.location = "logout";
                                    };
                                    Cytomine.Notifications.confirm("Are you sure ?", "Logout", logoutFunction,"question", function(){});

                                }
                            }]
                    },
                    {
                        xtype: 'box',
                        region: 'center',
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
    console.log("ERROR : " + res.responseText);
    App.setAlert(false, ""+ jsonData.errors);
});


//Prevent closing the app accidentaly
//window.onbeforeunload = function(){ return ''; }

