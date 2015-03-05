package com.huewu.pla.sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.maxwin.view.XListView;
import me.maxwin.view.XListView.IXListViewListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dodola.model.DuitangInfo;
import com.dodowaterfall.Helper;
import com.dodowaterfall.widget.ScaleImageView;
import com.example.android.bitmapfun.util.ImageFetcher;

public class PullToRefreshSampleActivity extends FragmentActivity implements IXListViewListener {
    private ImageFetcher mImageFetcher;//类似于universe_Image_loader
    private XListView mAdapterView = null;
    private StaggeredAdapter mAdapter = null;
    private int next_start;
    private static String url1 = "http://www.duitang.com/napi/blog/list/by_club_id/?club_id=54bcb931a310829b892b1a9e&limit=0&start=";
    
    ContentTask task = new ContentTask(this, 2,url1,48);

    private class ContentTask extends AsyncTask<Integer, Integer, List<DuitangInfo>> {

        private Context mContext;
        private int mType = 1;
        private String targetUrl;
    	private int current_start;
    	
        public ContentTask(Context context, int type,String targetUrl,int current_start) {
            super();
            mContext = context;
            mType = type;
            this.targetUrl = targetUrl;
            this.current_start = current_start;
        }

        @Override
        protected List<DuitangInfo> doInBackground(Integer... params) {
        	try {
    			String result = doGet(current_start, targetUrl);
    			return Analysis(result);
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            return null;
        }
        /**
    	 * 
    	 * @param params
    	 * 向服务器端传的参数
    	 * @param url
    	 * @return
    	 * @throws Exception
    	 */
    	public  String doGet(int current_start, String url)
    			throws Exception {
    		String result = null;
    		// 获取HttpClient对象
    		HttpClient httpClient = new DefaultHttpClient();
    		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
    		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
    		// 新建HttpPost对象
    		HttpGet httpGet = new HttpGet(url+current_start);  
    	
    	
    		// 获取HttpResponse实例
    		HttpResponse httpResp = httpClient.execute(httpGet);
    		// 判断是够请求成功
    		if (httpResp.getStatusLine().getStatusCode() == 200) {
    			// 获取返回的数据
    			result = EntityUtils.toString(httpResp.getEntity(), "UTF-8");
    		} else {
    			Log.i("HttpGet", "HttpPost方式请求失败");
    		}

    		return result;
    	}
        private List<DuitangInfo> Analysis(String jsonStr) throws JSONException {
			// TODO Auto-generated method stub
    		/******************* 解析 ***********************/
        	List<DuitangInfo> duitangs = new ArrayList<DuitangInfo>();
        	
    		JSONTokener jsonParser = new JSONTokener(jsonStr);  
    		JSONObject page = (JSONObject) jsonParser.nextValue();
    		JSONObject data = (JSONObject) page.getJSONObject("data");
    		int next_start = data.getInt("next_start");
    		PullToRefreshSampleActivity.this.next_start = next_start;
    		
    		JSONArray object_list = data.getJSONArray("object_list");

  
    		for(int i = 0;i < object_list.length(); i++) {
    			
    			DuitangInfo newsInfo1 = new DuitangInfo();
    			
    			JSONObject object = object_list.getJSONObject(i);
    			int favorite_count = object.getInt("favorite_count");
    			String msg = object.getString("msg");
    			newsInfo1.setMsg(msg);
    			JSONObject photo = object.getJSONObject("photo");
    			
    			int width = photo.getInt("width");
    			int height = photo.getInt("height");
    			newsInfo1.setHeight(height);
    			String urlPath = photo.getString("path");
    			newsInfo1.setImage_url(urlPath);
        		duitangs.add(newsInfo1);
    		}
    		
    		return duitangs;
  
		}

		@Override
        protected void onPostExecute(List<DuitangInfo> result) {
            if (mType == 1) {

                mAdapter.addItemTop(result);
                mAdapter.notifyDataSetChanged();
                mAdapterView.stopRefresh();

            } else if (mType == 2) {
                mAdapterView.stopLoadMore();
                mAdapter.addItemLast(result);
                mAdapter.notifyDataSetChanged();
            }

        }

    }

    /**
     * 添加内容
     * 
     * @param pageindex
     * @param type
     *            1为下拉刷新 2为加载更多
     */
    private void AddItemToContainer(int next_start, int type) {
        if (task.getStatus() != Status.RUNNING) {
        	ContentTask task = new ContentTask(this, type,url1,next_start);
            task.execute(100);
        }
    }

    public class StaggeredAdapter extends BaseAdapter {
        private Context mContext;
        private LinkedList<DuitangInfo> mInfos;
        private XListView mListView;

        public StaggeredAdapter(Context context, XListView xListView) {
            mContext = context;
            mInfos = new LinkedList<DuitangInfo>();
            mListView = xListView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            DuitangInfo duitangInfo = mInfos.get(position);

            if (convertView == null) {
                LayoutInflater layoutInflator = LayoutInflater.from(parent.getContext());
                convertView = layoutInflator.inflate(R.layout.infos_list, null);
                holder = new ViewHolder();
                holder.imageView = (ScaleImageView) convertView.findViewById(R.id.news_pic);
                holder.contentView = (TextView) convertView.findViewById(R.id.news_title);
                convertView.setTag(holder);
            }

            holder = (ViewHolder) convertView.getTag();
            holder.imageView.setImageWidth(duitangInfo.getWidth());
            holder.imageView.setImageHeight(duitangInfo.getHeight());
            holder.contentView.setText(duitangInfo.getMsg());
            mImageFetcher.loadImage(duitangInfo.getImage_url(), holder.imageView);
            return convertView;
        }

        class ViewHolder {
            ScaleImageView imageView;
            TextView contentView;
            TextView timeView;
        }

        @Override
        public int getCount() {
            return mInfos.size();
        }

        @Override
        public Object getItem(int arg0) {
            return mInfos.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        public void addItemLast(List<DuitangInfo> datas) {
            mInfos.addAll(datas);
        }

        public void addItemTop(List<DuitangInfo> datas) {
            for (DuitangInfo info : datas) {
                mInfos.addFirst(info);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pull_to_refresh_sample);
        mAdapterView = (XListView) findViewById(R.id.list);
        mAdapterView.setPullLoadEnable(true);
        mAdapterView.setXListViewListener(this);

        mAdapter = new StaggeredAdapter(this, mAdapterView);

        mImageFetcher = new ImageFetcher(this, 240);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapterView.setAdapter(mAdapter);
        AddItemToContainer(next_start, 2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRefresh() {
    	next_start = 48;
        AddItemToContainer(next_start, 1);

    }

    @Override
    public void onLoadMore() {
        AddItemToContainer(next_start, 2);

    }
}// end of class
