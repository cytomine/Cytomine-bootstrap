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
            [name : "GIGA-DEV", updated : null, deleted : null,  group : [[ name :"GIGA"]]]
    ]

    createProjects(projectSamples)

    /* Scans */
    def scanSamples = [
            [filename : 'Boyden - essai _10x_02', path : 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Boyden/essai_10x_02.one.jp2'],
            [filename : 'Aperio - 003' , path : 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/003.jp2'],
            [filename : 'Aperio - 2005900969-2', path : 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Aperio/2005900969-2.jp2'],
            [filename : 'Agar_seul_1', path : 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Cops/Agar_seul_1.jp2'],
            [filename : 'Curcu2', path : 'file:///media/datafast/tfeweb2010/BDs/WholeSlides/Cops/Curcu2.jp2']
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

          /* Create a special group the user */
          def userGroupName = item.username + "_private_group"
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
          item.group.each { elem ->
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


  def createScans(scanSamples) {
    def scans = Scan.list() ?: []
    if (!scans) {
      scanSamples.each { item ->
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
  }
}
