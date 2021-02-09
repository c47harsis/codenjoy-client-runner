mvn -f ../pom.xml -B clean package -DskipTests=true
java -jar ../target/codenjoy-client-runner.war