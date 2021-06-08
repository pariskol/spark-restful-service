#!/bin/bash

server_ip=62.171.136.62

cd "$(dirname "$0")"
cd ..

scp target/psanta.jar root@$server_ip:/kgdev/psanta/
ssh -t root@$server_ip "sudo systemctl restart psanta.service"
