package be.cytomine.project

class ScanController {

    def scaffold = Scan

    def browse = {
      //println "params.id="+params.id
       Scan scan = Scan.findById(params.id)
      //generate possibles urls for the OpenLayers.Layer instance
       def urls = '["' + scan.getData().getMime().imageServers().url.join('","') + '"]'
       ['scan' : scan, 'urls' : urls]
    }
}
