var ProjectDashboardProperties = Backbone.View.extend({

    annotationPropertyCollection : null,
    annotationCollection: null,

    imageInstancePropertyCollection : null,
    imageInstanceCollection: null,

    projectPropertyCollection : null,

    initialize:  function (options) {
        this.idDomain = options.idDomain;
        this.nameDomain = options.nameDomain;
    },

    render: function () {
        var self = this;

        require(["text!application/templates/dashboard/Properties.tpl.html"], function (propertiesTpl) {
            self.doLayout(propertiesTpl);
        });
    },

    refresh: function (idDomain, nameDomain) {
        var self = this;

        self.nameDomain = nameDomain;

        if (self.nameDomain != "Project") {
            self.initIdentifiantSelect(idDomain);
        } else {
            var select = $("#identifiantSelect");
            select.empty();
            select.attr("disabled", "disabled");

            self.initTableProperty();
            self.initPropertyRowEvents();
            self.loadAutocomplete();
        }

        self.initRadioButton();
    },

    doLayout: function (propertiesTpl) {
        var self = this;
        console.log("doLayout");

        var content = _.template(propertiesTpl, {id:self.model.id, name: self.model.get("name")});
        $("#tabs-properties-"+self.model.id).append(content);


        //In case of a user use a link in menu explore (popupAnnotation for example)
        if (self.idDomain != null) {
            self.initIdentifiantSelect(self.idDomain);
            self.initRadioButton();
        }

        $("#buttonAnnotationProperty").click(function() {
            window.app.controllers.dashboard.navigate("#tabs-annotationproperties-" + window.app.status.currentProject + "-undefined" ,true);
        });

        $("#buttonImageInstanceProperty").click(function() {
            window.app.controllers.dashboard.navigate("#tabs-imageproperties-" + window.app.status.currentProject + "-undefined",true);
        });

        $("#buttonProjectProperty").click(function() {
            window.app.controllers.dashboard.navigate("#tabs-projectproperties-" + window.app.status.currentProject + "-undefined",true);
        });

        $("#addProperty").click(function(event) {
            console.log("click button add");
            event.preventDefault();
            self.addPropertyTable();
        });
        $("#identifiantSelect").click(function() {
            console.log("click select");
            self.redirectProperty();
        });
        $("#deleteProperty").click(function() {
            console.log("click button delete");
            self.deleteProperty();
        });
    },

    initPropertyRowEvents : function () {
        var self = this;

        $("td.propertyKey").live("dblclick", function () {
            var id = $(this).attr('data-id');
            var idForm = "propertyFormKey" + id;
            var model;

            if (self.nameDomain == "Annotation") {
                model = self.annotationPropertyCollection.get(id);
            } else if (self.nameDomain == "ImageInstance") {
                model = self.imageInstancePropertyCollection.get(id);
            } else if (self.nameDomain == "Project") {
                model = self.projectPropertyCollection.get(id);
            }

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
                        window.app.view.message("Property", json.errors, "error");
                    }
                });
            });
        });

        $("td.propertyValue").live("dblclick", function () {
            var id = $(this).attr('data-id');
            var idForm = "propertyFormValue" + id;
            var model;

            if (self.nameDomain == "Annotation") {
                model = self.annotationPropertyCollection.get(id);
            } else if (self.nameDomain == "ImageInstance") {
                model = self.imageInstancePropertyCollection.get(id);
            } else if (self.nameDomain == "Project") {
                model = self.projectPropertyCollection.get(id);
            }

            var td = $(this);
            var formEdit = _.template("<form id='<%= idForm %>'><input value='<%= value %>' type='text' id='input_NewValue'></form>", {value : model.get('value'), idForm : idForm});
            $(this).html(formEdit);

            $("#"+idForm).submit(function(event) {
                event.preventDefault();
                model.save({ value : $("#input_NewValue").val()}, {
                    success: function (model, response) {
                        td.empty();
                        td.html(model.get('value'));
                        return false;
                    },
                    error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Property", json.errors, "error");
                    }
                });
            });
        });
    },

    initIdentifiantSelect: function (ident) {
        var self = this;
        var select = $("#identifiantSelect");

        select.empty();
        select.attr("disabled", "disabled");

        if (self.nameDomain == "Annotation") {
            new AnnotationCollection({project: self.model.id}).fetch({
                success: function (collection, response) {
                    self.annotationCollection = collection;
                    addValueSelect(collection, ident);
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation Property", json.errors, "error");
                }
            });
        } else if (self.nameDomain == "ImageInstance") {
            new ImageInstanceCollection({project: self.model.id}).fetch({
                success: function (collection, response) {
                    self.imageInstanceCollection = collection;
                    addValueSelect(collection, ident);
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("ImageInstance Property", json.errors, "error");
                }
            });
        }

        var addValueSelect = function (collection, id) {
            if (_.size(collection) > 0) {
                $(select).removeAttr("disabled");
            }
            collection.each(function(options) {
                var date = window.app.convertLongToDate(options.get('created'));
                var option = _.template("<option value='<%= id %>'><%= value %> - <%= created %></option>", { id : options.get('id'), value : options.get('id'), created: date});
                select.append(option);
            });

            if (id != null) {
                select.val(id);
            }
            self.initTableProperty();
            self.initPropertyRowEvents();
            self.loadAutocomplete();
        }
    },

    redirectProperty: function () {
        var self = this;

        if (self.nameDomain == "Annotation") {
            window.location.href ="#tabs-annotationproperties-" + window.app.status.currentProject + "-" + $("#identifiantSelect").val();
        } else if (self.nameDomain == "ImageInstance") {
            window.location.href ="#tabs-imageproperties-" + window.app.status.currentProject + "-" + $("#identifiantSelect").val();
        } else if (self.nameDomain == "Project") {
            window.location.href ="#tabs-projectproperties-" + window.app.status.currentProject + "-" + $("#identifiantSelect").val();
        }

        self.initRadioButton();
    },

    initRadioButton: function () {
        var self = this;

        if (self.nameDomain == "Annotation") {
            $("#buttonImageInstanceProperty").attr("class","btn btn-primary");
            $("#buttonProjectProperty").attr("class","btn btn-primary");
            $("#buttonAnnotationProperty").attr("class","btn btn-primary active");
        } else if (self.nameDomain == "ImageInstance") {
            $("#buttonAnnotationProperty").attr("class","btn btn-primary");
            $("#buttonProjectProperty").attr("class","btn btn-primary");
            $("#buttonImageInstanceProperty").attr("class","btn btn-primary active");
        } else if (self.nameDomain == "Project") {
            $("#buttonAnnotationProperty").attr("class","btn btn-primary");
            $("#buttonImageInstanceProperty").attr("class","btn btn-primary");
            $("#buttonProjectProperty").attr("class","btn btn-primary active");
        }
    },

    initTableProperty : function () {
        var self = this;

        var collection;
        var idDomain = $("#identifiantSelect").val();
        var tbody = $(this.el).find("#tableProperty");
        tbody.empty();

        if (self.nameDomain == "Annotation") {
            collection = self.annotationCollection;

            new AnnotationPropertyCollection({idAnnotation: idDomain }).fetch({
                success: function (collection, response) {
                    self.annotationPropertyCollection = collection;
                    collection.each(function(model) {
                        self.drawOption(model);
                    });
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Annotation Property", json.errors, "error");
                }
            });
        } else if (self.nameDomain == "ImageInstance") {
            collection = self.imageInstanceCollection;

            new ImageInstancePropertyCollection({idImageInstance: idDomain }).fetch({
                success: function (collection, response) {
                    self.imageInstancePropertyCollection = collection;
                    collection.each(function(model) {
                        self.drawOption(model);
                    });
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("ImageInstance Property", json.errors, "error");
                }
            });
        } else if (self.nameDomain == "Project") {
            new ProjectPropertyCollection({idProject: window.app.status.currentProject }).fetch({
                success: function (collection, response) {
                    self.projectPropertyCollection = collection;
                    collection.each(function(model) {
                        self.drawOption(model);
                    });
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Project Property", json.errors, "error");
                }
            });
        }

        //Add Image Or Text
        var imageOrTextPlace = $("#loadImageOrText");
        imageOrTextPlace.empty();

        if (self.nameDomain != "Project") {
            collection.each(function(options) {
                if (idDomain == options.get('id')) {
                    var option = _.template("<img align='middle' id='imageProperty-<%=id%>' width='150px' height='100px' src='<%=image%>'>", { id : options.get('id'), image : options.get('cropURL')});
                    imageOrTextPlace.append(option);
                    imageOrTextPlace.attr("href","#tabs-image-" + window.app.status.currentProject + "-" + options.get('image') + "-");
                }
            });
        } else {
            var option = _.template("<p id='textProperty-<%=id%>'><%=name%></p>", { id : window.app.status.currentProject, name : window.app.status.currentProjectModel.get('name')});
            imageOrTextPlace.append(option);
            imageOrTextPlace.attr("href","##tabs-dashboard-" + window.app.status.currentProject);
        }

    },

    drawOption: function (model) {
        var tbody = $(this.el).find("#tableProperty");
        var option = _.template("<tr class='trProperty<%= id %>' id='<%= id %>'><td data-id='<%= id %>' class='propertyKey'><%= key %></td>" +
            "<td data-id='<%= id %>' class='propertyValue'><%= value %></td>" +
            "<td><input type='checkbox'  id='checkbox<%= id %>'></td></tr>", {id : model.get('id'), key : model.get('key'), value : model.get('value')});
        tbody.append(option);

        //Empty input key and value
        $("#input_key").val("");
        $("#input_value").val("");
    },

    addPropertyTable: function() {
        var self = this;

        if ($("#input_key").val() != "" && $("#input_value").val() != "") {
            if (self.nameDomain == "Annotation") {
                new  AnnotationPropertyModel({domainIdent : $("#identifiantSelect").val(), key: $("#input_key").val(), value : $("#input_value").val()}).save({domainIdent : $("#identifiantSelect").val(), key: $("#input_key").val(), value : $("#input_value").val()}, {
                    success: function (model, response) {
                        self.drawOption(model);
                        self.annotationPropertyCollection.push(model);
                    },
                    error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Annotation Property", json.errors, "error");
                    }
                });
            } else if (self.nameDomain == "ImageInstance") {
                new  ImageInstancePropertyModel({domainIdent : $("#identifiantSelect").val(), key: $("#input_key").val(), value : $("#input_value").val()}).save({domainIdent : $("#identifiantSelect").val(), key: $("#input_key").val(), value : $("#input_value").val()}, {
                    success: function (model, response) {
                        self.drawOption(model);
                        self.imageInstancePropertyCollection.push(model);
                    },
                    error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("ImageInstance Property", json.errors, "error");
                    }
                });
            } else if (self.nameDomain == "Project") {
                new  ProjectPropertyModel({domainIdent : window.app.status.currentProject, key: $("#input_key").val(), value : $("#input_value").val()}).save({domainIdent : window.app.status.currentProject, key: $("#input_key").val(), value : $("#input_value").val()}, {
                    success: function (model, response) {
                        self.drawOption(model);
                        self.projectPropertyCollection.push(model);
                    },
                    error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Project Property", json.errors, "error");
                    }
                });
            }
        }
    },

    deleteProperty: function () {
        var self = this;
        var collection;

        if (self.nameDomain == "Annotation") {
            collection = self.annotationPropertyCollection;
        } else if (self.nameDomain == "ImageInstance") {
            collection = self.imageInstancePropertyCollection;
        } else if (self.nameDomain == "Project") {
            collection = self.projectPropertyCollection;
        }

        collection.each(function(model) {
            if ($("#checkbox"+model.id).is(':checked')) {
                model.destroy({
                    success: function (model, response) {
                        window.app.view.message(self.nameDomain + " Property", response.message, "success");
                        $("tr.trProperty"+model.id).empty();
                    },
                    error : function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message(self.nameDomain + " Property", json.errors, "error");
                    }
                });
            }
        });

    },

    loadAutocomplete: function () {
        var self = this;
        var keyNameArray = []; //array for autocompletion
        var domainName = null;

        if (self.nameDomain == "Annotation") {
            domainName = "annotation";
        } else if (self.nameDomain == "ImageInstance") {
            domainName = "imageinstance";
        }

        if (domainName != null) {
            $.get("/api/"+domainName+"/property/key.json?idProject=" + window.app.status.currentProject, function(data) {
                _.each (data.collection, function (item){
                    keyNameArray.push(item);
                });

                //autocomplete
                $("#input_key").autocomplete({
                    minLength: 0, //with min=0, if user erase its text, it will show all key withouth name constraint
                    source: keyNameArray
                });
            });
        } else {
            $("#input_key").autocomplete({
                source: keyNameArray
            });
        }
    }
});