var ImageView = Backbone.View.extend({
    tagName : "div",
    initialize: function(options) {
        this.images = null; //array of images that are printed
        this.container = options.container;
        this.page = options.page;
        this.nb_thumb_by_page = 30;
        this.appendingThumbs = false;
        if (this.page == undefined) this.page = 0;
        _.bindAll(this, 'render');
    },
    render: function() {
        var self = this;

        if (window.app.status.currentProjectModel.get("numberOfImages") != 0) {
            $(self.el).empty();
        } else {
            require(["text!application/templates/dashboard/NoImageAvailable.tpl.html"], function (tpl) {
                $(self.el).html(_.template(tpl, { idProject : window.app.status.currentProjectModel.id}))
            });
        }


        self.appendThumbs(self.page);

        $(window).scroll(function(){
            //1. Look if the tabs is active. don't append thumbs if not
            var currentUrl = "" + window.location;
            if (currentUrl.search("#tabs-images-") == -1){
                return;
            }
            //2. Look if we are already appending thumbs. If yes, return
            if (self.appendingThumbs) return;

            if  (($(window).scrollTop() + 50) >= $(document).height() - $(window).height()){
                /*console.log("$(window).scrollTop() : " + $(window).scrollTop());
                 console.log("$(document).height()- $(window).height() " + ($(document).height() - $(window).height()));
                 */


                self.appendThumbs(++self.page);

            }
        });


        return this;
    },
    showLoading : function() {
        window.app.view.message("Loading...", "", "info");
    },
    appendThumbs : function(page) {
        var self = this;
        self.appendingThumbs = true;
        var inf = Math.abs(page) * this.nb_thumb_by_page;
        var sup = (Math.abs(page) + 1) * this.nb_thumb_by_page;
        if (inf > window.app.status.currentProjectModel.get("numberOfImages")) {
            return
        }
        if (Math.abs(page) * this.nb_thumb_by_page < self.model.size() ) {
            this.showLoading();
        }
        self.tabsContent = [];


        self.model = new ImageInstanceCollection({project: window.app.status.currentProject, inf : inf, sup : sup});
        self.model.fetch({
            success : function (collection, response){
                var idDivPage = window.app.status.currentProject + "-image-page-" + self.page;
                if ($("#"+idDivPage).length == 0) { //create page div
                    $(self.el).append(_.template("<div id='<%= idDivPage %>'></div>",{
                        idDivPage :idDivPage
                    }));
                } else { //empty page div
                    $("#"+idDivPage).empty();
                }
                var cpt = 0;
                while (cpt < (sup - inf) && cpt < collection.size()) {
                    var image  = collection.at(cpt);
                    var thumb = new ImageThumbView({
                        model : image
                    }).render();
                    $("#"+idDivPage).append(thumb.el);
                    cpt++;
                    self.tabsContent.push(image.id);
                }
                self.appendingThumbs = false;
            }
        });


    },
    /**
     * Add the thumb image
     * @param image Image model
     */
    add : function(image) {
        var self = this;
        var thumb = new ImageThumbView({
            model : image,
            className : "row",
            id : "thumb"+image.get('id')
        }).render();
        $(self.el).append(thumb.el);

    },
    /**
     * Remove thumb image with id
     * @param idImage  Image id
     */
    remove : function (idImage) {
        $("#thumb"+idImage).remove();
    },
    refresh : function() {
        for (var p = 0; p <= this.page; p++)
            this.appendThumbs(p);
    }
});
