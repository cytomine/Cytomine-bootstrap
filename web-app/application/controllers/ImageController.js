
var ImageController = Backbone.Controller.extend({



    routes: {
        "image"            :   "image",
        "image/p:page"     :   "image"
    },

    image : function(page) {
        if (!this.view) {
                              console.log("new imageview");
            this.view = new ImageView({
                page : page,
                model : window.models.images,
                el:$("#warehouse > .image"),
                container : window.app.components.warehouse
            }).render();

            this.view.container.views.image = this.view;
        }

        this.view.container.show(this.view, "#warehouse > .sidebar", "image");
    }


});