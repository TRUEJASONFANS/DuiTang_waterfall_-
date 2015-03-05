package com.example.gettingdata;

import java.util.HashMap;
import java.util.Map;

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

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GettingAndParsingTask extends AsyncTask<Integer, Integer, String> {

	private String targetUrl;
	private Handler mHandler;
	private int current_start;
	
	public GettingAndParsingTask(String targetUrl, int nextStart, Handler mHandler) {
		this.targetUrl = targetUrl;
		this.mHandler = mHandler;
		this.current_start = nextStart;
	}

	@Override
	protected String doInBackground(Integer... params) {
		// TODO Auto-generated method stub
		try {
			String result = doGet(current_start, targetUrl);
			Analysis(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 访问数据库并返回JSON数据字符串
	 * 
	 * @param params
	 *            向服务器端传的参数
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

	/**
	 * 解析
	 * 
	 * @throws JSONException
	 */
	private  void Analysis(String jsonStr)
			throws JSONException {
		/******************* 解析 ***********************/
		Map<String,Object> datalist = new HashMap<String,Object> (); 
		JSONTokener jsonParser = new JSONTokener(jsonStr);  
		JSONObject page = (JSONObject) jsonParser.nextValue();
		JSONObject data = (JSONObject) page.getJSONObject("data");
		int next_start = data.getInt("next_start");
		datalist.put("next_start", next_start);		
		JSONArray object_list = data.getJSONArray("object_list");
		datalist.put("object_list", object_list);
		/*
		for(int i = 0;i < object_list.length(); i++) {
			JSONObject object = object_list.getJSONObject(i);
			int favorite_count = object.getInt("favorite_count");
			String msg = object.getString("msg");
			JSONObject photo = object.getJSONObject("photo");
			
			int width = photo.getInt("width");
			int height = photo.getInt("height");
			String urlPath = photo.getString("path");
			
		}
		*/

		Message msg = Message.obtain();
		msg.what = 1;
		msg.obj = datalist;
		mHandler.sendMessage(msg);
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		
	}

}
