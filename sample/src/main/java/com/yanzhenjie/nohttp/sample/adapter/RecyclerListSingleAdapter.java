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
import com.yanzhenjie.nohttp.sample.util.OnItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * <p>RecyerlView的Grid形式Title形式适配器。</p>
 * Created in Jan 28, 2016 5:04:03 PM.
 *
 * @author Yan Zhenjie.
 */
public class RecyclerListSingleAdapter extends BaseAdapter<RecyclerListSingleAdapter.TextViewHolder> {

    private List<String> mData;

    private OnItemClickListener mClickListener;

    public RecyclerListSingleAdapter(List<String> data, OnItemClickListener clickListener) {
        this.mData = data;
        this.mClickListener = clickListener;
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TextViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid_title,
                parent, false));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class TextViewHolder extends BaseAdapter.BaseViewHolder implements View.OnClickListener {

        @BindView(R.id.item_list_title)
        TextView mTextView;

        public TextViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void setData() {
            mTextView.setText(mData.get(getAdapterPosition()));
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null)
                mClickListener.onItemClick(v, getAdapterPosition());
        }

    }

}
