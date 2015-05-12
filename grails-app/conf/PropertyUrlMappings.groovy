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


class PropertyUrlMappings
{
    static mappings = {

        "/api/user/$idUser/imageinstance/$idImage/annotationposition.$format"(controller:"restProperty"){
            action = [GET:"listAnnotationPosition"]
        }

        //project
        "/api/project/$idProject/property.$format"(controller:"restProperty"){
            action = [GET:"listByProject",POST: "addPropertyProject"]
        }
        "/api/project/$idProject/key/$key/property.$format"(controller:"restProperty"){
            action = [GET:"showProject"]
        }
        "/api/project/$idProject/property/$id.$format"(controller:"restProperty"){
            action = [GET:"showProject",PUT:"update", DELETE:"delete"]
        }

        //annotation
        "/api/annotation/$idAnnotation/property.$format"(controller:"restProperty"){
            action = [GET:"listByAnnotation",POST: "addPropertyAnnotation"]
        }
        "/api/annotation/$idAnnotation/key/$key/property.$format"(controller:"restProperty"){
            action = [GET:"showAnnotation"]
        }
        "/api/annotation/$idAnnotation/property/$id.$format"(controller:"restProperty"){
            action = [GET:"showAnnotation",PUT:"update", DELETE:"delete"]
        }
        "/api/annotation/property/key.$format"(controller:"restProperty"){
            action = [GET:"listKeyForAnnotation"]
        }

        //IMAGEINSTANCE
        "/api/imageinstance/$idImageInstance/property.$format"(controller:"restProperty"){
            action = [GET:"listByImageInstance",POST: "addPropertyImageInstance"]
        }
        "/api/imageinstance/$idImageInstance/key/$key/property.$format"(controller:"restProperty"){
            action = [GET:"showImageInstance"]
        }
        "/api/imageinstance/$idImageInstance/property/$id.$format"(controller:"restProperty"){
            action = [GET:"showImageInstance",PUT:"update", DELETE:"delete"]
        }
        "/api/imageinstance/property/key.$format"(controller:"restProperty"){
            action = [GET:"listKeyForImageInstance"]
        }

        //generic domain
        "/api/domain/$domainClassName/$domainIdent/property.$format"(controller:"restProperty"){
            action = [GET:"listByDomain",POST: "addPropertyDomain"]
        }
        "/api/domain/$domainClassName/$domainIdent/key/$key/property.$format"(controller:"restProperty"){
            action = [GET:"showDomain"]
        }
        "/api/domain/$domainClassName/$domainIdent/property/$id.$format"(controller:"restProperty"){
            action = [GET:"showDomain",PUT:"update", DELETE:"delete"]
        }

        "/api/keywords.$format"(controller:"keywords"){
            action = [GET:"list"]
        }
    }
}
