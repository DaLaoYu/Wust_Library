package com.chen.library.Login;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LoginUtils {
	public static String cookie = "";
	public static boolean isLogin=false;

	/***
	 * 
	 * @param url
	 *            ��ҳ��½����
	 * @return ����form��֮�󣬷����б�ǩ(<input name...value..>)���ݵ�HashMap<String,
	 *         String>(),��Ϊname,ֵΪvalue
	 * @throws ParseException
	 *             jsoup�����쳣
	 * @throws IOException
	 *             IO�쳣
	 */
	public static HashMap<String, String> getLoginFormData(String url) {
		Document document = Jsoup.parse(getHtml(url));
		Elements element1 = document.getElementsByTag("form");// �ҳ�����form��
		Element formElement = element1.select("[method=POST]").first();// ɸѡ���ύ����Ϊpost�ĵ�һ����
		Elements elements = formElement.select("input[name]");// �ѱ��д���name���Ե�����input��ǩȡ��

		HashMap<String, String> parmas = new HashMap<String, String>();

		for (Element temp : elements) {
			if (temp.attr("name").equals("select")) {
				parmas.put(temp.attr("name"), "bar_no");
				continue;
			}
			parmas.put(temp.attr("name"), temp.attr("value"));// ������ȡ����input��ȡ����name������Map��
		}
		System.out.println(parmas.toString());
		return parmas;
	}

	/******
	 * 
	 * @param userName
	 *            �û���
	 * @param passWord
	 *            ����
	 * @param verifyCode
	 *            ��֤��
	 * @return List<NameValuePair> ���������Ϣ
	 * @throws ParseException
	 * @throws IOException
	 */
	public static List<NameValuePair> initLoginParmas(HashMap<String, String> parmasMap, String userName,
			String passWord, String verifyCode)  {
		List<NameValuePair> parmasList = new ArrayList<NameValuePair>();
		// HashMap<String, String> parmasMap = data;
		Set<String> keySet = parmasMap.keySet();

		for (String temp : keySet) {
			if (temp.contains("number")) {
				parmasMap.put(temp, userName);
			} else if (temp.contains("passwd")) {
				parmasMap.put(temp, passWord);
			} else if (temp.contains("captcha")) {
				parmasMap.put(temp, verifyCode);
			}
		}

		Set<String> keySet2 = parmasMap.keySet();
		System.out.println("----------�������ݣ�");
		for (String temp : keySet2) {
			System.out.println(temp + " = " + parmasMap.get(temp));
		}
		for (String temp : keySet2) {
			parmasList.add(new BasicNameValuePair(temp, parmasMap.get(temp)));
		}

		return parmasList;

	}

	/****
	 * 
	 * @param url
	 *            ��½ҳ��url���ӵ�ַ
	 * @return ��½��ҳhtml�ַ�������
	 * @throws ParseException
	 *             jsoup�����쳣
	 * @throws IOException
	 *             io�쳣
	 */
	public static String getHtml(String url) {
		String result = "";
		HttpPost post = new HttpPost(url);
		if (cookie != "") {
			post.addHeader("Cookie", cookie);
		}
		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(response.getEntity(), "utf-8");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
//			Toast.makeText(context, "������������", Toast.LENGTH_SHORT).show();
		} finally {
			client.getConnectionManager().shutdown();
		}
		return result;
	}

	/***
	 * ʹ�� �û��������룬��֤�� ��½
	 * 
	 * @param userName
	 * @param passWord
	 * @param verifyCode
	 * @return ��½״̬�� 302�ɹ���200��ʧ��
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static int login(HashMap<String, String> parmasMap, String userName, String passWord, String verifyCode) {
		int statusCode=0;
		List<NameValuePair> parmasList = new ArrayList<NameValuePair>();
		try {
			parmasList = initLoginParmas(parmasMap, userName, passWord, verifyCode);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		HttpPost post = new HttpPost("http://opac.lib.wust.edu.cn:8080/reader/redr_verify.php");
		if (cookie != "") {
			post.addHeader("Cookie", cookie);
		}
		post.getParams().setParameter("http.protocol.handle-redirects", false);
		// ��ֹ�Զ��ض���Ŀ���ǻ�ȡ��һ��ResponseHeader��Cookie��Location
		post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=gbk");
		// ���ñ���ΪGBK
		try {
			post.setEntity(new UrlEncodedFormEntity(parmasList, "GBK"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		try {
			HttpResponse response = new DefaultHttpClient().execute(post);
			statusCode = response.getStatusLine().getStatusCode();
			System.out.println("״̬��= " + statusCode);
			if (statusCode == 200) {// û��ת����
				System.out.println("��½ʧ�ܣ���������������");

			} else if (statusCode == 302) {
				String location = response.getFirstHeader("location").getValue();
				// �ض����ַ��Ŀ�������ӵ���ҳ
				String mainUrl = "http://opac.lib.wust.edu.cn:8080/reader/" + location;
				// ������ҳ��ַ
				System.out.println("location=========" + mainUrl);
//				System.out.println("��ת֮���ҳ��------->" + getHtml(mainUrl));
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	

		return statusCode;
		
	}

}
