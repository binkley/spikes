# Amazon Java SDK logging demo

A demonstration of enabling user-controlled logging with Amazon Java SDK.  The
demonstration focuses on SQS, but the technique is generally useful with:

* Cloud Formation
* EC2
* KMS
* S3
* SNS
* SQS

Essentially, any portion of the Amazon Java SDK using [`AmazonHttpClient`]
for HTTP communications.

## Running the demonstration

1. Ensure Docker is running locally.
2. Use `docker-compose up` in the project root to start
   [Localstack](https://github.com/localstack/localstack).
3. Wait for Localstack to announce "Ready."

## Running from an IDE

{:start="4"}
4. Open and run
   [`DemoApplication`](src/main/java/com/example/demo/DemoApplication.java).

## Running from the command line

{:start="4"}
4. Run `./gradlew build`.
5. Run `java -jar build/libs/demo-0.0.1-SNAPSHOT.jar`.

