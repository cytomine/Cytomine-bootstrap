var ImageFiltersPanel = Backbone.View.extend({
   tagName : "div",
   filter : null,
   enabled : false,
   parameters : [],
   browseImageView : null,
   /**
    * Constructor
    * @param options
    */
   initialize: function(options) {
      this.browseImageView = options.browseImageView;
   },
   /**
    * Grab the layout and call ask for render
    */
   render : function() {
      var self = this;
      require(["text!application/templates/explorer/ImageFiltersPanel.tpl.html"
      ], function(  tpl) {
         self.doLayout( tpl);
      });
      return this;
   },

   /**
    * Render the html into the DOM element associated to the view
    * @param tpl
    */
   doLayout: function(tpl) {
      var self = this;
      var el = $('#imageFiltersPanel' + this.model.get('id'));
      new DraggablePanelView({
         el : el,
         template : _.template(tpl, this.model.toJSON())
      }).render();
      el.find("#contrast"+this.model.get("id")).slider({
         min : 0,
         max : 256,
         value : 128,
         change: function(event, ui) { self.redraw(); }
      });
      el.find("#brightness"+this.model.get("id")).slider({
         min : 0,
         max : 256,
         value : 128,
         change: function(event, ui) {  self.redraw(); }
      });

      el.find("input[name=invert]").change(function () {self.redraw();});
      el.find("input[name=filterActive]").change(function () {
         self.enabled = ($(this).attr("checked") == "checked");
         self.redraw();
      });
      el.find("a[name=reset]").click(function () {
         el.find("#brightness"+self.model.get("id")).slider( "option", "value", 128 );
         el.find("#contrast"+self.model.get("id")).slider( "option", "value", 128 );
         el.find("input[name=invert]").removeAttr("checked");
         return false;
      });
   },

   updateGetURL : function () {
      var self = this;
      var getAdvancedURL = function (bounds) {
         bounds = this.adjustBounds(bounds);
         var res = this.map.getResolution();
         var x = Math.round((bounds.left - this.tileOrigin.lon) / (res * this.tileSize.w));
         var y = Math.round((this.tileOrigin.lat - bounds.top) / (res * this.tileSize.h));
         var z = this.map.getZoom();

         var tileIndex = x + y * this.tierSizeInTiles[z].w + this.tileCountUpToTier[z];
         var path = "TileGroup" + Math.floor( (tileIndex) / 256 ) +
             "/" + z + "-" + x + "-" + y + ".jpg";
         var url = this.url;

         var updatedUrl = _.map(url, function(url){
            var parametersSTR = "";
            _.each(self.parameters, function(parameter) {
               parametersSTR += parameter.key;
               parametersSTR += "=";
               parametersSTR += parameter.value;
               parametersSTR += "&";
            });
            var index = url.indexOf("method=");
            if (index == -1) { //Original layer
                url = "vision/process?method=none&url="+url;
            }
            return url.substring(0, index) + parametersSTR + url.substring(index, url.length);


         });
         if (OpenLayers.Util.isArray(updatedUrl)) {
            url = this.selectUrl(path, updatedUrl);
         }
         return url + path;

      };
      self.browseImageView.map.baseLayer.getURL = getAdvancedURL;
      self.browseImageView.map.baseLayer.redraw();

   },
   redraw : function() {
      if (!this.enabled) {
         this.parameters = [];
         this.updateGetURL();
         return;
      }
      var el = $("#imageFiltersPanel" + this.model.get("id"));
      var brightness = parseInt(el.find("#brightness"+this.model.get("id")).slider("value"));
      var contrast = parseInt(el.find("#contrast"+this.model.get("id")).slider("value"));
      var invert =(el.find("input[name=invert]").attr("checked") == "checked");
      this.parameters = [];
      this.parameters.push({ key : "brightness", value : brightness});
      this.parameters.push({ key : "contrast", value : contrast});
      if (invert) this.parameters.push({ key : "invert", value : true});
      this.updateGetURL();
   }
});
