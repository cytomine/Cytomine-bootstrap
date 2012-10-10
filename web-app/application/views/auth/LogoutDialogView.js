var LogoutDialogView = Backbone.View.extend({
    tagName:"div",

    initialize:function (options) {
    },
    doLayout:function (tpl) {
        var dialog = new ConfirmDialogView({
            el:'#dialogs',
            template:_.template(tpl, {}),
            dialogAttr:{
                dialogID:"#logout-confirm"
            }
        }).render();

        $("#submit-logout").click(function (e) {
            e.preventDefault();
            window.location = "logout";
        });

        $("#cancel-logout").click(function (e) {
            e.preventDefault();
            $("#logout-confirm").modal('hide');
            $("#logout-confirm").remove();
        });

        return this;
    },
    render:function () {
        var self = this;
        require(["text!application/templates/auth/LogoutDialog.tpl.html"], function (tpl) {
            self.doLayout(tpl);
        });
    }
});