package com.test.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * CloudFormation Template Generator
 * 
 * This class provides functionality to generate CloudFormation templates
 * for various AWS resources programmatically.
 */
public class CFNTemplateGenerator {
    
    private final ObjectMapper jsonMapper;
    private final YAMLMapper yamlMapper;
    private ObjectNode template;
    
    public CFNTemplateGenerator() {
        this.jsonMapper = new ObjectMapper();
        this.yamlMapper = new YAMLMapper();
        initializeTemplate();
    }
    
    /**
     * Initialize a basic CloudFormation template structure
     */
    private void initializeTemplate() {
        template = jsonMapper.createObjectNode();
        template.put("AWSTemplateFormatVersion", "2010-09-09");
        template.put("Description", "Generated CloudFormation template");
        template.set("Parameters", jsonMapper.createObjectNode());
        template.set("Resources", jsonMapper.createObjectNode());
        template.set("Outputs", jsonMapper.createObjectNode());
    }
    
    /**
     * Set template description
     */
    public CFNTemplateGenerator setDescription(String description) {
        template.put("Description", description);
        return this;
    }
    
    /**
     * Add a parameter to the template
     */
    public CFNTemplateGenerator addParameter(String name, String type, String description, String defaultValue) {
        ObjectNode parameter = jsonMapper.createObjectNode();
        parameter.put("Type", type);
        parameter.put("Description", description);
        if (defaultValue != null) {
            parameter.put("Default", defaultValue);
        }
        
        ((ObjectNode) template.get("Parameters")).set(name, parameter);
        return this;
    }
    
    /**
     * Add an S3 bucket resource
     */
    public CFNTemplateGenerator addS3Bucket(String logicalId, String bucketName, boolean enableVersioning) {
        ObjectNode resource = jsonMapper.createObjectNode();
        resource.put("Type", "AWS::S3::Bucket");
        
        ObjectNode properties = jsonMapper.createObjectNode();
        if (bucketName != null) {
            properties.put("BucketName", bucketName);
        }
        
        if (enableVersioning) {
            ObjectNode versioningConfig = jsonMapper.createObjectNode();
            versioningConfig.put("Status", "Enabled");
            properties.set("VersioningConfiguration", versioningConfig);
        }
        
        resource.set("Properties", properties);
        ((ObjectNode) template.get("Resources")).set(logicalId, resource);
        return this;
    }
    
    /**
     * Add an EC2 instance resource
     */
    public CFNTemplateGenerator addEC2Instance(String logicalId, String instanceType, String amiId, String keyName) {
        ObjectNode resource = jsonMapper.createObjectNode();
        resource.put("Type", "AWS::EC2::Instance");
        
        ObjectNode properties = jsonMapper.createObjectNode();
        properties.put("InstanceType", instanceType);
        properties.put("ImageId", amiId);
        if (keyName != null) {
            properties.put("KeyName", keyName);
        }
        
        resource.set("Properties", properties);
        ((ObjectNode) template.get("Resources")).set(logicalId, resource);
        return this;
    }
    
    /**
     * Add a Lambda function resource
     */
    public CFNTemplateGenerator addLambdaFunction(String logicalId, String functionName, String runtime, String handler, String roleArn) {
        ObjectNode resource = jsonMapper.createObjectNode();
        resource.put("Type", "AWS::Lambda::Function");
        
        ObjectNode properties = jsonMapper.createObjectNode();
        properties.put("FunctionName", functionName);
        properties.put("Runtime", runtime);
        properties.put("Handler", handler);
        properties.put("Role", roleArn);
        
        // Basic code structure
        ObjectNode code = jsonMapper.createObjectNode();
        code.put("ZipFile", "exports.handler = async (event) => { return 'Hello from Lambda!'; };");
        properties.set("Code", code);
        
        resource.set("Properties", properties);
        ((ObjectNode) template.get("Resources")).set(logicalId, resource);
        return this;
    }
    
    /**
     * Add an output to the template
     */
    public CFNTemplateGenerator addOutput(String name, String value, String description) {
        ObjectNode output = jsonMapper.createObjectNode();
        output.put("Value", value);
        output.put("Description", description);
        
        ((ObjectNode) template.get("Outputs")).set(name, output);
        return this;
    }
    
    /**
     * Generate the template as JSON string
     */
    public String generateJSON() throws Exception {
        return jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(template);
    }
    
    /**
     * Generate the template as YAML string
     */
    public String generateYAML() throws Exception {
        return yamlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(template);
    }
    
    /**
     * Reset the template to start fresh
     */
    public CFNTemplateGenerator reset() {
        initializeTemplate();
        return this;
    }
    
    /**
     * Get a sample web application template
     */
    public static CFNTemplateGenerator createWebAppTemplate() {
        return new CFNTemplateGenerator()
            .setDescription("Sample web application infrastructure")
            .addParameter("InstanceType", "String", "EC2 instance type", "t2.micro")
            .addParameter("KeyName", "String", "EC2 Key Pair name", null)
            .addS3Bucket("WebAppBucket", null, true)
            .addEC2Instance("WebServer", "t2.micro", "ami-0abcdef1234567890", "MyKeyPair")
            .addOutput("BucketName", "!Ref WebAppBucket", "Name of the S3 bucket")
            .addOutput("InstanceId", "!Ref WebServer", "Instance ID of the web server");
    }
    
    /**
     * Get a sample serverless application template
     */
    public static CFNTemplateGenerator createServerlessTemplate() {
        return new CFNTemplateGenerator()
            .setDescription("Sample serverless application infrastructure")
            .addParameter("FunctionName", "String", "Lambda function name", "MyServerlessFunction")
            .addS3Bucket("ServerlessBucket", null, false)
            .addLambdaFunction("ServerlessFunction", "MyServerlessFunction", "nodejs18.x", "index.handler", "arn:aws:iam::123456789012:role/lambda-role")
            .addOutput("FunctionArn", "!GetAtt ServerlessFunction.Arn", "ARN of the Lambda function")
            .addOutput("BucketName", "!Ref ServerlessBucket", "Name of the S3 bucket");
    }
}