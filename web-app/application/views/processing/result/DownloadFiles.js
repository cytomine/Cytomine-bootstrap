var DownloadFiles = Backbone.View.extend({
    project:null,
    terms:null,
    jobs:null,
    software:null,
    initialize:function (options) {
        this.terms = window.app.status.currentTermsCollection;
        this.project = options.project;
        this.jobs = options.jobs;
        this.software = options.software;
    },
    render:function () {
        var self = this;
        require([
            "text!application/templates/processing/DownloadFiles.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl)
            });
        return this;
    },
    doLayout : function(tpl) {
        var self = this;
        var content = _.template(tpl, {});
        $(this.el).append(content);

        new JobDataCollection({ job:self.model.id}).fetch({
               success:function (collection, response) {

                   collection.each(function(data) {
                       $("#jobDataResult").append('<a href="/api/jobdata/'+data.id+'/download" class="btn btn-large btn-primary">'+data.get("filename")+'</a>');
                       $("#jobDataResult").find("#"+data.id).click(function(evt){
                           self.downloadFile(data.id);
                       });
                   });

               }
           });
    },
    downloadFile : function(idJobData) {



    }
});