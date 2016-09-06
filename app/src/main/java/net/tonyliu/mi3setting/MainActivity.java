package net.tonyliu.mi3setting;

import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;

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
                if (Helper.writeLine("/sys/class/spi_master/spi0/spi0.0/reset", "1")) {
                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.baseband_resetting), Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Helper.showRootFail(getApplicationContext());
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
            switch (Helper.get("persist.audio.vns.mode")) {
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
            try {
                switch (val) {
                    case 1:
                        Helper.set("persist.audio.vns.mode", String.valueOf(val));
                        break;
                    case 2:
                        Helper.set("persist.audio.vns.mode", String.valueOf(val));
                        break;
                    default:
                        throw new NumberFormatException();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    //=======================MicMode=======================
}

