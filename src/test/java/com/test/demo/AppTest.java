package com.test.demo;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit test for App class - CloudFormation Template Generator Demo
 */
public class AppTest 
{
    /**
     * Test that the main method runs without exceptions
     */
    @Test
    public void testMainMethodExecution()
    {
        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        try {
            // Run the main method
            App.main(new String[]{});
            
            // Verify output contains expected content
            String output = outContent.toString();
            assertTrue("Output should contain demo header", 
                      output.contains("CloudFormation Template Generator Demo"));
            assertTrue("Output should contain basic S3 template section", 
                      output.contains("Generating basic S3 bucket template"));
            assertTrue("Output should contain web app template section", 
                      output.contains("Generating web application infrastructure template"));
            assertTrue("Output should contain serverless template section", 
                      output.contains("Generating serverless application template"));
            assertTrue("Output should contain custom template section", 
                      output.contains("Generating custom multi-resource template"));
            assertTrue("Output should contain validation section", 
                      output.contains("Template validation and analysis"));
            assertTrue("Output should contain merging section", 
                      output.contains("Template merging demonstration"));
            assertTrue("Output should contain completion message", 
                      output.contains("Demo completed successfully"));
            
        } catch (Exception e) {
            fail("Main method should not throw exceptions: " + e.getMessage());
        } finally {
            // Restore original system output
            System.setOut(originalOut);
        }
    }
    
    /**
     * Test that the output contains valid CloudFormation template structure
     */
    @Test
    public void testOutputContainsValidTemplateStructure()
    {
        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        try {
            // Run the main method
            App.main(new String[]{});
            
            String output = outContent.toString();
            
            // Verify CloudFormation template elements are present
            assertTrue("Output should contain AWSTemplateFormatVersion", 
                      output.contains("AWSTemplateFormatVersion"));
            assertTrue("Output should contain Description field", 
                      output.contains("Description:"));
            assertTrue("Output should contain Resources section", 
                      output.contains("Resources:"));
            assertTrue("Output should contain AWS resource types", 
                      output.contains("AWS::S3::Bucket") || 
                      output.contains("AWS::EC2::Instance") || 
                      output.contains("AWS::Lambda::Function"));
            
        } finally {
            // Restore original system output
            System.setOut(originalOut);
        }
    }
    
    /**
     * Test that validation and analysis features are working
     */
    @Test
    public void testValidationAndAnalysisOutput()
    {
        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        try {
            // Run the main method
            App.main(new String[]{});
            
            String output = outContent.toString();
            
            // Verify validation and analysis output
            assertTrue("Output should contain validation results", 
                      output.contains("Template Validation:"));
            assertTrue("Output should contain template statistics", 
                      output.contains("Template Statistics:"));
            assertTrue("Output should contain resource counts", 
                      output.contains("Total Resources:"));
            assertTrue("Output should contain merged template", 
                      output.contains("Merged template"));
            
        } finally {
            // Restore original system output
            System.setOut(originalOut);
        }
    }
    
    /**
     * Test that template merging functionality is demonstrated
     */
    @Test
    public void testTemplateMergingDemo()
    {
        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        try {
            // Run the main method
            App.main(new String[]{});
            
            String output = outContent.toString();
            
            // Verify merging functionality is demonstrated
            assertTrue("Output should show template merging", 
                      output.contains("Template merging demonstration"));
            assertTrue("Output should contain merged template validation", 
                      output.contains("Merged Template Validation"));
            assertTrue("Output should contain storage and compute resources", 
                      output.contains("StorageBucket") && output.contains("ComputeInstance"));
            
        } finally {
            // Restore original system output
            System.setOut(originalOut);
        }
    }
    
    /**
     * Test that no errors are printed to stderr
     */
    @Test
    public void testNoErrorsInExecution()
    {
        // Capture system error output
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));
        
        try {
            // Run the main method
            App.main(new String[]{});
            
            // Verify no errors were printed
            String errorOutput = errContent.toString();
            assertTrue("No errors should be printed to stderr", 
                      errorOutput.isEmpty());
            
        } finally {
            // Restore original system error output
            System.setErr(originalErr);
        }
    }
    
    /**
     * Test that all demo sections are present in correct order
     */
    @Test
    public void testDemoSectionsOrder()
    {
        // Capture system output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        
        try {
            // Run the main method
            App.main(new String[]{});
            
            String output = outContent.toString();
            
            // Find positions of each section
            int basicPos = output.indexOf("1. Generating basic S3 bucket template");
            int webAppPos = output.indexOf("2. Generating web application infrastructure template");
            int serverlessPos = output.indexOf("3. Generating serverless application template");
            int customPos = output.indexOf("4. Generating custom multi-resource template");
            int validationPos = output.indexOf("5. Template validation and analysis");
            int mergingPos = output.indexOf("6. Template merging demonstration");
            
            // Verify all sections are present and in correct order
            assertTrue("Basic template section should be present", basicPos > 0);
            assertTrue("Web app section should be present", webAppPos > 0);
            assertTrue("Serverless section should be present", serverlessPos > 0);
            assertTrue("Custom template section should be present", customPos > 0);
            assertTrue("Validation section should be present", validationPos > 0);
            assertTrue("Merging section should be present", mergingPos > 0);
            
            // Verify order
            assertTrue("Sections should be in correct order", 
                      basicPos < webAppPos && 
                      webAppPos < serverlessPos && 
                      serverlessPos < customPos && 
                      customPos < validationPos && 
                      validationPos < mergingPos);
            
        } finally {
            // Restore original system output
            System.setOut(originalOut);
        }
    }
    
    /**
     * Rigorous Test :-)
     * Keep the original test for backward compatibility
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }
}
