import be.cytomine.security.User
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.project.Scan
import be.cytomine.warehouse.Mime
import be.cytomine.warehouse.Data
import be.cytomine.acquisition.Scanner
import be.cytomine.server.ImageServer


class BootStrap {
  def springSecurityService
  def init = { servletContext ->

    /* Users */

    def usersSamples = [
            [username : 'rmaree', firstname : 'Raphaël', lastname : 'Marée', email : 'rmaree@ulg.ac.be'],
            [username : 'lrollus', firstname : 'Loic', lastname : 'Rollus', email : 'lrollus@ulg.ac.be'],
            [username : 'stevben', firstname : 'Benjamin', lastname : 'Stévens', email : 'bstevens@ulg.ac.be']
    ]

    def userRole = SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority : "ROLE_USER").save()
    def adminRole = SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority : "ROLE_ADMIN").save()

    def users = User.list() ?: []
    if (!users) {
      usersSamples.each { item ->
        def user = new User(
                username : item.username,
                firstname : item.firstname,
                lastname : item.lastname,
                email : item.email,
                password : springSecurityService.encodePassword("password"),
                dateCreated : new Date(),
                enabled : true)
        if (user.validate()) {
          println "Creating user ${user.username}..."

          user.save(flush : true)

          SecUserSecRole.create(user, userRole)
          SecUserSecRole.create(user, adminRole)

          users << user
        } else {
          println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
          user.errors.each {
            err -> println err
          }
        }
      }
    }

    /* Scanners */

    def scannersSamples = [
            [brand : "gigascan", model : "MODEL1"]
    ]

    def scanners = Scanner.list() ?: []
    if (!scanners) {
      scannersSamples.each { item ->
        Scanner scanner = new Scanner(brand : item.brand, model : item.model)

        if (scanner.validate()) {
          println "Creating scanner ${scanner.brand} - ${scanner.model}"

          scanner.save(flush : true)

          scanners << scanner
        } else {
          println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
          scanner.errors.each {
            err -> println err
          }
        }
      }
    }

    /* Image Server */
    def imageServerSamples =  [
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://139.165.108.140:38/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver'
            ]
    ]

    def imageServers = ImageServer.list() ?: []
    if (!imageServers) {
      imageServerSamples.each { item ->
        ImageServer imageServer = new ImageServer(name : item.name, url : item.url, className : item.className)

        if (imageServer.validate()) {
          println "Creating image server ${imageServer.name}"

          imageServer.save()

          imageServers << imageServer
        } else {
          println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
          imageServer.errors.each {
            err -> println err
          }
        }
      }
    }

    /* MIME Types */
    def mimeSamples = [
            [extension : "jp2", mimeType : "image/jp2"],
            [extension : "tif", mimeType : "image/tiff"],
    ]

    def mimes = Mime.list() ?: []
    if (!mimes) {
      mimeSamples.each { item ->
        Mime mime = new Mime(extension : item.extension, mimeType : item.mimeType, imageServer: ImageServer.findById(1))
        if (mime.validate()) {
          println "Creating mime ${mime.extension} : ${mime.mimeType}"

          mime.save(flush : true)



          mimes << mime
        } else {
          println("\n\n\n Errors in account boostrap for ${mime.extension} : ${mime.mimeType}!\n\n\n")
          mime.errors.each {
            err -> println err
          }
        }
      }
    }


    /* Scans */
    def imagesSamples = [
            [filename : 'Boyden - essai _10x_02', path : 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Boyden/essai_10x_02.one.jp2'],
            [filename : 'Aperio - 003' , path : 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/003.jp2'],
            [filename : 'Aperio - 2005900969-2', path : 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/2005900969-2.jp2']
    ]

    def scans = Scan.list() ?: []
    if (!scans) {
      imagesSamples.each { item ->
        def mime = Mime.findByExtension("jp2")

        def scanner = Scanner.findByBrand("gigascan")

        def data = new Data(path : item.path, mime : mime)

        if (data.validate()) {
          println "Create data ${data.path}..."

          data.save(flush : true)


          def scan = new Scan(
                  filename: item.filename,
                  data : data,
                  scanner : scanner
          )
          if (scan.validate()) {
            println "Create scan : ${scan.filename}..."

            scan.save(flush : true)

            scans << scan
          } else {
            println("\n\n\n Errors in account boostrap for ${item.filename}!\n\n\n")
            scan.errors.each {
              err -> println err
            }

          }
        } else {
          println("\n\n\n Errors in account boostrap for ${item.filename}!\n\n\n")
          data.errors.each {
            err -> println err
          }

        }
      }
    }




    def destroy = {
    }
  }
}
