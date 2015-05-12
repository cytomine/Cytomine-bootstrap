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
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class UserUrlMappings {

    static mappings = {
        /* User */
        "/api/signature.$format"(controller:"restUser"){
            action = [GET:"signature"]
        }
        "/api/user.$format"(controller:"restUser"){
            action = [GET:"list",POST:"add"]
        }
        "/api/user/$id.$format"(controller:"restUser"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/user/$id/keys.$format"(controller:"restUser"){
            action = [GET:"keys"]
        }
        "/api/userkey/$publicKey/keys.$format"(controller:"restUser"){
            action = [GET:"keys"]
        }
        "/api/user/current.$format"(controller:"restUser"){
            action = [GET:"showCurrent"]
        }
        "/api/user/$id/friends.$format"(controller:"restUser"){
            action = [GET:"listFriends"]
        }
        "/api/userJob.$format"(controller:"restUserJob"){
            action = [POST:"createUserJob"]
        }
        "/api/userJob/$id.$format"(controller:"restUserJob"){
            action = [GET:"showUserJob"]
        }

        "/api/group/$id/user.$format"(controller: "restUser"){
            action = [GET:"listByGroup"]
        }

        "/api/project/$id/user.$format"(controller: "restUser"){
            action = [GET:"showByProject",POST:"addUser"]
        }
        "/api/project/$id/admin.$format"(controller: "restUser"){
            action = [GET:"showAdminByProject"]
        }
        "/api/project/$id/creator.$format"(controller: "restUser"){
            action = [GET:"showCreatorByProject"]
        }
        "/api/project/$id/user/$idUser.$format"(controller: "restUser"){
            action = [DELETE:"deleteUserFromProject",POST:"addUserToProject"]
        }
        "/api/project/$id/user/$idUser/admin.$format"(controller: "restUser"){
            action = [DELETE:"deleteUserAdminFromProject",POST:"addUserAdminToProject"]
        }
        "/api/project/$id/userjob.$format"(controller: "restUserJob"){
            action = [GET:"listUserJobByProject"]
        }
        "/api/ontology/$id/user.$format"(controller: "restUser"){
            action = [GET:"showUserByOntology"]
        }
        "/api/ontology/$id/creator.$format"(controller: "restUser"){
            action = [GET:"showCreatorByOntology"]
        }
        "/api/project/$id/userlayer.$format"(controller: "restUser"){
            action = [GET:"showLayerByProject"]
        }

        "/api/project/$id/online/user.$format"(controller: "restUser"){
            action = [GET:"listOnlineFriendsWithPosition"]
        }
        "/api/ldap/user.$format"(controller:"restUser"){
            action = [POST:"addFromLDAP"]
        }
        "/api/ldap/$username/user.$format"(controller:"restUser"){
            action = [GET:"isInLdap"]
        }


        "/api/domain/$domainClassName/$domainIdent/user/$user.$format"(controller:"restACL"){
            action = [GET:"list",POST:"add",DELETE: "delete"]
        }

        //for admin
        "/api/acl.$format"(controller:"restACL"){
            action = [GET:"listACL"]
        }
        "/api/acl/domain.$format"(controller:"restACL"){
            action = [GET:"listDomain"]
        }

        "/api/user/$id/password.$format"(controller:"restUser"){
            action = [PUT:"resetPassword"]
        }

        "/api/token.$format"(controller:"login"){
            action = [GET:"buildToken",POST:"buildToken"]
        }

        /**
         * Reporting
         */
        "/api/project/$id/user/download"(controller: "restUser"){
            action = [GET:"downloadUserListingLightByProject"]
        }

    }
}
