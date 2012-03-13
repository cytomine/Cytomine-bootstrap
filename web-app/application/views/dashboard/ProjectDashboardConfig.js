var ProjectDashboardConfig = Backbone.View.extend({
    rendered : false,
    openParameterGrid : [],
    initialize : function (options) {
        this.el = "#tabs-config-" + this.model.id;
    },
    render : function() {
        console.log("ProjectDashboardConfig.render:"+this.el);
        console.log($(this.el));
        this.initImageFilters();
        this.initSoftwares();
        this.rendered = true;
    },
    refresh : function() {
        if (!this.rendered) this.render();
    },
    removeImageFilter : function (idImageFilter) {
        var self = this;
        new ProjectImageFilterModel({ id : idImageFilter}).destroy({
            success : function (model, response) {
                $(self.el).find("li.imageFilter"+idImageFilter).remove();
                window.app.view.message("", response.message, "success");
            }
        });
        return false;
    },
    removeSoftware : function (idSoftwareProject) {
        var self = this;
        new SoftwareProjectModel({ id : idSoftwareProject }).destroy({
            success : function (model, response) {
                $(self.el).find("li.software"+idSoftwareProject).remove();
                window.app.view.message("", response.message, "success");
            },
            error : function(model, response) {

            }
        });
        return false;
    },
    renderFilters : function() {
        var self = this;
        var el = $(this.el).find(".image-filters");
        new ProjectImageFilterCollection({ project : self.model.id}).fetch({
            success : function (imageFilters, response) {
                imageFilters.each(function (imageFilter) {
                    self.renderImageFilter(imageFilter, el);
                    window.app.view.message("", response.message, "success");
                });
            }
        });
    },
    renderSoftwares : function() {
        var self = this;
        var el = $(this.el).find(".softwares");
        new SoftwareProjectCollection({ project : self.model.id}).fetch({
            success : function (softwareProjectCollection, response) {
                softwareProjectCollection.each(function (softwareProject) {
                    self.renderSoftware(softwareProject, el);
                });
            }
        });

    },
    renderSoftware :function(softwareProject, el){
        var tpl = _.template("<li class='software<%= id %>' style='padding-bottom : 3px;'><a class='btn btn-danger removeSoftware' data-id='<%= id %>' href='#'><i class='icon-trash icon-white' /> Delete</a> <%= name %></li>", softwareProject.toJSON());
        $(el).append(tpl);
    },
    renderImageFilter : function (imageFilter, el) {
        var tpl = _.template("<li class='imageFilter<%= id %>' style='padding-bottom : 3px;'> <a class='btn btn-danger removeImageFilter' data-id='<%= id %>' href='#'><i class=' icon-trash icon-white' /> Delete</a> <%= name %></li>", imageFilter.toJSON());
        $(el).append(tpl);
    },
    initImageFilters : function() {
        var self = this;
        var el = $(this.el).find(".image-filters");

        new ImageFilterCollection().fetch({
            success : function (imageFilters, response) {
                imageFilters.each(function (imageFilter) {
                    var option = _.template("<option value='<%=  id %>'><%=   name %></option>", imageFilter.toJSON());
                    $(self.el).find("#addImageFilter").append(option);

                });
                $(self.el).find("#addImageFilterButton").click(function(){
                    new ProjectImageFilterModel({ project : self.model.id, imageFilter : $(self.el).find("#addImageFilter").val()}).save({},{
                        success : function (imageFilter, response) {
                            self.renderImageFilter(new ImageFilterModel(imageFilter.toJSON().imagefilterproject), el);
                            window.app.view.message("", response.message, "success");
                        },
                        error : function (response) {
                            window.app.view.message("", $.parseJSON(response.responseText).errors, "error");
                        }
                    });
                    return false;
                });
            }
        });

        self.renderFilters();

        $(this.el).find("a.removeImageFilter").live('click',function() {
            var idImageFilter = $(this).attr("data-id");
            self.removeImageFilter(idImageFilter);
            return false;
        });

    },
    initSoftwares : function () {
        var self = this;
        var el = $(this.el).find(".softwares");
        new SoftwareCollection().fetch({
            success : function (softwareCollection, response) {
                softwareCollection.each(function (software) {
                    var option = _.template("<option value='<%= id %>'><%= name %></option>", software.toJSON());
                    $(self.el).find("#addSoftware").append(option);
                });
                $(self.el).find("#addSoftwareButton").click(function(){
                    new SoftwareProjectModel({ project : self.model.id, software : $(self.el).find("#addSoftware").val()}).save({},{
                        success : function (softwareProject, response) {
                            self.renderSoftware(new SoftwareProjectModel(softwareProject.toJSON().softwareproject), el);
                            window.app.view.message("", response.message, "success");
                        },
                        error : function (model, response) {
                            window.app.view.message("", $.parseJSON(response.responseText).errors, "error");
                        }
                    });
                    return false;
                });
            }
        });

        self.renderSoftwares();

        $(this.el).find("a.removeSoftware").live('click', function() {
            var idSoftwareProject = $(this).attr('data-id');
            self.removeSoftware(idSoftwareProject);
            return false;
        });
    }
});