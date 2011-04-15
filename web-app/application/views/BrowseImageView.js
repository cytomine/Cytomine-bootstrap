var BrowseImageView = Backbone.View.extend({
    tagName: "div",
    layers: {},
    initialize: function (options) {

    },
    render: function () {
        var tpl = ich.browseimagetpl(this.model.toJSON(), true);
        $(this.el).append(tpl);
        var tabs = $(this.el).children('.tabs');
        this.el.tabs("add", "#tabs-" + this.model.get('id'), this.model.get('filename'));
        this.el.css("display", "block");
        this.initToolbar();
        this.initMap();
        this.initOntology();
        this.initVectorLayers();


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
            mouseDown: function (evt) { //IF WE DON'T DO THAT, Mouse Up is not triggered if dragging or sliding
                this.isMouseDown = false; //CONTINUE
                /*this.ignoreEvent(evt);*/
            }
        });
        var options = {
            resolutions: resolutions,
            maxExtent: maxExtent,
            tileSize: tileSize,
            controls: [
                //new OpenLayers.Control.Navigation({zoomWheelEnabled : true, mouseWheelOptions: {interval: 1}, cumulative: false}),
                new OpenLayers.Control.Navigation(), new OpenLayers.Control.PanZoomBar(), layerSwitcher, new OpenLayers.Control.MousePosition(), new OpenLayers.Control.OverviewMap({
                    div: document.getElementById('overviewMap' + this.model.get('id')),
                    //size: new OpenLayers.Size(metadata.width / Math.pow(2, openURLLayer.getViewerLevel()), metadata.height / Math.pow(2,(openURLLayer.getViewerLevel()))),
                    size: new OpenLayers.Size(metadata.width / Math.pow(2, this.layers.baseLayer.getViewerLevel()), metadata.height / Math.pow(2, (this.layers.baseLayer.getViewerLevel()))),
                    minRatio: 1,
                    maxRatio: 1024,
                    mapOptions: mapOptions
                }), new OpenLayers.Control.KeyboardDefaults()]
        };
        this.map = new OpenLayers.Map("map" + this.model.get('id'), options);
        this.map.addLayer(this.layers.baseLayer);
        //this.map.addLayer(this.layers.secondLayer);
        this.map.setCenter(new OpenLayers.LonLat(lon, lat), 2);


        $('#layerSwitcher' + this.model.get('id')).find('.slider').slider({
            value: 100,
            slide: function (e, ui) {
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
            drag: function (event, ui) {
                $(this).css("width", overviewWidth);
                $(this).css("height", overviewHeight);
            }
        });
        var layerSwitecherWidth = $('#layerSwitcher' + this.model.get('id')).width();
        var layerSwitecherHeight = $('#layerSwitcher' + this.model.get('id')).height();
        $('#layerSwitcher' + this.model.get('id')).draggable({
            drag: function (event, ui) {
                $(this).css("width", layerSwitecherWidth);
                $(this).css("height", layerSwitecherHeight);
            }
        });
    },
    initToolbar: function () {
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
    },
    initVectorLayers: function () {
        var self = this;
        var colors = ["#006b9a", "#a11323", "#b7913e", "#CCCCCC"];
        var colorIndex = 0;
        window.app.models.users.fetch({
            success: function () {
                window.app.models.users.each(function (user) {
                    var layerAnnotation = new AnnotationLayer(user.get('firstname'), self.model.get('id'), user.get('id'), colors[colorIndex], self.ontologyTreeView );
                    layerAnnotation.loadAnnotations(self.map);
                    if (user.get('id') == window.app.status.user.id) {
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
        var idOntology = window.app.models.projects.get(window.app.status.currentProject).get('ontology');
        this.ontologyTreeView = new OntologyTreeView({
            el: $("#ontology" + this.model.get("id")),
            idImage: this.model.get("id"),
            model: window.app.models.ontologies.get(idOntology)
        }).render();

    },
    initTools: function (controls) {
        for (var key in controls) {
            this.map.addControl(controls[key]);
        }
    }
});




/* Annotation Layer */


var AnnotationLayer = function (name, imageID, userID, color, ontologyTreeView) {

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
    this.ontologyTreeView = ontologyTreeView;
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
                alias.ontologyTreeView.refresh(evt.feature.attributes.idAnnotation);
            },
            'featureunselected': function () {
                if (alias.dialog != null) alias.dialog.destroy();
                alias.ontologyTreeView.clear();
            },
            'featureadded': function (evt) {
                console.log("onFeatureAdded start:" + evt.feature.attributes.idAnnotation);
                /* Check if feature must throw a listener when it is added
                 * true: annotation already in database (no new insert!)
                 * false: new annotation that just have been draw (need insert)
                 * */
                if (evt.feature.attributes.listener != 'NO') {
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
    },


    /*Load annotation from database on layer */
    loadAnnotations: function (map) {
        var alias = this;
        new AnnotationCollection({user : this.userID, image : this.imageID}).fetch({
            success : function (collection, response) {
                collection.each(function(annotation) {
                    var format = new OpenLayers.Format.WKT();
                    var point = (format.read(annotation.get("location")));
                    var feature = new OpenLayers.Feature.Vector(point.geometry);
                    feature.attributes = {
                        idAnnotation: annotation.get("id"),
                        listener: 'NO',
                        importance: 10
                    };
                    alias.addFeature(feature);
                });
            }
        });
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
    /*Add annotation in database*/
    addAnnotation: function (feature) {
        var newFeature = null;
        var format = new OpenLayers.Format.WKT();
        var geomwkt = format.write(feature);
        var alias = this;
        var annotation = new AnnotationModel({
            //"class": "be.cytomine.project.Annotation",
            name: "we don't know yet",
            location: geomwkt,
            image: this.imageID,
            parse: function(response) {
                console.log("response : " + response);
                window.app.view.message("Annotation", response.message, "");
                return response.annotation;
            }});
        annotation.save(annotation.toJSON(), {
            success: function (model, response) {
                console.log(response.message);
                // window.app.view.message(response.message);

                model.set({id : response.annotation.id});
                console.log("new annotation id" + response.annotation.id);

                var point = (format.read(response.annotation.location));
                var geom = point.geometry;
                newFeature = new OpenLayers.Feature.Vector(geom);
                newFeature.attributes = {
                    idAnnotation: response.annotation.id,
                    listener: 'NO',
                    importance: 10
                };

                var terms = alias.ontologyTreeView.getTermsChecked();
                var counter = 0;
                if (terms.length == 0) {
                    alias.addTermCallback(0,0,feature, newFeature);
                }

                _.each(terms, function (id) {
                    new AnnotationTermModel({
                        term: id,
                        annotation: response.annotation.id
                    }).save(null, {success : function (model, response) {
                        alias.addTermCallback(terms.length, ++counter, feature, newFeature);
                    }});
                });
            },
            error: function (model, response) {
                console.log("error new annotation id");
            }
        });
    },
    addTermCallback : function(total, counter, oldFeature, newFeature) {
        if (counter < total) return;
        this.addFeature(newFeature);
        this.controls.select.unselectAll();
        this.controls.select.select(newFeature);
        this.vectorsLayer.removeFeatures([oldFeature]);
    },
    /*Remove annotation from database*/
    removeAnnotation: function (feature) {
        new AnnotationModel({id:feature.attributes.idAnnotation}).destroy(); //TODO : callback success-error
        this.vectorsLayer.removeFeatures([feature]);
    },

    /*Modifiy annotation on database*/
    updateAnnotation: function (feature) {
        var format = new OpenLayers.Format.WKT();
        var geomwkt = format.write(feature);
        new AnnotationModel({id:feature.attributes.idAnnotation}).fetch({
            success : function(model, response) {
                model.set({location : geomwkt});
                model.save();  //TODO : callback success-error
            }
        });
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
    },
    /* Callbacks undo/redo */
    annotationAdded: function (idAnnotation) {
        var self = this;
        var annotation = new AnnotationModel({
            id: idAnnotation
        }).fetch({
                     success: function (model) {
                         var format = new OpenLayers.Format.WKT();
                         var location = format.read(model.get('location'));
                         var feature = new OpenLayers.Feature.Vector(location.geometry);
                         feature.attributes = {
                             idAnnotation: model.get('id'),
                             listener: 'NO',
                             importance: 10
                         };
                         self.addFeature(feature);
                     }
                 });
    },
    annotationRemoved: function (idAnnotation) {
        this.removeFeature(idAnnotation);
    },
    annotationUpdated: function (idAnnotation, idImage) {
        this.annotationRemoved(idAnnotation);
        this.annotationAdded(idAnnotation);
    }
}