# Cytomine Core

## Prerequisites

This module requires these dependencies :
* PostgreSQL >= 9.2
* Postgis >= 1.5
* Grails >= 2.3.5

### Install PostgreSQL

#### On Max OS X

First, install Homebrew (http://brew.sh/) and follow the instructions : 
 ```bash
ruby -e "$(curl -fsSL https://raw.github.com/Homebrew/homebrew/go/install)"
 ```
then install Postgis (will instal PostgreSQL as a dependencies):
 ```bash
brew install postgis 
 ```
	
#### On Linux (Ubuntu/Debian)

 ```bash
apt-get install postgis
 ```

### Install Grails

Install GVM (http://gvmtool.net/)
 ```bash
curl -s get.gvmtool.net | bash
 ```
 Install Grails :
 ```bash
gvm install grails 2.3.5
 ```
[Grails Documentation](http://grails.org/doc/2.3.x/guide/)

## Init databases 

These are different possible environment while running Cytomine Core 
* cytomine : dev environment
* cytomineempty : scratch environment (dev environment with an empty database at start)
* cytomineprod : production environment
* cytominetest : test environment (for running integration tests)

Then create all these 4 database
 ```bash
 createdb $DATABASE_NAME;
 psql -d $DATABASE_NAME -c "CREATE EXTENSION postgis;"
  ```

## Run Cytomine Core

[Grails Command Line Documentation](http://grails.org/doc/2.3.x/guide/commandLine.html)

#### In dev mode :
```bash
export GRAILS_OPTS="-Xmx1G -Xms256m -XX:MaxPermSize=256m -server"
grails -Dserver.port=8080 run-app
```

####  In scratch mode :
```bash
# free cytominempty
dropdb cytomineempty;
createdb cytomineempty;
psql -d $cytomineempty -c "CREATE EXTENSION postgis;"
 
# start grails with scratch environment
export GRAILS_OPTS="-Xmx1G -Xms256m -XX:MaxPermSize=256m"
grails  -Dgrails.env=scratch -Dserver.port=8080 run-app
```

####  In prod mode :
```bash
export GRAILS_OPTS="-Xmx1G -Xms256m -XX:MaxPermSize=256m"
grails prod -Dserver.port=8080 run-app
```

####  In test mode :
```bash
export GRAILS_OPTS="-Xmx1G -Xms256m -XX:MaxPermSize=256m -server -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commons.logging.simplelog.showdatetime=true -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG"
grails -Dserver.port=8090 test-app functional:functional -echoOut -coverage
```

## Deployment : generate the WAR

As simple as :
```bash
grails war
```

This command will generate `./target/cytomine.war`

## Generate the documentation

As simple as 
```bash
grails doc
```

This command will generate `./target/docs/index.html`

