FROM alpine:3.21
LABEL authors="rohdef"

ARG version
ARG secondary

ENV version $version
ENV secondary $secondary

WORKDIR /foo
COPY entrypoint.sh /foo/entrypoint.sh

ENTRYPOINT ["/foo/entrypoint.sh"]