package net.tonyliu.mi3setting;

import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.DataOutputStream;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RadioButton mic1 = (RadioButton) findViewById(R.id.Mic1);
        final RadioButton mic2 = (RadioButton) findViewById(R.id.Mic2);
        final Button reBaseband = (Button) findViewById(R.id.button);
        switch (MicMode.get()) {
            case 1:
                mic1.setChecked(true);
                break;
            case 2:
                mic2.setChecked(true);
                break;
        }
        RadioGroup group = (RadioGroup) this.findViewById(R.id.raidoGroup);
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                int radioButtonId = arg0.getCheckedRadioButtonId();

                switch (radioButtonId) {
                case R.id.Mic1:
                    MicMode.set(1);
                    break;

                case R.id.Mic2:
                    MicMode.set(2);
                    break;
                }
            }
        });
        reBaseband.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (runCommand("echo 1 > /sys/class/spi_master/spi0/spi0.0/reset", true)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "正在重启基带...", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "无法获取Root权限，请检查！", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    //=======================MicMode=======================
    /*
    * 获取通话降噪设置
    * 方法 获取设置 MicMode.get()
    * 方法 更改设置 MicMode.set(值) 可取 1、2,否则抛出异常
    */
    public static class MicMode {
        static int get() { //获取MicMode值
            switch (SystemProperties.get("persist.audio.vns.mode")) {
                case "1":
                    return 1;
                case "2":
                    return 2;
                default:
                    set(2);
                    return 2;
            }
        }

        static void set(int val) {//更改MicMode值
            switch (val) {
                case 1:
                    SystemProperties.set("persist.audio.vns.mode", String.valueOf(val));
                    break;
                case 2:
                    SystemProperties.set("persist.audio.vns.mode", String.valueOf(val));
                    break;
                default:
                    throw new NumberFormatException();
            }
            SystemProperties.set("persist.audio.vns.mode", String.valueOf(val));
        }
    }

    //=======================MicMode=======================
    //=======================RunShell======================
    /*
    * 运行Shell命令
    * 方法 reommand((String）Shell命令）
    * 重写 以root权限运行
    * 方法 reommand((String）Shell命令,True）
    */
    public static boolean runCommand(String cmd) { //不提权运行shell指令
        return runCommand(cmd, false);
    }

    public static boolean runCommand(String cmd, boolean su) {//重写 提权运行shell指令
        Process process = null;
        DataOutputStream os = null;
        String ShellMode = null;
        if (su) {
            ShellMode = "su"; //提权
        } else {
            ShellMode = "sh"; //不提权
        }
        try {
            process = Runtime.getRuntime().exec(ShellMode); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception ignored) {
            }
        }
    }
    //=======================RunShell======================
}

