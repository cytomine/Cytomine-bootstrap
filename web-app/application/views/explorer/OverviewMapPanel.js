/**
 * Created by IntelliJ IDEA.
 * User: stevben
 * Date: 12/06/11
 * Time: 12:33
 * To change this template use File | Settings | File Templates.
 */



var OverviewMapPanel = Backbone.View.extend({
       tagName : "div",

       /**
        * ExplorerTabs constructor
        * @param options
        */
       initialize: function(options) {

       },
       /**
        * Grab the layout and call ask for render
        */
       render : function() {
          var self = this;
          require([
             "text!application/templates/explorer/OverviewMap.tpl.html"
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
          new DraggablePanelView({
                 el : $('#overviewMap' + self.model.get('id')),
                 className : "overviewPanel",
                 template : _.template(tpl, {id : self.model.get('id'), isDesktop : !window.app.view.isMobile})
              }).render();
       }
    });
