package com.test.demo;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for CFNTemplateUtils
 */
public class CFNTemplateUtilsTest {
    
    @Test
    public void testValidateValidTemplate() throws Exception {
        CFNTemplateGenerator generator = new CFNTemplateGenerator()
            .setDescription("Test template")
            .addS3Bucket("TestBucket", "test-bucket", false);
        
        String template = generator.generateJSON();
        CFNTemplateUtils.ValidationResult result = CFNTemplateUtils.validateTemplate(template);
        
        assertTrue("Template should be valid", result.isValid());
        assertTrue("Should have no errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidateInvalidTemplate() {
        String invalidTemplate = "{ \"Description\": \"Invalid template\" }";
        CFNTemplateUtils.ValidationResult result = CFNTemplateUtils.validateTemplate(invalidTemplate);
        
        assertFalse("Template should be invalid", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertTrue("Should report missing AWSTemplateFormatVersion", 
                  result.getErrors().stream().anyMatch(error -> error.contains("AWSTemplateFormatVersion")));
        assertTrue("Should report missing Resources", 
                  result.getErrors().stream().anyMatch(error -> error.contains("Resources")));
    }
    
    @Test
    public void testValidateTemplateWithWarnings() throws Exception {
        CFNTemplateGenerator generator = new CFNTemplateGenerator()
            .setDescription("Empty template");
        
        String template = generator.generateJSON();
        CFNTemplateUtils.ValidationResult result = CFNTemplateUtils.validateTemplate(template);
        
        assertTrue("Template should be valid", result.isValid());
        assertFalse("Should have warnings", result.getWarnings().isEmpty());
        assertTrue("Should warn about no resources", 
                  result.getWarnings().stream().anyMatch(warning -> warning.contains("no resources")));
    }
    
    @Test
    public void testExtractDependencies() throws Exception {
        CFNTemplateGenerator generator = new CFNTemplateGenerator()
            .addS3Bucket("MyBucket", "test-bucket", false)
            .addOutput("BucketName", "!Ref MyBucket", "Bucket name");
        
        String template = generator.generateJSON();
        Map<String, Set<String>> dependencies = CFNTemplateUtils.extractDependencies(template);
        
        assertNotNull("Dependencies should not be null", dependencies);
        assertTrue("Should contain MyBucket", dependencies.containsKey("MyBucket"));
    }
    
    @Test
    public void testGetTemplateStats() throws Exception {
        CFNTemplateGenerator generator = new CFNTemplateGenerator()
            .addParameter("InstanceType", "String", "Instance type", "t2.micro")
            .addParameter("KeyName", "String", "Key name", null)
            .addS3Bucket("Bucket1", "bucket1", false)
            .addS3Bucket("Bucket2", "bucket2", true)
            .addEC2Instance("Instance1", "t2.micro", "ami-12345", "MyKey")
            .addOutput("BucketName", "!Ref Bucket1", "Bucket name")
            .addOutput("InstanceId", "!Ref Instance1", "Instance ID");
        
        String template = generator.generateJSON();
        CFNTemplateUtils.TemplateStats stats = CFNTemplateUtils.getTemplateStats(template);
        
        assertEquals("Should have 3 total resources", 3, stats.getTotalResourceCount());
        assertEquals("Should have 2 parameters", 2, stats.getParameterCount());
        assertEquals("Should have 2 outputs", 2, stats.getOutputCount());
        
        Map<String, Integer> resourceCounts = stats.getResourceTypeCounts();
        assertEquals("Should have 2 S3 buckets", Integer.valueOf(2), resourceCounts.get("AWS::S3::Bucket"));
        assertEquals("Should have 1 EC2 instance", Integer.valueOf(1), resourceCounts.get("AWS::EC2::Instance"));
    }
    
    @Test
    public void testMergeTemplates() throws Exception {
        CFNTemplateGenerator template1 = new CFNTemplateGenerator()
            .setDescription("Template 1")
            .addS3Bucket("Bucket1", "bucket1", false)
            .addParameter("Param1", "String", "Parameter 1", "default1");
        
        CFNTemplateGenerator template2 = new CFNTemplateGenerator()
            .setDescription("Template 2")
            .addEC2Instance("Instance1", "t2.micro", "ami-12345", "MyKey")
            .addParameter("Param2", "String", "Parameter 2", "default2")
            .addOutput("InstanceId", "!Ref Instance1", "Instance ID");
        
        String mergedJson = CFNTemplateUtils.mergeTemplates(
            template1.generateJSON(), 
            template2.generateJSON()
        );
        
        assertNotNull("Merged template should not be null", mergedJson);
        assertTrue("Should contain Bucket1", mergedJson.contains("Bucket1"));
        assertTrue("Should contain Instance1", mergedJson.contains("Instance1"));
        assertTrue("Should contain Param1", mergedJson.contains("Param1"));
        assertTrue("Should contain Param2", mergedJson.contains("Param2"));
        assertTrue("Should contain InstanceId output", mergedJson.contains("InstanceId"));
    }
    
    @Test
    public void testValidationResultToString() {
        CFNTemplateUtils.ValidationResult result = new CFNTemplateUtils.ValidationResult();
        result.addError("Test error");
        result.addWarning("Test warning");
        
        String output = result.toString();
        
        assertNotNull("ToString should not return null", output);
        assertTrue("Should contain validation result header", output.contains("Validation Result"));
        assertTrue("Should contain error", output.contains("Test error"));
        assertTrue("Should contain warning", output.contains("Test warning"));
        assertTrue("Should show valid status", output.contains("Valid: false"));
    }
    
    @Test
    public void testTemplateStatsToString() throws Exception {
        CFNTemplateGenerator generator = new CFNTemplateGenerator()
            .addS3Bucket("Bucket1", "bucket1", false)
            .addS3Bucket("Bucket2", "bucket2", true)
            .addEC2Instance("Instance1", "t2.micro", "ami-12345", "MyKey")
            .addParameter("TestParam", "String", "Test parameter", "default")
            .addOutput("TestOutput", "test-value", "Test output");
        
        String template = generator.generateJSON();
        CFNTemplateUtils.TemplateStats stats = CFNTemplateUtils.getTemplateStats(template);
        
        String output = stats.toString();
        
        assertNotNull("ToString should not return null", output);
        assertTrue("Should contain statistics header", output.contains("Template Statistics"));
        assertTrue("Should contain total resources", output.contains("Total Resources: 3"));
        assertTrue("Should contain parameters count", output.contains("Parameters: 1"));
        assertTrue("Should contain outputs count", output.contains("Outputs: 1"));
        assertTrue("Should contain S3 bucket count", output.contains("AWS::S3::Bucket: 2"));
        assertTrue("Should contain EC2 instance count", output.contains("AWS::EC2::Instance: 1"));
    }
    
    @Test
    public void testValidateInvalidJson() {
        String invalidJson = "{ invalid json }";
        CFNTemplateUtils.ValidationResult result = CFNTemplateUtils.validateTemplate(invalidJson);
        
        assertFalse("Should be invalid", result.isValid());
        assertTrue("Should have JSON format error", 
                  result.getErrors().stream().anyMatch(error -> error.contains("Invalid JSON format")));
    }
    
    @Test
    public void testValidateResourceWithoutType() {
        String templateWithoutType = "{"
            + "\"AWSTemplateFormatVersion\": \"2010-09-09\","
            + "\"Resources\": {"
            + "  \"BadResource\": {"
            + "    \"Properties\": {}"
            + "  }"
            + "}"
            + "}";
        
        CFNTemplateUtils.ValidationResult result = CFNTemplateUtils.validateTemplate(templateWithoutType);
        
        assertFalse("Should be invalid", result.isValid());
        assertTrue("Should report missing Type field", 
                  result.getErrors().stream().anyMatch(error -> error.contains("missing Type field")));
    }
}