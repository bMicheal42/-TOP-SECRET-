FROM docker.io/clojure:lein-alpine

ENV POSTGRES_PASSWORD="password"
ENV MY_DOG=Rex\ The\ Dog
ENV MY_CAT=fluffy

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN lein uberjar

CMD ["java", "-jar", "target/untitled-0.1.0-SNAPSHOT-standalone.jar"]