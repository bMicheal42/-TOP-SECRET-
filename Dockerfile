FROM docker.io/clojure:lein-alpine

ENV DB_HOST="35.246.52.38"
ENV DB_PORT="5432"
ENV DB_USER="postgres"
ENV DB_PASSWORD="password"

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN lein uberjar

CMD ["java", "-jar", "target/untitled-0.1.0-SNAPSHOT-standalone.jar"]