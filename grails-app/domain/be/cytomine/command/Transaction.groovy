package be.cytomine.command

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

import be.cytomine.CytomineDomain

/**
 * @author lrollus
 * A transaction allow to group command. It allow to undo/redo multiple command (e.g. add annotation x + add term y to x = 2 commands)
 * Its a long number generated with sequence (thread-safe)
 */
class Transaction extends CytomineDomain {
    //A transaction is just an id (provide by cytomine domain)
}
