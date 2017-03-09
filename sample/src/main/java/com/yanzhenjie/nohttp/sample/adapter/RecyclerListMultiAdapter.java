/*
 * Copyright 2015 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.nohttp.sample.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.entity.ListItem;
import com.yanzhenjie.nohttp.sample.util.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * <p>RecyclerView的List形式Title+SubTitle的适配器。</p>
 * Created in Jan 28, 2016 5:04:03 PM.
 *
 * @author Yan Zhenjie.
 */
public class RecyclerListMultiAdapter extends BaseAdapter<RecyclerListMultiAdapter.TextViewHolder> {


    private List<ListItem> mData;

    private OnItemClickListener mClickListener;

    public RecyclerListMultiAdapter(List<ListItem> data, OnItemClickListener clickListener) {
        this.mData = data;
        this.mClickListener = clickListener;
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                .item_list_title_subtitle, parent, false));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class TextViewHolder extends BaseAdapter.BaseViewHolder implements View.OnClickListener {

        @BindView(R.id.item_list_title)
        TextView mTextView;

        @BindView(R.id.item_list_title_sub)
        TextView mTextViewSub;

        public TextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void setData() {
            ListItem listItem = mData.get(getAdapterPosition());
            mTextView.setText(listItem.getTitle());
            mTextViewSub.setText(listItem.getSubTitle());
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null)
                mClickListener.onItemClick(v, getAdapterPosition());
        }

    }

}
