package io.github.perseverantdt.dprpbuilder.util;

import com.github.ushiosan23.simple_ini.SimpleIni;
import com.github.ushiosan23.simple_ini.section.SimpleSection;
import com.github.zafarkhaja.semver.Version;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ProgramConfigs {
    Version targetVersion;
    Version buildVersion;
    Path outputPath;
    Path generalPackPngPath;
    Path generalReadMePath;
    Path generalLicensePath;

    String datapackName;
    String datapackDescription;
    Path dataFolderPath;
    boolean datapackBuildZipped;
    Path datapackPackPngPath;
    Path datapackReadMePath;
    Path datapackLicensePath;
    boolean datapackOverwriteOutput;

    String assetPackName;
    String assetPackDescription;
    Path assetFolderPath;
    boolean assetPackBuildZipped;
    Path assetPackPackPngPath;
    Path assetPackReadMePath;
    Path assetPackLicensePath;
    boolean assetPackOverwriteOutput;

    public Version getTargetVersion() {
        return targetVersion;
    }
    public Version getBuildVersion() {
        return buildVersion;
    }
    public Path getOutputPath() {
        return outputPath;
    }
    public Path getGeneralPackPngPath() {
        return generalPackPngPath;
    }
    public Path getGeneralReadMePath() {
        return generalReadMePath;
    }
    public Path getGeneralLicensePath() {
        return generalLicensePath;
    }
    public String getDatapackName() {
        return datapackName;
    }
    public String getDatapackDescription() {
        return datapackDescription;
    }
    public Path getDataFolderPath() {
        return dataFolderPath;
    }
    public boolean isDatapackBuildZipped() {
        return datapackBuildZipped;
    }
    public Path getDatapackPackPngPath() {
        return datapackPackPngPath;
    }
    public Path getDatapackReadMePath() {
        return datapackReadMePath;
    }
    public Path getDatapackLicensePath() {
        return datapackLicensePath;
    }
    public boolean isDatapackOverwriteOutput() {
        return datapackOverwriteOutput;
    }
    public String getAssetPackName() {
        return assetPackName;
    }
    public String getAssetPackDescription() {
        return assetPackDescription;
    }
    public Path getAssetFolderPath() {
        return assetFolderPath;
    }
    public boolean isAssetPackBuildZipped() {
        return assetPackBuildZipped;
    }
    public Path getAssetPackPackPngPath() {
        return assetPackPackPngPath;
    }
    public Path getAssetPackReadMePath() {
        return assetPackReadMePath;
    }
    public Path getAssetPackLicensePath() {
        return assetPackLicensePath;
    }
    public boolean isAssetPackOverwriteOutput() {
        return assetPackOverwriteOutput;
    }
    public static ProgramConfigs fromIniFile(Path iniFile) throws IOException {
        if (Files.notExists(iniFile)) throw new IOException("Could not find the specified file: " + iniFile);
        SimpleIni<SimpleSection> iniConfig = new SimpleIni<>();
        iniConfig.load(iniFile);
        return readIniFile(iniConfig, iniFile.toString());
    }
    public static ProgramConfigs fromIniStream(InputStream iniStream, String streamName) throws IOException {
        SimpleIni<SimpleSection> iniConfig = new SimpleIni<>();
        iniConfig.load(iniStream);
        return readIniFile(iniConfig, streamName);
    }
    public void replaceWith(ProgramConfigs newConfigs) {
        if (newConfigs.targetVersion != null) targetVersion = newConfigs.targetVersion;
        if (newConfigs.buildVersion != null) buildVersion = newConfigs.buildVersion;
        if (newConfigs.outputPath != null) outputPath = newConfigs.outputPath;
        if (newConfigs.generalPackPngPath != null) generalPackPngPath = newConfigs.generalPackPngPath;
        if (newConfigs.generalReadMePath != null) generalReadMePath = newConfigs.generalReadMePath;
        if (newConfigs.generalLicensePath != null) generalLicensePath = newConfigs.generalLicensePath;
        if (newConfigs.datapackName != null) datapackName = newConfigs.datapackName;
        if (newConfigs.datapackDescription != null) datapackDescription = newConfigs.datapackDescription;
        if (newConfigs.dataFolderPath != null) dataFolderPath = newConfigs.dataFolderPath;
        datapackBuildZipped = newConfigs.datapackBuildZipped;
        if (newConfigs.datapackPackPngPath != null) datapackPackPngPath = newConfigs.datapackPackPngPath;
        if (newConfigs.datapackReadMePath != null) datapackReadMePath = newConfigs.datapackReadMePath;
        if (newConfigs.datapackLicensePath != null) datapackLicensePath = newConfigs.datapackLicensePath;
        datapackOverwriteOutput = newConfigs.datapackOverwriteOutput;
        if (newConfigs.assetPackName != null) assetPackName = newConfigs.assetPackName;
        if (newConfigs.assetPackDescription != null) assetPackDescription = newConfigs.assetPackDescription;
        if (newConfigs.assetFolderPath != null) assetFolderPath = newConfigs.assetFolderPath;
        assetPackBuildZipped = newConfigs.assetPackBuildZipped;
        if (newConfigs.assetPackPackPngPath != null) assetPackPackPngPath = newConfigs.assetPackPackPngPath;
        if (newConfigs.assetPackReadMePath != null) assetPackReadMePath = newConfigs.assetPackReadMePath;
        if (newConfigs.assetPackLicensePath != null) assetPackLicensePath = newConfigs.assetPackLicensePath;
        assetPackOverwriteOutput = newConfigs.assetPackOverwriteOutput;
    }

    static ProgramConfigs readIniFile(SimpleIni<SimpleSection> iniConfig, String iniPath) throws NoSuchElementException {
        if (!iniConfig.sectionExists("General"))
            throw new NoSuchElementException(String.format("Could not find a required section in %1$s: Missing [General]", iniPath));
        SimpleSection generalConfigs = iniConfig.getSection("General").orElseThrow();

        Version targetVersion = null;
        Optional<String> _targetVersion = generalConfigs.get("targetVersion");
        if (_targetVersion.isPresent()) {
            targetVersion = Version.valueOf(_targetVersion.get());
        }
        Optional<String> _buildVersion = generalConfigs.get("buildVersion");
        Version buildVersion = null;
        if (_buildVersion.isPresent() && !_buildVersion.get().equals("")) {
            buildVersion = Version.valueOf(_buildVersion.get());
        }
        Path outputPath = null;
        Optional<String> _outputPath = generalConfigs.get("outputPath");
        if (_outputPath.isPresent() && !_outputPath.get().equals("")) {
            outputPath = Path.of(_outputPath.get());
        }
        Optional<String> _generalPackPngPath = generalConfigs.get("packpngPath");
        Path generalPackPngPath = null;
        if (_generalPackPngPath.isPresent() && !_generalPackPngPath.get().equals("")) {
            generalPackPngPath = Path.of(_generalPackPngPath.get());
            if (Files.notExists(generalPackPngPath)) generalPackPngPath = null;
        }
        Optional<String> _generalReadMePath = generalConfigs.get("readmePath");
        Path generalReadMePath = null;
        if (_generalReadMePath.isPresent() && !_generalReadMePath.get().equals("")) {
            generalReadMePath = Path.of(_generalReadMePath.get());
            if (Files.notExists(generalReadMePath)) generalReadMePath = null;
        }
        Optional<String> _generalLicensePath = generalConfigs.get("licensePath");
        Path generalLicensePath = null;
        if (_generalLicensePath.isPresent() && !_generalLicensePath.get().equals("")) {
            generalLicensePath = Path.of(_generalLicensePath.get());
            if (Files.notExists(generalLicensePath)) generalLicensePath = null;
        }

        if (!iniConfig.sectionExists("Datapack")) {
            throw new NoSuchElementException(String.format("Could not find a required section in %1$s: Missing [Datapack]", iniPath));
        }
        SimpleSection datapackConfigs = iniConfig.getSection("Datapack").orElseThrow();
        String datapackName = null;
        Optional<String> _datapackName = datapackConfigs.get("name");
        if (_datapackName.isPresent()) datapackName = _datapackName.get();
        String datapackDescription = null;
        Optional<String> _datapackDescription = datapackConfigs.get("description");
        if (_datapackDescription.isPresent()) datapackDescription = _datapackDescription.get();
        Path dataFolderPath = null;
        Optional<String> _dataFolderPath = datapackConfigs.get("dataFolderPath");
        if (_dataFolderPath.isPresent() && !_dataFolderPath.get().equals("")) {
            dataFolderPath = Path.of(_dataFolderPath.get());
            if (Files.notExists(dataFolderPath)) dataFolderPath = null;
        }
        boolean datapackBuildZipped = datapackConfigs.getAsBooleanOrDefault("buildZipped", true);
        Path datapackPackPngPath = null;
        Optional<String> _datapackPackPngPath = datapackConfigs.get("packpngPath");
        if (_datapackPackPngPath.isPresent() && !_datapackPackPngPath.get().equals("")) {
            datapackPackPngPath = Path.of(_datapackPackPngPath.get());
            if (Files.notExists(datapackPackPngPath)) datapackPackPngPath = null;
        }
        Path datapackReadMePath = null;
        Optional<String> _datapackReadMePath = datapackConfigs.get("readmePath");
        if (_datapackReadMePath.isPresent() && !_datapackReadMePath.get().equals("")) {
            datapackReadMePath = Path.of(_datapackReadMePath.get());
            if (Files.notExists(datapackReadMePath)) datapackReadMePath = null;
        }
        Path datapackLicensePath = null;
        Optional<String> _datapackLicensePath = datapackConfigs.get("licensePath");
        if (_datapackLicensePath.isPresent() && !_datapackLicensePath.get().equals("")) {
            datapackLicensePath = Path.of(_datapackLicensePath.get());
            if (Files.notExists(datapackLicensePath)) datapackLicensePath = null;
        }
        boolean datapackOverwriteOutput = datapackConfigs.getAsBooleanOrDefault("overwrite", false);

        if (!iniConfig.sectionExists("ResourcePack")) {
            throw new NoSuchElementException(String.format("Could not find a required section in %1$s: Missing [Resource Pack]", iniPath));
        }
        SimpleSection resourcePackConfigs = iniConfig.getSection("ResourcePack").orElseThrow();
        String resourcePackName = null;
        Optional<String> _resourcePackName = resourcePackConfigs.get("name");
        if (_resourcePackName.isPresent()) resourcePackName = _resourcePackName.get();
        String resourcePackDescription = null;
        Optional<String> _resourcePackDescription = resourcePackConfigs.get("description");
        if (_resourcePackDescription.isPresent()) resourcePackDescription = _resourcePackDescription.get();
        Path assetFolderPath = null;
        Optional<String> _assetFolderPath = resourcePackConfigs.get("assetsFolderPath");
        if (_assetFolderPath.isPresent() && !_assetFolderPath.get().equals("")) {
            assetFolderPath = Path.of(_assetFolderPath.get());
            if (Files.notExists(assetFolderPath)) assetFolderPath = null;
        }
        boolean resourcePackBuildZipped = resourcePackConfigs.getAsBooleanOrDefault("buildZipped", true);
        Path resourcePackPackPngPath = null;
        Optional<String> _resourcePackPackPngPath = resourcePackConfigs.get("packpngPath");
        if (_resourcePackPackPngPath.isPresent() && !_resourcePackPackPngPath.get().equals("")) {
            resourcePackPackPngPath = Path.of(_resourcePackPackPngPath.get());
            if (Files.notExists(resourcePackPackPngPath)) resourcePackPackPngPath = null;
        }
        Path resourcePackReadMePath = null;
        Optional<String> _resourcePackReadMePath = resourcePackConfigs.get("readmePath");
        if (_resourcePackReadMePath.isPresent() && !_resourcePackReadMePath.get().equals("")) {
            resourcePackReadMePath = Path.of(_resourcePackReadMePath.get());
            if (Files.notExists(resourcePackReadMePath)) resourcePackReadMePath = null;
        }
        Path resourcePackLicensePath = null;
        Optional<String> _resourcePackLicensePath = resourcePackConfigs.get("licensePath");
        if (_resourcePackLicensePath.isPresent() && !_resourcePackLicensePath.get().equals("")) {
            resourcePackLicensePath = Path.of(_resourcePackLicensePath.get());
            if (Files.notExists(resourcePackLicensePath)) resourcePackLicensePath = null;
        }
        boolean resourcePackOverwriteOutput = resourcePackConfigs.getAsBooleanOrDefault("overwrite", false);

        return new ProgramConfigs(targetVersion, buildVersion, outputPath, generalPackPngPath, generalReadMePath, generalLicensePath, datapackName, datapackDescription, dataFolderPath, datapackBuildZipped, datapackPackPngPath, datapackReadMePath, datapackLicensePath, datapackOverwriteOutput, resourcePackName, resourcePackDescription, assetFolderPath, resourcePackBuildZipped, resourcePackPackPngPath, resourcePackReadMePath, resourcePackLicensePath, resourcePackOverwriteOutput);
    }

    ProgramConfigs(Version targetVersion, Version buildVersion, Path outputPath, Path generalPackPngPath, Path generalReadMePath, Path generalLicensePath, String datapackName, String datapackDescription, Path dataFolderPath, boolean datapackBuildZipped, Path datapackPackPngPath, Path datapackReadMePath, Path datapackLicensePath, boolean datapackOverwriteOutput, String assetPackName, String assetPackDescription, Path assetFolderPath, boolean assetPackBuildZipped, Path assetPackPackPngPath, Path assetPackReadMePath, Path assetPackLicensePath, boolean assetPackOverwriteOutput) {
        this.targetVersion = targetVersion;
        this.buildVersion = buildVersion;
        this.outputPath = outputPath;
        this.generalPackPngPath = generalPackPngPath;
        this.generalReadMePath = generalReadMePath;
        this.generalLicensePath = generalLicensePath;
        this.datapackName = datapackName;
        this.datapackDescription = datapackDescription;
        this.dataFolderPath = dataFolderPath;
        this.datapackBuildZipped = datapackBuildZipped;
        this.datapackPackPngPath = datapackPackPngPath;
        this.datapackReadMePath = datapackReadMePath;
        this.datapackLicensePath = datapackLicensePath;
        this.datapackOverwriteOutput = datapackOverwriteOutput;
        this.assetPackName = assetPackName;
        this.assetPackDescription = assetPackDescription;
        this.assetFolderPath = assetFolderPath;
        this.assetPackBuildZipped = assetPackBuildZipped;
        this.assetPackPackPngPath = assetPackPackPngPath;
        this.assetPackReadMePath = assetPackReadMePath;
        this.assetPackLicensePath = assetPackLicensePath;
        this.assetPackOverwriteOutput = assetPackOverwriteOutput;
    }
}
