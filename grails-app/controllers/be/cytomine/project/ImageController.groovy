package be.cytomine.project

class ImageController {

    def scaffold = Image

    def browse = {
      //println "params.id="+params.id
       Image scan = Image.findById(params.id)
      //generate possibles urls for the OpenLayers.Layer instance
       def urls = '["' + scan.getData().getMime().imageServers().url.join('","') + '"]'
       ['scan' : scan, 'urls' : urls]
    }
}
