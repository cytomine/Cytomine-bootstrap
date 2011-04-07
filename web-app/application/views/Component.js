var Component = Backbone.View.extend({
    tagName : "div",
    views : {},
    initialize: function(options) {
        this.divId = options.divId;
        this.el = options.el;
        this.template = options.template;
        this.buttonAttr = options.buttonAttr;
    },
    render: function() {
        $(this.el).append(this.template);
        if (this.buttonAttr.elButton) {
            this.addToMenu();
        }
        return this;
    },
    addToMenu : function () {
        //var button = _.template('<a id="<%= id %>" href="<%= route %>" style="margin-right:5px;"><%= text %></a>', {id : this.buttonAttr.elButton, route : this.buttonAttr.route, text : this.buttonAttr.buttonText});
        var button = ich.menubuttontpl({id : this.buttonAttr.elButton, route : this.buttonAttr.route, text : this.buttonAttr.buttonText}, true);
        $(this.buttonAttr.buttonWrapper).append(button);
        $("#"+this.buttonAttr.elButton).button({icons: {primary:this.buttonAttr.icon}});
        if (this.buttonAttr.click) {
            $("#"+this.buttonAttr.elButton).click(this.buttonAttr.click);
        }
    },
    activate : function () {
        $("#"+this.divId).show();
        $("#"+this.buttonAttr.elButton).addClass("ui-state-disabled");
    },
    deactivate : function () {
        $("#"+this.divId).hide();
        $("#"+this.buttonAttr.elButton).removeClass("ui-state-disabled");
    },
    show : function(view) {
        for (var i in this.views) {
            var v = this.views[i];
            if (v != view) {
                 $(v.el).hide();
            }
        }
        $(view.el).fadeIn('fast');
    }
});

