
var ApplicationController = Backbone.Controller.extend({

    routes: {
        "login"     :   "login",
        "logout"    :   "logout",
        "explorer"  :   "explorer",
        "admin"     :   "admin",
        "undo"      :   "undo",
        "redo"      :   "redo"
    },

    undo : function() {
		$.getJSON('command/undo.json', function(data) {
            var items = [];

            $.each(data, function(key, val) {

            });

            window.app.message("Redo", data.message, "");


        });

    },

    redo : function () {
        $.getJSON('command/redo.json', function(data) {
            var items = [];

            $.each(data, function(key, val) {

            });

            window.app.message("Undo", data.message, "");


        });

    },

    login : function () {
        $("#app").hide();
        new ConfirmDialogView({
            el:'#dialogs',
            //template : _.template($('#login-dialog-tpl').html()),
            template : ich.logindialogtpl({}, true),
            dialogAttr : {
                dialogID : "#login-confirm",
                buttons: {
                    "Login": function() {

                    }
                },
                close :function (event) {
                    $(this).remove();
                }
            }
        }).render();
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
    },

    startup : function () {

		this.status = new Status('server/ping', function(status) {
            var dialog = new ConfirmDialogView({
                el:'#dialogs',
                //template : _.template($('#server-down-tpl').html()),
                template : ich.serverdowntpl({}, true),
                dialogAttr : {
                    dialogID : "#server-down",
                    buttons: {
                        "OK": function() {

                        }
                    },
                    close :function (event) {
                        $(this).remove();
                    }
                }
            }).render();
        }, 10000);

        new ProjectController();
        new ImageController();
        new BrowseController();
        window.app = new ApplicationView({
            el: $('#app')
        }).render();
        this.showComponent(window.app.components.explorer);
        Backbone.history.start();


    },


    explorer: function() {
        this.showComponent(window.app.components.explorer);
    },

    admin: function() {
        this.showComponent(window.app.components.admin);
    },

    showComponent : function (component) {
        for (var i in window.app.components) {
            var c = window.app.components[i];
            if (c == component) continue;
            c.deactivate();
        }
        $("#app").show();
        component.activate();

    }

});