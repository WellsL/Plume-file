Plume File Metadata Database
===========================

A Plume File module to help store file metadata into database.

Installation
------------
1. Install Maven dependency:
```xml
<dependency>
  <groupId>com.coreoz</groupId>
  <artifactId>plume-file-metadata-database</artifactId>
</dependency>
```
2. In the `ApplicationModule` class, install the following Guice module:
```java
install(new GuiceFileMetadataDatabaseModule());
```

3. Create the `plm_file` table by applying the correct [creation script](sql/)