package be.cytomine.server.resolvers

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/01/11
 * Time: 10:08
 */
class DjatokaResolver extends Resolver {

  public DjatokaResolver() {
    super()
    args.put("rft_id", "")
    args.put("url_ver", "Z39.88-2004")
    args.put("svc_id", "info:lanl-repo/svc/getRegion")
    args.put("svc_val_fmt", "info:ofi/fmt:kev:mtx:jpeg2000")
    args.put("svc.format", "image/jpeg")
    args.put("svc.scale", "192")
    //args.put("svc.level", "8")
    //args.put("svc.rotate", "0")
  }

  public String getFileKey() {
    "rft_id"
  }

}
