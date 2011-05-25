// App
// ---
// View. Represents the entire application "viewport". Available in the global
// namespace as `window.app` and contains various utility methods.
var ApplicationView = Backbone.View.extend({

       tagName : "div",
       className : "layout",
       components : {},

       events: {
          "click #undo":          "undo",
          "click #redo":          "redo"
       },

       undo : function () {
          window.app.controllers.command.undo();
       },

       redo : function () {
          window.app.controllers.command.redo();
       },

       initialize: function(options) {
          this.initComponents();
       },
       doLayout: function(tpl) {
          $(this.el).html(_.template(tpl, {}));
          _.each(this.components, function (component) {
             component.render();
          });

          $("#noProjectDialog").panel({collapsible:false, height : "100%"});
          return this;
       },
       render : function() {
          var self = this;
          require([
             "text!application/templates/BaseLayout.tpl.html"
          ],
              function(tpl) {
                 self.doLayout(tpl);
                 console.log("base layout rendered");
              });
       },
       initComponents : function() {
          var self = this;
          require([
             "text!application/templates/UploadComponent.tpl.html",
             "text!application/templates/WarehouseComponent.tpl.html",
             "text!application/templates/explorer/ExplorerComponent.tpl.html"
          ],
              function(uploadTpl, warehouseTpl, explorerTpl) {
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
                           route : "#warehouse"
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
                           $("#" + this.divId).show();
                           $("#" + this.buttonAttr.elButton).addClass("ui-state-disabled");
                           if(window.app.controllers.dashboard.view!=null)
                              window.app.controllers.dashboard.view.refresh(); //refresh dashboard
                        }
                     });


                 /*this.components.admin = new Component({
                  el : $("#content"),
                  //template : _.template($('#admin-tpl').html()),
                  template :_.template(adminTpl, {}),
                  buttonAttr : {
                  elButton : "admin-button",
                  buttonText : "Admin Area",
                  buttonWrapper : $("#menu"),
                  icon : "ui-icon-gear",
                  route : "#admin"
                  },
                  divId : "admin"
                  }).render();*/
                 self.components.logout = new Component({
                        el : "#content",
                        template : "",
                        buttonAttr : {
                           elButton : "logout-button",
                           buttonText : "Logout",
                           buttonWrapper : "#menu",
                           icon : "ui-icon-power",
                           route : "#",
                           click : function() {  window.app.controllers.auth.logout();}
                        },
                        divId : "logout"
                     });
              });


       },

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



