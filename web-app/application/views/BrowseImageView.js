var BrowseImageView = Backbone.View.extend({
       tagName: "div",
       layers: [],
       baseLayer : null,
       map : null,
       initOptions : null,
       /**
        * BrowseImageView constructor
        * Accept options used for initialization
        * @param options
        */
       initialize: function (options) {
          this.initOptions = options.initOptions;
       },
       /**
        * Render the html into the DOM element associated to the view
        * @param tpl
        */
       doLayout: function (tpl) {
          var tpl = _.template(tpl, this.model.toJSON());
          $(this.el).append(tpl);
          var tabs = $(this.el).children('.tabs');
          this.el.tabs("add", "#tabs-" + this.model.get('id'), this.model.get('filename'));
          this.el.css("display", "block");
          this.initToolbar();
          this.initMap();
          this.initOntology();
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
       },
       /**
        * Check init options and call appropriate methods
        */
       show : function() {
          var self = this;
          if (this.initOptions.goToAnnotation != undefined) {
             _.each(this.layers, function(layer) {
                self.goToAnnotation(layer,  self.initOptions.goToAnnotation.value);
             });
          }

       },
       /**
        * Move the OpenLayers view to the Annotation, at the
        * optimal zoom
        * @param layer The vector layer containing annotations
        * @param idAnnotation the annotation
        */
       goToAnnotation : function(layer, idAnnotation) {
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
             this.map.moveTo(new OpenLayers.LonLat(feature.geometry.getCentroid().x, feature.geometry.getCentroid().y), Math.max(0, zoom));
          }
       },
       /**
        * Callback used by AnnotationLayer at the end of theirs initializations
        * @param layer
        */
       layerLoadedCallback : function (layer) {
          if (this.initOptions.goToAnnotation != undefined) {
             this.goToAnnotation(layer, this.initOptions.goToAnnotation.value);
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
          if (mime == "jp2") self.initDjatoka();
          if (mime == "vms" || mime == "mrxs") self.initIIP();
       },
       /**
        * Add a base layer (image) on the Map
        * @param layer the layer to add
        */
       addBaseLayer : function(layer) {
          this.map.addLayer(layer);
          this.map.setBaseLayer(this.baseLayer);
          var layerID = "layerSwitch" + (this.layers.length - 1); //index of the layer in this.layers array
          var liLayer = _.template("<li><input type='radio' id='{{id}}' name='baseLayerRadio' checked /><label style='font-weight:bold;color:#FFF' for='{{id}}'>{{name}}</label></li>", {id : layerID, name : layer.name.substr(0,15)+"..."});
          $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").append(liLayer);
          $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").find("#"+layerID).click(function(){
             console.log(">>"+layer.name);
          });

       },
       /**
        * Add a vector layer on the Map
        * @param layer the layer to add
        * @param userID the id of the user associated to the layer
        */
       addVectorLayer : function(layer, userID) {
          this.map.addLayer(layer);
          this.layers.push(layer);
          var layerID = "layerSwitch" + (this.layers.length - 1); //index of the layer in this.layers array
          console.log("layer ID : " + layerID);
          var color = window.app.models.users.get(userID).get('color');
          var liLayer = _.template("<li><input type='checkbox' id='{{id}}' name='annotationLayerRadio' style='display:inline;' checked /><label style='display:inline;font-weight:bold;color:#FFF;padding:5px;' for='{{id}}'>{{name}}</label><span style='display:inline;margin:3px;width:10px;height:10px;background-color:{{color}};'>&nbsp;&nbsp;&nbsp;&nbsp;</span></li>", {id : layerID, name : layer.name, color : color});
          $("#layerSwitcher"+this.model.get("id")).find(".annotationLayers").append(liLayer);
          $("#layerSwitcher"+this.model.get("id")).find(".annotationLayers").find("#"+layerID).click(function(){
             var checked = $(this).attr("checked");
             layer.setVisibility(checked);
          });
       },
       /**
        * Create a draggable Panel containing Layers names
        * and tools
        */
       createLayerSwitcher : function() {
          var self = this;
          require([
             "text!application/templates/explorer/LayerSwitcher.tpl.html"
          ], function(tpl) {
             var content = _.template(tpl, {id : self.model.get("id")});
             $("#layerSwitcher"+self.model.get("id")).html(content);
             new DraggablePanelView({
                    el : $('#layerSwitcher' + self.model.get('id'))
                 }).render();
          });
       },
       /**
        * Create a draggable Panel containing a OverviewMap
        */
       createOverviewMap : function() {
          var self = this;
          require([
             "text!application/templates/explorer/OverviewMap.tpl.html"
          ], function(tpl) {
             new DraggablePanelView({
                    el : $('#overviewMap' + self.model.get('id')),
                    template : _.template(tpl, {id : self.model.get('id')})
                 }).render();
          });

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
                   metadata = {width : value[0], height : value[1], nbZoom : nbZoom};

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
             var zoomify_url = baseURLs[0] + "/fcgi-bin/iipsrv.fcgi?zoomify=" + self.model.get('path') +"/";
             self.baseLayer = new OpenLayers.Layer.Zoomify( self.model.get('filename'), zoomify_url,
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
                   //new OpenLayers.Control.Navigation({zoomWheelEnabled : true, mouseWheelOptions: {interval: 1}, cumulative: false}),
                   new OpenLayers.Control.Navigation(),
                   new OpenLayers.Control.PanZoomBar(),
                   new OpenLayers.Control.MousePosition(),

                   /*new OpenLayers.Control.OverviewMap({
                    bounds : new OpenLayers.Bounds(0, 0, metadata.width, metadata.height),
                    size: new OpenLayers.Size(metadata.width / Math.pow(2, numZoomLevels), metadata.height / Math.pow(2, numZoomLevels)),
                    div: document.getElementById('overviewMap' + self.model.get('id'))
                    }),*/
                   new OpenLayers.Control.KeyboardDefaults()]
             };

             self.map = new OpenLayers.Map("map" + self.model.get('id'), options);
             self.addBaseLayer(self.baseLayer);
             self.map.zoomToMaxExtent();

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
       initDjatoka: function () {
          console.log("initDjatoka");
          var self = this;
          this.baseLayer = new OpenLayers.Layer.OpenURL(this.model.get('filename'), this.model.get('imageServerBaseURL'), {
                 transitionEffect: 'resize',
                 layername: 'basic',
                 format: 'image/jpeg',
                 rft_id: this.model.get('path'),
                 metadataUrl: this.model.get('metadataUrl')
              });


          var metadata = this.baseLayer.getImageMetadata();
          var resolutions = this.baseLayer.getResolutions();
          var maxExtent = new OpenLayers.Bounds(0, 0, metadata.width, metadata.height);
          var tileSize = this.baseLayer.getTileSize();
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
                       size: new OpenLayers.Size(metadata.width / Math.pow(2, this.baseLayer.getViewerLevel()), metadata.height / Math.pow(2, (this.baseLayer.getViewerLevel()))),
                       minRatio: 1,
                       maxRatio: 1024,
                       mapOptions: mapOptions
                    }), new OpenLayers.Control.KeyboardDefaults()]
          };



          this.map = new OpenLayers.Map("map" + this.model.get('id'), options);
          console.log("MAP CREATED + " + this.map);
          this.addBaseLayer(this.baseLayer);
          this.map.setCenter(new OpenLayers.LonLat(lon, lat), 2);
          self.createOverviewMap();
       },
       /**
        * Init the toolbar
        */
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

          /*toolbar.find('button[name=ruler]').button({
           text: false,
           icons: {
           secondary: "ui-icon-arrow-2-ne-sw"

           }
           });  */
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
       initVectorLayers: function () {
          var self = this;
          window.app.models.users.fetch({
                 success: function () {
                    window.app.models.users.each(function (user) {
                       var layerAnnotation = new AnnotationLayer(user.get('firstname'), self.model.get('id'), user.get('id'), user.get('color'), self.ontologyTreeView, self.map );
                       layerAnnotation.loadAnnotations(self);

                       //layerAnnotation.initHightlight(self.map);
                       var isOwner = user.get('id') == window.app.status.user.id;

                       if (isOwner) {
                          console.log("user.get('id')="+user.get('id'));
                          self.userLayer = layerAnnotation;
                          layerAnnotation.initControls(self, isOwner);
                          layerAnnotation.registerEvents(self.map);
                          self.userLayer.toggleIrregular();
                          //simulate click on Navigate button
                          var toolbar = $('#toolbar' + self.model.get('id'));
                          toolbar.find('input[id=none' + self.model.get('id') + ']').click();
                       } else {
                          /*layerAnnotation.initControls(self, isOwner);
                           layerAnnotation.registerEvents(self.map);
                           layerAnnotation.controls.select.activate();  */
                       }

                    });

                 }
              });
       },
       /**
        * Create a draggable Panel containing a tree which represents the Ontology
        * associated to the Image
        */
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

                    require([
                       "text!application/templates/explorer/OntologyTree.tpl.html"
                    ], function(tpl) {
                       new DraggablePanelView({
                              el : $('#ontologyTree' + self.model.get('id')),
                              template : _.template(tpl, {id : self.model.get('id')})
                           }).render();
                    });
                 }
              });



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



