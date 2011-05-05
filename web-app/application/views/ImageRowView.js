var ImageRowView = Backbone.View.extend({

    tagName: "div",
    className : "",
    events: {

    },

    initialize: function(options) {
        _.bindAll(this, 'render');
    },

    render: function() {
        $(this.el).html(ich.imagerowtpl(this.model.toJSON(), true));
        return this;
    }
});
