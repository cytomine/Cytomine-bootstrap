var AccountDetails = Backbone.View.extend({
    initialize: function(options) {

    },
    events:{

    },
    render: function() {
        var self = this;
        require([
            "text!application/templates/account/AccountDetails.tpl.html"
        ],
                function(tpl) {
                    self.doLayout(tpl);
                });

        return this;
    },

    doLayout : function(tpl) {
        $(this.el).html(_.template(tpl, this.model.toJSON()));
    }
});