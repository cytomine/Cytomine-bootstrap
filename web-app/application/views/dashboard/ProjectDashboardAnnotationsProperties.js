var ProjectDashboardAnnotationsProperties = Backbone.View.extend({

    idAnnotationPropertyCollection : null,
    idAnnotationCollection: null,

    initialize:  function (options) {
        this.idAnnotation = options.idAnnotation;
    },

    render: function () {

        var self = this;

        require(["text!application/templates/dashboard/AnnotationProperties.tpl.html"], function (propertiesTpl) {
            self.doLayout(propertiesTpl);
        });
    },

    refresh: function (annotations) {
        var self = this;
        var select = $(this.el).find("#annotationSelect");

        if (annotations != null) {
            select.val(annotations);
        }

        self.initTableAnnotation();
    },

    doLayout: function (propertiesTpl) {

        var self = this;
        console.log("doLayout");

        var content = _.template(propertiesTpl, {id:self.model.id, name: self.model.get("name")});
        $("#tabs-annotationsproperties-"+self.model.id).append(content);

        $("#addAnnotationProperty").click(function() {
            console.log("click button add");
            self.addPropertyTable();
        });
        $("#annotationSelect").click(function() {
            console.log("click select");
            //self.initTableAnnotation();
            self.redirectAnnotation();
        });
        $("#deleteAnnotationProperty").click(function() {
            console.log("click button delete");
            self.deleteAnnotationProperty();
        });

        self.initAnnotationSelect(self.idAnnotation);
        self.initPropertyRowEvents();
        self.loadAutocomplete();
    },

    initPropertyRowEvents : function () {
        var self = this;
        $("td.annotationPropertyKey").live("dblclick", function () {
            var id = $(this).attr('data-id');
            var idForm = "annotationPropertyFormKey" + id;
            var model = self.idAnnotationPropertyCollection.get(id);

            var formEdit = _.template("<form id='<%= idForm %>'><input value='<%= key %>' type='text' id='input_NewKey'></form>", {key : model.get('key'), idForm : idForm});
            $(this).html(formEdit);
            var td = $(this);

            $("#"+idForm).submit(function() {
                model.save({ key : $("#input_NewKey").val()}, {
                    success: function (model, response) {
                        td.empty();
                        td.html(model.get('key'));
                        return false;
                    },
                    error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Annotations Property", json.errors, "error");
                    }
                });
            });
        });


        $("td.annotationPropertyValue").live("dblclick", function () {
            var id = $(this).attr('data-id');
            var idForm = "annotationPropertyFormValue" + id;
            var model = self.idAnnotationPropertyCollection.get(id);
            var td = $(this);
            var formEdit = _.template("<form id='<%= idForm %>'><input value='<%= value %>' type='text' id='input_NewValue'></form>", {value : model.get('value'), idForm : idForm});
            $(this).html(formEdit);

            $("#"+idForm).submit(function() {
                model.save({ value : $("#input_NewValue").val()}, {
                    success: function (model, response) {
                        td.empty();
                        td.html(model.get('value'));
                        return false;
                    },
                    error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Annotations Property", json.errors, "error");
                    }
                });
            });
        });
    },

    initAnnotationSelect: function (idAnnotation) {
        var self = this;
        var select = $(this.el).find("#annotationSelect");
        select.empty();
        select.attr("disabled", "disabled");
        console.log("Get annotation for properties");
        new AnnotationCollection({project: self.model.id}).fetch({
            success: function (collection, response) {
                self.idAnnotationCollection = collection;
                if (_.size(collection) > 0) {
                    $(select).removeAttr("disabled");
                }
                var isSelected = true
                collection.each(function(options) {
                    var date = new Date(options.get('created'));
                    var option = _.template("<option value='<%= id %>'><%= value %> - <%= created %></option>", { id : options.get('id'), value : options.get('id'), created: date});
                    select.append(option);
                });

                if (idAnnotation != null) {
                    select.val(idAnnotation);
                }

                self.initTableAnnotation();
            }
        });
    },

    redirectAnnotation: function () {
        window.location.href ="#tabs-annotationsproperties-" + window.app.status.currentProject + "-" + $("#annotationSelect").val();
    },

    initTableAnnotation : function () {
        var self = this;
        var tbody = $(this.el).find("#tableAnnotationProperty");
        tbody.empty();
        var idAnnotation = $("#annotationSelect").val();

        //Add Image
        self.idAnnotationCollection.each(function(options) {
            if (idAnnotation == options.get('id')) {
                $("#imageSelect").attr("src",options.get('cropURL'));
                $("#loadImageFromAP").attr("href","#tabs-image-" + window.app.status.currentProject + "-" + options.get('image') + "-");
            }
        });

        new AnnotationPropertyCollection({idAnnotation: idAnnotation }).fetch({
            success: function (collection, response) {
                self.idAnnotationPropertyCollection = collection;
                collection.each(function(model) {
                    self.drawOption(model);
                });
            }
        });
    },

    drawOption: function (model) {
        var tbody = $(this.el).find("#tableAnnotationProperty");
        var option = _.template("<tr class='annotationProperty<%= id %>' id='<%= id %>'><td data-id='<%= id %>' class='annotationPropertyKey'><%= key %></td>" +
            "<td data-id='<%= id %>' class='annotationPropertyValue'><%= value %></td>" +
            "<td><input type='checkbox'  id='checkbox<%= id %>'></td></tr>", {id : model.get('id'), key : model.get('key'), value : model.get('value')});
        tbody.append(option);
    },

    addPropertyTable: function() {
        var self = this;
        if ($("#input_key").val() != "" && $("#input_value").val() != "") {
            var id;

            //create annotationProperty
            var annotationProperty = new  AnnotationPropertyModel({domainIdent : $("#annotationSelect").val(), key: $("#input_key").val(), value : $("#input_value").val()}).save({domainIdent : $("#annotationSelect").val(), key: $("#input_key").val(), value : $("#input_value").val()}, {
                success: function (model, response) {
                    id = model.get('id');
                    self.drawOption(model);
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotations Property", json.errors, "error");
                }
            });
        }
    },

    deleteAnnotationProperty: function () {
        var self = this;
        self.idAnnotationPropertyCollection.each(function(model) {
            if ($("#checkbox"+model.id).is(':checked')) {
                model.destroy({
                    success: function (model, response) {
                        window.app.view.message("Annotations Property", response.message, "success");
                        $("tr.annotationProperty"+model.id).empty();
                    },
                    error : function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Annotations Property", json.errors, "error");
                    }
                });
            }
        });
    },

    loadAutocomplete: function () {
        var self = this;
        var keyNameArray = []; //array for autocompletion

        $.get("/api/annotation/property/key.json?idProject=" + window.app.status.currentProject, function(data) {
            _.each (data.collection, function (item){
                keyNameArray.push(item);
            });

            //autocomplete
            $("#input_key").autocomplete({
                minLength: 0, //with min=0, if user erase its text, it will show all key withouth name constraint
                source: keyNameArray
            });
        });
    }
});