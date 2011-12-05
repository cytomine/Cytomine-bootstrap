
var DraggablePanelView = Backbone.View.extend({
    tagName : "div",
    initialize: function(options) {
        this.iPad = ( navigator.userAgent.match(/iPad/i) != null );
        this.el = options.el;
        this.template = options.template;
        this.className = options.className;
        /*this.dialogAttr = options.dialogAttr;
         if (!options.dialogAttr.width) this.dialogAttr.width = 'auto';
         if (!options.dialogAttr.height) this.dialogAttr.height = 'auto';*/
    },
    render: function() {

        var self = this;
        $(this.el).html(this.template);
        if (this.iPad) return this;
        var width = $(this.el).width();
        var height = $(this.el).height();
        $(this.el).draggable({
            cancel: 'input',
            start: function(event, ui) {
                width = $(self.el).width();
                height = $(self.el).height();
            },
            stop : function(event, ui) {
                var currentPos = ui.helper.position();
                if (self.className) window.app.view.updatePosition(self.className, { left : parseInt(currentPos.left), top : parseInt(currentPos.top)});
                $(this).css("width", width);
                $(this).css("height", height);
            },
            drag: function (event, ui) {

            }
        });
        $(this.el).draggable( "option", "opacity", 0.35 );
        return this;
    }



});