package airhacks.zcfg;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ZCfgTest {
    
    @Test
    void loadOverwritesGlobalWithLocal(@TempDir Path tempDir) throws IOException {
        var globalProps = """
            server.port=8080
            app.name=global
            db.url=localhost:5432
            """;
        
        var localProps = """
            server.port=9090
            app.name=local
            """;
        
        // Create global config
        var globalDir = tempDir.resolve(".testapp");
        Files.createDirectories(globalDir);
        Files.writeString(globalDir.resolve("app.properties"), globalProps);
        
        // Create local config in current working directory
        Files.writeString(Path.of("app.properties"), localProps);
        
        // Change user.home temporarily
        var originalHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());
        
        try {
            ZCfg.load("testapp");
            
            // Local overwrites global
            assertThat(ZCfg.string("server.port")).isEqualTo("9090");
            assertThat(ZCfg.string("app.name")).isEqualTo("local");
            
            // Global-only property remains
            assertThat(ZCfg.string("db.url")).isEqualTo("localhost:5432");
        } finally {
            System.setProperty("user.home", originalHome);
            Files.deleteIfExists(Path.of("app.properties"));
            ZCfg.CACHE = null; // Reset cache
        }
    }
    
    @Test
    void systemPropertiesHaveHighestPriority() throws IOException {
        var localProps = """
            test.property=fromFile
            """;
        
        Files.writeString(Path.of("app.properties"), localProps);
        
        System.setProperty("test.property", "fromSystem");
        
        try {
            ZCfg.load("testapp");
            assertThat(ZCfg.string("test.property")).isEqualTo("fromSystem");
        } finally {
            System.clearProperty("test.property");
            Files.deleteIfExists(Path.of("app.properties"));
            ZCfg.CACHE = null; // Reset cache
        }
    }
    
    @Test
    void typedAccessors() {
        System.setProperty("test.port", "8080");
        System.setProperty("test.enabled", "true");
        System.setProperty("test.name", "test");
        
        try {
            ZCfg.load("testapp");
            assertThat(ZCfg.string("test.name")).isEqualTo("test");
            assertThat(ZCfg.string("missing", "default")).isEqualTo("default");
            assertThat(ZCfg.integer("test.port", 0)).isEqualTo(8080);
            assertThat(ZCfg.integer("missing", 9090)).isEqualTo(9090);
            assertThat(ZCfg.bool("test.enabled", false)).isTrue();
            assertThat(ZCfg.bool("missing", true)).isTrue();
        } finally {
            System.clearProperty("test.port");
            System.clearProperty("test.enabled");
            System.clearProperty("test.name");
            ZCfg.CACHE = null; // Reset cache
        }
    }
}