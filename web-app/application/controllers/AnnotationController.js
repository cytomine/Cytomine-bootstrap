var AnnotationController = Backbone.Router.extend({

    routes: {
        "annotation"            :   "annotation",
        "annotation/:idAnnotation"           :   "annotation",
        "share-annotation/:idAnnotation" : "share"
    },

    annotation : function(idAnnotation) {
        var self = this;
        if (!self.view) {
            window.app.models.images.fetch({
                success : function (collection, response) {
                    self.view = new AnnotationListView({
                        model : collection,
                        el:$("#warehouse > .annotation"),
                        container : window.app.view.components.warehouse,
                        idAnnotation : idAnnotation //selected annotation
                    }).render();
                    self.view.container.views.annotation = self.view;
                    self.view.container.show(self.view, "#warehouse > .sidebar", "annotation");
                    window.app.view.showComponent(window.app.view.components.warehouse);
                }
            });
        }
    },

    share : function(idAnnotation) {
        new AnnotationModel({id : idAnnotation}).fetch({
            success : function (model, response) {
                new ShareAnnotationView({
                    model : model,
                    image : model.get("image"),
                    project : model.get("project")
                }).render();
            }
        });
    }
});