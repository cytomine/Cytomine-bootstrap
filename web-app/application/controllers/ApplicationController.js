
var ApplicationController = Backbone.Controller.extend({

    models : {},

    routes: {
        "login"     :   "login",
        "logout"    :   "logout",
        "explorer"  :   "explorer",
        "admin"     :   "admin",
        "undo"      :   "undo",
        "redo"      :   "redo"
    },

    undo : function() {
		$.post('command/undo.json', {}, function(data) {
            var items = [];

            $.each(data, function(key, val) {

            });

            window.app.message("Redo", data.message, "");


        }, "json");

    },

    redo : function () {
        $.post('command/redo.json', {}, function(data) {
            var items = [];

            $.each(data, function(key, val) {

            });

            window.app.message("Undo", data.message, "");


        }, "json");

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
        //init models
        window.models = {};
        window.models.images = new ImageCollection();
        window.models.images.fetch();
        window.models.users = new UserCollection();
        window.models.users.fetch();

        //init controllers
        new ProjectController();
        new ImageController();
        new BrowseController();

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

        //init base layout
        window.app = new ApplicationView({
            el: $('#app')
        }).render();

        //show explorer
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