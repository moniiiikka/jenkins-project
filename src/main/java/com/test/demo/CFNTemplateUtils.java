package com.test.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * Utility class for CloudFormation template operations
 * 
 * Provides additional functionality for template validation,
 * resource dependency analysis, and template manipulation.
 */
public class CFNTemplateUtils {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * Validate basic CloudFormation template structure
     */
    public static ValidationResult validateTemplate(String templateJson) {
        ValidationResult result = new ValidationResult();
        
        try {
            JsonNode template = mapper.readTree(templateJson);
            
            // Check required fields
            if (!template.has("AWSTemplateFormatVersion")) {
                result.addError("Missing required field: AWSTemplateFormatVersion");
            }
            
            if (!template.has("Resources")) {
                result.addError("Missing required field: Resources");
            } else if (template.get("Resources").size() == 0) {
                result.addWarning("Template has no resources defined");
            }
            
            // Validate resource types
            JsonNode resources = template.get("Resources");
            if (resources != null) {
                resources.fields().forEachRemaining(entry -> {
                    JsonNode resource = entry.getValue();
                    if (!resource.has("Type")) {
                        result.addError("Resource '" + entry.getKey() + "' missing Type field");
                    } else {
                        String type = resource.get("Type").asText();
                        if (!isValidResourceType(type)) {
                            result.addWarning("Unknown resource type: " + type);
                        }
                    }
                });
            }
            
            // Check for circular dependencies (basic check)
            Set<String> circularDeps = findCircularDependencies(template);
            if (!circularDeps.isEmpty()) {
                result.addError("Circular dependencies detected: " + circularDeps);
            }
            
        } catch (Exception e) {
            result.addError("Invalid JSON format: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Extract resource dependencies from template
     */
    public static Map<String, Set<String>> extractDependencies(String templateJson) {
        Map<String, Set<String>> dependencies = new HashMap<>();
        
        try {
            JsonNode template = mapper.readTree(templateJson);
            JsonNode resources = template.get("Resources");
            
            if (resources != null) {
                resources.fields().forEachRemaining(entry -> {
                    String resourceName = entry.getKey();
                    JsonNode resource = entry.getValue();
                    Set<String> deps = new HashSet<>();
                    
                    // Look for !Ref and !GetAtt references
                    findReferences(resource, deps);
                    
                    dependencies.put(resourceName, deps);
                });
            }
            
        } catch (Exception e) {
            System.err.println("Error extracting dependencies: " + e.getMessage());
        }
        
        return dependencies;
    }
    
    /**
     * Get template statistics
     */
    public static TemplateStats getTemplateStats(String templateJson) {
        TemplateStats stats = new TemplateStats();
        
        try {
            JsonNode template = mapper.readTree(templateJson);
            
            // Count resources by type
            JsonNode resources = template.get("Resources");
            if (resources != null) {
                resources.fields().forEachRemaining(entry -> {
                    JsonNode resource = entry.getValue();
                    if (resource.has("Type")) {
                        String type = resource.get("Type").asText();
                        stats.incrementResourceType(type);
                    }
                });
            }
            
            // Count parameters
            JsonNode parameters = template.get("Parameters");
            if (parameters != null) {
                stats.setParameterCount(parameters.size());
            }
            
            // Count outputs
            JsonNode outputs = template.get("Outputs");
            if (outputs != null) {
                stats.setOutputCount(outputs.size());
            }
            
        } catch (Exception e) {
            System.err.println("Error calculating stats: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Merge two CloudFormation templates
     */
    public static String mergeTemplates(String template1Json, String template2Json) throws Exception {
        JsonNode t1 = mapper.readTree(template1Json);
        JsonNode t2 = mapper.readTree(template2Json);
        
        ObjectNode merged = mapper.createObjectNode();
        
        // Use template1 as base
        merged.setAll((ObjectNode) t1);
        
        // Merge resources
        ObjectNode mergedResources = (ObjectNode) merged.get("Resources");
        JsonNode t2Resources = t2.get("Resources");
        if (t2Resources != null) {
            mergedResources.setAll((ObjectNode) t2Resources);
        }
        
        // Merge parameters
        ObjectNode mergedParams = (ObjectNode) merged.get("Parameters");
        JsonNode t2Params = t2.get("Parameters");
        if (t2Params != null) {
            mergedParams.setAll((ObjectNode) t2Params);
        }
        
        // Merge outputs
        ObjectNode mergedOutputs = (ObjectNode) merged.get("Outputs");
        JsonNode t2Outputs = t2.get("Outputs");
        if (t2Outputs != null) {
            mergedOutputs.setAll((ObjectNode) t2Outputs);
        }
        
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(merged);
    }
    
    // Helper methods
    
    private static boolean isValidResourceType(String type) {
        // Basic validation - in real implementation, this would check against AWS documentation
        return type.startsWith("AWS::") && type.contains("::");
    }
    
    private static Set<String> findCircularDependencies(JsonNode template) {
        // Simplified circular dependency detection
        // In a real implementation, this would use graph algorithms
        return new HashSet<>();
    }
    
    private static void findReferences(JsonNode node, Set<String> references) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                
                if ("!Ref".equals(key) && value.isTextual()) {
                    references.add(value.asText());
                } else if ("!GetAtt".equals(key) && value.isArray() && value.size() > 0) {
                    references.add(value.get(0).asText());
                } else {
                    findReferences(value, references);
                }
            });
        } else if (node.isArray()) {
            node.forEach(item -> findReferences(item, references));
        }
    }
    
    // Inner classes for results
    
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Validation Result:\n");
            sb.append("Valid: ").append(isValid()).append("\n");
            
            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                errors.forEach(error -> sb.append("  - ").append(error).append("\n"));
            }
            
            if (!warnings.isEmpty()) {
                sb.append("Warnings:\n");
                warnings.forEach(warning -> sb.append("  - ").append(warning).append("\n"));
            }
            
            return sb.toString();
        }
    }
    
    public static class TemplateStats {
        private final Map<String, Integer> resourceTypeCounts = new HashMap<>();
        private int parameterCount = 0;
        private int outputCount = 0;
        
        public void incrementResourceType(String type) {
            resourceTypeCounts.put(type, resourceTypeCounts.getOrDefault(type, 0) + 1);
        }
        
        public void setParameterCount(int count) {
            this.parameterCount = count;
        }
        
        public void setOutputCount(int count) {
            this.outputCount = count;
        }
        
        public Map<String, Integer> getResourceTypeCounts() {
            return new HashMap<>(resourceTypeCounts);
        }
        
        public int getTotalResourceCount() {
            return resourceTypeCounts.values().stream().mapToInt(Integer::intValue).sum();
        }
        
        public int getParameterCount() {
            return parameterCount;
        }
        
        public int getOutputCount() {
            return outputCount;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Template Statistics:\n");
            sb.append("Total Resources: ").append(getTotalResourceCount()).append("\n");
            sb.append("Parameters: ").append(parameterCount).append("\n");
            sb.append("Outputs: ").append(outputCount).append("\n");
            sb.append("Resource Types:\n");
            
            resourceTypeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> sb.append("  ").append(entry.getKey())
                    .append(": ").append(entry.getValue()).append("\n"));
            
            return sb.toString();
        }
    }
}