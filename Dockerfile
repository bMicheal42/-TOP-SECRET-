FROM docker.io/clojure:lein-alpine

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN lein uberjar
RUN ./build.sh

CMD ["java", "-jar", "target/untitled-0.1.0-SNAPSHOT-standalone.jar"]