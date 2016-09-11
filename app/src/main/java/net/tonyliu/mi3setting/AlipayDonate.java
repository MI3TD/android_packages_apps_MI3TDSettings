package net.tonyliu.mi3setting;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import java.net.URISyntaxException;

public class AlipayDonate extends DialogPreference {
    private final String code_;

    public AlipayDonate(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AlipayDonate, 0, 0);
        try {
            code_ = a.getString(R.styleable.AlipayDonate_code);

            if (code_ == null) {
                throw new IllegalArgumentException("AlipayDonate: error - code is not specified");
            }
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            Context context = getContext();
            try {
                AlipayZeroSdk.startAlipayClient(context, code_);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(context, context.getString(R.string.alipay_donate_fail), Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    // code from https://github.com/fython/AlipayZeroSdk/blob/master/library/src/main/java/moe/feng/alipay/zerosdk/AlipayZeroSdk.java
    // thanks to fython
    static private class AlipayZeroSdk {
        // 旧版支付宝二维码通用 Intent Scheme Url 格式
        private static final String INTENT_URL_FORMAT = "intent://platformapi/startapp?saId=10000007&" +
                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{urlCode}%3F_s" +
                "%3Dweb-other&_t=1472443966571#Intent;" +
                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";

        /**
         * 打开转账窗口
         * 旧版支付宝二维码方法，需要使用 https://fama.alipay.com/qrcode/index.htm 网站生成的二维码
         * 这个方法最好，但在 2016 年 8 月发现新用户可能无法使用
         *
         * @param context  Parent Context
         * @param urlCode  手动解析二维码获得地址中的参数，例如 https://qr.alipay.com/aehvyvf4taua18zo6e 最后那段
         * @return 是否成功调用
         */
        static boolean startAlipayClient(Context context, String urlCode) {
            return startIntentUrl(context, INTENT_URL_FORMAT.replace("{urlCode}", urlCode));
        }

        /**
         * 打开 Intent Scheme Url
         *
         * @param context       Parent Context
         * @param intentFullUrl Intent 跳转地址
         * @return 是否成功调用
         */
        private static boolean startIntentUrl(Context context, String intentFullUrl) {
            try {
                Intent intent = Intent.parseUri(
                        intentFullUrl,
                        Intent.URI_INTENT_SCHEME
                );
                context.startActivity(intent);
                return true;
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
