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
             self.components.logout = new Component({
                el : "#content",
                template : "",
                buttonAttr : {
                   elButton : "user-button",
                   buttonText :window.app.status.user.model.prettyName(),
                   buttonWrapper : "#menu",
                   icon : "ui-icon-power",
                   route : "#",
                   click : function() { window.app.controllers.auth.logout();return false; }
                },
                divId : "logout"
             });

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

ApplicationView.prototype.message =  function(title, message, type, pnotify) {
   ApplicationView.prototype.message(title, message, type, pnotify, true);
}
ApplicationView.prototype.message =  function(title, message, type, pnotify,history) {
   type = type || 'status';

   if(message!=undefined)
   {
      message.responseText && (message = message.responseText);
   }

   var opts = {
      pnotify_title: title,
      pnotify_text: message,
      pnotify_notice_icon: "ui-icon ui-icon-info",
      pnotify_type : type,
      pnotify_history: history
   };
   $.pnotify(opts);

}



