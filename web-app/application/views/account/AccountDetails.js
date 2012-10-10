var AccountDetails = Backbone.View.extend({
    initialize:function (options) {

    },
    events:{

    },
    render:function () {
        var self = this;
        require([
            "text!application/templates/account/AccountDetails.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl);
            });
        return this;
    },
    editProfile:function () {
        var self = this;
        var user = new UserModel(this.model.toJSON());
        user.save({
            "firstname":$("#input_firstname").val(),
            "lastname":$("#input_lastname").val(),
            "email":$("#input_email").val()
        }, {
            success:function (model, response) {
                self.model = new UserModel(response.user);
                window.app.view.message("Success", response.message, "success");
            },
            error:function (model, response) {
                window.app.view.message("Error", response.message, "error");
            }
        });
    },
    editPassword:function () {
        var user = new UserModel(this.model.toJSON());
        user.save({
            "password":$("#input_new_password").val(),
            "password2":$("#input_new_password").val()
        }, {
            success:function (model, response) {
                window.app.view.message("Success", response.message, "success");
                $("#input_new_password").val("");
                $("#input_new_password_confirm").val("");
                $("#input_password").val("");
                $("#input_password").closest(".control-group").removeClass("success");
                $("#input_new_password").closest(".control-group").removeClass("success");
                $("#input_new_password_confirm").closest(".control-group").removeClass("success");
            },
            error:function (model, response) {
                window.app.view.message("Error", response.message, "error");
            }
        });
    },
    validatePassword:function () {
        return $("#input_new_password").val() != "" &&
            $("#input_new_password_confirm").val() != "" &&
            ($("#input_new_password").val() == $("#input_new_password_confirm").val());
    },
    doLayout:function (tpl) {
        var self = this;
        $(this.el).html(_.template(tpl, this.model.toJSON()));
        $("#edit_profile_form").submit(function (e) {
            self.editProfile();
            e.preventDefault();
        });
        $("#input_new_password_confirm").keyup(function () {
            $("#input_new_password_confirm").closest('.control-group').removeClass("warning");
            $("#input_new_password_confirm").closest('.control-group').removeClass("success");
            $("#input_new_password").closest('.control-group').removeClass("warning");
            $("#input_new_password").closest('.control-group').removeClass("success");
            if ($(this).val() != "") {
                if (self.validatePassword()) {
                    $("#input_new_password_confirm").closest('.control-group').addClass("success");
                    $("#input_new_password").closest('.control-group').addClass("success");
                    $("#submit_edit_password").removeAttr("disabled");
                } else {
                    $("#input_new_password_confirm").closest('.control-group').addClass("warning");
                    $("#input_new_password").closest('.control-group').addClass("warning");
                    $("#submit_edit_password").attr("disabled", "disabled");
                }
            }
        });
        $("#input_new_password").keyup(function () {
            if ($(this).val() != "") {
                $("#input_new_password_confirm").removeAttr("disabled");
            } else {
                $("#input_new_password_confirm").attr("disabled", "disabled");
            }
        });
        $("#input_password").keyup(function () {
            console.log("change");
            var newPassword = $("#input_password").val();
            var data = { 'j_username':self.model.get('username'), 'j_password':newPassword}
            $.ajax({
                url:'j_spring_security_check',
                type:'post',
                dataType:'json',
                data:data,
                success:function (data) {
                    $("#input_password").closest('.control-group').removeClass("warning");
                    $("#input_password").closest('.control-group').addClass("success");
                    $("#input_new_password").removeAttr("disabled");
                },
                error:function (data) {
                    $("#input_password").closest('.control-group').removeClass("success");
                    if (newPassword != "") {
                        $("#input_password").closest('.control-group').addClass("warning");
                    }
                    $("#input_new_password").attr("disabled", "disabled");
                    $("#submit_edit_password").attr("disabled", "disabled");
                }
            });
        });
        $("#edit_password_form").submit(function (e) {
            if (self.validatePassword()) {
                self.editPassword();
            }
            e.preventDefault();
        });
        $("#regenerate_keys_form").submit(function (e) {
            var user = new UserModel(self.model.toJSON());
            user.save({
                'publicKey':"",
                'privateKey':""
            }, {
                success:function (model, response) {
                    window.app.view.message("Success", response.message, "success");
                    self.model = new UserModel(response.user);
                    $("#input_public_key").val(self.model.get("publicKey"));
                    $("#input_private_key").val(self.model.get("privateKey"));
                },
                error:function (model, response) {
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
                field.closest(".control-group").addClass("warning");
            } else {
                field.closest(".control-group").removeClass("warning");
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