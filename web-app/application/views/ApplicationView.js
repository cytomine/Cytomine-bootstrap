// App
// ---
// View. Represents the entire application "viewport". Available in the global
// namespace as `window.app` and contains various utility methods.
var ApplicationView = Backbone.View.extend({
    tagName : "div",
    className : "layout",
    //template : _.template($('#base-layout-tpl').html()),
    components : {},
    initialize: function(options) {
        
    },
    render: function() {
        $(this.el).html(ich.baselayouttpl({}, true));

        this.initComponents();
        return this;
    },
    initComponents : function() {
        this.components.explorer = new Component({
            el : $("#content"),
            //template : _.template($('#explorer-tpl').html()),
            template : ich.explorertpl({}, true),
            buttonAttr : {
                elButton : "explorer-button",
                buttonText : "Explorer",
                buttonWrapper : $("#menu"),
                icon : "ui-icon-search",
                route : "#explorer"
            },
            divId : "explorer"
        }).render();
        this.components.admin = new Component({
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
        }).render();
        this.components.logout = new Component({
            el : $("#content"),
            //template : _.template($('#logout-tpl').html()),
            template : ich.logouttpl({}, true),
            buttonAttr : {
                elButton : "logout-button",
                buttonText : "Logout",
                buttonWrapper : $("#menu"),
                icon : "ui-icon-power",
                route : "#logout"
            },
            divId : "logout"
        }).render();


        // Display a loading overlay over the page viewport.
        /*loading: function(message) {
         /*this.loadingView = new LoadingView({message: message});
         $(this.el).append(this.loadingView.el);*/
    },
// Remove a loading overlay from the page viewport.
    done: function() {
        console.log("done");
        /*this.loadingView.remove();
         delete this.loadingView;*/
    },
// Display a popup message.
    message: function(title, message, type) {
        type = type || 'status';
        message.responseText && (message = message.responseText);
        var stack_bottomright = {"dir1": "up", "dir2": "left", "firstpos1": 15, "firstpos2": 15};
        var opts = {
            pnotify_title: title,
            pnotify_text: message,
            pnotify_notice_icon: "ui-icon ui-icon-info",
            pnotify_type : type,
            pnotify_addclass: "stack-bottomright",
            pnotify_stack: stack_bottomright
        };
        $.pnotify(opts);

    }
});



