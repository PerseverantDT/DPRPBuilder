package io.github.perseverantdt.dprpbuilder.core;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.perseverantdt.dprpbuilder.util.InputOutput;
import io.github.perseverantdt.dprpbuilder.util.PackFormatEntry;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Datapack {
    static final PackFormatEntry[] dpFormats = new PackFormatEntry[] {
        new PackFormatEntry(4, "1.13 - 1.14.4"),
        new PackFormatEntry(5, "1.15 - 1.16.1"),
        new PackFormatEntry(6, "1.16.2 - 1.16.5"),
        new PackFormatEntry(7, "1.17 - 1.17.1"),
        new PackFormatEntry(8, "1.18 - 1.18.1"),
        new PackFormatEntry(9, "1.18.2"),
        new PackFormatEntry(10, "1.19 - 1.19.2")
    };
    // TODO: Add support for files used by MC mods, if any.
    static final String[] validExtensions = new String[] {
        "mcfunction",
        "json",
        "mcmeta"
    };
    String name;
    String description;
    int format;
    Version version;
    Path dataFolderPath;
    Path[] files;
    Path packpngPath;
    Path readmePath;
    Path licensePath;

    public static Datapack createDatapack(Path dataFolderPath, Version targetVersion, String name, String description, Version buildVersion, Path packpngPath, Path readmePath, Path licensePath) {
        Path[] files = InputOutput.getFiles(dataFolderPath, true);
        ArrayList<Path> cleanedFiles = new ArrayList<>();
        for (Path file : files) {
            if (Arrays.stream(validExtensions).anyMatch(ext -> FilenameUtils.getExtension(file.toString()).equals(ext))) {
                cleanedFiles.add(file);
            }
        }
        files = cleanedFiles.toArray(new Path[0]);
        int format = 10;
        for (PackFormatEntry dpFormat : dpFormats) {
            if (dpFormat.includes(targetVersion)) {
                format = dpFormat.getFormat();
            }
        }

        return new Datapack(name, description, format, buildVersion, dataFolderPath, files, packpngPath, readmePath, licensePath);
    }

    public void buildAsZipInMemory(Path outputPath, boolean overwrite) throws IOException {
        ByteArrayOutputStream datapackInMemory = new ByteArrayOutputStream();
        try (ZipOutputStream datapackZip = new ZipOutputStream(datapackInMemory)) {
            JsonObject pack = new JsonObject();
            pack.add("pack_format", new JsonPrimitive(format));
            pack.add("description", new JsonPrimitive(description));
            JsonObject mcmeta = new JsonObject();
            mcmeta.add("pack", pack);

            Gson builder = new GsonBuilder().setPrettyPrinting().create();

            ZipEntry packmcmeta = new ZipEntry("pack.mcmeta");
            datapackZip.putNextEntry(packmcmeta);
            datapackZip.write(builder.toJson(mcmeta).getBytes(StandardCharsets.UTF_8));
            datapackZip.closeEntry();

            if (packpngPath != null) {
                ZipEntry packpng = new ZipEntry("pack.png");
                datapackZip.putNextEntry(packpng);
                datapackZip.write(Files.readAllBytes(packpngPath));
                datapackZip.closeEntry();
            }
            if (readmePath != null) {
                ZipEntry readme = new ZipEntry("README" + ((!FilenameUtils.getExtension(readmePath.toString()).equals("")) ? "." + FilenameUtils.getExtension(readmePath.toString()) : ""));
                datapackZip.putNextEntry(readme);
                datapackZip.write(Files.readAllBytes(readmePath));
                datapackZip.closeEntry();
            }
            if (licensePath != null) {
                ZipEntry license = new ZipEntry("LICENSE" + ((!FilenameUtils.getExtension(licensePath.toString()).equals("")) ? "." + FilenameUtils.getExtension(licensePath.toString()) : ""));
                datapackZip.putNextEntry(license);
                datapackZip.write(Files.readAllBytes(licensePath));
                datapackZip.closeEntry();
            }

            for (Path fileToAdd : files) {
                if (Files.notExists(fileToAdd)) continue;

                String entryName = fileToAdd.toAbsolutePath().toString();
                entryName = entryName.replace(dataFolderPath.toString(), "data").replace(File.separatorChar, '/');
                ZipEntry fileEntry = new ZipEntry(entryName);
                datapackZip.putNextEntry(fileEntry);
                datapackZip.write(Files.readAllBytes(fileToAdd));
                datapackZip.closeEntry();
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
            if (Files.exists(zipPath)) {
                try {
                    Files.delete(zipPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        else {
            int iteration = 0;
            while (Files.exists(Path.of(outputPath.toString(), zipName + (iteration != 0 ? "(" + iteration + ")" : "") + ".zip"))) {
                iteration++;
            }
            zipPath = Path.of(outputPath.toString(), zipName + (iteration != 0 ? "(" + iteration + ")" : "") + ".zip");
        }

            Files.write(zipPath, datapackInMemory.toByteArray());
            System.out.printf("Datapack created at %1$s.\n", zipPath.toAbsolutePath());
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

            if (packpngPath != null) Files.write(Path.of(finalFolderName.toString(), "pack.png"), Files.readAllBytes(packpngPath));
            if (readmePath != null) Files.write(Path.of(finalFolderName.toString(), "README" + (!FilenameUtils.getExtension(readmePath.toString()).equals("") ? "." + FilenameUtils.getExtension(readmePath.toString()) : "")), Files.readAllBytes(readmePath));
            if (licensePath != null) Files.write(Path.of(finalFolderName.toString(), "LICENSE" + (!FilenameUtils.getExtension(licensePath.toString()).equals("") ? "." + FilenameUtils.getExtension(licensePath.toString()) : "")), Files.readAllBytes(licensePath));

            for (Path fileToAdd : files) {
                String filePath = fileToAdd.toAbsolutePath().toString().replace(dataFolderPath.toAbsolutePath().toString(), "");
                Path outPath = Path.of(finalFolderName.toString(), "data", filePath);

                if (Files.notExists(outPath.getParent())) {
                    Files.createDirectories(outPath.getParent().toAbsolutePath());
                }
                Files.write(outPath, Files.readAllBytes(fileToAdd));
            }

        System.out.printf("Datapack created at %1$s.\n", finalFolderName.toAbsolutePath());
    }

    Datapack(String name, String description, int format, Version version, Path dataFolderPath, Path[] files, Path packpngPath, Path readmePath, Path licensePath) {
        this.name = name;
        this.description = description;
        this.format = format;
        this.version = version;
        this.dataFolderPath = dataFolderPath;
        this.files = files;
        this.packpngPath = packpngPath;
        this.readmePath = readmePath;
        this.licensePath = licensePath;
    }
}
