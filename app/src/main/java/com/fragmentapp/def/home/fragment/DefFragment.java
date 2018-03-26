package com.fragmentapp.def.home.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.fragmentapp.R;
import com.fragmentapp.base.LazyFragment;
import com.fragmentapp.def.home.adapter.ArticleAdapter;
import com.fragmentapp.def.home.bean.ArticleDataBean;
import com.fragmentapp.def.home.imple.IArticleView;
import com.fragmentapp.def.home.presenter.ArticlePresenter;
import com.fragmentapp.helper.DensityUtil;
import com.fragmentapp.helper.EmptyLayout;
import com.fragmentapp.view.powerful.PowerGroupListener;
import com.fragmentapp.view.powerful.PowerfulStickyDecoration;
import com.fragmentapp.view.refresh.DefFootView;
import com.fragmentapp.view.refresh.DefHeaderView;
import com.fragmentapp.view.refresh.DownHeadView;
import com.fragmentapp.view.refresh.RefreshLayout;
import com.fragmentapp.view.refresh.StickyHeadView;
import com.fragmentapp.view.refresh.SunHeadView;
import com.fragmentapp.view.refresh.TextHeadView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by liuzhen on 2017/11/8.
 */

public class DefFragment extends LazyFragment implements IArticleView {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    RefreshLayout refreshLayout;
    private TextHeadView textHeadView;
    private DownHeadView downHeadView;//扇形头部
    private StickyHeadView stickyHeadView;//粘性头部
    private SunHeadView sunHeadView;
    private ArticleAdapter adapter;

    private ArticlePresenter presenter;
    private int page = 1,lastPage = -1;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_def;
    }

    @Override
    protected void init() {
        presenter = new ArticlePresenter(this);
        page = 1;
        presenter.getArticleList(page);

        adapter = new ArticleAdapter(R.layout.item_def,getContext());
        adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        final int height = DensityUtil.dp2px(getContext(),180);
        PowerfulStickyDecoration decoration = PowerfulStickyDecoration.Builder.init(new PowerGroupListener() {
            @Override
            public String getGroupName(int position) {
                return adapter.getItemText(position);
            }

            @Override
            public View getGroupView(int position) {
                View view = getLayoutInflater().inflate(R.layout.item_recyclerview_over_group, null, false);
                TextView tv_name = view.findViewById(R.id.tv_name);
                tv_name.setText(adapter.getItemText(position));
                return view;
            }

            @Override
            public int getHeadViewHeight() {
                return height;
            }
        }).setGroupHeight(DensityUtil.dp2px(getActivity(), 34))
        .build();
        recyclerView.addItemDecoration(decoration);

        textHeadView = new TextHeadView(getContext());
        downHeadView = new DownHeadView(getContext());
        stickyHeadView = new StickyHeadView(getContext());
        sunHeadView = new SunHeadView(getContext());
        refreshLayout
                .setHeaderView(downHeadView)
                .setHeaderView(textHeadView)
                .setHeaderView(new DefHeaderView(getContext()))
                .setHeaderView(stickyHeadView)
                .setHeaderView(sunHeadView)
                .setFootView(new DefFootView(getContext()))
                .setCallBack(new RefreshLayout.CallBack() {
                    @Override
                    public void refreshHeaderView(int state, String stateVal) {
                        textHeadView.setText(stateVal);
                        switch (state) {
                            case RefreshLayout.DOWN_REFRESH: // 下拉刷新状态
                                break;
                            case RefreshLayout.RELEASE_REFRESH: // 松开刷新状态
                                break;
                            case RefreshLayout.LOADING: // 正在刷新中状态
                                if (refreshLayout.isBottom() == false) {
                                    page = 1;
                                    lastPage = -1;
                                }
                                presenter.getArticleList(page);
                                sunHeadView.upAnim();
                                break;
                        }
                    }

                    @Override
                    public void pullListener(int y) {
                        int pullHeight = y / 2;
                        downHeadView.setPull_height(pullHeight);
                        stickyHeadView.move(pullHeight);
                    }
                });

        emptyLayout.setCallBack(new EmptyLayout.CallBack() {
            @Override
            public void click() {
                presenter.getArticleList(page);
            }
        });

        View headView = getLayoutInflater().inflate(R.layout.item_def_head,null);
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        headView.setLayoutParams(params);
        adapter.addHeaderView(headView);
    }

    @OnClick({R.id.search})
    public void search(View view) {
        new SearchFragment().show(getFragmentManager(), TAG);
    }

    @Override
    public void success(List<ArticleDataBean.ListBean> list) {
        if (list.size() == 0){
            emptyLayout.showEmpty((ViewGroup) getView(),"empty");
        }else {
            page++;//如果有数据则+1下一页
            if (lastPage != page) {
                if (refreshLayout.isBottom())
                    adapter.addData(list);
                else
                    adapter.setNewData(list);
            }
            lastPage = page;
//            Toast.makeText(getActivity(), "success"+adapter.getItemCount(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void error() {
        emptyLayout.showEmpty((ViewGroup) getView(),"error");
//        Toast.makeText(getActivity(),"error",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loading() {
        showDialog();
    }

    @Override
    public void loadStop() {
        dismissDialog();
        refreshLayout.stop();
    }
}
