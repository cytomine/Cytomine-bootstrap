var ReviewTermListing = Backbone.View.extend({
    width: null,
    software: null,
    project: null,
    parent: null,
    params: [],
    paramsViews: [],
    initialize: function (options) {
        this.container = options.container;
    },
    render: function () {
        var self = this;
        self.addTerm();
    },
    addTerm : function() {
        var self = this;
        self.model.each(function(term) {


           // $(self.el).append('<div class="span1" id="termDrop'+term.id+'">'+term.get('name')+'</div>')

            $(self.el).append('<div data-term="'+term.id+'" style="border-width: 3px; border-style: solid; border-color: '+term.get('color')+';min-width : 175px; min-height: 175px;text-align: center;margin-bottom: 15px;margin-top: 15px;margin-left: 15px;margin-right: 15px;" class="span1 termChoice" id="termDrop'+term.id+'">'+term.get('name')+'</div>');
        });
        $( "#termReviewChoice" ).sortable();
        $( "#termReviewChoice" ).disableSelection();
    }
});