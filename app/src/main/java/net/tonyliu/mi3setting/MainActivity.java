package net.tonyliu.mi3setting;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.os.SystemProperties;
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
        //开始读取MicMode
        switch (getMicMode()){
            case 1:
                mic1.setChecked(true);
                break;
            case 2:
                mic2.setChecked(true);
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
        reBaseband.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {

            if (runCommand("echo 1 > /sys/class/spi_master/spi0/spi0.0/reset", true)==true){
                Toast toast=Toast.makeText(getApplicationContext(),"正在重启基带...",Toast.LENGTH_LONG);
                toast.show();
            }else {
                Toast toast=Toast.makeText(getApplicationContext(),"无法获取Root权限，请检查！",Toast.LENGTH_SHORT);
                toast.show();
            }
            }
        });
    }
    //=======================MicMode=======================
    /*
    * 获取通话降噪设置
    * 方法 获取设置 getMicMode()
    * 方法 更改设置 setMicMode(值) 可取 1、2,否则抛出异常
    */
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
        switch (val){
            case 1:
                SystemProperties.set("persist.audio.vns.mode",String.valueOf(val));
                break;
            case 2:
                SystemProperties.set("persist.audio.vns.mode",String.valueOf(val));
                break;
            default :
            throw new NumberFormatException();
        }
        SystemProperties.set("persist.audio.vns.mode",String.valueOf(val));
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
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec(cmd); //运行shell指令
            return process.waitFor()==0;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
    }
    public static boolean runCommand(String cmd,boolean su) {//重写 提权运行shell指令
        Process process = null;
        DataOutputStream os = null;
        if (su=true ){
            try {
                process = Runtime.getRuntime().exec("su"); //切换到root帐号
                os = new DataOutputStream(process.getOutputStream());
                os.writeBytes(cmd + "\n");
                os.writeBytes("exit\n");
                os.flush();
                return process.waitFor()==0;
            } catch (Exception e) {
                return false;
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                    process.destroy();
                } catch (Exception e) {
                }
            }
        }else{
            return false;
        }
    }
    //=======================RunShell======================
}

