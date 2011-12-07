var ActivityView = Backbone.View.extend({

    initialize: function(options) {
        this.el = options.el
    },

    render : function() {
        var self = this;
        require([
            "text!application/templates/activity/ActivityView.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });
    },

    doLayout: function (tpl) {
        var self = this;
        $(self.el).html(tpl);
        var divTarget = $("#activity-content");
        divTarget.empty();
        new CommandCollection().fetch({
            success : function (collection, response) {
                collection.each(function (commandHistory){
                    var commandHTML = _.template("<li><%= message %></li>", { message : commandHistory.get("command").action});
                    $(self.el).append(commandHTML);
                });

            }
        });
    }
});