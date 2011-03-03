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
    tab: function(idTab, idImage, tabTitle, image) {
        var overview = new Ext.Panel({
            flex:1,
            cls : 'overviewMapPanel',
            title  : 'Overview',
            layout : 'fit',
            iconCls :'image-min',
            collapsible: true,
            autoScroll: false,
            html : '<div id="overviewMap'+idImage+'"></div>',
            /*autoWidth : true,*/
            width : 256,
            autoHeight : true,
            listeners: {
                beforeexpand: function(){
                    //Ext.getCmp("sidebar"+idTab).doLayout();
                },
                collapse: function(){
                    //Ext.getCmp("sidebar"+idTab).doLayout();
                }
            }

        });
        var layerSwitcher = new Ext.Panel({
            flex:1,
            cls : 'layerSwitcherPanel',
            title  : 'Layers',
            layout : 'fit',
            iconCls :'magnifier-medium',
            collapsible: true,
            autoScroll: false,
            html : '<div id="layerSwitcher'+idImage+'"></div>',
            /*autoWidth : true,*/
            autoHeight : true,
            width : 256

        });
        var ontologyPanel = new Ext.Panel({
            id:'tp-ontologies-filter',
            flex:1,
            width : 256,
            xtype: 'treepanel',
            collapsible: true,
            border: false,
            title: 'Filtrer par ontologies',
            iconCls: 'tag-label-funnel',
            cls: 'ontologies-tree',
            /*tbar: {
             items: [
             //{ xtype: 'tbtext', iconCls: 'magnifier-medium', text: 'Filtre:' },
             { id:'tf-ontologies-filter', xtype: 'textfield', name: 'filter', tooltip: ULg.lang.Viewer.filter.filter },
             {
             iconCls: 'magnifier-medium',
             handler: function() {
             var tree = Ext.getCmp('tp-ontologies-filter');
             var loader = tree.getLoader();
             loader.baseParams.filter = Ext.getCmp('tf-ontologies-filter').getValue();
             tree.root.reload();
             }
             },
             '->',
             '-',
             {
             iconCls: 'node',
             tooltip: ULg.lang.Viewer.filter.showAll,
             enableToggle: true,
             pressed: true,
             handler: function() {
             var tree = Ext.getCmp('tp-ontologies-filter');
             var loader = tree.getLoader();
             loader.baseParams.filter = Ext.getCmp('tf-ontologies-filter').getValue();
             tree.root.reload();
             }
             }
             ]
             },*/
            useArrows: true,
            autoScroll: true,
            animate: true,
            containerScroll: true,
            line: true,
            // auto create TreeLoader
            loader: {
                dataUrl: 'http://localhost:8080/cytomine-web/api/ontology/28/tree.json',
                baseParams: {
                    filter:''
                }
            },
            root: {
                nodeType: 'async',
                text: 'cell',
                expanded: true,
                id: 'CL:0000000'
            },
            listeners: {
                beforeexpand: function(){
                    Ext.getCmp("sidebar"+idTab).doLayout();
                },
                collapse: function(){
                    Ext.getCmp("sidebar"+idTab).doLayout();
                }
            }
        });
        return new Ext.Panel({
            id: idTab,
            layout   : 'border',
            bodyCssClass: 'overflow-auto',
            iconCls: 'image',
            title: tabTitle,
            closable:true,
            tbar : {
                items: [
                    {name : 'none',tooltip: ULg.lang.Viewer.annotations.toolMove, iconCls:'hand', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', pressed:true, handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);image.getUserLayer().toggleControl(this);}},
                    {name : 'select',tooltip: ULg.lang.Viewer.annotations.toolSelect, iconCls:'cursor', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);image.getUserLayer().toggleControl(this);}},
                    {name : 'regular',tooltip: ULg.lang.Viewer.annotations.toolSelect, iconCls:'layer-shape', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);image.getUserLayer().setSides(4);image.getUserLayer().toggleControl(this);}},
                    {name : 'regular',tooltip: ULg.lang.Viewer.annotations.toolSelect, iconCls:'layer-ellipse', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);image.getUserLayer().setSides(30);image.getUserLayer().toggleControl(this);}},
                    {name : 'polygon',tooltip: ULg.lang.Viewer.annotations.toolPolygon, iconCls:'layer-polygon', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);image.getUserLayer().toggleControl(this);}},
                    {name : 'modify',tooltip: ULg.lang.Viewer.annotations.toolPolygon, iconCls:'ruler-crop', enableToggle: true, toggleGroup:'controlToggle'+idTab+'', handler: function() {console.log("Toolbar toggle : " + this.id);this.toggle(true);image.getUserLayer().toggleControl(this);}},
                    {
                        xtype: 'tbsplit',
                        text: 'Options',
                        menu: [{
                            text: 'Allow Rotate',
                            checked: false,
                            id : 'rotate',
                            name : 'rotate',
                            handler: function() {console.log("Toolbar toggle : " + this.id);image.getUserLayer().toggleRotate();}
                            //group: 'quality'
                        }, {
                            text: 'Allow Resize',
                            checked: false,
                            id : 'resize',
                            name : 'resize',
                            handler: function() {console.log("Toolbar toggle : " + this.id);image.getUserLayer().toggleResize();}
                            //group: 'quality'
                        }, {
                            text: 'Allow Drag',
                            checked: false,
                            id : 'drag',
                            name : 'drag',
                            handler: function() {console.log("Toolbar toggle : " + this.id);image.getUserLayer().toggleDrag();}
                            //group: 'quality'
                        },{
                            text: 'Allow irregular',
                            checked: false,
                            id : 'irregular',
                            name : 'irregular',
                            handler: function() {console.log("Toolbar toggle : " + this.id);image.getUserLayer().toggleIrregular();}
                            //group: 'quality'
                        }]
                    },
                    '-',
                    '->',
                    '-'
                ]
            },
            items : [
                {
                    id : "sidebar"+idTab,
                    region: 'east',
                    animCollapse: false,
                    collapsible: false,
                    split: true,
                    autoWidth : true,
                    minSize: 256,
                    maxSize: 256,
                    collapseMode:'mini',
                    title : tabTitle,
                    listeners: {
                        collapse: function(p) {
                            console.log("collapse");
                        },
                        expand: function(p) {
                            console.log("expand");
                        }
                    },
                    containerScroll: true,
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    items : [
                        overview,
                        layerSwitcher
                        //ontologyPanel
                    ]
                },
                {
                    region: 'center',
                    html : '<div id="map'+idImage+'"></div>'
                    //autoLoad : {url:url,scripts:true}
                }
            ],
            listeners: {
                show: function(p) {

                }
            }
        });
    },
    initSideBar : function(imageID, image) {
        var alias = this;
        console.log("currentProject is: " + Cytomine.currentProject );
        var projectStore = Cytomine.Models.Project.store();

        projectStore.on('load', function(){
            var project = projectStore.getById(Cytomine.currentProject);
            var termURL = project.get("termURL");
            var userURL = project.get("userURL");

            /*console.log("Opening termStore : " + termURL);
             var termStore =  new Ext.data.JsonStore({
             url : termURL,
             autoLoad: true,
             root: 'term',
             fields:[
             'id','name', 'comment', 'ontology', 'color'
             ],
             listeners : {
             load : function () {
             console.log("termStore loaded");
             }
             }
             });*/

            console.log("Opening userStore : " + userURL);
            var userStore =  new Ext.data.JsonStore({
                url : userURL,
                autoLoad: true,
                root: 'user',
                fields:[
                    'id','username', 'firstname', 'lastname', 'email'
                ],
                listeners : {
                    load : function () {
                        var colors = ["#006b9a", "#a11323", "#b7913e"];
                        var colorIndex = 0;
                        userStore.each(function(user) {

                            var userID = user.data.id;   //we should use accessors record instead of data directly
                            var userName = user.data.firstname + " " + user.data.lastname;
                            var layerAnnotation = new Cytomine.Project.AnnotationLayer( userName, imageID, userID, colors[colorIndex]);
                            layerAnnotation.loadAnnotations(image);
                            image.annotationsLayers[user.data.id] = layerAnnotation;
                            console.log("#### " + user.data.id + " vs " + Cytomine.Application.user);
                            if (user.data.id == Cytomine.Application.userID) {
                                image.userLayer = layerAnnotation;
                                layerAnnotation.initControls(image);
                                layerAnnotation.registerEvents();
                            }
                            colorIndex++;
                        });
                    }
                }
            });
        });
    },
    create : function (imageID) {
        var alias = this;
        var imageStore = Cytomine.Application.getModel('image');
        var record = imageStore.getById(imageID);
        var image = new Cytomine.Project.Image(record.get('imageServerBaseURL'), record.get('id'), record.get('filename'), record.get('path'), record.get('metadataUrl'));
        Cytomine.Application.addTab(imageID, alias.tab(record.get('id'), record.get('id'), record.get('filename'), image));
        image.initMap(); //render into created tab
        Cytomine.Application.storeImage(imageID, image);




        alias.initSideBar(imageID, image);
    },
    addAnnotation : function(idAnnotation, idImage) {
        if (Cytomine.images[idImage] == null) return; //image is not open
        var layer = Cytomine.images[idImage].userLayer;
        var annotationStore = Cytomine.Models.Annotation.store(idImage, Cytomine.Application.userID);
        annotationStore.on('load', function(){
            var record = annotationStore.getById(idAnnotation);
            var layer = Cytomine.images[idImage].userLayer;
            var format = new OpenLayers.Format.WKT();
            var location =  format.read(record.get('location'));
            var feature = new OpenLayers.Feature.Vector( location.geometry);
            feature.attributes = {idAnnotation: record.get('id'), listener:'NO',importance: 10 };

            layer.addFeature(feature);
        });
    },
    removeAnnotation : function(idAnnotation, idImage) {
        if (Cytomine.images[idImage] == null) return; //image is not open
        var layer = Cytomine.images[idImage].userLayer;
        layer.removeFeature(idAnnotation);
    },
    updateAnnotation : function(idAnnotation, idImage) {
        this.removeAnnotation(idAnnotation, idImage);
        this.addAnnotation(idAnnotation, idImage); //add also update
    }

};
