var ConfirmDialogView = Backbone.View.extend({
       tagName : "div",
       templateURL : null,
       templateData : null,
       initialize: function(options) {
          this.el = options.el;
          this.template = options.template;
          this.templateURL = options.templateURL;
          this.templateData = options.templateData;
          this.dialogAttr = options.dialogAttr;
          if (!options.dialogAttr.width) this.dialogAttr.width = 'auto';
          if (!options.dialogAttr.height) this.dialogAttr.height = 'auto';
       },
       doLayout : function(tpl)  {
          $(this.el).html(tpl);
          $(this.dialogAttr.dialogID).dialog({
                 create: function (event, ui) {
                    $(".ui-widget-header").hide();
                 },
                 resizable: false,
                 draggable : false,
                 width: this.dialogAttr.width,
                 height: this.dialogAttr.height,
                 closeOnEscape : true,
                 modal: true,
                 /*close : this.dialogAttr.close,*/
                 buttons: this.dialogAttr.buttons
              });
           $(".ui-panel-header").css("display","block");
       },
       render: function() {
          var self = this;
          if (this.template == null && this.templateURL != null && this.templateData != null) {
             require([this.templateURL], function(tpl) {
                self.template = _.template(tpl, self.templateData);
                self.doLayout(self.template);
             });
          } else {
             this.doLayout(this.template);
          }
          return this;
       }



    });