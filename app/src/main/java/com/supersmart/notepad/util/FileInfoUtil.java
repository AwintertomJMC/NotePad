package com.supersmart.notepad.util;

import com.supersmart.notepad.data.FileInfo;

import java.io.File;

public class FileInfoUtil {

    public static FileInfo getFileInfoFromFile(File file) {
        return file == null ? null : getFileInfoFromFile(file.getName());
    }

    public static FileInfo getFileInfoFromFile(String fileFullName) {
        if (fileFullName == null||fileFullName.isEmpty()) {
            return null;
        }
        int pointIndex = fileFullName.lastIndexOf('.');
        FileInfo fileInfo = new FileInfo();
        if (pointIndex == -1) {
            fileInfo.setFileName(fileFullName);
            fileInfo.setFileType("");
        }
        else {
            fileInfo.setFileName(fileFullName.substring(0, pointIndex));
            fileInfo.setFileType(fileFullName.substring(pointIndex + 1));
        }
        return fileInfo;
    }

    public static String getFullPathOfFile(String path, String fileName, String fileType) {
        StringBuilder stringBuilder = new StringBuilder(path);
        if (!path.endsWith("/")) {
            stringBuilder.append("/");
        }
        stringBuilder.append(fileName);
        if (fileType!=null && !fileType.isEmpty()) {
            if (fileType.startsWith(".")) {
                stringBuilder.append(fileType);
            }
            else {
                stringBuilder.append(".").append(fileType);
            }
        }
        return stringBuilder.toString();
    }
}
