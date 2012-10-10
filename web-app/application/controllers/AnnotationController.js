var AnnotationController = Backbone.Router.extend({

    routes:{
        "annotation":"annotation",
        "annotation/:idAnnotation":"annotation",
        "share-annotation/:idAnnotation":"share",
        "copy-annotation/:idAnnotation":"copy"
    },

    annotation:function (idAnnotation) {
        var self = this;
        if (!self.view) {
            window.app.models.images.fetch({
                success:function (collection, response) {
                    self.view = new AnnotationListView({
                        model:collection,
                        el:$("#warehouse > .annotation"),
                        container:window.app.view.components.warehouse,
                        idAnnotation:idAnnotation //selected annotation
                    }).render();
                    self.view.container.views.annotation = self.view;
                    self.view.container.show(self.view, "#warehouse > .sidebar", "annotation");
                    window.app.view.showComponent(window.app.view.components.warehouse);
                }
            });
        }
    },

    share:function (idAnnotation) {
        new AnnotationModel({id:idAnnotation}).fetch({
            success:function (model, response) {
                new ShareAnnotationView({
                    model:model,
                    image:model.get("image"),
                    project:model.get("project")
                }).render();
            }
        });
    },

    copy:function (idAnnotation) {
        new AnnotationCopyModel({id:idAnnotation}).save({id:idAnnotation}, {
                success:function (model, response) {
                    window.app.view.message("Annotation", response.message, "success");
                },
                error:function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation", json.errors, "error");
                }
            }
        );
        window.history.back();
    }
});