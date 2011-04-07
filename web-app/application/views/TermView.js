/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/04/11
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
var TermView = Backbone.View.extend({
    tagName : "div",
    // template : _.template($('#image-view-tpl').html()),
    initialize: function(options) {
        this.container = options.container;
        this.ontology = options.ontology;
        if (this.ontology == undefined) this.ontology = -1;
    },
    render: function() {
        console.log("TermView.render");

        /*$(this.el).html(ich.termviewtpl({}, true));
         var self = this;

         return this;         */
        var self = this;
        var tpl = ich.termviewtpl({ontology:self.ontology}, true);
        $(this.el).html(tpl);
        console.log("html");
        this.model.fetch({
            success: function(){
                console.log("Success");
                console.log("Model size=" + self.model.length);
                /*self.model.each(function(term) {
                    console.log("render item");
                    $(self.el).append(term.get('id') + "<br>");
                    });*/

                /*$(self.el).imagesLoaded( function(){
                 $(self.el).isotope({
                 itemSelector: '.thumb-wrap'
                 });

                 }); */

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
