package io.github.perseverantdt.dprpbuilder;

import com.github.zafarkhaja.semver.Version;
import io.github.perseverantdt.dprpbuilder.core.Datapack;
import io.github.perseverantdt.dprpbuilder.core.ResourcePack;
import io.github.perseverantdt.dprpbuilder.util.ProgramConfigs;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    static final Path configPath = Path.of("./dprpbuilder.ini");
    static final Version programVersion = Version.valueOf("0.1.0");
    static final URL versionUrl;
    static final URL releaseUrl;
    static {
        try {
            versionUrl = new URL("https://raw.githubusercontent.com/PerseverantDT/DPRPBuilder/main/VERSION.txt");
            releaseUrl = new URL("https://github.com/PerseverantDT/DPRPBuilder/releases");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        JFrame popup = new JFrame();
        try {
            Scanner s = new Scanner(versionUrl.openStream());

            String latestVersionText = s.nextLine();
            Version latestVersion = Version.valueOf(latestVersionText);

            if (latestVersion.greaterThan(programVersion)) {
                int update = JOptionPane.showConfirmDialog(popup, "New version detected. Go to latest release for update?", "DPRPBuilder", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (update == JOptionPane.YES_OPTION) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.browse(releaseUrl.toURI());
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        String os = System.getProperty("os.name").toLowerCase();
                        Runtime runtime = Runtime.getRuntime();

                        if (os.contains("win")) {
                            runtime.exec("rundll32 url.dll,FileProtocolHandler " + releaseUrl.toString());
                        }
                        else if (os.contains("mac")) {
                            runtime.exec("open " + releaseUrl.toString());
                        }
                        else if (os.contains("nix") || os.contains("nux")) {
                            runtime.exec("xdg-open " + releaseUrl.toString());
                        }
                    }
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(popup, "Could not get the latest version of the application.", "DPRPBuilder", JOptionPane.WARNING_MESSAGE);
        }

        if (Files.notExists(configPath)) {
            System.out.println("No build configs found. Creating build configs...");
            try (InputStream defaultConfigs = Main.class.getResourceAsStream("/dprpbuilder.ini")) {
                if (defaultConfigs == null) {
                    JOptionPane.showMessageDialog(popup, "Could not find default configs. Please report this to Perseverant Determination.", "DPRPBuilder", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }

                Files.write(configPath, defaultConfigs.readAllBytes());
                System.out.printf("Build configs created at %1$s. Please edit them before running this program again.\n", configPath);
                System.exit(0);
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(popup, "An error occurred while trying to write default build configs.", "DPRPBuilder", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        ProgramConfigs configs;

        try {
            // TODO: Generate program configs from command line.
            try (InputStream defaultConfigs = Main.class.getResourceAsStream("/dprpbuilder.ini")) {
                if (defaultConfigs == null) {
                    JOptionPane.showMessageDialog(popup, "Could not find default configs. Please report this to Perseverant Determination.", "DPRPBuilder", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
                configs = ProgramConfigs.fromIniStream(Main.class.getResourceAsStream("/dprpbuilder.ini"), "Default configs");
            }
            ProgramConfigs userConfigs = ProgramConfigs.fromIniFile(configPath);
            configs.replaceWith(userConfigs);
        }
        catch (IOException e) {
            
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
