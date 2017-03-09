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
package com.yanzhenjie.nohttp.sample.activity.cancel;

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
 * <p>演示怎么取消请求。</p>
 * Created in Oct 23, 2015 1:13:06 PM.
 *
 * @author Yan Zhenjie.
 */
public class CancelActivity extends BaseActivity {

    @Override
    protected void onActivityCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_cacel);

        List<String> imageItems = Arrays.asList(getResources().getStringArray(R.array.activity_cancel_item));
        RecyclerListSingleAdapter listAdapter = new RecyclerListSingleAdapter(imageItems, mItemClickListener);
        RecyclerView recyclerView = ButterKnife.findById(this, R.id.rv_cancel_activity);
        recyclerView.setAdapter(listAdapter);
    }

    /**
     * list item单击。
     */
    private OnItemClickListener mItemClickListener = (v, position) -> {
        Intent intent = null;
        switch (position) {
            case 0:// 和Activity声明周日联动。
                intent = new Intent(CancelActivity.this, CancelLinkageActivity.class);
                break;
            case 1:// 根据sign取消队列中某几个请求。
                intent = new Intent(CancelActivity.this, CancelSignActivity.class);
                break;
            case 2:// 取消队列中所有请求。
                intent = new Intent(CancelActivity.this, CancelAllActivity.class);
                break;
            default:
                break;
        }
        if (intent != null)
            startActivity(intent);
    };

}