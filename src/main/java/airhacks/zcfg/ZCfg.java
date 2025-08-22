package airhacks.zcfg;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuration loader that reads properties in order:
 * 1. ~/.[appName]/app.properties (global)
 * 2. ./app.properties (local, overwrites global)
 * 3. System properties (highest priority)
 */
public interface ZCfg {
    
    Properties INSTANCE = load(appName());
    
    static String appName() {
        return System.getProperty("app.name", "app");
    }
    
    static Properties load(String appName) {
        var properties = new Properties();
        
        // Load global properties from ~/.[appName]/app.properties
        var userHome = System.getProperty("user.home");
        var globalConfig = Path.of(userHome, "." + appName, "app.properties");
        if (Files.exists(globalConfig)) {
            loadFromFile(globalConfig, properties);
        }
        
        // Load local properties from ./app.properties (overwrites global)
        var localConfig = Path.of("app.properties");
        if (Files.exists(localConfig)) {
            loadFromFile(localConfig, properties);
        }
        
        // System properties have highest priority
        properties.putAll(System.getProperties());
        
        return properties;
    }
    
    static void loadFromFile(Path file, Properties properties) {
        try (InputStream is = Files.newInputStream(file)) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load properties from: " + file, e);
        }
    }
    
    static String string(String key) {
        return INSTANCE.getProperty(key);
    }
    
    static String string(String key, String defaultValue) {
        return INSTANCE.getProperty(key, defaultValue);
    }
    
    static int integer(String key, int defaultValue) {
        var value = INSTANCE.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
    
    static boolean bool(String key, boolean defaultValue) {
        var value = INSTANCE.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
}