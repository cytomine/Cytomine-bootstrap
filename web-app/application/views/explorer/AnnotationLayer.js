var AnnotationStatus = {
	NO_TERM : 'NO_TERM',
	MULTIPLE_TERM : 'MULTIPLE_TERM',
	TOO_SMALL : 'TOO_SMALL'
}
var AnnotationLayerUtils = AnnotationLayerUtils || {};
AnnotationLayerUtils.createFeatureFromAnnotation  = function (annotation) {
    var location = annotation.location || annotation.get('location');
    var terms = annotation.term || annotation.get('term');
    var format = new OpenLayers.Format.WKT();
    var point = format.read(location);
    var geom = point.geometry;
    var feature = new OpenLayers.Feature.Vector(geom);
    var term = AnnotationStatus.NO_TERM; //no term associated
    if (terms.length > 1) { //multiple term
        term = AnnotationStatus.MULTIPLE_TERM;
    } else if (terms.length == 1) {
        term = terms[0]; //put ID
    }
    feature.attributes = {
        idAnnotation: annotation.id,
        measure : 'NO',
        listener: 'NO',
        importance: 10,
        term : term
    };
    console.log("FEATURE="+feature);
    return feature;
};

/* Annotation Layer */
OpenLayers.Format.Cytomine = OpenLayers.Class(OpenLayers.Format, {
    read: function(collection) {
        var self = this;
        var features = [];
        _.each(collection,function(annotation) {
            if (_.indexOf(self.annotationLayer.featuresHidden, annotation.id) != -1) return;
            var feature = AnnotationLayerUtils.createFeatureFromAnnotation(annotation);
            features.push(feature);
        });
        return features;
    }

});

var AnnotationLayer = function (name, imageID, userID, color, ontologyTreeView, browseImageView, map) {
    this.ontologyTreeView = ontologyTreeView;
	this.pointRadius = window.app.view.isMobile ? 12 : 8;
    this.name = name;
    this.map = map;
    this.imageID = imageID;
    this.userID = userID;
	var rules = [new OpenLayers.Rule({
		symbolizer: {strokeColor:"#FF0000",strokeWidth: 2}, 
		// symbolizer: {}, // instead if you want to keep default colors 
		elseFilter: true 
	})]; 

	var defaultStyle =  new OpenLayers.Style({
		'fillColor'   : '#EEEEEE',
		'fillOpacity' : .8,
		'strokeColor' : '#000000',
		'strokeWidth' : 3,
		'pointRadius' : this.pointRadius
	});
	defaultStyle.addRules(rules);
	var selectStyle = new OpenLayers.Style({
		'fillColor'   : '#EEEEEE',
		'fillOpacity' : .8,
		'strokeColor' : '#FF0000',
		'strokeWidth' : 3,
		'pointRadius' : this.pointRadius
	});
	selectStyle.addRules(rules);
	var styleMap = new OpenLayers.StyleMap({
		'default' : defaultStyle,
		'select' : selectStyle
	});
	styleMap.styles["default"].addRules(rules);
	styleMap.addUniqueValueRules('default', 'term', this.getSymbolizer());
    this.vectorsLayer = new OpenLayers.Layer.Vector(this.name, {
        renderers: ["Canvas", "SVG", "VML"],
        strategies: [
			new OpenLayers.Strategy.BBOX({resFactor: 1})
		],
        protocol: new OpenLayers.Protocol.Script({
            url: new AnnotationCollection({user : this.userID, image : this.imageID, term: undefined}).url().replace("json", "jsonp"),
            format: new OpenLayers.Format.Cytomine({ annotationLayer : this}),
            callbackKey : "callback"
        }),
		'styleMap' : styleMap
	});
    this.controls = null;
    this.dialog = null;
    this.rotate = false;
    this.featuresHidden = [];
    this.resize = false;
    this.drag = false;
    this.irregular = false;
    this.aspectRatio = false;
    this.browseImageView = browseImageView;
    this.map = browseImageView.map;
    this.popup = null;
    this.hoverControl = null;
    this.triggerUpdateOnUnselect = true;
    this.isOwner = null;
    this.deleteOnSelect = false; //true if select tool checked
    this.measureOnSelect = false;
    this.magicOnClick = false;
    this.cpt = 0;
}

AnnotationLayer.prototype = {
	getSymbolizer : function() {
		var symbolizers_lookup = {};
		symbolizers_lookup[AnnotationStatus.NO_TERM] = { //NO TERM ASSOCIATED
			'fillColor'   : "#EEEEEE",
			'fillOpacity' : .6,
			'strokeColor' : '#000000',
			'strokeWidth' : 3,
			'pointRadius' : this.pointRadius
		};
		symbolizers_lookup[AnnotationStatus.MULTIPLE_TERM] = { //MULTIPLE TERM ASSOCIATED
			'fillColor'   : "#CCCCCC",
			'fillOpacity' : .6,
			'strokeColor' : '#000000',
			'strokeWidth' : 3,
			'pointRadius' : this.pointRadius
		};
		symbolizers_lookup[AnnotationStatus.TOO_SMALL] = { //MULTIPLE TERM ASSOCIATED
			'fillColor'   : "#FF0000",
			'fillOpacity' : 1,
			'strokeColor' : '#FF0000',
			'strokeWidth' : 5,
			'pointRadius' : this.pointRadius
		};
		window.app.status.currentTermsCollection.each(function (term) {			
			symbolizers_lookup[term.id] = {
				'fillColor'   : term.get('color'),
				'fillOpacity' : .6,
				'strokeColor' : '#000000',
				'strokeWidth' : 3,
				'pointRadius' : this.pointRadius
			}
		});
		return symbolizers_lookup
	},
    registerEvents: function (map) {

        var self = this;

        this.vectorsLayer.events.on({
            clickFeature : function (evt) {

            },
            onSelect : function (evt) {

            },
            featureselected: function (evt) {
                if (!self.measureOnSelect) {
                    self.ontologyTreeView.refresh(evt.feature.attributes.idAnnotation);

                    if (self.deleteOnSelect == true) {
                        self.removeSelection();
                    } else {
                        self.showPopup(map, evt);
                        //self.vectorsLayer.drawFeature(evt.feature);
                    }
                }
                else self.showPopupMeasure(map, evt);

            },
            'featureunselected': function (evt) {

                if (self.measureOnSelect) self.vectorsLayer.removeFeatures(evt.feature);

                if (self.dialog != null) self.dialog.destroy();
                self.ontologyTreeView.clear();
                self.ontologyTreeView.clearAnnotation();
                self.clearPopup(map, evt);
                self.vectorsLayer.drawFeature(evt.feature);
                //alias.ontologyTreeView.refresh(null);
            },
            'featureadded': function (evt) {
                //Abort if the geometry contains less than 3 vertices
//                if (evt.feature.geometry.getVertices().length < 3) {
//                    self.vectorsLayer.removeFeatures(evt.feature);
//                    return;
//                }

                /* Check if feature must throw a listener when it is added
                 * true: annotation already in database (no new insert!)
                 * false: new annotation that just have been draw (need insert)
                 * */
                if (!self.measureOnSelect) {
                    if (evt.feature.attributes.listener != 'NO') {

                        evt.feature.attributes.measure = 'YES';
                        self.addAnnotation(evt.feature);
                    }
                }
                else {
                    self.controls.select.unselectAll();
                    self.controls.select.select(evt.feature);
                }

            },
            'sketchcomplete': function (evt) {
                if (self.triggerUpdateOnUnselect) self.updateAnnotation(evt.feature);
            },
            'featuremodified': function (evt) {
                //prevent to update an annotation when it is unnecessary
                if (self.triggerUpdateOnUnselect) self.updateAnnotation(evt.feature);
            },
            'onDelete': function (feature) {
            },
            "moveend": function() {
                self.clearPopup();
            }
        });
    },
    initControls: function (map, selectFeature) {
        /*if (isOwner) { */

        this.controls = {
            'freehand': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Polygon, {
                handlerOptions: {
                    freehand: true,
                    holeModifier: "altKey"
                }
            }),
            'point': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Point),
            'line': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Path),
            'polygon': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Polygon, {
                handlerOptions: {
                    holeModifier: "altKey"
                }
            }),
            'regular': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.RegularPolygon, {
                handlerOptions: {
                    sides: 5,
                    holeModifier: "altKey"
                }
            }),
            'modify': new OpenLayers.Control.ModifyFeature(this.vectorsLayer),
            'select': selectFeature
        }
        this.controls.freehand.freehand = true;

        /* else {

         this.controls = {
         'select': new OpenLayers.Control.SelectFeature(this.vectorsLayer)
         }
         }*/
        map.initTools(this.controls);

    },
    loadAnnotations: function (browseImageView) {

        var self = this;
        browseImageView.addVectorLayer(this, this.userID);
        /*new AnnotationCollection({user : this.userID, image : this.imageID, term: undefined}).fetch({
         success : function (collection, response) {
         collection.each(function(annotation) {
         var feature = self.AnnotationLayerUtils.createFeatureFromAnnotation(annotation);
         self.addFeature(feature);
         });
         browseImageView.layerLoadedCallback(self);
         }
         });*/

        browseImageView.layerLoadedCallback(self);
    },
    addFeature: function (feature) {
        this.vectorsLayer.addFeatures(feature);
    },
    selectFeature: function (feature) {
        this.controls.select.unselectAll();
        this.controls.select.select(feature);
    },
    removeFeature: function (idAnnotation) {
        var feature = this.getFeature(idAnnotation);
        this.vectorsLayer.removeFeatures(feature);
        this.ontologyTreeView.clearAnnotation();
        this.ontologyTreeView.clear();
    },
    getFeature : function(idAnnotation) {
        var features = this.vectorsLayer.getFeaturesByAttribute("idAnnotation", idAnnotation);
        if (_.isArray(features) && _.size(features) > 0) return features[0];
        return null;
    },
    removeSelection: function () {
        for (var i in this.vectorsLayer.selectedFeatures) {
            var feature = this.vectorsLayer.selectedFeatures[i];
            this.removeAnnotation(feature);
        }
    },
    clearPopup : function (map, evt) {
        var self = this;
        var elem = $("#annotationDetailPanel"+self.browseImageView.model.id);
        elem.empty();
        elem.hide();
    },
    hideFeature : function(feature) {
        var idAnnotation = feature.attributes.idAnnotation;
        if (_.indexOf(this.featuresHidden,idAnnotation) == -1) {
            this.featuresHidden.push(idAnnotation);
        }
        this.vectorsLayer.refresh();
    },
    showFeature : function(idAnnotation) {
        this.featuresHidden = _.without(this.featuresHidden, idAnnotation);
        this.vectorsLayer.refresh();
    },
    showPopup : function(map, evt) {
        var self = this;
        require([
            "text!application/templates/explorer/PopupAnnotation.tpl.html"
        ], function(tpl) {
            new AnnotationModel({id : evt.feature.attributes.idAnnotation}).fetch({
                success : function (annotation, response) {
                    annotation.set({"username" : window.app.models.users.get(annotation.get("user")).prettyName()});

                    var terms = new Array();
                    //browse all term and compute the number of user who add this term
                    _.each(annotation.get("userByTerm"), function(termuser) {
                        var idTerm = termuser.term;
                        var termName = window.app.status.currentTermsCollection.get(idTerm).get('name');
                        var userCount = termuser.user.length;
                        var idOntology = window.app.status.currentProjectModel.get('ontology');
                        var tpl = _.template("<a href='#ontology/<%=   idOntology %>/<%=   idTerm %>'><%=   termName %></a> (<%=   userCount %>)", {idOntology : idOntology, idTerm : idTerm, termName : termName, userCount: userCount});
                        terms.push(tpl);

                    });
                    annotation.set({"terms" : terms.join(", ")});

                    var content = _.template(tpl, annotation.toJSON());
                    var elem = $("#annotationDetailPanel"+self.browseImageView.model.id);
                    elem.html(content);
                    elem.show();
                    $("#annotationHide" + annotation.id).off('click');
                    $("#annotationHide" + annotation.id).on('click',function() {

                        self.controls.select.unselectAll();
                        var feature = self.getFeature(annotation.id);
                        if (feature == undefined || feature == null) return false;
                        self.hideFeature(feature);
                        var url = "tabs-image-"+window.app.status.currentProjectModel.get("id")+"-"+self.browseImageView.model.get("id")+"-";
                        window.app.controllers.browse.navigate(url, false);
                        return false;
                    });
                    if (window.app.status.currentProjectModel.get('privateLayer') || window.app.status.currentProjectModel.get('retrievalDisable')){
                        $("#loadSimilarAnnotation" + annotation.id).html("Retrieval not available");
                    } else {
                        self.showSimilarAnnotation(annotation);
                    }
                }
            });
        });
    },
    showSimilarAnnotation : function(model) {
        var self = this;
        console.log('showSimilarAnnotation');
        if(window.app.status.currentTermsCollection==undefined || (window.app.status.currentTermsCollection.length > 0 && window.app.status.currentTermsCollection.at(0).id == undefined)) {
            new TermCollection({idOntology:window.app.status.currentProjectModel.get('ontology')}).fetch({
                success : function (terms, response) {
                    window.app.status.currentTermsCollection=terms;
                    self.showSimilarAnnotationResult(model);
            }});
        } else {
            self.showSimilarAnnotationResult(model);
        }
    },
    showSimilarAnnotationResult : function(model) {
        var self = this;
        console.log('showSimilarAnnotationResult');
        new AnnotationRetrievalModel({annotation : model.id}).fetch({
            success : function (collection, response) {

                var termsList = collection.get('term');

                //var termsCollection = new TermCollection(termsList);

                var bestTerm1Object;
                var bestTerm2Object;

                var sum = 0;
                var i = 0;

                _.each(termsList,function(term) {
                    sum = sum + term.rate;
                    if(i==0) bestTerm1Object = term;
                    if(i==1) bestTerm2Object = term;
                    i++;
                });

                var bestTerm1;
                var bestTerm2;
                var bestTerm1Value = 0;
                var bestTerm2Value = 0;
                if(bestTerm1Object!=undefined) {
                    bestTerm1 = bestTerm1Object.id;
                    bestTerm1Value = bestTerm1Object.rate;
                    if(bestTerm1Value==0) bestTerm1 = undefined;
                }
                if(bestTerm2Object!=undefined) {
                    bestTerm2 = bestTerm2Object.id;
                    bestTerm2Value = bestTerm2Object.rate;
                    if(bestTerm2Value==0) bestTerm2 = undefined;
                }

                bestTerm1Value = (bestTerm1Value / sum) * 100;
                bestTerm2Value = (bestTerm2Value / sum) * 100;

                //Print suggested Term
                self.printSuggestedTerm(model, window.app.status.currentTermsCollection.get(bestTerm1), window.app.status.currentTermsCollection.get(bestTerm2), bestTerm1Value, bestTerm2Value, window.app.status.currentTermsCollection, collection.get('annotation'));
            },error: function (bad, response) {
                $("#loadSimilarAnnotation" + model.id).replaceWith("Error: cannot reach retrieval");
            }});
    },
    printSuggestedTerm : function(annotation, bestTerm1, bestTerm2, bestTerm1Value, bestTerm2Value, terms, similarAnnotation) {
        var self = this;
        var suggestedTerm = "";
        var suggestedTerm2 = "";
        if (bestTerm1 != undefined)
            suggestedTerm += "<span id=\"changeBySuggest" + bestTerm1.id + "\" style=\"display : inline\"><u>" + bestTerm1.get('name') + "</u> (" + Math.round(bestTerm1Value) + "%)<span>";
        if (bestTerm2 != undefined)
            suggestedTerm2 += " or " + "<span id=\"changeBySuggest" + bestTerm2.id + "\" style=\"display : inline\"><u>" + bestTerm2.get('name') + "</u> (" + Math.round(bestTerm2Value) + "%)<span>";

        $("#suggTerm" + annotation.id).empty();
        $("#suggTerm" + annotation.id).append(suggestedTerm);
        $("#suggTerm" + annotation.id).append(suggestedTerm2);
        if (bestTerm1 != undefined) {
            self.createSuggestedTermLink(bestTerm1, annotation);
        }

        if (bestTerm2 != undefined) {
            self.createSuggestedTermLink(bestTerm2, annotation);
        }

        $("#loadSimilarAnnotation" + annotation.id).replaceWith("<a href=\"#\" id=\"annotationSimilar" + annotation.id + "\"> Search similar annotations</a>");
        $("#annotationSimilar" + annotation.id).click(function() {
            $('#annotationRetrieval').replaceWith("");
            $("#annotationRetrievalMain").empty();
            $("#annotationRetrievalMain").append("<div id=\"annotationRetrieval\"></div>");


            var bestTerms = [bestTerm1,bestTerm2];
            var bestTermsValue = [bestTerm1Value, bestTerm2Value];
            var panel = new AnnotationRetrievalView({
                model : new AnnotationRetrievalCollection(similarAnnotation),
                projectsPanel : self,
                container : self,
                el : "#annotationRetrieval",
                baseAnnotation : annotation,
                terms : terms,
                bestTerms : bestTerms,
                bestTermsValue : bestTermsValue
            }).render();
            return false;

        });
    },

    createSuggestedTermLink : function(term, annotation) {
        var self = this;
        $("#changeBySuggest" + term.id).click(function() {
            new AnnotationTermModel({term: term.id,annotation: annotation.id,clear : true
            }).save(null, {success : function (termModel, response) {
                window.app.view.message("Correct Term", response.message, "");
                //Refresh tree
                self.ontologyTreeView.refresh(annotation.id);
            } ,error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Correct term", "error:" + json.errors, "");
            }});
        });
    },

    showPopupMeasure : function(map, evt) {
        var self = this;
        require([
            "text!application/templates/explorer/PopupMeasure.tpl.html"
        ], function(tpl) {
            if (evt.feature.popup != null) {
                return;
            }
            var resolution = self.browseImageView.model.get("resolution");
            var length = evt.feature.geometry.getLength();
            if (resolution != undefined && resolution != null) {
                length *= resolution;
                length = Math.round(length * 1000) / 1000;
                length += " µm";
            } else {
                length += " pixels";
            }
            var content = _.template(tpl, {length:length});
            self.popup = new OpenLayers.Popup("chicken",
                new OpenLayers.LonLat(evt.feature.geometry.getBounds().right + 50, evt.feature.geometry.getBounds().bottom + 50),
                new OpenLayers.Size(200, 60),
                content,
                false);
            self.popup.setBackgroundColor("transparent");
            self.popup.setBorder(0);
            self.popup.padding = 0;

            evt.feature.popup = self.popup;
            self.popup.feature = evt.feature;
            map.addPopup(self.popup);
        });


    },
    enableHightlight : function () {
        //this.hoverControl.activate();
    },
    disableHightlight : function () {
        //this.hoverControl.deactivate();
    },
    /*Add annotation in database*/
    addAnnotation: function (feature) {
        var alias = this;
        var self = this;
        var format = new OpenLayers.Format.WKT();
        var geomwkt = format.write(feature);
        var terms = alias.ontologyTreeView.getTermsChecked();
        var annotation = new AnnotationModel({
            name: "",
            location: geomwkt,
            image: this.imageID,
            term:terms
        });

        annotation.save({}, {
            success: function (annotation, response) {
                new AnnotationModel({id : response.annotation.id}).fetch({
                    success : function (annotation, response) {
                        //annotation.set(response.annotation.id);
                        var message = response.message;
                        self.vectorsLayer.removeFeatures([feature]);
                        var newFeature = AnnotationLayerUtils.createFeatureFromAnnotation(annotation);
                        self.addFeature(newFeature);
                        self.controls.select.unselectAll();
                        self.controls.select.select(newFeature);
                        var cropURL = annotation.get('cropURL');
                        var cropImage = _.template("<img src='<%=   url %>' alt='<%=   alt %>' style='max-width: 175px;max-height: 175px;' />", { url : cropURL, alt : cropURL});
                        var alertMessage = _.template("<p><%=   message %></p><div><%=   cropImage %></div>", { message : message, cropImage : cropImage});
                        window.app.view.message("Annotation added", alertMessage, "success");
                    }
                });

            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Add annotation", "error:" + json.errors, "error");
            }
        });


    },
    removeAnnotation : function(feature) {
        var idAnnotation = feature.attributes.idAnnotation;
        this.removeFeature(feature);
        this.controls.select.unselectAll();
        this.vectorsLayer.removeFeatures([feature]);
        var self = this;
        new AnnotationModel({id:feature.attributes.idAnnotation}).destroy({
            success: function (model, response) {
                window.app.view.message("Annotation", response.message, "success");
                self.browseImageView.refreshAnnotationTabs(undefined);

            },
            error: function (model, response) {
                var json = $.parseJSON(response.responseText);
                window.app.view.message("Annotation", json.errors, "error");
            }
        });
    },

    /*Modifiy annotation on database*/
    updateAnnotation: function (feature) {
        if (feature.attributes.idAnnotation == undefined) return;
        var self = this;
        var format = new OpenLayers.Format.WKT();
        var geomwkt = format.write(feature);
        new AnnotationModel({id:feature.attributes.idAnnotation}).fetch({
            success : function(model, response) {

                model.save({location : geomwkt},{
                    success : function (annotation, response) {
                        var message = response.message;
                        var alertMessage = _.template("<p><%=   message %></p>", { message : message});
                        window.app.view.message("Annotation edited", alertMessage, "success");
                    },
                    error : function(model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Annotation", json.errors, "");
                    }
                });
            }
        });
    },
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
    disableMeasureOnSelect : function() {
        var self = this;
        this.measureOnSelect = false;
        //browse measure on select
        for (var i in this.vectorsLayer.features) {
            var feature = this.vectorsLayer.features[i];


            if (feature.attributes.measure == undefined || feature.attributes.measure == 'YES') {
                self.vectorsLayer.removeFeatures(feature);
                if (feature.popup) {
                    self.popup.feature = null;
                    self.map.removePopup(feature.popup);
                    feature.popup.destroy();
                    feature.popup = null;
                    self.popup = null;
                }

            }
        }
    },
    toggleControl: function (name) {
        //Simulate an OpenLayers.Control.EraseFeature tool by using SelectFeature with the flag 'deleteOnSelect'
        this.deleteOnSelect = false;
        this.disableMeasureOnSelect();
        this.magicOnClick = false;
        for (key in this.controls) {
            var control = this.controls[key];
            if (name == key) {
                control.activate();

                if (control == this.controls.modify) {
                    for (var i in this.vectorsLayer.selectedFeatures) {
                        var feature = this.vectorsLayer.selectedFeatures[i];
                        control.selectFeature(feature);
                    }
                }
            } else {
                control.deactivate();
                if (control == this.controls.modify) {
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
        var deleteOnSelectBackup = self.deleteOnSelect;
        self.deleteOnSelect = false;
        new AnnotationModel({
            id: idAnnotation
        }).fetch({
            success: function (model) {
                var feature = AnnotationLayerUtils.createFeatureFromAnnotation(model);
                self.addFeature(feature);
                self.selectFeature(feature);
                self.controls.select.activate();
                self.deleteOnSelect = deleteOnSelectBackup;
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
        this.ontologyTreeView.check(idTerm);
        this.refresh();
    },
    termRemoved: function (idAnnotation, idTerm) {
        this.ontologyTreeView.uncheck(idTerm);
        this.refresh();
    }
};