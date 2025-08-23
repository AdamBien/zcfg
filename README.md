# zcfg
Zero Dependency Configuration Utility

A one-class configuration loader for Java applications that reads standard Java properties files from global user directory, local project directory, and system properties with defined precedence rules. No external dependencies required.

## Usage

```java
// Initialize once at application startup
ZCfg.load("myapp");

// Access configuration values with defaults
var port = ZCfg.integer("server.port", 8080);
var debug = ZCfg.bool("debug.enabled", false);
var dbUrl = ZCfg.string("db.url", "localhost:5432");
```

## Configuration Loading Order

1. `~/.myapp/app.properties` - Global user configuration
2. `./app.properties` - Local project configuration (overwrites global)
3. System properties - Highest priority (overwrites all)

## Properties Format

Uses standard Java properties format:

```properties
# Database configuration
db.url=localhost:5432
db.username=admin
db.timeout=30

# Server settings
server.port=8080
debug.enabled=true
```
