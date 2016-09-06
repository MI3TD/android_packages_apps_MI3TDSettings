package net.tonyliu.mi3setting;

import android.os.FileUtils;
import android.os.SystemProperties;

import java.lang.RuntimeException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class Helper {
    public static String exec(boolean su, String cmdLine) throws IOException {
        return exec(su, cmdLine, null);
    }
    public static String exec(boolean su, String cmdLine, String input) throws IOException {
        String[] cmd = {
                su ? "su" : "sh",
                "-c",
                cmdLine
        };

        Process process = Runtime.getRuntime().exec(cmd);
        try {
            if (input != null) {
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                try {
                    os.writeBytes(input);
                    os.flush();
                } finally {
                    os.close();
                }
            }

            BufferedReader in =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

            try {
                int status = process.waitFor();
                if (status != 0) {
                    throw new IOException(String.valueOf(status));
                }
            } catch (InterruptedException exception) {
                exception.printStackTrace();
                throw new IOException(exception);
            }

            return sb.toString();
        } finally {
            process.destroy();
        }
    }

    public static String readOneLine(String fileName) {
        String line = null;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(fileName), 512);
            line = reader.readLine();
        } catch (IOException e) {
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // ignored, not much we can do anyway
            }
        }

        return line;
    }

    public static boolean writeLine(String fileName, String value) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(value.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException ignored1) {
            try {
                exec(true, "cat > /sys/class/spi_master/spi0/spi0.0/reset", value);
            } catch (IOException ignored2) {
                ignored2.printStackTrace();;
                return false;
            }
        }

        return true;
    }

    public static void set(String name, String value) throws IOException {
        try {
            SystemProperties.set(name, value);
        } catch (RuntimeException e) {
            exec(true, "setprop '" + name + "' '" + value + "'");
        }
    }

    public static String get(String name) {
        return SystemProperties.get(name);
    }

    public static String get(String name, String def) {
        return SystemProperties.get(name, def);
    }

    public static boolean get(String name, boolean def) {
        return SystemProperties.getBoolean(name, def);
    }
}
