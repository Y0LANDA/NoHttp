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
package com.yanzhenjie.nohttp.sample.activity.download;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.yanzhenjie.nohttp.sample.R;
import com.yanzhenjie.nohttp.sample.activity.BaseActivity;
import com.yanzhenjie.nohttp.sample.adapter.RecyclerListSingleAdapter;
import com.yanzhenjie.nohttp.sample.util.OnItemClickListener;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;

/**
 * <p>下载件demo.</p>
 * Created in Oct 10, 2015 12:58:25 PM.
 *
 * @author Yan Zhenjie.
 */
public class DownloadActivity extends BaseActivity {

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_download);

        List<String> listItems = Arrays.asList(getResources().getStringArray(R.array.activity_download_item));

        RecyclerListSingleAdapter listAdapter = new RecyclerListSingleAdapter(listItems, mItemClickListener);
        RecyclerView recyclerView = ButterKnife.findById(this, R.id.rv_download_activity);
        recyclerView.setAdapter(listAdapter);
    }

    /**
     * list item单击.
     */
    private OnItemClickListener mItemClickListener = (v, position) -> {
        if (position == 0) {// 下载单个文件
            startActivity(new Intent(DownloadActivity.this, DownloadSingleFileActivity.class));
        } else if (position == 1) {// 下载多个文件
            startActivity(new Intent(DownloadActivity.this, DownloadFileListActivity.class));
        }
    };

}