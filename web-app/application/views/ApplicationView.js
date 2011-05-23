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
       doLayout: function(html) {
          $(this.el).html(_.template(html, {}));
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
              function(html) {
                 self.doLayout(html);
              });
       },
       initComponents : function() {
          var self = this;
          require([
             "text!application/templates/UploadComponent.tpl.html",
             "text!application/templates/WarehouseComponent.tpl.html",
             "text!application/templates/ExplorerComponent.tpl.html"
          ],
              function(upload, warehouse, explorer) {
                 self.components.upload = new Component({
                        el : "#content",
                        template : _.template(upload, {}),
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
                        template : _.template(warehouse, {}),
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
                        template : _.template(explorer, {}),
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
                  template : ich.admintpl({}, true),
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
          /*for (var i in window.app.view.components) {
           var c = window.app.view.components[i];
           if (c == component) continue;
           c.deactivate();
           }*/
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

   var stack_bottomright = {"dir1": "up", "dir2": "left", "firstpos1": 15, "firstpos2": 15};
   var opts = {
      pnotify_title: title,
      pnotify_text: message,
      pnotify_notice_icon: "ui-icon ui-icon-info",
      pnotify_type : type,
      pnotify_history: history
      //pnotify_addclass: "stack-bottomright",
      //pnotify_stack: stack_bottomright
   };
   $.pnotify(opts);

}



