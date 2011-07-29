/* Annotation Layer */


var AnnotationLayer = function (name, imageID, userID, color, ontologyTreeView, browseImageView, map) {

   var styleMap = new OpenLayers.StyleMap({
      "default" : OpenLayers.Util.applyDefaults({fillColor: color, fillOpacity: 0.5, strokeColor: "black", strokeWidth: 2},
          OpenLayers.Feature.Vector.style["default"]),
      "select" : OpenLayers.Util.applyDefaults({fillColor: "#25465D", fillOpacity: 0.5, strokeColor: "black", strokeWidth: 2},
          OpenLayers.Feature.Vector.style["default"])
   });
   this.ontologyTreeView = ontologyTreeView;
   this.name = name;
   this.map = map,
       this.imageID = imageID;
   this.userID = userID;
   this.vectorsLayer = new OpenLayers.Layer.Vector(this.name, {
      styleMap: styleMap,
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
   this.browseImageView = browseImageView;
   this.map = browseImageView.map;
   this.popup = null;
   this.hoverControl = null;
   this.isOwner = null;
   this.deleteOnSelect = false; //true if select tool checked
   this.measureOnSelect = false;
   this.magicOnClick = false;
}

AnnotationLayer.prototype = {



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
            //alias.ontologyTreeView.refresh(null);
         },
         'featureadded': function (evt) {

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
         'beforefeaturemodified': function (evt) {

         },
         'afterfeaturemodified': function (evt) {

            self.updateAnnotation(evt.feature);

         },
         'onDelete': function (feature) {

         }
      });
   },
   initControls: function (map, selectFeature) {
      /*if (isOwner) { */

      this.controls = {
         'freehand': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Polygon, {handlerOptions: {freehand: true}}),
         'point': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Point),
         'line': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Path),
         'polygon': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.Polygon),
         'regular': new OpenLayers.Control.DrawFeature(this.vectorsLayer, OpenLayers.Handler.RegularPolygon, {
            handlerOptions: {
               sides: 5
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
      map.initAutoAnnoteTools();
   },


   /*Load annotation from database on layer */
   loadAnnotations: function (browseImageView) {

      var self = this;
      new AnnotationCollection({user : this.userID, image : this.imageID, term: undefined}).fetch({
         success : function (collection, response) {
            collection.each(function(annotation) {
               var feature = self.createFeatureFromAnnotation(annotation);
               self.addFeature(feature);
            });
            browseImageView.layerLoadedCallback(self);
         }
      });
      browseImageView.addVectorLayer(this, this.userID);
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
      var feature = this.getFeature(idAnnotation);
      if (feature != null && feature.popup) {
         this.map.removePopup(feature.popup);
         feature.popup.destroy();
         feature.popup = null;
         this.popup = null;
      }
      this.vectorsLayer.removeFeatures(feature);
      this.ontologyTreeView.clearAnnotation();
      this.ontologyTreeView.clear();
      this.features[idAnnotation] = null;

   },
   getFeature : function(idAnnotation) {
      return this.features[idAnnotation];
   },
   removeSelection: function () {
      for (var i in this.vectorsLayer.selectedFeatures) {
         var feature = this.vectorsLayer.selectedFeatures[i];

         this.removeAnnotation(feature);
      }
   },
   clearPopup : function (map, evt) {
      var self = this;
      feature = evt.feature;
      if (feature.popup) {
         self.popup.feature = null;
         map.removePopup(feature.popup);
         feature.popup.destroy();
         feature.popup = null;
         self.popup = null;
      }
   },
   hideAnnotation : function(idAnnotation) {
      var feature = this.getFeature(idAnnotation);
      this.controls.select.unselectAll();
      this.vectorsLayer.drawFeature(feature, { display : 'none'});

      //alert("feature != null ? " + (feature.getVisibility()));
   },
   showPopup : function(map, evt) {
      var self = this;
      require([
         "text!application/templates/explorer/PopupAnnotation.tpl.html"
      ], function(tpl) {
         //
         if (evt.feature.popup != null) {
            return;
         }
         new AnnotationModel({id : evt.feature.attributes.idAnnotation}).fetch({
            success : function (model, response) {
               var json = model.toJSON()
               //username
               json.username = window.app.models.users.get(json.user).prettyName();
               //term
               var terms = new Array();
               _.each(json.term,function(idTerm){
                  var tpl = _.template("<a href='#ontology/{{idOntology}}/{{idTerm}}'>{{termName}}</a>", {idOntology : window.app.status.currentProjectModel.get('ontology'), idTerm : idTerm, termName : window.app.status.currentTermsCollection.get(idTerm).get('name')});
                  terms.push(tpl);

               });
               json.terms = terms.join(", ");

               var content = _.template(tpl, json);
               self.popup = new OpenLayers.Popup("",
                   new OpenLayers.LonLat(evt.feature.geometry.getBounds().right + 50, evt.feature.geometry.getBounds().bottom + 50),
                   new OpenLayers.Size(250, 100),
                   content,
                   false);
               self.popup.setBackgroundColor("transparent");
               self.popup.setBorder(0);
               self.popup.padding = 0;

               evt.feature.popup = self.popup;
               self.popup.feature = evt.feature;
               map.addPopup(self.popup);
               $("#annotationHide"+model.id).click(function(){
                  self.hideAnnotation(model.id);
                  return false;
               });
            }
         });
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
         var length =  evt.feature.geometry.getLength();
         if (resolution != undefined && resolution != null) {
            length *= resolution;
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
   initHightlight : function (map) { //buggy :(
      /*this.hoverControl = new OpenLayers.Control.SelectFeature(this.vectorsLayer, {
       hover: true,
       highlightOnly: true,
       renderIntent: "temporary",
       eventListeners: {

       featurehighlighted: this.showPopup,
       featureunhighlighted: this.clearpopup
       }
       });


       map.addControl(this.hoverControl);
       //this.hoverControl.activate();   */
   },

   /*Add annotation in database*/
   addAnnotation: function (feature) {

      var format = new OpenLayers.Format.WKT();
      var geomwkt = format.write(feature);
      var alias = this;
      var annotation = new AnnotationModel({
         //"class": "be.cytomine.project.Annotation",
         name: "",
         location: geomwkt,
         image: this.imageID,
         parse: function(response) {

            window.app.view.message("Annotation", response.message, "");
            return response.annotation;
         }
      });


      new BeginTransactionModel({}).save({}, {
         success: function (model, response) {

            annotation.save(annotation.toJSON(), {
               success: function (annotation, response) {


                  var annotationID = response.annotation.id;
                  var message = response.message;

                  var terms = alias.ontologyTreeView.getTermsChecked();

                  if (terms.length == 0) {
                     alias.addTermCallback(0, 0, feature, annotationID, message, undefined);
                  }

                  var counter = 0;
                  _.each(terms, function (idTerm) {
                     new AnnotationTermModel({
                        term: idTerm,
                        annotation: response.annotation.id
                     }).save(null, {success : function (termModel, response) {
                        alias.addTermCallback(terms.length, ++counter, feature, annotationID, message, idTerm);
                     }});
                  });

               },
               error: function (model, response) {
                  var json = $.parseJSON(response.responseText);
                  window.app.view.message("Add annotation", "error:" + json.errors, "");
               }
            });

         },
         error: function (model, response) {

         }
      });

   },
   addTermCallback : function(total, counter, oldFeature, annotationID, message, idTerm) {
      if (counter < total) return;
      var self = this;
      new AnnotationModel({id:annotationID}).fetch({
         success : function (annotation, response) {
            self.vectorsLayer.removeFeatures([oldFeature]);
            var newFeature = self.createFeatureFromAnnotation(annotation);
            self.addFeature(newFeature);
            self.controls.select.unselectAll();
            self.controls.select.select(newFeature);
            window.app.view.message("Add annotation", message, "");
            new EndTransactionModel({}).save();

            self.browseImageView.refreshAnnotationTabs(undefined);
         },
         error : function(model, response) {

            new EndTransactionModel({}).save();
         }
      });

   },
   createFeatureFromAnnotation :function (annotation) {
      var multipleTermColor = "#000000";
      var format = new OpenLayers.Format.WKT();
      var point = format.read(annotation.get("location"));
      var geom = point.geometry;
      var feature = new OpenLayers.Feature.Vector(geom);
      feature.attributes = {
         idAnnotation: annotation.get("id"),
         measure : 'NO',
         listener: 'NO',
         importance: 10
      };
      if (_.size(annotation.get("term")) > 1) { //multiple terms
         feature.style =  {
            strokeColor :multipleTermColor,
            fillColor :  multipleTermColor,
            fillOpacity : 0.6
         }
      } else {
         _.each(annotation.get("term"), function(idTerm){
            feature.style =  {
               strokeColor : window.app.status.currentTermsCollection.get(idTerm).get('color'),
               fillColor :  window.app.status.currentTermsCollection.get(idTerm).get('color'),
               fillOpacity : 0.6
            }
         });
      }

      return feature;
   },
   removeAnnotation : function(feature) {
      var terms = this.ontologyTreeView.getTermsChecked();
      var idAnnotation = feature.attributes.idAnnotation;
      this.removeFeature(feature);
      this.controls.select.unselectAll();
      this.vectorsLayer.removeFeatures([feature]);
      var self = this;


      new AnnotationTermCollection({idAnnotation:idAnnotation}).fetch({success:function (collection, response) {

         new AnnotationModel({id:feature.attributes.idAnnotation}).destroy({
            success: function (model, response) {
               window.app.view.message("Annotation", response.message, "");
               self.browseImageView.refreshAnnotationTabs(undefined);
               console.log("collection="+collection.length);
               /*collection.each(function(term) {
                console.log("term="+term.id);


                });*/
               self.browseImageView.refreshAnnotationTabs(undefined);

            },
            error: function (model, response) {
               var json = $.parseJSON(response.responseText);
               window.app.view.message("Annotation", json.errors, "");
            }
         });

      }});

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

    // Check if feature must throw a listener when it is added
    // true: annotation already in database (no new insert!)
    // false: new annotation that just have been draw (need insert)
    //
    if(evt.feature.attributes.listener!='NO')
    {

    alias.addAnnotation(evt.feature);
    }
    },*/

   /** Triggered when update feature **/
   /* onFeatureUpdate : function (evt) {


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


         if(feature.attributes.measure==undefined || feature.attributes.measure == 'YES') {
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

      var annotation = new AnnotationModel({
         id: idAnnotation
      }).fetch({
         success: function (model) {
            var feature = self.createFeatureFromAnnotation(model);
            /*var format = new OpenLayers.Format.WKT();
             var location = format.read(model.get('location'));
             var feature = new OpenLayers.Feature.Vector(location.geometry);
             feature.attributes = {
             idAnnotation: model.get('id'),
             listener: 'NO',
             measure : 'NO',
             importance: 10
             };*/
            self.addFeature(feature);
            self.selectFeature(feature);
            self.controls.select.activate();
            self.deleteOnSelect = deleteOnSelectBackup;
            self.browseImageView.refreshAnnotationTabs(undefined);
         }
      });

   },
   annotationRemoved: function (idAnnotation) {
      this.removeFeature(idAnnotation);
      this.browseImageView.refreshAnnotationTabs(undefined);
   },
   annotationUpdated: function (idAnnotation, idImage) {
      this.annotationRemoved(idAnnotation);
      this.annotationAdded(idAnnotation);
   },
   termAdded: function (idAnnotation, idTerm) {
      var self = this;

      this.ontologyTreeView.check(idTerm);
   },
   termRemoved: function (idAnnotation, idTerm) {

      this.ontologyTreeView.uncheck(idTerm);
   }
};