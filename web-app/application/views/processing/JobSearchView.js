var JobSearchView = Backbone.View.extend({
    width:null,
    software:null,
    project:null,
    parent:null,
    initialize:function (options) {
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
    },
    render:function () {
        var self = this;
        require([
            "text!application/templates/processing/JobSearch.tpl.html"
        ],
                function (jobSearchViewTpl) {
                    self.loadResult(jobSearchViewTpl);
                });
        return this;
    },
    loadResult:function (jobSearchViewTpl) {
        console.log("JobSearchView.loadResult");
        var self = this;
        var content = _.template(jobSearchViewTpl, {});
        $(self.el).empty();
        $(self.el).append(content);

        var width = ($(window).width() - 200);
        var height = ($(window).height() - 200);
        $(self.el).dialog({
            width:width,
            height:height,
            modal:true,
            buttons:[
                {
                    text:"Close",
                    click:function () {
                        $(this).dialog("close");
                    }
                }
            ], close:function (event, ui) {
                $(self.el).empty();
            }
        });

        self.printJobListingPanel();
    },
    printBasicSearchPanel:function () {

    },
    printAdvancedSearchPanel:function () {

    },
    printFilterPanel:function () {

    },
    printJobListingPanel:function () {
        console.log("JobSearchView.printJobListingPanel");
        var self = this;

        if (window.app.models.projects == undefined || (window.app.models.projects.length > 0 && window.app.models.projects.at(0).id == undefined)) {
            window.app.models.projects = new ProjectCollection();
            window.app.models.projects.fetch({
                success:function (collection, response) {
                    self.openJobListing();
                }
            });
        } else {
            self.openJobListing();
        }
    },
    openJobListing:function () {
        var self = this;
        new JobCollection({ project:self.project.id, software:self.software.id}).fetch({
            success:function (collection, response) {
                var listing = new JobTableView({
                    width:self.software,
                    project:self.project,
                    software:self.software,
                    el:$("#jobTablesList"),
                    parent:self,
                    jobs:collection

                }).render();
                console.log("listing1:" + listing);
                new JobSearchEngineView({
                    width:self.software,
                    project:self.project,
                    software:self.software,
                    el:$("#jobFilterList"),
                    parent:self,
                    listing:listing,
                    allJobs:collection
                }).render();

            }
        });
    },
    refreshSearch:function () {

    }

});