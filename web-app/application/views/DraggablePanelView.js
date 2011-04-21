
var DraggablePanelView = Backbone.View.extend({
    tagName : "div",

    initialize: function(options) {
        this.el = options.el;
        this.template = options.template;
        /*this.dialogAttr = options.dialogAttr;
        if (!options.dialogAttr.width) this.dialogAttr.width = 'auto';
        if (!options.dialogAttr.height) this.dialogAttr.height = 'auto';*/
    },
    render: function() {
        //$(this.el).html(this.template);
        var self = this;
        var width = $(this.el).width();
        var height = $(this.el).height();
        $(this.el).draggable({
            drag: function (event, ui) {
                $(this).css("width", width);
                $(this).css("height", height);
            }
        });
        /*var dialog = $(this.dialogAttr.dialogID).dialog({
            open: function (e, ui) {
                $(this).closest('.ui-dialog').css(self.dialogAttr.css);
                //$(this).siblings('div.ui-dialog-titlebar').remove();
            },
            resizable: true,
            draggable : true,
            closeText: 'hide',
            width: this.dialogAttr.width,
            height: this.dialogAttr.height,
            closeOnEscape : false,
            modal: false
            //close : this.dialogAttr.close,
            //buttons: this.dialogAttr.buttons
        });*/
        return this;
    }



});