var ApplicationView = Backbone.View.extend({

   tagName : "div",
   className : "layout",
   components : {},
   events: {
      "click #undo":          "undo",
      "click #redo":          "redo"
   },

   /**
    *  UNDO the last command
    */
   undo : function () {
      window.app.controllers.command.undo();
   },

   /**
    * REDO the last command
    */
   redo : function () {
      window.app.controllers.command.redo();
   },

   /**
    * ApplicationView constructor. Call the initialization of its components
    * @param options
    */
   initialize: function(options) {

   },
   /**
    * Render the html into the DOM element associated to the view
    * @param tpl
    */
   doLayout: function(tpl, renderCallback) {
      $(this.el).html(_.template(tpl, {}));
      _.each(this.components, function (component) {
         component.render();
      });

      renderCallback.call();
      //$('#switcher').themeswitcher();

      return this;
   },
   /**
    * Grab the layout and call ask for render
    */
   render : function(renderCallback) {
      this.initComponents();
      var self = this;
      require([
         "text!application/templates/BaseLayout.tpl.html"
      ],
          function(tpl) {
             self.doLayout(tpl, renderCallback);
          });
      return this;
   },
   initUserMenu : function () {

     //Init user menu
      require([
         "text!application/templates/MenuDropDown.tpl.html"
      ],function(tpl) {
         $("#menu-right").append(tpl);
         $("#logout").click(function() { window.app.controllers.auth.logout();return false; });
         $("#loggedUser").html(window.app.status.user.model.prettyName());
      });

   },
   /**
    * Initialize the components of the application
    */
   initComponents : function() {
      var self = this;
      require([
         "text!application/templates/upload/UploadComponent.tpl.html",
         "text!application/templates/WarehouseComponent.tpl.html",
         "text!application/templates/explorer/ExplorerComponent.tpl.html",
         "text!application/templates/AdminComponent.tpl.html"
      ],
          function(uploadTpl, warehouseTpl, explorerTpl, adminTpl) {
             self.components.upload = new Component({
                el : "#content",
                template : _.template(uploadTpl, {}),
                buttonAttr : {
                   elButton : "upload-button",
                   buttonText : "Upload",
                   buttonWrapper : "#menu",
                   dataContent : "Send your data !",
                   dataTitle : "Upload",
                   icon : "ui-icon-circle-arrow-s",
                   route : "#upload"
                },
                divId : "upload"
             });
             self.components.warehouse = new Component({
                el : "#content",
                template : _.template(warehouseTpl, {}),
                buttonAttr : {
                   elButton : "warehouse-button",
                   buttonText : "Organize",
                   buttonWrapper : "#menu",
                   dataContent : "Organize your projects, images, etc...",
                   dataTitle : "Organize",
                   icon : "ui-icon-wrench",
                   route : "#project"
                },
                divId : "warehouse"
             });
             self.components.explorer = new Component({
                el : "#content",
                template : _.template(explorerTpl, {}),
                buttonAttr : {
                   elButton : "explorer-button",
                   buttonText : "Explore",
                   buttonWrapper : "#menu",
                   dataContent : "View your data",
                   dataTitle : "Explore",
                   icon : "ui-icon-image",
                   route : "#explorer"
                },
                divId : "explorer",
                activate: function () {
                   if (window.app.status.currentProject == undefined)
                      $("#explorer > .noProject").show();
                   else
                      $("#explorer > .noProject").hide();
                   $("#" + this.divId).show();
                   $("#" + this.buttonAttr.elButton).addClass("ui-state-disabled");
                }
             });
             /*self.components.admin = new Component({
                el : "#content",
                template : _.template(adminTpl, {}),
                buttonAttr : {
                   elButton : "admin-button",
                   buttonText : "Admin",
                   buttonWrapper : "#menu",
                   icon : "ui-icon-wrench",
                   route : "#admin/users"
                },
                divId : "admin"
             });*/

             /*self.components.logout = new Component({
                el : "#content",
                template : "",
                buttonAttr : {
                   elButton : "user-button",
                   buttonText :,
                   buttonWrapper : "#menu",
                   dataContent : "we have to delete this popover for logout",
                   dataTitle : "huhu",
                   icon : "ui-icon-power",
                   route : "#",
                   click :
                },
                divId : "logout"
             });*/
          });
   },
   /**
    * Show a component
    * @param Component the reference to the component
    */
   showComponent : function (component) {
      _.each(this.components, function (c) {
         if (c != component) c.deactivate();
      });
      $("#app").show();
      component.activate();
   }
});

ApplicationView.prototype.message =  function(title, message, type) {
   type = type || 'info';
   if(message!=undefined) {
      message.responseText && (message = message.responseText);
   }

   var tpl = '<div style="min-width: 200px" id="alert{{timestamp}}" class="alert-message {{type}} fade in" data-alert="alert"><a class="close" href="#">×</a><p><strong>{{alert}}</strong> {{message}}</p></div>';
   var timestamp = new Date().getTime();
   $("#alerts").append(_.template(tpl, { alert : title, message : message, timestamp : timestamp, type : type}));
   $("#alert"+timestamp).alert();
   setTimeout(function(){
      $("#alert"+timestamp).alert('close');
      $("#alert"+timestamp).remove();
   }, 3000);

}



