package com.chen.library;

import java.io.IOException;
import java.util.HashMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chen.library.CustomDialog.CustomDialogUtils;
import com.chen.library.HttpPath.UrlPath;
import com.chen.library.Login.LoginUtils;
import com.chen.library.NetWorkState.NetworkUtils;

public class LoginActivity extends Activity {
	private EditText idEditText;
	private EditText passwordEditText;
	private EditText verifyEditText;
	private TextView remindText;
	private ImageView verifyImageView;
	private Button loginbButton;
	private Button cancelButton;
	private AlertDialog.Builder alertDialog;
	private Dialog progressDialog;
	private ImageView backImageView;
	static HashMap<String, String> data;

	Handler alertDialogHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			int showAlertDialogFlag = msg.what;
			progressDialog.dismiss();
			if (showAlertDialogFlag != 302) {
				alertDialog.show();

			} else {
				LoginUtils.isLogin = true;
				Intent intent = new Intent();
				intent.setClass(LoginActivity.this, MyLibraryActivity.class);
				LoginActivity.this.startActivity(intent);
				LoginActivity.this.finish();
			}
		};
	};

	Handler formHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			LoginActivity.data = (HashMap<String, String>) msg.obj;
		};
	};

	// ����listviewͼƬ
	Handler imageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			byte[] data = (byte[]) msg.obj;
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			verifyImageView.setImageBitmap(bitmap);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);
		backImageView = (ImageView) findViewById(R.id.about_us_back);
		backImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				LoginActivity.this.finish();
			}
		});
		idEditText = (EditText) this.findViewById(R.id.txtId);
		passwordEditText = (EditText) this.findViewById(R.id.txtPassword);
		verifyEditText = (EditText) this.findViewById(R.id.txtVerityCode);
		remindText = (TextView) this.findViewById(R.id.remindText);
		verifyImageView = (ImageView) this.findViewById(R.id.verifyImage);
		loginbButton = (Button) this.findViewById(R.id.btnLogin);
		cancelButton = (Button) this.findViewById(R.id.btnCacel);
		alertDialog = new AlertDialog.Builder(this);
		progressDialog = CustomDialogUtils.getViewContainer(this).getDialog();
		// progressDialog.setMessage("��½��...");

		if (NetworkUtils.getNetWorkState(this)) {
			new Thread(new GetLoginFormDataThread(UrlPath.LOGIN_URL)).start();
		} else {
			Toast.makeText(LoginActivity.this, "�����������ӣ�", Toast.LENGTH_SHORT)
					.show();
			Intent intent = new Intent();
			intent.setClass(LoginActivity.this, MainPageActivity.class);
			startActivity(intent);
			this.finish();
		}

		alertDialog.setTitle("��½ʧ�ܣ�");
		alertDialog.setMessage("�����û�����������Ϣ��");
		alertDialog.setIcon(R.drawable.ic_launcher);
		alertDialog.setPositiveButton("ȷ��",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

						return;
					}
				});
		alertDialog.setNegativeButton("����",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread(new LoginThread(idEditText.getText()
								.toString(), passwordEditText.getText()
								.toString(), verifyEditText.getText()
								.toString())).start();
						progressDialog.setCancelable(false);
						progressDialog.show();

					}
				});

		loginbButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new Thread(new LoginThread(idEditText.getText().toString(),
						passwordEditText.getText().toString(), verifyEditText
								.getText().toString())).start();
				progressDialog.setCancelable(false);
				progressDialog.show();

			}
		});

		verifyEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				if (arg1) {

					String s1 = "������������ʾ��"
							+ "<font color=\"red\" face=\"����\" size=\"3\">"
							+ "��ɫ" + "</font>" + "����";
					remindText.setText(Html.fromHtml(s1));

					new Thread(new DownloadImageThread(UrlPath.VERIFY_IMAGE_URL
							+ idEditText.getText().toString().trim())).start();
				}
			}
		});

	}

	/***
	 * 
	 * @author chen ��ȡLoginFormData�߳�
	 * 
	 */
	public class GetLoginFormDataThread implements Runnable {
		private String loginUrl;

		public GetLoginFormDataThread(String url) {
			this.loginUrl = url;
		}

		@Override
		public void run() {

			try {
				HashMap<String, String> result;
				result = LoginUtils.getLoginFormData(loginUrl);
				Message msg = Message.obtain();
				msg.obj = result;
				formHandler.sendMessage(msg);

			} catch (ParseException e) {
				e.printStackTrace();

			}
		}
	}

	/****
	 * 
	 * @author chen ������֤ͼƬ�߳���
	 * 
	 */
	public class DownloadImageThread implements Runnable {
		private String imageUrl;

		public DownloadImageThread(String url) {
			this.imageUrl = url;
		}

		@Override
		public void run() {
			byte[] message;
			try {
				message = getByteArray(imageUrl);
				Message msg = Message.obtain();
				msg.obj = message;
				imageHandler.sendMessage(msg);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public class LoginThread implements Runnable {
		private String id;
		private String psd;
		private String code;

		public LoginThread(String id, String psd, String code) {
			this.id = id;
			this.psd = psd;
			this.code = code;
		}

		@Override
		public void run() {
			int statusCode = LoginUtils.login(data, id, psd, code);
			Message msg = Message.obtain();
			msg.what = statusCode;
			alertDialogHandler.sendMessage(msg);

		}
	}

	/*****
	 * 
	 * @param url
	 *            ͼƬurl
	 * @return ��֤ͼƬ�ֽ�����
	 * @throws ParseException
	 * @throws IOException
	 */
	private byte[] getByteArray(String url) throws ParseException, IOException {
		HttpGet get = new HttpGet(url);
		// ���ñ���ΪGBK
		HttpResponse response = new DefaultHttpClient().execute(get);
		LoginUtils.cookie = response.getFirstHeader("Set-Cookie").getValue();
		// ȡ��cookie����������
		System.out.println("��ȡͼƬʱcookie============" + LoginUtils.cookie);
		System.out.println("��ȡͼƬʱ״̬��========"
				+ response.getStatusLine().getStatusCode());
		HttpEntity entity = response.getEntity();
		return EntityUtils.toByteArray(entity);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			this.finish();

		}

		return false;

	}
}
