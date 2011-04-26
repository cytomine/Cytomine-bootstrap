/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 26/04/11
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
var ProjectPanelView = Backbone.View.extend({
    tagName : "div",
    loadImages : true, //load images from server or simply show/hide images
    project : null,
    projectElem : "#projectlist",
    imagesListElem :  "#imagelistproject",
    //template : _.template($('#project-view-tpl').html()),
    initialize: function(options) {
        this.container = options.container;
         _.bindAll(this, 'render');
    },
    openImagesList: function(idProject) {
        var self = this;
        console.log("click open image:"+idProject);

        if(!this.loadImages) {
            //images are already loaded
            console.log("CLOSE");
            $(self.imagesListElem+idProject).toggle(1000); //toggle(time) doesn't work with isotope?
            return;
        }
        console.log("OPEN");

        this.loadImages = false;//don't load again images
        var page = 0
        var tpl = ich.imageprojectviewtpl({page : (Math.abs(page)+1), id : idProject}, true);

        console.log(self.projectElem+idProject);
        $(self.projectElem+idProject).append(tpl);

        //$("#imagelistproject"+id).hide();
        new ImageCollection({project:idProject}).fetch({
            success: function(collection,response){
                var cpt = 0;
                var nb_thumb_by_page = 21;
                var inf = Math.abs(page) * nb_thumb_by_page;
                var sup = (Math.abs(page) + 1) * nb_thumb_by_page;
                console.log("Model size=" + collection.length);
                collection.each(function(image) {
                    if ((cpt > inf) && (cpt <= sup)) {
                        var thumb = new ImageThumbView({
                            model : image
                        }).render();
                        $(self.imagesListElem + idProject).append(thumb.el);
                    }
                    cpt++;
                });

                $(self.imagesListElem+idProject).imagesLoaded( function(){
                    $(self.imagesListElem+idProject).isotope({
                        itemSelector: '.thumb-wrap'
                    });

                });
                //$("#imagelistproject"+id).show(1000);
                $(self.imagesListElem+idProject).attr("title","open");
            },
            error: function(error){
                for (property in error) {
                    console.log(property + ":" + error[property]);
                }
            }
        });

    },
    render: function() {
        var self = this;
        self.project = this.model;
        console.log(self.project.toJSON());
        var json = self.project.toJSON();

        //Get ontology name
        new OntologyModel({id:json.ontology}).fetch({success : function (ontology,response) {
            console.log("ontology name:" + ontology.get('name'));
            json.ontology = ontology.get('name');

            var proj = ich.projectviewtpl(json);
            $(self.el).append(proj);

            $("#projectopenimages"+self.project.get('id')).click(function () {

                self.openImagesList(self.project.get('id'));

            });

            $("#projectchange"+self.project.get('id')).click(function () {

                console.log("change project");
                console.log(window.app.controllers.browse.imagesOpen);

            });
        }
        });

        return this;
    }
});
