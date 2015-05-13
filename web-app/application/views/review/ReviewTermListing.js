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

var ReviewTermListing = Backbone.View.extend({
    width: null,
    software: null,
    project: null,
    parent: null,
    params: [],
    paramsViews: [],
    initialize: function (options) {
        this.container = options.container;
    },
    render: function () {
        var self = this;
        self.addTerm();
    },
    addTerm : function() {
        var self = this;
        self.sortWithOrder();
        self.model.each(function(term) {

            var spanText = _.template(self.container.termSpanTemplate,term.toJSON());
//            $(self.el).append('<div data-term="'+term.id+'" style="background-color: '+term.get('color')+';border-width: 3px; border-style: solid;min-width : 125px; min-height: 175px;text-align: center;margin-bottom: 15px;margin-top: 15px;margin-left: 15px;margin-right: 15px;" class="span1 termChoice" id="termDrop'+term.id+'">'+spanText+'</div>');
            $(self.el).append('<div data-term="'+term.id+'" style="background-color: '+term.get('color')+';min-height: 175px;" class="col-md-3 termChoice" id="termDrop'+term.id+'">'+spanText+'</div>');
        //"
        });
        $(self.el).find("div>span").css("padding","0px");

        $( "#termReviewChoice" ).sortable({update:function() {
              self.saveActualTermOrder();

        }});
        $( "#termReviewChoice" ).disableSelection();
    },
    sortWithOrder : function() {
        var self = this;
        var value = $.cookie('review-order-'+window.app.status.currentProjectModel.get('ontology'));
        if(value!=undefined) {
            var array = _.sortBy(self.model.models, function(item){ return value.indexOf(item.get('id')); });
            self.model.models = array;
        }

    },
    saveActualTermOrder : function() {
        var self = this;


        var orderTerm = _.map($(self.el).find('div'), function(elem) {
             return $(elem).data('term');
                });

        console.log(orderTerm);

        $.cookie('review-order-'+window.app.status.currentProjectModel.get('ontology'), orderTerm);
    }
});