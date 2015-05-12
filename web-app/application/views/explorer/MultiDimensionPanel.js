/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

        $.get("/api/imageinstance/"+json.id+"/imagesequence/possibilities.json", function(data) {

            console.log("GET data:"+data);

            var positionTex = "This image has no other dimension."
            if(data.c!=null && data.c!=undefined) {
                positionTex = "Picture is in position c: "+data.c+" z: "+data.z + " s: "+data.s + " t: "+data.t;
            }

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
                var min=0;
                var max=0;
                var possibilities = data[type];
                var possibilityText = "none";
                if(possibilities!=null) {
                    min = _.min(possibilities, function(value){ return value; });
                    max = _.max(possibilities, function(value){ return value; });
                    possibilityText = max;
                }

                $('#multidimensionPanel' + self.model.get('id')).find(".spinnerChoice").append(_.template(tplspinner,{
                    dim : text,
                    dimText : text,
                    possibility : possibilityText
                }));

                var spinnerEl = el.find('#spinner'+text);
                spinnerEl.attr("value", value);
                spinnerEl.spinner({
                    min:min,
                    max:max,
                    disabled:(min==max && max==0)
                });

                if (min==max && max==0) { //hide it
                    spinnerEl.parent().parent().hide();
                }





                if(type=="channel") {

                    var colors = ["#0000FF","#00FF00","#5B3B11","#FF0000","#FFD700","#00FFFF","#778899","#9400D3"]
                    var buttonContainer = el.find("#spinnerChannel").parent().parent();
                    if(max>0) {
                        var indice = 0;
                        for(var i=min;i<max+1;i++) {
                            indice = indice <  colors.length? indice : 0
                            var color = colors[indice]

                            buttonContainer.append('<br/><input class="mergeChannel" data-channel="'+i+'" type="checkbox" id="mergeChannel'+i+'"+> Channel' + i + " ");
                            buttonContainer.append('<input type="text" style="width:100px;" class="span1" id="mergeChannelColor'+i+'" value="'+color+'" id="cp1" >')
                            //var colorPicker = buttonContainer.find('#mergeChannelColor'+i).colorpicker();
                            indice++;
                        }
                        buttonContainer.append('<br/><button class="btn btn-default btn-xs merge">Merge</button>');
                    }
                }

            };

            loadSpinner("channel","Channel",data.c);
            loadSpinner("zStack","Zstack",data.z);
            loadSpinner("slice","Slice",data.s);
            loadSpinner("time","Time",data.t);




            el.find(".merge").click(function() {
                var colors = []
                var inputs = $(".mergeChannel");
                _.each(inputs,function(input) {
                    if($(input).is(":checked")) {
                        var id = $(input).data("channel");

                        colors.push([id,$("#mergeChannelColor"+id).val()]);
                    }
                });
                console.log("colors");
                //window.app.mergeChannel = colors;
                window.sessionStorage.setItem('mergeChannel', JSON.stringify(colors) );
                self.goToOtherImage(self.browseImageView.model,self.browseImageView.model,"channel");
            });

            el.find(".goToImage").on("click", function() {
                self.goToDimension(data.imageGroup,self.getValue("Channel"),self.getValue("Zstack"),self.getValue("Slice"),self.getValue("Time"),null);
            });


        });

    },
    getValue : function(type){
        return $('#multidimensionPanel' + this.model.get('id')).find('#spinner'+type).spinner('value')
    },
    goToDimension : function(group, channel, zstack,slice, time, merge) {
        var self = this;

        ///api/imagegroup/$id/$channel/$zstack/$slice/$time/imagesequence
        var url = "/api/imagegroup/"+group+"/"+channel+"/"+zstack+"/"+slice+"/"+time+"/imagesequence.json"
        console.log(url) ;
        $.get(url, function(data) {
            var currentImage =  self.browseImageView.model;
            var idImage = data.image;
            var image = new ImageInstanceModel(data.model);


            if(idImage==currentImage.id) {
                return;
            }

            //get current position, next image will be opened at the same position/zoom level
            self.goToOtherImage(currentImage,image,merge);
        });
    },
    goToOtherImage : function(currentImage,nextImage,merge) {
        var self = this;
        var zoom = self.browseImageView.map.zoom;
        var x = self.browseImageView.map.center.lon;
        var y = self.browseImageView.map.center.lat;

        if(self.browseImageView.divPrefixId=='tabs-image' || currentImage.get('reviewUser')!=null) {
            console.log("goToOtherImage") ;
            window.app.controllers.browse.tabs.goToImage(nextImage.id,currentImage.get('project'),currentImage.id, self.browseImageView.getMode(),nextImage,x,y,zoom,merge);
        }  else {
            new ImageReviewModel({id: idImage}).save({}, {
                success: function (model, response) {
                    window.app.view.message("Image", response.message, "success");
                    window.app.controllers.browse.tabs.goToImage(idImage,currentImage.get('project'),currentImage.id, self.browseImageView.getMode(),image,x,y,zoom,merge);
                },
                error: function (model, response) {
                    var json = $.parseJSON(response.responseText);
                    window.app.view.message("Image", json.errors, "error");
                }});
        }
    }
});
