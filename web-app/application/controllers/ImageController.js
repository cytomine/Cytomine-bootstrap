
var ImageController = Backbone.Controller.extend({

    routes: {
        "image"            :   "image",
        "image/p:page"     :   "image"
    },

    image : function(page) {
        console.log("request page "+ page);
        if (!this.view) {

            this.view = new ImageView({
                page : page,
                model : new ImageCollection(),
                el:$("#explorer > .image"),
                container : window.app.components.explorer
            }).render();

            this.view.container.views.image = this.view;
        }

        this.view.container.show(this.view);
    }


});