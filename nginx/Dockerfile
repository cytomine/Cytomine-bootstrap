# Pull base image.
FROM cytomine/base

MAINTAINER BS "b.stevens@ulg.ac.be"

RUN apt-get update
RUN apt-get install -y software-properties-common

# Install Nginx.
RUN \
  add-apt-repository -y ppa:nginx/stable && \
  apt-get update && \
  apt-get install -y nginx-extras && \
  echo "\ndaemon off;" >> /etc/nginx/nginx.conf && \
  chown -R www-data:www-data /var/lib/nginx

# Define mountable directories.
#VOLUME ["/data", "/etc/nginx/sites-enabled", "/var/log/nginx"]

ADD nginx.conf.sample /tmp/nginx.conf.sample

# Define working directory.
WORKDIR /etc/nginx

# Define default command.
#CMD ["nginx"]

# Expose ports.
EXPOSE 80
EXPOSE 443

ADD deploy.sh /tmp/deploy.sh
RUN chmod +x /tmp/deploy.sh

ENTRYPOINT ["/tmp/deploy.sh"]
