
var ImageController = Backbone.Controller.extend({



    routes: {
        "image"            :   "image",
        "image/p:page"     :   "image"
    },

    image : function(page) {
        /*   $("#explorer > .sidebar").find("a[class=title]").removeClass("active");
        $("#explorer > .sidebar").find("a[name=image]").addClass("active");*/
        if (!this.view) {

            this.view = new ImageView({
                page : page,
                model : window.models.images,
                el:$("#explorer > .image"),
                container : window.app.components.explorer
            }).render();

            this.view.container.views.image = this.view;
        }

        this.view.container.show(this.view, "#explorer > .sidebar", "image");
    }


});