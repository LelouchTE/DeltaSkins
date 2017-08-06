/*
 * This file is part of DeltaSkins.
 *
 * DeltaSkins is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DeltaSkins is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeltaSkins.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.DeltaSkins.Shared;

import com.gmail.tracebachi.DeltaSkins.Shared.Exceptions.RateLimitedException;
import com.gmail.tracebachi.DeltaSkins.Shared.Interfaces.IDeltaSkins;
import com.google.common.base.Preconditions;
import com.google.gson.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * GeeItsZee (tracebachi@gmail.com)
 */
public class MojangApi implements AutoCloseable
{
    private static final int CONNECT_TIMEOUT = 7000;
    private static final int READ_TIMEOUT = 7000;
    private static final String NAMES_TO_IDS_ADDR =
        "https://api.mojang.com/profiles/minecraft";
    private static final String SKIN_ADDR =
        "https://sessionserver.mojang.com/session/minecraft/profile/";

    private IDeltaSkins plugin;
    private LinkedHashSet<String> uuidRequests = new LinkedHashSet<>();

    private Gson gson = new GsonBuilder().create();
    private JsonParser parser = new JsonParser();

    private final Object uuidRequestLock = new Object();
    private final Object parserLock = new Object();

    public MojangApi(IDeltaSkins plugin)
    {
        this.plugin = plugin;
    }

    public boolean addUuidRequest(String name)
    {
        Preconditions.checkNotNull(name, "Name was null.");
        Preconditions.checkArgument(!name.isEmpty(), "Name was empty.");

        synchronized(uuidRequestLock)
        {
            return uuidRequests.add(name);
        }
    }

    public boolean removeUuidRequest(String name)
    {
        Preconditions.checkNotNull(name, "Name was null.");
        Preconditions.checkArgument(!name.isEmpty(), "Name was empty.");

        synchronized(uuidRequestLock)
        {
            return uuidRequests.remove(name);
        }
    }

    public int getUuidRequestCount()
    {
        synchronized(uuidRequestLock)
        {
            return uuidRequests.size();
        }
    }

    public JsonArray getUuids() throws IOException, RateLimitedException
    {
        synchronized(uuidRequestLock)
        {
            // If requests are empty, do not make a request
            if(uuidRequests.size() <= 0)
            {
                return new JsonArray();
            }
        }

        String stringPayload = convertUuidRequestsToStringPayload();

        HttpURLConnection httpConn = openNameToIdConnection();
        httpConn.setRequestMethod("POST");
        httpConn.setRequestProperty("Content-Type", "application/json");
        OutputStream outputStream = httpConn.getOutputStream();

        byte[] payload = stringPayload.getBytes(StandardCharsets.UTF_8);
        outputStream.write(payload);
        outputStream.flush();
        outputStream.close();
        plugin.debugApi("[Payload] " + stringPayload);

        if(httpConn.getResponseCode() == 429)
        {
            throw new RateLimitedException();
        }

        StringWriter writer = new StringWriter();
        InputStream inputStream = httpConn.getInputStream();
        IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
        inputStream.close();
        String response = writer.toString();
        writer.close();
        plugin.debugApi("[Response] " + response);

        synchronized(parserLock)
        {
            return parser.parse(response).getAsJsonArray();
        }
    }

    public JsonObject getSkin(String uuid) throws IOException, RateLimitedException
    {
        HttpURLConnection httpConn = openSkinConnection(uuid);

        if(httpConn.getResponseCode() == 429)
        {
            throw new RateLimitedException();
        }

        StringWriter writer = new StringWriter();
        InputStream inputStream = httpConn.getInputStream();
        IOUtils.copy(inputStream, writer, StandardCharsets.UTF_8);
        String response = writer.toString();
        plugin.debugApi("[Response] " + response);

        synchronized(parserLock)
        {
            return parser.parse(response).getAsJsonObject();
        }
    }

    public void close()
    {
        gson = null;
        parser = null;
        uuidRequests = null;
        plugin = null;
    }

    private HttpURLConnection openSkinConnection(String uuid) throws IOException
    {
        URL url = new URL(SKIN_ADDR + uuid + "?unsigned=false");
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        return (HttpURLConnection) connection;
    }

    private HttpURLConnection openNameToIdConnection() throws IOException
    {
        URL url = new URL(NAMES_TO_IDS_ADDR);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        return (HttpURLConnection) connection;
    }

    private String convertUuidRequestsToStringPayload()
    {
        synchronized(uuidRequestLock)
        {
            Iterator<String> iterator = uuidRequests.iterator();
            JsonArray array = new JsonArray();

            for(int i = 0; i < 100 && iterator.hasNext(); ++i)
            {
                String next = iterator.next();

                if(next != null && !next.isEmpty())
                {
                    array.add(new JsonPrimitive(next));
                }

                iterator.remove();
            }

            return gson.toJson(array);
        }
    }
}
