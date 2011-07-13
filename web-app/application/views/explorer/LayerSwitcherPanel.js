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

       /**
        * Render the html into the DOM element associated to the view
        * @param tpl
        */
       doLayout: function(tpl) {
          var self = this;
          var content = _.template(tpl, {id : self.model.get("id")});
          $("#layerSwitcher"+self.model.get("id")).html(content);
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
