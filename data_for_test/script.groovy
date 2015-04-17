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

//run me with
//groovy -cp 'Cytomine-client-java.jar'  script.groovy
import be.cytomine.client.*;
import be.cytomine.client.collections.*;
import be.cytomine.client.models.*;
String publickey = "PUBLIC_KEY";
String privatekey = "PRIVATE_KEY";
String cytomineCoreUrl = "http://CORE_URL";
String cytomineImsUrl = "http://UPLOAD_URL/";
 
println "********************************************************************"
println "Launch Test:"
Cytomine cytomine = new Cytomine(cytomineCoreUrl, publickey, privatekey, "./");
 
ProjectCollection projects = cytomine.getProjects();
if(projects.size()>0) {
	println "This instance has already some datas."
	println "End of script."
	println "None datas were added."
	return
}

println "Add an ontology"
Ontology ont = cytomine.addOntology("ASimpleOntology")
println "Ontology created : "
println "  name : " + ont.get("name")
println "  id : " + ont.getId()
println "Add a project"
Project proj = cytomine.addProject("ASimpleProject", ont.id)
println "Project created : "
println "  name : " + proj.get("name")
println "  ontology : " + proj.get("ontology")
println "  id : " + proj.getId()
println "********************************************************************"

String imagePATH = "/tmp/images/test.tiff"; // fill here the absolute path of an image.

StorageCollection storages = cytomine.getStorages();
while(storages.size() == 0) {
	storages = cytomine.getStorages();
	Thread.sleep(1000);
}
println "storage : " + storages.get(0).id;
println "********************************************************************"

Cytomine cytomineUpload = new Cytomine(cytomineImsUrl+"/", publickey, privatekey, "./");
cytomineUpload.uploadImage(imagePATH, proj.getId(), storages.get(0).id, cytomineCoreUrl, null, false);




ImageInstanceCollection images = cytomine.getImageInstances(proj.getId());
while(images.size() != 1) {
	images = cytomine.getImageInstances(proj.getId());
	Thread.sleep(30000);
}

println "DONE"
println "********************************************************************"

