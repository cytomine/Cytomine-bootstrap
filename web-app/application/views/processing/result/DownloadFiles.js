var DownloadFiles = Backbone.View.extend({
    project: null,
    terms: null,
    jobs: null,
    software: null,
    initialize: function (options) {
        this.terms = window.app.status.currentTermsCollection;
        this.project = options.project;
        this.jobs = options.jobs;
        this.software = options.software;
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/processing/DownloadFiles.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl)
            });
        return this;
    },
    doLayout: function (tpl) {
        var self = this;
        var content = _.template(tpl, {});
        $(this.el).append(content);


        var refresh = function () {
            new JobDataCollection({ job: self.model.id}).fetch({
                success: function (collection, response) {
                    $("#jobDataResult").find('tbody').empty();
                    collection.each(function (data) {
                        console.log("data=" + data + " " + collection.length);
                        $("#jobDataResult").find('tbody').append('<tr id="' + data.id + '"></tr>');
                        var row = $("#jobDataResult").find('tbody').find("tr#" + data.id);
                        row.append('<td>' + data.get("filename") + '</td>');
                        row.append('<td>' + data.get("key") + '</td>');
                        row.append('<td>' + self.convertSize(data.get("size")) + '</td>');
                        row.append('<td><a href="/api/jobdata/' + data.id + '/view" class="label label-info">' + data.get("filename") + '</a></td>');
                        row.append('<td><a href="/api/jobdata/' + data.id + '/download" class="label label-info">' + data.get("filename") + '</a></td>');
                    });

                }
            });
        };
        refresh();
        var interval = setInterval(refresh, 5000);
        $(window).bind('hashchange', function () {
            clearInterval(interval);
        });


    },
    convertSize: function (bytes) {
        var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
        if (bytes == 0) {
            return 'n/a';
        }
        var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
        return Math.round(bytes / Math.pow(1024, i), 2) + ' ' + sizes[i];
    }
});