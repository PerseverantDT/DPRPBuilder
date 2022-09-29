package io.github.perseverantdt.dprpbuilder;

import com.github.zafarkhaja.semver.Version;
import io.github.perseverantdt.dprpbuilder.core.Datapack;
import io.github.perseverantdt.dprpbuilder.core.ResourcePack;
import io.github.perseverantdt.dprpbuilder.util.ProgramConfigs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    static final Path configPath = Path.of("./dprpbuilder.ini");
    static final Version programVersion = Version.valueOf("0.1.0");

    public static void main(String[] args) {
        if (Files.notExists(configPath)) {
            System.out.println("No build configs found. Creating build configs...");
            try (InputStream defaultConfigs = Main.class.getResourceAsStream("/dprpbuilder.ini")) {
                if (defaultConfigs == null) {
                    throw new RuntimeException("Could not find default configs. Please report this to Perseverant Determination");
                }

                Files.write(configPath, defaultConfigs.readAllBytes());
                System.out.printf("Build configs created at %1$s. Please edit them before running this program again.\n", configPath);
                System.exit(0);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ProgramConfigs configs;

        try {
            // TODO: Generate program configs from command line.
            configs = ProgramConfigs.fromIniStream(Main.class.getResourceAsStream("/dprpbuilder.ini"), "Default configs");
            ProgramConfigs userConfigs = ProgramConfigs.fromIniFile(configPath);
            configs.replaceWith(userConfigs);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (configs.getDataFolderPath() != null) {
            Datapack datapack = Datapack.createDatapack(configs.getDataFolderPath(), configs.getTargetVersion(), configs.getDatapackName(), configs.getDatapackDescription(), configs.getBuildVersion(), (configs.getDatapackPackPngPath() != null) ? configs.getDatapackPackPngPath() : configs.getGeneralPackPngPath(), (configs.getDatapackReadMePath() != null) ? configs.getDatapackReadMePath() : configs.getGeneralReadMePath(), (configs.getDatapackLicensePath() != null) ? configs.getDatapackLicensePath() : configs.getGeneralLicensePath());
            try {
                if (configs.isDatapackBuildZipped())
                    datapack.buildAsZipInMemory(configs.getOutputPath(), configs.isDatapackOverwriteOutput());
                else {
                    datapack.buildAsFolder(configs.getOutputPath(), configs.isDatapackOverwriteOutput());
                    // TODO: Implement an output cleaning algorithm if, for some reason, building to folder fails.
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            System.out.println("Could not find data folder. Skipping datapack build.");
        }

        if (configs.getAssetFolderPath() != null) {
            ResourcePack resourcePack = ResourcePack.createResourcePack(configs.getAssetFolderPath(), configs.getTargetVersion(), configs.getAssetPackName(), configs.getAssetPackDescription(), configs.getBuildVersion(), (configs.getAssetPackPackPngPath() != null) ? configs.getAssetPackPackPngPath() : configs.getGeneralPackPngPath(), (configs.getAssetPackReadMePath() != null) ? configs.getAssetPackReadMePath() : configs.getGeneralReadMePath(), (configs.getAssetPackLicensePath() != null) ? configs.getAssetPackLicensePath() : configs.getGeneralLicensePath());
            try {
                if (configs.isAssetPackBuildZipped())
                    resourcePack.buildAsZipInMemory(configs.getOutputPath(), configs.isAssetPackOverwriteOutput());
                else resourcePack.buildAsFolder(configs.getOutputPath(), configs.isAssetPackOverwriteOutput());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            System.out.println("Could not find assets folder. Skipping resource pack build...");
        }
    }
}
