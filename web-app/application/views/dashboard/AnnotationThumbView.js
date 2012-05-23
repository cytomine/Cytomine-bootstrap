var AnnotationThumbView = Backbone.View.extend({

    events: {

    },

    initialize: function(options) {
        this.term = options.term;
        _.bindAll(this, 'render');
    },

    render: function() {
        var self = this;
        require(["text!application/templates/dashboard/AnnotationThumb.tpl.html"], function(tpl) {
            var annotationJSON = self.model.toJSON();
            annotationJSON.sameUser = (window.app.status.user.id==annotationJSON.user);
            $(self.el).html(_.template(tpl, annotationJSON));
            $(self.el).attr("data-annotation", self.model.get("id"));
            if (self.term != undefined) $(self.el).attr("data-term", self.term);

            //POPOVER
            /*
            var thumb = $(self.el).find(".thumb");
            var popoverTitle = _.template("<%= name %>", {name : self.term.get('name')});
            var popoverContent = _.template("<%= name %>", {name : self.term.get('name')});
            thumb.attr("data-original-title", popoverTitle);
            thumb.attr("data-content", popoverContent);
            thumb.popover({
                html : true,
                placement : "below"
            });*/
            self.initDraggable();
        });
        return this;
    },

    initDraggable : function () {
        var self = this;
        $(self.el).draggable({
            scroll : true,
            //scrollSpeed : 00,
            revert: true,
            delay : 500,
            opacity: 0.35,
            cursorAt : { top : 85, left : 90},
            start : function(event, ui) {
                var thumb = $(self.el).find(".thumb");
                thumb.popover('hide');
            },
            stop : function(event, ui) {

            }

        });
    }
});
