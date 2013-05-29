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
        self.sortWithOrder();
        self.model.each(function(term) {

            var spanText = _.template(self.container.termSpanTemplate,term.toJSON());
            $(self.el).append('<div data-term="'+term.id+'" style="background-color: '+term.get('color')+';border-width: 3px; border-style: solid; border-color: rgb(183, 177, 177);min-width : 125px; min-height: 175px;text-align: center;margin-bottom: 15px;margin-top: 15px;margin-left: 15px;margin-right: 15px;" class="span1 termChoice" id="termDrop'+term.id+'">'+spanText+'</div>');
        });
        $(self.el).find("div>span").css("padding","0px");

        $( "#termReviewChoice" ).sortable({update:function() {
              self.saveActualTermOrder();

        }});
        $( "#termReviewChoice" ).disableSelection();
    },
    sortWithOrder : function() {
        var self = this;
        var value = $.cookie('review-order-'+window.app.status.currentProjectModel.get('ontology'));
        if(value!=undefined) {
            var array = _.sortBy(self.model.models, function(item){ return value.indexOf(item.get('id')); });
            self.model.models = array;
        }

    },
    saveActualTermOrder : function() {
        var self = this;


        var orderTerm = _.map($(self.el).find('div'), function(elem) {
             return $(elem).data('term');
                });

        console.log(orderTerm);

        $.cookie('review-order-'+window.app.status.currentProjectModel.get('ontology'), orderTerm);
    }
});