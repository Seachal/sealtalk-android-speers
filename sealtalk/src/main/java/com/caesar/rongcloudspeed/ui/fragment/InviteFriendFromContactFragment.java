package com.caesar.rongcloudspeed.ui.fragment;

import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import com.caesar.rongcloudspeed.model.SimplePhoneContactInfo;
import com.caesar.rongcloudspeed.ui.adapter.CommonListAdapter;
import com.caesar.rongcloudspeed.ui.adapter.ListWithSideBarBaseAdapter;
import com.caesar.rongcloudspeed.ui.adapter.models.ListItemModel;
import com.caesar.rongcloudspeed.viewmodel.CommonListBaseViewModel;
import com.caesar.rongcloudspeed.viewmodel.InviteFriendFromContactViewModel;

public class InviteFriendFromContactFragment extends CommonListBaseFragment {
    private OnContactSelectedListener onContactSelectedListener;
    private InviteFriendFromContactViewModel inviteFriendFromContactViewModel;

    @Override
    protected CommonListBaseViewModel createViewModel() {
        inviteFriendFromContactViewModel = ViewModelProviders.of(this).get(InviteFriendFromContactViewModel.class);
        return inviteFriendFromContactViewModel;
    }

    @Override
    protected boolean isUseSideBar() {
        return true;
    }

    @Override
    protected ListWithSideBarBaseAdapter getAdapter() {
        CommonListAdapter adapter = (CommonListAdapter) super.getAdapter();
        adapter.setOnItemClickListener(new CommonListAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position, ListItemModel data) {
                if (onContactSelectedListener != null) {
                    int selectedSize = adapter.getSelectedOtherIds().size();
                    onContactSelectedListener.OnContactSelected(data, selectedSize);
                }
            }
        });
        return adapter;
    }

    /**
     * 获取当前选择的联系人
     *
     * @return
     */
    public List<SimplePhoneContactInfo> getCheckedContactInfo() {
        List<SimplePhoneContactInfo> result = new ArrayList<>();
        CommonListAdapter listAdapter = (CommonListAdapter) getListAdapter();
        List<ListItemModel> data = listAdapter.getData();
        if (data != null) {
            for (ListItemModel model : data) {
                if (model.getData() instanceof SimplePhoneContactInfo && model.getCheckStatus() == ListItemModel.CheckStatus.CHECKED) {
                    SimplePhoneContactInfo info = (SimplePhoneContactInfo) model.getData();
                    result.add(info);
                }
            }
        }

        return result;
    }

    public interface OnContactSelectedListener {
        void OnContactSelected(ListItemModel changedModel, int totalSelected);
    }

    public void setOnContactSelectedListener(OnContactSelectedListener listener) {
        this.onContactSelectedListener = listener;
    }

    public void search(String keyword) {
        inviteFriendFromContactViewModel.search(keyword);
    }

}
