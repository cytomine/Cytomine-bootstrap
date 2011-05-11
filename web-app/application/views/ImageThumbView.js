var ImageThumbView = Backbone.View.extend({

    events: {

    },

    initialize: function(options) {
        this.id = "thumb"+this.model.get('id');
        _.bindAll(this, 'render');
    },

    render: function() {
        this.model.set({ project : window.app.status.currentProject });
        $(this.el).html(ich.imagethumbtpl(this.model.toJSON(), true));
        return this;
    }
});

var ImageSelectView = Backbone.View.extend({

    events: {

    },

    initialize: function(options) {
        this.id = "thumb"+this.model.get('id');
        _.bindAll(this, 'render');
    },

    render: function() {
        this.model.set({ project : window.app.status.currentProject });
        $(this.el).html(ich.imageselecttpl(this.model.toJSON(), true));
        return this;
    }
});
