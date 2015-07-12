package com.chen.library;

import java.util.ArrayList;
import java.util.List;

import com.chen.library.Login.LoginUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MyLibraryActivity extends Activity {
	private Button btnLogout;
	private LinearLayout aboutInfoLayout;
	private LinearLayout aboutBorrowLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_mylibrary);
		aboutInfoLayout = (LinearLayout) findViewById(R.id.about_info);
		aboutBorrowLayout = (LinearLayout) findViewById(R.id.about_borrow);
		btnLogout = (Button) this.findViewById(R.id.btnLogout);
		btnLogout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				LoginUtils.isLogin = false;
				MyLibraryActivity.this.startActivity(new Intent(
						MyLibraryActivity.this, MainPageActivity.class));
			}
		});
		aboutInfoLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MyLibraryActivity.this,
						MyInfomationActivity.class);
				startActivity(intent);
			}
		});
		aboutBorrowLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MyLibraryActivity.this,
						CurrentBorrowActivity.class);
				startActivity(intent);
			}
		});

	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// // �����˳��Ի���
	// AlertDialog isExit = new AlertDialog.Builder(this).create();
	// // ���öԻ������
	// isExit.setTitle("ϵͳ��ʾ");
	// // ���öԻ�����Ϣ
	// isExit.setMessage("ȷ��Ҫ�˳���");
	// // ���ѡ��ť��ע�����
	// isExit.setButton("ȷ��", listener);
	// isExit.setButton2("ȡ��", listener);
	// // ��ʾ�Ի���
	// isExit.show();
	//
	// }
	//
	// return false;
	//
	// }
	//
	// /** �����Ի��������button����¼� */
	// DialogInterface.OnClickListener listener = new
	// DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// switch (which) {
	// case DialogInterface.BUTTON_POSITIVE:// "ȷ��"��ť�˳�����
	// finish();
	// break;
	// case DialogInterface.BUTTON_NEGATIVE:// "ȡ��"�ڶ�����ťȡ���Ի���
	// break;
	// default:
	// break;
	// }
	// }
	// };
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			this.finish();

		}

		return false;

	}

}
