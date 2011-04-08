var ImageThumbView = Backbone.View.extend({

    events: {

    },

    initialize: function(options) {
        _.bindAll(this, 'render');
    },

    render: function() {
        $(this.el).html(ich.imagethumbtpl(this.model.toJSON(), true));
        return this;
    }
});
