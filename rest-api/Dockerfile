FROM eclipse-temurin:17.0.11_9-jre-alpine

EXPOSE 8080
LABEL authors="rohdef"

COPY build/distributions/rfbpa-web.tar ./

RUN tar xf rfbpa-web.tar

ENTRYPOINT ["sh", "rfbpa-web/bin/rfbpa-web"]
