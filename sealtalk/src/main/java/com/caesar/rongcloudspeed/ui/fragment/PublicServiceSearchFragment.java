package com.caesar.rongcloudspeed.ui.fragment;

import com.caesar.rongcloudspeed.ui.interfaces.OnPublicServiceClickListener;

public class PublicServiceSearchFragment extends PublicServiceFragment {
    public PublicServiceSearchFragment(OnPublicServiceClickListener listener) {
        super(listener);
    }

    @Override
    protected void onLoad() {
        //不加载本地资源
    }

    public void search(String match) {
        viewModel.searchPublicServices(match);
    }


}
