package net.tonyliu.mi3setting;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.os.SystemProperties;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RadioButton mic1 = (RadioButton) findViewById(R.id.Mic1);
        final RadioButton mic2 = (RadioButton) findViewById(R.id.Mic2);
        //开始读取MicMode
        switch (getMicMode()){
            case 1:
                mic1.setChecked(true);
                mic2.setChecked(false);
                break;
            case 2:
                mic2.setChecked(true);
                mic1.setChecked(false);
                break;
        }
        RadioGroup group = (RadioGroup)this.findViewById(R.id.raidoGroup);
        //绑定一个匿名监听器
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                // TODO Auto-generated method stub
                //获取变更后的选中项的ID
                int radioButtonId = arg0.getCheckedRadioButtonId();
                //根据ID获取RadioButton的实例
                RadioButton rb = (RadioButton)MainActivity.this.findViewById(radioButtonId);
                //通过判断对象来设置模式
                if(rb == mic1){
                    setMicMode(1);
                }if (rb == mic2){
                    setMicMode(2);
                }else {
//nothing
                }

            }
        });
        }
    //=======================MicMode=======================

    public int getMicMode() { //获取MicMode值
        switch (SystemProperties.get("persist.audio.vns.mode")){
            case "1":
                return 1;
            case "2":
                return 2;
            default :
                setMicMode(2);
                return 2;
        }
    }

    public void setMicMode(int val) {//更改MicMode值
            SystemProperties.set("persist.audio.vns.mode",String.valueOf(val));
    }
    //=======================MicMode=======================
}

