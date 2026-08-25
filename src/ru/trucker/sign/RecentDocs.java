package ru.trucker.sign;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Список недавно открытых документов (persistable URI). */
public class RecentDocs {

    public static class Item {
        public String name;
        public String uri;
        public long ts;
    }

    private static final String PREFS = "recent";
    private static final int MAX = 20;

    private RecentDocs() {}

    public static void add(Context c, Uri uri, String name) {
        List<Item> list = list(c);
        String u = uri.toString();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).uri.equals(u)) {
                list.remove(i);
                break;
            }
        }
        Item it = new Item();
        it.name = name == null || name.isEmpty() ? "Документ" : name;
        it.uri = u;
        it.ts = System.currentTimeMillis();
        list.add(0, it);
        while (list.size() > MAX) list.remove(list.size() - 1);
        save(c, list);
    }

    public static void remove(Context c, Uri uri) {
        List<Item> list = list(c);
        String u = uri.toString();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).uri.equals(u)) {
                list.remove(i);
                break;
            }
        }
        save(c, list);
    }

    public static List<Item> list(Context c) {
        List<Item> out = new ArrayList<>();
        String s = c.getSharedPreferences(PREFS, 0).getString("items", "[]");
        try {
            JSONArray arr = new JSONArray(s);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Item it = new Item();
                it.name = o.optString("name", "Документ");
                it.uri = o.optString("uri");
                it.ts = o.optLong("ts");
                if (!it.uri.isEmpty()) out.add(it);
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    private static void save(Context c, List<Item> list) {
        JSONArray arr = new JSONArray();
        try {
            for (Item it : list) {
                JSONObject o = new JSONObject();
                o.put("name", it.name);
                o.put("uri", it.uri);
                o.put("ts", it.ts);
                arr.put(o);
            }
        } catch (Exception ignored) {
        }
        c.getSharedPreferences(PREFS, 0).edit().putString("items", arr.toString()).apply();
    }
}
