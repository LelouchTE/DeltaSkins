package com.gmail.tracebachi.DeltaSkins.Shared;

import com.google.common.base.Preconditions;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 9/17/16.
 */
public class Settings
{
    private static Map<String, MessageFormat> formatMap = new HashMap<>();

    public static void setFormatMap(Map<String, MessageFormat> formatMap)
    {
        Settings.formatMap = Preconditions.checkNotNull(formatMap, "FormatMap was null.");
    }

    public static String format(String key, String... args)
    {
        MessageFormat messageFormat = formatMap.get(key);

        if(messageFormat != null)
        {
            return messageFormat.format(args);
        }
        else
        {
            return "Unspecified format: " + key;
        }
    }
}
