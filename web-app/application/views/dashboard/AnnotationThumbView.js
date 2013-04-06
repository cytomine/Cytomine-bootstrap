var AnnotationThumbView = Backbone.View.extend({

    events: {

    },

    initialize: function (options) {
        this.term = options.term;
        _.bindAll(this, 'render');
    },
    render: function () {
        var self = this;
        require(["text!application/templates/dashboard/AnnotationThumb.tpl.html", "text!application/templates/dashboard/AnnotationPopOverThumb.tpl.html"], function (tpl, popoverTpl) {
            var annotation = self.model.clone();

            var ratePourcent = "";
            if (annotation.get("rate") != undefined && !isNaN(annotation.get("rate"))) {
                ratePourcent = Math.round(annotation.get("rate") * 10000) / 100 + "%";
            }

            var colorStyle = undefined;
            if (annotation.get("idTerm") == annotation.get("idExpectedTerm")) {
                colorStyle = "#cccccc";
            } else if (annotation.get("idTerm") != annotation.get("idExpectedTerm")) {
                colorStyle = "#F89406"
            }



            //if user job, construct link to the job
            var jobLink = null;
            var userJob = window.app.models.projectUserJob.get(annotation.get("user"));

            if (userJob) {
                jobLink = _.template("#tabs-algos-<%= idProject %>-<%= idSoftware%>-<%= idJob %>", {
                    idProject: window.app.status.currentProject,
                    idSoftware: userJob.get("idSoftware"),
                    idJob: userJob.get("idJob")
                });
            }

            annotation.set({
                sameUser: (window.app.status.user.id == annotation.get("user")),
                ratePourcent: ratePourcent,
                colorStyle: colorStyle,
                jobLink: jobLink,
                nbComments: annotation.get("nbComments")!=undefined ? annotation.get("nbComments") : 0
            });


            $(self.el).html(_.template(tpl, annotation.toJSON()));
            $(self.el).attr("data-annotation", annotation.get("id"));
            if (self.term != undefined) {
                $(self.el).attr("data-term", self.term);
            }


            //POPOVER
            //See if algo suggest something and agrees
            var userByTerm = []
            _.each(annotation.get("userByTerm"), function (it) {
                var termName = window.app.status.currentTermsCollection.get(it.term).get('name');
                var userName = [];
                _.each(it.user, function (userID) {
                    userName.push(window.app.view.getUserNameById(userID));
                })
                userByTerm.push({
                    userName: userName.join(" and "),
                    termName: termName
                });
            });

            var termName = undefined;
            var expectedTermName = undefined;
            if (annotation.get("idTerm") != annotation.get("idExpectedTerm")) {
                termName = window.app.status.currentTermsCollection.get(annotation.get("idTerm")).get('name');
                expectedTermName = window.app.status.currentTermsCollection.get(annotation.get("idExpectedTerm")).get('name')
            }
            var popoverContent = _.template(popoverTpl, {
                created: window.app.convertLongToDate(annotation.get("created")),
                userName: window.app.view.getUserNameById(annotation.get("user")),
                userByTerm: userByTerm,
                termName: termName,
                expectedTermName: expectedTermName
            });

            $(self.el).popover({
                html: true,
                title: 'Annotation details',
                content: popoverContent,
                placement: 'right',
                trigger: 'hover'
            });

            self.initDraggable();
        });
        return this;
    },

    initDraggable: function () {
        var self = this;
        $(self.el).draggable({
            scroll: true,
            //scrollSpeed : 00,
            revert: true,
            delay: 500,
            opacity: 0.35,
            cursorAt: { top: 85, left: 90},
            start: function (event, ui) {
                var thumb = $(self.el).find(".thumb");
                thumb.popover('hide');
            },
            stop: function (event, ui) {

            }

        });
    }
});
