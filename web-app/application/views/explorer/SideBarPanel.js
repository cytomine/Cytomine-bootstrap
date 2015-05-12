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
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */

var SideBarPanel = Backbone.View.extend({

    initToggle: function (el, elContent, sourceEvent, storageKey) {
        var self = this;
        sourceEvent.click(function (e) {
            if (elContent.is(':hidden')) {
                self.show($(this), elContent, storageKey);
            } else {
                self.hide($(this), elContent, storageKey);
            }
        });

        if (storageKey && window.localStorage.getItem(storageKey)) {
            var pref = window.localStorage.getItem(storageKey);
            if (pref.visible) {
                this.show(sourceEvent, elContent, storageKey);
            } else {
                this.hide(sourceEvent, elContent, storageKey);
            }
        }
    },

    show: function (link, elContent, storageKey) {
        if (storageKey) {
            window.localStorage.setItem(storageKey, { visible: true});
        }
        link.removeClass("glyphicon-plus");
        link.addClass("glyphicon-minus");
        elContent.show();
    },

    hide: function (link, elContent, storageKey) {
        if (storageKey) {
            window.localStorage.setItem(storageKey, { visible: false});
        }
        link.addClass("glyphicon-plus");
        link.removeClass("glyphicon-minus");
        elContent.hide();
    }
});

