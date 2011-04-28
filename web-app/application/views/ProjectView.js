var ProjectView = Backbone.View.extend({
    tagName : "div",
    searchText : "",
    //template : _.template($('#project-view-tpl').html()),
    initialize: function(options) {
        this.container = options.container;
    },
    render: function() {
        $(this.el).html(ich.projectsviewtpl({}, true));
        var self = this;


        $('#panelCenter_4').panel({
            collapseSpeed:1000,
            width:"80%"
        });

        $("#projectrefreshbutton").button({
            icons : {secondary: "ui-icon-refresh" }

        });
        $("#projectrefreshbutton").click(function() {
             self.printProjects();

        });

        $("#projectallbutton").button({
            icons : {secondary: "ui-icon-battery-3" }

        });
        $("#projectallbutton").click(function() {
             self.printProjects();
        });


        $("#projectsearchbutton").button({
            icons : {secondary: "ui-icon-search" }

        });
        $("#projectsearchbutton").click(function() {

        });
        console.log(window.app.models.ontologies.length);
        //render ontologies choice
        new OntologyCollection({}).fetch({
            success : function (collection, response) {

            collection.each(function(ontology){
                var choice = ich.ontologieschoicetpl({id:ontology.id,name:ontology.get("name")}, true);
                $("#ontologyChoiceList").append(choice);
            });
        }});

        self.printProjects();

        return this;
    },
    printProjects : function() {
        var self = this;
        $("#projectlist").empty();
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




                    $("#projectlist").append(panel.el);

                });
                //autocomplete
                console.log("projectNameArray="+projectNameArray);
                $("input#projectsearch").autocomplete({
                    source : projectNameArray,
                    select : function (event)
                    {
                        console.log("select item");

                    },
                    search : function(event)
                    {
                        console.log(event);
                        //self.searchText =
                    }
                });

            }
        });
    }
});
