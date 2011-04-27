var ProjectView = Backbone.View.extend({
    tagName : "div",
    //template : _.template($('#project-view-tpl').html()),
    initialize: function(options) {
        this.container = options.container;
    },
    render: function() {
        $(this.el).html(ich.projectsviewtpl({}, true));
        var self = this;
        var projectNameArray = new Array(); //to have autocompletion
        new ProjectCollection({user : this.userID}).fetch({
            success : function (collection, response) {
                collection.each(function(project) {
                    console.log('project:'+project.get('id'));
                    projectNameArray.push(project.get('name'));
                    var json = project.toJSON();
                        //create panel for a specific project
                        var panel = new ProjectPanelView({
                            model : project
                        }).render();

                        $(self.el).append(panel.el);

                });
                //autocomplete
                console.log("projectNameArray="+projectNameArray);
                $("input#projectsearch").autocomplete({
                    source : projectNameArray,
                    select : function (event)
                    {
                        console.log("select item");

                    }
                });

            }
        });


        return this;
    }
});
