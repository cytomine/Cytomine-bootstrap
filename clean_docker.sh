rm -f .cookies
docker stop $(docker ps -a -q)
docker rm -v $(docker ps -a -q)
#docker rmi $(docker images -q)
