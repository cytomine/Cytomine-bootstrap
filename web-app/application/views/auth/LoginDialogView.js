var LoginDialogView = Backbone.View.extend({
    tagName: "div",
    dialog : null,
    initialize: function (options) {
    },
    doLayout: function (tpl) {
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
        $('#login-form').submit(function(e) {
            e.preventDefault();
            window.app.controllers.auth.doLogin();
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
    }
});