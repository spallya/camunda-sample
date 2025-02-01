## Vider Quantum Engine

### How to build and deploy docker image
```bash
# Build image using below commands
sudo docker build -t vider-quantumn:8.0 .
sudo docker image tag vider-quantumn:8.0 spallya/vider-quantumn:8.0
sudo docker image push spallya/vider-quantumn:8.0

# In application server, run image using below commands
sudo docker pull spallya/vider-quantumn:8.0
sudo docker run -d -p 80:8080 -e spring.profiles.active=dev -t spallya/vider-quantumn:8.0
```

sudo docker run -p 4444:4444 -p 3006:3006 --network=bridge -t atom-pro:1.0

sudo docker run -p 4444:4444 -p 3006:3006 --network=bridge\
-v /tmp/.X11-unix:/tmp/.X11-unix \
-e DISPLAY=unix$DISPLAY \
atom-pro:1.0
