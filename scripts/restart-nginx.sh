#!/bin/bash

nginx -s reload
systemctl restart nginx.service
systemctl status nginx.service
