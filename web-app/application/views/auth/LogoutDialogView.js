var LogoutDialogView = Backbone.View.extend({
   tagName : "div",

   initialize: function(options) {
   },
   doLayout: function(tpl) {
      var dialog = new ConfirmDialogView({
         el:'#dialogs',
         template : _.template(tpl, {}),
         dialogAttr : {
            dialogID : "#logout-confirm"
         }
      }).render();

      $("#submit-logout").click(function(){
         window.location = "logout";
      });

      return this;
   },
   render: function() {
      var self = this;
      require(["text!application/templates/auth/LogoutDialog.tpl.html"], function(tpl) {
         self.doLayout(tpl);
      });
   }
});