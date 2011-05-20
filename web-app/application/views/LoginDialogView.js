var LoginDialogView = Backbone.View.extend({
    tagName : "div",

    initialize: function(options) {
    },
    render: function() {
        var dialog = new ConfirmDialogView({
            el:'#dialogs',
            //template : _.template($('#login-dialog-tpl').html()),
            template : ich.logindialogtpl({version : window.app.status.version}, true),
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
    close : function() {
       $('#login-form').close();
    }



});