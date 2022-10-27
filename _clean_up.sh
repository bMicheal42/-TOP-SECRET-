#Delete the application's Service Gcloud
kubectl delete --force=true pod health-samurai

#Delete your cluster Gcloud
gcloud container -q clusters delete health-samurai

#Delete all images in Gcloud
for digest in $(gcloud container images list-tags gcr.io/river-bruin-366614/health-samurai --format='get(digest)')
do
  gcloud container images delete -q --force-delete-tags "gcr.io/river-bruin-366614/health-samurai@${digest}"
done

#Delete images in Docker
docker image prune -af