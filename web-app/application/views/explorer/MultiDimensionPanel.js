/**
 * Created with IntelliJ IDEA.
 * User: stevben
 * Date: 23/01/13
 * Time: 12:52
 * To change this template use File | Settings | File Templates.
 */

var MultiDimensionPanel = SideBarPanel.extend({
    tagName: "div",

    /**
     * ExplorerTabs constructor
     * @param options
     */
    initialize: function (options) {
        this.browseImageView = options.browseImageView;
    },
    /**
     * Grab the layout and call ask for render
     */
    render: function () {
        var self = this;
        require([
            "text!application/templates/explorer/MultiDimensionPanel.tpl.html","text!application/templates/explorer/MultiDimensionSpinnerPanel.tpl.html"
        ], function (tpl,tplspinner) {
            self.doLayout(tpl,tplspinner);
        });
        return this;
    },
    /**
     * Render the html into the DOM element associated to the view
     * @param tpl
     */
    doLayout: function (tpl,tplspinner) {
        var self = this;
        var json = self.model.toJSON();
        json.originalFilename = self.model.getVisibleName(window.app.status.currentProjectModel.get('blindMode'));


        console.log("create spinner");


        $.get("/api/imageinstance/"+json.id+"/imagesequence/possibilities.json", function(data) {

            console.log("GET data:"+data);

            var positionTex = "c: "+data.c+" z: "+data.z + " s: "+data.s + " t: "+data.t;
            json.position=positionTex;
            var content = _.template(tpl,json);

            $('#multidimensionPanel' + self.model.get('id')).html(content);
            var el = $('#multidimensionPanel' + self.model.get('id'));
            var elContent1 = el.find(".dimPanelContent1");
            var sourceEvent1 = el.find(".toggle-content1");
            var elContent2 = el.find(".dimPanelContent2");
            var sourceEvent2 = el.find(".toggle-content2");
            self.initToggle(el, elContent1, sourceEvent1, "dimPanelContent1");
            self.initToggle(el, elContent2, sourceEvent2, "dimPanelContent2");



            var loadSpinner = function(type, text, value) {
                console.log("loadSpinner:"+type);
                var min=0;
                var max=0;
                var possibilities = data[type];
                console.log("possibilities:"+possibilities);
                var possibilityText = "none";
                if(possibilities!=null) {
                    min = _.min(possibilities, function(value){ return value; });
                    max = _.max(possibilities, function(value){ return value; });
                    possibilityText = max;
                }
                console.log("multidimensionPanel");
                $('#multidimensionPanel' + self.model.get('id')).find(".spinnerChoice").append(_.template(tplspinner,{dim:text,dimText:text,possibility:possibilityText}));

                console.log("spinner:"+'#spinner'+text);
                el.find('#spinner'+text).spinner({hold:true,value:value,min:min,max:max,disabled:(min==max && max==0)});

                console.log("changed");
                el.find('#spinner'+text).on("changed",function() {
                    el.find(".goToImage").empty();
                    el.find(".goToImage").text("Go to layer c:" + self.getValue("Channel") +" z:" + self.getValue("Zstack") +" s:" + self.getValue("Slice") +" t:" + self.getValue("Time"));
                });
            };

             console.log("***********LOAD SPINNER");
            loadSpinner("channel","Channel",data.c);
            loadSpinner("zStack","Zstack",data.z);
            loadSpinner("slice","Slice",data.s);
            loadSpinner("time","Time",data.t);

            el.find(".goToImage").click(function() {
                console.log("")

                self.goToDimension(data.imageGroup,self.getValue("Channel"),self.getValue("Zstack"),self.getValue("Slice"),self.getValue("Time"))

            });


        });

    },
    getValue : function(type){
        return $('#multidimensionPanel' + this.model.get('id')).find('#spinner'+type).spinner('value')
    },
    goToDimension : function(group, channel, zstack,slice, time) {
        var self = this;

        ///api/imagegroup/$id/$channel/$zstack/$slice/$time/imagesequence
        var url = "/api/imagegroup/"+group+"/"+channel+"/"+zstack+"/"+slice+"/"+time+"/imagesequence.json"
        console.log(url) ;
        $.get(url, function(data) {
            var currentImage =  self.browseImageView.model;
            var idImage = data.image

            if(idImage==currentImage.id) {
                return;
            }

            if(self.browseImageView.divPrefixId=='tabs-image' || currentImage.get('reviewUser')!=null) {
                window.location = "#" + self.browseImageView.divPrefixId + "-"+currentImage.get('project')+"-"+idImage+"-";
                $("#closeTab" +self.browseImageView.divPrefixId+ "-"+currentImage.id).click();
            } else {
                new ImageReviewModel({id: idImage}).save({}, {
                    success: function (model, response) {
                        window.app.view.message("Image", response.message, "success");
                        window.location = '#'+ self.browseImageView.divPrefixId  + '-' + currentImage.get('project') + '-' + response.imageinstance.id + '-';
                        $("#closeTab" +self.browseImageView.divPrefixId+ "-"+ currentImage.id).click();
                    },
                    error: function (model, response) {
                        var json = $.parseJSON(response.responseText);
                        window.app.view.message("Image", json.errors, "error");
                    }});
            }
        });
    }
});
