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

        $("#searchJobFilterDateAfter").datepicker({
            numberOfMonths: 3,
            showButtonPanel: true
        });

        $("#searchJobFilterDateBefore").datepicker({
            numberOfMonths: 3,
            showButtonPanel: true
        });

        $("#searchJobFilterStatusNotLaunch").append('<span class="label btn-inverse">Not Launch!</span>');
        $("#searchJobFilterStatusInQueue").append('<span class="label btn-info">In queue!</span>');
        $("#searchJobFilterStatusRunning").append('<span class="label btn-primary">Running!</span>');
        $("#searchJobFilterStatusSuccess").append('<span class="label btn-success">Success!</span>');
        $("#searchJobFilterStatusFailed").append('<span class="label btn-danger">Failed!</span>');
        $("#searchJobFilterStatusIndetereminate").append('<span class="label btn-inverse">Indetereminate!</span>');
        $("#searchJobFilterStatusWait").append('<span class="label btn-warning">Wait!</span>');

        $("#searchJobFilterButton").click(function() {
            self.launchSearch();
        });


    },
    launchSearch: function() {
        var self = this;
        console.log("search job");
        var filterJobs = self.allJobs.models;
        filterJobs = self.searchById(filterJobs, $("#searchJobFilterId").val());
        filterJobs = self.searchByNumber(filterJobs, $("#searchJobFilterNumber").val());
        filterJobs = self.searchByCreationDateAfter(filterJobs, $("#searchJobFilterDateAfter").datepicker( "getDate" ));
        filterJobs = self.searchByCreationDateBefore(filterJobs, $("#searchJobFilterDateBefore").datepicker( "getDate" ));
        filterJobs = self.searchByStatus(filterJobs, 0, $("#searchJobFilterStatusNotLaunchCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 1, $("#searchJobFilterStatusInQueueCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 2, $("#searchJobFilterStatusRunningCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 3, $("#searchJobFilterStatusSuccessCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 4, $("#searchJobFilterStatusFailedCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 5, $("#searchJobFilterStatusIndetereminateCheck").is(':checked'));
        filterJobs = self.searchByStatus(filterJobs, 6, $("#searchJobFilterStatusWaitCheck").is(':checked'));
        self.listing.refresh(new JobCollection(filterJobs));

    },
    searchById : function(jobs, num) {
        console.log("Before searchById:"+jobs.length +" value="+num);
        if(num==undefined || num==null || num.trim()=="") return jobs;
        var filterJobs = [];

        _.each(jobs, function(job) {
            console.log(job.get('id')+"|"+num);
             if(job.get('id')==num) {
                 filterJobs.push(job);
             }
        });
        console.log("After searchById:"+filterJobs.length);
        return filterJobs;
    },
    searchByNumber : function(jobs, num) {
        console.log("Before searchByNumber:"+jobs.length +" value="+num);
        if(num==undefined || num==null || num.trim()=="") return jobs;
        var filterJobs = [];
        _.each(jobs, function(job) {
            console.log(job.get('number')+"|"+num);
             if(job.get('number')==num) {
                 filterJobs.push(job);
             }
        });
        console.log("After searchByNumber:"+filterJobs.length);
        return filterJobs;
    },
    searchByCreationDateAfter : function(jobs, datetime) {
        console.log("Before searchByCreationDateAfter:"+jobs.length +" datetime="+datetime);
        if(datetime==undefined || datetime==null) return jobs;
        var filterJobs = [];
        _.each(jobs, function(job) {
            console.log(job.get('created')+"|"+datetime.getTime());
             if(job.get('created')>=datetime.getTime()) {
                 filterJobs.push(job);
             }
        });
        console.log("After searchByCreationDateAfter:"+filterJobs.length);
        return filterJobs;
    },
    searchByCreationDateBefore : function(jobs, datetime) {
        console.log("Before searchByCreationDateBefore:"+jobs.length +" datetime="+datetime);
        if(datetime==undefined || datetime==null) return jobs;
        var filterJobs = [];
        _.each(jobs, function(job) {
            console.log(job.get('created')+"|"+datetime.getTime());
             if(job.get('created')<datetime.getTime()) {
                 filterJobs.push(job);
             }
        });
        console.log("After searchByCreationDateBefore:"+filterJobs.length);
        return filterJobs;
    },
    searchByStatus : function(jobs, status, check) {
        console.log("Before searchByStatus:"+jobs.length +" status="+status + " check="+check) ;
        if(check) return jobs; //if check, include jobs from this status so no filter
        var filterJobs = [];
        _.each(jobs, function(job) {
            console.log(job.get('status')+"|"+status);
             if(job.get('status')!=status) {
                 filterJobs.push(job);
             }
        });
        console.log("After searchByStatus:"+filterJobs.length);
        return filterJobs;

    }
});