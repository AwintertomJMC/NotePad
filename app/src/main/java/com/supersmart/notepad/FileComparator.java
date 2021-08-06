package com.supersmart.notepad;

import java.io.File;
import java.util.Comparator;

public class FileComparator
{
    public static Comparator<File> fileComparatorByName = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            if(o1.isDirectory()&&o2.isFile()) return -1;
            if(o1.isFile()&&o2.isDirectory()) return 1;
            return o1.getName().compareTo(o2.getName());
        }
    };
    public static Comparator<File> fileComparatorBySize = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            if (o1.length()<o2.length()) return -1;
            else if(o1.length()>=o2.length()) return 1;
            return -1;
        }
    };
    public static Comparator<File> fileComparatorByTime = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            if (o1.lastModified()<o2.lastModified()) return -1;
            else if(o1.lastModified()>=o2.lastModified()) return 1;
            return -1;
        }
    };
}
