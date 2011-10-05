var ConfirmDialogView = Backbone.View.extend({
   tagName : "div",
   templateURL : null,
   templateData : null,
   initialize: function(options) {
      this.el = options.el;
      this.template = options.template;
      this.templateURL = options.templateURL;
      this.autoOpen = options.autoOpen;
      this.templateData = options.templateData;
      this.dialogAttr = options.dialogAttr;
      this.dialogAttr.autoOpen =  options.dialogAttr.autoOpen;
      if (options.dialogAttr.autoOpen == undefined) this.dialogAttr.autoOpen = true;
      if (!options.dialogAttr.width) this.dialogAttr.width = 'auto';
      if (!options.dialogAttr.height) this.dialogAttr.height = 'auto';
   },
   doLayout : function(tpl)  {
      var self = this;
      $(this.el).html(tpl);

      $(this.dialogAttr.dialogID).modal({
         keyboard : true,
         show : true
      });
      $(this.dialogAttr.dialogID).bind('hidden', function () {
         $(self.dialogAttr.dialogID).remove();
      });
      /*$(this.dialogAttr.dialogID).modal('show');*/
         /* {

         create: function (event, ui) {
            $(self.dialogAttr.dialogID).prev('.ui-dialog-titlebar').hide();
         },
         resizable: false,
         autoOpen : this.dialogAttr.autoOpen,
         draggable : false,
         width: this.dialogAttr.width,
         height: this.dialogAttr.height,
         closeOnEscape : true,
         modal: true,
         //close : this.dialogAttr.close,
         buttons: this.dialogAttr.buttons
      }
      );*/

      /*$(".ui-panel-header").css("display","block");*/
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