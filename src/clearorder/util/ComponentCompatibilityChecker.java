package clearorder.util;

import clearorder.model.Product;
import java.util.List;

/**
 * Utility class to check compatibility between PC components
 * based on manufacturer and socket type restrictions.
 */
public class ComponentCompatibilityChecker {    /**
     * Check compatibility between a processor and motherboard using simplified socket types
     * 
     * Rules:
     * - INTEL_COMPATIBLE processors work with: INTEL_COMPATIBLE and UNIVERSAL motherboards
     * - AMD_COMPATIBLE processors work with: AMD_COMPATIBLE and UNIVERSAL motherboards
     * - UNIVERSAL components work with everything
     * 
     * @param processor The processor Product
     * @param motherboard The motherboard Product
     * @return CompatibilityResult indicating if compatible and any warning message
     */
    public static CompatibilityResult checkProcessorMotherboardCompatibility(Product processor, Product motherboard) {
        if (processor == null || motherboard == null) {
            return new CompatibilityResult(true, ""); // Allow if either is null
        }
        
        String processorSocket = processor.getSocketType();
        String motherboardSocket = motherboard.getSocketType();
        
        // If socket type info is missing, fall back to manufacturer-based rules
        if (processorSocket == null || motherboardSocket == null) {
            return checkManufacturerCompatibility(processor, motherboard);
        }
        
        // Check socket compatibility with new simplified system
        if ("UNIVERSAL".equals(processorSocket) || "UNIVERSAL".equals(motherboardSocket)) {
            return new CompatibilityResult(true, ""); // UNIVERSAL works with everything
        }
        
        if ("INTEL_COMPATIBLE".equals(processorSocket)) {
            if ("INTEL_COMPATIBLE".equals(motherboardSocket) || "UNIVERSAL".equals(motherboardSocket)) {
                return new CompatibilityResult(true, "");
            } else {
                return new CompatibilityResult(false,
                    "Compatibility Issue:\n" +
                    "Intel processors are not compatible with AMD-only motherboards.\n" +
                    "Intel processors work with: ASRock, ASUS, MSI motherboards\n\n" +
                    "Current selection:\n" +
                    "- Processor: " + processor.getProductName() + " (" + processor.getManufacturerName() + ")\n" +
                    "- Motherboard: " + motherboard.getProductName() + " (" + motherboard.getManufacturerName() + ")\n\n" +
                    "Please select a compatible motherboard.");
            }
        }
        
        if ("AMD_COMPATIBLE".equals(processorSocket)) {
            if ("AMD_COMPATIBLE".equals(motherboardSocket) || "UNIVERSAL".equals(motherboardSocket)) {
                return new CompatibilityResult(true, "");
            } else {
                return new CompatibilityResult(false,
                    "Compatibility Issue:\n" +
                    "AMD processors are not compatible with Intel-only motherboards.\n" +
                    "AMD processors work with: Gigabyte, ASRock, MSI motherboards\n\n" +
                    "Current selection:\n" +
                    "- Processor: " + processor.getProductName() + " (" + processor.getManufacturerName() + ")\n" +
                    "- Motherboard: " + motherboard.getProductName() + " (" + motherboard.getManufacturerName() + ")\n\n" +
                    "Please select a compatible motherboard.");
            }
        }
        
        return new CompatibilityResult(true, ""); // Default to compatible
    }
    
    /**
     * Fallback method for manufacturer-based compatibility checking
     * when socket type information is not available
     */
    private static CompatibilityResult checkManufacturerCompatibility(Product processor, Product motherboard) {
        String processorManufacturer = processor.getManufacturerName();
        String motherboardManufacturer = motherboard.getManufacturerName();
        
        if (processorManufacturer == null || motherboardManufacturer == null) {
            return new CompatibilityResult(true, ""); // Allow if manufacturer info missing
        }
        
        String cpuManu = processorManufacturer.toLowerCase();
        String mbManu = motherboardManufacturer.toLowerCase();
        
        // Intel processor compatibility rules
        if (cpuManu.contains("intel")) {
            if (mbManu.contains("gigabyte")) {
                return new CompatibilityResult(false, 
                    "Compatibility Issue:\n" +
                    "Intel processors are not compatible with Gigabyte motherboards.\n" +
                    "Intel processors work with: ASRock, ASUS, MSI\n\n" +
                    "Current selection:\n" +
                    "- Processor: " + processor.getProductName() + " (" + processor.getManufacturerName() + ")\n" +
                    "- Motherboard: " + motherboard.getProductName() + " (" + motherboard.getManufacturerName() + ")\n\n" +
                    "Please select a compatible motherboard.");
            }
        }
        
        // AMD processor compatibility rules  
        if (cpuManu.contains("amd")) {
            if (mbManu.contains("asus")) {
                return new CompatibilityResult(false,
                    "Compatibility Issue:\n" +
                    "AMD processors are not compatible with ASUS motherboards.\n" +
                    "AMD processors work with: Gigabyte, ASRock, MSI\n\n" +
                    "Current selection:\n" +
                    "- Processor: " + processor.getProductName() + " (" + processor.getManufacturerName() + ")\n" +
                    "- Motherboard: " + motherboard.getProductName() + " (" + motherboard.getManufacturerName() + ")\n\n" +
                    "Please select a compatible motherboard.");
            }
        }
        
        return new CompatibilityResult(true, "✓ Components are compatible");
    }
    
    /**
     * Check compatibility for all selected components in a PC build
     * @param selectedComponents List of selected Product components
     * @return CompatibilityResult with overall compatibility status
     */
    public static CompatibilityResult checkOverallCompatibility(List<Product> selectedComponents) {
        if (selectedComponents == null || selectedComponents.isEmpty()) {
            return new CompatibilityResult(true, "");
        }
        
        Product processor = null;
        Product motherboard = null;
        
        // Find processor and motherboard in the selection
        for (Product component : selectedComponents) {
            if (component == null) continue;
            
            String type = component.getProductType();
            if (type != null) {
                if (type.toLowerCase().contains("processor") || type.toLowerCase().contains("cpu")) {
                    processor = component;
                } else if (type.toLowerCase().contains("motherboard")) {
                    motherboard = component;
                }
            }
        }
        
        // Check processor-motherboard compatibility
        if (processor != null && motherboard != null) {
            return checkProcessorMotherboardCompatibility(processor, motherboard);
        }
        
        return new CompatibilityResult(true, ""); // No compatibility issues found
    }
    
    /**
     * Result class to hold compatibility check results
     */
    public static class CompatibilityResult {
        public final boolean isCompatible;
        public final String message;
        
        public CompatibilityResult(boolean isCompatible, String message) {
            this.isCompatible = isCompatible;
            this.message = message;
        }
    }
}
