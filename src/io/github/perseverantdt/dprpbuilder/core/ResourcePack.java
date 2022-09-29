package io.github.perseverantdt.dprpbuilder.core;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.perseverantdt.dprpbuilder.util.InputOutput;
import io.github.perseverantdt.dprpbuilder.util.PackFormatEntry;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePack {
    static final PackFormatEntry[] rpFormats = new PackFormatEntry[]{
        new PackFormatEntry(1, "1.6.1 - 1.8.9"),
        new PackFormatEntry(2, "1.9 - 1.10.2"),
        new PackFormatEntry(3, "1.11 - 1.12.2"),
        new PackFormatEntry(4, "1.13 - 1.14.4"),
        new PackFormatEntry(5, "1.15 - 1.16.1"),
        new PackFormatEntry(6, "1.16.2 - 1.16.5"),
        new PackFormatEntry(7, "1.17.x"),
        new PackFormatEntry(8, "1.18.x"),
        new PackFormatEntry(9, "1.19.x")
    };
    // TODO: Add support for files used by MC mods, especially Optifine.
    static final String[] validExtensions = new String[]{
        "json",
        "mcmeta",
        "obj",
        "ogg",
        "fsh",
        "vsh",
        "nbt",
        "ttf",
        "glsl"
    };

    String name;
    String description;
    int format;
    Version version;
    Path assetFolderPath;
    Path[] files;
    Path packpngPath;
    Path readmePath;
    Path licensePath;

    public static ResourcePack createResourcePack(Path assetFolderPath, Version targetVersion, String name, String description, Version buildVersion, Path packpngPath, Path readmePath, Path licensePath) {
        Path[] files = InputOutput.getFiles(assetFolderPath, true);
        ArrayList<Path> cleanedFiles = new ArrayList<>();
        for (Path file : files) {
            if (Arrays.stream(validExtensions).anyMatch(ext -> FilenameUtils.getExtension(file.toString()).equals(ext))) {
                cleanedFiles.add(file);
            }
        }
        files = cleanedFiles.toArray(new Path[0]);
        int format = 10;
        for (PackFormatEntry rpFormat : rpFormats) {
            if (rpFormat.includes(targetVersion)) {
                format = rpFormat.getFormat();
            }
        }

        return new ResourcePack(name, description, format, buildVersion, assetFolderPath, files, packpngPath, readmePath, licensePath);
    }

    public void buildAsZipInMemory(Path outputPath, boolean overwrite) throws IOException {
        ByteArrayOutputStream resourcePackInMemory = new ByteArrayOutputStream();
        try (ZipOutputStream resourcePackZip = new ZipOutputStream(resourcePackInMemory)) {
            JsonObject pack = new JsonObject();
            pack.add("pack_format", new JsonPrimitive(format));
            pack.add("description", new JsonPrimitive(description));
            JsonObject mcmeta = new JsonObject();
            mcmeta.add("pack", pack);

            Gson builder = new GsonBuilder().setPrettyPrinting().create();

            ZipEntry packmcmeta = new ZipEntry("pack.mcmeta");
            resourcePackZip.putNextEntry(packmcmeta);
            resourcePackZip.write(builder.toJson(mcmeta).getBytes(StandardCharsets.UTF_8));
            resourcePackZip.closeEntry();

            if (packpngPath != null) {
                ZipEntry packpng = new ZipEntry("pack.png");
                resourcePackZip.putNextEntry(packpng);
                resourcePackZip.write(Files.readAllBytes(packpngPath));
                resourcePackZip.closeEntry();
            }
            if (readmePath != null) {
                ZipEntry readme = new ZipEntry("README" + ((!FilenameUtils.getExtension(readmePath.toString()).equals("")) ? "." + FilenameUtils.getExtension(readmePath.toString()) : ""));
                resourcePackZip.putNextEntry(readme);
                resourcePackZip.write(Files.readAllBytes(readmePath));
                resourcePackZip.closeEntry();
            }
            if (licensePath != null) {
                ZipEntry license = new ZipEntry("LICENSE" + ((!FilenameUtils.getExtension(licensePath.toString()).equals("")) ? "." + FilenameUtils.getExtension(licensePath.toString()) : ""));
                resourcePackZip.putNextEntry(license);
                resourcePackZip.write(Files.readAllBytes(licensePath));
                resourcePackZip.closeEntry();
            }

            for (Path fileToAdd : files) {
                if (Files.notExists(fileToAdd)) continue;

                String entryName = fileToAdd.toAbsolutePath().toString();
                entryName = entryName.replace(assetFolderPath.toString(), "assets").replace(File.separatorChar, '/');
                ZipEntry fileEntry = new ZipEntry(entryName);
                resourcePackZip.putNextEntry(fileEntry);
                resourcePackZip.write(Files.readAllBytes(fileToAdd));
                resourcePackZip.closeEntry();
            }
        }

        if (Files.notExists(outputPath)) {
            Files.createDirectories(outputPath.toAbsolutePath());
        }
        StringBuilder _zipName = new StringBuilder();
        _zipName.append(name);
        if (version != null) {
            _zipName.append(" v").append(version);
        }

        String zipName = _zipName.toString();
        Path zipPath;
        if (overwrite) {
            zipPath = Path.of(outputPath.toString(), zipName + ".zip");
            Files.deleteIfExists(zipPath);
        }
        else {
            int iteration = 0;
            while (Files.exists(Path.of(outputPath.toString(), zipName + (iteration != 0 ? "(" + iteration + ")" : "") + ".zip"))) {
                iteration++;
            }

            zipPath = Path.of(outputPath.toString(), zipName + (iteration != 0 ? "(" + iteration + ")" : "") + ".zip");
        }

        Files.write(zipPath, resourcePackInMemory.toByteArray());
        System.out.printf("Resource pack created at %1$s.\n", zipPath.toAbsolutePath());
    }
    public void buildAsFolder(Path outputPath, boolean overwrite) throws IOException {
        if (Files.notExists(outputPath)) {
            Files.createDirectories(outputPath.toAbsolutePath());
        }

        StringBuilder _folderName = new StringBuilder();
        _folderName.append(name);
        if (version != null) _folderName.append(" v").append(version);
        String folderName = _folderName.toString();

        Path finalFolderName;
        if (overwrite) {
            finalFolderName = Path.of(outputPath.toString(), folderName);
            if (Files.exists(finalFolderName)) {
                Path[] filesInFolder = InputOutput.getFiles(finalFolderName, true);
                Path[] foldersInFolder = InputOutput.getFolders(finalFolderName, true);

                for (Path file : filesInFolder) {
                    Files.deleteIfExists(file);
                }
                for (Path folder : foldersInFolder) {
                    Files.deleteIfExists(folder);
                }
            }
            else {
                Files.createDirectory(finalFolderName);
            }
        }
        else {
            int iteration = 0;
            while (Files.exists(Path.of(outputPath.toString(), folderName + (iteration != 0 ? "(" + iteration + ")" : "")))) {
                iteration++;
            }

            finalFolderName = Path.of(outputPath.toString(), folderName + (iteration != 0 ? "(" + iteration + ")" : ""));
            Files.createDirectory(finalFolderName);
        }

        Gson builder = new GsonBuilder().setPrettyPrinting().create();

        JsonObject pack = new JsonObject();
        pack.add("pack_format", new JsonPrimitive(format));
        pack.add("description", new JsonPrimitive(description));
        JsonObject mcmeta = new JsonObject();
        mcmeta.add("pack", pack);

        Files.writeString(Path.of(finalFolderName.toString(), "pack.mcmeta"), builder.toJson(mcmeta));

        if (packpngPath != null)
            Files.write(Path.of(finalFolderName.toString(), "pack.png"), Files.readAllBytes(packpngPath));
        if (readmePath != null)
            Files.write(Path.of(finalFolderName.toString(), "README" + (!FilenameUtils.getExtension(readmePath.toString()).equals("") ? "." + FilenameUtils.getExtension(readmePath.toString()) : "")), Files.readAllBytes(readmePath));
        if (licensePath != null)
            Files.write(Path.of(finalFolderName.toString(), "LICENSE" + (!FilenameUtils.getExtension(licensePath.toString()).equals("") ? "." + FilenameUtils.getExtension(licensePath.toString()) : "")), Files.readAllBytes(licensePath));

        for (Path fileToAdd : files) {
            String filePath = fileToAdd.toAbsolutePath().toString().replace(assetFolderPath.toAbsolutePath().toString(), "");
            Path outPath = Path.of(finalFolderName.toString(), "assets", filePath);

            if (Files.notExists(outPath.getParent())) {
                Files.createDirectories(outPath.getParent().toAbsolutePath());
            }
            Files.write(outPath, Files.readAllBytes(fileToAdd));
        }

        System.out.printf("Resource Pack created at %1$s.\n", finalFolderName.toAbsolutePath());
    }

    ResourcePack(String name, String description, int format, Version version, Path assetFolderPath, Path[] files, Path packpngPath, Path readmePath, Path licensePath) {
        this.name = name;
        this.description = description;
        this.format = format;
        this.version = version;
        this.assetFolderPath = assetFolderPath;
        this.files = files;
        this.packpngPath = packpngPath;
        this.readmePath = readmePath;
        this.licensePath = licensePath;
    }
}
