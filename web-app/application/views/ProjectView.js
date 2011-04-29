var ProjectView = Backbone.View.extend({
    tagName : "div",
    projects : null,
    ontologies : null,
    searchProjectPanelElem : "#searchProjectPanel",
    allProjectsButtonElem: "#projectallbutton",
    searchProjectTextBoxElem : "#projectsearchtextbox",
    searchProjectButtonElem : "#projectsearchbutton",
    searchProjectCheckedOntologiesElem : 'input[type=checkbox][name=ontology]:checked',
    searchProjectOntolgiesListElem : "#ontologyChoiceList",
    projectListElem : "#projectlist",
    initialize: function(options) {
        this.container = options.container;
    },
    //TODO: "utility" function (move in an other file...)
    //Return true if a contains obj otherwise return false
    contains: function(a, obj) {
        var i = a.length;
        while (i--) {
            if (a[i] == obj) {
                return true;
            }
        }
        return false;
    },
    render: function() {

        var self = this;

        $(this.el).html(ich.projectsviewtpl({}, true));

        //create search panel
        $(self.searchProjectPanelElem).panel({
            collapseSpeed:1000
        });

        //configure "all projects" button
        $(self.allProjectsButtonElem).button({
            icons : {secondary: "ui-icon-battery-3" }

        });
        $(self.allProjectsButtonElem).click(function() {
            $(self.searchProjectTextBoxElem).val("");

             $.each($(self.searchProjectCheckedOntologiesElem), function(index, value) {
                            $(self.searchProjectCheckedOntologiesElem).click();
                        });

            self.filterProjects("");
        });

        //configure "search" button
        $(self.searchProjectButtonElem).button({
            icons : {secondary: "ui-icon-search" }

        });
        $(self.searchProjectButtonElem).click(function() {
            //get search text for the name
            var searchText = $(self.searchProjectTextBoxElem).val();

            //get ontologies list for ontology
            var searchOntologies = new Array();
            $.each($(self.searchProjectCheckedOntologiesElem), function(index, value) {
                var idOntology =  $(value).attr('id').replace("ontologies","");
                searchOntologies.push(idOntology);
            });

            self.filterProjects(searchText==""?undefined:searchText,searchOntologies.length==0?undefined:searchOntologies);

        });

        //render ontologies choice
        new OntologyCollection({}).fetch({
            success : function (collection, response) {
                self.ontologies = collection;
                collection.each(function(ontology){
                    var choice = ich.ontologieschoicetpl({id:ontology.id,name:ontology.get("name")}, true);
                    $(self.searchProjectOntolgiesListElem).append(choice);
                });
            }});

        self.printProjects();

        return this;
    },
    //show only project that have text value in their name and a ontology from searchOntologies.
    filterProjects : function(text,searchOntologies) {
        var self = this;

        self.projects.each(function(project) {

            if(text==undefined || project.get('name').toLowerCase().contains(text.toLowerCase()))
            {
                if(searchOntologies==undefined || self.contains(searchOntologies,project.get('ontology')))
                    $(self.projectListElem+project.id).show();
                else
                    $(self.projectListElem+project.id).hide();

            }
            else
                $(self.projectListElem+project.id).hide();
        });
    },
    //print all projects
    printProjects : function() {
        var self = this;

        //clear de list
        $(self.projectListElem).empty();

        //array for autocompletion
        var projectNameArray = new Array();
        new ProjectCollection({user : this.userID}).fetch({
            success : function (collection, response) {
                self.projects = collection;
                collection.each(function(project) {

                    projectNameArray.push(project.get('name'));
                    var json = project.toJSON();

                    //create panel for a specific project
                    var panel = new ProjectPanelView({
                        model : project
                    }).render();
                    $(self.projectListElem).append(panel.el);

                });
                //autocomplete
                $(self.searchProjectTextBoxElem).autocomplete({
                    source : projectNameArray,
                    select : function (event,ui)
                    {
                        self.filterProjects(ui.item.label);

                    },
                    search : function(event)
                    {
                    }
                });

            }
        });
    }
});
