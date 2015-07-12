package com.chen.library.NetWorkState;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
/**
 * 
 * @author chen
 * 	// ��ȡ����״̬��Ҫ��һ����Ȩ�ޣ������Ӧ�������ļ�
 * �����ȡ����״̬
 *
 */
public class NetworkUtils {

	
	
	/****
	 * 
	 * @param context �����Ķ���
	 * @return boolean��������������״̬
	 */
	public static  boolean getNetWorkState(Context context) {
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) {
			// do nothing;do what is required to do;
			return true;
		} else {
			return false;
		}
	}

}
