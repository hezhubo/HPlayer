package com.hezb.hplayer.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hezb.hplayer.R;
import com.hezb.hplayer.base.BaseFragment;
import com.hezb.hplayer.ui.activity.PlayerActivity;
import com.hezb.hplayer.ui.view.LongPressTextView;
import com.hezb.hplayer.util.FileManager;

/**
 * 显示HTML页面  TODO DEBUG
 * Created by hezb on 2015/12/17.
 */
public class WebViewFragment extends BaseFragment implements View.OnClickListener {

    private final String SEARCH_BAIDU = "http://www.baidu.com/s?wd=";
    private final String HOME_URL = "http://www.baidu.com/";
    private final String ERROR_URL = "file:///android_asset/www/404.html";

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private WebSettings webSettings;

    private View mShowLayout;
    private TextView mShowAddress;
    private ImageView mRefresh;
    private View mEditLayout;
    private EditText mAddressEdit;
    private TextView mAddressConfirm;
    private View mGoBack;
    private View mGoForward;
    private View mGoHome;
    private View mShowSource;
    private View mSourceLayout;
    private LongPressTextView mSourceText;
    private View mSourceClose;

    private InputMethodManager imm;

    private String currentUrl;

    private boolean needSearch = true;// 是否需要搜索引擎搜索

    private final int SHOW_HTML_SOURCE = 1;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_HTML_SOURCE:
                    mSourceText.setText((String) msg.obj);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_webview;
    }

    @Override
    protected void initAllMember() {
        currentUrl = HOME_URL;

        mFindViewById();

        initWebView();

        initOther();
    }

    private void mFindViewById() {
        mProgressBar = (ProgressBar) findViewById(R.id.web_progressbar);
        mWebView = (WebView) findViewById(R.id.webview);
        mShowLayout = findViewById(R.id.show_layout);
        mShowAddress = (TextView) findViewById(R.id.show_address);
        mRefresh = (ImageView) findViewById(R.id.refresh);
        mEditLayout = findViewById(R.id.edit_layout);
        mAddressEdit = (EditText) findViewById(R.id.edit_address);
        mAddressConfirm = (TextView) findViewById(R.id.address_confirm);
        mGoBack = findViewById(R.id.go_back);
        mGoForward = findViewById(R.id.go_forward);
        mGoHome = findViewById(R.id.go_home);
        mShowSource = findViewById(R.id.show_source);
        mSourceLayout = findViewById(R.id.html_source_layout);
        mSourceText = (LongPressTextView) findViewById(R.id.html_source);
        mSourceClose = findViewById(R.id.close_source);
    }

    private void initWebView() {
        mWebView.setWebChromeClient(new VideoWebChromeClient());
        webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAppCacheEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(true);// 支持缩放
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);// 取消显示缩放按钮
        webSettings.setSupportMultipleWindows(true);// 支持多窗口

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.equals(ERROR_URL)) {
                    return;
                }
                mShowAddress.setText(view.getTitle());
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.equals(ERROR_URL)) {
                    return;
                }
                currentUrl = url;
                mShowAddress.setText(url);
                if (FileManager.isVideoPath(url) || isVideoUrl(url)) {
                    goPlayer(url);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 可拦截所以页面内跳转
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                mShowAddress.setText(view.getTitle());
                view.loadUrl(ERROR_URL);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                mShowAddress.setText(view.getTitle());
                super.onReceivedHttpError(view, request, errorResponse);
                view.loadUrl(ERROR_URL);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                mShowAddress.setText(view.getTitle());
                // 拦截错误页面
                view.loadUrl(ERROR_URL);
            }

        });
        mWebView.addJavascriptInterface(new JavaScriptInterface() {

            @Override
            @JavascriptInterface
            public void showSource(String html) {
                Message message = new Message();
                message.what = SHOW_HTML_SOURCE;
                message.obj = html;
                mHandler.sendMessage(message);
            }
        }, "HPlayerJs");

        mWebView.loadUrl(currentUrl);
    }

    /**
     * 初始化其他组件
     */
    private void initOther() {
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        mShowAddress.setOnClickListener(this);
        mRefresh.setOnClickListener(this);
        mAddressConfirm.setOnClickListener(this);
        mGoBack.setOnClickListener(this);
        mGoForward.setOnClickListener(this);
        mGoHome.setOnClickListener(this);
        mShowSource.setOnClickListener(this);
        mSourceClose.setOnClickListener(this);

        mAddressEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (EditorInfo.IME_ACTION_DONE == actionId
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode())) {
                    doChangeUrl(v.getText().toString());
                }
                return false;
            }
        });
        mAddressEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 3) {
                    int lastPoint = s.toString().lastIndexOf(".");// 最后一个点的位置
                    if (lastPoint != -1 && lastPoint != 0
                            && lastPoint + 2 < s.length()) {// 不在第一位，且后面带2个以上字符
                        mAddressConfirm.setText(R.string.enter);
                        needSearch = false;
                        return;
                    }
                }
                mAddressConfirm.setText(R.string.search);
                needSearch = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 搜索/进入
     */
    private void doChangeUrl(String tempUrl) {
        if (!tempUrl.isEmpty() && !tempUrl.equals(currentUrl)) {
            if (needSearch) {
                currentUrl = SEARCH_BAIDU + tempUrl;
            } else {
                if (tempUrl.lastIndexOf("http://") != -1 ||
                        tempUrl.lastIndexOf("https://") != -1 ||
                        tempUrl.lastIndexOf("rtsp://") != -1 ||
                        tempUrl.lastIndexOf("rtmp://") != -1 ||
                        tempUrl.lastIndexOf("rtp://") != -1 ||
                        tempUrl.lastIndexOf("file://") != -1) {
                    currentUrl = tempUrl;
                } else {
                    currentUrl = "http://" + tempUrl;
                }
            }

            mWebView.loadUrl(currentUrl);
        }
        mShowLayout.setVisibility(View.VISIBLE);
        mEditLayout.setVisibility(View.GONE);
        imm.hideSoftInputFromWindow(mAddressEdit.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 去播放
     */
    private void goPlayer(String playUrl) {
        Intent intent = new Intent(mContext, PlayerActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(playUrl));
        startActivity(intent);
    }

    /**
     * @return 是否编辑状态
     */
    public boolean isEditVisible() {
        if (mEditLayout != null && mEditLayout.getVisibility() == View.VISIBLE) {
            mEditLayout.setVisibility(View.GONE);
            mShowLayout.setVisibility(View.VISIBLE);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 展示网页源码
     */
    public void showHtmlSource() {
        mSourceText.setText(R.string.loading_html_source);
        mSourceLayout.setVisibility(View.VISIBLE);
        mWebView.loadUrl("javascript:window.HPlayerJs.showSource(" +
                "document.getElementsByTagName('html')[0].innerHTML);");
    }

    private boolean isVideoUrl(String url) {
        if (url.startsWith("rtsp://")) {
            return true;
        }
        if (url.startsWith("rtmp://")) {
            return true;
        }
        if (url.startsWith("rmp://")) {
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        mWebView.stopLoading();
        mWebView.destroyDrawingCache();
        mWebView.removeAllViews();
        mWebView.setVisibility(View.GONE);
        mWebView.destroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_address:
                mEditLayout.setVisibility(View.VISIBLE);
                mShowLayout.setVisibility(View.GONE);
                needSearch = false;
                mAddressEdit.setText(currentUrl);
                mAddressEdit.requestFocus();
                imm.showSoftInput(mAddressEdit, 0);
                break;
            case R.id.refresh:
                if (ERROR_URL.equals(mWebView.getUrl()) || mWebView.getUrl() == null) {
                    mWebView.loadUrl(currentUrl);
                } else {
                    mWebView.reload();
                }
                break;
            case R.id.address_confirm:
                doChangeUrl(mAddressEdit.getText().toString());
                break;
            case R.id.go_back:
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                }
                break;
            case R.id.go_forward:
                if (mWebView.canGoForward()) {
                    mWebView.goForward();
                }
                break;
            case R.id.go_home:
                mWebView.loadUrl(HOME_URL);
                break;
            case R.id.show_source:
                showHtmlSource();
                break;
            case R.id.close_source:
                mSourceLayout.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }

    class VideoWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mProgressBar.setProgress(newProgress);
            if (newProgress != 100) {
                mProgressBar.setVisibility(View.VISIBLE);
            } else {
                mProgressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            callback.onCustomViewHidden();// 没有全屏
        }

    }

    interface JavaScriptInterface {

        @JavascriptInterface
        void showSource(String html);

    }

}
