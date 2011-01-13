package be.cytomine.api

import be.cytomine.project.Scan
import be.cytomine.server.ImageServer
import be.cytomine.server.resolvers.DjatokaResolver
import org.codehaus.groovy.tools.GroovyClass


class RestimageController {


    Map<String, Object> resolvers = new HashMap<String,Object>()
    private final static  String imageNotFound = "http://www.montefiore.ulg.ac.be/~maree/cytomine/cyto.png"


    def thumb = {
      resolvers.put("DjatokaResolver", new DjatokaResolver())
      Scan scan = Scan.findById(params.idscan)
      ImageServer imageServer = scan.getData().getMime().imageServer
      println imageServer.getName()
      if (imageServer != null) {
        def obj = resolvers.get(imageServer.className)
        if (obj instanceof DjatokaResolver) {
            DjatokaResolver resolver = (DjatokaResolver) obj
            resolver.args.put(resolver.getFileKey(), scan.getData().getPath())
            String url = resolver.toURL(imageServer.url)
            redirect(url:url)
        }
        else {
          redirect(url:imageNotFound)
        }
      } else {
        redirect(url:imageNotFound)
      }

    }
}



