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
 * Created by julien 
 * Date : 25/02/15
 * Time : 15:01
 */

class AmqpQueueUrlMappings{
    static mappings = {
        "/api/amqp_queue.$format"(controller:"restAmqpQueue"){
            action = [GET: "list",POST:"add"]
        }
        "/api/amqp_queue/$id.$format"(controller:"restAmqpQueue"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/amqp_queue.$format?name=$name"(controller:"restAmqpQueue"){
            action = [GET:"listByNameILike"]
        }
        "/api/amqp_queue/name/$name.$format"(controller:"restAmqpQueue"){
            action = [GET:"show"]
        }
    }
}
