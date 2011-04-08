var Tabs = Backbone.View.extend({
    tagName : "div",
    images : {}, //that we are browsing
    initialize: function(options) {
        this.container = options.container
    },
    render: function() {
        $(this.el).html(ich.taptpl({}, true));
        var tabs = $(this.el).children('.tabs');
        var height = 0;
        tabs.tabs({
            add: function(event, ui) {
                tabs.tabs('select', '#' + ui.panel.id);
                $("#"+ui.panel.id).parent().parent().css('height', "100%");
                $("#"+ui.panel.id).attr('style', 'width:100%;height:100%;');
            },
            'show': function(event, ui){

                //$("#"+ui.panel.id).attr('style', 'width:100%;height:100%;');
                return true;
            },
            select: function(event, ui) {
                return true;
            }
        });

        $("ul.tabs a").css('height', $("ul.tabs").height())



        return this;
    },
    addTab : function(idImage) {
        var self = this;
        if (self.images[idImage] != undefined) {
            console.log("Already exists : " + idImage);
            return; //already added
        }
        window.models.images.fetch({
            success : function () {
                var tabs = $(self.el).children('.tabs');
                self.images[idImage] = new BrowseImageView({
                    model : window.models.images.get(idImage),
                    el: tabs
                }).render();
            }
        });
    },
    showTab : function(idImage) {
        var tabs = $(this.el).children('.tabs');
        tabs.tabs('select', '#tabs-' + idImage);
    }
});
