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
        popup.setAlwaysOnTop(true);
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
            e.printStackTrace();
        }

        if (Files.notExists(configPath)) {
            System.out.println("No build configs found. Creating build configs...");
            try (InputStream defaultConfigs = Main.class.getResourceAsStream("/dprpbuilder.ini")) {
                if (defaultConfigs == null) {
                    JOptionPane.showMessageDialog(popup, "Could not find default configs. Please report this to Perseverant Determination.", "DPRPBuilder", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }

                Files.write(configPath, defaultConfigs.readAllBytes());
                JOptionPane.showMessageDialog(popup, "Build configurations created at " + configPath + ". Please edit them before running the application again.", "DPRPBuilder", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(popup, "An error occurred while trying to write default build configs.", "DPRPBuilder", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        }

        ProgramConfigs configs = null;

        try {
            // TODO: Generate program configs from command line.
            try (InputStream defaultConfigs = Main.class.getResourceAsStream("/dprpbuilder.ini")) {
                if (defaultConfigs == null) {
                    JOptionPane.showMessageDialog(popup, "Could not find default configs. Please report this to Perseverant Determination.", "DPRPBuilder", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }

                configs = ProgramConfigs.fromIniStream(defaultConfigs, "Default configs");
            }

            ProgramConfigs userConfigs = ProgramConfigs.fromIniFile(configPath);
            configs.replaceWith(userConfigs);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(popup, "An error occurred while trying to read build configuration files.", "DPRPBuilder", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        if (configs.getDataFolderPath() != null) {
            Datapack datapack = Datapack.createDatapack(configs.getDataFolderPath(), configs.getTargetVersion(), configs.getDatapackName(), configs.getDatapackDescription(), configs.getBuildVersion(), (configs.getDatapackPackPngPath() != null) ? configs.getDatapackPackPngPath() : configs.getGeneralPackPngPath(), (configs.getDatapackReadMePath() != null) ? configs.getDatapackReadMePath() : configs.getGeneralReadMePath(), (configs.getDatapackLicensePath() != null) ? configs.getDatapackLicensePath() : configs.getGeneralLicensePath());
            try {
                if (configs.isDatapackBuildZipped())
                    datapack.buildAsZipInMemory(configs.getOutputPath(), configs.isDatapackOverwriteOutput());
                else {
                    datapack.buildAsFolder(configs.getOutputPath(), configs.isDatapackOverwriteOutput());
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(popup, "An error occurred while trying to build the datapack.", "DPRPBuilder", JOptionPane.ERROR_MESSAGE);
                // TODO: Implement an output cleaning algorithm if, for some reason, building to folder fails.
                e.printStackTrace();
                System.exit(1);
            }
        }
        else {
            JOptionPane.showMessageDialog(popup, "Could not find the specified data folder. Skipping datapack build.", "DPRPBuilder", JOptionPane.WARNING_MESSAGE);
        }

        if (configs.getAssetFolderPath() != null) {
            ResourcePack resourcePack = ResourcePack.createResourcePack(configs.getAssetFolderPath(), configs.getTargetVersion(), configs.getAssetPackName(), configs.getAssetPackDescription(), configs.getBuildVersion(), (configs.getAssetPackPackPngPath() != null) ? configs.getAssetPackPackPngPath() : configs.getGeneralPackPngPath(), (configs.getAssetPackReadMePath() != null) ? configs.getAssetPackReadMePath() : configs.getGeneralReadMePath(), (configs.getAssetPackLicensePath() != null) ? configs.getAssetPackLicensePath() : configs.getGeneralLicensePath());
            try {
                if (configs.isAssetPackBuildZipped())
                    resourcePack.buildAsZipInMemory(configs.getOutputPath(), configs.isAssetPackOverwriteOutput());
                else resourcePack.buildAsFolder(configs.getOutputPath(), configs.isAssetPackOverwriteOutput());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(popup, "An error occurred while trying to build the resource pack.", "DPRPBuilder", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        }
        else {
            JOptionPane.showMessageDialog(popup, "Could not find the specified assets folder. Skipping resource pack build.", "DPRPBuilder", JOptionPane.WARNING_MESSAGE);
        }

        popup.dispose();
        System.exit(0);
    }
}
