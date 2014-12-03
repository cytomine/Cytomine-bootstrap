docker stop core
docker rm core
docker stop nginx
docker rm nginx

sh create_docker_images.sh
sh start_deploy.sh



