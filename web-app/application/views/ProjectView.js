var ProjectView = Backbone.View.extend({
    tagName : "div",
    //template : _.template($('#project-view-tpl').html()),
    initialize: function(options) {
        this.container = options.container;
    },
    render: function() {
        $(this.el).html(ich.projectsviewtpl({}, true));
        var self = this;

        new ProjectCollection({user : this.userID}).fetch({
            success : function (collection, response) {
                collection.each(function(project) {
                    console.log('project:'+project.get('id'));
                    var json = project.toJSON();
                        //create panel for a specific project
                        var panel = new ProjectPanelView({
                            model : project
                        }).render();

                        $(self.el).append(panel.el);

                });
            }
        });
        return this;
    }
});
