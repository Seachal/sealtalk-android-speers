package com.caesar.rongcloudspeed.circle.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.blankj.utilcode.util.ActivityUtils;
import com.bumptech.glide.Glide;
import com.caesar.rongcloudspeed.R;
import com.caesar.rongcloudspeed.constants.Constant;
import com.caesar.rongcloudspeed.data.result.CircleHeaderResult;
import com.caesar.rongcloudspeed.data.result.CircleItemResult;
import com.caesar.rongcloudspeed.network.AppNetworkUtils;
import com.caesar.rongcloudspeed.ui.activity.MainDiscoveryActivity;
import com.caesar.rongcloudspeed.ui.activity.SPLessonDetailActivity;
import com.caesar.rongcloudspeed.ui.activity.SectionMessageActivity;
import com.caesar.rongcloudspeed.ui.activity.SeekHelperDetailActivity;
import com.caesar.rongcloudspeed.ui.dialog.LoadingDialog;
import com.caesar.rongcloudspeed.utils.UserInfoUtils;
import com.caesar.rongcloudspeed.data.BaseData;
import com.caesar.rongcloudspeed.network.NetworkCallback;
import com.caesar.rongcloudspeed.network.NetworkUtils;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.caesar.rongcloudspeed.circle.adapter.CircleAdapter;
import com.yiw.circledemo.bean.CircleHeaderItem;
import com.yiw.circledemo.bean.CircleItem;
import com.caesar.rongcloudspeed.circle.contral.CirclePublicCommentController;
import com.yiw.circledemo.listener.SwipeListViewOnScrollListener;
import com.yiw.circledemo.utils.CommonUtils;
import com.yiw.circledemo.utils.DatasUtil;
import com.yiw.circledemo.utils.HeightComputable;
import com.caesar.rongcloudspeed.circle.widgets.MultiImageView;


import java.util.ArrayList;
import java.util.List;

/**
 * @author yiw
 * @ClassName: FriendCircleActivity
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @date 2015-12-28 下午4:21:18
 */
public class FriendCircleActivity extends FragmentActivity implements SwipeRefreshLayout.OnRefreshListener, HeightComputable {

    protected static final String TAG = FriendCircleActivity.class.getSimpleName();
    private ListView mCircleLv;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CircleAdapter mAdapter;
    private LinearLayout mEditTextBody;
    private EditText mEditText;
    private TextView sendTv;
    private ImageView back;
    private ImageView add;
    private LoadingDialog dialog;
    private int mScreenHeight;
    private int mEditTextBodyHeight;
    private CirclePublicCommentController mCirclePublicCommentController;
    private LinearLayout circle_header_lesson;
    private LinearLayout circle_header_demand;
    private LinearLayout circle_header_book;
    private Button circleMoreBtn;
    private TextView urlTipTv1;
    private TextView urlTipTv2;
    private CircleHeaderItem headerItem1;
    private CircleHeaderItem headerItem2;
    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle);
        userid = UserInfoUtils.getAppUserId(this);
        initView();
        loadData();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mRefreshLayout);
        mCircleLv = (ListView) findViewById(R.id.circleLv);
        back = (ImageView) findViewById(R.id.back);
        add = (ImageView) findViewById(R.id.add);
        back.setOnClickListener(view -> {
            finish();
        });
        add.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainDiscoveryActivity.class);
            startActivity(intent);
        });
        mCircleLv.setOnScrollListener(new SwipeListViewOnScrollListener(mSwipeRefreshLayout));
        mCircleLv.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mEditTextBody.getVisibility() == View.VISIBLE) {
                    mEditTextBody.setVisibility(View.GONE);
                    CommonUtils.hideSoftInput(FriendCircleActivity.this, mEditText);
                    return true;
                }
                return false;
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);
        mAdapter = new CircleAdapter(this);
        mAdapter.setOnMultiImageItemClickListener((parent, view, position) -> {
            MultiImageView multiImageView = (MultiImageView) parent;
            ImagePagerActivity.imageSize = new ImageSize(view.getWidth(), view.getHeight());
            Log.d("view.getWidth():", String.valueOf(view.getWidth()) + ",view.getHeight():" + String.valueOf(view.getHeight()));
            ImagePagerActivity.startImagePagerActivity(FriendCircleActivity.this, multiImageView.getImagesList(), position);
        });
        mCircleLv.setAdapter(mAdapter);
        mEditTextBody = (LinearLayout) findViewById(R.id.editTextBodyLl);
        mEditText = (EditText) findViewById(R.id.circleEt);
        sendTv = (TextView) findViewById(R.id.sendTv);

        mCirclePublicCommentController = new CirclePublicCommentController(this, mEditTextBody, mEditText, sendTv);
        mCirclePublicCommentController.setListView(mCircleLv);
        mAdapter.setCirclePublicCommentController(mCirclePublicCommentController);
        setViewTreeObserver();

        circle_header_lesson = (LinearLayout) findViewById(R.id.circle_header_lesson);
        circle_header_demand = (LinearLayout) findViewById(R.id.circle_header_demand);
        circle_header_book = (LinearLayout) findViewById(R.id.circle_header_book);
        circleMoreBtn = (Button) findViewById(R.id.circleMoreBtn);
        urlTipTv1 = (TextView) findViewById(R.id.urlTipTv1);
        urlTipTv2 = (TextView) findViewById(R.id.urlTipTv2);

        circle_header_lesson.setOnClickListener(view -> {
            String lessonID = headerItem1.getObject_id();
            String lessonName = headerItem1.getPost_title();
            String lessonPrice = headerItem1.getPost_price();
            String lessonSmeta = headerItem1.getSmeta();
            String lessonContent = headerItem1.getPost_excerpt();
            String lessonSource = headerItem1.getPost_source();
            Intent intent = new Intent(FriendCircleActivity.this, SPLessonDetailActivity.class);
            intent.putExtra("lesson_id", lessonID);
            intent.putExtra("lesson_name", lessonName);
            intent.putExtra("lesson_price", lessonPrice);
            intent.putExtra("lesson_smeta", lessonSmeta);
            intent.putExtra("lesson_content", lessonContent);
            intent.putExtra("lesson_source", lessonSource);
            startActivity(intent);
        });

        circle_header_demand.setOnClickListener(view -> {
            String seekID = headerItem2.getObject_id();
            String seekTitle = headerItem2.getPost_title();
            String seekPrice = headerItem2.getPost_price();
            String seekContent = headerItem2.getPost_excerpt();
            String seekDate = headerItem2.getPost_date();
            String photos_urls = headerItem2.getPhotos_urls();
            Intent intent = new Intent(FriendCircleActivity.this, SeekHelperDetailActivity.class);
            intent.putExtra("seek_id", seekID);
            intent.putExtra("seek_title", seekTitle);
            intent.putExtra("seek_date", seekDate);
            intent.putExtra("seek_price", seekPrice);
            intent.putExtra("seek_content", seekContent);
            intent.putExtra("photos_urls", photos_urls);
            startActivity(intent);
        });

        circle_header_book.setOnClickListener(view -> {

        });

        circleMoreBtn.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("headerItem1", headerItem1);
            bundle.putSerializable("headerItem2", headerItem2);
            ActivityUtils.startActivity(bundle, FriendCircleActivity.this, SectionMessageActivity.class);
        });
    }


    private void setViewTreeObserver() {

        final ViewTreeObserver swipeRefreshLayoutVTO = mSwipeRefreshLayout.getViewTreeObserver();
        swipeRefreshLayoutVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect r = new Rect();
                mSwipeRefreshLayout.getWindowVisibleDisplayFrame(r);
                int screenH = mSwipeRefreshLayout.getRootView().getHeight();
                int keyH = screenH - (r.bottom - r.top);
                if (keyH == CommonUtils.keyboardHeight) {//有变化时才处理，否则会陷入死循环
                    return;
                }
                Log.d(TAG, "keyH = " + keyH + " &r.bottom=" + r.bottom + " &top=" + r.top);
                CommonUtils.keyboardHeight = keyH;
                mScreenHeight = screenH;//应用屏幕的高度
                mEditTextBodyHeight = mEditTextBody.getHeight();
                if (mCirclePublicCommentController != null) {
                    mCirclePublicCommentController.handleListViewScroll();
                }
            }
        });
    }

    @Override
    public void onRefresh() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);

    }

    private void loadData() {
        showLoadingDialog("");
        List<CircleItem> datas = new ArrayList<>();
        NetworkUtils.fetchInfo(AppNetworkUtils.initRetrofitApi().fetchFriendCircle(userid),
                new NetworkCallback<CircleItemResult>() {
                    @Override
                    public void onSuccess(CircleItemResult circleItemResult) {
                        if (circleItemResult != null && circleItemResult.getReferer() != null) {
                            mAdapter.setDatas(circleItemResult.getReferer().getPosts());
                            mAdapter.notifyDataSetChanged();
                        }
                        dismissLoadingDialog();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        dismissLoadingDialog();
                        Toast.makeText(FriendCircleActivity.this, "网络异常", Toast.LENGTH_LONG).show();
                    }
                });
        NetworkUtils.fetchInfo(AppNetworkUtils.initRetrofitApi().headerFriendCircle(),
                new NetworkCallback<CircleHeaderResult>() {
                    @Override
                    public void onSuccess(CircleHeaderResult circleHeaderResult) {
                        if (circleHeaderResult != null && circleHeaderResult.getReferer() != null) {
                            List<CircleHeaderItem> circleList = circleHeaderResult.getReferer();
                            for (CircleHeaderItem item : circleList) {
                                String termString = item.getTerm_id();
                                if (termString.equals("43")) {
                                    headerItem2 = item;
                                    urlTipTv2.setText(item.getPost_title());
                                } else {
                                    headerItem1 = item;
                                    urlTipTv1.setText(item.getPost_title());
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("Throwable", String.valueOf(t));
                        Toast.makeText(FriendCircleActivity.this, "网络异常", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //移除所有
        handler.removeCallbacksAndMessages(null);
    }

    private long dialogCreateTime;
    private Handler handler = new Handler();

    /**
     * 显示加载 dialog
     *
     * @param msg
     */
    public void showLoadingDialog(String msg) {
        if (dialog == null || (dialog.getDialog() != null && !dialog.getDialog().isShowing())) {
            dialogCreateTime = System.currentTimeMillis();
            dialog = new LoadingDialog();
            dialog.setLoadingInformation(msg);
            dialog.show(getSupportFragmentManager(), "loading_dialog");
        }
    }

    /**
     * 显示加载 dialog
     *
     * @param msgResId
     */
    public void showLoadingDialog(int msgResId) {
        showLoadingDialog(getString(msgResId));
    }

    /**
     * 取消加载dialog
     */
    public void dismissLoadingDialog() {
        dismissLoadingDialog(null);
    }

    /**
     * 取消加载dialog. 因为延迟， 所以要延时完成之后， 再在 runnable 中执行逻辑.
     * <p>
     * 延迟关闭时间是因为接口有时返回太快。
     */
    public void dismissLoadingDialog(Runnable runnable) {
        if (dialog != null && dialog.getDialog() != null && dialog.getDialog().isShowing()) {
            // 由于可能请求接口太快，则导致加载页面一闪问题， 所有再次做判断，
            // 如果时间太快（小于 500ms）， 则会延时 1s，再做关闭。
            if (System.currentTimeMillis() - dialogCreateTime < 500) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (runnable != null) {
                            runnable.run();
                        }
                        dialog.dismiss();
                        dialog = null;
                    }
                }, 1000);

            } else {
                dialog.dismiss();
                dialog = null;
                if (runnable != null) {
                    runnable.run();
                }
            }
        }
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public int getEditTextBodyHeight() {
        return mEditTextBodyHeight;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mEditTextBody != null && mEditTextBody.getVisibility() == View.VISIBLE) {
                mEditTextBody.setVisibility(View.GONE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
