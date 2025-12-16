package com.test.demo;

/**
 * CloudFormation Template Generator Demo Application
 * 
 * This application demonstrates the Q Developer feature for generating
 * CloudFormation templates programmatically.
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println("=== CloudFormation Template Generator Demo ===\n");
        
        try {
            // Demo 1: Basic S3 bucket template
            System.out.println("1. Generating basic S3 bucket template:");
            System.out.println("=====================================");
            CFNTemplateGenerator basicTemplate = new CFNTemplateGenerator()
                .setDescription("Basic S3 bucket with versioning")
                .addS3Bucket("MyBucket", "my-demo-bucket-12345", true)
                .addOutput("BucketName", "!Ref MyBucket", "Name of the created S3 bucket");
            
            String basicYaml = basicTemplate.generateYAML();
            System.out.println(basicYaml);
            System.out.println();
            
            // Demo 2: Web application infrastructure
            System.out.println("2. Generating web application infrastructure template:");
            System.out.println("===================================================");
            CFNTemplateGenerator webAppTemplate = CFNTemplateGenerator.createWebAppTemplate();
            String webAppYaml = webAppTemplate.generateYAML();
            System.out.println(webAppYaml);
            System.out.println();
            
            // Demo 3: Serverless application infrastructure
            System.out.println("3. Generating serverless application template:");
            System.out.println("=============================================");
            CFNTemplateGenerator serverlessTemplate = CFNTemplateGenerator.createServerlessTemplate();
            String serverlessYaml = serverlessTemplate.generateYAML();
            System.out.println(serverlessYaml);
            System.out.println();
            
            // Demo 4: Custom template with multiple resources
            System.out.println("4. Generating custom multi-resource template:");
            System.out.println("============================================");
            CFNTemplateGenerator customTemplate = new CFNTemplateGenerator()
                .setDescription("Custom infrastructure with multiple AWS resources")
                .addParameter("Environment", "String", "Environment name", "dev")
                .addParameter("InstanceType", "String", "EC2 instance type", "t2.micro")
                .addS3Bucket("DataBucket", null, true)
                .addEC2Instance("AppServer", "t2.micro", "ami-0abcdef1234567890", "MyKeyPair")
                .addLambdaFunction("ProcessorFunction", "data-processor", "python3.9", "lambda_function.lambda_handler", "arn:aws:iam::123456789012:role/lambda-execution-role")
                .addOutput("DataBucketName", "!Ref DataBucket", "S3 bucket for data storage")
                .addOutput("AppServerInstanceId", "!Ref AppServer", "EC2 instance ID")
                .addOutput("ProcessorFunctionArn", "!GetAtt ProcessorFunction.Arn", "Lambda function ARN");
            
            String customYaml = customTemplate.generateYAML();
            System.out.println(customYaml);
            System.out.println();
            
            // Demo 5: Template validation and analysis
            System.out.println("5. Template validation and analysis:");
            System.out.println("===================================");
            
            // Validate the custom template
            String customJson = customTemplate.generateJSON();
            CFNTemplateUtils.ValidationResult validation = CFNTemplateUtils.validateTemplate(customJson);
            System.out.println("Template Validation:");
            System.out.println(validation);
            
            // Get template statistics
            CFNTemplateUtils.TemplateStats stats = CFNTemplateUtils.getTemplateStats(customJson);
            System.out.println(stats);
            
            // Demo 6: Template merging
            System.out.println("6. Template merging demonstration:");
            System.out.println("=================================");
            
            CFNTemplateGenerator template1 = new CFNTemplateGenerator()
                .setDescription("Storage resources")
                .addS3Bucket("StorageBucket", "storage-bucket-demo", true);
            
            CFNTemplateGenerator template2 = new CFNTemplateGenerator()
                .setDescription("Compute resources")
                .addEC2Instance("ComputeInstance", "t2.small", "ami-0abcdef1234567890", "MyKeyPair")
                .addOutput("InstanceId", "!Ref ComputeInstance", "Compute instance ID");
            
            String mergedTemplate = CFNTemplateUtils.mergeTemplates(
                template1.generateJSON(), 
                template2.generateJSON()
            );
            
            System.out.println("Merged template (JSON):");
            System.out.println(mergedTemplate);
            System.out.println();
            
            // Validate merged template
            CFNTemplateUtils.ValidationResult mergedValidation = CFNTemplateUtils.validateTemplate(mergedTemplate);
            System.out.println("Merged Template Validation:");
            System.out.println(mergedValidation);
            
            System.out.println("=== Demo completed successfully! ===");
            
        } catch (Exception e) {
            System.err.println("Error generating CloudFormation templates: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
