var ProjectSearchPanel = Backbone.View.extend({
    idUser: null,
    container: null,
    ontologies: null,
    disciplines: null,
    projectsPanel: null,
    allProjectsButtonElem: "#projectallbutton",
    //addProjectButtonElem : "#projectaddbutton",
    searchProjectOntolgiesListElem: "#ontologyChoiceList",
    searchProjectDisciplinesListElem: "#disciplineChoiceList",
    sliderNumberOfImagesElem: "#numberofimageSlider",
    labelNumberOfImagesElem: "#amountNumberOfImages",
    sliderNumberOfSlidesElem: "#numberofslideSlider",
    labelNumberOfSlidesElem: "#amountNumberOfSlides",
    sliderNumberOfAnnotationsElem: "#numberofannotationSlider",
    labelNumberOfAnnotationsElem: "#amountNumberOfAnnotations",
    searchProjectTextBoxElem: "#projectsearchtextbox",
    searchProjectButtonElem: "#projectsearchbutton",
    searchProjectCheckedOntologiesElem: 'input[type=checkbox][name=ontology]:checked',
    addProjectCheckedOntologiesRadioElem: 'input[type=radio][name=ontologyradio]:checked',
    addProjectCheckedDisciplinesRadioElem: 'input[type=radio][name=disciplineradio]:checked',
    addProjectCheckedUsersCheckboxElem: 'input[type=checkbox][name=usercheckbox]:checked',
    initialize: function (options) {
        this.idUser = options.idUser;
        this.container = options.container;
        this.projectsPanel = options.projectsPanel;
        this.ontologies = options.ontologies;
        this.disciplines = options.disciplines;
    },
    events: {
        //"click .addProject": "showAddProjectPanel",
        "change .searchProjectCriteria": "searchProject",
        "click .showAllProject": "showAllProject",
        "click .refreshAllProject": "refreshAllProject"
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/project/ProjectSearchPanel.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl);
            });

        return this;
    },

    doLayout: function (tpl) {
        var self = this;
        var search = _.template(tpl, {});
        $(this.el).empty();
        $(this.el).append(search);

        require([
            "text!application/templates/project/OntologiesChoices.tpl.html", "text!application/templates/project/DisciplinesChoices.tpl.html"
        ],
            function (ontologiesChoices, disciplinesChoices) {
                self.loadPanelAndButton(ontologiesChoices, disciplinesChoices);
            });
        self.loadSlider();
        self.loadAutocomplete();
    },
    /**
     * Load panel and all buttons/checkbox
     */
    loadPanelAndButton: function (ontologiesChoices, disciplinesChoices) {
        var self = this;

        self.ontologies.each(function (ontology) {
            var choice = _.template(ontologiesChoices, {id: ontology.id, name: ontology.get("name")});
            $(self.searchProjectOntolgiesListElem).append(choice);
        });

        self.disciplines.each(function (discipline) {
            var choice = _.template(disciplinesChoices, {id: discipline.id, name: discipline.get("name")});
            $(self.searchProjectDisciplinesListElem).append(choice);
        });

    },
    /**
     * Load slider for images, annotations,.. number and compute the min/max value
     */
    loadSlider: function () {

        var self = this;
        //init slider to serach by slides number, images number...
        var minNumberOfImage = Number.MAX_VALUE;
        var maxNumberOfImage = 0;
        var minNumberOfSlide = Number.MAX_VALUE;
        var maxNumberOfSlide = 0;
        var minNumberOfAnnotation = Number.MAX_VALUE;
        var maxNumberOfAnnotation = 0;
        self.model.each(function (project) {

            var numberOfImage = parseInt(project.get('numberOfImages'));
            var numberOfSlide = parseInt(project.get('numberOfSlides'));
            var numberOfAnnotation = parseInt(project.get('numberOfAnnotations'));

            if (numberOfImage < minNumberOfImage) {
                minNumberOfImage = numberOfImage;
            }
            if (numberOfImage > maxNumberOfImage) {
                maxNumberOfImage = numberOfImage;
            }
            if (numberOfSlide < minNumberOfSlide) {
                minNumberOfSlide = numberOfSlide;
            }
            if (numberOfSlide > maxNumberOfSlide) {
                maxNumberOfSlide = numberOfSlide;
            }
            if (numberOfAnnotation < minNumberOfAnnotation) {
                minNumberOfAnnotation = numberOfAnnotation;
            }
            if (numberOfAnnotation > maxNumberOfAnnotation) {
                maxNumberOfAnnotation = numberOfAnnotation;
            }

        });

        minNumberOfImage = self.changeMaxValueToZero(minNumberOfImage);
        maxNumberOfImage = self.changeMaxValueToZero(maxNumberOfImage);
        minNumberOfSlide = self.changeMaxValueToZero(minNumberOfSlide);
        maxNumberOfSlide = self.changeMaxValueToZero(maxNumberOfSlide);
        minNumberOfAnnotation = self.changeMaxValueToZero(minNumberOfAnnotation);
        maxNumberOfAnnotation = self.changeMaxValueToZero(maxNumberOfAnnotation);

        //create slider
        self.createSliderWithoutAmountPrint(self.sliderNumberOfImagesElem, self.labelNumberOfImagesElem, minNumberOfImage, maxNumberOfImage);
        self.createSliderWithoutAmountPrint(self.sliderNumberOfSlidesElem, self.labelNumberOfSlidesElem, minNumberOfSlide, maxNumberOfSlide);
        self.createSliderWithoutAmountPrint(self.sliderNumberOfAnnotationsElem, self.labelNumberOfAnnotationsElem, minNumberOfAnnotation, maxNumberOfAnnotation);
    },
    changeMaxValueToZero : function(value) {
       if(value==Number.MAX_VALUE) {
           return 0;
       } else {
           return value;
       }

    },
    /**
     * Create autocomplete project name box in the textbox
     */
    loadAutocomplete: function () {
        var self = this;
        //array for autocompletion
        var projectNameArray = [];

        self.model.each(function (project) {
            projectNameArray.push(project.get('name'));
        });

        //autocomplete  old version with jq ui
//        $(self.searchProjectTextBoxElem).autocomplete({
//            minLength: 0, //with min=0, if user erase its text, it will show all project withouth name constraint
//            source: projectNameArray,
//            select: function (event, ui) {
//                $(self.searchProjectTextBoxElem).val(ui.item.label)
//                self.searchProject();
//
//            },
//            search: function (event) {
//                console.log("search=" + $(self.searchProjectTextBoxElem).val());
//                self.searchProject();
//            }
//        });




        $(self.searchProjectTextBoxElem).typeahead({source:projectNameArray,minLength:0});

        $(self.searchProjectTextBoxElem).bind('propertychange keyup input paste click change',function() {

            self.searchProject();
        });

    },
    /**
     * Create a slider elem in slideElem with min/max value and a label with its amount in labelElem
     * @param sliderElem Html Element that will be a slider
     * @param labelElem Html Element that will print info
     * @param min  Minimum value for slider
     * @param max Maximum value for slider
     */
    createSliderWithoutAmountPrint: function (sliderElem, labelElem, min, max) {
        var self = this;

//        $(sliderElem).noUiSlider({
//            range: [20, 100]
//           ,start: [40, 80]
//           ,step: 20
//           ,slide: function(){
//           }
////            range: [min, max]
////           ,start: [min, max]
////           ,step: 1
////           ,slide: function(){
//////              var values = $(this).val();
//////                $(labelElem).val("" + values[ 0 ] + " - " + values[ 1 ]);
//////                self.searchProject();
////           }
//        });
////        $(labelElem).val("" + $(sliderElem).val()[0] + " - " + $(sliderElem).val()[1]);
//        $(labelElem).find("a").click(function() {
//            alert("1234");
//            return false;}
//        );

        $(sliderElem).slider({
            range: true,
            min: min,
            max: max,
            values: [ min, max ],
            change: function (event, ui) {
                $(labelElem).val("" + ui.values[ 0 ] + " - " + ui.values[ 1 ]);
                self.searchProject();
            }
        });
        $(labelElem).val("" + $(sliderElem).slider("values", 0) + " - " + $(sliderElem).slider("values", 1));
    },
    /**
     * Refresh search panel with all projects info
     * @param Projects Projects list
     */
    refreshSearchPanel: function (Projects) {

        //refresh item from search panel
        //ex: if a user add 1 sample to the project that have the hight number of sample, number of slides slider value must be change
        var self = this;

        self.loadSlider();
        self.loadAutocomplete();
    },
    /**
     * Reset every item in the form by its "default" value (textbox empty, nothing check,..)
     */
    showAllProject: function () {
        var self = this;

        //reset every element
        $(self.searchProjectTextBoxElem).val("");
        $("#ontologyChoiceList").val(-1);
        $("#disciplineChoiceList").val(-1);
        self.resetSlider(self.sliderNumberOfImagesElem);
        self.resetSlider(self.sliderNumberOfSlidesElem);
        self.resetSlider(self.sliderNumberOfAnnotationsElem);

        //start a search
        self.searchProject();
    },
    refreshAllProject: function () {
        this.container.refresh();
    },
    /**
     * Reset slider by putting its first cursor to min and the second one to max
     * @param sliderElem Element for slider
     */
    resetSlider: function (sliderElem) {
        //put the min slider cursor to min and the other to max
        var min = $(sliderElem).slider("option", "min");
        var max = $(sliderElem).slider("option", "max");
        $(sliderElem).slider("values", [min, max]);
    },
    /**
     * Search project with all info from the form
     */
    searchProject: function () {
        var self = this;
        console.log("searchProject");
        var searchText = $(self.searchProjectTextBoxElem).val();
        console.log("searchText="+searchText);
        var searchOntologies = [];
        var selectedOntology = $("#ontologyChoiceList").val();
        if (selectedOntology != -1) {
            searchOntologies.push(selectedOntology);
        }


        var searchDisciplines = [];
        var selectedDiscipline = $("#disciplineChoiceList").val();
        if (selectedDiscipline != -1) {
            searchDisciplines.push(selectedDiscipline);
        }
        /*$.each($(self.searchProjectCheckedOntologiesElem), function(index, value) {
         var idOntology =  $(value).attr('id').replace("ontologies","");
         searchOntologies.push(idOntology);
         });*/

        //get number of images [min,max]
        var numberOfImages = [];
        numberOfImages.push($(self.sliderNumberOfImagesElem).slider("values", 0));
        numberOfImages.push($(self.sliderNumberOfImagesElem).slider("values", 1));

        //get number of slides [min,max]
        var numberOfSlides = [];
        numberOfSlides.push($(self.sliderNumberOfSlidesElem).slider("values", 0));
        numberOfSlides.push($(self.sliderNumberOfSlidesElem).slider("values", 1));

        //get number of annotation [min,max]
        var numberOfAnnotations = [];
        numberOfAnnotations.push($(self.sliderNumberOfAnnotationsElem).slider("values", 0));
        numberOfAnnotations.push($(self.sliderNumberOfAnnotationsElem).slider("values", 1));

        self.filterProjects(
            searchText == "" ? undefined : searchText,
            searchOntologies.length == 0 ? undefined : searchOntologies,
            searchDisciplines.length == 0 ? undefined : searchDisciplines,
            numberOfImages,
            numberOfSlides,
            numberOfAnnotations);
    },

    /**
     * Show only project that match with params
     * @param searchText Project Name
     * @param searchOntologies Ontologies
     * @param searchNumberOfImages Number of image array [min,max]
     * @param searchNumberOfSlides Number of sample array [min,max]
     * @param searchNumberOfAnnotations  Number of annotations array [min,max]
     */
    filterProjects: function (searchText, searchOntologies, searchDisciplines, searchNumberOfImages, searchNumberOfSlides, searchNumberOfAnnotations) {

        var self = this;

        var projects = new ProjectCollection(self.model.models);

        //each search function takes a search data and a collection and it return a collection without elem that
        //don't match with data search
        projects = self.filterByProjectsByName(searchText, projects);
        projects = self.filterProjectsByOntology(searchOntologies, projects);
        projects = self.filterProjectsByDiscipline(searchDisciplines, projects);
        projects = self.filterProjectsByNumberOfImages(searchNumberOfImages, projects);
        projects = self.filterProjectsByNumberOfSlides(searchNumberOfSlides, projects);
        projects = self.filterProjectsByNumberOfAnnotations(searchNumberOfAnnotations, projects);
        //add here filter function

        //show project from "projects" (and hide the other) in project view
        self.container.showProjects(projects);
    },
    filterByProjectsByName: function (searchText, projectOldList) {

        if (searchText == undefined) {
            return projectOldList;
        }

        var projectNewList = new ProjectCollection(projectOldList.models);

        projectOldList.each(function (project) {
            var projectName = project.get('name').toLowerCase();
            var search = searchText.toLowerCase();
            var contains = (projectName.indexOf(search) != -1);
            if (!contains) {
                projectNewList.remove(project);
            }
        });
        return projectNewList;
    },
    filterProjectsByOntology: function (searchOntologies, projectOldList) {
        var self = this;
        var projectNewList = new ProjectCollection(projectOldList.models);

        projectOldList.each(function (project) {

            var idOntology = project.get('ontology') + "";
            if (searchOntologies != undefined && _.indexOf(searchOntologies, idOntology) == -1) {
                projectNewList.remove(project);
            }
        });
        return projectNewList;
    },
    filterProjectsByDiscipline: function (searchDisciplines, projectOldList) {
        var self = this;
        var projectNewList = new ProjectCollection(projectOldList.models);

        projectOldList.each(function (project) {

            var idDiscipline = project.get('discipline') + "";
            if (searchDisciplines != undefined && _.indexOf(searchDisciplines, idDiscipline) == -1) {
                projectNewList.remove(project);
            }
        });
        return projectNewList;
    },
    filterProjectsByNumberOfImages: function (searchNumberOfImages, projectOldList) {
        var self = this;
        var projectNewList = new ProjectCollection(projectOldList.models);
        projectOldList.each(function (project) {
            var numberOfImages = project.get('numberOfImages');
            if (searchNumberOfImages[0] > numberOfImages || searchNumberOfImages[1] < numberOfImages) {
                projectNewList.remove(project);
            }
        });
        return projectNewList;
    },
    filterProjectsByNumberOfSlides: function (searchNumberOfSlides, projectOldList) {
        var self = this;
        var projectNewList = new ProjectCollection(projectOldList.models);
        projectOldList.each(function (project) {
            var numberOfSlides = project.get('numberOfSlides');
            if (searchNumberOfSlides[0] > numberOfSlides || searchNumberOfSlides[1] < numberOfSlides) {
                projectNewList.remove(project);
            }
        });
        return projectNewList;
    },
    filterProjectsByNumberOfAnnotations: function (searchNumberOfAnnotations, projectOldList) {
        var self = this;
        var projectNewList = new ProjectCollection(projectOldList.models);
        projectOldList.each(function (project) {
            var numberOfAnnotations = project.get('numberOfAnnotations');
            if (searchNumberOfAnnotations[0] > numberOfAnnotations || searchNumberOfAnnotations[1] < numberOfAnnotations) {
                projectNewList.remove(project);
            }
        });
        return projectNewList;
    }
});