FROM gradle:8.1.1-jdk11 AS build
COPY . /src
WORKDIR /src

RUN gradle fatJar
FROM openjdk:11-jre-slim

RUN apt-get -y update
EXPOSE 8990
RUN mkdir /app


COPY --from=build /src/script/dev.env /app/.env
COPY --from=build /src/script/run-eqplus-sync.sh /app/
COPY --from=build /src/src/main/resources/ /app/
COPY --from=build /src/build/libs/EquityPlusSync.jar /app/EquityPlusSync.jar

WORKDIR /app

RUN chmod +x /app/run-eqplus-sync.sh
CMD /app/run-eqplus-sync.sh