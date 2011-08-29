
var ImageFiltersPanel = Backbone.View.extend({
   tagName : "div",
   filter : null,
   /**
    * Constructor
    * @param options
    */
   initialize: function(options) {
      this.browseImageView = options.browseImageView;

       BrightnessFilter = OpenLayers.Class(OpenLayers.Tile.CanvasFilter, {
            brightness: -50,
            contrast:0,
            webworkerScript: "lib/openlayers/canvas/openlayers/example/canvasfilter-webworker.js",
            numberOfWebWorkers: 1, // per tile*/

            process: function(image) {
               console.log("filter...");
                var options = {
                    brightness: this.brightness,
                    contrast: this.contrast
                };

                // directly calling 'Pixastic.applyAction' instead of 'Pixastic.process'
                // works better in Chromium when using a proxy
                Pixastic.applyAction(image, image, "brightness", options);

                if (!options.resultCanvas) {
                    // if something went wrong, return the original image
                    return image;
                } else {
                    return options.resultCanvas;
                }
            },

            supportsAsync: function() {
                return true;
            },

            getParameters: function() {
                // these parameters are passed to the web worker script
                return {
                    brightness: this.brightness,
                    contrast: this.contrast
                };
            },

            CLASS_NAME: "BrightnessFilter"
        });

        this.filter = new BrightnessFilter();
   },
   /**
    * Grab the layout and call ask for render
    */
   render : function() {
      var self = this;
      require(["https://raw.github.com/jseidelin/pixastic/master/pixastic.core.js", "https://github.com/jseidelin/pixastic/raw/master/actions/brightness.js",
         "text!application/templates/explorer/ImageFiltersPanel.tpl.html"
      ], function(pixastic, pixasticBrightness, tpl) {
         self.doLayout( tpl);
      });
      return this;
   },

   /**
    * Render the html into the DOM element associated to the view
    * @param tpl
    */
   doLayout: function(tpl) {
      var content = _.template(tpl, this.model.toJSON());
      $("#imageFiltersPanel" + this.model.get("id")).html(content);
      this.initBindings();
   },

   initBindings : function () {
      var self = this;
      var el = $("#imageFiltersPanel" + this.model.get("id"));
      el.find("input[name=brightness]").change(function () {self.redraw();});
      el.find("input[name=contrast]").change(function () {self.redraw();});
   },
   redraw : function() {
      console.log("redraw " + this.model.get("id"));
      var el = $("#imageFiltersPanel" + this.model.get("id"));
      var brightness = el.find("input[name=brightness]").attr("value");
      var contrast = el.find("input[name=contrast]").attr("value");
      console.log("brightness = " + brightness);
      console.log("contrast = " + contrast);
      this.filter.brightness = brightness;
      this.filter.contrast = contrast;
      var self = this;
      // refresh map
      _.each(this.browseImageView.baseLayers, function (layer) {
         if (layer.name != "Original") return;
         console.log("redraw layer " + layer.name);
         layer.canvasFilter = self.filter;
         layer.redraw();
   //      layer.redraw();
      });

   }

});
