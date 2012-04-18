var JobSearchView = Backbone.View.extend({
    width : null,
    software: null,
    project : null,
    parent : null,
    initialize: function(options) {
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/processing/JobSearch.tpl.html"
        ],
               function(jobSearchViewTpl) {
                   self.loadResult(jobSearchViewTpl);
               });
        return this;
    },
    loadResult : function (jobSearchViewTpl) {
        console.log("JobSearchView.loadResult");
        var self = this;
        var content = _.template(jobSearchViewTpl, {});
        $(self.el).empty();
        $(self.el).append(content);

        var width = ($(window).width() - 200);
        var height = ($(window).height() - 200);
        $(self.el).dialog({ width: width, height: height, modal:true });

        self.printJobListingPanel();
    },
    printBasicSearchPanel : function() {

    },
    printAdvancedSearchPanel : function() {

    },
    printFilterPanel : function() {

    },
    printJobListingPanel : function() {
        console.log("JobSearchView.printJobListingPanel");
        var self = this;
        new JobCollection({ project : self.project.id, software:  self.software.id}).fetch({
             success : function (collection, response) {
                 new JobTableView({
                     width : self.software,
                     project : self.project,
                     software : self.software,
                     el : $("#jobTablesList"),
                     parent : self,
                     jobs: collection

                 }).render();
             }
         });

    },
    refreshSearch : function() {

    }

});