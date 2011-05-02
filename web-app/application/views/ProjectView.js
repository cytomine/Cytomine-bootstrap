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

        new ProjectCollection({user : self.userID}).fetch({
            success : function (collection, response) {
                self.printProjects(collection);
            }});

        return this;
    },
    //show only project that have searchText value in their name and a ontology from searchOntologies.
    filterProjects : function(searchText,searchOntologies) {
        var self = this;

        var projects =  new ProjectCollection(self.projects.models);

        //each search function takes a search data and a collection and it return a collection without elem that
        //don't match with data search
        projects = self.filterByProjectsByName(searchText,projects);
        projects = self.filterProjectsByOntology(searchOntologies,projects);
        //add here filter function


        self.projects.each(function(project) {
            //if project is in project result list, show it
            if(projects.get(project.id)!=null)
                $(self.projectListElem+project.id).show();
            else
                $(self.projectListElem+project.id).hide();
        });
    },
    filterByProjectsByName : function(searchText,projectOldList) {

        var projectNewList =  new ProjectCollection(projectOldList.models);

        projectOldList.each(function(project) {
            //if text is undefined: don't hide project
            //if project name contains search text, don't hide project
            if(searchText!=undefined && !project.get('name').toLowerCase().contains(searchText.toLowerCase()))
                projectNewList.remove(project);
        });

        return projectNewList;
    },

    filterProjectsByOntology : function(searchOntologies,projectOldList) {
        var self = this;
        var projectNewList =  new ProjectCollection(projectOldList.models);

        projectOldList.each(function(project) {

            if(searchOntologies!=undefined && !self.contains(searchOntologies,project.get('ontology')))
               projectNewList.remove(project);
        });
        return projectNewList;
    },

    //print project from collection
    printProjects : function(collection) {
        var self = this;

        //clear de list
        $(self.projectListElem).empty();

        //array for autocompletion
        var projectNameArray = new Array();

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
