kubectl delete pod health-samurai

docker image build -t clj-on-k8s/health-samurai .
docker tag clj-on-k8s/health-samurai gcr.io/river-bruin-366614/health-samurai
docker push gcr.io/river-bruin-366614/health-samurai

# Deploy in kuber
kubectl run health-samurai --image gcr.io/river-bruin-366614/health-samurai --port 3000
#kubectl expose pods health-samurai --type "LoadBalancer" #mb ne nado voobshe