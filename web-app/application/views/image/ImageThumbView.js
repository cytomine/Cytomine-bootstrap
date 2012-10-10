var ImageThumbView = Backbone.View.extend({
    className:"thumb-wrap",
    tplProperties:null,

    events:{

    },

    initialize:function (options) {
        this.id = "thumb" + this.model.get('id');
        _.bindAll(this, 'render');
    },

    render:function () {
        this.model.set({ project:window.app.status.currentProject });
        var self = this;
        require(["text!application/templates/image/ImageThumb.tpl.html", "text!application/templates/image/ImageThumbProperties.tpl.html"], function (tpl, tplProperties) {
            self.tplProperties = tplProperties;
            var filename = self.model.get('filename');
            var title = (filename.length < 27) ? filename : filename.substr(0, 24) + "...";
            var resolution = Math.round(1000 * self.model.get('resolution')) / 1000; //round to third decimal
            self.model.set({title:title, resolution:resolution});
            $(self.el).html(_.template(tpl, self.model.toJSON()));
            $(self.el).find("#image-properties-" + self.model.id).html(_.template(tplProperties, self.model.toJSON()));
            $(self.el).find("#getImageProperties-" + self.model.id).on("click", function () {
                $("#image-properties").remove();
                new ImagePropertiesView({model:self.model}).render();
                return false;
            });


        });
        return this;
    },

    refresh:function () {
        $(this.el).find("#image-properties-" + this.model.id).html(_.template(this.tplProperties, this.model.toJSON()));
    }
});

