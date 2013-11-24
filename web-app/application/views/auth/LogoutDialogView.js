var LogoutDialogView = Backbone.View.extend({
    tagName: "div",
    dialog : null,
    initialize: function (options) {
    },
    doLayout: function (tpl) {
        this.dialog = new ConfirmDialogView({
            el: '#dialogs',
            template: _.template(tpl, {}),
            dialogAttr: {
                dialogID: "#logout-confirm"
            }
        }).render();

        $("#submit-logout").click(function (e) {
            e.preventDefault();
            window.location = "logout";
        });

        return this;
    },
    render: function () {
        var self = this;
        require(["text!application/templates/auth/LogoutDialog.tpl.html"], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    }
});