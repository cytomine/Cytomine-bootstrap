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
                //$("#"+ui.panel.id).attr('style', 'width:100%;height:100%;');
            },
            'show': function(event, ui){
                //$("#"+ui.panel.id).attr('style', 'width:100%;height:100%;');
                return true;
            }
        });
        $("ul.tabs a").css('height', $("ul.tabs").height())



        return this;
    },
    openTab: function(image) {
        var self = this;
        window.models.images.fetch({
            success : function () {
                var tabs = $(self.el).children('.tabs');
                self.images.image = new BrowseImageView({
                    model : window.models.images.get(image),
                    el: tabs
                }).render();
            }
        });

    }
});
