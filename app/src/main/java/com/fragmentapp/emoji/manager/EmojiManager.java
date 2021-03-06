package com.fragmentapp.emoji.manager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Xml;

import com.fragmentapp.App;
import com.fragmentapp.emoji.bean.EmojiItem;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.collection.LruCache;

/** 默认表情管理 */
public class EmojiManager {

    private static final float DEF_SCALE = 0.6f;
    private static final float SMALL_SCALE = 0.45F;

    private static final String EMOT_DIR = "emoji/";//asstes路径

    // max cache size
    private static final int CACHE_MAX_SIZE = 1024;

    private static Pattern pattern;

    // default entries
    private static final List<EmojiItem> defaultEntries = new ArrayList<EmojiItem>();
    // text to entry
    private static final Map<String, EmojiItem> text2entry = new HashMap<String, EmojiItem>();
    // asset bitmap cache, key: asset path
    private static LruCache<String, Bitmap> drawableCache;

    static {
        Context context = App.getInstance();
        load(context, EMOT_DIR + "emoji.xml");//加载默认表情

        pattern = makePattern();

        drawableCache = new LruCache<String, Bitmap>(CACHE_MAX_SIZE) {
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (oldValue != newValue)
                    oldValue.recycle();
            }
        };
    }

    //
    // display
    //

    public static final int getDisplayCount() {
        return defaultEntries.size();
    }

    public static final Drawable getDisplayDrawable(Context context, int index) {
        String text = (index >= 0 && index < defaultEntries.size() ?
                defaultEntries.get(index).text : null);
        return text == null ? null : getDrawable(context, text);
    }

    public static final String getDisplayText(int index) {
        return index >= 0 && index < defaultEntries.size() ? defaultEntries
                .get(index).text : null;
    }

    public static final List<EmojiItem> getEmojiList() {
        return defaultEntries;
    }

    public static final Pattern getPattern() {
        return pattern;
    }

    public static final Drawable getDrawable(Context context, String text) {
        EmojiItem entry = text2entry.get(text);
        if (entry == null) {
            return null;
        }

        Bitmap cache = drawableCache.get(entry.assetPath);
        if (cache == null) {
            cache = loadAssetBitmap(context, entry.assetPath);
        }
        return new BitmapDrawable(context.getResources(), cache);
    }

    //
    // internal
    //

    private static Pattern makePattern() {
        return Pattern.compile(patternOfDefault());
    }

    private static String patternOfDefault() {
        return "\\[[^\\[]{1,10}\\]";
    }

    private static Bitmap loadAssetBitmap(Context context, String assetPath) {
        InputStream is = null;
        try {
            Resources resources = context.getResources();
            Options options = new Options();
            options.inDensity = DisplayMetrics.DENSITY_HIGH;
            options.inScreenDensity = resources.getDisplayMetrics().densityDpi;
            options.inTargetDensity = resources.getDisplayMetrics().densityDpi;
            is = context.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(is, new Rect(), options);
            if (bitmap != null) {
                drawableCache.put(assetPath, bitmap);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static final void load(Context context, String xmlPath) {
        new EntryLoader().load(context, xmlPath);
    }

    //
    // load emoticons from asset
    //
    private static class EntryLoader extends DefaultHandler {
        private String catalog = "";

        void load(Context context, String assetPath) {
            text2entry.clear();
            defaultEntries.clear();
            InputStream is = null;
            try {
                is = context.getAssets().open(assetPath);
                Xml.parse(is, Xml.Encoding.UTF_8, this);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        /** 开始解析XML时调用 */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if (localName.equals("Catalog")) {
                catalog = attributes.getValue(uri, "Title");
            } else if (localName.equals("Emoticon")) {
                String tag = attributes.getValue(uri, "Tag");
                String fileName = attributes.getValue(uri, "File");
                EmojiItem entry = new EmojiItem(tag, EMOT_DIR + catalog + "/" + fileName);

                text2entry.put(entry.text, entry);
                if (catalog.equals("default")) {
                    defaultEntries.add(entry);
                }
            }
        }
    }

    public static void replaceEmoticons(Context context, Editable editable, int start, int count) {
        if (count <= 0 || editable.length() < start + count)
            return;

        CharSequence s = editable.subSequence(start, start + count);
        Matcher matcher = EmojiManager.getPattern().matcher(s);
        while (matcher.find()) {
            int from = start + matcher.start();
            int to = start + matcher.end();
            String emot = editable.subSequence(from, to).toString();
            Drawable d = getEmotDrawable(context, emot, SMALL_SCALE);
            if (d != null) {
                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                editable.setSpan(span, from, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static Drawable getEmotDrawable(Context context, String text, float scale) {
        Drawable drawable = EmojiManager.getDrawable(context, text);

        // scale
        if (drawable != null) {
            int width = (int) (drawable.getIntrinsicWidth() * scale);
            int height = (int) (drawable.getIntrinsicHeight() * scale);
            drawable.setBounds(0, 0, width, height);
        }

        return drawable;
    }

}
