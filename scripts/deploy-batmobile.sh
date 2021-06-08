#!/bin/bash

server_ip=62.171.136.62

scp target/batmobile.jar root@$server_ip:/kgdev/batmobile/
ssh -t root@$server_ip "sudo systemctl restart batmobile.service"
