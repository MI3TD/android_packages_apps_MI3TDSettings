package net.tonyliu.mi3setting;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.widget.Toast;
import android.content.Context;

import java.lang.RuntimeException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class Helper {

    Context context;

    public Helper(Context context) {
        this.context = context;
    }

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
                try (DataOutputStream os = new DataOutputStream(process.getOutputStream())) {
                    os.writeBytes(input);
                    os.flush();
                }
            }

            StringBuilder sb = new StringBuilder();
            try (InputStreamReader isr = new InputStreamReader(process.getInputStream())) {
                try (BufferedReader in = new BufferedReader(isr)) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                }
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
        try (FileReader fr = new FileReader(fileName)) {
            try (BufferedReader reader = new BufferedReader(fr, 512)) {
                return reader.readLine();
            }
        } catch (IOException e) {
            // ignored, not much we can do anyway
            return null;
        }
    }

    public static boolean writeLine(String fileName, String value) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(value.getBytes());
            fos.flush();
        } catch (IOException ignored1) {
            try {
                exec(true, "cat > '" + fileName + "'", value);
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

    public static void showRootFail(Context context) {
        Toast toast = Toast.makeText(context, context.getString(R.string.root_failed), Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void resetBaseband(Context context) {
        if (writeLine("/sys/class/spi_master/spi0/spi0.0/reset", "1")) {
            Toast toast = Toast.makeText(context, context.getString(R.string.baseband_resetting), Toast.LENGTH_LONG);
            toast.show();
        } else {
            showRootFail(context);
        }
    }

    public static boolean hasPermission(Context context, String[] perms) {
        for (String perm : perms) {
            if (!hasPermission(context, perm)) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasPermission(Context context, String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    public void joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(context.getResources().getString(R.string.link_base_key) + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent);
        } catch (Exception e) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(context.getResources().getString(R.string.notice));
            dialog.setMessage(context.getResources().getString(R.string.QQ_not_installed_notice));
            dialog.setCancelable(true);
            dialog.setPositiveButton(context.getResources().getString(R.string.ok), null);
            dialog.show();
        }
    }

}
