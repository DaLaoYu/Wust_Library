package com.chen.library;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.chen.library.CustomDialog.CustomDialogUtils;
import com.chen.library.HttpPath.UrlPath;
import com.chen.library.NetWorkState.NetworkUtils;
import com.chen.library.Utils.HttpUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

//����ҳ��
public class SearchBookActivity extends Activity {
	private ListView listView;
	private MyAdapter adapter;
	private TextView textView;
	private EditText editText;
	private ImageView imageView;
	private TextView textView1;
	public List<String> linkList = new ArrayList<String>();// ���Ӽ���
	public List<Map<String, String>> list = new ArrayList<Map<String, String>>();// ListView������
	private boolean isDevide = false;// �Ƿ��ҳ�ı��
	private Spinner spinner;
	private Button button;
	public int currentPageNo = 1;// ��¼��ǰҳ��
	public int totalPageNo;// ��ҳ�����ɽ����õ�
	private ImageView backImageView;
	private Dialog dialog;
	private int titleFlag;
	// ʵ����MyHandler����
	MyHandler handler = new MyHandler();

	Handler getImageUrlHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			String urlString = (String) msg.obj;

			new Thread(new ImageThread(urlString)).start();

		};
	};
	// ����listviewͼƬ
	Handler imageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			byte[] data = (byte[]) msg.obj;
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			imageView.setImageBitmap(bitmap);
		};
	};

	// ��Ӽ��������
	Handler handlerResultNo = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Map<String, String> map = (Map<String, String>) msg.obj;
			String noString = map.get("listItemNumber");
			totalPageNo = Integer.parseInt(map.get("pageNo"));
			textView1.setTextSize(12);
			textView1.setText("��������" + noString + "�����,��" + totalPageNo + "ҳ");

		};

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ���ز����ļ�
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_book);
		dialog = CustomDialogUtils.getViewContainer(this).getDialog();
		backImageView = (ImageView) findViewById(R.id.about_us_back);
		backImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				SearchBookActivity.this.finish();
			}
		});

		listView = (ListView) findViewById(R.id.listView1);
		button = (Button) this.findViewById(R.id.button1);
		editText = (EditText) this.findViewById(R.id.editText1);
		textView1 = (TextView) this.findViewById(R.id.textView1);
		titleFlag = 1;
		adapter = new MyAdapter(list);
		// spinner��������
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getSpinnerData());

		// ����������ʽ������Զ���
		arrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// ���������ť�����¼�
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// ���ȼ���Ƿ������������װ��getNetWorkState()��������Ҫandroid.permission.ACCESS_NETWORK_STATEȨ��
				if (NetworkUtils.getNetWorkState(SearchBookActivity.this)) {// ����������
					if (editText.getText().toString().equals("")) {
						// �ؼ���Ϊ��ʱ����������ؼ���
						Toast toast = new Toast(SearchBookActivity.this);
						View view = LayoutInflater
								.from(SearchBookActivity.this).inflate(
										R.layout.custom_toast, null);
						TextView textView = (TextView) view
								.findViewById(R.id.textView1);
						textView.setText("������ؼ��֣�");
						toast.setView(view);
						toast.setGravity(Gravity.LEFT | Gravity.TOP, 250, 100);
						toast.setDuration(1000);
						toast.show();

					} else {// �ؼ��ֲ�Ϊ��ʱ

						SearchBookActivity.this.list.clear();
						
						adapter.notifyDataSetChanged();// ����adapter���ݱ仯,�����д�޷���ҳ��ʾ���µ�����
						currentPageNo = 1;
						dialog.show();
						dialog.setCancelable(false);
						titleFlag=1;
						String bookName = editText.getText().toString().trim();
						try {
							bookName = URLEncoder.encode(bookName, "utf-8");
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}
//						// ������ȡҳ�������������߳�
//						new Thread(new GetTotalItemThread(UrlPath.PAGE_URL1
//								+ bookName + UrlPath.PAGE_URL2 + currentPageNo))
//								.start();
						// ���������߳�
						new Thread(new MyThread(UrlPath.PAGE_URL1 + bookName
								+ UrlPath.PAGE_URL2 + currentPageNo)).start();

					}
				} else {// ����������ʱ
					Toast toast = new Toast(SearchBookActivity.this);
					View view = LayoutInflater.from(SearchBookActivity.this)
							.inflate(R.layout.custom_toast, null);
					toast.setView(view);
					toast.setGravity(Gravity.LEFT | Gravity.TOP, 250, 100);
					toast.setDuration(1000);
					toast.show();
				}

			}

		});
		// �����ѡ���¼�
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				System.out.println("-->�����" + position);
				Intent intent = new Intent(SearchBookActivity.this,
						DescribePageActivity.class);
				for (int i = 0; i < linkList.size(); i++) {
					System.out.println(linkList.get(i) + "-->" + i);
				}
				// ���ݸ��������ҳ���ӣ���ֻ������һ����
				intent.putExtra("linkedUrl", linkList.get(position));
				// ��������ҳ
				SearchBookActivity.this.startActivity(intent);
			}
		});
		// ��ӻ����¼����ж��Ƿ���з�ҳ
		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// �ô����ж�ʮ����ҪҲ�ǹؼ�
				if (isDevide
						&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					currentPageNo++;// ��ǰҳ��1

					if (currentPageNo <= totalPageNo) {// ��һЩ���ѹ���
						Toast.makeText(getApplicationContext(), "Ŭ��������......",
								Toast.LENGTH_LONG).show();

						// ������ȡ��ҳ���ݵ��̣߳�urlƴ�յõ�
						new Thread(new MyThread(UrlPath.PAGE_URL1
								+ editText.getText().toString().trim()
								+ UrlPath.PAGE_URL2 + currentPageNo)).start();
					} else {// ��һЩ���ѹ���
						Toast.makeText(getApplicationContext(), "�Ѿ�������Ŷ��",
								Toast.LENGTH_SHORT).show();
					}

				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// �ı��ҳ���
				isDevide = (firstVisibleItem + visibleItemCount == totalItemCount);

			}
		});

	}

	// ��xml�ļ��л�ȡspinner������
	public List<String> getSpinnerData() {
		List<String> data = new ArrayList<String>();
		String[] tempString = getResources().getStringArray(R.array.according);
		for (int i = 0; i < tempString.length; i++) {
			data.add(tempString[i]);
		}
		return data;
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
				view = LayoutInflater.from(SearchBookActivity.this).inflate(
						R.layout.item, null);

			} else {
				view = convertView;
			}
			textView = (TextView) view.findViewById(R.id.textView1);
			imageView = (ImageView) view.findViewById(R.id.imageView1);

			// ����textViewҪ��ʾ����������ɫ��һ��ΪHTML��ʽ��һ��Ϊ�ı���ʽ
			String s1 = "<font color=\"red\" face=\"����\" size=\"3\">"
					+ data.get(position).get("up").substring(4) + "</font>";
			textView.setText(Html.fromHtml(s1));
			textView.append(data.get(position).get("down"));

			return view;
		}
	}

	// �������ݵ�handler��
	public class MyHandler extends Handler {

		List<Map<String, String>> list_get_every_time = new ArrayList<Map<String, String>>();

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 110) {
				Toast.makeText(SearchBookActivity.this, "δ��������¼",
						Toast.LENGTH_SHORT).show();
				textView1.setTextSize(12);
				textView1.setText("��������" + 0 + "�����,��" + 0 + "ҳ");
				dialog.dismiss();
				return;

			}
			list_get_every_time = (List<Map<String, String>>) msg.obj;
			list.addAll(list_get_every_time);// ������������

			if (currentPageNo == 1) {// �ڻ�ȡ����һҳ���ݵ�ʱ���������������������������ú����һЩ��������ÿ�η�ҳ���Զ���ʾ����һ��
				listView.setAdapter(adapter);
			}
			adapter.notifyDataSetChanged();// ����adapter���ݱ仯,�����д�޷���ҳ��ʾ���µ�����
			dialog.dismiss();
		}
	}

	// ��ȡ����ͼƬ���߳�
	public class ImageThread implements Runnable {
		public String image_path;

		public ImageThread(String image_path) {
			this.image_path = image_path;
		}

		@Override
		public void run() {

			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(image_path);
			try {
				HttpResponse httpResponse = httpClient.execute(httpPost);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					byte[] data = EntityUtils.toByteArray(httpResponse
							.getEntity());

					Message message = Message.obtain();
					message.obj = data;
					imageHandler.sendMessage(message);

				}
			} catch (ClientProtocolException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			} finally {
				httpClient.getConnectionManager().shutdown();
			}
		}

	}

	public class GetImageUrlThread implements Runnable {
		private String urlString;

		public GetImageUrlThread(String urlString) {
			this.urlString = urlString;

		}

		@Override
		public void run() {

			String htmlString = HttpUtils.GetString(urlString, "utf-8");
			Document document = Jsoup.parse(htmlString);

			String imageUrl = document.select("img#book_img").first()
					.attr("src");
			System.out.println(imageUrl);
			Message message = Message.obtain();
			message.obj = imageUrl;
			getImageUrlHandler.sendMessage(message);

		}

	}

	public class GetTotalItemThread implements Runnable {
		private String workPath;

		public GetTotalItemThread(String workPath) {
			this.workPath = workPath;
		}

		@Override
		public void run() {

			String listItemNumber;
			String pageNo;

			Document document = Jsoup.parse(HttpUtils.doGetString(workPath,
					"utf-8"));
			Elements elements = document.select("strong.red");
			if (elements == null) {
				return;
			}
			Map<String, String> map = new HashMap<String, String>();
			for (Element element : elements) {
				listItemNumber = element.text();
				System.out.println(listItemNumber);
				// ����������
				map.put("listItemNumber", listItemNumber);

			}
			Elements elements2 = document.select("span.pagination");
			for (Element element : elements2) {
				pageNo = element.getElementsByTag("font").last().text();
				map.put("pageNo", pageNo);
				// ����ҳ���
				System.out.println(pageNo);

			}
			Message message = Message.obtain();
			message.obj = map;
			handlerResultNo.sendMessage(message);
			map = null;

		}

	}

	// ��ȡlistview���ݵ��߳�
	public class MyThread implements Runnable {
		String updata = "";
		String downdata = "";
		List<Map<String, String>> list_once = new ArrayList<Map<String, String>>();
		private String workPath;

		public MyThread(String workPath) {
			this.workPath = workPath;
		}

		@Override
		public void run() {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(workPath);
			try {

				HttpResponse httpResponse = httpClient.execute(httpGet);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					String data = EntityUtils.toString(
							httpResponse.getEntity(), "utf-8");
					Document document = Jsoup.parse(data);

					Elements elements = document.select("li.book_list_info");
					Element element1=elements.first();
					if (element1 == null) {
						Message message = Message.obtain();

						message.what = 110;
						handler.sendMessage(message);
						return;
					}
					if (titleFlag == 1) {
						String listItemNumber;
						String pageNo;

						Elements itemsElements = document.select("strong.red");
						if (itemsElements == null) {
							return;
						}
						Map<String, String> map = new HashMap<String, String>();
						for (Element element : itemsElements) {
							listItemNumber = element.text();
							System.out.println(listItemNumber);
							// ����������
							map.put("listItemNumber", listItemNumber);

						}
						Elements elements2 = document.select("span.pagination");
						for (Element element : elements2) {
							pageNo = element.getElementsByTag("font").last()
									.text();
							map.put("pageNo", pageNo);
							// ����ҳ���
							System.out.println(pageNo);

						}
						Message message = Message.obtain();
						message.obj = map;
						handlerResultNo.sendMessage(message);
						map = null;
						titleFlag++;

					}

					for (Element element_item : elements) {
						Map<String, String> pass_dataMap = new HashMap<String, String>();
						// ��ȡ��һҳ������urlString
						String urlString = element_item.getElementsByTag("a")
								.first().attr("href");

						linkList.add(urlString);// �������ӱ�
						Elements span_elements = element_item
								.getElementsByTag("span");
						// downdataΪ�·���ʾ����
						for (Element element2 : span_elements) {
							downdata += "\n" + element2.text();
						}
						Elements h3_elements = element_item
								.getElementsByTag("h3");
						// updataΪ�Ϸ���ʾ����
						for (Element element3 : h3_elements) {
							updata += element3.text();
						}
						// ���뼯����
						pass_dataMap.put("up", updata);
						pass_dataMap.put("down", downdata);

						list_once.add(pass_dataMap);
						pass_dataMap = null;
						downdata = "";
						updata = "";
						urlString = "";
					}

					Message message = Message.obtain();

					message.obj = list_once;
					message.what = 111;
					handler.sendMessage(message);

				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

	// �������ò˵�
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			this.finish();

		}

		return false;

	}

}
