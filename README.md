OSS Index Website and Utilities
===============================

OSS Index is a website dedicated to making security, licensing, and maintenance information available. Along with the free information, OSS Index also provides premium services.

This repository houses OSS Index's bug tracking, as well as providing a home to related open source libraries and utilities.

Website and Utilities issues
----------------------------
[Click here](https://github.com/twoducks/ossindex/issues) to raise an issue report against OSS Index or the utilities.

OSS Index Report Assistant
--------------------------
The report assistant is used to generate configuration files that may be uploaded to an OSS Index Inventory.

### Building

Clone the repository:

```
git clone https://github.com/twoducks/ossindex.git
```

Build:

```
cd ossidnex/ossindex-core
mvn install
cd ../ossindex-report-assistant
mvn package
```

### Running

Basic usage:

```
java -jar ossindex/ossindex-report-assistant/target/ossindex-report-assistant-0.0.1-SNAPSHOT-jar-with-dependencies.jar

usage: assistant
 -D <dir>       output directory
 -help          print this message
 -merge <arg>   configuration files to merge together
 -scan <dir>    directory to scan in order to create new configuration files
```

Generate configurations for a directory:

```
java -jar ossindex/ossindex-report-assistant/target/ossindex-report-assistant-0.0.1-SNAPSHOT-jar-with-dependencies.jar -scan mysourcedir -D myoutputdir

c:\git.twoducks\ossindex\ossindex-report-assistant\.classpath
c:\git.twoducks\ossindex\ossindex-report-assistant\.gitignore
c:\git.twoducks\ossindex\ossindex-report-assistant\.project
...
```
This will product two output files:

* ossindex.private.json - This file should be kept private, it contains path information
* ossindex.public.json - This file can be uploaded to ossindex.net, it contains only SHA1 checksums

Licensing
---------

All source code is licensed under the [BSD 3-clause license](http://opensource.org/licenses/BSD-3-Clause).
