package com.gmail.tracebachi.DeltaSkins.Shared;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;

/**
 * GeeItsZee (tracebachi@gmail.com)
 */
public interface JsonHelper
{
    static JsonElement readJsonElementFromFile(File file, JsonElement defaultReturn)
    {
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            JsonParser parser = new JsonParser();
            JsonElement parsed = parser.parse(reader);

            reader.close();
            return parsed;
        }
        catch(FileNotFoundException e)
        {
            return defaultReturn;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return defaultReturn;
        }
    }
}
