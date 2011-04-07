var Tabs = Backbone.View.extend({
    tagName : "div",
    images : {}, //that we are browsing
    initialize: function(options) {
        this.container = options.container
    },
    render: function() {
        $(this.el).html(ich.taptpl({}, true));
        var tabs = $(this.el).children('.tabs');
        tabs.tabs({
            add: function(event, ui) {
                tabs.tabs('select', '#' + ui.panel.id);
            },
            'show': function(event, ui){
                $(ui.panel).attr('style', 'width:100%;height:100%;');
                return true;
            }
        });
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
