CORE_URL=localhost-core
IMS_URLS="[localhost-ims,localhost-ims2]"
UPLOAD_URL=localhost-upload
RETRIEVAL_URL=localhost-retrieval
IIP_OFF_URL=localhost-iip-base
IIP_VENT_URL=localhost-iip-ventana
IIP_CYTO_URL=localhost-iip-cyto
IIP_JP2_URL=localhost-iip-jp2000

HAS_GLUSTER=false
GLUSTER_SERVER=192.168.0.202
VOLUME=aurora

IS_LOCAL=true

# BACKUP_BOOL : backup active or not
BACKUP_BOOL=false
# SENDER_EMAIL, SENDER_EMAIL_PASS, SENDER_EMAIL_SMTP : email params of the sending account
# RECEIVER_EMAIL : email adress of the receiver
SENDER_EMAIL='your.email@gmail.com'
SENDER_EMAIL_PASS='passwd'
SENDER_EMAIL_SMTP_HOST='smtp.gmail.com'
SENDER_EMAIL_SMTP_PORT='587'
RECEIVER_EMAIL='receiver@XXX.com'

#possible values : memory, redis
RETRIEVAL_ENGINE=memory


IMS_STORAGE_PATH=/data
IMS_BUFFER_PATH=/data/_buffer

BIOFORMAT_ENABLED="false"

# You don't to change the datas below this line instead of advanced customization
# ---------------------------

CORE_WAR_URL="http://cytomine.be/release/core/ROOT.war"
IMS_WAR_URL="http://cytomine.be/release/ims/ROOT.war"
RETRIEVAL_JAR_URL="http://cytomine.be/release/retrieval/CBIRest-0.2.0.zip"

MEMCACHED_PASS="mypass"

BIOFORMAT_JAR_URL="http://cytomine.be/release/bioformat/BioFormatStandAlone.tar.gz"
BIOFORMAT_ALIAS="bioformat"
BIOFORMAT_PORT="4321"

