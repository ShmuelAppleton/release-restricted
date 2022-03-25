package com.releaserestricted.Util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import net.runelite.http.api.RuneLiteAPI;
import net.runelite.http.api.item.ItemPrice;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class Http {

    public static String runeliteversion = "1.8.15.1";

    public static OkHttpClient client = RuneLiteAPI.CLIENT;

    public static Map<Integer, String> getItemNames() {
        Map<Integer, String> itemNames = Collections.emptyMap();
        try {
            ItemPrice[] prices = getItemPrices();
            if (prices != null) {
                ImmutableMap.Builder<Integer, String> map = ImmutableMap.builderWithExpectedSize(prices.length);
                for (ItemPrice price : prices) {
                    map.put(price.getId(), price.getName());
                }
                itemNames = map.build();
            }
        } catch (IOException e) {
            System.out.println("Error getting item names");
        }
        return itemNames;
    }

    public static ItemPrice[] getItemPrices() throws IOException {
        HttpUrl.Builder urlBuilder =
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("api.runelite.net")
                        .addPathSegment(runeliteversion)
                        .addPathSegment("item")
                        .addPathSegment("prices.js");

        HttpUrl url = urlBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            InputStream in = response.body().byteStream();
            return RuneLiteAPI.GSON.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), ItemPrice[].class);
        } catch (JsonParseException ex) {
            throw new IOException(ex);
        }
    }

}
