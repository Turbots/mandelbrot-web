mvn clean package
mkdir -p target/dependency
cd target/dependency
tar -zxf ../*.jar
cd ../..
mvn install