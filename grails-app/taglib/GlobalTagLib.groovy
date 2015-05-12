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
 * Created by lrollus on 5/8/14.
 */
class GlobalTagLib {

    static namespace = "wthr"
    def userAgentIdentService

    def isOldMsie = { attrs, body ->
        if (userAgentIdentService.isMsie() && userAgentIdentService.getBrowserVersionNumber()<=8) {
            out << body()
        }
    }

    def isNotOldMsie = { attrs, body ->
        if (!userAgentIdentService.isMsie() || (userAgentIdentService.isMsie() && userAgentIdentService.getBrowserVersionNumber()>8)) {
            out << body()
        }
    }

    def isMsie = { attrs, body ->
        if (userAgentIdentService.isMsie()) {
            out << body()
        }
    }

    def isNotMsie = { attrs, body ->
        if (!userAgentIdentService.isMsie()) {
            out << body()
        }
    }

    def isFirefox = { attrs, body ->
        if (userAgentIdentService.isFirefox()) {
            out << body()
        }
    }

    def isChrome = { attrs, body ->
        if (userAgentIdentService.isChrome()) {
            out << body()
        }
    }


    def isBlackberry = { attrs, body ->
        if (userAgentIdentService.isBlackberry()) {
            out << body()
        }
    }
}