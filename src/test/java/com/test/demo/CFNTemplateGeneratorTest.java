package com.test.demo;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for CFNTemplateGenerator
 */
public class CFNTemplateGeneratorTest {
    
    private CFNTemplateGenerator generator;
    private ObjectMapper mapper;
    
    @Before
    public void setUp() {
        generator = new CFNTemplateGenerator();
        mapper = new ObjectMapper();
    }
    
    @Test
    public void testBasicTemplateStructure() throws Exception {
        String json = generator.generateJSON();
        JsonNode template = mapper.readTree(json);
        
        // Verify basic template structure
        assertEquals("2010-09-09", template.get("AWSTemplateFormatVersion").asText());
        assertEquals("Generated CloudFormation template", template.get("Description").asText());
        assertTrue(template.has("Parameters"));
        assertTrue(template.has("Resources"));
        assertTrue(template.has("Outputs"));
    }
    
    @Test
    public void testSetDescription() throws Exception {
        String customDescription = "My custom CloudFormation template";
        generator.setDescription(customDescription);
        
        String json = generator.generateJSON();
        JsonNode template = mapper.readTree(json);
        
        assertEquals(customDescription, template.get("Description").asText());
    }
    
    @Test
    public void testAddParameter() throws Exception {
        generator.addParameter("InstanceType", "String", "EC2 instance type", "t2.micro");
        
        String json = generator.generateJSON();
        JsonNode template = mapper.readTree(json);
        JsonNode parameter = template.get("Parameters").get("InstanceType");
        
        assertNotNull(parameter);
        assertEquals("String", parameter.get("Type").asText());
        assertEquals("EC2 instance type", parameter.get("Description").asText());
        assertEquals("t2.micro", parameter.get("Default").asText());
    }
    
    @Test
    public void testAddParameterWithoutDefault() throws Exception {
        generator.addParameter("KeyName", "String", "EC2 Key Pair name", null);
        
        String json = generator.generateJSON();
        JsonNode template = mapper.readTree(json);
        JsonNode parameter = template.get("Parameters").get("KeyName");
        
        assertNotNull(parameter);
        assertEquals("String", parameter.get("Type").asText());
        assertEquals("EC2 Key Pair name", parameter.get("Description").asText());
        assertNull(parameter.get("Default"));
    }
    
    @Test
    public void testAddS3Bucket() throws Exception {
        generator.addS3Bucket("MyBucket", "my-test-bucket", true);
        
        String json = generator.generateJSON();
        JsonNode template = mapper.readTree(json);
        JsonNode resource = template.get("Resources").get("MyBucket");
        
        assertNotNull(resource);
        assertEquals("AWS::S3::Bucket", resource.get("Type").asText());
        assertEquals("my-test-bucket", resource.get("Properties").get("BucketName").asText());
        assertEquals("Enabled", resource.get("Properties").get("VersioningConfiguration").get("Status").asText());
    }
    
    @Test
    public void testAddS3BucketWithoutVersioning() throws Exception {
        generator.addS3Bucket("SimpleBucket", null, false);
        
        String json = generator.generateJSON();
        JsonNode template = mapper.readTree(json);
        JsonNode resource = template.get("Resources").get("SimpleBucket");
        
        assertNotNull(resource);
        assertEquals("AWS::S3::Bucket", resource.get("Type").asText());
        assertNull(resource.get("Properties").get("BucketName"));
        assertNull(resource.get("Properties").get("VersioningConfiguration"));
    }
    
    @Test
    public void testAddEC2Instance() throws Exception {
        generator.addEC2Instance("WebServer", "t2.micro", "ami-12345", "MyKeyPair");
        
        String json = generator.generateJSON();
        JsonNode template = mapper.readTree(json);
        JsonNode resource = template.get("Resources").get("WebServer");
        
        assertNotNull(resource);
        assertEquals("AWS::EC2::Instance", resource.get("Type").asText());
        assertEquals("t2.micro", resource.get("Properties").get("InstanceType").asText());
        assertEquals("ami-12345", resource.get("Properties").get("ImageId").asText());
        assertEquals("MyKeyPair", resource.get("Properties").get("KeyName").asText());
    }
    
    @Test
    public void testAddLambdaFunction() throws Exception {
        generator.addLambdaFunction("MyFunction", "test-function", "nodejs18.x", "index.handler", "arn:aws:iam::123456789012:role/lambda-role");
        
        String json = generator.generateJSON();
        JsonNode template = mapper.readTree(json);
        JsonNode resource = template.get("Resources").get("MyFunction");
        
        assertNotNull(resource);
        assertEquals("AWS::Lambda::Function", resource.get("Type").asText());
        assertEquals("test-function", resource.get("Properties").get("FunctionName").asText());
        assertEquals("nodejs18.x", resource.get("Properties").get("Runtime").asText());
        assertEquals("index.handler", resource.get("Properties").get("Handler").asText());
        assertEquals("arn:aws:iam::123456789012:role/lambda-role", resource.get("Properties").get("Role").asText());
        assertNotNull(resource.get("Properties").get("Code").get("ZipFile"));
    }
    
    @Test
    public void testAddOutput() throws Exception {
        generator.addOutput("BucketName", "!Ref MyBucket", "Name of the S3 bucket");
        
        String json = generator.generateJSON();
        JsonNode template = mapper.readTree(json);
        JsonNode output = template.get("Outputs").get("BucketName");
        
        assertNotNull(output);
        assertEquals("!Ref MyBucket", output.get("Value").asText());
        assertEquals("Name of the S3 bucket", output.get("Description").asText());
    }
    
    @Test
    public void testGenerateYAML() throws Exception {
        generator.setDescription("YAML test template")
                .addS3Bucket("TestBucket", "yaml-test-bucket", false);
        
        String yaml = generator.generateYAML();
        
        assertNotNull(yaml);
        assertTrue(yaml.contains("AWSTemplateFormatVersion: \"2010-09-09\""));
        assertTrue(yaml.contains("Description: \"YAML test template\""));
        assertTrue(yaml.contains("TestBucket:"));
        assertTrue(yaml.contains("Type: \"AWS::S3::Bucket\""));
    }
    
    @Test
    public void testReset() throws Exception {
        // Add some content
        generator.setDescription("Test template")
                .addS3Bucket("TestBucket", "test-bucket", true)
                .addOutput("TestOutput", "test-value", "test description");
        
        // Reset and verify it's back to default
        generator.reset();
        
        String json = generator.generateJSON();
        JsonNode template = mapper.readTree(json);
        
        assertEquals("Generated CloudFormation template", template.get("Description").asText());
        assertEquals(0, template.get("Resources").size());
        assertEquals(0, template.get("Outputs").size());
        assertEquals(0, template.get("Parameters").size());
    }
    
    @Test
    public void testWebAppTemplate() throws Exception {
        CFNTemplateGenerator webAppTemplate = CFNTemplateGenerator.createWebAppTemplate();
        String json = webAppTemplate.generateJSON();
        JsonNode template = mapper.readTree(json);
        
        // Verify template structure
        assertEquals("Sample web application infrastructure", template.get("Description").asText());
        
        // Verify parameters
        assertTrue(template.get("Parameters").has("InstanceType"));
        assertTrue(template.get("Parameters").has("KeyName"));
        
        // Verify resources
        assertTrue(template.get("Resources").has("WebAppBucket"));
        assertTrue(template.get("Resources").has("WebServer"));
        
        // Verify outputs
        assertTrue(template.get("Outputs").has("BucketName"));
        assertTrue(template.get("Outputs").has("InstanceId"));
    }
    
    @Test
    public void testServerlessTemplate() throws Exception {
        CFNTemplateGenerator serverlessTemplate = CFNTemplateGenerator.createServerlessTemplate();
        String json = serverlessTemplate.generateJSON();
        JsonNode template = mapper.readTree(json);
        
        // Verify template structure
        assertEquals("Sample serverless application infrastructure", template.get("Description").asText());
        
        // Verify parameters
        assertTrue(template.get("Parameters").has("FunctionName"));
        
        // Verify resources
        assertTrue(template.get("Resources").has("ServerlessBucket"));
        assertTrue(template.get("Resources").has("ServerlessFunction"));
        
        // Verify outputs
        assertTrue(template.get("Outputs").has("FunctionArn"));
        assertTrue(template.get("Outputs").has("BucketName"));
    }
    
    @Test
    public void testFluentInterface() throws Exception {
        // Test that all methods return the generator instance for chaining
        CFNTemplateGenerator result = generator
            .setDescription("Fluent test")
            .addParameter("TestParam", "String", "Test parameter", "default")
            .addS3Bucket("FluentBucket", "fluent-bucket", true)
            .addEC2Instance("FluentInstance", "t2.micro", "ami-12345", "TestKey")
            .addLambdaFunction("FluentFunction", "fluent-func", "nodejs18.x", "index.handler", "role-arn")
            .addOutput("FluentOutput", "test-value", "Test output");
        
        assertSame(generator, result);
        
        // Verify all components were added
        String json = generator.generateJSON();
        JsonNode template = mapper.readTree(json);
        
        assertEquals("Fluent test", template.get("Description").asText());
        assertTrue(template.get("Parameters").has("TestParam"));
        assertTrue(template.get("Resources").has("FluentBucket"));
        assertTrue(template.get("Resources").has("FluentInstance"));
        assertTrue(template.get("Resources").has("FluentFunction"));
        assertTrue(template.get("Outputs").has("FluentOutput"));
    }
}