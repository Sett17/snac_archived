FROM golang:1.19-bullseye as build

WORKDIR /

RUN git clone https://github.com/Sett17/snac.git

#builds a static binary
RUN cd snac && CGO_ENABLED=0 go build -tags netgo

FROM alpine:3.16

COPY --from=build /snac/snac /
COPY config.yaml /

EXPOSE 8080
CMD ["/snac"]