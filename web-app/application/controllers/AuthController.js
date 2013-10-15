var AuthController = Backbone.Router.extend({

    routes: {
    },

    login: function () {
        var loginView = new LoginDialogView({}).render();
    },
    logout: function () {
        var logoutView = new LogoutDialogView({}).render();
    },

    doLogin: function () {
        var app = new ApplicationView(); //in order to use message function
        var data = $("#login-form").serialize(); //should be in LoginDIalogView

        var location = window.location;
        $.ajax({
            url: 'j_spring_security_check',
            type: 'post',
            dataType: 'json',
            data: data,
            success: function (data) {
                app.message("Welcome", "You are logged as " + data.fullname, "", "success");
                new UserModel({id: data.id}).fetch({
                    success: function (model, response) {
                        window.app.status.user = {
                            authenticated: true,
                            id: data.id,
                            model: model,
                            filenameVisible : true
                        }
                        $("#login-confirm").remove();
                        window.app.startup();
//                        window.location = location;
//                        window.location.reload(true);
                    }
                });

            },
            error: function (data) {
                var resp = $.parseJSON(data.responseText);
                $('#submit-login').attr('disabled', 'disabled');
                $('#login-confirm').effect("shake", { times: 2 }, 100);
                setTimeout(function () {
                    $('#submit-login').removeAttr('disabled');
                }, 400);
                app.message("Error", resp.message, "error");
            }
        });
        return false;
    }
});