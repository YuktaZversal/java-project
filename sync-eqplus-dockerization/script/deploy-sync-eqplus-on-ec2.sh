#!/usr/bin/env bash
source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/base.env" set
source "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/$ENVIRONMENT.env" set
#login aws
aws ecr get-login-password --region us-east-2 --output text | sudo docker login --username AWS --password-stdin $ACCOUNT_NUMBER.dkr.ecr.us-east-2.amazonaws.com

#start docker
sudo service docker start

#pull image from ECR
sudo docker pull $ACCOUNT_NUMBER.dkr.ecr.us-east-2.amazonaws.com/$REPO_NAME:latest


sudo docker run --name sync-eqplus -p 8990:8990 -d --rm $ACCOUNT_NUMBER.dkr.ecr.us-east-2.amazonaws.com/$REPO_NAME 

sudo docker logs --follow sync-eqplus >> $DIR_PATH/sync-eqplus_log_$TIMESTAMP.log 2>&1 &
sudo docker wait sync-eqplus
sudo docker rmi $ACCOUNT_NUMBER.dkr.ecr.us-east-2.amazonaws.com/$REPO_NAME:latest
exit 0

#stop docker
#sudo service docker stop
