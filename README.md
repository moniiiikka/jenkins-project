# CloudFormation Template Generator

This project demonstrates a Q Developer feature for generating AWS CloudFormation templates programmatically using Java.

## Features

- **Programmatic CloudFormation Template Generation**: Create CloudFormation templates using a fluent Java API
- **Multiple AWS Resource Support**: Generate templates for S3 buckets, EC2 instances, Lambda functions, and more
- **Multiple Output Formats**: Generate templates in both JSON and YAML formats
- **Pre-built Templates**: Includes sample templates for common use cases (web applications, serverless applications)
- **Extensible Design**: Easy to add support for additional AWS resources

## Supported AWS Resources

- **AWS::S3::Bucket**: S3 buckets with optional versioning
- **AWS::EC2::Instance**: EC2 instances with configurable instance types and AMIs
- **AWS::Lambda::Function**: Lambda functions with runtime and handler configuration
- **Parameters**: Template parameters with types, descriptions, and default values
- **Outputs**: Template outputs with values and descriptions

## Usage

### Basic Usage

```java
// Create a new template generator
CFNTemplateGenerator generator = new CFNTemplateGenerator();

// Add resources using the fluent API
generator
    .setDescription("My CloudFormation template")
    .addS3Bucket("MyBucket", "my-unique-bucket-name", true)
    .addEC2Instance("WebServer", "t2.micro", "ami-12345", "MyKeyPair")
    .addOutput("BucketName", "!Ref MyBucket", "Name of the S3 bucket");

// Generate the template
String yamlTemplate = generator.generateYAML();
String jsonTemplate = generator.generateJSON();
```

### Pre-built Templates

```java
// Web application infrastructure
CFNTemplateGenerator webApp = CFNTemplateGenerator.createWebAppTemplate();
String template = webApp.generateYAML();

// Serverless application infrastructure
CFNTemplateGenerator serverless = CFNTemplateGenerator.createServerlessTemplate();
String template = serverless.generateYAML();
```

## Running the Demo

To see the CloudFormation template generator in action:

```bash
# Compile the project
mvn compile

# Run the demo application
mvn exec:java -Dexec.mainClass="com.test.demo.App"
```

The demo will generate and display several example CloudFormation templates:

1. **Basic S3 Bucket Template**: Simple S3 bucket with versioning
2. **Web Application Infrastructure**: EC2 instance with S3 bucket for a web application
3. **Serverless Application**: Lambda function with S3 bucket for serverless architecture
4. **Custom Multi-Resource Template**: Complex template with multiple AWS resources

## Running Tests

Execute the test suite to verify functionality:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CFNTemplateGeneratorTest
mvn test -Dtest=AppTest
```

## Project Structure

```
src/
├── main/java/com/test/demo/
│   ├── App.java                    # Demo application
│   └── CFNTemplateGenerator.java   # Main generator class
└── test/java/com/test/demo/
    ├── AppTest.java                # Tests for demo application
    └── CFNTemplateGeneratorTest.java # Tests for generator class
```

## Dependencies

- **AWS SDK for Java v2**: CloudFormation client and utilities
- **Jackson**: JSON and YAML processing
- **JUnit 4**: Unit testing framework

## Example Output

The generator produces valid CloudFormation templates like this:

```yaml
AWSTemplateFormatVersion: "2010-09-09"
Description: "Basic S3 bucket with versioning"
Parameters: {}
Resources:
  MyBucket:
    Type: "AWS::S3::Bucket"
    Properties:
      BucketName: "my-demo-bucket-12345"
      VersioningConfiguration:
        Status: "Enabled"
Outputs:
  BucketName:
    Value: "!Ref MyBucket"
    Description: "Name of the created S3 bucket"
```

## Extending the Generator

To add support for new AWS resources:

1. Add a new method to `CFNTemplateGenerator` class
2. Follow the existing pattern for resource creation
3. Add corresponding unit tests
4. Update this README with the new resource type

Example:

```java
public CFNTemplateGenerator addDynamoDBTable(String logicalId, String tableName, String hashKey) {
    ObjectNode resource = jsonMapper.createObjectNode();
    resource.put("Type", "AWS::DynamoDB::Table");
    
    ObjectNode properties = jsonMapper.createObjectNode();
    properties.put("TableName", tableName);
    // Add more properties...
    
    resource.set("Properties", properties);
    ((ObjectNode) template.get("Resources")).set(logicalId, resource);
    return this;
}
```

## License

This project is for demonstration purposes and is provided as-is.