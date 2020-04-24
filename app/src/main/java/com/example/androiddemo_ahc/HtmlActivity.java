package com.example.androiddemo_ahc;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;

public class HtmlActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_FILE_CHOOSER = 1;
    WebView webView;
    private ValueCallback uploadMessageAboveL;
    private ValueCallback mUploadCallBack;
    private String mCameraFilePath;
    private String param;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html);

        webView=findViewById(R.id.webView);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.clearCache(true);
                finish();
            }
        });

        String hide = getIntent().getStringExtra("HIDE");

        if (hide.equals("hide_title")){//需要隐藏就传入headHidden=1 需要显示title就不传
           param="&headHidden=1";
        }
        if (hide.equals("hide_no")){
            findViewById(R.id.title).setBackgroundColor(getResources().getColor(R.color.black_333));
        }
        String url="https://团队ID.ahc.ink/chat.html?headHidden=1&customer={\"名称\":\"张先生\",\"邮箱\":\"test@test.com\",\"手机\":\"19900000000\"}";

        //这里做一个传递顾客资料的示例（key字段可以根据自己需求，自定义）
        ParamEntity paramEntity=new ParamEntity();
        paramEntity.set名称("张先生");
        paramEntity.set会员账号("test");
        paramEntity.set会员等级("VIP8");
        String s = JsonParseUtils.parseToJson(paramEntity);
//        url="这里需要换成自己工作台的对话链接网址"+"?headHidden=1&customer="+s;
        url="https://10003.ahc.ink/chat.html?"+"&customer="+s+param;
        setWebView(url);
    }

    /**
     *
     * @param url 拼接规则 url="对话链接(例:https://团队ID.ahc.ink/chat.html)?+"headHidden=1( 1则为隐藏title 不传则不隐藏)"+"&传递顾客参数(例如：customer={})"+"&其它参数（可选择 例如：uniqueId=会员唯一ID）"
     */
    private void setWebView(String url) {
        WebSettings webSettings = webView.getSettings();
        //设置WebView属性，能够执行Javascript脚本
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);//开启本地DOM存储
        //设置可以访问文件
        webSettings.setAllowFileAccess(true);
        webView.setWebChromeClient(mWebChromeClient);
        webView.setWebViewClient(mWebViewClient);
        webView.loadUrl(url);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //do you  work
        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //该方法在Build.VERSION_CODES.LOLLIPOP以前有效，从Build.VERSION_CODES.LOLLIPOP起，建议使用shouldOverrideUrlLoading(WebView, WebResourceRequest)} instead
            //返回false，意味着请求过程里，不管有多少次的跳转请求（即新的请求地址），均交给webView自己处理，这也是此方法的默认处理
            //返回true，说明你自己想根据url，做新的跳转，比如在判断url符合条件的情况下，我想让webView加载http://ask.csdn.net/questions/178242

            if (url!=null){
//                view.loadUrl(url);//用webView显示
                //如果想要跳转到系统浏览器
                try {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                }catch (Exception e){}
                return true;
            }

            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request)
        {
            //返回false，意味着请求过程里，不管有多少次的跳转请求（即新的请求地址），均交给webView自己处理，这也是此方法的默认处理
            //返回true，说明你自己想根据url，做新的跳转，比如在判断url符合条件的情况下，我想让webView加载http://ask.csdn.net/questions/178242
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (request.getUrl()!=null){
//                    view.loadUrl(request.getUrl().toString());//用webView显示
                    //如果想要跳转到系统浏览器
                    try {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        intent.setData(request.getUrl());
                        startActivity(intent);
                        return true;
                    }catch (Exception e){}
                    return true;
                }
            }

            return false;
        }


    };
    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            //do you work
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            uploadMessageAboveL = filePathCallback;

            if (hasPermission()) {
                showFileChooser();
            }else {
                clearUploadMessage();
            }
            return true;
        }
    };

    /**
     * 权限申请
     * @return
     */
    public boolean hasPermission(){
        String[] perms = {
                // 把你想要申请的权限放进这里就行，注意用逗号隔开
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };
        boolean flag = EasyPermissionUtils.checkPermission(this, perms);
        if (!flag) {
            EasyPermissionUtils.requireSomePermission(this,"获取手机读写权限",1,perms);
        }
        return flag;
    }

    /**
     * 打开选择文件/相机
     */
    private void showFileChooser() {

        Intent intent1 = new Intent(Intent.ACTION_PICK, null);
        intent1.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*;video/*");
//        Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
//        intent1.addCategory(Intent.CATEGORY_OPENABLE);
//        intent1.setType("image/*;video/*");

        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mCameraFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                System.currentTimeMillis() + ".jpg";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // android7.0注意uri的获取方式改变
            Uri photoOutputUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    new File(mCameraFilePath));
            intent2.putExtra(MediaStore.EXTRA_OUTPUT, photoOutputUri);
        } else {
            intent2.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
        }
//
//        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
//        chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
//        chooser.putExtra(Intent.EXTRA_INTENT, intent1);
//        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent2});
        startActivityForResult(intent1, REQUEST_CODE_FILE_CHOOSER);
    }

    // 获取文件的真实路径
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILE_CHOOSER) {
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (result == null && !TextUtils.isEmpty(mCameraFilePath)) {
                // 看是否从相机返回
                File cameraFile = new File(mCameraFilePath);
                if (cameraFile.exists()) {
                    result = Uri.fromFile(cameraFile);
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                }
            }
            if (result != null) {
                String path = PickUtils.getPath(this, result);
                if (!TextUtils.isEmpty(path)) {
                    File f = new File(path);
                    if (f.exists() && f.isFile()) {
                        Uri newUri = Uri.fromFile(f);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (uploadMessageAboveL != null) {
                                if (newUri != null) {
                                    uploadMessageAboveL.onReceiveValue(new Uri[]{newUri});
                                    uploadMessageAboveL = null;
                                    return;
                                }
                            }
                        } else if (mUploadCallBack != null) {
                            if (newUri != null) {
                                mUploadCallBack.onReceiveValue(newUri);
                                mUploadCallBack = null;
                                return;
                            }
                        }
                    }
                }
            }
            clearUploadMessage();
            return;
        }
    }

    /**
     * webview没有选择文件也要传null，防止下次无法执行
     */
    private void clearUploadMessage() {
        if (uploadMessageAboveL != null) {
            uploadMessageAboveL.onReceiveValue(null);
            uploadMessageAboveL = null;
        }
        if (mUploadCallBack != null) {
            mUploadCallBack.onReceiveValue(null);
            mUploadCallBack = null;
        }
    }


}
