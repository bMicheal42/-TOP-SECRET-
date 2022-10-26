docker image prune -af
docker image build -t clj-on-k8s/health-samurai .
docker tag clj-on-k8s/health-samurai gcr.io/river-bruin-366614/health-samurai
gcloud --quiet auth configure-docker
docker push gcr.io/river-bruin-366614/health-samurai