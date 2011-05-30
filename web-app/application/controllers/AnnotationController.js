/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:05
 * To change this template use File | Settings | File Templates.
 */

var AnnotationController = Backbone.Controller.extend({



    routes: {
        "annotation"            :   "annotation",
        "annotation/:idAnnotation"           :   "annotation"
    },

    annotation : function(idAnnotation) {
        var self = this;
        console.log("AnnotationController:"+idAnnotation);
        if (!self.view) {
            console.log("Annotation controller");
            window.app.models.annotations.fetch({
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


    }


});