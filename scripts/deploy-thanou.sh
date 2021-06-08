#!/bin/bash

server_ip=62.171.136.62

cd "$(dirname "$0")"
cd ..

scp target/thanou.jar root@$server_ip:/kgdev/thanou/
ssh -t root@$server_ip "sudo systemctl restart thanou.service"
