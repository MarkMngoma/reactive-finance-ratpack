package za.co.ratpack.finance.reactive.functions.rest.integration.openapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry for discovering and matching DTO/model classes from the classpath.
 * Scans configured packages at initialization and builds a field-signature registry
 * for matching JSON payloads to their corresponding Java classes.
 *
 * @author markmngoma
 */
public class ClasspathSchemaRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ClasspathSchemaRegistry.class);
    
    private final List<String> basePackages;
    private Map<String, Class<?>> classNameRegistry; // simple name → Class
    private Map<Set<String>, Class<?>> fieldSignatureRegistry; // field names → Class
    private Map<Class<?>, Set<String>> classFieldsCache; // Class → field names
    private boolean initialized = false;
    
    /**
     * Creates a registry with default packages for this project.
     */
    public ClasspathSchemaRegistry() {
        this(getDefaultPackages());
    }
    
    /**
     * Creates a registry with specified base packages to scan.
     * 
     * @param basePackages list of package names to scan for DTOs/models
     */
    public ClasspathSchemaRegistry(List<String> basePackages) {
        this.basePackages = new ArrayList<>(basePackages);
        this.classNameRegistry = new HashMap<>();
        this.fieldSignatureRegistry = new HashMap<>();
        this.classFieldsCache = new HashMap<>();
    }
    
    /**
     * Gets default packages from system property or uses hardcoded defaults.
     */
    private static List<String> getDefaultPackages() {
        String sysProp = System.getProperty("openapi.spec.scan.packages");
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return Arrays.asList(sysProp.split(","))
                    .stream()
                    .map(String::trim)
                    .collect(Collectors.toList());
        }
        
        // Default packages for this project
        return Arrays.asList(
            "za.co.ratpack.finance.reactive.rest.v1.dto",
            "za.co.ratpack.finance.reactive.domain.mybatis.model"
        );
    }
    
    /**
     * Lazily initializes the registry by scanning packages.
     */
    private synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        logger.info("Initializing ClasspathSchemaRegistry with packages: {}", basePackages);
        
        for (String basePackage : basePackages) {
            scanPackage(basePackage);
        }
        
        logger.info("ClasspathSchemaRegistry initialized. Found {} classes", classNameRegistry.size());
        initialized = true;
    }
    
    /**
     * Scans a package for DTO/model classes and registers them.
     */
    private void scanPackage(String packageName) {
        try {
            String path = packageName.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(path);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File directory = new File(resource.getFile());
                
                if (directory.exists() && directory.isDirectory()) {
                    scanDirectory(directory, packageName);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to scan package: {}", packageName, e);
        }
    }
    
    /**
     * Recursively scans a directory for class files.
     */
    private void scanDirectory(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                registerClass(className);
            }
        }
    }
    
    /**
     * Loads and registers a class if it's a valid DTO/model class.
     */
    private void registerClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            
            // Filter: only register concrete classes with fields (not interfaces, enums, abstract)
            if (clazz.isInterface() || clazz.isEnum() || Modifier.isAbstract(clazz.getModifiers())) {
                return;
            }
            
            // Check if class has at least one field (including inherited)
            Set<String> fieldNames = extractFieldNames(clazz);
            if (fieldNames.isEmpty()) {
                return;
            }
            
            // Register by simple name (with collision detection)
            String simpleName = clazz.getSimpleName();
            if (classNameRegistry.containsKey(simpleName)) {
                logger.warn("Simple name collision detected for '{}': {} and {}. Using field signature matching instead.",
                           simpleName, classNameRegistry.get(simpleName).getName(), clazz.getName());
            }
            classNameRegistry.put(simpleName, clazz);
            
            // Register by field signature (primary matching mechanism)
            fieldSignatureRegistry.put(fieldNames, clazz);
            classFieldsCache.put(clazz, fieldNames);
            
            logger.debug("Registered class: {} with fields: {}", clazz.getSimpleName(), fieldNames);
            
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            logger.debug("Could not load class: {}", className);
        }
    }
    
    /**
     * Extracts all field names from a class including inherited fields.
     */
    private Set<String> extractFieldNames(Class<?> clazz) {
        Set<String> fieldNames = new HashSet<>();
        Class<?> current = clazz;
        
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                // Skip static and synthetic fields
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                fieldNames.add(field.getName());
            }
            current = current.getSuperclass();
        }
        
        return fieldNames;
    }
    
    /**
     * Finds the best matching class for a given set of JSON field names.
     * Uses a scoring approach: exact match > highest subset overlap.
     * 
     * @param jsonFieldNames the field names extracted from JSON
     * @return Optional containing the best matching class, or empty if no good match found
     */
    public Optional<Class<?>> findMatchingClass(Set<String> jsonFieldNames) {
        if (!initialized) {
            initialize();
        }
        
        if (jsonFieldNames == null || jsonFieldNames.isEmpty()) {
            return Optional.empty();
        }
        
        // Try exact match first
        Class<?> exactMatch = fieldSignatureRegistry.get(jsonFieldNames);
        if (exactMatch != null) {
            logger.debug("Found exact match for fields {}: {}", jsonFieldNames, exactMatch.getSimpleName());
            return Optional.of(exactMatch);
        }
        
        // Find best subset match
        // Scoring favors classes with fewer extra fields (higher specificity)
        // E.g., JSON {a,b} matches ClassA{a,b,c} with score 0.67 better than ClassB{a,b,c,d} with score 0.5
        Class<?> bestMatch = null;
        double bestScore = 0.0;
        
        for (Map.Entry<Set<String>, Class<?>> entry : fieldSignatureRegistry.entrySet()) {
            Set<String> classFields = entry.getKey();
            
            // Check if JSON fields are a subset of class fields
            if (classFields.containsAll(jsonFieldNames)) {
                // Calculate overlap percentage (higher = more specific match)
                double score = (double) jsonFieldNames.size() / classFields.size();
                
                if (score > bestScore) {
                    bestScore = score;
                    bestMatch = entry.getValue();
                }
            }
        }
        
        if (bestMatch != null) {
            logger.debug("Found subset match for fields {} with score {}: {}", 
                        jsonFieldNames, bestScore, bestMatch.getSimpleName());
            return Optional.of(bestMatch);
        }
        
        logger.debug("No matching class found for fields: {}", jsonFieldNames);
        return Optional.empty();
    }
    
    /**
     * Gets the field names for a registered class.
     */
    public Set<String> getFieldNames(Class<?> clazz) {
        if (!initialized) {
            initialize();
        }
        return classFieldsCache.getOrDefault(clazz, extractFieldNames(clazz));
    }
    
    /**
     * Gets a class by its simple name.
     */
    public Optional<Class<?>> getClassByName(String simpleName) {
        if (!initialized) {
            initialize();
        }
        return Optional.ofNullable(classNameRegistry.get(simpleName));
    }
    
    /**
     * Returns all registered classes.
     */
    public Collection<Class<?>> getAllClasses() {
        if (!initialized) {
            initialize();
        }
        return new ArrayList<>(classNameRegistry.values());
    }
}
