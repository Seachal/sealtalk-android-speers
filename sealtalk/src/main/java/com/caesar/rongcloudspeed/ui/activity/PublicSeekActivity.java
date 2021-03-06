package com.caesar.rongcloudspeed.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.allen.library.SuperTextView;
import com.caesar.rongcloudspeed.R;
import com.caesar.rongcloudspeed.bean.BaiduTextBean;
import com.caesar.rongcloudspeed.callback.UpLoadImgCallback;
import com.caesar.rongcloudspeed.common.IntentExtra;
import com.caesar.rongcloudspeed.common.NoScrollGridView;
import com.caesar.rongcloudspeed.data.BaseData;
import com.caesar.rongcloudspeed.network.AppNetworkUtils;
import com.caesar.rongcloudspeed.network.NetworkCallback;
import com.caesar.rongcloudspeed.network.NetworkResultUtils;
import com.caesar.rongcloudspeed.network.NetworkUtils;
import com.caesar.rongcloudspeed.ui.adapter.NinePicturesAdapter;
import com.caesar.rongcloudspeed.util.ToastUitl;
import com.caesar.rongcloudspeed.util.ToastUtils;
import com.caesar.rongcloudspeed.utils.ImageLoaderUtils;
import com.caesar.rongcloudspeed.utils.QiniuUtils;
import com.caesar.rongcloudspeed.utils.UserInfoUtils;
import com.yiw.circledemo.utils.BastiGallery;
import com.yuyh.library.imgsel.ImageLoader;
import com.yuyh.library.imgsel.ImgSelActivity;
import com.yuyh.library.imgsel.ImgSelConfig;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author HIAPAD
 */
public class PublicSeekActivity extends Activity implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "PublicSeekActivity";
    private static final int REQUEST_CODE_SELECT_INDUSTRY = 1001;
    private static final int REQUEST_CODE_SELECT_PROFESSION = 1002;
    private static final int REQUEST_CODE_SELECT_SOFT = 1003;
    private static final int REQUEST_CODE_SELECT_PAY = 1000;
    TextView post_title_text;
    EditText post_edit_title;
    EditText post_edit_price;
    NoScrollGridView post_image_gridview;
    EditText post_edit_mobile;
    EditText post_edit_content;
    Button post_commit_btn;
    SuperTextView post_industy_btn;
    SuperTextView post_profession_btn;
    SuperTextView post_soft_btn;
    private String mParam1;
    private String mParam2;
    private NinePicturesAdapter ninePicturesAdapter;
    private int REQUEST_CODE = 120;
    public static final int RESULT_OK = -1;
    private String postTitle;
    private String postPrice;
    private String phoneNumber;
    private String postContent;
    private String photos_url[] = new String[9];
    private String uidString;
    private String industryIDString;
    private String professionIDString;
    private String softIDString;
    private List<String> pathStringList = new ArrayList<>();
    private String baiduToken;
    private ProgressDialog progressDialog;

    /**
     * 默认是发布出售
     */
    public static int CODE_SUCC = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_goods_detail);
        post_title_text = (TextView) this.findViewById(R.id.post_title_text);
        post_edit_title = (EditText) this.findViewById(R.id.post_edit_title);
        post_edit_price = (EditText) this.findViewById(R.id.post_edit_price);
        post_industy_btn = (SuperTextView) this.findViewById(R.id.post_industy_btn);
        post_profession_btn = (SuperTextView) this.findViewById(R.id.post_profession_btn);
        post_soft_btn = (SuperTextView) this.findViewById(R.id.post_soft_btn);
        post_image_gridview = (NoScrollGridView) this.findViewById(R.id.post_image_gridview);
        post_edit_mobile = (EditText) this.findViewById(R.id.post_edit_mobile);
        post_edit_content = (EditText) this.findViewById(R.id.post_edit_content);
        post_commit_btn = (Button) this.findViewById(R.id.post_commit_btn);
        uidString = UserInfoUtils.getAppUserId(this);
        baiduToken = UserInfoUtils.getBaiduToken(this);
        phoneNumber = UserInfoUtils.getPhone(this);
        ninePicturesAdapter = new NinePicturesAdapter(this, 9, new NinePicturesAdapter.OnClickAddListener() {
            @Override
            public void onClickAdd(int positin) {
                choosePhoto();
            }
        });
        post_image_gridview.setAdapter(ninePicturesAdapter);
        if (uidString.equals("0")) {
            post_edit_mobile.setText("");
        } else {
            post_edit_mobile.setText(phoneNumber);
        }
        post_edit_title.setHint("请输入求助标题");
        post_edit_content.setHint("请输入您求助的真实情况，包括标题，详情等");
        post_title_text.setText("同行求助发布");
        post_commit_btn.setOnClickListener(this);
        post_industy_btn.setOnClickListener(this);
        post_profession_btn.setOnClickListener(this);
        post_soft_btn.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在处理，请稍后...");
    }


    public void postBackAction(View view) {
        finish();
    }


    @Override
    public void onResume() {
        super.onResume();
        uidString = UserInfoUtils.getAppUserId(PublicSeekActivity.this);
        phoneNumber = UserInfoUtils.getPhone(PublicSeekActivity.this);
        if (uidString.equals("0")) {
            post_edit_mobile.setText("");
        } else {
            post_edit_mobile.setText(phoneNumber);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.post_industy_btn:
                startActivityForResult(new Intent(this, IndustrySelectActivity.class), REQUEST_CODE_SELECT_INDUSTRY);
                break;
            case R.id.post_profession_btn:
                startActivityForResult(new Intent(this, ProfessionSelectActivity.class), REQUEST_CODE_SELECT_PROFESSION);
                break;
            case R.id.post_soft_btn:
                startActivityForResult(new Intent(this, SoftSelectActivity.class), REQUEST_CODE_SELECT_SOFT);
                break;
            case R.id.post_commit_btn:
                postTitle = post_edit_title.getText().toString();
                postPrice = post_edit_price.getText().toString();
                phoneNumber = post_edit_mobile.getText().toString();
                postContent = post_edit_content.getText().toString();
                if (uidString.equals("0")) {
                    Intent loginIntent = new Intent(PublicSeekActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                } else {
                    if (TextUtils.isEmpty(postTitle) || postTitle.length() < 5) {
                        ToastUitl.showToastWithImg(getString(R.string.circle_publish_empty), R.drawable.ic_warm);
                    } else {
                        if (TextUtils.isEmpty(industryIDString)) {
                            ToastUitl.showToastWithImg("求助行业不能为空", R.drawable.ic_warm);
                        } else {
                            if (TextUtils.isEmpty(professionIDString)) {
                                ToastUitl.showToastWithImg("求助专业不能为空", R.drawable.ic_warm);
                            } else {
                                if (TextUtils.isEmpty(softIDString)) {
                                    ToastUitl.showToastWithImg("求助工具分类不能为空", R.drawable.ic_warm);
                                } else {
                                    if (TextUtils.isEmpty(phoneNumber)) {
                                        ToastUitl.showToastWithImg(getString(R.string.circle_mobile_empty), R.drawable.ic_warm);
                                    } else {
                                        hideInput();
                                        List<String> imageList = ninePicturesAdapter.getData();
                                        pathStringList = new ArrayList<>();
                                        for (int i = 0; i < imageList.size(); i++) {
                                            String path = imageList.get(i);
                                            if (path.length() > 12) {
                                                pathStringList.add(path);
                                            }
                                        }
                                        if (pathStringList.size() > 0) {
                                            progressDialog.show();
                                            seekHandler.sendEmptyMessage(0);
                                        } else {
                                            ToastUitl.showToastWithImg(getString(R.string.circle_image_empty), R.drawable.ic_warm);
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
                break;
            default:
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler seekHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    NetworkUtils.fetchInfo(AppNetworkUtils.initRetrofitBaiduApi().getBaiduTextCheck(postTitle, baiduToken),
                            new NetworkCallback<BaiduTextBean>() {
                                @Override
                                public void onSuccess(BaiduTextBean baiduTextBean) {
                                    int conclusionType = baiduTextBean.getConclusionType();
                                    if (conclusionType == 1) {
                                        Intent intent = new Intent(PublicSeekActivity.this, SeekHelperOrderActivity.class);
                                        intent.putExtra("seek_name", postTitle);
                                        intent.putExtra("seek_price", "10");
                                        intent.putExtra("seek_type", "3");
                                        intent.putExtra("industryIDString", industryIDString);
                                        intent.putExtra("professionIDString", professionIDString);
                                        intent.putExtra("softIDString", softIDString);
                                        startActivityForResult(intent, REQUEST_CODE_SELECT_PAY);
                                    } else {
                                        progressDialog.dismiss();
                                        BaiduTextBean.BaiduTextData baiduTextData = baiduTextBean.getData().get(0);
                                        ToastUitl.showToastWithImg(baiduTextData.getMsg(), R.drawable.ic_warm);
                                    }
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    progressDialog.dismiss();
                                    Toast.makeText(PublicSeekActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                    break;
                case 1:
                    if (TextUtils.isEmpty(postPrice)) {
                        postPrice = "0.00";
                    }
                    NetworkUtils.fetchInfo(AppNetworkUtils.initRetrofitApi().AddPostCircleArticle(uidString, "43", postTitle, postPrice, phoneNumber, photos_url, postContent, industryIDString, professionIDString, softIDString),
                            new NetworkCallback<BaseData>() {
                                @Override
                                public void onSuccess(BaseData baseData) {
                                    if (NetworkResultUtils.isSuccess(baseData)) {
                                        post_edit_title.setText("");
                                        post_edit_content.setText("");
                                        Toast.makeText(PublicSeekActivity.this, "信息发布成功", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(PublicSeekActivity.this, "信息发布失败，请稍后再试", Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();
                                    setResult(RESULT_OK, getIntent());
                                    finish();
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    Toast.makeText(PublicSeekActivity.this, "信息发布失败，请稍后再试", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });

                    break;
                default:
                    break;
            }
        }
    };

    private void upLoadAllFeedBackImg(List<String> pathList) {
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d(TAG, "EMClient.getInstance().onCancel");
            }
        });
        progressDialog.setMessage("正在上传");
        progressDialog.show();
        QiniuUtils.getUploadManagerInstance();
        //已经上传过的不上传
        photos_url = new String[pathList.size()];
        for (int i = 0; i < pathList.size(); i++) {
            String fileName = pathList.get(i);
            Bitmap bitmap = BastiGallery.getimage(fileName);
            //bitmap=drawCenterLable(PublicGoodsActivity.this,bitmap,"同行快线");
            //byte[] img = BastiGallery.Bitmap2Bytes( bitmap );
            byte[] img = compressBitmap(bitmap, 128);
            int finalI = i;
            QiniuUtils.uploadImg(PublicSeekActivity.this, img, QiniuUtils.createImageKey(phoneNumber), new UpLoadImgCallback() {
                @Override
                public void onSuccess(String imgUrl) {
                    photos_url[finalI] = imgUrl;
                    Log.e("111111111111", "imgUrl = " + imgUrl);
                    if (finalI == pathList.size()-1) {
                        progressDialog.dismiss();
                        seekHandler.sendEmptyMessage(1);
                    }
                }

                @Override
                public void onFailure() {
                    ToastUtils.showShortToast("第" + finalI + "图片上传失败，请重试");
                }
            });
        }
    }


    public static byte[] compressBitmap(Bitmap bitmap, float size) {
        if (bitmap == null || BastiGallery.Bitmap2Bytes(bitmap).length <= size * 1024) {
            return null;//如果图片本身的大小已经小于这个大小了，就没必要进行压缩
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);//如果签名是png的话，则不管quality是多少，都不会进行质量的压缩
        while (baos.toByteArray().length / 1024f > size) {
            quality = quality - 4;// 每次都减少4
            baos.reset();// 重置baos即清空baos
            if (quality <= 0) {
                break;
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            //Log.e(TAG,"------质量--------"+baos.toByteArray().length/1024f);
        }
        return baos.toByteArray();
    }

    /*
   添加全屏斜着45度的文字
   */
    public static Bitmap drawCenterLable(Context context, Bitmap bmp, String text) {
        float scale = context.getResources().getDisplayMetrics().density;
        //创建一样大小的图片
        Bitmap newBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        //创建画布
        Canvas canvas = new Canvas(newBmp);
        canvas.drawBitmap(bmp, 0, 0, null);  //绘制原始图片
        canvas.save();
        canvas.rotate(45); //顺时针转45度
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.argb(50, 255, 255, 255)); //白色半透明
        paint.setTextSize(100 * scale);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        Rect rectText = new Rect();  //得到text占用宽高， 单位：像素
        paint.getTextBounds(text, 0, text.length(), rectText);
        double beginX = (bmp.getHeight() / 2 - rectText.width() / 2) * 1.4;  //45度角度值是1.414
        double beginY = (bmp.getWidth() / 2 - rectText.width() / 2) * 1.4;
        canvas.drawText(text, (int) beginX, (int) beginY, paint);
        canvas.restore();
        return newBmp;
    }

    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    /**
     * 开启图片选择器
     */
    private void choosePhoto() {
        ImgSelConfig config = new ImgSelConfig.Builder(loader)
                // 是否多选
                .multiSelect(true)
                // 确定按钮背景色
                .btnBgColor(Color.TRANSPARENT)
                .titleBgColor(ContextCompat.getColor(PublicSeekActivity.this, R.color.colorAccent))
                // 使用沉浸式状态栏
                .statusBarColor(ContextCompat.getColor(PublicSeekActivity.this, R.color.colorAccent))
                // 返回图标ResId
                .backResId(R.drawable.ic_arrow_back)
                .title("图片")
                // 第一个是否显示相机
                .needCamera(true)
                // 最大选择图片数量
                .maxNum(9 - ninePicturesAdapter.getPhotoCount())
                .build();
        ImgSelActivity.startActivity(this, config, REQUEST_CODE);
    }

    private ImageLoader loader = new ImageLoader() {
        @Override
        public void displayImage(Context context, String path, ImageView imageView) {
            ImageLoaderUtils.display(context, imageView, path);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE) {
                List<String> resultList = data.getStringArrayListExtra(ImgSelActivity.INTENT_RESULT);
                if (ninePicturesAdapter != null) {
                    ninePicturesAdapter.addAll(resultList);
                }
            } else if (requestCode == REQUEST_CODE_SELECT_INDUSTRY) {
                industryIDString = data.getStringExtra("industryIDString");
                post_industy_btn.setRightString(data.getStringExtra("industryNameString"));
            } else if (requestCode == REQUEST_CODE_SELECT_PROFESSION) {
                professionIDString = data.getStringExtra("professionIDString");
                post_profession_btn.setRightString(data.getStringExtra("professionNameString"));
            } else if (requestCode == REQUEST_CODE_SELECT_SOFT) {
                softIDString = data.getStringExtra("softIDString");
                post_soft_btn.setRightString(data.getStringExtra("softNameString"));
            } else if (requestCode == REQUEST_CODE_SELECT_PAY) {
                if (pathStringList.size() > 0) {
                    upLoadAllFeedBackImg(pathStringList);
                }

            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
