
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
                 resizable: false,
                 draggable : false,
                 width: this.dialogAttr.width,
                 height: this.dialogAttr.height,
                 closeOnEscape : true,
                 modal: true,
                 /*close : this.dialogAttr.close,*/
                 buttons: this.dialogAttr.buttons
              });
       },
       render: function() {
          var self = this;
          if (this.template == null && this.templateURL != null && this.templateData != null) {
             console.log("require JS : " + this.templateURL);
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