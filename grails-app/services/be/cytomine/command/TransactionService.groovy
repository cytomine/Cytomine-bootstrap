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

/**
 * Start/Stop transaction (group of command request)
 */
class TransactionService {
    def springSecurityService
    static transactional = true

    /**
     * Start a transaction and return it
     */
    Transaction start() {
        synchronized (this.getClass()) {
            //A transaction is a simple domain with a id (= transaction id)
            Transaction transaction = new Transaction()
            transaction.save(flush:true)
            return transaction
        }
    }
}
