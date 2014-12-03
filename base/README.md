# Dockerfile Base (Ubuntu 14 SSH)

## Info

This Dockerfile creates a container running Ubuntu 14.04 LTS (trusty) with SSHD server

## Install

`docker build -t cytomine/base .`

## Usage

```docker run -d -p 22 cytomine/base```

## Meta

Build with docker 1.3.0
