../mvnw -f ../pom.xml -B clean package -DskipTests=true
java -jar ../target/codenjoy-client-runner.war --context=/client-runner --server.port=8081 --service.solutions.path=./solutions