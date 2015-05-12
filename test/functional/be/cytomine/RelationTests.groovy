package be.cytomine

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

import be.cytomine.ontology.Relation
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 21/02/11
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
class RelationTests {

  void testListRelationWithCredential() {

    log.info("get relation")
    String URL = Infos.CYTOMINEURL+"api/relation.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response:"+response)
      assert 200==code
    def json = JSON.parse(response)
    assert json.collection instanceof JSONArray
  }


    void testShowRelationWithCredential() {

      log.info("show relation")
        Relation relation = BasicInstanceBuilder.getRelation()
      String URL = Infos.CYTOMINEURL+"api/relation/${relation.id}.json"
      HttpClient client = new HttpClient();
      client.connect(URL,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD);
      client.get()
      int code  = client.getResponseCode()
      String response = client.getResponseData()
      client.disconnect();

      log.info("check response:"+response)
        assert 200==code
      def json = JSON.parse(response)
      assert json instanceof JSONObject
    }

    void testShowRelationWithCredentialNotExist() {

      log.info("show relation")
      String URL = Infos.CYTOMINEURL+"api/relation/-99.json"
      HttpClient client = new HttpClient();
      client.connect(URL,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD);
      client.get()
      int code  = client.getResponseCode()
      String response = client.getResponseData()
      client.disconnect();

      log.info("check response:"+response)
        assert 404==code
    }
}
