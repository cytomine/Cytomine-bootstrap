var LoadingDialogView = Backbone.View.extend({
    tagName:"div",
    initialize:function (options) {
    },
    doLayout:function (tpl) {
        var dialog = new ConfirmDialogView({
            el:'#dialogs',
            template:_.template(tpl, {})
        }).render();

    },
    render:function () {
        var self = this;
        require(["text!application/templates/auth/LoadingDialog.tpl.html"], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },
    initProgressBar:function () {
        $("#progress").show();
        $("#login-progressbar").progressbar({
            value:0
        });
    },
    progress:function (value) {
        $("#login-progressbar").progressbar({
            value:value
        });
    },
    close:function () {

    }
});