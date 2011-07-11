var BrowseImageView = Backbone.View.extend({
       tagName: "div",
       /**
        * BrowseImageView constructor
        * Accept options used for initialization
        * @param options
        */
       initialize: function (options) {
          this.initCallback = options.initCallback;
          this.layers = [];
          this.baseLayers = [];
          this.annotationsPanel = null;
          this.map = null;
          this.currentAnnotation = null;
          _.bindAll(this, "initVectorLayers");
       },
       /**
        * Render the html into the DOM element associated to the view
        * @param tpl
        */
       doLayout: function (tpl) {
          var self = this;
          var templateData = this.model.toJSON();
          templateData.project = window.app.status.currentProject;
          var tpl = _.template(tpl, templateData);
          $(this.el).append(tpl);
          var tabs = $(this.el).children('.tabs');
          this.el.tabs("add", "#tabs-image-" + window.app.status.currentProject + "-" + this.model.get('id') + "-", this.model.get('filename'));
          this.el.css("display", "block");
          this.initToolbar();
          this.initMap();
          this.initOntology();
          this.initAnnotationsTabs();
          return this;
       },
       /**
        * Grab the layout and call ask for render
        */
       render : function() {
          var self = this;
          require([
             "text!application/templates/explorer/BrowseImage.tpl.html"
          ], function(tpl) {
             self.doLayout(tpl);
          });
          return this;
       },
       /**
        * Check init options and call appropriate methods
        */
       show : function(options) {
          var self = this;
          if (options.goToAnnotation != undefined) {
             _.each(this.layers, function(layer) {
                console.log("layer : " + layer);
                self.goToAnnotation(layer,  options.goToAnnotation.value);
             });
          }

       },
       refreshAnnotationTabs : function (idTerm) {
          this.annotationsPanel.refreshAnnotationTabs(idTerm);
       },
       /**
        * Move the OpenLayers view to the Annotation, at the
        * optimal zoom
        * @param layer The vector layer containing annotations
        * @param idAnnotation the annotation
        */
       goToAnnotation : function(layer, idAnnotation) {
          var self = this;
          var feature = layer.getFeature(idAnnotation);
          if (feature != undefined) {
             var bounds = feature.geometry.bounds;
             //Compute the ideal zoom to view the feature
             var featureWidth = bounds.right  - bounds.left;
             var featureHeight = bounds.top - bounds.bottom;
             var windowWidth = $(window).width();
             var windowHeight = $(window).height();
             var zoom = this.map.getNumZoomLevels()-1;
             var tmpWidth = featureWidth;
             var tmpHeight = featureHeight;
             while ((tmpWidth > windowWidth) || (tmpHeight > windowHeight)) {
                tmpWidth /= 2;
                tmpHeight /= 2;
                zoom--;
             }
             layer.controls.select.unselectAll();
             layer.controls.select.select(feature);
             self.currentAnnotation = idAnnotation;
             this.map.moveTo(new OpenLayers.LonLat(feature.geometry.getCentroid().x, feature.geometry.getCentroid().y), Math.max(0, zoom));
          }
       },
       getFeature : function (idAnnotation) {
          console.log("USER LAYER : >>>" + this.userLayer);
          return this.userLayer.getFeature(idAnnotation);
       },
       removeFeature : function (idAnnotation) {
          console.log("USER LAYER : >>>" + this.userLayer);
          return this.userLayer.removeFeature(idAnnotation);
       },
       /**
        * Callback used by AnnotationLayer at the end of theirs initializations
        * @param layer
        */
       layerLoadedCallback : function (layer) {
          if (_.isFunction(this.initCallback)) this.initCallback.call();
       },
       /**
        * Return the AnnotationLayer of the logged user
        */
       getUserLayer: function () {
          return this.userLayer;
       },
       /**
        * Initialize the OpenLayers Map
        */
       initMap : function () {
          var self = this;
          var mime = this.model.get('mime');
          if (mime == "jp2") self.initDjatoka();
          if (mime == "vms" || mime == "mrxs" || mime == "tif" || mime == "tiff") self.initIIP();
       },
       /**
        * Add a base layer (image) on the Map
        * @param layer the layer to add
        */
       addBaseLayer : function(layer) {
          var self = this;
          this.map.addLayer(layer);
          this.baseLayers.push(layer);
          self.map.setBaseLayer(layer);
          var radioName = "layerSwitch-" + this.model.get("id");
          var layerID = "layerSwitch-" + this.model.get("id") + "-" + new Date().getTime(); //index of the layer in this.layers array
          var liLayer = _.template("<li><input type='radio' id='{{id}}' name='{{radioName}}' checked/><label style='font-weight:normal;color:#FFF' for='{{id}}'> {{name}}</label></li>", {id : layerID, radioName:radioName, name : layer.name.substr(0,15)});
          $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").append(liLayer);
          $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").find("#"+layerID);
          $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").find("#"+layerID).click(function(){
             self.map.setBaseLayer(layer);
          });
       },
       /**
        * Add a vector layer on the Map
        * @param layer the layer to add
        * @param userID the id of the user associated to the layer
        */
       addVectorLayer : function(layer, userID) {
          this.map.addLayer(layer.vectorsLayer);
          this.layers.push(layer);
          var layerID = "layerSwitch" + (this.layers.length - 1); //index of the layer in this.layers array
          console.log("layer ID : " + layerID);
          var color = window.app.models.users.get(userID).get('color');
          var liLayer = _.template("<li><input type='checkbox' id='{{id}}' name='annotationLayerRadio' style='display:inline;' checked /><label style='display:inline;font-weight:normal;color:#FFF;padding:5px;' for='{{id}}'>{{name}}</label></li>", {id : layerID, name : layer.vectorsLayer.name, color : color});
          $("#layerSwitcher"+this.model.get("id")).find(".annotationLayers").append(liLayer);
          $("#layerSwitcher"+this.model.get("id")).find(".annotationLayers").find("#"+layerID).click(function(){
             var checked = $(this).attr("checked");
             layer.vectorsLayer.setVisibility(checked);
          });
       },
       /**
        * Create a draggable Panel containing Layers names
        * and tools
        */
       createLayerSwitcher : function() {
          new LayerSwitcherPanel({
                 model : this.model,
                 el : this.el
              }).render();
       },
       /**
        * Create a draggable Panel containing a OverviewMap
        */
       createOverviewMap : function() {
          new OverviewMapPanel({
                 model : this.model,
                 el : this.el
              }).render();
       },
       /**
        * Init the Map if ImageServer is IIPImage
        */
       initIIP : function () {
          console.log("initIIP");
          var self = this;
          var parseIIPMetadaResponse = function(response) {
             var metadata = null;
             var respArray = response.split("\n");
             _.each(respArray, function(it){
                var arg = it.split(":");
                if (arg[0] == "Max-size") {
                   var value = arg[1].split(" ");
                   var t_width  = value[0];
                   var t_height = value[1];
                   var nbZoom = 0;
                   while (t_width >= 256 || t_height >= 256) {
                      nbZoom++;
                      t_width = t_width / 2;
                      t_height = t_height / 2;
                   }
                   metadata = {width : value[0], height : value[1], nbZoom : nbZoom, overviewWidth : 200, overviewHeight : Math.round((200/value[0]*value[1]))};

                }

             });
             return metadata;
          }

          var initZoomifyLayer = function(metadata) {
             /* First we initialize the zoomify pyramid (to get number of tiers) */
             console.log("Init zoomify with (width, height) : " + metadata.width +","+ metadata.height);
             var baseURLs = self.model.get('imageServerBaseURL');
             console.log("baseURL : " + baseURLs.length);
             console.log("nbZoom " + metadata.nbZoom);
             var zoomify_url = []
             _.each(baseURLs, function(baseURL) {
                var url = baseURL + "/fcgi-bin/iipsrv.fcgi?zoomify=" + self.model.get('path') +"/";
                zoomify_url.push(url);
             });

             var baseLayer = new OpenLayers.Layer.Zoomify(
                 "Original",
                 zoomify_url,
                 new OpenLayers.Size( metadata.width, metadata.height )
                 , {transitionEffect: 'resize'}
             );
             var anotherLayer = new OpenLayers.Layer.Zoomify( "Otsu", "http://localhost:8080/cytomine-web/proxy/otsu?url="+zoomify_url,
                 new OpenLayers.Size( metadata.width, metadata.height ) );

             var layerSwitcher = self.createLayerSwitcher();

             //var numZoomLevels =  metadata.nbZoom;
             /* Map with raster coordinates (pixels) from Zoomify image */
             var options = {
                maxExtent: new OpenLayers.Bounds(0, 0, metadata.width, metadata.height),
                maxResolution: Math.pow(2,  metadata.nbZoom ),
                numZoomLevels:  metadata.nbZoom+1,
                units: 'pixels',
                controls: [
                   new OpenLayers.Control.TouchNavigation({
                          dragPanOptions: {
                             enableKinetic: true
                          }
                       }),
                   //new OpenLayers.Control.Navigation({zoomWheelEnabled : true, mouseWheelOptions: {interval: 1}, cumulative: false}),
                   new OpenLayers.Control.Navigation(),
                   new OpenLayers.Control.PanZoomBar(),
                   new OpenLayers.Control.MousePosition(),
                   new OpenLayers.Control.KeyboardDefaults()]
             };

             var overviewMap = new OpenLayers.Layer.Image(
                 "Overview"+self.model.get("id"),
                 self.model.get("thumb"),
                 new OpenLayers.Bounds(0, 0, metadata.width, metadata.height),
                 new OpenLayers.Size(metadata.overviewWidth, metadata.overviewHeight)
             );

             console.log("metadata.overviewHeight = " + metadata.overviewHeight);
             var overviewMapControl = new OpenLayers.Control.OverviewMap({
                    size: new OpenLayers.Size(metadata.overviewWidth, metadata.overviewHeight),
                    layers: [overviewMap],
                    div: document.getElementById('overviewMap' + self.model.get('id')),
                    minRatio: 1,
                    maxRatio: 1024,
                    mapOptions : {
                       maxExtent: new OpenLayers.Bounds(0, 0, metadata.width, metadata.height),
                       maximized: true
                    }
                 });

             self.map = new OpenLayers.Map("map" + self.model.get('id'), options);
             self.addBaseLayer(anotherLayer);
             self.addBaseLayer(baseLayer);
             self.createOverviewMap();
             self.map.zoomToMaxExtent();
             self.map.addControl(overviewMapControl);

          }

          $.ajax({
                 async: false,
                 processData : false,
                 dataType : 'text',
                 url: this.model.get('metadataUrl'),
                 success: function(response){
                    var metadata = parseIIPMetadaResponse(response);
                    initZoomifyLayer(metadata);
                 },
                 error: function(){
                    console.log("error");
                 }
              });
       },
       /**
        * Init the Map if ImageServer is Adore Djatoka
        */
       reloadAnnotation : function(idAnnotation) {
          var self = this;
          self.removeFeature(idAnnotation);
          new AnnotationModel({id:idAnnotation}).fetch({
                 success: function(annotation, response) {
                    var feature = self.userLayer.createFeatureFromAnnotation(annotation);
                    self.userLayer.addFeature(feature);
                    self.userLayer.selectFeature(feature);
                 }
              });
       },
       initDjatoka: function () {
          console.log("initDjatoka");
          var self = this;
          var baseLayer = new OpenLayers.Layer.OpenURL(this.model.get('filename'), this.model.get('imageServerBaseURL'), {
                 transitionEffect: 'resize',
                 layername: 'basic',
                 format: 'image/jpeg',
                 rft_id: this.model.get('path'),
                 metadataUrl: this.model.get('metadataUrl')
              });


          var metadata = baseLayer.getImageMetadata();
          var resolutions = baseLayer.getResolutions();
          var maxExtent = new OpenLayers.Bounds(0, 0, metadata.width, metadata.height);
          var tileSize = baseLayer.getTileSize();
          var lon = metadata.width / 2;
          var lat = metadata.height / 2;
          var mapOptions = {
             maxExtent: maxExtent,
             maximized: true
          };



          var layerSwitcher = this.createLayerSwitcher();


          var options = {
             resolutions: resolutions,
             maxExtent: maxExtent,
             tileSize: tileSize,
             controls: [
                //new OpenLayers.Control.Navigation({zoomWheelEnabled : true, mouseWheelOptions: {interval: 1}, cumulative: false}),
                new OpenLayers.Control.Navigation(), new OpenLayers.Control.PanZoomBar(), new OpenLayers.Control.MousePosition(),
                new OpenLayers.Control.OverviewMap({
                       div: document.getElementById('overviewMap' + this.model.get('id')),
                       //size: new OpenLayers.Size(metadata.width / Math.pow(2, openURLLayer.getViewerLevel()), metadata.height / Math.pow(2,(openURLLayer.getViewerLevel()))),
                       size: new OpenLayers.Size(metadata.width / Math.pow(2, baseLayer.getViewerLevel()), metadata.height / Math.pow(2, (baseLayer.getViewerLevel()))),
                       minRatio: 1,
                       maxRatio: 1024,
                       mapOptions: mapOptions
                    }), new OpenLayers.Control.KeyboardDefaults()]
          };



          this.map = new OpenLayers.Map("map" + this.model.get('id'), options);
          console.log("MAP CREATED + " + this.map);
          this.addBaseLayer(baseLayer);
          this.map.setCenter(new OpenLayers.LonLat(lon, lat), 2);
          self.createOverviewMap();
       },
       initAutoAnnoteTools : function () {

          var self = this;

          var handleMapClick = function handleMapClick(evt) {

             if (!self.getUserLayer().magicOnClick) return;
             // don't select the point, select a square around the click-point
             var clickbuffer = 18; //how many pixels around should it look?
             var sw = self.map.getLonLatFromViewPortPx(new
                 OpenLayers.Pixel(evt.xy.x-clickbuffer , evt.xy.y+clickbuffer) );
             var ne = self.map.getLonLatFromViewPortPx(new
                 OpenLayers.Pixel(evt.xy.x+clickbuffer , evt.xy.y-clickbuffer) );

             // open a popup window, supplying the coords in GET
             var url =  sw.lon+','+sw.lat+','+ne.lon+','+ne.lat;
             alert(url);
          }
          this.map.events.register('click', this.map, handleMapClick);


       },
       /**
        * Init the toolbar
        */
       initToolbar: function () {
          var toolbar = $('#toolbar' + this.model.get('id'));
          var self = this;
          toolbar.find('input[name=select]').button({
                 //text : false,
                 // icons: {
                 // primary: "ui-icon-seek-start"
                 // }
              });
          toolbar.find('button[name=delete]').button({
                 text: false,
                 icons: {
                    primary: "ui-icon-trash"

                 }
              });

          toolbar.find('button[name=ruler]').button({
                 text: false,
                 icons: {
                    secondary: "ui-icon-arrow-2-ne-sw"

                 }
              });
          toolbar.find('input[id^=ruler]').button({
                 text: true,
                 icons: {
                    secondary: "ui-icon-arrow-2-ne-sw"

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
          toolbar.find('span[class=ruler-toolbar]').buttonset();

          toolbar.find('input[id=none' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().toggleControl("none");
             self.getUserLayer().enableHightlight();
          });
          toolbar.find('input[id=select' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().toggleControl("select");
             self.getUserLayer().disableHightlight();
          });
          /*toolbar.find('input[id=freehand' + this.model.get('id') + ']').click(function () {
           self.getUserLayer().controls.select.unselectAll();
           self.getUserLayer().toggleControl("freehand");
           self.getUserLayer().disableHightlight();
           });*/
          toolbar.find('input[id=regular4' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().setSides(4);
             self.getUserLayer().toggleControl("regular");
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=regular30' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().setSides(15);
             self.getUserLayer().toggleControl("regular");
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=polygon' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().toggleControl("polygon");
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=magic' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().toggleControl("select");
             self.getUserLayer().magicOnClick = true;
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=modify' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().toggleEdit();
             self.getUserLayer().toggleControl("modify");
             self.getUserLayer().disableHightlight();
          });
          toolbar.find('input[id=delete' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
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
          toolbar.find('input[id=ruler' + this.model.get('id') + ']').click(function () {
             self.getUserLayer().controls.select.unselectAll();
             self.getUserLayer().toggleControl("line");
             self.getUserLayer().measureOnSelect = true;
             self.getUserLayer().disableHightlight();
          });
          /*toolbar.find('input[id=irregular' + this.model.get('id') + ']').click(function () {
           self.getUserLayer().toggleIrregular();
           });
           toolbar.find('input[id=irregular' + this.model.get('id') + ']').hide();*/

       },
       /**
        * Collect data and call appropriate methods in order to add Vector Layers on the Map
        */
       initVectorLayers: function (ontologyTreeView) {
          var self = this;
          window.app.models.users.fetch({
                 success: function () {
                    window.app.models.users.each(function (user) {
                       var layerAnnotation = new AnnotationLayer(user.prettyName(), self.model.get('id'), user.get('id'), user.get('color'), ontologyTreeView, self, self.map );
                       layerAnnotation.loadAnnotations(self);
                       var isOwner = user.get('id') == window.app.status.user.id;
                       if (isOwner) {
                          self.userLayer = layerAnnotation;
                          layerAnnotation.initControls(self, isOwner);
                          layerAnnotation.registerEvents(self.map);
                          self.userLayer.toggleIrregular();
                          //simulate click on Navigate button
                          var toolbar = $('#toolbar' + self.model.get('id'));
                          toolbar.find('input[id=none' + self.model.get('id') + ']').click();
                       } else {
                          layerAnnotation.initControls(self, isOwner);
                          layerAnnotation.registerEvents(self.map);
                          layerAnnotation.controls.select.activate();
                       }

                    });
                 }
              });
       },

       initAnnotationsTabs : function(){
          this.annotationsPanel = new AnnotationsPanel({
                 el : this.el,
                 model : this.model
              }).render();

       },
       /**
        * Create a draggable Panel containing a tree which represents the Ontology
        * associated to the Image
        */
       initOntology: function () {
          var self = this;
          new OntologyPanel({
                 el : this.el,
                 model : this.model,
                 callback : self.initVectorLayers,
                 browseImageView : self
              }).render();

       },
       /**
        * Bind controls to the map
        * @param controls the controls we want to bind
        */
       initTools: function (controls) {
          for (var key in controls) {
             this.map.addControl(controls[key]);
          }
       }
    });



