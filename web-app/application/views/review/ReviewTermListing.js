var ReviewTermListing = Backbone.View.extend({
    width: null,
    software: null,
    project: null,
    parent: null,
    params: [],
    paramsViews: [],
    initialize: function (options) {

    },
    render: function () {
        var self = this;
        self.addTerm();
    },
    addTerm : function() {
        var self = this;
        self.model.each(function(term) {
            //$(self.el).append('<div class="span1" id="termDrop'+term.id+'">'+term.get('name')+'</div>')
            $(self.el).append('<div style="border-width: 3px; border-style: solid; border-color: '+term.get('color')+';width : 100px; height: 100px;position: relative;text-align: center;" class="span1" id="termDrop'+term.id+'">'+term.get('name')+'</div>');
        });
    }
});