package com.fragmentapp.im.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fragmentapp.R;
import com.fragmentapp.im.MessageStatus;
import com.fragmentapp.im.bean.MsgBean;

/**
 * Created by liuzhen on 2018/8/7.
 */

public class IMImageTextHolder extends IMBaseHolder{

    protected TextView tv_update,tv_content,tv_title;
    protected ImageView iv_file;

    @Override
    protected void init(boolean isSelef) {
        iv_file = getView(R.id.iv_file);
        tv_update = getView(R.id.tv_update);
        tv_content = getView(R.id.tv_content);
        tv_title = getView(R.id.tv_title);
    }

    @Override
    protected void status(int status) {
        switch (status) {//0:消息占位（在发送图片、文件等耗时任务是）1:发送中 2:发送成功 3:已收到 4:已读 5:撤销 10:发送失败 11:已删除
            case MessageStatus.SPACE:
            case MessageStatus.CREATED:
                pb_load.setVisibility(View.VISIBLE);
                btn_error.setVisibility(View.GONE);
                tv_read.setVisibility(View.GONE);
                break;
            case MessageStatus.SEND_ERROR:
                pb_load.setVisibility(View.GONE);
                tv_read.setVisibility(View.GONE);
                btn_error.setVisibility(View.VISIBLE);
                break;
            case MessageStatus.SEND_SUCCEED:
            case MessageStatus.RECEIVE_SUCCEED:
                pb_load.setVisibility(View.GONE);
                btn_error.setVisibility(View.GONE);
                tv_read.setVisibility(View.VISIBLE);
                tv_read.setText("已送达");
                break;
            case MessageStatus.READ:
                pb_load.setVisibility(View.GONE);
                btn_error.setVisibility(View.GONE);
                tv_read.setVisibility(View.VISIBLE);
                tv_read.setText("已读");
                break;
        }
    }

    public IMImageTextHolder(View view) {
        super(view);

    }

    @Override
    public void content(final MsgBean item){

        tv_title.setText("这是一个图文消息");
        tv_content.setText("xxxxxxxxxxxxx");

        String val = "xxx 更新于xxxx xx xx";
        tv_update.setText(val);

        btn_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.click(v,item);
                }
            }
        });
        layout_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.click(v,item);
                }
            }
        });
        layout_content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.longClick(v, item);
                }
                return true;
            }
        });
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.click(v,item);
                }
            }
        });
    }

}
