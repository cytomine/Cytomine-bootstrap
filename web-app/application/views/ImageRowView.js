var ImageRowView = Backbone.View.extend({

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
