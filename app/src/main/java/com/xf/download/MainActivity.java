package com.xf.download;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.xf.download.lib.DownLoadBean;
import com.xf.download.lib.DownLoadManeger;

public class MainActivity extends Activity {
    private DownLoadManeger downLoadManeger;
    private String[] urls = new String[]{"https://49d2147716ff75a9dc3c984f02381780.dd.cdntips.com/imtt.dd.qq.com/16891/43A12D30FFBD592D7EADDB5630C216D2.apk?mkey=5bfd721e71f69c3d&f=8935&fsname=com.tencent.mm_6.7.3_1360.apk&csr=1bbd&cip=113.246.186.200&proto=https",
            "https://a159c9ff9b872014320c639e4638836f.dd.cdntips.com/imtt.dd.qq.com/16891/0669F69E8F4A697A8E66FDB86C06A06F.apk?mkey=5c0915c671f6482c&f=0ce9&fsname=com.tencent.mobileqq_7.9.0_954.apk&csr=1bbd&cip=113.246.110.217&proto=https",
            "https://76ce58ad7d747d908aee9f600f7d6ec6.dd.cdntips.com/imtt.dd.qq.com/16891/161853FBFC30DF5CD3E7C43CDAA1CCF4.apk?mkey=5c09159971f6482c&f=8eb5&fsname=com.tencent.weishi_4.8.0.588_480.apk&csr=1bbd&cip=113.246.110.217&proto=https",
            "https://a159c9ff9b872014320c639e4638836f.dd.cdntips.com/imtt.dd.qq.com/16891/AB14566BECB2AFA2B8B2BEF864A94595.apk?mkey=5c09157e71f6482c&f=1849&fsname=com.tencent.mtt_8.9.5.4610_8954610.apk&csr=1bbd&cip=113.246.110.217&proto=https",
            "https://bf45a1d0861cf7963d7797cd2532fd4c.dd.cdntips.com/imtt.dd.qq.com/16891/EFFC26EB70508E0342F767A1636E7CD3.apk?mkey=5c09155471f6482c&f=0c27&fsname=com.tencent.qqpimsecure_7.11.0_1280.apk&csr=1bbd&cip=113.246.110.217&proto=https"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downLoadManeger = DownLoadManeger.getInstance();
        downLoadManeger.setAutoDownLoad(true);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(new ListAdapter());
    }



    public void stop(View view) {
        downLoadManeger.pauseAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downLoadManeger.onDestroy();
    }

    class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return urls.length;
        }

        @Override
        public Object getItem(int position) {
            return urls[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_download, parent, false);
                holder.progressBar = convertView.findViewById(R.id.progressBar);
                holder.btn_start = convertView.findViewById(R.id.btn_start);
                holder.btn_pause = convertView.findViewById(R.id.btn_pause);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            downLoadManeger.addTask(new DownLoadManeger.DownloadBuilder().listener(new DownLoadBean.DownLoadListener() {
                @Override
                public void onStart(long totalSize) {
                    holder.progressBar.setMax((int) totalSize);
                }

                @Override
                public void onError(String error) {
                }

                @Override
                public void onProgress(long progress) {
                    holder.progressBar.setProgress((int) progress);
                }

                @Override
                public void onFinish(String filePath) {
                    Toast.makeText(getApplicationContext(),filePath,Toast.LENGTH_SHORT).show();
                }
            }).url(urls[position]).fileName("test" + position + ".apk").filePath("sdcard/"));
            holder.btn_start.setOnClickListener(v -> downLoadManeger.start(urls[position]));
            holder.btn_pause.setOnClickListener(v -> downLoadManeger.pause(urls[position]));
            return convertView;
        }

        class ViewHolder {
            ProgressBar progressBar;
            Button btn_start;
            Button btn_pause;
        }
    }
}
