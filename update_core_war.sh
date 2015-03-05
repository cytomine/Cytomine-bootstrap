docker stop core
docker rm -v core
docker stop nginx
docker rm -v nginx

sh create_docker_images.sh
sh start_deploy.sh



