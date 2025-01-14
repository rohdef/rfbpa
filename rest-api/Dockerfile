FROM eclipse-temurin:23-jre-alpine

EXPOSE 8080
LABEL authors="rohdef"

COPY build/distributions/rfbpa-web.tar ./

RUN tar xf rfbpa-web.tar

ENTRYPOINT ["sh", "rfbpa-web/bin/rfbpa-web"]
