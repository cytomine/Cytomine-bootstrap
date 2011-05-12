var AnnotationThumbView = Backbone.View.extend({

    events: {

    },

    initialize: function(options) {
        this.id = "annotationthumb"+this.model.get('id');
        _.bindAll(this, 'render');
    },

    render: function() {
        var json = this.model.toJSON();
        json.project = window.app.status.currentProject;
        $(this.el).html(ich.annotationthumbtpl(json, true));
        return this;
    }
});
