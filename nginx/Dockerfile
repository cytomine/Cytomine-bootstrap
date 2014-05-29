# Pull base image.
FROM cytomine/base

MAINTAINER BS "b.stevens@ulg.ac.be"

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

ADD nginx.conf /tmp/nginx.conf
RUN cat /tmp/nginx.conf > /etc/nginx/nginx.conf

# Define working directory.
WORKDIR /etc/nginx

# Define default command.
CMD ["nginx"]

# Expose ports.
EXPOSE 80
EXPOSE 443
