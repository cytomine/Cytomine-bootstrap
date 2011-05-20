var LogoutDialogView = Backbone.View.extend({
    tagName : "div",

    initialize: function(options) {
    },
    render: function() {
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
        return this;
    }
});