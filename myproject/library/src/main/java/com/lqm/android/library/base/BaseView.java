package com.lqm.android.library.base;

public interface BaseView {
    /*******
     * 内嵌加载
     *******/
    void showLoading(String title);

    void stopLoading();

    void showErrorTip(String msg);
}
