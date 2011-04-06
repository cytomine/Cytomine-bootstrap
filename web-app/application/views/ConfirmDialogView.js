
var ConfirmDialogView = Backbone.View.extend({
    tagName : "div",

    initialize: function(options) {
        this.el = options.el;
        this.template = options.template;
        this.dialogAttr = options.dialogAttr;
    },
    render: function() {
        $(this.el).html(this.template);
        var dialog = $(this.dialogAttr.dialogID).dialog({
            resizable: false,
            draggable : false,
            closeOnEscape : true,
            modal: true,
          //  close : this.dialogAttr.close,
            buttons: this.dialogAttr.buttons
        });
        return this;
    }



});