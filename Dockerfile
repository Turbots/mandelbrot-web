FROM openjdk:8-jre-alpine
LABEL maintainer="dhubau@gmail.com"
VOLUME /tmp
EXPOSE 8080
ADD target/mandelbrot-web-0.0.4-SNAPSHOT.jar mandelbrot-web.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/mandelbrot-web.jar"]