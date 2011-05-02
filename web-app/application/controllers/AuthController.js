var AuthController = Backbone.Controller.extend({

    routes: {
        "logout"    :   "logout"
    },

    login : function () {

        var doLogin = function () {
            var app = new ApplicationView(); //in order to use message function
            var data = $("#login-form").serialize();
            $.ajax({
                url: 'j_spring_security_check',
                type: 'post',
                dataType : 'json',
                data : data,
                success : function(data){
                    app.message("Welcome", "You are logged as " + data.fullname, "");
                    window.app.status.user = {
                        authenticated : true,
                        id : data.id
                    }
                    window.app.startup();

                },
                error : function(data) {
                    var resp = $.parseJSON(data.responseText);

                    app.message("Error", resp.message, "error");
                }
            });
            return false;
        }
        var dialog = new ConfirmDialogView({
            el:'#dialogs',
            //template : _.template($('#login-dialog-tpl').html()),
            template : ich.logindialogtpl({}, true),
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
        $('#login-form').submit(doLogin);
        $('#login-form').keydown(function(e){
            if (e.keyCode == 13) { //ENTER_KEY
                $('#login-form').submit();
                return false;
            }
        });

    },

    logout : function () {
        var dialog = new ConfirmDialogView({
            el:'#dialogs',
            //template : _.template($('#logout-dialog-tpl').html()),
            template : ich.logoutdialogtpl({}, true),
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
    }
});