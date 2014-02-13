var LoginDialogView = Backbone.View.extend({
    tagName: "div",
    dialog : null,
    initialize: function (options) {
    },
    doLayout: function (tpl) {
        var self = this;
        this.dialog = new ConfirmDialogView({
            el: '#dialogs',
            template: _.template(tpl, {version: window.app.status.version}),
            dialogAttr: {
                dialogID: "#login-confirm",
                backdrop: false
            }
        }).render();

        $("#j_username").click(function () {
            $(this).select();
        });
        $("#j_password").click(function () {
            $(this).select();
        });
        $('#submit-login').click(function(e) {
            e.preventDefault();
            window.app.controllers.auth.doLogin();
        });
        $('#submit-forgotPassword').click(function(e) {
            e.preventDefault();
            window.app.controllers.auth.doForgotPassword();
        });
        $('#submit-forgotUsername').click(function(e) {
            e.preventDefault();
            window.app.controllers.auth.doForgotUsername();
        });
        $('#forgotUsername').click(function(e) {
            e.preventDefault();
            self.forgotUsername();
        });
        $('#forgotPassword').click(function(e) {
            e.preventDefault();
            self.forgotPassword();
        });
        $('#restoreLogin').click(function(e) {
            e.preventDefault();
            self.restoreLogin();
        });


        return this;
    },
    render: function () {
        var self = this;
        require(["text!application/templates/auth/LoginDialog.tpl.html"], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    close: function () {
        this.dialog.close();
    },
    forgotPassword : function () {
        $("#formGrouploginPassword").hide();
        $("#formGrouploginEmail").hide();
        $("#formGroupSubmitLogin").hide();
        $("#formGroupSubmitForgotUsername").hide();
        $("#help-inline").hide();

        $("#help-inline-forgot").show();
        $("#formGrouploginUsername").show();
        $("#formGroupSubmitForgotPassword").show();
    },
    forgotUsername : function () {
        $("#formGrouploginUsername").hide();
        $("#formGrouploginPassword").hide();
        $("#formGroupSubmitLogin").hide();
        $("#formGroupSubmitForgotPassword").hide();
        $("#help-inline").hide();

        $("#help-inline-forgot").show();
        $("#formGrouploginEmail").show();
        $("#formGroupSubmitForgotUsername").show();
    },
    restoreLogin : function () {
        $("#formGrouploginUsername").show();
        $("#formGrouploginPassword").show();
        $("#formGroupSubmitLogin").show();
        $("#help-inline").show();

        $("#help-inline-forgot").hide();
        $("#formGroupSubmitForgotPassword").hide();
        $("#formGroupSubmitForgotUsername").hide();
        $("#formGrouploginEmail").hide();
    }

});