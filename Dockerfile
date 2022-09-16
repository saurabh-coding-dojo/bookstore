FROM maven:3.8.6-openjdk-11 
ENV DB_URL=jdbc:mysql://book.cv5jovm2xn05.us-east-2.rds.amazonaws.com:3306/devops 
ENV DB_PORT=3306 
ENV DB_NAME=book
ENV DB_USERNAME=admin
ENV DB_PASSWORD=Devops2022 
WORKDIR /app 
ADD pom.xml . 
RUN ["/usr/local/bin/mvn-entrypoint.sh","mvn","verify","clean","--fail-never"] 
COPY . . 
RUN mvn clean package 
EXPOSE 8080 
ENTRYPOINT ["java","-jar","target/booktracker-0.0.1-SNAPSHOT.jar"]