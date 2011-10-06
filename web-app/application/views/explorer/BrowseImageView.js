var BrowseImageView = Backbone.View.extend({
   tagName: "div",
   /**
    * BrowseImageView constructor
    * Accept options used for initialization
    * @param options
    */
   initialize: function (options) {
      this.iPad = ( navigator.userAgent.match(/iPad/i) != null );
      this.initCallback = options.initCallback;
      this.layers = [];
      this.layersLoaded = 0;
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
      if (this.iPad) this.initMobile();
      return this;
   },
   initMobile : function () {


   },
   /**
    * Grab the layout and call ask for render
    */
   render : function() {
      var self = this;
      //var template = (this.iPad) ? "text!application/templates/explorer/BrowseImageMobile.tpl.html" : "text!application/templates/explorer/BrowseImage.tpl.html";

      require(["lib/OpenLayers-2.11/OpenLayers.js", "text!application/templates/explorer/BrowseImage.tpl.html"
      ], function(openLayers,  tpl) {
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

            self.goToAnnotation(layer,  options.goToAnnotation.value);
         });
      }

   },
   refreshAnnotationTabs : function (idTerm) {
      this.annotationsPanel.refreshAnnotationTabs(idTerm);
   },
   setLayerVisibility : function(layer, visibility) {
      // manually check (or uncheck) the third checkbox in the menu:
      $("#layerSwitcher"+this.model.get("id")).find("ul.annotationLayers").find(":checkbox").each(function(){
         if (layer.name != $(this).attr("value")) return;
         if (visibility) {
            if ($(this).attr("checked") != "checked") {
               this.click();
            }
         } else {
            if ($(this).attr("checked") == "checked") {
               this.click();
            }
         }
      });
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
         layer.showFeature(feature);
         self.setLayerVisibility(layer, true);
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
         //Simulate click on select button
         var toolbar = $('#toolbar' + this.model.get('id'));
         toolbar.find('input[id=select' + self.model.get('id') + ']').click();
         layer.controls.select.unselectAll();
         layer.controls.select.select(feature);
         self.currentAnnotation = idAnnotation;
         this.map.moveTo(new OpenLayers.LonLat(feature.geometry.getCentroid().x, feature.geometry.getCentroid().y), Math.max(0, zoom));
      }
   },
   getFeature : function (idAnnotation) {
      return this.userLayer.getFeature(idAnnotation);
   },
   removeFeature : function (idAnnotation) {
      return this.userLayer.removeFeature(idAnnotation);
   },
   /**
    * Callback used by AnnotationLayer at the end of theirs initializations
    * @param layer
    */
   layerLoadedCallback : function (layer) {
      var self = this;

      this.layersLoaded++;
      var project = window.app.status.currentProjectModel;
      if (this.layersLoaded == project.get("users").length) {
         //Init MultiSelected in LayerSwitcher
         /*$("#layerSwitcher"+this.model.get("id")).find("select").multiselect({
            click: function(event, ui){
               _.each(self.layers, function(layer){
                  if (layer.name != ui.value) return;
                  if (ui.checked) {
                     _.each(layer.vectorsLayer.features, function (feature) {
                        if (feature.style != undefined && feature.style.display != 'none') return;
                        layer.showFeature(feature);
                     });
                  }
                  layer.vectorsLayer.setVisibility(ui.checked);
               });
            },
            checkAll: function(){
               _.each(self.layers, function(layer){
                  layer.vectorsLayer.setVisibility(true);
                  _.each(layer.vectorsLayer.features, function (feature) {
                     if (feature.style != undefined && feature.style.display != 'none') return;
                     layer.showFeature(feature);
                  });
               });
            },
            uncheckAll: function(){
               _.each(self.layers, function(layer){
                  layer.vectorsLayer.setVisibility(false);
               });
            }
         });*/

         //Init Controls on Layers
         var vectorLayers = _.map(this.layers, function(layer){ return layer.vectorsLayer;});
         var selectFeature = new OpenLayers.Control.SelectFeature(vectorLayers);
         _.each(this.layers, function(layer){
            layer.initControls(self, selectFeature);
            layer.registerEvents(self.map);
            if (layer.isOwner) {
               self.userLayer = layer;
               layer.vectorsLayer.setVisibility(true);
               layer.toggleIrregular();
               //Simulate click on None toolbar
               var toolbar = $('#toolbar' + self.model.get('id'));
               toolbar.find('input[id=none' + self.model.get('id') + ']').click();
            } else {
               layer.controls.select.activate();
               layer.vectorsLayer.setVisibility(false);
            }
         });

         if (_.isFunction(self.initCallback)) self.initCallback.call();

         self.initAutoAnnoteTools();
      }

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
      //if (mime == "jp2") self.initDjatoka();
      if (mime == "vms" || mime == "mrxs" || mime == "tif" || mime == "tiff" || mime == "svs") self.initIIP();
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
      this.layerSwitcherPanel.addBaseLayer(layer, this.model);
   },

   /**
    * Add a vector layer on the Map
    * @param layer the layer to add
    * @param userID the id of the user associated to the layer
    */
   addVectorLayer : function(layer, userID) {
      this.map.addLayer(layer.vectorsLayer);
      this.layers.push(layer);
      this.layerSwitcherPanel.addVectorLayer(layer, this.model, userID);
   },
   /**
    * Create a draggable Panel containing Layers names
    * and tools
    */
   createLayerSwitcher : function() {
      this.layerSwitcherPanel = new LayerSwitcherPanel({
         browseImageView : this,
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

      var self = this;
      var initZoomifyLayer = function(metadata, zoomify_urls) {
         _.each(zoomify_urls, function (url) {
            console.log("URL > " + url);
         });


         var baseLayer = new OpenLayers.Layer.Zoomify(
             "Original",
             zoomify_urls,
             new OpenLayers.Size( metadata.width, metadata.height )
         );
         //baseLayer.transitionEffect = 'resize';

         var haematoxylinURLS = _.map(zoomify_urls, function (url){ return "proxy/haematoxylin?url="+url;});
         var haematoxylinLayer = new OpenLayers.Layer.Zoomify( "Haematoxylin", haematoxylinURLS, new OpenLayers.Size( metadata.width, metadata.height ) );
         var eosinURLS = _.map(zoomify_urls, function (url){ return "proxy/eosin?url="+url;});
         var eosinLayer = new OpenLayers.Layer.Zoomify( "Eosin", eosinURLS, new OpenLayers.Size( metadata.width, metadata.height ) );
         var binaryURLS = _.map(zoomify_urls, function (url){ return "proxy/binary?url="+url;});
         var binaryLayer = new OpenLayers.Layer.Zoomify( "Binary", binaryURLS, new OpenLayers.Size( metadata.width, metadata.height ) );
         //String [] methods={" "Li", "MaxEntropy","Mean", "MinError(I)", "Minimum", "Moments", "Otsu", "Percentile", "RenyiEntropy", "Shanbhag" , "Triangle", "Yen"};
         var thresholdLayersParameters = [
            { name : "Huang Threshold", baseUrl : "proxy/huang?url=" },
            { name : "Intermodes Threshold", baseUrl : "proxy/intermodes?url=" },
            { name : "IsoData Threshold", baseUrl : "proxy/isodata?url=" },
            { name : "Li Threshold", baseUrl : "proxy/li?url=" },
            { name : "MaxEntropy Threshold", baseUrl : "proxy/maxentropy?url=" },
            { name : "Mean Threshold", baseUrl : "proxy/mean?url=" },
            { name : "MinError(I) Threshold", baseUrl : "proxy/minerror?url=" },
            { name : "Minimum Threshold", baseUrl : "proxy/minimum?url=" },
            { name : "Moments Threshold", baseUrl : "proxy/moments?url=" },
            { name : "Otsu Threshold", baseUrl : "proxy/otsu?url=" },
            { name : "Percentile Threshold", baseUrl : "proxy/percentile?url=" },
            { name : "RenyiEntropy Threshold", baseUrl : "proxy/renyientropy?url=" },
            { name : "Shanbhag Threshold", baseUrl : "proxy/shanbhag?url=" },
            { name : "Triangle Threshold", baseUrl : "proxy/triangle?url=" },
            { name : "Yen Threshold", baseUrl : "proxy/yen?url=" }
         ]
         var thresholdLayers = []
         _.each(thresholdLayersParameters, function (parameters) {
            var url = _.map(zoomify_urls, function (url){ return parameters.baseUrl+url;});
            var layer =   new OpenLayers.Layer.Zoomify( parameters.name, url, new OpenLayers.Size( metadata.width, metadata.height ) );
            thresholdLayers.push(layer);
         });
         var layerSwitcher = self.createLayerSwitcher();

         //var numZoomLevels =  metadata.nbZoom;
         /* Map with raster coordinates (pixels) from Zoomify image */
         var options = {
            maxExtent: new OpenLayers.Bounds(0, 0, metadata.width, metadata.height),
            maxResolution: Math.pow(2,  metadata.nbZoom ),
            numZoomLevels:  metadata.nbZoom+1,
            units: 'pixels',
            tileSize: new OpenLayers.Size(256,256),
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
               new OpenLayers.Control.KeyboardDefaults()],
            eventListeners: {
               //"moveend": mapEvent,
               "zoomend": function (event) {
                  var map = event.object;
                  var maxMagnification = self.model.get("magnification");
                  var deltaZoom = map.getNumZoomLevels() - map.getZoom() - 1;
                  var magnification = maxMagnification;
                  if (deltaZoom != 0)
                     magnification = maxMagnification / (Math.pow(2,deltaZoom));
                  magnification = Math.round(magnification * 100) / 100;
                  $("#zoomInfoPanel"+self.model.id).html(magnification + "X");
               }
               /*"changelayer": mapLayerChanged,
                "changebaselayer": mapBaseLayerChanged*/
            }
         };

         var overviewMap = new OpenLayers.Layer.Image(
             "Overview"+self.model.get("id"),
             self.model.get("thumb"),
             new OpenLayers.Bounds(0, 0, metadata.width, metadata.height),
             new OpenLayers.Size(metadata.overviewWidth, metadata.overviewHeight)
         );


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

         //Set the height of the map manually
         var paddingTop = 71;
         var height = $(window).height() - paddingTop;
         $("#map"+self.model.get('id')).css("height",height);
         $(window).resize(function() {
            var height = $(window).height() - paddingTop;
            $("#map"+self.model.get('id')).css("height",height);
         });
         self.addBaseLayer(binaryLayer);
         _.each (thresholdLayers , function  (layer) {
            self.addBaseLayer(layer);
         });
         self.addBaseLayer(haematoxylinLayer);
         self.addBaseLayer(eosinLayer);
         self.addBaseLayer(baseLayer);
         self.createOverviewMap();
         self.map.zoomToMaxExtent();
         self.map.addControl(overviewMapControl);

         //Compute the ideal initial zoom
         var windowWidth = $(window).width();
         var windowHeight = $(window).height() - paddingTop;
         var imageWidth = metadata.width;
         var imageHeight = metadata.height;
         var idealZoom = metadata.nbZoom;
         while (imageWidth > windowWidth || imageHeight > windowHeight) {
            imageWidth /= 2;
            imageHeight /= 2;
            idealZoom--;
         }
         self.map.zoomTo(idealZoom);
      }

      var t_width  = self.model.get("width");
      var t_height = self.model.get("height");
      var nbZoom = 0;
      while (t_width >= 256 || t_height >= 256) {
         nbZoom++;
         t_width = t_width / 2;
         t_height = t_height / 2;
      }
      var metadata = {width : self.model.get("width"), height : self.model.get("height"), nbZoom : nbZoom, overviewWidth : 200, overviewHeight : Math.round((200/self.model.get("width")*self.model.get("height")))};
      new ImageServerUrlsModel({id : self.model.get('baseImage')}).fetch({
         success : function (model, response) {
            initZoomifyLayer(metadata, model.get('imageServersURLs'));
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

   initAutoAnnoteTools : function () {

      var self = this;

      var handleMapClick = function handleMapClick(evt) {
         if (!self.getUserLayer().magicOnClick) return;
         var lonlat = self.map.getLonLatFromViewPortPx(evt.xy);

         var y = parseInt(self.model.get("height")) - lonlat.lat;
         var x = lonlat.lon;
         console.log(self.model.get("height"));
         var url = "processing/detect/"+self.model.get("id")+"/"+x+"/"+y;
         //alert(url);
         $.getJSON(url,
             function (response) {
                self.getUserLayer().addAnnotation(response.geometry);
             }
         );

      }
      self.map.events.register("click", self.getUserLayer().vectorsLayer, handleMapClick);
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
      toolbar.find('input[id=freehand' + this.model.get('id') + ']').click(function () {
         self.getUserLayer().controls.select.unselectAll();
         self.getUserLayer().toggleControl("freehand");
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
      var project = window.app.status.currentProjectModel;
      var projectUsers = window.app.models.users.select(function(user){
         return _.include(project.get("users"), user.id);
      });
      _.each(projectUsers, function (user) {
         var layerAnnotation = new AnnotationLayer(user.prettyName(), self.model.get('id'), user.get('id'), user.get('color'), ontologyTreeView, self, self.map );
         layerAnnotation.isOwner = (user.get('id') == window.app.status.user.id);
         layerAnnotation.loadAnnotations(self);
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



