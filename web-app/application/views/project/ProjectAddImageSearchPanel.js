var ProjectAddImageSearchPanel = Backbone.View.extend({
    images: null,
    tab: null,
    initialize: function (options) {
        var self = this;
        this.container = options.container;
        this.images = options.images;
        this.tab = options.tab;

    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/project/ProjectAddImageSearchDialog.tpl.html"
        ],
            function (tpl) {
                self.doLayout(tpl);
            });
        return this;
    },
    doLayout: function (tpl) {
        var self = this;


        var json = self.model.toJSON();

        //Get ontology name
        var idOntology = json.ontology;
        json.ontologyId = idOntology;
        json.tab = self.tab;
        var dialog = _.template(tpl, json);
        $(self.el).append(dialog);


        // $(self.el).find("#tabsProjectaddimagedialog"+this.model.get('id')).tabs();

        self.renderImageListing();

        var imagesNameArray = [];

        //TODO: improve perf and fill it when browse in an other place?
        /*self.images.each(function(image) {
         imagesNameArray.push(image.get('filename'));
         }); */


        //autocomplete
        $("#filenamesearchtextboxup" + self.model.id + "-" + self.tab).autocomplete({
            minLength: 0, //with min=0, if user erase its text, it will show all project withouth name constraint
            source: imagesNameArray,
            select: function (event, ui) {
                $("#filenamesearchtextboxup" + self.model.id + "-" + self.tab).val(ui.item.label)
                self.container.searchImages();

            },
            search: function (event) {


                self.container.searchImages();
            }
        });

        $("#datestartsearchup" + self.model.id + "-" + self.tab).change(function () {
            self.container.searchImages();
        });
        $("#dateendsearchup" + self.model.id + "-" + self.tab).change(function () {
            self.container.searchImages();
        });

        return this;

    },
    search: function (images) {
        var self = this;


        //
        return self.filterImages(searchText == "" ? undefined : searchText, dateStart, dateEnd);
    },
    filterImages: function (searchText, dateStart, dateEnd) {

        var self = this;
        var images = new ImageCollection(self.images.models);

        //each search function takes a search data and a collection and it return a collection without elem that
        //don't match with data search

        images = self.filterByImagesByName(searchText, images);

        images = self.filterByDateStart(dateStart, images);

        images = self.filterByDateEnd(dateEnd, images);

        //add here filter function
        return images;


    },
    filterByImagesByName: function (searchText, imagesOldList) {

        var imagesNewList = new ImageCollection(imagesOldList.models);

        imagesOldList.each(function (image) {
            if (searchText != undefined && !image.get('filename').toLowerCase().contains(searchText.toLowerCase())) {
                imagesNewList.remove(image);
            }
        });

        return imagesNewList;
    },

    filterByDateStart: function (dateStart, imagesOldList) {

        var imagesNewList = new ImageCollection(imagesOldList.models);

        imagesOldList.each(function (image) {
            var dateAdded = new Date()
            dateAdded.setTime(image.get('created'));
            //
            if (dateStart != undefined && dateAdded < dateStart) {
                imagesNewList.remove(image);
            }
        });

        return imagesNewList;

    },

    filterByDateEnd: function (dateEnd, imagesOldList) {
        var imagesNewList = new ImageCollection(imagesOldList.models);

        imagesOldList.each(function (image) {
            var dateAdded = new Date()
            dateAdded.setTime(image.get('created'));
            //
            if (dateEnd != undefined && dateAdded > dateEnd) {
                imagesNewList.remove(image);
            }
        });

        return imagesNewList;
    },

    renderImageListing: function () {
        var self = this;


    }
});