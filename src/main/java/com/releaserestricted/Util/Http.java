package com.releaserestricted.Util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import net.runelite.client.RuneLite;
import net.runelite.http.api.RuneLiteAPI;
import net.runelite.http.api.item.ItemPrice;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Http {

    private static String runeliteVersion = "runelite-1.8.15.1";
    public static OkHttpClient client = RuneLiteAPI.CLIENT;
    private static Document doc;

    public static CompletableFuture<ReleaseDate> getReleaseDateByName(String name) {
        CompletableFuture<ReleaseDate> future = new CompletableFuture<>();

        String url = getWikiUrl(name);

        requestAsync(url).whenCompleteAsync((responseHTML, ex) -> {
            doc = Jsoup.parse(responseHTML);
            Elements ReleaseDataInfo = doc.select("table.rsw-infobox > tbody > tr:has(th:contains(released)) > td > a");
            String monthPart = ReleaseDataInfo.first().text();
            ReleaseDataInfo.remove(0);
            String yearPart = ReleaseDataInfo.first().text();
            ReleaseDate date = new ReleaseDate(monthPart, yearPart);
            future.complete(date);
        });
        return future;
    }

    public static String sanitizeName(String name) {
        if (name != null) {
            name = name.trim().toLowerCase().replaceAll("\\s+", "_");
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        return name;
    }

    public static String getWikiUrl(String itemOrMonsterName) {
        String sanitizedName = sanitizeName(itemOrMonsterName);
        return "https://oldschool.runescape.wiki/w/" + sanitizedName;
    }

    public static Map<Integer, String> getItemNames() {
        Map<Integer, String> itemNames = Collections.emptyMap();
        try {
            ItemPrice[] prices = getItemPrices();
            if (prices != null) {
                Map<Integer, String> map = new HashMap<>();
                for (ItemPrice price : prices) {
                    map.put(price.getId(), price.getName());
                }
                itemNames = map;
            }
        } catch (IOException e) {
            System.out.println("Error getting item names");
        }
        System.out.println("item names retrieved");
        System.out.println(itemNames.get(526));
        return itemNames;
    }

    public static ItemPrice[] getItemPrices() throws IOException {
        HttpUrl.Builder urlBuilder =
                new HttpUrl.Builder()
                        .scheme("https")
                        .host("api.runelite.net")
                        .addPathSegment(runeliteVersion)
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

    private static CompletableFuture<String> requestAsync(String url) {
        CompletableFuture<String> future = new CompletableFuture<>();

        Request request = new Request.Builder().url(url).header("User-Agent", RuneLite.USER_AGENT + " (release-restricted)").build();

        client
                .newCall(request)
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException ex) {
                                future.completeExceptionally(ex);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try (ResponseBody responseBody = response.body()) {
                                    if (!response.isSuccessful()) future.complete("");

                                    future.complete(responseBody.string());
                                } finally {
                                    response.close();
                                }
                            }
                        });

        return future;
    }

}
