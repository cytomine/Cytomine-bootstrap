package be.cytomine.security

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

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.SearchEngineFilterAPI

/**
 * Created by hoyoux on 07.11.14.
 */
class SearchEngineFilterSecurityTest extends SecurityTestsAbstract {

    void testSecurityForCytomineAdmin() {
        //create new filter
        def filter = BasicInstanceBuilder.getSearchEngineFilterNotExist()
        def result = SearchEngineFilterAPI.create(filter.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

        //check if create == good HTTP Code
        assert 200 == result.code

        Long id = result.data.id

        //check if show == good HTTP Code
        result = SearchEngineFilterAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //check if delete == good HTTP Code
        result = SearchEngineFilterAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testSecurityForFilterCreator() {
        //init user
        def USERNAME1 = "user1";
        def PASSWORD1 = "password";

        User user = BasicInstanceBuilder.getUser1()

        //create new filter
        def filter = BasicInstanceBuilder.getSearchEngineFilterNotExist()
        def result = SearchEngineFilterAPI.create(filter.encodeAsJSON(), USERNAME1, PASSWORD1)

        //check if create == good HTTP Code
        assert 200 == result.code

        Long id = result.data.id

        //check if show == good HTTP Code
        result = SearchEngineFilterAPI.show(id, USERNAME1, PASSWORD1)
        assert 200 == result.code

        //check if delete == good HTTP Code
        result = SearchEngineFilterAPI.delete(id, USERNAME1, PASSWORD1)
        assert 200 == result.code
    }

    void testSecurityForSimpleUser() {
        //init user creator
        def USERNAME2 = "user2";
        def PASSWORD2 = "password";

        User creator = BasicInstanceBuilder.getUser2()
        //init user tester
        def USERNAME1 = "user1";
        def PASSWORD1 = "password";

        User user = BasicInstanceBuilder.getUser1()


        //create new filter
        def filter = BasicInstanceBuilder.getSearchEngineFilterNotExist()
        def result = SearchEngineFilterAPI.create(filter.encodeAsJSON(), USERNAME2, PASSWORD2)

        //check if create == good HTTP Code
        assert 200 == result.code

        Long id = result.data.id

        //check if show == good HTTP Code
        result = SearchEngineFilterAPI.show(id, USERNAME1, PASSWORD1)
        assert 403 == result.code

        //check if delete == good HTTP Code
        result = SearchEngineFilterAPI.delete(id, USERNAME1, PASSWORD1)
        assert 403 == result.code
    }

    void testSecurityForAnonymous() {
        //create new filter
        def filter = BasicInstanceBuilder.getSearchEngineFilterNotExist()
        def result = SearchEngineFilterAPI.create(filter.encodeAsJSON(), Infos.BADLOGIN, Infos.BADPASSWORD)

        //check if create == good HTTP Code
        assert 401 == result.code

        //create a filter for the end of the test
        filter = BasicInstanceBuilder.getSearchEngineFilterNotExist()
        result = SearchEngineFilterAPI.create(filter.encodeAsJSON(), Infos.ADMINLOGIN, Infos.ADMINPASSWORD)

        Long id = result.data.id

        //check if show == good HTTP Code
        result = SearchEngineFilterAPI.show(id, Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code

        //check if delete == good HTTP Code
        result = SearchEngineFilterAPI.delete(id, Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code
    }

}
