package com.caesar.rongcloudspeed.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import com.caesar.rongcloudspeed.R;
import com.caesar.rongcloudspeed.common.IntentExtra;
import com.caesar.rongcloudspeed.db.model.GroupEntity;
import com.caesar.rongcloudspeed.db.model.UserInfo;
import com.caesar.rongcloudspeed.model.Resource;
import com.caesar.rongcloudspeed.model.Status;
import com.caesar.rongcloudspeed.model.qrcode.QrCodeDisplayType;
import com.caesar.rongcloudspeed.ui.dialog.MorePopWindow;
import com.caesar.rongcloudspeed.ui.dialog.QrcodePopWindow;
import com.caesar.rongcloudspeed.ui.view.SealTitleBar;
import com.caesar.rongcloudspeed.utils.ImageLoaderUtils;
import com.caesar.rongcloudspeed.utils.ToastUtils;
import com.caesar.rongcloudspeed.utils.UserInfoUtils;
import com.caesar.rongcloudspeed.utils.ViewCapture;
import com.caesar.rongcloudspeed.utils.log.SLog;
import com.caesar.rongcloudspeed.viewmodel.DisplayQRCodeViewModel;
import com.caesar.rongcloudspeed.wx.WXManager;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;

import static io.rong.imkit.plugin.image.PictureSelectorActivity.REQUEST_CODE_ASK_PERMISSIONS;

/**
 * 显示二维码界面
 */
public class QrCodeDisplayActivity extends TitleBaseActivity implements View.OnClickListener,QrcodePopWindow.OnPopWindowItemClickListener {
    private final String TAG = "QrCodeDisplayActivity";
    private final int REQUEST_CODE_FORWARD_TO_SEALTALK = 1000;
    /**
     * 分享类型定义：SealTalk
     */
    private final int SHARE_TYPE_SEALTALK = 0;
    /**
     * 分享类型定义：微信
     */
    private final int SHARE_TYPE_WECHAT = 1;
    private QrCodeDisplayType qrType;
    private String targetId;
    private String fromId;
    private SealTitleBar sealTitleBar;

    private LinearLayout qrCodeCardLl;
    private ImageView portraitIv;
    private TextView mainInfoTv;
    private TextView subInfoTv;
    private ImageView qrCodeIv;
    private TextView qrCodeDescribeTv;
    private TextView qrNoCodeDescribeTv;
    private Bitmap qrCodeBitmap;

    private DisplayQRCodeViewModel qrCodeViewModel;
    private int shareType = -1;// 分享类型，用于当保存

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sealTitleBar = getTitleBar();
        Intent intent = getIntent();
        if (intent == null) {
            SLog.d(TAG, "intent can't null, to finish.");
            finish();
            return;
        }

        qrType = (QrCodeDisplayType) intent.getSerializableExtra(IntentExtra.SERIA_QRCODE_DISPLAY_TYPE);
        targetId = intent.getStringExtra(IntentExtra.STR_TARGET_ID);
        fromId = intent.getStringExtra(IntentExtra.START_FROM_ID);

        if (qrType == null || targetId == null) {
            SLog.d(TAG, "qrType and targetId can't null, to finish.");
            finish();
            return;
        }

        setContentView(getContentViewId());

        initView();
        initViewModel();
    }

    public int getContentViewId() {
        return R.layout.profile_activity_show_qrcode;
    }

    private void initView() {
        qrNoCodeDescribeTv = findViewById(R.id.profile_tv_qr_card_info_no_code_describe);
        // 二维码描述
        qrCodeDescribeTv = findViewById(R.id.profile_tv_qr_card_info_describe);
        if (qrType == QrCodeDisplayType.GROUP) {
            sealTitleBar.setTitle(R.string.profile_group_qrcode);
            qrCodeDescribeTv.setText(R.string.profile_qrcode_group_tips);
        } else if (qrType == QrCodeDisplayType.PRIVATE) {
            sealTitleBar.setRightImage(R.drawable.seal_ic_main_more);
            sealTitleBar.setOnBtnRightClickListener(this);
            sealTitleBar.setTitle(R.string.seal_main_mine_qrcode);
            qrCodeDescribeTv.setText(R.string.profile_qrcode_private_tips);
        }
        // 二维码卡片父容器
        qrCodeCardLl = findViewById(R.id.profile_fl_card_capture_area_container);
        // 二维码信息所属头像
        portraitIv = findViewById(R.id.profile_iv_card_info_portrait);
        // 二维码信息所属名称
        mainInfoTv = findViewById(R.id.profile_tv_qr_info_main);
        // 二维码信息所属副信息
        subInfoTv = findViewById(R.id.profile_tv_qr_info_sub);
        // 二维码图片
        qrCodeIv = findViewById(R.id.profile_iv_qr_code);
        // 保存图片
        findViewById(R.id.profile_tv_qr_save_phone).setOnClickListener(this);
        // 分享至 SealTalk
        findViewById(R.id.profile_tv_qr_share_to_sealtalk).setOnClickListener(this);
        // 分享至微信
        findViewById(R.id.profile_tv_qr_share_to_wechat).setOnClickListener(this);
    }

    private void initViewModel() {
        qrCodeViewModel = ViewModelProviders.of(this).get(DisplayQRCodeViewModel.class);

        // 获取 QRCode 结果
        qrCodeViewModel.getQRCode().observe(this, resource -> {
            if (resource.data != null) {
                qrCodeBitmap=resource.data;
                qrCodeIv.setImageBitmap(qrCodeBitmap);
            }
        });

        ViewGroup.LayoutParams qrCodeLayoutParams = qrCodeIv.getLayoutParams();

        if (qrType == QrCodeDisplayType.GROUP) {
            // 获取群组信息结果
            qrCodeViewModel.getGroupInfo().observe(this, resource -> {
                if (resource.data != null) {
                    updateGroupInfo(resource.data);
                }
            });
            // 请求群组信息
            qrCodeViewModel.requestGroupInfo(targetId);
            // 获取群组二维码
            qrCodeViewModel.requestGroupQRCode(targetId, fromId, qrCodeLayoutParams.width, qrCodeLayoutParams.height);
        } else if (qrType == QrCodeDisplayType.PRIVATE) {
            // 获取用户信息结果
            qrCodeViewModel.getUserInfo().observe(this, resource -> {
                if (resource.data != null) {
                    updateUserInfo(resource.data);
                }
            });

            // 请求用户信息
            qrCodeViewModel.requestUserInfo(targetId);
            // 获取用户二维码
            qrCodeViewModel.requestUserQRCode(targetId, qrCodeLayoutParams.width, qrCodeLayoutParams.height);
        }

        // 保存图片到本地
        qrCodeViewModel.getSaveLocalBitmapResult().observe(this, resource -> {
            if (resource.status == Status.SUCCESS) {
                // 保存成功后加入媒体扫描中，使相册中可以显示此图片
                MediaScannerConnection.scanFile(QrCodeDisplayActivity.this.getApplicationContext(), new String[]{resource.data}, null, null);

                String msg = QrCodeDisplayActivity.this.getString(R.string.profile_save_picture_at) + ":" + resource.data;
                ToastUtils.showToast(msg, Toast.LENGTH_LONG);
            }
        });

        // 分享至 SealTalk 或 微信
        qrCodeViewModel.getSaveCacheBitmapResult().observe(this, resource -> {
            if (resource.status == Status.SUCCESS) {
                if (shareType == SHARE_TYPE_WECHAT) {
                    shareToWeChat();
                } else if (shareType == SHARE_TYPE_SEALTALK) {
                    shareToSealTalk();
                }
            }
        });
    }

    /**
     * 更新群组相关信息
     *
     * @param groupEntity
     */
    private void updateGroupInfo(GroupEntity groupEntity) {
        ImageLoaderUtils.displayGroupPortraitImage(groupEntity.getPortraitUri(), portraitIv);
        mainInfoTv.setText(groupEntity.getName());
        subInfoTv.setText(getString(R.string.common_member_count, groupEntity.getMemberCount()));
        // 0表示已开启群认证
//        if (groupEntity.getCertiStatus() == 0) {
//            qrCodeIv.setVisibility(View.INVISIBLE);
//            qrCodeDescribeTv.setVisibility(View.INVISIBLE);
//            qrNoCodeDescribeTv.setVisibility(View.VISIBLE);
//        }
    }

    /**
     * 更新用户相关信息
     *
     * @param userInfo
     */
    private void updateUserInfo(UserInfo userInfo) {
        ImageLoaderUtils.displayUserPortraitImage(userInfo.getPortraitUri(), portraitIv);
        mainInfoTv.setText(userInfo.getName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_tv_qr_save_phone:
                saveQRCodeToLocal();
                break;
            case R.id.profile_tv_qr_share_to_sealtalk:
                shareToSealTalk();
                break;
            case R.id.profile_tv_qr_share_to_wechat:
                shareToWeChat();
                break;
            case R.id.btn_right:
                QrcodePopWindow morePopWindow = new QrcodePopWindow(QrCodeDisplayActivity.this, QrCodeDisplayActivity.this);
                morePopWindow.showPopupWindow(v);
                break;
        }
    }

    /**
     * 保存二维码到本地
     */
    private void saveQRCodeToLocal() {
        if (!checkHasStoragePermission()) {
            return;
        }
        qrCodeViewModel.saveQRCodeToLocal(ViewCapture.getViewBitmap(qrCodeCardLl));
    }

    /**
     * 分享至 SealTalk
     */
    private void shareToSealTalk() {
        Resource<String> resource = qrCodeViewModel.getSaveCacheBitmapResult().getValue();
        if (resource != null && resource.data != null) {
            shareType = -1;
            // 跳转到转发
            Uri uri = Uri.fromFile(new File(resource.data));
            ImageMessage imageMessage = ImageMessage.obtain(uri, uri, true);
            // 消息中发送目标需要在转发界面中选择，暂时只填充空消息
            Message message = Message.obtain("", Conversation.ConversationType.NONE, imageMessage);
            Intent intent = new Intent(this, ForwardActivity.class);
            ArrayList<Message> messageList = new ArrayList<>();
            messageList.add(message);
            intent.putParcelableArrayListExtra(IntentExtra.FORWARD_MESSAGE_LIST, messageList);
            intent.putExtra(IntentExtra.BOOLEAN_ENABLE_TOAST, false);
            startActivityForResult(intent, REQUEST_CODE_FORWARD_TO_SEALTALK);
        } else {
            if (!checkHasStoragePermission()) {
                return;
            }
            shareType = SHARE_TYPE_SEALTALK;
            qrCodeViewModel.saveQRCodeToCache(ViewCapture.getViewBitmap(qrCodeCardLl));
        }
    }

    /**
     * 分享至微信
     */
    private void shareToWeChat() {
        Resource<String> resource = qrCodeViewModel.getSaveCacheBitmapResult().getValue();
        if (resource != null && resource.data != null) {
            shareType = -1;
            // 分享至微信
            WXManager.getInstance().sharePicture(resource.data);
        } else {
            if (!checkHasStoragePermission()) {
                return;
            }
            shareType = SHARE_TYPE_WECHAT;
            qrCodeViewModel.saveQRCodeToCache(ViewCapture.getViewBitmap(qrCodeCardLl));
        }
    }

    private boolean checkHasStoragePermission() {
        // 从6.0系统(API 23)开始，访问外置存储需要动态申请权限
        if (Build.VERSION.SDK_INT >= 23) {
            int checkPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FORWARD_TO_SEALTALK) {
            if (resultCode == RESULT_OK) {
                ToastUtils.showToast(R.string.common_share_success);
            } else if (requestCode == RESULT_FIRST_USER) {
                ToastUtils.showToast(R.string.common_share_failed);
            }
        }
    }

    @Override
    public void onStartChartClick() {
        qrCodeDescribeTv.setText(R.string.profile_qrcode_private_tips);
        qrCodeIv.setImageBitmap(qrCodeBitmap);
    }

    @Override
    public void onCreateGroupClick() {
        qrCodeDescribeTv.setText(R.string.profile_qrcode_private_tips1);
        createQRcodeImage("https://a.feigecb.com/?:for".concat(UserInfoUtils.getAppUserId(this)));
    }

    @Override
    public void onAddFriendClick() {
        qrCodeDescribeTv.setText(R.string.profile_qrcode_private_tips2);
        createQRcodeImage("https://a.feigecb.com/?:to".concat(UserInfoUtils.getAppUserId(this)));
    }

    public void createQRcodeImage(String url) {
        ViewGroup.LayoutParams qrCodeLayoutParams = qrCodeIv.getLayoutParams();
        int w = qrCodeLayoutParams.width;
        int h = qrCodeLayoutParams.height;
        try {
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, w, h, hints);
            int[] pixels = new int[w * h];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * w + x] = 0xff000000;
                    } else {
                        pixels[y * w + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            //显示到我们的ImageView上面
            qrCodeIv.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
