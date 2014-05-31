all:
	echo "Can do 'clean', 'image' or 'container'"
clean:
	sudo docker stop dnsmasq
	sudo docker rm dnsmasq

image:	
	sudo docker build -rm -t='sroegner/dnsmasq' .

container:
	sudo docker run \
	-v="$$(pwd)/dnsmasq.hosts:/dnsmasq.hosts" \
	-name=dnsmasq \
	-p='127.0.0.10:53:5353/udp' \
	-d sroegner/dnsmasq
	sudo docker ps 
	sudo docker logs $$(sudo docker ps -l -q)
