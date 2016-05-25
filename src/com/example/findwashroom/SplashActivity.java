package com.example.findwashroom;

import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashAdListener;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.yhtye.findwashroom.R;
import com.yhtye.gongjiao.tools.NetUtil;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 启动页面
 *
 */
public class SplashActivity extends BaseActivity {

    private final static Handler mHandler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 网络检查
        if (!NetUtil.checkNet(this)) {
            setContentView(R.layout.activity_splash);
            Toast.makeText(this, R.string.network_tip, Toast.LENGTH_LONG).show();
            mHandler.postDelayed(new splashhandler(), 800);
        } else {
            setContentView(R.layout.activity_ads_splash);
            // 加载广告
            loadAds();
            TextView timeView = (TextView) findViewById(R.id.splash_title);
            // 倒计时
            new TimeCountDownTask(timeView, 3).execute();
        }
        
        // 日志传输过程采用加密模式
        AnalyticsConfig.enableEncrypt(true);
    }
    
    /**
     * 加载广告
     */
    private void loadAds() {
        RelativeLayout adsParent = (RelativeLayout) this.findViewById(R.id.adsRl);
        // the observer of AD
        SplashAdListener listener = new SplashAdListener() {
            @Override
            public void onAdDismissed() {
                jumpWhenCanClick(); //  跳转至您的应用主界面
            }

            @Override
            public void onAdFailed(String arg0) {
                jump();
            }

            @Override
            public void onAdPresent() {
            }

            @Override
            public void onAdClick() {
            }
        };
        String adPlaceId = "2553624"; 
        new SplashAd(this, adsParent, listener, adPlaceId, true);
    }
    
    /**
     * 当设置开屏可点击时，需要等待跳转页面关闭后，再切换至您的主窗口。故此时需要增加canJumpImmediately判断。 
     * 另外，点击开屏还需要在onResume中调用jumpWhenCanClick接口。
     */
    public boolean canJumpImmediately = false;

    private void jumpWhenCanClick() {
        if (canJumpImmediately) {
            this.startActivity(new Intent(SplashActivity.this, MainActivity.class));
            this.finish();
        } else {
            canJumpImmediately = true;
        }
    }
    
    /**
     * 不可点击的开屏，使用该jump方法，而不是用jumpWhenCanClick
     */
    private void jump() {
        this.startActivity(new Intent(SplashActivity.this, MainActivity.class));
        this.finish();
    }
    
    /**
     * 跳过广告
     * 
     * @param v
     */
    public void skipAdsClick(View v) {
        jump();
    }
    
    /**
     *  倒计时
     */
    private class TimeCountDownTask extends AsyncTask<Void, Void, Boolean> {
        TextView timeView;
        int limit_time = 0;

        TimeCountDownTask(TextView timeView, int time) {
            this.timeView = timeView;
            this.limit_time = time;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            while (limit_time > 0) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        timeView.setText(limit_time + "秒 | 跳过");
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                limit_time--;
            }
            jump();
            return null;
        }
    }
    
    private class splashhandler implements Runnable {
        public void run() {
            // 销毁当前Activity，切换到主页面
            Intent intent=new Intent();  
            intent.setClass(SplashActivity.this, MainActivity.class);  
            startActivity(intent);
            SplashActivity.this.finish();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        
        if (canJumpImmediately) {
            jumpWhenCanClick();
        }
        canJumpImmediately = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        
        canJumpImmediately = false;
    }
}
