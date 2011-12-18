var ExplorerTabs = Backbone.View.extend({
   tagName : "div",
   triggerRoute : true,
   /**
    * ExplorerTabs constructor
    * @param options
    */
   initialize: function(options) {
      this.tabs = [], //that we are browsing
          this.container = options.container
   },
   /**
    * Grab the layout and call ask for render
    */
   render : function() {
      var self = this;
      require(["text!application/templates/explorer/Tabs.tpl.html"], function(tpl) {
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
      $(this.el).html(_.template(tpl, {}));
      var tabs = $(this.el).children('.tabs');
      /*tabs.tabs().find( ".ui-tabs-nav" ).sortable({ axis: "x" });*/
      tabs.tabs({
         add: function(event, ui) {

            /*$("#"+ui.panel.id).parent().parent().css('height', "100%");*/
            if (ui.panel.id != ("tabs-dashboard-"+window.app.status.currentProject)
                && (ui.panel.id != "tabs-images-"+window.app.status.currentProject)
                && ui.panel.id != ("tabs-annotations-"+window.app.status.currentProject)
                && ui.panel.id != ("tabs-algos-"+window.app.status.currentProject)) {
               tabs.find("ul").find("a[href=#"+ui.panel.id+"]").find("span").attr("style", "display:inline;");
               tabs.find("ul").find("a[href=#"+ui.panel.id+"]").parent().append("<span class='ui-icon ui-icon-close' style='display:inline;cursor:pointer;'>&nbsp;&nbsp;&nbsp;&nbsp;</span>");
               tabs.find("ul").find("a[href=#"+ui.panel.id+"]").parent().find("span.ui-icon-close" ).click(function() {
                  var index = $( "li", tabs ).index( $( this ).parent() );
                  self.removeTab(index);
               });
               self.triggerRoute = false;
               tabs.tabs('select', '#' + ui.panel.id);
               self.triggerRoute = true;
            } else {
               //$(ui.panel).css({ 'background-image': 'url(http://subtlepatterns.com/patterns/whitey.png)'});
            }
            /*$("#"+ui.panel.id).attr('style', 'width:100%;height:100%;overflow:hidden;');*/
         },
         show: function(event, ui){
            /*$("#"+ui.panel.id).attr('style', 'width:100%;height:100%;overflow:hidden;');*/
            return true;
         },
         select: function(event, ui) {
            window.app.controllers.browse.navigate("#"+ui.panel.id, self.triggerRoute);

         }
      });

      $("ul.tabs a").css('height', $("ul.tabs").height());
      return this;
   },
   /**
    *  Add a Tab containing a BrowseImageView instance
    *  @idImage : the id of the Image we want to display
    *  @options : some init options we want to pass the the BrowseImageView Instance
    */
   addBrowseImageView : function(idImage, options) {
      var self = this;
      var tab = this.getBrowseImageView(idImage);
      if (tab != null) {
         tab.view.show(options);
         return;
      }
      var tabs = $(self.el).children('.tabs');
      new ImageInstanceModel({id : idImage}).fetch({
         success : function(model, response) {
            var view = new BrowseImageView({
               model : model,
               initCallback : function(){view.show(options)},
               el: tabs
            }).render();
            self.tabs.push({idImage : idImage,view : view});
         }
      });
   },
   /**
    * Return the reference to a BrowseImageView instance
    * contained in a tab
    * @param idImage the ID of an Image contained in a BrowseImageView
    */
   getBrowseImageView : function(idImage) {
      var object  = _.detect(this.tabs, function(object) {
         return object.idImage == idImage;
      });
      return object != null ? object : null;
   },
   /**
    * Remove a Tab
    * @param index the identifier of the Tab
    */
   removeTab : function (index) {
      this.tabs.splice(index,1);
      var tabs = $(this.el).children('.tabs');
      tabs.tabs( "remove", index);
   },
   /**
    * Show a tab
    * @param index the identifier of the Tab
    */
   showTab : function(index) {
      var tabs = $("#explorer > .browser").children(".tabs");
      tabs.tabs("select", "#tabs-image-"+window.app.status.currentProject+"-"+index+"-");
      return;
   },
   /**
    * Return the number of opened tabs
    */
   size : function() {
      return _.size(this.tabs);
   },
   /**
    * Close all the Tabs
    */
   closeAll : function() {
      var tabs = $(this.el).children('.tabs');
      for (var i = tabs.tabs('length') - 1; i >= 0; i--) {
         tabs.tabs('remove', i);
      }
      this.tabs = [];
      tabs.tabs("destroy");
      $(this.el).hide();
      $(this.el).parent().find('.noProject').show();
   },
   /**
    * Add a ProjectDashBoardView instance in the first Tab
    * @param dashboard the ProjectDashBoardView instance
    */
   addDashboard : function(dashboard) {

      var tabs = $(this.el).children('.tabs');
      tabs.tabs("add", "#tabs-dashboard-"+window.app.status.currentProject, 'Dashboard');
      tabs.tabs("add", "#tabs-images-"+window.app.status.currentProject, 'Images');
      tabs.tabs("add", "#tabs-annotations-"+window.app.status.currentProject, 'Annotations');
      tabs.tabs("add", "#tabs-algos-"+window.app.status.currentProject, 'Expert');
      $("#explorer > .browser").show();
      $("#explorer > .noProject").hide();
      this.tabs.push({
         idImage : 0,
         view : dashboard
      });
      this.tabs.push({
         idImage : 1,
         view : dashboard
      });
      this.tabs.push({
         idImage : 2,
         view : dashboard
      });
      this.tabs.push({
         idImage : 3,
         view : dashboard
      });
   },
   /**
    * Ask to the dashboard view to refresh
    */
   getDashboard : function () {
      var dashboardTab = _.detect(this.tabs, function(object) {
         return object.idImage == 0;
      });
      return dashboardTab;
   }
});
