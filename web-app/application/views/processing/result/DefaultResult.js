var DefaultResult = Backbone.View.extend({
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
            "text!application/templates/processing/DefaultResult.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl)
            });
        return this;
    },
    doLayout:function (tpl) {
        var content = _.template(tpl, {});
        $(this.el).append(content);
    }
});