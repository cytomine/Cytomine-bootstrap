import be.cytomine.security.User
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole
import be.cytomine.project.Scan
import be.cytomine.warehouse.Mime
import be.cytomine.warehouse.Data
import be.cytomine.acquisition.Scanner
import be.cytomine.server.ImageServer
import be.cytomine.server.MimeImageServer
import be.cytomine.security.Group
import be.cytomine.security.UserGroup
import be.cytomine.project.Project
import be.cytomine.project.ProjectGroup
import be.cytomine.project.Slide
import be.cytomine.project.ProjectSlide
import be.cytomine.command.Transaction

class BootStrap {
  def springSecurityService
  def init = { servletContext ->

    /* Groups */
    def groupsSamples = [
            [name : "GIGA"],
            [name : "LBTD"] ,
            [name : "ANAPATH"]
    ]
    createGroups(groupsSamples)


    /* Users */
    def usersSamples = [
            [username : 'rmaree', firstname : 'Raphaël', lastname : 'Marée', email : 'rmaree@ulg.ac.be', group : [[ name :"GIGA"]]],
            [username : 'lrollus', firstname : 'Loic', lastname : 'Rollus', email : 'lrollus@ulg.ac.be', group : [[ name :"GIGA"]]],
            [username : 'stevben', firstname : 'Benjamin', lastname : 'Stévens', email : 'bstevens@ulg.ac.be', group : [[ name :"GIGA"], [name : "ANAPATH"]]]
    ]
    createUsers(usersSamples)


    /* Scanners */
    def scannersSamples = [
            [brand : "gigascan", model : "MODEL1"]
    ]
    createScanners(scannersSamples)


    /* MIME Types */
    def mimeSamples = [
            [extension : "jp2", mimeType : "image/jp2"],
            [extension : "tif", mimeType : "image/tiff"],
    ]
    createMimes(mimeSamples)


    /* Image Server */
    def imageServerSamples =  [
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is1.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is2.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is3.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is4.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver'
            ],
            [
                    'name' : 'Adore-Djatoka',
                    'url' : 'http://is5.cytomine.be:38',
                    'service' : '/adore-djatoka/resolver',
                    'className' : 'DjatokaResolver'
            ]

    ]
    createImageServers(imageServerSamples)

    /* Projects */
    def projectSamples = [
            [name : "GIGA-DEV", updated : null, deleted : null,  groups : [[ name :"GIGA"]]]
    ]

    createProjects(projectSamples)

    /* Slides */
    def slideSamples = [
            [name : "testSlide", order : 8, projects : [[name : "GIGA-DEV"]]]
    ]
    createSlides(slideSamples)

    /* Scans */
    def scanSamples = [
            [filename: 'Boyden - essai _10x_02',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Boyden/essai_10x_02.one.jp2',slide : 'testslide' ],
            [filename: 'Aperio - 003',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/003.jp2',slide : 'testslide' ],
            [filename: 'Aperio - 2005900969-2', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/2005900969-2.jp2',slide : 'testslide' ],
            [filename: 'bottom-nocompression', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/bottom-nocompression-crop-8levels-256.jp2',slide : 'testslide' ],
            [filename: '70pc_cropnew', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/PhDelvenne/2_02_JPEG_70pc_cropnew.jp2',slide : 'testslide' ],
            [filename: 'Agar seul 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Agar-seul-1.jp2',slide : 'testslide' ],
            [filename: 'Agar seul 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Agar-seul-2.jp2',slide : 'testslide' ],
            [filename: 'Curcu 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-1.jp2',slide : 'testslide' ],
            [filename: 'Curcu 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-2.jp2',slide : 'testslide' ],
            [filename: 'Curcu 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-3.jp2',slide : 'testslide' ],
            [filename: 'Curcu 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-4.jp2',slide : 'testslide' ],
            [filename: 'Curcu 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-5.jp2',slide : 'testslide' ],
            [filename: 'Curcu 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-6.jp2',slide : 'testslide' ],
            [filename: 'Curcu 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-7.jp2',slide : 'testslide' ],
            [filename: 'Curcu non soluble 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-1.jp2',slide : 'testslide' ],
            [filename: 'Curcu non soluble 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-2.jp2',slide : 'testslide' ],
            [filename: 'Curcu non soluble 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-3.jp2',slide : 'testslide' ],
            [filename: 'Curcu non soluble 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-4.jp2',slide : 'testslide' ],
            [filename: 'Curcu non soluble 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Curcu-non-soluble-5.jp2',slide : 'testslide' ],
            [filename: 'Gemzar 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-1.jp2',slide : 'testslide' ],
            [filename: 'Gemzar 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-2.jp2',slide : 'testslide' ],
            [filename: 'Gemzar 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-3.jp2',slide : 'testslide' ],
            [filename: 'Gemzar 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-4.jp2',slide : 'testslide' ],
            [filename: 'Gemzar 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-5.jp2',slide : 'testslide' ],
            [filename: 'Gemzar 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-6.jp2',slide : 'testslide' ],
            [filename: 'Gemzar 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-7.jp2',slide : 'testslide' ],
            [filename: 'Gemzar 8', path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-8.jp2',slide : 'testslide' ],
            [filename: 'Gemzar + Curcu 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-1.jp2',slide : 'testslide' ],
            [filename: 'Gemzar + Curcu 2',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-2.jp2',slide : 'testslide' ],
            [filename: 'Gemzar + Curcu 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-3.jp2',slide : 'testslide' ],
            [filename: 'Gemzar + Curcu 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-4.jp2',slide : 'testslide' ],
            [filename: 'Gemzar + Curcu 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-5.jp2',slide : 'testslide' ],
            [filename: 'Gemzar + Curcu 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-6.jp2',slide : 'testslide' ],
            [filename: 'Gemzar + Curcu 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/Gemzar-Curcu-7.jp2',slide : 'testslide' ],
            [filename: 'HPg 1',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-1.jp2',slide : 'testslide' ],
            [filename: 'HPg 3',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-3.jp2',slide : 'testslide' ],
            [filename: 'HPg 4',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-4.jp2',slide : 'testslide' ],
            [filename: 'HPg 5',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-5.jp2',slide : 'testslide' ],
            [filename: 'HPg 6',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-6.jp2',slide : 'testslide' ],
            [filename: 'HPg 7',path:'file:///media/datafast/tfeweb2010/BDs/WholeSlides/DCataldo/20090805-20090810/HPg-7.jp2',slide : 'testslide' ]
    ]
    createScans(scanSamples)

    def destroy = {
    }

  }

  /* Methods */

  def createGroups(groupsSamples) {
    def groups = Group.list() ?: []
    //if (!groups) {
    groupsSamples.each { item->
      def group = new Group(name : item.name)
      if (group.validate()) {
        println "Creating group ${group.name}..."

        group.save(flush : true)

        groups << group
      }
      else {
        println("\n\n\n Errors in group boostrap for ${item.name}!\n\n\n")
        group.errors.each {
          err -> println err
        }
      }
    }
    //}
  }


  def createUsers(usersSamples) {
    def userRole = SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority : "ROLE_USER").save()
    def adminRole = SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority : "ROLE_ADMIN").save()

    def users = User.list() ?: []
    if (!users) {
      usersSamples.each { item ->
        User user = new User(
                username : item.username,
                firstname : item.firstname,
                lastname : item.lastname,
                email : item.email,
                password : springSecurityService.encodePassword("password"),
                dateCreated : new Date(),
                enabled : true)
        if (user.validate()) {
          println "Creating user ${user.username}..."
         // user.addToTransactions(new Transaction())
          user.save(flush : true)

          /* Create a special group the user */
          def userGroupName = item.username
          def userGroup = [
                  [name : userGroupName]
          ]
          createGroups(userGroup)
          Group group = Group.findByName(userGroupName)
          UserGroup.link(user, group)

          /* Handle groups */
          item.group.each { elem ->
            group = Group.findByName(elem.name)
            UserGroup.link(user, group)
          }

          /* Add Roles */
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
  }

  def createScanners(scannersSamples) {
    def scanners = Scanner.list() ?: []
    if (!scanners) {
      scannersSamples.each { item ->
        Scanner scanner = new Scanner(brand : item.brand, model : item.model)

        if (scanner.validate()) {
          println "Creating scanner ${scanner.brand} - ${scanner.model}..."

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
  }

  def createMimes(mimeSamples) {
    def mimes = Mime.list() ?: []
    if (!mimes) {
      mimeSamples.each { item ->
        Mime mime = new Mime(extension : item.extension,
                mimeType : item.mimeType,
                imageServer: ImageServer.findById(1))
        if (mime.validate()) {
          println "Creating mime ${mime.extension} : ${mime.mimeType}..."

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
  }

  def createImageServers(imageServerSamples) {
    def imageServers = ImageServer.list() ?: []
    if (!imageServers) {
      imageServerSamples.each { item ->
        ImageServer imageServer = new ImageServer(
                name : item.name,
                url : item.url,
                service : item.service,
                className : item.className)

        if (imageServer.validate()) {
          println "Creating image server ${imageServer.name}... : ${imageServer.url}"

          imageServer.save()

          imageServers << imageServer

          /* Link with MIME JP2 */
          Mime mime = Mime.findByExtension("jp2")
          MimeImageServer.link(imageServer, mime)

        } else {
          println("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
          imageServer.errors.each {
            err -> println err
          }
        }
      }
    }
  }


  def createProjects(projectSamples) {
    def projects = Project.list() ?: []
    if (!projects) {
      projectSamples.each { item->
        def project = new Project(
                name : item.name,
                created : new Date(),
                updated : item.updated,
                deleted : item.deleted
        )
        if (project.validate()){
          println "Creating project  ${project.name}..."

          project.save(flush : true)

          /* Handle groups */
          item.groups.each { elem ->
            Group group = Group.findByName(elem.name)
            ProjectGroup.link(project, group)
          }

          projects << project

        } else {
          println("\n\n\n Errors in project boostrap for ${item.name}!\n\n\n")
          project.errors.each {
            err -> println err
          }
        }
      }
    }
  }

  def createSlides(slideSamples) {
    def slides = Slide.list() ?: []
    if (!slides) {
      slideSamples.each {item->
        def slide = new Slide(name : item.name, order : item.order)

        if (slide.validate()) {
          println "Creating slide  ${item.name}..."

          slide.save(flush : true)

          /* Link to projects */
          item.projects.each { elem ->
            Project project = Project.findByName(elem.name)
            ProjectSlide.link(project, slide)
          }

        } else {
          println("\n\n\n Errors in slide boostrap for ${item.name}!\n\n\n")
          slide.errors.each {
            err -> println err
          }
        }
      }
    }
  }

  def createScans(scanSamples) {
    def scans = Scan.list() ?: []
    if (!scans) {
      scanSamples.each { item ->
        def mime = Mime.findByExtension("jp2")

        def scanner = Scanner.findByBrand("gigascan")

        def data = new Data(path : item.path, mime : mime)

        if (data.validate()) {
          println "Creating data ${data.path}..."

          data.save(flush : true)


          def scan = new Scan(
                  filename: item.filename,
                  data : data,
                  scanner : scanner,
                  slide : Slide.findByName(item.slide)
          )

          if (scan.validate()) {
            println "Creating scan : ${scan.filename}..."

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
  }
}
