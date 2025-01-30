# extend base image with apache mvn for openapi generator
FROM mcr.microsoft.com/devcontainers/typescript-node:1-22-bookworm

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

RUN wget https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.deb && \
    dpkg -i jdk-21_linux-x64_bin.deb

RUN mvn -version
