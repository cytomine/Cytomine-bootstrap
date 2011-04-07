/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
var OntologyView = Backbone.View.extend({
    tagName : "div",
    // template : _.template($('#image-view-tpl').html()),
    initialize: function(options) {
        this.container = options.container;
    },
    render: function() {
        console.log("OntologyView.render");

        var self = this;
        var tpl = ich.ontologyviewtpl({}, true);
        $(this.el).html(tpl);
        console.log("html");
        this.model.fetch({
            success: function(){
                console.log("Success");
                console.log("Model size=" + self.model.length);

                 var json = self.model.toJSON();
                 console.log("json="+JSON.stringify(json));


                    $(function () {
                        $("#ontologytree").jstree({
                            "json_data" : {
                                "data" :json
                            },
                            "plugins" : ["json_data", "ui","themeroller"]

                        });
                    });


            },
            error: function(error){
                for (property in error) {
                    console.log('error:'+property + ":" + error[property]);
                }
            }
        });




        return this;
    }
});
