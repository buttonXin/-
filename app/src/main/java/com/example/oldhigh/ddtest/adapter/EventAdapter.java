package com.example.oldhigh.ddtest.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.oldhigh.ddtest.R;
import com.example.oldhigh.ddtest.bean.NewEventBean;
import com.example.oldhigh.ddtest.util.L;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by oldhigh on 2017/11/26.
 */

public class EventAdapter extends BaseAdapterRV<NewEventBean> {
    @Override
    public RecyclerView.ViewHolder createVHolder(ViewGroup parent, int viewType) {
        return new EventHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.adapter_event , parent , false));
    }

    @Override
    protected void onBindVH(RecyclerView.ViewHolder viewHolder, int pos, NewEventBean eventBean) {

        final EventHolder holder = (EventHolder) viewHolder;

        holder.text_content_adapter.setText(eventBean.getContent());

        holder.text_time_adapter.setText(
                new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(eventBean.getTime())));

        if (holder.text_content_adapter.getLineCount() == 3 ){

            holder.text_content_adapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.text_content_adapter.getMaxLines() == Integer.MAX_VALUE){
                        holder.text_content_adapter.setMaxLines(3);

                    }else {
                        holder.text_content_adapter.setMaxLines(Integer.MAX_VALUE);

                    }
                }
            });

        }
    }

    public static class EventHolder extends Holder {

        @BindView(R.id.text_time_adapter)
        TextView text_time_adapter ;

        @BindView(R.id.text_content_adapter)
        TextView text_content_adapter ;


        public EventHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this , itemView);

           /* text_content_adapter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (text_content_adapter.getMaxLines() == Integer.MAX_VALUE){
                        text_content_adapter.setMaxLines(3);

                    }else {
                        text_content_adapter.setMaxLines(Integer.MAX_VALUE);

                    }
                    L.e("---> line = " + text_content_adapter.getLineCount());

                }
            });*/

        }
    }
}
