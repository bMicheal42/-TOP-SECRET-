# Create a Cluster
gcloud container clusters create health-samurai
gcloud container clusters get-credentials health-samurai

# Create Docker image and push it
docker image build -t clj-on-k8s/health-samurai . #TODO остается предыдущий Image в docker
docker tag clj-on-k8s/health-samurai gcr.io/river-bruin-366614/health-samurai
gcloud --quiet auth configure-docker
docker push gcr.io/river-bruin-366614/health-samurai  #TODO остается предыдущий Image в cloud

# Deploy in kuber
kubectl run health-samurai --image gcr.io/river-bruin-366614/health-samurai --port 3000
kubectl expose pods health-samurai --type "LoadBalancer"