var LoginDialogView = Backbone.View.extend({
       tagName : "div",
       initialize: function(options) {
       },
       doLayout: function(html) {
          var dialog = new ConfirmDialogView({
                 el:'#dialogs',
                 template : _.template(html, {version : window.app.status.version}),
                 dialogAttr : {
                    dialogID : "#login-confirm",
                    width : 475,
                    height : 375,
                    buttons: {
                       "Login": function() {
                          $('#login-form').submit();
                       }
                    },
                    close :function (event) {
                       /*window.location = "403";*/
                    }
                 }
              }).render();
          $("#progress").hide();
          $("#remember_me").button();
          $("#j_username").click(function() {
             $(this).select();
          });
          $("#j_password").click(function() {
             $(this).select();
          });
          $('#login-form').submit(window.app.controllers.auth.doLogin);
          $('#login-form').keydown(function(e){
             if (e.keyCode == 13) { //ENTER_KEY
                $('#login-form').submit();
                return false;
             }
          });
          return this;
       },
       render: function() {
          var self = this;
          require(["text!application/templates/LoginDialog.tpl.html"], function(html) {
             self.doLayout(html);
          });
       },
       close : function() {
          $('#login-form').close();
       }



    });