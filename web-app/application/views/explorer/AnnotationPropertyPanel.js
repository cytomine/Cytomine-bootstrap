var AnnotationPropertyPanel = SideBarPanel.extend({
    tagName: "div",
    keyAnnotationProperty: null,
    annotationPropertyLayers : [],
    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function (options) {
        this.browseImageView = options.browseImageView;
        this.callback = options.callback;
        //this.layer = options.layer;
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require([
            "text!application/templates/explorer/AnnotationPropertyPanel.tpl.html"
        ], function (tpl) {
            self.doLayout(tpl);
        });
        return this;
    },

    initSelect: function (id) {
        var self = this;
        var select = $(this.el).find("#selectLayersAnnotationProperty-"+id);
        select.empty();

        var first = _.template("<option id='nokey' value='<%= id %>'><%= value %></option>", { id : "selectedEmpty", value : "No Key Selected"});
        select.append(first);

        $.get("/api/annotation/property/key.json?idImage=" + id + "&user=true", function(data) {
            _.each (data.collection, function (item){
                var option = _.template("<option value='<%= id %>' class='<%= clazz %>'><%= value %></option>", { id : item.key, value : item.key, clazz : item.userId});
                select.append(option);
            });

            sortSelect();
            filterSelect();
        });

        var sortSelect = function sortArray(){
            var list= new Array();
            var el= document.getElementById('selectLayersAnnotationProperty-'+id); //:to do use class or find another way

            for(var i=0;i<el.options.length-1;i++){
                list[i]=[el.options[i+1].text, $(el.options[i+1]).attr('class')];
            }
            list=list.sort();

            for(var i=0;i<el.options.length-1;i++){
                el.options[i+1].id=list[i][0];
                el.options[i+1].value=list[i][0];
                el.options[i+1].text=list[i][0];
                $(el.options[i+1]).attr('class', list[i][1]);
            }
        }
        var filterSelect = function (){

            $("#selectLayersAnnotationProperty-"+id+" option").hide();
            $("#selectLayersAnnotationProperty-"+id+" #nokey").show();

            _.each(self.browseImageView.layers, function (layer) {

                if (layer.vectorsLayer.visibility) {
                    $("#selectLayersAnnotationProperty-"+id+" ."+layer.userID).show();
                }

            });

            $("#selectLayersAnnotationProperty-"+idImage).val('selectedEmpty');
        }
    },

    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl) {
        var self = this;
        var idImage = this.model.get('id');
        var el = $('#annotationPropertyPanel'+idImage);
        el.html(_.template(tpl, {id: idImage}));
        var elContent = el.find(".annotationPropertyContent");
        var sourceEvent = el.find(".toggle-content");
        this.initToggle(el, elContent, sourceEvent, "annotationPropertyContent");

        self.initSelect(idImage);

        $("#selectLayersAnnotationProperty-"+idImage).on("change", function() {
            self.updateAnnotationProperyLayers();
        });

        el.find(".refreshProperties").click(function(elem) {
           self.initSelect(idImage);
        })
    },

    updateAnnotationProperyLayers : function() {
        var self = this;
        var idImage = this.model.get('id');
        _.each(self.annotationPropertyLayers, function (annotationPropertyLayer) {
            annotationPropertyLayer.removeFromMap();
        });
        self.annotationPropertyLayers = [];

        var key = $("#selectLayersAnnotationProperty-"+idImage).val();

        $("#selectLayersAnnotationProperty-"+idImage+" option").hide();
        $("#selectLayersAnnotationProperty-"+idImage+" #nokey").show();

        _.each(self.browseImageView.layers, function (layer) {

            if (layer.vectorsLayer.visibility) {
                $("#selectLayersAnnotationProperty-"+idImage+" ."+layer.userID).show();
                if (key != "selectedEmpty") {
                    var annotationPropertyLayer = new AnnotationPropertyLayer(self.model.get('id'), layer.userID, self.browseImageView, key);
                    annotationPropertyLayer.addToMap();
                    //annotationPropertyLayer.setZIndex(726);

                    self.annotationPropertyLayers.push(annotationPropertyLayer);
                }
            }

        });

        var optionId;
        if(key != 'selectedEmpty') {
            optionId = key;
        } else {
            optionId = 'nokey';
        }
        // if the option is no more visible return to No key selected
        // only method to check if an option is visible (in a select list !)
        if($(document.getElementById(optionId)).attr('style') == "display: none;"){
            $("#selectLayersAnnotationProperty-"+idImage).val('selectedEmpty');
        }
    }
});
