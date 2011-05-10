var AnnotationThumbView = Backbone.View.extend({

    events: {

    },

    initialize: function(options) {
        this.id = "annotationthumb"+this.model.get('id');
        _.bindAll(this, 'render');
    },

    render: function() {

        $(this.el).html(ich.annotationthumbtpl(this.model.toJSON(), true));
        return this;
    }
});
