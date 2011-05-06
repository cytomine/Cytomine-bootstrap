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
        this.map.setCenter(new OpenLayers.LonLat(lon, lat), 2);


        $('#layerSwitchercontent' + this.model.get('id')).find('.slider').slider({
            value: 100,
            slide: function (e, ui) {
                self.layers.baseLayer.setOpacity(ui.value / 100);
            }
        });

        new DraggablePanelView({
            el : $('#layerSwitcher' + this.model.get('id')),
            template : ich.layerswitchercontenttpl({id : this.model.get('id')}, true)/*,
             dialogAttr : {
             dialogID : "#layerswitcherdialog" + this.model.get('id'),
             width : 200,
             height : 200,
             css : {left: 'auto', right : '30px', top: 'auto', bottom : '100px'}
             }*/
        }).render();

        new DraggablePanelView({
            el : $('#overviewMap' + this.model.get('id')),
            template : ich.overviewmapcontenttpl({id : this.model.get('id')}, true)/*,
             dialogAttr : {
             dialogID : "#overviewmapdialog" + this.model.get('id'),
             width : 200,
             height : 200,
             css : {left: 'auto', right : '30px', top: '100px', bottom : 'auto'}
             }*/
        }).render();
        /*for (var i in layerSwitcher.baseLayers) {
         var layer = layerSwitcher.baseLayers[i];
         var switchName = layer['inputElem']['name'];
         var elemName = 'input[name="' + switchName + '"]';

         }*/

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
        toolbar.find('span[class=nav-toolbar]').buttonset();
        toolbar.find('span[class=draw-toolbar]').buttonset();
        toolbar.find('span[class=edit-toolbar]').buttonset();
        toolbar.find('span[class=delete-toolbar]').buttonset();

        toolbar.find('input[id=none' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleControl("none");
            self.getUserLayer().enableHightlight();
        });
        toolbar.find('input[id=select' + this.model.get('id') + ']').click(function () {
         self.getUserLayer().toggleControl("select");
         self.getUserLayer().disableHightlight();
         });
        toolbar.find('input[id=regular4' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().setSides(4);
            self.getUserLayer().toggleControl("regular");
            self.getUserLayer().disableHightlight();
        });
        toolbar.find('input[id=regular30' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().setSides(15);
            self.getUserLayer().toggleControl("regular");
            self.getUserLayer().disableHightlight();
        });
        toolbar.find('input[id=polygon' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleControl("polygon");
            self.getUserLayer().disableHightlight();
        });
        toolbar.find('input[id=modify' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleEdit();
            self.getUserLayer().toggleControl("modify");
            self.getUserLayer().disableHightlight();
        });
        toolbar.find('input[id=delete' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleControl("select");
            self.getUserLayer().deleteOnSelect = true;
            self.getUserLayer().disableHightlight();
        });
        toolbar.find('input[id=rotate' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleRotate();
            self.getUserLayer().disableHightlight();
        });
        toolbar.find('input[id=resize' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleResize();
            self.getUserLayer().disableHightlight();

        });
        toolbar.find('input[id=drag' + this.model.get('id') + ']').click(function () {
            self.getUserLayer().toggleDrag();
            self.getUserLayer().disableHightlight();
        });

        /*toolbar.find('input[id=irregular' + this.model.get('id') + ']').click(function () {
         self.getUserLayer().toggleIrregular();
         });
         toolbar.find('input[id=irregular' + this.model.get('id') + ']').hide();*/

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
                    //layerAnnotation.initHightlight(self.map);
                    if (user.get('id') == window.app.status.user.id) {
                        self.userLayer = layerAnnotation;
                        layerAnnotation.initControls(self);
                        layerAnnotation.registerEvents(self.map);
                        self.userLayer.toggleIrregular();
                        //simulate click on Navigate button
                        var toolbar = $('#toolbar' + self.model.get('id'));
                        toolbar.find('input[id=none' + self.model.get('id') + ']').click();
                    }
                    colorIndex++;

                });
            }
        });
    },
    initOntology: function () {



        var self =this;


        var ontology = new ProjectModel({id:window.app.status.currentProject}).fetch({
            success : function(model, response) {
                var idOntology = model.get('ontology');
                var ontology = new OntologyModel({id:idOntology}).fetch({
                    success : function(model, response) {
                        self.ontologyTreeView = new OntologyTreeView({
                            el: $("#ontologyTree" + self.model.get("id")),
                            idImage: self.model.get("id"),
                            model: model
                        }).render();
                        self.initVectorLayers();
                    }
                });

                new DraggablePanelView({
                    el : $('#ontologyTree' + self.model.get('id')),
                    template : ich.ontologytreecontenttpl({id : self.model.get('id')}, true)/*,
                     dialogAttr : {
                     dialogID : "#ontologytreedialog" + this.model.get('id'),
                     width : 200,
                     height : 200,
                     css : {left: '30px', right : 'auto', top: 'auto', bottom : '100px'}
                     }*/
                }).render();
            }
        });



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
    hoverControl : null,
    deleteOnSelect : false, //true if select tool checked
    registerEvents: function (map) {
        var self = this;

        this.vectorsLayer.events.on({
            featureselected: function (evt) {
                self.ontologyTreeView.refresh(evt.feature.attributes.idAnnotation);
                console.log("self.deleteOnSelect =>" +self.deleteOnSelect );
                if (self.deleteOnSelect == true) {
                    self.removeSelection();
                };
                self.showPopup(map, evt);
            },
            'featureunselected': function (evt) {
                if (self.dialog != null) self.dialog.destroy();
                console.log("featureunselected");
                self.ontologyTreeView.clear();
                self.ontologyTreeView.clearAnnotation();
                self.clearPopup(map, evt);
                //alias.ontologyTreeView.refresh(null);
            },
            'featureadded': function (evt) {
                /* Check if feature must throw a listener when it is added
                 * true: annotation already in database (no new insert!)
                 * false: new annotation that just have been draw (need insert)
                 * */
                if (evt.feature.attributes.listener != 'NO') {
                    self.addAnnotation(evt.feature);
                }
            },
            'beforefeaturemodified': function (evt) {
                console.log("Selected " + evt.feature.id + " for modification");
            },
            'afterfeaturemodified': function (evt) {

                self.updateAnnotation(evt.feature);

            },
            'onDelete': function (feature) {
                console.log("delete " + feature.id);
            }
        });
    },
    initControls: function (map) {
        this.controls = {
            'point': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Point),
            'line': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Path),
            'polygon': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Polygon),
            'regular': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.RegularPolygon, {
                handlerOptions: {
                    sides: 5
                }
            }),
            'modify': new OpenLayers.Control.ModifyFeature(this.vectorsLayer),
            'select': new OpenLayers.Control.SelectFeature(this.vectorsLayer)
        }
        map.initTools(this.controls);
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
    selectFeature: function (feature) {
        this.controls.select.unselectAll();
        this.controls.select.select(feature);
    },
    removeFeature: function (idAnnotation) {
        var feature = this.features[idAnnotation];
        this.vectorsLayer.removeFeatures(feature);
        this.ontologyTreeView.clearAnnotation();
        this.ontologyTreeView.clear();
        this.features[idAnnotation] = null;

    },
    removeSelection: function () {
        for (var i in this.vectorsLayer.selectedFeatures) {
            var feature = this.vectorsLayer.selectedFeatures[i];
            console.log(feature);
            this.removeAnnotation(feature);
        }
    },
    clearPopup : function (map, evt) {
        feature = evt.feature;
        if (feature.popup) {
            popup.feature = null;
            map.removePopup(feature.popup);
            feature.popup.destroy();
            feature.popup = null;
            popup = null;
        }
    },
    showPopup : function(map, evt) {
        //console.log(e.type, e.feature.id, e.feature.attributes.idAnnotation);
        if(evt.feature.popup != null){
            return;
        }
        new AnnotationModel({id : evt.feature.attributes.idAnnotation}).fetch({
            success : function (model, response) {
                var content = ich.popupannotationtpl(model.toJSON(), true);
                popup = new OpenLayers.Popup("chicken",
                        new OpenLayers.LonLat(evt.feature.geometry.getBounds().right + 50, evt.feature.geometry.getBounds().bottom + 50),
                        new OpenLayers.Size(200,60),
                        content,
                        false);
                popup.setBackgroundColor("transparent");
                popup.setBorder(0);
                popup.padding = 0;

                evt.feature.popup = popup;
                popup.feature = evt.feature;
                map.addPopup(popup);
            }
        });
    },
    enableHightlight : function () {
        //this.hoverControl.activate();
    },
    disableHightlight : function () {
        //this.hoverControl.deactivate();
    },
    initHightlight : function (map) { //buggy :(
        this.hoverControl = new OpenLayers.Control.SelectFeature(this.vectorsLayer, {
            hover: true,
            highlightOnly: true,
            renderIntent: "temporary",
            eventListeners: {

                featurehighlighted: this.showPopup,
                featureunhighlighted: this.clearpopup
            }
        });


        map.addControl(this.hoverControl);
        //this.hoverControl.activate();
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


        new BeginTransactionModel({}).save({}, {
            success: function (model, response) {
                console.log(response.message);
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
            error: function (model, response) {
                console.log("ERRORR: error transaction begin");
            }
        });

    },
    addTermCallback : function(total, counter, oldFeature, newFeature) {
        if (counter < total) return;
        this.addFeature(newFeature);
        this.controls.select.unselectAll();
        this.controls.select.select(newFeature);
        this.vectorsLayer.removeFeatures([oldFeature]);
        new EndTransactionModel({}).save();
    },
    removeTermCallback : function(total, counter, feature,idAnnotation) {
        console.log("counter " + counter + " vs " + total);
        if (counter < total) return;
        this.removeFeature(feature);
        this.controls.select.unselectAll();
        this.vectorsLayer.removeFeatures([feature]);

        new AnnotationModel({id:feature.attributes.idAnnotation}).destroy({success: function(){
            console.log("Delete annotation");
            console.log("End transaction");
            new EndTransactionModel({}).save();
        }});



    },
    removeAnnotation : function(feature) {
        var alias = this;
        var idAnnotation = feature.attributes.idAnnotation;
        console.log("Delete annotation id ="+idAnnotation);
        var annotation = new AnnotationModel({id:idAnnotation});
        var counter = 0;
        new BeginTransactionModel({}).save({}, {
            success: function (model, response) {

                new AnnotationTermCollection({idAnnotation:idAnnotation}).fetch({success:function (collection, response){
                    if (collection.size() == 0) {
                        alias.removeTermCallback(0,0, feature, idAnnotation);
                        return;
                    }
                    collection.each(function(term) {
                        console.log("delete term="+term.id + " from annotation " + idAnnotation);
                        console.log("annotationTerm="+JSON.stringify(term));

                        new AnnotationTermModel({annotation:idAnnotation,term:term.id}).destroy({success : function (model, response) {
                            alias.removeTermCallback(collection.length, ++counter, feature, idAnnotation);
                        }});

                    });

                }});

            },
            error: function (model, response) {
                console.log("ERRORR: error transaction begin");
            }
        });

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
        this.resize = false;
        this.drag = false;
        this.rotate = true;
        this.updateControls();
        this.toggleControl("modify");
    },
    toggleResize: function () {
        this.resize = true;
        this.drag = false;
        this.rotate = false;
        this.updateControls();
        this.toggleControl("modify");
    },
    toggleDrag: function () {
        this.resize = false;
        this.drag = true;
        this.rotate = false;
        this.updateControls();
        this.toggleControl("modify");

    },
    toggleEdit: function () {
        this.resize = false;
        this.drag = false;
        this.rotate = false;
        this.updateControls();
        this.toggleControl("modify");

    },
    toggleIrregular: function () {
        console.log("toggleIrregular");
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
        //Simulate an OpenLayers.Control.EraseFeature tool by using SelectFeature with the flag 'deleteOnSelect'
        this.deleteOnSelect = false;
        for (key in this.controls) {
            var control = this.controls[key];
            if (name == key) {
                control.activate();
                console.log("activate " + name);
                if (control == this.controls.modify)  {
                    for (var i in this.vectorsLayer.selectedFeatures) {
                        var feature = this.vectorsLayer.selectedFeatures[i];
                        control.selectFeature(feature);
                    }
                }
            } else {
                control.deactivate();
                if (control == this.controls.modify)  {
                    for (var i in this.vectorsLayer.selectedFeatures) {
                        var feature = this.vectorsLayer.selectedFeatures[i];
                        control.unselectFeature(feature);
                    }

                }
            }
        }

    },
    /* Callbacks undo/redo */
    annotationAdded: function (idAnnotation) {
        var self = this;
        this.controls.select.deactivate();
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
                         self.selectFeature(feature);
                         self.controls.select.activate();
                     }
                 });

    },
    annotationRemoved: function (idAnnotation) {
        this.removeFeature(idAnnotation);
    },
    annotationUpdated: function (idAnnotation, idImage) {
        this.annotationRemoved(idAnnotation);
        this.annotationAdded(idAnnotation);
    },
    termAdded: function (idAnnotation, idTerm) {
        var self = this;
        console.log("termAdded");
        this.ontologyTreeView.check(idTerm);
    },
    termRemoved: function (idAnnotation, idTerm) {
        console.log("termRemoved");
        this.ontologyTreeView.uncheck(idTerm);
    }
}