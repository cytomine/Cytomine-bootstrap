docker stop ims
docker rm -v ims
docker stop nginx
docker rm -v nginx

sh create_docker_images.sh
sh start_deploy.sh

