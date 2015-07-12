package com.chen.library;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chen.library.CustomDialog.CustomDialogUtils;
import com.chen.library.NetWorkState.NetworkUtils;
import com.chen.library.Utils.HttpUtils;
import com.chen.library.SearchBookActivity.MyAdapter;
import com.chen.library.SearchBookActivity.MyHandler;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class SearchPaperActivity extends Activity {
	private MyAdapter adapter;
	private ImageView backImageView;
	private Button searchButton;
	private EditText paperEditText;
	public int currentPageNo = 1;// ��¼��ǰҳ��
	public int totalPageNo;// ��ҳ�����ɽ����õ�
	public int loadNum = 1;
	public List<Map<String, String>> list = new ArrayList<Map<String, String>>();// ListView������
	private TextView nameTextView;
	private TextView authorTextView;
	public String path1 = "http://s.g.wanfangdata.com.cn/Paper.aspx?q=%E9%A2%98%E5%90%8D:";
	public String path2 = "%20DBID:WF_XW&f=c.Thesis&p=";
	private PullToRefreshListView mPullRefreshListView;
	private ArrayAdapter<String> mAdapter;
	public Dialog dialog;
	public List<String> linkList;// ���Ӽ���
	public List<String> contentsLinkList;// ���Ӽ���

	// ʵ����MyHandler����
	MyHandler handler = new MyHandler();

	Handler notificationThreadHandler = new Handler() {
		public void handleMessage(Message msg) {
			mPullRefreshListView.onRefreshComplete();
			Toast.makeText(getApplicationContext(), "�Ѿ�������Ŷ��",
					Toast.LENGTH_SHORT).show();
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_paper);
		searchButton = (Button) this.findViewById(R.id.button1);
		paperEditText = (EditText) this.findViewById(R.id.editText1);
		dialog = CustomDialogUtils.getViewContainer(this).getDialog();
		dialog.setCancelable(false);
		adapter = new MyAdapter(list);
		linkList = new ArrayList<String>();
		contentsLinkList = new ArrayList<String>();

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list1);
		mPullRefreshListView.setMode(Mode.PULL_FROM_END);// ������ˢ��
		mPullRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						String label = DateUtils.formatDateTime(
								getApplicationContext(),
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);
						// Update the LastUpdatedLabel
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);
						// Do work to refresh the list here.
						// new GetDataTask().execute();
						if (currentPageNo <= totalPageNo) {

							Log.v("----<currentPage", currentPageNo
									+ "------>�й�" + totalPageNo);

							String paperName = paperEditText.getText()
									.toString().trim();

							try {
								paperName = URLEncoder.encode(paperName,
										"utf-8");
							} catch (UnsupportedEncodingException e1) {
								e1.printStackTrace();
							}
							String urlString = path1 + paperName + path2
									+ currentPageNo;
							Log.v("-----url------>>>>>", urlString);
							new Thread(new MyThread(urlString)).start();
						} else {
							new Thread(new NotificationThread()).start();
						}

					}
				});

		backImageView = (ImageView) findViewById(R.id.about_us_back);
		backImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SearchPaperActivity.this.finish();
			}
		});
		mPullRefreshListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(SearchPaperActivity.this,
						AcademicPaperDetail.class);

				// ���ݸ��������ҳ���ӣ���ֻ������һ����
				intent.putExtra("linkedUrl", linkList.get(position - 1));
				Log.v("���������----��", linkList.get(position - 1));
				intent.putExtra("contentsUrl",
						contentsLinkList.get(position - 1));
				Log.v("���������----��", linkList.get(position - 1) + "----->"
						+ contentsLinkList.get(position - 1));
				// ��������ҳhttp://d.g.wanfangdata.com.cn/Thesis_D365640.aspx
				// http://d.g.wanfangdata.com.cn/ThesisCatalog.aspx?ID=Thesis_D365640
				SearchPaperActivity.this.startActivity(intent);
			}

		});
		// ���������ť�����¼�
		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// ���ȼ���Ƿ������������װ��getNetWorkState()��������Ҫandroid.permission.ACCESS_NETWORK_STATEȨ��
				if (NetworkUtils.getNetWorkState(SearchPaperActivity.this)) {// ����������
					if (paperEditText.getText().toString().equals("")) {
						// �ؼ���Ϊ��ʱ����������ؼ���
						Toast toast = new Toast(SearchPaperActivity.this);
						View view = LayoutInflater.from(
								SearchPaperActivity.this).inflate(
								R.layout.custom_toast, null);
						TextView textView = (TextView) view
								.findViewById(R.id.textView1);
						textView.setText("������ؼ��֣�");
						toast.setView(view);
						toast.setGravity(Gravity.LEFT | Gravity.TOP, 250, 100);
						toast.setDuration(1000);
						toast.show();

					} else {// �ؼ��ֲ�Ϊ��ʱ
						currentPageNo = 1;
						String paperName = paperEditText.getText().toString()
								.trim();

						try {
							paperName = URLEncoder.encode(paperName, "utf-8");
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}
						String urlString = path1 + paperName + path2
								+ currentPageNo;
						Log.v("-----url------>>>>>", urlString);
						new Thread(new MyThread(urlString)).start();
						dialog.show();
					}
				} else {// ����������ʱ
					Toast toast = new Toast(SearchPaperActivity.this);
					View view = LayoutInflater.from(SearchPaperActivity.this)
							.inflate(R.layout.custom_toast, null);
					toast.setView(view);
					toast.setGravity(Gravity.LEFT | Gravity.TOP, 250, 100);
					toast.setDuration(1000);
					toast.show();
				}

			}

		});
	}

	// listview�Զ������������
	public class MyAdapter extends BaseAdapter {
		private List<Map<String, String>> data;// ���ݼ�

		public MyAdapter(List<Map<String, String>> data) {
			this.data = data;
		}

		// ��ȡ���ݼ���С
		@Override
		public int getCount() {
			return data.size();
		}

		// ��ȡitem��Ӧ������
		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		// item��λ�ã������ݼ��е�position
		@Override
		public long getItemId(int position) {
			return position;
		}

		// ����Ҫ�Ļ�ȡItemde����ͼ���漰����������ã������ڴ�й¶
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = null;
			if (convertView == null) {
				view = LayoutInflater.from(SearchPaperActivity.this).inflate(
						R.layout.paper_listview_item, null);

			} else {
				view = convertView;
			}
			nameTextView = (TextView) view.findViewById(R.id.txt_name);
			authorTextView = (TextView) view.findViewById(R.id.txt_author);

			nameTextView.setText(data.get(position).get("up"));
			authorTextView.setText(data.get(position).get("down"));

			return view;
		}
	}

	// �������ݵ�handler��
	public class MyHandler extends Handler {

		List<Map<String, String>> listOnce = new ArrayList<Map<String, String>>();

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what==110) {
				dialog.dismiss();
				try {
					new Thread().sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Toast.makeText(SearchPaperActivity.this, "δ�ҵ��������", Toast.LENGTH_SHORT).show();
				return;
				
			}

			listOnce = (List<Map<String, String>>) msg.obj;
			list.addAll(listOnce);// ������������
			Log.v("url ����-----��", linkList.toString());

			if (currentPageNo == 1) {// �ڻ�ȡ����һҳ���ݵ�ʱ���������������������������ú����һЩ��������ÿ�η�ҳ���Զ���ʾ����һ��
				ListView actualListView = mPullRefreshListView
						.getRefreshableView();
				actualListView.setAdapter(adapter);
				dialog.dismiss();
			}
			mPullRefreshListView.onRefreshComplete();
			adapter.notifyDataSetChanged();// ����adapter���ݱ仯,�����д�޷���ҳ��ʾ���µ�����
			currentPageNo++;

		}
	}

	// ��ȡlistview���ݵ��߳�
	public class MyThread implements Runnable {

		List<Map<String, String>> listOnce;
		private String workPath;

		public MyThread(String workPath) {
			this.workPath = workPath;
			listOnce = new ArrayList<Map<String, String>>();
		}

		@Override
		public void run() {

			String data = HttpUtils.GetString(workPath, "utf-8");
			Log.v("----->>>>>>data------>>>>>", data);
			Document document = Jsoup.parse(data);

			
			Element pageNumElement=document.select("p.pager_space span.page_link").first();
			if (pageNumElement == null) {
				Log.v("----->>>>>>�Ƿ���ҵ�------>>>>>", "��");

				Message message = Message.obtain();

				message.what = 110;
				handler.sendMessage(message);
				return;
			} else {
				Elements elements = document
						.select("div#content div#content_div div div ul.list_ul");

				
				if (loadNum == 1) {
					totalPageNo = Integer.parseInt(pageNumElement.text()
							.charAt(2) + "");
					Log.v("----->>>>>>ҳ��------>>>>>", pageNumElement.text()
							.charAt(2) + "");
				}

				for (Element element : elements) {
					Map<String, String> pass_dataMap = new HashMap<String, String>();

					String urlString = element.select("li.title_li").first()
							.getElementsByTag("a").last().attr("href");

					linkList.add(urlString);// �������ӱ�

					if (element.select("li.zi a").size() <= 2) {
						contentsLinkList.add("no");// �������ӱ�

					} else {

						String contentsString = element.select("li.zi span")
								.last().getElementsByTag("a").first()
								.attr("href");

						contentsLinkList.add(contentsString);// �������ӱ�

					}

					String titleString = element.select("li.title_li").first()
							.text();
					String authorString = element.select("li.greencolor")
							.first().text();
					Log.v("----->>>>>>title------>>>>>", titleString);
					Log.v("----->>>>>>author------>>>>>", authorString);
					// ���뼯����
					pass_dataMap.put("up", titleString);
					pass_dataMap.put("down", authorString);

					listOnce.add(pass_dataMap);
					titleString = "";
					authorString = "";
				}
			}

			Message message = Message.obtain();

			message.obj = listOnce;
			message.what = 111;
			handler.sendMessage(message);

		}
	}

	public class NotificationThread implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			notificationThreadHandler.sendEmptyMessage(0);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			this.finish();

		}

		return false;

	}
}
