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

var MultiSelectView = Backbone.View.extend({
    collection: null,
    multiple: null,
    initialize: function (options) {
        this.el = options.el;
        this.collection = options.collection;
        this.multiple = options.multiple;
    },
    buildMultiSelect: function () {
        var self = this;
        self.addHtmlElem();
    },
    getHtmlElem: function () {
        console.log("getHtmlElem");
        var self = this;
        var classRequier = "";
        //mark default value as selected:
        var defaultValues = self.getDefaultValue();
        if (self.param.required) {
            classRequier = 'border-style: solid; border-width: 2px;'
        }
        else {
            classRequier = 'border-style: dotted;border-width: 2px;'
        }
        var valueStr = '<select class="domainList" multiple="multiple">';
        self.collection.each(function (value) {
            var selClass = "";
            _.each(defaultValues, function (def) {
                if (def == value.id) {
                    selClass = 'selected="selected"';
                }
            });
            valueStr = valueStr + '<option ' + selClass + ' value="' + value.id + '">' + value.get(self.printAttribut) + '</option>';
        });
        valueStr = valueStr + '</select>';
        return valueStr;
    },
    addHtmlElem: function () {
        var self = this;
        console.log("addHtmlElem");
        self.el.append(self.getHtmlElem());

        var fnSelectionView = null;

        if (self.multiple) {
            fnSelectionView = function (numChecked, numTotal, checkedItem) {
                var selectTitle = [];
                $(checkedItem).each(function () {
                    selectTitle.push($(this).attr("title"));
                });
                var selectText = selectTitle.join(", ");
                selectText = selectText.substring(0, Math.min(15, selectText.length));
                return numChecked + " selected";
            }
        } else {
            fnSelectionView = function (numChecked, numTotal, checkedItem) {
                if (numChecked == 0) {
                    return "0 selected";
                }
                var selectTitle = [];
                $(checkedItem).each(function () {
                    selectTitle.push($(this).attr("title"));
                });
                var selectText = selectTitle.join(", ");
                selectText = selectText.substring(0, Math.min(10, selectText.length));
                return selectText;
            };
        }

        self.el.find(".domainList").multiselect({'autoOpen': false, minWidth: 300, 'height': 200, 'multiple': self.multiple, 'selectedText': fnSelectionView}).multiselectfilter();
        self.el.find("button").width("150");
        //put header menu option on the same line
        self.el.find(".ui-multiselect-menu").find("span").css("display", "inline");
        self.el.find(".ui-multiselect-menu").find("input").css("display", "inline");
        self.el.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").css("display", "inline");
        //put check all on left and deselect all on right
        self.el.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").eq(0).css("float", "left");
        self.el.find(".ui-multiselect-menu").find(".ui-multiselect-header").find("li").eq(1).css("float", "right");
        self.el.find(".ui-multiselect-menu").find("li").css("display", "block");
        //print scroll only vertical
        self.el.find("ul.ui-multiselect-checkboxes").css('overflow-y', 'scroll');
        self.el.find("ul.ui-multiselect-checkboxes").css('overflow-x', 'hidden');
        self.el.find(".domainList").multiselect("close");

    },
    checkAll: function () {
        console.log("checkAll");
        this.el.find(".domainList").multiselect("checkAll");
    },
    uncheckAll: function () {
        console.log("uncheckAll");
        this.el.find(".domainList").multiselect("uncheckAll");
    },
    isCheckAll: function () {
        var self = this;
        var numberOfChoice = self.el.find(".domainList").length;
        var numberOfSelect = self.el.find(".domainList").multiselect("getChecked").length;
        console.log("numberOfChoice=" + numberOfChoice);
        console.log("numberOfSelect=" + numberOfSelect);
        return (numberOfChoice == numberOfSelect);
    }
});