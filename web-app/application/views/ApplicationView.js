// App
// ---
// View. Represents the entire application "viewport". Available in the global
// namespace as `window.app` and contains various utility methods.
var ApplicationView = Backbone.View.extend({

    tagName : "div",
    className : "layout",
    components : {},

    events: {
        "click #undo":          "undo",
        "click #redo":          "redo"
    },

    undo : function () {
        window.app.controllers.command.undo();
    },

    redo : function () {
        window.app.controllers.command.redo();
    },

    initialize: function(options) {
        this.initComponents();
    },
    render: function() {
        $(this.el).html(ich.baselayouttpl({}, true));
        _.each(this.components, function (component) {
            component.render();
        });
        /* init upload */
        $('#file_upload').fileUploadUI({
            uploadTable: $('#files'),
            downloadTable: $('#files'),
            buildUploadRow: function (files, index) {
                return $('<tr><td class="file_upload_preview"><\/td>' +
                        '<td>' + files[index].name + '<\/td>' +
                        '<td class="file_upload_progress"><div><\/div><\/td>' +
                        '<td class="file_upload_start">' +
                        '<button class="ui-state-default ui-corner-all" title="Start Upload">' +
                        '<span class="ui-icon ui-icon-circle-arrow-e">Start Upload<\/span>' +
                        '<\/button><\/td>' +
                        '<td class="file_upload_cancel">' +
                        '<button class="ui-state-default ui-corner-all" title="Cancel">' +
                        '<span class="ui-icon ui-icon-cancel">Cancel<\/span>' +
                        '<\/button><\/td><\/tr>');
            },
            buildDownloadRow: function (file) {
                return $('<tr><td>' + file.name + '<\/td><\/tr>');
            },
            beforeSend: function (event, files, index, xhr, handler, callBack) {
                handler.uploadRow.find('.file_upload_start button').click(function () {
                    callBack();
                    return false;
                });
            }
        });
        $('#start_uploads').click(function () {
            $('.file_upload_start button').click();
            return false;
        });
        return this;
    },
    initComponents : function() {
        this.components.upload = new Component({
            el : "#content",
            template : ich.uploadtpl({}, true),
            buttonAttr : {
                elButton : "upload-button",
                buttonText : "Upload",
                buttonWrapper : "#menu",
                icon : "ui-icon-circle-arrow-s",
                route : "#upload"
            },
            divId : "upload"
        });

        this.components.warehouse = new Component({
            el : "#content",
            template : ich.warehousetpl({}, true),
            buttonAttr : {
                elButton : "warehouse-button",
                buttonText : "Manage",
                buttonWrapper : "#menu",
                icon : "ui-icon-search",
                route : "#warehouse"
            },
            divId : "warehouse"
        });
        this.components.explorer = new Component({
            el : "#content",
            template : ich.explorertpl({}, true),
            buttonAttr : {
                elButton : "explorer-button",
                buttonText : "Explore",
                buttonWrapper : "#menu",
                icon : "ui-icon-image",
                route : "#explorer"
            },
            divId : "explorer"
        });

        /*this.components.admin = new Component({
         el : $("#content"),
         //template : _.template($('#admin-tpl').html()),
         template : ich.admintpl({}, true),
         buttonAttr : {
         elButton : "admin-button",
         buttonText : "Admin Area",
         buttonWrapper : $("#menu"),
         icon : "ui-icon-gear",
         route : "#admin"
         },
         divId : "admin"
         }).render();*/
        this.components.logout = new Component({
            el : "#content",
            template : ich.logouttpl({}, true),
            buttonAttr : {
                elButton : "logout-button",
                buttonText : "Logout",
                buttonWrapper : "#menu",
                icon : "ui-icon-power",
                route : "#logout"
            },
            divId : "logout"
        });

    },

    showComponent : function (component) {
        /*for (var i in window.app.view.components) {
            var c = window.app.view.components[i];
            if (c == component) continue;
            c.deactivate();
        }*/
        _.each(this.components, function (c) {
            if (c != component) c.deactivate();
        });
        $("#app").show();
        component.activate();

    }
});

ApplicationView.prototype.message =  function(title, message, type, pnotify) {
     ApplicationView.prototype.message(title, message, type, pnotify,true);
}
ApplicationView.prototype.message =  function(title, message, type, pnotify,_history) {
    type = type || 'status';
    message.responseText && (message = message.responseText);
    var stack_bottomright = {"dir1": "up", "dir2": "left", "firstpos1": 15, "firstpos2": 15};
    var opts = {
        pnotify_title: title,
        pnotify_text: message,
        pnotify_notice_icon: "ui-icon ui-icon-info",
        pnotify_type : type,
        pnotify_history: _history
        //pnotify_addclass: "stack-bottomright",
        //pnotify_stack: stack_bottomright
    };
    $.pnotify(opts);

}



