var LogoutDialogView = Backbone.View.extend({
       tagName : "div",

       initialize: function(options) {
       },
       doLayout: function(html) {
          var dialog = new ConfirmDialogView({
                 el:'#dialogs',
                 template : _.template(html, {}),
                 dialogAttr : {
                    dialogID : "#logout-confirm",

                    buttons: {
                       "Confirm": function() {
                          window.location = "logout";
                       },
                       "Cancel": function() {
                          $(this).dialog("close");
                       }
                    },
                    close :function (event) {
                       $(this).remove();
                    }
                 }
              }).render();
          return this;
       },
       render: function() {
          var self = this;
          require(["text!application/templates/LogoutDialog.tpl.html"], function(html) {
             self.doLayout(html);
          });
       }
    });