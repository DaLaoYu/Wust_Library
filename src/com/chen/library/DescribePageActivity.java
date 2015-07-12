package com.chen.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chen.library.CustomDialog.CustomDialogUtils;
import com.chen.library.DataUtils.XMLParser;
import com.chen.library.HttpPath.UrlPath;
import com.chen.library.Utils.HttpUtils;
import com.chen.library.Utils.HttpsUtils;
import com.chen.library.Utils.ImageUtils;

//�鼮����ҳ
public class DescribePageActivity extends Activity {

	private TextView titleTextView;// ��ʾdtֵ
	private TextView valueTextView;// ��ʾddֵ
	private ListView listView;// ������ʾ�������ݵ�ListView����
	private ImageView imageView;// ������ʾ�鼮ͼƬ��ImageView����
	private Dialog progressDialog;// ��ȡ��Ϣʱչʾ�Ľ��������ڽ�����ɺ���ʧ
	private List<String> isbnName;// isbn�ַ�������
	private String imagePath;// ͼƬ��ַ
	private String summaryString;// �鼮����
	private ImageView backImageView;
	// ��������鼮ͼƬ��handler
	final Handler imageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			byte[] result = (byte[]) msg.obj;
			if (result == null) {
				return;

			}
			// �����ֽ�������λͼ
			Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0,
					result.length);
			// ΪͼƬ�ؼ�����λͼ������ʾͼƬ

			imageView.setImageBitmap(bitmap);
		};
	};

	// ��������鼮��Ϣ��handler
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			List<Map<String, String>> data = (List<Map<String, String>>) msg.obj;
			CustomAdapter adapter = new CustomAdapter(data);
			listView.setAdapter(adapter);

			progressDialog.dismiss();// ��������ʧ
//			if (isbnName.get(0) != null) {
//				// ������ʼ��ȡͼƬurl���������߳�
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						String isbnPathString = UrlPath.DOUBAN_BOOK_INFO_WITH_ISBN
//								+ isbnName.get(0).toString();
//						Log.v("isbn------->isbnPathString", isbnPathString);
//						String jsonString = "";
//						if (isbnPathString != null) {
//							jsonString = HttpsUtils.GetString(isbnPathString,
//									"utf-8");
//						}
//						Log.v("jsonString------->jsonString", jsonString);
//						try {
//							JSONObject tmpJsonObject = new JSONObject(
//									jsonString);
//
////							imagePath = tmpJsonObject
////									.getJSONObject("rating").getString(
////											"image");
//							imagePath=tmpJsonObject.getString("image");
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}
//
//						System.out.println("imagePath=========" + imagePath);
//						// summaryString = UrlPath.SUMMARY_OF_BOOK;
//						// System.out.println("summary=========" +
//						// summaryString);
//						// textView3.setText("this is summaryString");
//						// ��ʼ��ȡͼƬ���̣߳���imageHandler�и���ͼƬ
//						if (imagePath == null) {
//							return;
//
//						}
//						new Thread(new ImageThread(ImageUtils.GetMediumPictureUrl(isbnPathString))).start();
//
//					}
//				}).start();
//			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ��ΪҪ�Զ�����⣬����ȥ��title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ���ز����ļ�
		this.setContentView(R.layout.activity_describe_page);
		backImageView = (ImageView) findViewById(R.id.back_img);
		backImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				DescribePageActivity.this.finish();
			}
		});

		isbnName = new ArrayList<String>();// ��ʼ��isbnName
		summaryString = "";// ��ʼ��summaryString
		imagePath = "";// ��ʼ��imagePath
		// ����Ϊ����id��ȡ�ؼ����
		imageView = (ImageView) this.findViewById(R.id.imageView1);
		listView = (ListView) this.findViewById(R.id.listView1);
		progressDialog = CustomDialogUtils.getViewContainer(this).getDialog();
		progressDialog.show();// ��ʾ������
		progressDialog.setCancelable(false);// ���ɴ���ȡ��
		Intent intent = getIntent();
		// ��ȡ�����Ӧ������ҳ����
		final String loadUrl = UrlPath.WUST_LIB_HOME_URL
				+ intent.getStringExtra("linkedUrl");

		// ���鼮���ӻ�ȡ�����Ӧ����ҳ����Ϣ���������õ�������Ϣ
		new Thread(new GetBookInofThread(loadUrl)).start();

	}

	public class GetBookInofThread implements Runnable {
		private String urlString;

		public GetBookInofThread(String urlString) {
			this.urlString = urlString;
		}

		@Override
		public void run() {

			List<Map<String, String>> list = new ArrayList<Map<String, String>>();

			String html = HttpUtils.GetString(urlString, "utf-8");
			if (html == null) {

				return;
			}
			// �����õ���Jsoup������html�ַ����õ��ĵ�����
			Document document = Jsoup.parse(html);
			// �õ�����<dl class="booklist">�ı�ǩ
			Elements elements = document.select("dl.booklist");
			for (Element element : elements) {
				Map<String, String> map = new HashMap<String, String>();

				String dt = element.getElementsByTag("dt").first().text();
				System.out.println(dt);

				String dd = element.getElementsByTag("dd").first().text();
				System.out.println(dd);
				if (dt.equals("ISBN������:")) {
					String isbnName1 = dd.substring(0, 17);
					System.out.println("isbnName===========" + isbnName1);
					isbnName.add(isbnName1);

				} else if (dt.equals("ISBN:")) {
					String isbnName2 = dd.substring(0, 13);
					System.out.println("isbnName===========" + isbnName2);
					isbnName.add(isbnName2);
				}
				map.put("dt", dt);
				map.put("dd", dd);
				list.add(map);

			}

			Message message = Message.obtain();
			message.obj = list;
			handler.sendMessage(message);

		}
	}

	// imageThread�Ķ���
	class ImageThread implements Runnable {
		private String urlString;

		public ImageThread(String urlString) {
			this.urlString = urlString;
		}

		@Override
		public void run() {
			byte[] data = HttpUtils.GetByteArray(urlString);
			Message message = Message.obtain();
			message.obj = data;
			// ����handlel������Ϣ��Ҳ�����첽������
			imageHandler.sendMessage(message);
		}
	}

	// �Զ���listView���������࣬����̳���BaseAdapter����д�����еķ���
	class CustomAdapter extends BaseAdapter {
		private List<Map<String, String>> map;// ���ݼ�

		public CustomAdapter(List<Map<String, String>> map) {
			this.map = map;
		}

		// ��ȡ���ݼ���С
		@Override
		public int getCount() {
			return map.size();
		}

		// ��ȡitem��Ӧ������
		@Override
		public Object getItem(int position) {
			return map.get(position);
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
			if (convertView == null) {// ������õ�viewΪ�գ�������Զ���Ĳ����ļ����õ���ͼ
				view = LayoutInflater.from(getApplicationContext()).inflate(
						R.layout.item_detail, null);

			} else {// ��Ϊ��ʱ������������ͼ
				view = convertView;
			}
			// ��������Ϊ����listView��Ӧitem����ʾ��Ϣ
			titleTextView = (TextView) view.findViewById(R.id.textView1);
			valueTextView = (TextView) view.findViewById(R.id.textView2);
			valueTextView.setTextSize(18);
			titleTextView.setText(map.get(position).get("dt"));
			valueTextView.setText(map.get(position).get("dd"));

			return view;
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
