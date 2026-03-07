package org.firstinspires.ftc.teamcode;

import android.annotation.SuppressLint;
import android.util.Base64;

import com.qualcomm.robotcore.util.ReadWriteFile;

import org.json.JSONArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.zip.GZIPInputStream;

/**
 * Quanomous

 * Open-source loader + storage system for Quanomous generated autos.

 * Pipeline:
 *   Quanomous Generator
 *      ↓
 *   Base64 + GZIP encoded string
 *      ↓
 *   process()
 *      ↓
 *   Decode → Parse → Hash → Save → Return filename

 * Teams can then:
 *   QuanomousCommand.execute(Quanomous.load(name))
 */
@SuppressLint("SdCardPath")
public class Quanomous {

    /** Base directory for autos */
    private static final String QUANOMOUS_DIR = "/sdcard/FIRST/quanomous/";

    /** Last auto hash (prevents duplicate autos) */
    private static volatile String lastHash = null;

    /** Last saved filename */
    private static volatile String lastName = null;

    /** Current selected auto */
    private static volatile String currentAuto = null;

    /**
     * Process encoded Quanomous data
     *
     * @param data Base64 + GZIP encoded auto
     * @return filename of saved auto
     */
    public static synchronized String process(String data) {
        String jsonText = decode(data);
        JSONArray json = parse(jsonText);

        String canonical = json.toString();
        String nextHash = hash(canonical);

        if (nextHash.equals(lastHash)) {
            return lastName;
        }

        String nextName = getFilename();

        save(nextName, json);

        lastHash = nextHash;
        lastName = nextName;
        currentAuto = nextName;

        return nextName;
    }

    /**
     * Decode Base64 + GZIP data
     */
    public static String decode(String data) {
        try {
            byte[] bytes = Base64.decode(data, Base64.NO_WRAP);

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            try (GZIPInputStream gis = new GZIPInputStream(bais);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                byte[] buf = new byte[8192];

                for (int r; (r = gis.read(buf)) != -1; ) {
                    baos.write(buf, 0, r);
                }

                return baos.toString(StandardCharsets.UTF_8.name());
            }

        } catch (Exception e) {
            throw new RuntimeException("Quanomous decode failed", e);
        }
    }

    /**
     * Parse JSON text
     */
    public static JSONArray parse(String jsonText) {
        try {
            return new JSONArray(jsonText);
        } catch (Exception e) {
            throw new RuntimeException("Quanomous parse failed", e);
        }
    }

    /**
     * Save auto to disk
     */
    public static void save(String name, JSONArray jsonArray) {
        try {
            String json = jsonArray.toString(2);

            File file = new File(QUANOMOUS_DIR + name);

            ReadWriteFile.writeFile(file, json);

        } catch (Exception e) {
            throw new RuntimeException("Quanomous save failed", e);
        }
    }

    /**
     * Load auto from disk
     */
    public static JSONArray load(String name) {
        try {
            currentAuto = name;

            File file = new File(QUANOMOUS_DIR + name);

            String jsonText = ReadWriteFile.readFile(file);

            return new JSONArray(jsonText);

        } catch (Exception e) {
            throw new RuntimeException("Quanomous load failed", e);
        }
    }

    /**
     * Change autos by cycling
     */
    public static String change(int direction) {
        return direction > 0 ? next() : prev();
    }

    public static String prev() {
        File[] files = getSortedFileList();

        if (files.length == 0) return null;

        int idx = indexOf(currentAuto, files);
        if (idx < 0) idx = files.length;

        return files[(idx - 1 + files.length) % files.length].getName();
    }

    public static String next() {
        File[] files = getSortedFileList();

        if (files.length == 0) return null;

        int idx = indexOf(currentAuto, files);
        if (idx < 0) idx = -1;

        return files[(idx + 1 + files.length) % files.length].getName();
    }

    /** Generate filename */
    @SuppressLint({"SimpleDateFormat", "DefaultLocale"})
    private static String getFilename() {
        File[] files = getSortedFileList();

        String ts = new SimpleDateFormat("MM-dd-HHmm")
            .format(new Date());

        return String.format(
            "%s--%04d.json",
            ts,
            files.length + 1
        );
    }

    private static int indexOf(String name, File[] files) {
        if (name == null) return -1;

        for (int i = 0; i < files.length; i++) {
            if (name.equals(files[i].getName())) return i;
        }

        return -1;
    }

    private static File[] getSortedFileList() {
        File dir = new File(QUANOMOUS_DIR);

        File[] files = dir.listFiles(
            (d, n) -> n.toLowerCase().endsWith(".json")
        );

        if (files == null) return new File[0];

        Arrays.sort(files, Comparator.comparing(File::getName));

        return files;
    }

    /** SHA-256 hash (first 8 chars) */
    private static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] bytes = md.digest(
                text.getBytes(StandardCharsets.UTF_8)
            );

            String hex = toHex(bytes);

            return hex.substring(
                0,
                Math.max(1, Math.min(8, hex.length()))
            ).toLowerCase();

        } catch (Exception e) {
            throw new RuntimeException("Quanomous hash failed", e);
        }
    }

    private static String toHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();

        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;

            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }
}