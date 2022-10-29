#!/bin/bash

GKE_PROJECT=river-bruin-366614
GKE_CLUSTER=health-samurai-cluster
GKE_SERVICE_ACCOUNT=health-samurai-serviceaccount
GKE_ZONE=europe-west3-a

gcloud config set project $GKE_PROJECT
# Delete the cluster
gcloud container clusters delete $GKE_CLUSTER --region $GKE_ZONE -q

# Delete service account
gcloud iam -q service-accounts delete "$GKE_SERVICE_ACCOUNT@$GKE_PROJECT.iam.gserviceaccount.com"

# Delete repository
gcloud artifacts -q repositories delete $GKE_PROJECT --location $GKE_REGION


#Delete all images in Gcloud
for digest in $(gcloud container images list-tags gcr.io/river-bruin-366614/health-samurai --format='get(digest)')
do
  gcloud container images delete -q --force-delete-tags "gcr.io/river-bruin-366614/health-samurai@${digest}"
done

#Delete images in Docker
docker image prune -af