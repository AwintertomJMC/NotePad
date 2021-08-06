package com.supersmart.notepad.data;

import com.supersmart.notepad.util.FileInfoUtil;

import org.junit.Test;

public class FileInfoTest {

    @Test
    public void testFileInfo() {
        String fileName = ".panzer";
        System.out.println(FileInfoUtil.getFileInfoFromFile(fileName));
    }
}