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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.entity.LoadFile;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * <p>文件列表适配器。</p>
 * Created on 2016/5/31.
 *
 * @author Yan Zhenjie;
 */
public class LoadFileAdapter extends BaseAdapter<LoadFileAdapter.DownloadFileViewHolder> {

    /**
     * 需要加载的文件列表。
     */
    private List<LoadFile> loadFiles;

    public LoadFileAdapter(List<LoadFile> loadFiles) {
        this.loadFiles = loadFiles;
    }

    @Override
    public DownloadFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DownloadFileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                .item_load_file_status, parent, false));
    }

    @Override
    public int getItemCount() {
        return loadFiles == null ? 0 : loadFiles.size();
    }

    public class DownloadFileViewHolder extends BaseAdapter.BaseViewHolder implements View.OnClickListener {
        /**
         * 文件状态。
         */
        @BindView(R.id.tv_result)
        TextView mTvResult;
        /**
         * 文件进度。
         */
        @BindView(R.id.pb_progress)
        ProgressBar mPbProgress;

        public DownloadFileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void setData() {
            LoadFile loadFile = loadFiles.get(getAdapterPosition());
            mTvResult.setText(loadFile.getTitle());
            mPbProgress.setProgress(loadFile.getProgress());
        }

        @Override
        public void onClick(View v) {
        }
    }

}
