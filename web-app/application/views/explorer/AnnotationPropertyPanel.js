var AnnotationPropertyPanel = SideBarPanel.extend({
    tagName: "div",
    keyAnnotationProperty: null,

    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function (options) {
        this.browseImageView = options.browseImageView;
        this.callback = options.callback;
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require([
            "text!application/templates/explorer/AnnotationPropertyPanel.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },

    initSelect: function (layer, model) {
        var select = $(this.el).find("#selectLayersAnnotationProperty");
        select.empty();


        new AnnotationPropertyKeys({idImage: '16833'}).fetch({
            success: function (collection, response) {
                keyAnnotationProperty = collection;
                collection.each(function(model) {
                    console.log("ICI");
                })
            }
        });
    },

    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl) {
        var self = this;

        var el = $('#annotationPropertyPanel'+this.model.get('id'));
        el.html(_.template(tpl, {id: this.model.get('id')}));
        var elContent = el.find(".annotationPropertyContent");
        var sourceEvent = el.find(".toggle-content");
        this.initToggle(el, elContent, sourceEvent, "annotationPropertyContent");

        self.initSelect();

    }
});
