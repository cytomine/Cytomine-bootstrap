# Cytomine Core

## Prerequisites

This module requires these dependencies :
PostgreSQL >= 9.2
Postgis >= 1.5
Grails >= 2.3.5

### Install PostgreSQL

#### On Max OS X

First, install Homebrew (http://brew.sh/). 
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