FROM node:22.9.0

WORKDIR /main-backend/endpoint/src/main/resources/

COPY    *.yaml .

WORKDIR /app

RUN mkdir -p /usr/src/app/ds/models

RUN apt-get update && apt-get install -y maven

COPY package.json package-lock.json ./
RUN npm install

RUN npm install --save-dev @angular/cli@18.2.10 \
    && npm install --save-dev tailwindcss postcss autoprefixer

COPY . .

EXPOSE 8080

# Start the Angular application
CMD ["npm", "start", "--", "--host", "0.0.0.0"]
