var Tabs = Backbone.View.extend({
    tagName : "div",
    images : [], //that we are browsing
    initialize: function(options) {
        this.container = options.container
    },
    render: function() {
        var self = this;
        $(this.el).html(ich.taptpl({}, true));
        var tabs = $(this.el).children('.tabs');
        var height = 0;
        tabs.tabs({
            add: function(event, ui) {
                tabs.tabs('select', '#' + ui.panel.id);
                $("#"+ui.panel.id).parent().parent().css('height', "100%");
                $("#"+ui.panel.id).attr('style', 'width:100%;height:100%;');
                $("a[href=#"+ui.panel.id+"]").parent().append("<span class='ui-icon ui-icon-close'>Remove Tab</span>");
                $("a[href=#"+ui.panel.id+"]").parent().find("span.ui-icon-close" ).click(function() {
                    var index = $( "li", tabs ).index( $( this ).parent() );
                    self.removeTab(index);
                });
            },
            show: function(event, ui){

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
        var alreadyOpen = _.detect(self.images, function(object) {
            return object.idImage == idImage;
        });
        if (alreadyOpen) {
            return;
        }
        new ImageModel({id : idImage}).fetch({
            success : function(model, response) {
                var tabs = $(self.el).children('.tabs');
                var view = new BrowseImageView({
                    model : model,
                    el: tabs
                }).render();
                self.images.push({
                    idImage : idImage,
                    browImageView : view
                });
            }
        });
    },
    removeTab : function (index) {
        this.images.splice(index,1);
        var tabs = $(this.el).children('.tabs');
        tabs.tabs( "remove", index);
    },
    showTab : function(idImage) {
        var tabs = $(this.el).children('.tabs');
        tabs.tabs('select', '#tabs-' + idImage);
    },
    size : function() {
        return _.size(this.images);
    },
    closeAll : function() {
        var self = this;
        while (this.size() > 0) {
            self.removeTab(0);
        }
    }
});
