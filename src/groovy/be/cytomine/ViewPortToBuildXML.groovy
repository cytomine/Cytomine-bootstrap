package be.cytomine

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
 * 
 */
class ViewPortToBuildXML {

    public static void process() {
        //Read viewport
        def inputStreamFileSourceFull = new FileInputStream("grails-app/views/layouts/viewport.gsp");
        //must skip line 0->11 because not valid xml
        String content = convertStreamToString(inputStreamFileSourceFull,12)
        def  inputStreamFileSource = new ByteArrayInputStream(content.getBytes("UTF-8"));

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        Element viewPortXML = builder.parse(inputStreamFileSource).documentElement

        //Get lib/app src js files from viewports
        List<String> libFiles = getLibFilesFromViewPort(viewPortXML);
        List<String> appFiles = getAppFilesFromViewPort(viewPortXML);

        //Add lines to build.xml template files
        String libXML = addLinesToConcatElem(libFiles, "scripts/yui-compressor-ant-task/doc/lib/build_template.xml");
        String appXML = addLinesToConcatElem(appFiles, "scripts/yui-compressor-ant-task/doc/example/build_template.xml");

        //Write files
        writeToFile(libXML,"scripts/yui-compressor-ant-task/doc/lib/build.xml")
        writeToFile(appXML,"scripts/yui-compressor-ant-task/doc/example/build.xml")
    }


    public static void writeToFile(def xmlString, def filePath) {
      new File("$filePath").withWriter { out ->
          out.println xmlString
      }
    }

    public static String addLinesToConcatElem(List<String> lines, String file) {
        //readLibFilesXML
        def inputStreamFileSource = new FileInputStream(file);
        def builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        Document doc = builder.parse(inputStreamFileSource)
        def buildXML = doc.documentElement

        //addLibFilesToBuilXML
        def build = null

        def targetsElem = buildXML.getElementsByTagName("target")
        (0..<targetsElem.length).each{
            def targetElem = targetsElem.item(it)
            def scriptElemSrc = targetElem.attributes.getNamedItem('name').nodeValue.toString()

            if(scriptElemSrc.startsWith("build")) build = targetElem;
        }

        //save it as a new files

        def concat= build.getElementsByTagName("concat").item(0)

         if ( concat.hasChildNodes() )
        {
            while ( concat.childNodes.length >= 1 )
            {
                concat.removeChild( concat.firstChild );
            }
        }


        lines.each {
            Element fileset = doc.createElement('fileset')
            fileset.setAttribute("dir","\${src.dir}")
            fileset.setAttribute("includes",it)
            concat.appendChild(fileset)
        }
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        //initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);

        String xmlString = result.getWriter().toString();
        System.out.println(xmlString);
        return xmlString
    }


    public static List<String> getLibFilesFromViewPort(def viewPortXML) {
        List<String> libFiles = new ArrayList<String>()

        def gElem = getGElement(viewPortXML)

        def scriptElems = gElem.getElementsByTagName("script")

        (0..<scriptElems.length).each{
            def scriptElem = scriptElems.item(it)

            if(scriptElem.attributes.getNamedItem('src')!=null)  {
                def scriptElemSrc = scriptElem.attributes.getNamedItem('src').nodeValue.toString()
                if(scriptElemSrc.startsWith("lib")) libFiles.add(scriptElemSrc);
            }

        }

        libFiles.each {
            println "lib -> " + it
        }

        return libFiles
    }

    public static List<String> getAppFilesFromViewPort(def viewPortXML) {

        List<String> appFiles = new ArrayList<String>()


        def gElem = getGElement(viewPortXML)

        def scriptElems = gElem.getElementsByTagName("script")

        (0..<scriptElems.length).each{
            def scriptElem = scriptElems.item(it)

            if( scriptElem.attributes.getNamedItem('src')!=null) {
                def scriptElemSrc = scriptElem.attributes.getNamedItem('src').nodeValue.toString()
                if(scriptElemSrc.startsWith("app")) appFiles.add(scriptElemSrc);
            }
        }
        appFiles.each {
            println "app -> " + it
        }

        return appFiles
    }




    public static def getGElement(def records) {
        def g = null
        records.getElementsByTagName("head").item(0).childNodes.each {
            if(it.nodeName.equals("g:if") && g==null) g = it
            //println it.nodeName
        }
        return g
    }


    public static String convertStreamToString(InputStream is, int offset)
            throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = new StringBuilder();
        String line;
        int i=0;
        while ((line = r.readLine()) != null) {
            if(i>=offset) {
                //println line
                total.append(line);
            }

            i++
        }
        return total.toString()
    }
}
