var ImageThumbView = Backbone.View.extend({

    events: {
        "click"             : "open"
    },

    open : function () {
        console.log("open it :-)" + this.model.get("filename")); //work only in firefox
    },

    initialize: function(options) {
        _.bindAll(this, 'render');
    },

    render: function() {
        $(this.el).html(ich.imagethumbtpl(this.model.toJSON(), true));
        return this;
    }
});
