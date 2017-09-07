/*
 * Copyright (c) 2009-2017. Authors: see NOTICE file.
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
//groovy -cp 'Cytomine-client-java.jar'  injectSoftware.groovy
import be.cytomine.client.*;
import be.cytomine.client.sample.*;
String cytomineCoreUrl = args[0];
String publickey = args[1];
String privatekey = args[2];

println "********************************************************************"
println "Launch :"
Cytomine cytomine = new Cytomine(cytomineCoreUrl, publickey, privatekey);
 
println "Softwares :"+cytomine.getSoftwares();
SoftwareExample.addSoftwareComputeTermArea(cytomine);
println "ComputeTermArea added"
SoftwareExample.addSoftwareTissueDetect(cytomine);
println "TissueDetect added"
SoftwareExample.addSoftwareTissueSegmentBuilder(cytomine);
SoftwareExample.addSoftwareTissueSegmentPrediction(cytomine);
println "TissueSegment added"
SoftwareExample.addSoftwareCellClassifierFinder(cytomine);
SoftwareExample.addSoftwareCellClassifierBuilder(cytomine);
SoftwareExample.addSoftwareCellClassifierPrediction(cytomine);
SoftwareExample.addSoftwareCellClassifierValidation(cytomine);
println "CellClassifier added"
SoftwareExample.addSoftwareLandMarkBuilder(cytomine);
SoftwareExample.addSoftwareLandMarkPredict(cytomine);
println "LandMark added"
SoftwareExample.addSoftwareExportLandmark(cytomine);
println "Export LandMark added"

/*SoftwareExample.addSoftwareGlmBuilder(cytomine);
println "GlmBuilder added"
SoftwareExample.addSoftwareDmblLandmarkModelBuilder(cytomine);
println "DmblLandmarkModelBuilder added"
SoftwareExample.addSoftwareLcLandmarkModelBuilder(cytomine);
println "LcLandmarkModelBuilder added"
SoftwareExample.addSoftwareLandmarkGenericPredictor(cytomine);
println "LandmarkGenericPredictor added"
SoftwareExample.addSoftwareLandmarkDmblPredictor(cytomine);
println "LandmarkDmblPredictor added"
SoftwareExample.addSoftwareLandmarkLcPredictor(cytomine);
println "LandmarkLcPredictor added"*/


println "DONE"
println "Softwares :"+cytomine.getSoftwares();
println "********************************************************************"

