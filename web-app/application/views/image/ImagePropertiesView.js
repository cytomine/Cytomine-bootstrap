var ImagePropertiesView = Backbone.View.extend({
    tagName: "div",

    initialize: function (options) {
    },
    doLayout: function (tpl) {
        var self = this;
        this.dialog = new ConfirmDialogView({
            el: '#dialogs',
            template: _.template(tpl, this.model.toJSON()),
            dialogAttr: {
                dialogID: "#image-properties"
            }
        }).render();
        $("#closeImagePropertiesDialog").click(function (event) {
            event.preventDefault();
            self.dialog.close();
            return false;
        });

        return this;
    },
    render: function () {
        var self = this;
        require(["text!application/templates/image/ImageProperties.tpl.html"], function (tpl) {
            self.doLayout(tpl);
            self.printProperties();
        });
        return this;
    },
    printProperties: function () {
        var self = this;

        require(["text!application/templates/image/ImageProperty.tpl.html"], function (tpl) {
            new ImagePropertyCollection({image: self.model.get("baseImage")}).fetch({
                success: function (collection, response) {
                    var target = $("#image-properties-content");
                    target.empty();
                    collection.sort();
                    collection.each(function (model) {
                        target.append(_.template(tpl, {key: model.get("key"), value: model.get("value")}));
                    });
                }
            });
        });

    }

});