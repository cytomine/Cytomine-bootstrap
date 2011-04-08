var BrowseImageView = Backbone.View.extend({
    tagName: "div",
    layers : {},
    initialize: function (options) {

    },
    render: function () {
        var tpl = ich.browseimagetpl(this.model.toJSON(), true);
        $(this.el).append(tpl);
        var tabs = $(this.el).children('.tabs');
        console.log(this.model.get('filename'));
        this.el.tabs("add", "#tabs-" + this.model.get('id'), this.model.get('filename'));
        this.el.css("display", "block");
        this.initMap();
        this.initVectorLayers();
        this.initOntology();
        this.initSideBar();
        return this;
    },
    getUserLayer: function () {
        return this.userLayer;
    },
    initMap: function () {
        this.layers.baseLayer = new OpenLayers.Layer.OpenURL(this.model.get('filename'), this.model.get('imageServerBaseURL'), {
            transitionEffect: 'resize',
            layername: 'basic',
            format: 'image/jpeg',
            rft_id: this.model.get('path'),
            metadataUrl: this.model.get('metadataUrl')
        });
        /* this.layers.secondLayer = new OpenLayers.Layer.OpenURL(this.model.get('filename'), this.model.get('imageServerBaseURL'), {
         transitionEffect: 'resize',
         layername: 'basic',
         format: 'image/jpeg',
         rft_id: this.model.get('path'),
         metadataUrl: this.model.get('metadataUrl'),
         rotate : 90
         });*/
        var self = this;
        var metadata = this.layers.baseLayer.getImageMetadata();
        var resolutions = this.layers.baseLayer.getResolutions();
        var maxExtent = new OpenLayers.Bounds(0, 0, metadata.width, metadata.height);
        var tileSize = this.layers.baseLayer.getTileSize();
        var lon = metadata.width / 2;
        var lat = metadata.height / 2;
        var mapOptions = {
            maxExtent: maxExtent,
            maximized: true
        };

        var layerSwitcher = new OpenLayers.Control.LayerSwitcher({
            roundedCorner: false,
            roundedCornerColor: false,
            'div': document.getElementById('layerSwitcher' + this.model.get('id')),
            mouseDown: function(evt) {//IF WE DON'T DO THAT, Mouse Up is not triggered if dragging or sliding
                this.isMouseDown = false;  //CONTINUE
                /*this.ignoreEvent(evt);*/
            }
        });
        var options = {
            resolutions: resolutions,
            maxExtent: maxExtent,
            tileSize: tileSize,
            controls: [
                //new OpenLayers.Control.Navigation({zoomWheelEnabled : true, mouseWheelOptions: {interval: 1}, cumulative: false}),
                new OpenLayers.Control.Navigation(), new OpenLayers.Control.PanZoomBar(),
                layerSwitcher,
                new OpenLayers.Control.MousePosition(),
                new OpenLayers.Control.OverviewMap({
                    div: document.getElementById('overviewMap' + this.model.get('id')),
                    //size: new OpenLayers.Size(metadata.width / Math.pow(2, openURLLayer.getViewerLevel()), metadata.height / Math.pow(2,(openURLLayer.getViewerLevel()))),
                    size: new OpenLayers.Size(metadata.width / Math.pow(2, this.layers.baseLayer.getViewerLevel()), metadata.height / Math.pow(2, (this.layers.baseLayer.getViewerLevel()))),
                    minRatio: 1,
                    maxRatio: 1024,
                    mapOptions: mapOptions
                }),
                new OpenLayers.Control.KeyboardDefaults()]
        };
        this.map = new OpenLayers.Map("map" + this.model.get('id'), options);
        this.map.addLayer(this.layers.baseLayer);
        //this.map.addLayer(this.layers.secondLayer);
        this.map.setCenter(new OpenLayers.LonLat(lon, lat), 2);


        $('#layerSwitcher' + this.model.get('id')).find('.slider').slider({
            value: 100,
            slide: function(e, ui) {
                self.layers.baseLayer.setOpacity(ui.value / 100);
            }
        });

        /*for (var i in layerSwitcher.baseLayers) {
         var layer = layerSwitcher.baseLayers[i];
         var switchName = layer['inputElem']['name'];
         var elemName = 'input[name="' + switchName + '"]';

         }*/

        var overviewWidth = $('#overviewMap' + this.model.get('id')).width();
        var overviewHeight = $('#overviewMap' + this.model.get('id')).height();
        $('#overviewMap' + this.model.get('id')).draggable({
            drag: function(event, ui) {
                $(this).css("width", overviewWidth);
                $(this).css("height", overviewHeight);
            }
        });
        var layerSwitecherWidth = $('#layerSwitcher' + this.model.get('id')).width();
        var layerSwitecherHeight = $('#layerSwitcher' + this.model.get('id')).height();
        $('#layerSwitcher' + this.model.get('id')).draggable({
            drag: function(event, ui) {
                $(this).css("width", layerSwitecherWidth);
                $(this).css("height", layerSwitecherHeight);
            }
        });
    },
    initSideBar: function () {
        var toolbar = $('#toolbar' + this.model.get('id'));
        var self = this;
        toolbar.find('input[name=select]').button({
            /*text : false,
             icons: {
             primary: "ui-icon-seek-start"
             }*/
        });
        toolbar.find('button[name=delete]').button({
            text: false,
            icons: {
                primary: "ui-icon-trash"

            }
        });
        toolbar.find('input[name=rotate]').button();
        toolbar.find('input[name=resize]').button();
        toolbar.find('input[name=drag]').button();
        toolbar.find('input[name=irregular]').button();
        toolbar.find('span[class=draw]').buttonset();
        toolbar.find('input[id=none' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleControl("none");
        });
        toolbar.find('input[id=select' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleControl("select");
        });
        toolbar.find('input[id=regular4' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().setSides(4);
            self.getUserLayer().toggleControl("regular");
        });
        toolbar.find('input[id=regular30' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().setSides(15);
            self.getUserLayer().toggleControl("regular");
        });
        toolbar.find('input[id=polygon' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleControl("polygon");
        });
        toolbar.find('input[id=modify' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleControl("modify");
        });
        toolbar.find('button[id=delete' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().removeSelection();
        });
        toolbar.find('input[id=rotate' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleRotate();
        });
        toolbar.find('input[id=resize' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleResize();
        });
        toolbar.find('input[id=drag' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleDrag();
        });
        toolbar.find('input[id=irregular' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleIrregular();

        });
        //image.getUserLayer().toggleControl(this);
    },
    initVectorLayers: function () {
        var self = this;
        var colors = ["#006b9a", "#a11323", "#b7913e"];
        var colorIndex = 0;
        window.models.users.fetch({
            success: function () {
                window.models.users.each(function (user) {
                    console.log(user.get('username'));
                    var layerAnnotation = new AnnotationLayer(user.get('firstname'), self.model.get('id'), user.get('id'), colors[colorIndex]);
                    layerAnnotation.loadAnnotations(self.map);
                    if (user.get('id') == window.app.user) {
                        self.userLayer = layerAnnotation;
                        layerAnnotation.initControls(self);
                        layerAnnotation.registerEvents();
                    }
                    colorIndex++;
                });
            }
        });
    },
    initOntology: function () {

        var self = this;


        //ontologyID

        console.log("initOntology.render");

        var self = this;
        var tpl = ich.imageontologyviewtpl({}, true);
        $("#ontology"+ this.model.get("id")).html(tpl);

        window.models.projects.fetch({
            success: function () {
                console.log("fetch project");
                var currentProject = models.projects.get(window.app.currentProject);
                console.log("currentProject:"+currentProject);
                var currentOntologyId = currentProject.get('ontology');


                window.models.ontologies.fetch({
                    success: function () {
                        console.log("fetch ontologies");
                        var currentOntology = models.ontologies.get(currentOntologyId);
                        console.log("currentOntology:"+currentOntology);
                        var json = currentOntology.toJSON();
                        console.log("json="+JSON.stringify(json));

                        $(function () {
                            $("#ontology"+ self.model.get("id")).find('.tree').jstree({
                                "json_data" : {
                                    "data" :json
                                },
                                "plugins" : ["json_data", "ui","themeroller"]

                            });
                        });
                    }
                });

            }
        });

        //$("#ontology"+ this.model.get("id")).find('.tree').html("coucou");

        var ontologyPanelWidth = $('#ontology' + this.model.get('id')).width();
        var ontologyPanelHeight = $('#ontology' + this.model.get('id')).height();
        $('#ontology' + this.model.get('id')).draggable({
            drag: function(event, ui) {
                $(this).css("width", ontologyPanelWidth);
                $(this).css("height", ontologyPanelHeight);
            }
        });


        //console.log("html");
        //console.log("$('#ontology'"+this.model.get('id')").exists()="+($('#ontology' + this.model.get('id')).length>0));
        //console.log("$('#ontology{{id}}').exists()="+($('#ontology' + this.model.get('id')).length>0));
        //console.log("$('#browseimagetpl').exists()="+($('#browseimagetpl').length>0));

        //$('#imageontologytree').append("HELLO"+ "<br>");


        //$("#ontologyid").append("ontology" + "<br>");
        //$("#ontologyid").append("ontology" + "<br>");
    },
    initTools: function (controls) {
        for (var key in controls) {
            this.map.addControl(controls[key]);
        }
    }
});




/* Annotation Layer */


var AnnotationLayer = function (name, imageID, userID, color) {

    var myStyles = new OpenLayers.StyleMap({
        "default": new OpenLayers.Style({
            pointRadius: "${type}",
            // sized according to type attribute
            fillColor: color,
            strokeColor: "#000",
            strokeWidth: 2,
            graphicZIndex: 1,
            fillOpacity: 0.5
        }),
        "select": new OpenLayers.Style({
            fillColor: "#EEE",
            strokeColor: "#000",
            graphicZIndex: 2
        }),
        "modify": new OpenLayers.Style({
            fillColor: "#EEE",
            strokeColor: "#000",
            graphicZIndex: 2
        })
    });

    this.name = name;
    this.imageID = imageID;
    this.userID = userID;
    this.vectorsLayer = new OpenLayers.Layer.Vector(this.name, {
        //styleMap: myStyles,
        rendererOptions: {
            zIndexing: true
        }
    });
    this.features = [];
    this.controls = null;
    this.dialog = null;
    this.rotate = false;
    this.resize = false;
    this.drag = false;
    this.irregular = false;
    this.aspectRatio = false;
}

AnnotationLayer.prototype = {
    registerEvents: function () {
        var alias = this;
        this.vectorsLayer.events.on({
            featureselected: function (evt) {
                console.log("featureselected start:" + evt.feature.attributes.idAnnotation + "|" + "/cytomine-web/api/annotation/" + evt.feature.attributes.idAnnotation + "/term.json" + "|");
                var req = new XMLHttpRequest();
                console.log("req 1");
                req.open("GET", "/cytomine-web/api/annotation/" + evt.feature.attributes.idAnnotation + "/term.json", true);
                console.log("req 2");
                req.onreadystatechange = alias.selectAnnotation; // the handler
                console.log("req 3");
                req.send(null);

            },
            'featureunselected': function () {
                if (alias.dialog != null) alias.dialog.destroy();
            },
            'featureadded': function (evt) {
                console.log("onFeatureAdded start:" + evt.feature.attributes.idAnnotation);
                /* Check if feature must throw a listener when it is added
                 * true: annotation already in database (no new insert!)
                 * false: new annotation that just have been draw (need insert)
                 * */
                if (evt.feature.attributes.listener != 'NO') {
                    console.log("add " + evt.feature);
                    alias.addAnnotation(evt.feature);
                }
            },
            'beforefeaturemodified': function (evt) {
                console.log("Selected " + evt.feature.id + " for modification");
            },
            'afterfeaturemodified': function (evt) {
                console.log("onFeatureUpdate start");
                alias.updateAnnotation(evt.feature);
            },
            'onDelete': function (feature) {
                console.log("delete " + feature.id);
            }
        });
    },
    initControls: function (image) {
        var alias = this;
        //vectorsLayer.events.register("featureselected", vectorsLayer, this.selectAnnotation);
        this.controls = {
            point: new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Point),
            line: new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Path),
            polygon: new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Polygon),
            regular: new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.RegularPolygon, {
                handlerOptions: {
                    sides: 5
                }
            }),
            modify: new OpenLayers.Control.ModifyFeature(this.vectorsLayer),
            select: new OpenLayers.Control.SelectFeature(this.vectorsLayer)

        }
        console.log("initTools on image : " + image.filename);
        image.initTools(this.controls);
        //image.map.addLayer(vectorsLayer);
        //var panel = new OpenLayers.Control.Panel();
        //var nav = new OpenLayers.Control.NavigationHistory();
        //image.map.addControl(nav);
        //panel.addControls([nav.next, nav.previous]);
        //image.map.addControl(panel);
    },


    /*Load annotation from database on layer */
    loadAnnotations: function (map) {
        var req = new XMLHttpRequest();
        var url = "/cytomine-web/api/user/" + this.userID + "/image/" + this.imageID + "/annotation.json";
        console.log("Annotations URL : " + url);
        req.open("GET", url, true);
        //req.open("GET", "/cytomine-web/api/annotation/image/"+this.imageID+".json", true);
        var alias = this;
        req.onreadystatechange = function () {
            var format = new OpenLayers.Format.WKT();
            //vectorsAnnotations = new OpenLayers.Layer.Vector("Overlay");
            var points = [];
            //console.log("response for " + url + " : " + req.responseText);
            if (req.readyState == 4) {
                var JSONannotations = eval('(' + req.responseText + ')');
                //console.log(JSONannotations.annotation);

                for (i = 0; i < JSONannotations.annotation.length; i++) {
                    console.log("JSONannotations ID: " + JSONannotations.annotation[i].id);
                    //read from wkt to geometry
                    var point = (format.read(JSONannotations.annotation[i].location));
                    var geom = point.geometry;
                    var feature = new OpenLayers.Feature.Vector(geom);
                    feature.attributes = {
                        idAnnotation: JSONannotations.annotation[i].id,
                        listener: 'NO',
                        importance: 10
                    };
                    alias.addFeature(feature);
                }

            }
        };
        req.send(null);
        map.addLayer(this.vectorsLayer);
    },
    addFeature: function (feature) {
        this.features[feature.attributes.idAnnotation] = feature;
        this.vectorsLayer.addFeatures(feature);
    },
    removeFeature: function (idAnnotation) {
        var feature = this.features[idAnnotation];
        this.vectorsLayer.removeFeatures(feature);
        this.features[idAnnotation] = null;

    },
    removeSelection: function () {
        for (var i in this.vectorsLayer.selectedFeatures) {
            var feature = this.vectorsLayer.selectedFeatures[i];
            console.log(feature);
            this.removeAnnotation(feature);
        }
    },
    /*Remove annotation from database*/
    removeAnnotation: function (feature) {

        console.log("deleteAnnotation start");
        //console.log("delete " + "" + feature.attributes.idAnnotation);
        var req = new XMLHttpRequest();
        req.open("DELETE", "/cytomine-web/api/annotation/" + feature.attributes.idAnnotation, true);
        req.send(null);
        this.vectorsLayer.removeFeatures(feature);
        console.log("deleteAnnotation end");
    },
    /*Add annotation in database*/
    addAnnotation: function (feature) {
        console.log("addAnnotation start");
        var format = new OpenLayers.Format.WKT();
        var geomwkt = format.write(feature);
        var alias = this;
        console.log("add geomwkt=" + geomwkt);
        for (var i = 0; i < 1; i++) {
            var req = new XMLHttpRequest();
            //console.log("/cytomine-web/api/annotation/image/"+this.scanID+"/"+geomwkt);
            //req.open("POST", "/cytomine-web/api/annotation/image/"+this.scanID+"/"+geomwkt+".json", true);
            req.open("POST", "/cytomine-web/api/annotation.json", true);
            if (i == 0) req.onreadystatechange = function () {
                var format = new OpenLayers.Format.WKT();
                if (req.readyState == 4) {
                    console.log("response:" + req.responseText);
                    var JSONannotations = eval('(' + req.responseText + ')');
                    console.log(JSONannotations.annotation.id);
                    var point = (format.read(JSONannotations.annotation.location));
                    var geom = point.geometry;
                    var feature = new OpenLayers.Feature.Vector(geom);
                    feature.attributes = {
                        idAnnotation: JSONannotations.annotation.id,
                        listener: 'NO',
                        importance: 10
                    };
                    alias.addFeature(feature);

                }

            };

            var json = {
                annotation: {
                    "class": "be.cytomine.project.Annotation",
                    name: "test",
                    location: geomwkt,
                    image: this.imageID
                }
            }; //class is a reserved word in JS !
            req.send(JSON.stringify(json));
        }
        //Annotation hasn't any id => -1
        //feature.attributes = {idAnnotation: "-1"};
        this.hideAnnotation(feature);
        //feature.remove();
        console.log("onFeatureAdded end");
    },
    hideAnnotation: function (feature) {
        this.vectorsLayer.removeFeatures([feature]);
    },

    /*Remove annotation from database*/
    removeAnnotation: function (feature) {

        console.log("deleteAnnotation start");
        console.log("delete " + "" + feature.attributes.idAnnotation);

        var req = new XMLHttpRequest();
        req.open("DELETE", "/cytomine-web/api/annotation/" + feature.attributes.idAnnotation, true);
        req.send(null);
        this.hideAnnotation(feature);
        console.log("deleteAnnotation end");
    },

    /*Modifiy annotation on database*/
    updateAnnotation: function (feature) {

        var format = new OpenLayers.Format.WKT();
        var geomwkt = format.write(feature);
        console.log("update geomwkt=" + geomwkt + " " + feature.attributes.idAnnotation);

        var req = new XMLHttpRequest();
        req.open("PUT", "/cytomine-web/api/annotation/" + feature.attributes.idAnnotation + ".json", true);

        var json = {
            annotation: {
                "id": feature.attributes.idAnnotation,
                "class": "be.cytomine.project.Annotation",
                name: "test",
                location: geomwkt,
                image: this.imageID
            }
        }; //class is a reserved word in JS !
        req.send(JSON.stringify(json));

        console.log("onFeatureUpdate end");

    },

    /*Read a list of annotations from server response and add to the layer*/
    decodeAnnotations: function (alias) {
        var format = new OpenLayers.Format.WKT();
        //vectorsAnnotations = new OpenLayers.Layer.Vector("Overlay");
        var points = [];
        console.log("decodeAnnotations:" + req.readyState);

        if (req.readyState == 4) {
            //eval json
            var JSONannotations = eval('(' + req.responseText + ')');
            console.log(JSONannotations.annotation);

            for (i = 0; i < JSONannotations.annotation.length; i++) {
                console.log(JSONannotations.annotation[i].id);
                //read from wkt to geometry
                var point = (format.read(JSONannotations.annotation[i].location));
                var geom = point.geometry;

                var feature = new OpenLayers.Feature.Vector(
                        geom, {
                    some: 'data'
                }, {
                    pointRadius: 10,
                    fillColor: "green",
                    fillOpacity: 0.5,
                    strokeColor: "black"
                });

                feature.attributes = {
                    idAnnotation: JSONannotations.annotation[i].id,
                    listener: 'NO',
                    importance: 10
                };

                alias.vectorsLayer.addFeatures(feature);
            }

        }

    },

    /*Decode a single annotation from server response and add to the layer*/
    decodeNewAnnotation: function () {
        var format = new OpenLayers.Format.WKT();
        var points = [];
        if (req.readyState == 4) {
            //eval json
            console.log("response:" + req.responseText);
            var JSONannotations = eval('(' + req.responseText + ')');
            console.log(JSONannotations.annotation);

            console.log(JSONannotations.annotation.id);
            //read from wkt to geometry
            var point = (format.read(JSONannotations.annotation.location));
            var geom = point.geometry;

            var feature = new OpenLayers.Feature.Vector(
                    geom, {
                some: 'data'
            }, {
                pointRadius: 10,
                fillColor: "green",
                fillOpacity: 0.5,
                strokeColor: "black"
            });

            feature.attributes = {
                idAnnotation: JSONannotations.annotation.id,
                listener: 'NO',
                importance: 10
            };

            console.log("add to " + vectorsLayer.name);
            console.log("add to " + this.vectorsLayer.name);
            vectorsLayer.addFeatures(feature);

        }

    },
    /* Launch a dialog with annotation info */
    selectAnnotation: function () {
        /*
         console.log("selectAnnotation:"+req.readyState);
         if (req.readyState == 4)
         {
         //eval json
         console.log("response:"+req.responseText);
         var JSONannotations = eval('(' + req.responseText + ')');

         var terms =""+ "<BR>";
         for (i=0;i<JSONannotations.term.length;i++)
         {
         terms = terms +"*" +JSONannotations.term[i].name + "<BR>"
         }
         this.dialog = new Ext.Window({
         title: "Feature Info",
         layout: "fit",
         height: 130, width: 200,
         plain: true,
         items: [{
         border: false,
         bodyStyle: {
         padding: 5, fontSize: 13
         },
         html: "Term: "+terms
         }]
         });
         this.dialog.show();
         } */

    },
    /** Triggered when add new feature **/
    /*onFeatureAdded : function (evt) {
     console.log("onFeatureAdded start:"+evt.feature.attributes.idAnnotation);
     // Check if feature must throw a listener when it is added
     // true: annotation already in database (no new insert!)
     // false: new annotation that just have been draw (need insert)
     //
     if(evt.feature.attributes.listener!='NO')
     {
     console.log("add " + evt.feature);
     alias.addAnnotation(evt.feature);
     }
     },*/

    /** Triggered when update feature **/
    /* onFeatureUpdate : function (evt) {
     console.log("onFeatureUpdate start");

     this.updateAnnotation(evt.feature);
     },*/
    toggleRotate: function () {
        this.rotate = !this.rotate;
        this.updateControls();
    },
    toggleResize: function () {
        this.resize = !this.resize;
        this.updateControls();
    },
    toggleDrag: function () {
        this.drag = !this.drag;
        this.updateControls();

    },
    toggleIrregular: function () {
        this.irregular = !this.irregular;
        this.updateControls();
    },
    toggleAspectRatio: function () {
        this.aspectRatio = !this.aspectRatio;
        this.updateControls();
    },
    setSides: function (sides) {
        this.sides = sides;
        this.updateControls();
    },
    updateControls: function () {

        this.controls.modify.mode = OpenLayers.Control.ModifyFeature.RESHAPE;
        if (this.rotate) {
            this.controls.modify.mode |= OpenLayers.Control.ModifyFeature.ROTATE;
        }

        if (this.resize) {
            this.controls.modify.mode |= OpenLayers.Control.ModifyFeature.RESIZE;
            if (this.aspectRatio) {
                this.controls.modify.mode &= ~OpenLayers.Control.ModifyFeature.RESHAPE;
            }
        }
        if (this.drag) {
            this.controls.modify.mode |= OpenLayers.Control.ModifyFeature.DRAG;
        }
        if (this.rotate || this.drag) {
            this.controls.modify.mode &= ~OpenLayers.Control.ModifyFeature.RESHAPE;
        }
        this.controls.regular.handler.sides = this.sides;
        this.controls.regular.handler.irregular = this.irregular;
    },
    toggleControl: function (name) {
        for (key in this.controls) {
            var control = this.controls[key];
            if (name == key) {
                console.log("activate control : " + key + " in " + this.imageID);
                control.activate();
            } else {
                console.log("deactivate control : " + key + " in " + this.imageID);
                control.deactivate();
            }
        }
    }
}