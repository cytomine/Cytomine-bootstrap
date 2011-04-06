var Tabs = Backbone.View.extend({
    tagName : "div",
    images : {}, //that we are browsing
    initialize: function(options) {
        this.container = options.container
    },
    render: function() {
        $(this.el).html(ich.taptpl({}, true));
        var tabs = $(this.el).children('.tabs');
        tabs.tabs();
        return this;
    },
    openTab: function(image) {
        console.log("openTab" + image);
      var tabs = $(this.el).children('.tabs');
      this.images.image = new BrowseImageView({
            el: tabs
      }).render();
    }
});
