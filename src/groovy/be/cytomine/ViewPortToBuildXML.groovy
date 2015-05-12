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

import groovy.util.logging.Log
import org.w3c.dom.Document
import org.w3c.dom.Element

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * User: lrollus
 * Date: 16/03/12
 * GIGA-ULg
 * Extract data from viewport (import cytomine js + lib js) and build 2 xml files with application and lib data.
 * Thanks to these two xml files, we ca create application.js and lib.js
 * application.js: all js files from cytomine
 * lib.js: all js files from lib
 */
@Log
class ViewPortToBuildXML {

    /**
     * Create lib build.xml and app build.xml thanks to viewport info
     */
    public static void process() {
        //Read viewport
        def inputStreamFileSourceFull = new FileInputStream("grails-app/views/layouts/viewport.gsp");

        //must skip line 0->11 because not valid xml
        String content = convertStreamToString(inputStreamFileSourceFull, 12)
        content = content.replace("<jawr:script src='/i18n/messages.js'></jawr:script>", "")
        print content
        //convert string to xml element
        def inputStreamFileSource = new ByteArrayInputStream(content.getBytes("UTF-8"));
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()

        Element viewPortXML = builder.parse(inputStreamFileSource).documentElement

        //Get lib/app src js files from viewports
        List<String> libFiles = getLibFilesFromViewPort(viewPortXML);
        List<String> appFiles = getAppFilesFromViewPort(viewPortXML);

        //Add lines to build.xml template files
        String libXML = fillTemplateFile(libFiles, "scripts/yui-compressor-ant-task/doc/lib/build_template.xml");
        String appXML = fillTemplateFile(appFiles, "scripts/yui-compressor-ant-task/doc/example/build_template.xml");

        //Write files
        writeToFile(libXML, "scripts/yui-compressor-ant-task/doc/lib/build.xml")
        writeToFile(appXML, "scripts/yui-compressor-ant-task/doc/example/build.xml")
    }

    /**
     * Read template xml file and fill js files path from viewport
     * @param lines All js files to include
     * @param file Template file
     * @return XML string with all js files include template
     */
    public static String fillTemplateFile(List<String> lines, String file) {

        //read template file
        def inputStreamFileSource = new FileInputStream(file);
        def builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        Document doc = builder.parse(inputStreamFileSource)
        def buildXML = doc.documentElement

        def build = null


        def targetsElem = buildXML.getElementsByTagName("target")
        (0..<targetsElem.length).each {
            def targetElem = targetsElem.item(it)
            def scriptElemSrc = targetElem.attributes.getNamedItem('name').nodeValue.toString()
            if (scriptElemSrc.startsWith("build")) build = targetElem;
        }

        //save it as a new files
        def concat = build.getElementsByTagName("concat").item(0)
        if (concat.hasChildNodes()) {
            while (concat.childNodes.length >= 1) {
                concat.removeChild(concat.firstChild);
            }
        }

        //write lines
        lines.each {
            Element fileset = doc.createElement('fileset')
            fileset.setAttribute("dir", "\${src.dir}")
            fileset.setAttribute("includes", it)
            concat.appendChild(fileset)
        }

        //save it as xml and return it as string
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);

        String xmlString = result.getWriter().toString();
        return xmlString
    }

    /**
     * Get all lib js files from viewPort
     * @param viewPortXML ViewPortXML
     * @return List of js files
     */
    public static List<String> getLibFilesFromViewPort(def viewPortXML) {

        List<String> libFiles = new ArrayList<String>()

        //get root element
        def gElem = getGElement(viewPortXML)

        //get all script element
        def scriptElems = gElem.getElementsByTagName("script")

        //browse script elements and get src value if element is lib
        (0..<scriptElems.length).each {
            def scriptElem = scriptElems.item(it)

            if (scriptElem.attributes.getNamedItem('src') != null) {
                def scriptElemSrc = scriptElem.attributes.getNamedItem('src').nodeValue.toString()
                if (scriptElemSrc.startsWith("lib")) libFiles.add(scriptElemSrc.replace("?version=<g:meta name=\"app.version\"/>",""));
            }
        }

        libFiles.each {
            log.info "lib -> " + it
        }

        return libFiles
    }

    /**
     * Get all application js files from viewPort
     * @param viewPortXML ViewPortXML
     * @return List of js files
     */
    public static List<String> getAppFilesFromViewPort(def viewPortXML) {

        List<String> appFiles = new ArrayList<String>()

        //get root element
        def gElem = getGElement(viewPortXML)

        //get all script element
        def scriptElems = gElem.getElementsByTagName("script")

        //browse script elements and get src value if element is app
        (0..<scriptElems.length).each {
            def scriptElem = scriptElems.item(it)

            if (scriptElem.attributes.getNamedItem('src') != null) {
                def scriptElemSrc = scriptElem.attributes.getNamedItem('src').nodeValue.toString()
                if (scriptElemSrc.startsWith("app")) appFiles.add(scriptElemSrc.replace("?version=<g:meta name=\"app.version\"/>",""));
            }
        }
        appFiles.each {
            log.info "app -> " + it
        }

        return appFiles
    }

    /**
     * Get g element which is the parent of all import files
     * @param viewPortXML View port xml
     * @return G element
     */
    public static def getGElement(def viewPortXML) {
        def g = null
        viewPortXML.getElementsByTagName("head").item(0).childNodes.each {
            if (it.nodeName.equals("g:if") && g == null) g = it
        }
        return g
    }

    /**
     * Convert stream to string and start at line offset
     * @param is Stream
     * @param offset Line where the stream start to read
     * @return String with stream content
     * @throws IOException Error when reading file
     */
    public static String convertStreamToString(InputStream is, int offset) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = new StringBuilder();
        String line;
        int i = 0;
        while ((line = r.readLine()) != null) {
            if (i >= offset) {
                total.append(line);
            }
            i++
        }
        return total.toString()
    }

    /**
     * Write an xml string to a file
     * @param xmlString XML String
     * @param filePath Destination file
     */
    public static void writeToFile(def xmlString, def filePath) {
        new File("$filePath").withWriter { out ->
            log.info xmlString
        }
    }
}
