package io.github.perseverantdt.dprpbuilder.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Stream;

public class InputOutput {
    public static Path @NotNull [] getFiles(Path targetFolder, boolean recursive) {
        ArrayList<Path> files = new ArrayList<>();
        LinkedList<Path> folderPaths = new LinkedList<>();
        folderPaths.push(targetFolder);

        while (!folderPaths.isEmpty()) {
            Path folderPath = folderPaths.pollFirst();
            try (Stream<Path> children = Files.list(folderPath)) {
                children.forEach(child -> {
                    if (Files.isRegularFile(child)) files.add(child);
                    else if (Files.isDirectory(child) && recursive) folderPaths.push(child);
                });
            } catch (IOException e) {
                e.printStackTrace();
                return new Path[0];
            }
        }

        return files.toArray(new Path[0]);
    }
    public static Path @NotNull [] getFolders(Path targetFolder, boolean recursive) {
        ArrayList<Path> checkedFolders = new ArrayList<>();
        LinkedList<Path> foldersToCheck = new LinkedList<>();
        foldersToCheck.push(targetFolder);

        while (!foldersToCheck.isEmpty()) {
            Path folderToCheck = foldersToCheck.pollFirst();
            try (Stream<Path> children = Files.list(folderToCheck)) {
                children.forEach(child -> {
                    if (Files.isDirectory(child)) {
                        checkedFolders.add(child);
                        if (recursive) foldersToCheck.push(child);
                    }
                });
            }
            catch (IOException e) {
                e.printStackTrace();
                return new Path[0];
            }
        }

        return checkedFolders.toArray(new Path[0]);
    }
}
