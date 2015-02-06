var AccountDetails = Backbone.View.extend({
    initialize: function (options) {

    },
    events: {

    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/account/AccountDetails.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl);
            });
        return this;
    },
    editProfile: function () {
        var self = this;
        var user = new UserModel(this.model.toJSON());
        user.save({
            "firstname": $("#input_firstname").val(),
            "lastname": $("#input_lastname").val(),
            "email": $("#input_email").val()
        }, {
            success: function (model, response) {
                self.model = new UserModel(response.user);
                window.app.view.message("Success", response.message, "success");
            },
            error: function (model, response) {
                window.app.view.message("Error", response.message, "error");
            }
        });
    },
    editPassword: function () {
        var user = new UserModel(this.model.toJSON());
        user.save({
            "oldPassword": $("#password").val(),
            "password": $("#input_new_password").val()
        }, {
            success: function (model, response) {
                window.app.view.message("Success", response.message, "success");
                $("#input_new_password").val("");
                $("#input_new_password_confirm").val("");
                $("#input_password").val("");
                $("#input_password").closest(".form-group").removeClass("has-success");
                $("#input_new_password").closest(".form-group").removeClass("has-success");
                $("#input_new_password_confirm").closest(".form-group").removeClass("has-success");
                $("#password_expired_alert").hide();
                $("#input_password").closest('.form-group').show();
                $("#input_new_password").attr("disabled", "disabled");
                $("#input_new_password_confirm").attr("disabled", "disabled");

            },
            error: function (model, response) {
                window.app.view.message("Error", response.message, "error");
            }
        });
    },
    validatePassword: function () {
        return $("#input_new_password").val() != "" &&
            $("#input_new_password_confirm").val() != "" &&
            ($("#input_new_password").val() == $("#input_new_password_confirm").val());
    },
    doLayout: function (tpl) {
        var self = this;
        this.model.set({ host : window.location.host });
        this.model.set({ version : window.app.status.version });
        $(this.el).html(_.template(tpl, this.model.toJSON()));

        if (self.model.get("passwordExpired")) {
            $("#input_password").closest('.form-group').hide();
            $("#input_new_password").removeAttr("disabled");
            $("#password_expired_alert").show();
        }
        $("#edit_profile_form").submit(function (e) {
            self.editProfile();
            e.preventDefault();
        });


        $.ajax({
            type: "GET",
            url: "/api/ldap/"+this.model.toJSON().username+"/user.json",
            contentType:"application/json; charset=utf-8",
            dataType:"json",
            success: function(response) {
                if(response.result == true) {
                    $("#password_panel").remove();
                }
            }
        });




        $("#input_new_password_confirm").keyup(function () {
            $("#input_new_password_confirm").closest('.form-group').removeClass("has-warning");
            $("#input_new_password_confirm").closest('.form-group').removeClass("has-success");
            $("#input_new_password").closest('.form-group').removeClass("has-warning");
            $("#input_new_password").closest('.form-group').removeClass("has-success");
            if ($(this).val() != "") {
                $("#submit_edit_password").removeAttr("disabled");
                if (self.validatePassword()) {
                    $("#input_new_password_confirm").closest('.form-group').addClass("has-success");
                    $("#input_new_password").closest('.form-group').addClass("has-success");
                }
            } else {
                /*$("#input_new_password_confirm").closest('.form-group').addClass("has-warning");
                 $("#input_new_password").closest('.form-group').addClass("has-warning");*/
                 $("#submit_edit_password").attr("disabled", "disabled");
            }
        });
        $("#input_new_password").keyup(function () {
            if ($(this).val().length >= 5) {
                $("#input_new_password_confirm").removeAttr("disabled");
            } else {
                $("#input_new_password_confirm").attr("disabled", "disabled");
            }
        });
        $("#input_password").keyup(function () {
            console.log("change");
            var newPassword = $("#input_password").val();
            if (self.model.get("passwordExpired")) {
                return;
            }
            var data = { 'j_username': self.model.get('username'), 'j_password': newPassword}
            $.ajax({
                url: 'j_spring_security_check',
                type: 'post',
                dataType: 'json',
                data: data,
                success: function (data) {
                    $("#input_password").closest('.form-group').removeClass("has-warning");
                    $("#input_password").closest('.form-group').addClass("has-success");
                    $("#input_new_password").removeAttr("disabled");
                },
                error: function (data) {
                    $("#input_password").closest('.form-group').removeClass("has-success");
                    if (newPassword != "") {
                        $("#input_password").closest('.form-group').addClass("has-warning");
                    }
                    $("#input_new_password").attr("disabled", "disabled");
                    $("#submit_edit_password").attr("disabled", "disabled");
                }
            });
        });
        $("#edit_password_form").submit(function (e) {
            if (self.validatePassword()) {

                // check if not ldap
                $.ajax({
                    type: "GET",
                    url: "/api/ldap/"+this.model.toJSON().username+"/user.json",
                    contentType:"application/json; charset=utf-8",
                    dataType:"json",
                    success: function(response) {

                        if(response.result == false) {
                            self.editPassword();
                        }
                    }
                });
            } else {
                window.app.view.message("Change Password", "Confirmation password is not equal to the new password!", "error");
            }
            e.preventDefault();
        });
        $("#regenerate_keys_form").submit(function (e) {
            var user = new UserModel(self.model.toJSON());
            user.save({
                'publicKey': "",
                'privateKey': ""
            }, {
                success: function (model, response) {
                    window.app.view.message("Success", response.message, "success");
                    self.model = new UserModel(response.user);
                    $("#input_public_key").val(self.model.get("publicKey"));
                    $("#input_private_key").val(self.model.get("privateKey"));
                },
                error: function (model, response) {
                    window.app.view.message("Error", response.message, "error");
                }
            });
            e.preventDefault();
        });
        $("#edit_profile_form").keyup(function (e) {
            //update current field status
            var field = $(e.target);
            console.log("field val :" + field.val());
            if (field.val() == "") {
                field.closest(".form-group").addClass("has-warning");
            } else {
                field.closest(".form-group").removeClass("has-warning");
            }
            //update save button
            var canUpdate = ($("#input_firstname").val() != "") && ($("#input_lastname").val() != "") && ($("#input_email").val() != "");
            if (canUpdate) {
                $("#edit_profile_submit").removeAttr("disabled");
            } else {
                $("#edit_profile_submit").attr("disabled", "disabled");
            }
        });
    }
});