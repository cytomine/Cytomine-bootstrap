var AnnotationThumbView = Backbone.View.extend({

    events: {

    },

    initialize: function(options) {
        this.idTerm = options.idTerm;
        _.bindAll(this, 'render');
    },

    render: function() {
        var json = this.model.toJSON();
        var self = this;
        require(["text!application/templates/dashboard/AnnotationThumb.tpl.html"], function(tpl) {
            $(self.el).html(_.template(tpl, json));
            $(self.el).attr("data-annotation", self.model.get("id"));
            $(self.el).attr("data-term", self.idTerm);
            self.initDraggable();
        });
        return this;
    },

    initDraggable : function () {
        var self = this;
        $(self.el).draggable({
            scroll : true,
            //scrollSpeed : 100,
            revert: true,
            opacity: 0.35,
            delay : 500,
            cursorAt : { top : 85, left : 90},
            start : function(event, ui) {
            },
            stop : function(event, ui) {
            }

        });
    }
});
