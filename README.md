# Minecraft Datapack and Resource Pack Builder
[![MIT License](https://img.shields.io/github/license/PerseverantDT/DPRPBuilder)](https://opensource.org/licenses/MIT)

Build a combined datapack and resource pack workspace into its separate outputs 
using only one application.

## Requirements

- Java 17 JRE

## Usage

1. Download the JAR application from the [releases](https://github.com/PerseverantDT/DPRPBuilder/releases) tab.
2. Run the application by executing it from the file system or by running the following command.

```shell
java -jar DPRPBuilder.jar
```

3. On first run, the application will create the build configuration file in `./dprpbuilder.ini` and prompt you to edit the file before re-running the application.
4. Run the application again with the edited build configuration file.
5. Your datapack and resource pack will be built on the provided output path from the build configurations.

## Contributing

### Requirements

- IntelliJ IDEA
  - This project uses the IntelliJ build system because I'm still learning Gradle.
