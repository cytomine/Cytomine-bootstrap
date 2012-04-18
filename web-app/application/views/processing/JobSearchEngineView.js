var JobSearchEngineView = Backbone.View.extend({
    width : null,
    software: null,
    project : null,
    parent : null,
    listing : null,
    allJobs : null,
    initialize: function(options) {
        var self = this;
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
        this.listing = options.listing;
        this.allJobs = options.allJobs;
    },
    render : function() {
        var self = this;
        require([
            "text!application/templates/processing/JobSearchEngine.tpl.html"
        ],
               function(JobSearchEngineTpl) {
                   self.loadResult(JobSearchEngineTpl);
               });
        return this;
    },
    loadResult : function (JobSearchEngineTpl) {
        console.log("JobSearchEngineView.loadResult");
        var self = this;
        var content = _.template(JobSearchEngineTpl, {});
        $(self.el).empty();
        $(self.el).append(content);

        $("#searchJobFilterButton").click(function() {
            self.launchSearch();
        })
    },
    launchSearch: function() {
        var self = this;
        console.log("search job");
        var num =  $("#searchJobFilterNumber").val();

        var filterJobs = self.allJobs.models;
        filterJobs = self.searchByNumber(filterJobs, num);

        self.listing.refresh(new JobCollection(filterJobs));

    },
    searchByNumber : function(jobs, num) {
        var filterJobs = [];
        console.log("Before searchByNumber:"+jobs.length);
        _.each(jobs, function(job) {
            console.log(job.get('number')+"|"+num);
             if(job.get('number')==num) {
                 filterJobs.push(job);
             }
        });
        console.log("After searchByNumber:"+filterJobs.length);
        return filterJobs;
    }
});