var BrowseImageView = Backbone.View.extend({
    tagName : "div",
    initialize: function(options) {

    },
    render: function() {
        //var tpl = ich.browseimagetpl({}, true);
        //$(this.el).html(tpl);
        var tabs = $(this.el).children('.tabs');
        this.el.tabs("add","#tabs-4","Fourth Tab");
        this.el.css("display","block");
        return this;
    }
});
