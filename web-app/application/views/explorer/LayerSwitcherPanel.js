/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 12/06/11
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */

var LayerSwitcherPanel = Backbone.View.extend({
   tagName : "div",

   /**
    * ExplorerTabs constructor
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
      require([
         "text!application/templates/explorer/LayerSwitcher.tpl.html"
      ], function(tpl) {
         self.doLayout(tpl);
      });
      return this;
   },
   addBaseLayer : function (layer, model) {
      var self = this;
      var radioName = "layerSwitch-" + model.get("id");
      var layerID = "layerSwitch-" + model.get("id") + "-" + new Date().getTime(); //index of the layer in this.layers array
      var liLayer = _.template("<li><input type='radio' id='{{id}}' name='{{radioName}}' checked/><span style='color : #ffffff;'> {{name}}</span></li>", {id : layerID, radioName:radioName, name : layer.name.substr(0,15)});
      $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").append(liLayer);
      $("#layerSwitcher"+this.model.get("id")).find(".baseLayers").find("#"+layerID);
      $("#"+layerID).change(function(){
         self.browseImageView.map.setBaseLayer(layer);
      });
   },
   addVectorLayer : function (layer, model, userID) {
      var layerID = window.app.models.users.get(userID).prettyName();
      var layerID = "layerSwitch-" + model.get("id") + "-" + userID + "-"  + new Date().getTime(); //index of the layer in this.layers array
      var color = window.app.models.users.get(userID).get('color');
      var layerOptionTpl;
      if (layer.isOwner) {
         layerOptionTpl = _.template("<li><input id='{{id}}' type='checkbox' value='{{name}}' checked /><span style='color : #ffffff;'> {{name}}</span></li>", {id : layerID, name : layer.vectorsLayer.name, color : color});
      } else {
         layerOptionTpl = _.template("<li><input id='{{id}}' type='checkbox' value='{{name}}' /> <span style='color : #ffffff;'>{{name}}</span></li>", {id : layerID, name : layer.vectorsLayer.name, color : color});
      }
      $("#layerSwitcher"+model.get("id")).find("ul.annotationLayers").append(layerOptionTpl);

      $("#"+layerID).click(function(){
         var checked = $(this).attr("checked");
         layer.vectorsLayer.setVisibility(checked);
         if (checked) {
            _.each(layer.vectorsLayer.features, function (feature) {
               feature.style.display = '';
               layer.vectorsLayer.drawFeature(feature);
            });
         }
      });

   },
   /**
    * Render the html into the DOM element associated to the view
    * @param tpl
    */
   doLayout: function(tpl) {
      var self = this;
      var content = _.template(tpl, {id : self.model.get("id")});
      $("#layerSwitcher"+self.model.get("id")).html(content);

      $("#layerSwitcher"+self.model.get("id")).find(".toggleShowBaseLayers").click(function () {
         $("#layerSwitcher"+self.model.get("id")).find("ul.baseLayers").toggle(300);
         return false;
      });

      $("#layerSwitcher"+self.model.get("id")).find(".toggleShowVectorLayers").click(function () {
         $("#layerSwitcher"+self.model.get("id")).find("ul.annotationLayers").toggle(300);
         return false;
      });


      new DraggablePanelView({
         el : $('#layerSwitcher' + self.model.get('id'))
      }).render();
      /*$("#layers-slider-"+self.model.id).slider({
       value: 100,
       min : 0,
       max : 100,
       slide: function(e, ui) {

       _.each(self.browseImageView.layers, function(layer) {

       layer.vectorsLayer.setOpacity(ui.value / 100);
       });

       }
       });*/
      /*$("#image-slider-"+self.model.id).slider({
       value: 100,
       min : 0,
       max : 100,
       slide: function(e, ui) {
       self.map.baseLayer.setOpacity(ui.value / 100);
       }
       });*/
   }
});
