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
          this.initComponents();
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


          return this;
       },
       /**
        * Grab the layout and call ask for render
        */
       render : function(renderCallback) {
          var self = this;
          require([
             "text!application/templates/BaseLayout.tpl.html"
          ],
              function(tpl) {
                 self.doLayout(tpl, renderCallback);

                  $('#file_upload').fileUploadUI({

                 uploadTable: $('#files'),
                 downloadTable: $('#files'),
                 buildUploadRow: function (files, index) {
                    return $('<tr><td class="file_upload_preview"><\/td>' +
                        '<td>' + files[index].name + '<\/td>' +
                        '<td class="file_upload_progress"><div><\/div><\/td>' +
                        '<td class="file_upload_start">' +
                        '<button class="ui-state-default ui-corner-all" title="Start Upload">' +
                        '<span class="ui-icon ui-icon-circle-arrow-e">Start Upload<\/span>' +
                        '<\/button><\/td>' +
                        '<td class="file_upload_cancel">' +
                        '<button class="ui-state-default ui-corner-all" title="Cancel">' +
                        '<span class="ui-icon ui-icon-cancel">Cancel<\/span>' +
                        '<\/button><\/td><\/tr>');
                 },
                 buildDownloadRow: function (file) {
                    return $('<tr><td>' + file.name + '<\/td><\/tr>');
                 },
                 beforeSend: function (event, files, index, xhr, handler, callBack) {
                    handler.uploadRow.find('.file_upload_start button').click(function () {
                       callBack();
                       return false;
                    });
                 }
              });
          $('#start_uploads').click(function () {
             $('.file_upload_start button').click();
             return false;
          });
              });
          return this;
       },
       /**
        * Initialize the components of the application
        */
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

          $("#noProjectDialog").panel({collapsible:false, height : "100%"});


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



