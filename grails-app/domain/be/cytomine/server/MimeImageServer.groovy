package be.cytomine.server

import be.cytomine.warehouse.Mime

class MimeImageServer {

  ImageServer imageServer
  Mime mime

  static MimeImageServer link(ImageServer imageServer, Mime mime) {
    def mis = MimeImageServer.findByImageServerAndMime(imageServer, mime)
    if (!mis)
    {
      mis = new MimeImageServer()
      imageServer?.addToMis(mis)
      mime?.addToMis(mis)
      mis.save()
    }
    return mis
  }

  static void unlink(ImageServer imageServer,Mime mime) {
    def mis = MimeImageServer.findByImageServerAndMime(imageServer, mime)
    if (mis)
    {
      imageServer?.removeFromMis(mis)
      mime?.removeFromMis(mis)
      mis.delete()
    }
  }



}
