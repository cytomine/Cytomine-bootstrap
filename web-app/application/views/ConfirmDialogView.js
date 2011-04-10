
var ConfirmDialogView = Backbone.View.extend({
    tagName : "div",

    initialize: function(options) {
        this.el = options.el;
        this.template = options.template;
        this.dialogAttr = options.dialogAttr;
        if (!options.dialogAttr.width) this.dialogAttr.width = 'auto';
        if (!options.dialogAttr.height) this.dialogAttr.height = 'auto';
    },
    render: function() {
        $(this.el).html(this.template);
        var dialog = $(this.dialogAttr.dialogID).dialog({
            resizable: false,
            draggable : false,
            width: this.dialogAttr.width,
            height: this.dialogAttr.height,
            closeOnEscape : true,
            modal: true,
            /*close : this.dialogAttr.close,*/
            buttons: this.dialogAttr.buttons
        });
        return this;
    }



});