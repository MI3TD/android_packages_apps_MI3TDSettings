package net.tonyliu.mi3setting;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textview = (TextView)findViewById(R.id.textView);
        textview.setText("123"+getMicMode());
    }
    //=======================MicMode=======================

    public String getMicMode(){ //获取MicMode值
        return System.getProperty ("persist.audio.vns.mode");
    }
    public String setMicMode(int val){//更改MicMode值
        return System.setProperty("persist.audio.vns.mode", String.valueOf(val));
    }
    public boolean cutoverMicMode(){//切换MicMode值
        try{
            switch (getMicMode()){
                case "1":
                    setMicMode(2);
                    break;
                case "2":
                    setMicMode(1);
                    break;
                default :
                    setMicMode(2);
                    break;
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

}
