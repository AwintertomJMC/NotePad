package com.supersmart.notepad;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class CharsetNames
{
    static String standardCharsetsNames[] = {StandardCharsets.ISO_8859_1.name(),
            StandardCharsets.US_ASCII.name(),
            StandardCharsets.UTF_8.name(),
            StandardCharsets.UTF_16.name(),
            StandardCharsets.UTF_16BE.name(),
            StandardCharsets.UTF_16LE.name()};
}
