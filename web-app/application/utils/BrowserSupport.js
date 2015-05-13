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

var BrowserSupport = {
    CHARTS: "Charts cannot be display!",
    isTooOld : function() {
        return (bowser.msie && bowser.version <= 8)
    },

    addMessage : function(element, message) {
        require([
            "text!application/templates/utils/BrowserTooOld.tpl.html"
        ],
            function (tpl) {
                element.empty();
                element.append(_.template(tpl,{message:message}));
         });
    }

};






