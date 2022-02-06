FROM anapsix/alpine-java
MAINTAINER Florian Moetz <florian@moetz.co.at>

COPY ./build/libs/DarkSoulsOverlay.jar /DarkSoulsOverlay.jar

EXPOSE 8000

CMD ["java", "-jar", "DarkSoulsOverlay.jar"]
