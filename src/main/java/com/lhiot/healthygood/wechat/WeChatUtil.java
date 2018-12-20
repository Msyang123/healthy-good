/**
* Copyright © 2016 SGSL
* 湖南绿航恰果果农产品有限公司
* http://www.sgsl.com 
* All rights reserved. 
*/
package com.lhiot.healthygood.wechat;

import com.leon.microx.util.Jackson;
import com.leon.microx.util.StringUtils;
import com.lhiot.healthygood.config.HealthyGoodConfig;
import com.lhiot.healthygood.domain.template.TemplateParam;
import com.lhiot.healthygood.domain.user.KeywordValue;
import com.lhiot.healthygood.type.TemplateMessageEnum;
import com.lhiot.healthygood.util.DataItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.parsing.XPathParser;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.ConnectException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;


/**
 * 微信工具类
 * 
 * @author leon
 * @version 1.0 2016年11月18日下午3:31:26
 */
@Slf4j
@Service
public class WeChatUtil {
	public final  String encoding = "UTF-8";

	/** 获取token接口(GET) */
	public final  String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={0}&secret={1}";

	/** 获取ticket接口(GET) */
	public final  String TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token={0}&type=jsapi";

	/** 获取OPEN ID (GET) */
	public final  String OPEN_ID_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={0}&secret={1}&code={2}&grant_type=authorization_code";

	/** 获取微信用户信息 (GET) */
	public final  String USER_INFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token={0}&openid={1}&lang=zh_CN";

	/** oauth2网页授权接口(GET) */
	public final  String OAUTH2_URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid={0}&redirect_uri={1}&response_type=code&scope={2}&state={3}#wechat_redirect";

	/** 获取网友授权微信用户信息 (GET) */
	public final  String OAUTH2_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token={0}&openid={1}&lang=zh_CN";

	/**通过refresh_token刷新ACCESS_TOKEN*/
	public final String OAUTH2_REFRESH_ACCESS_TOKEN="https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={0}&grant_type=refresh_token&refresh_token={1}";

	/**发送模板消息*/
	public final String SEND_TEMPLATE_MESSAGE="https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={0}";
	//public final String SEND_TEMPLATE_MESSAGE="https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send?access_token={0}";
	
	/**查询微信公众号菜单接口(GET)*/
	public final String FIND_WEIXIN_MENU="https://api.weixin.qq.com/cgi-bin/menu/get?access_token={0}";
	
	/**创建微信公众号菜单接口(POST)*/
	public final String CREATE_WEIXIN_MENU="https://api.weixin.qq.com/cgi-bin/menu/create?access_token={0}";
	
	/************微信认证登录与支付配置*******************************************/
	private final HealthyGoodConfig.WechatOauthConfig wechatConfig;

	//private ObjectMapper om = new ObjectMapper();

	public WeChatUtil(HealthyGoodConfig healthyGoodConfig) {
		this.wechatConfig = healthyGoodConfig.getWechatOauth();
	}
	
	/**
	 * 微信回调时，获取参数
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public XPathParser getParametersByWeChatCallback(final HttpServletRequest request) throws IOException {
		BufferedReader reader = request.getReader();
		StringBuffer inputString = new StringBuffer();
		String line = "";
		while ((line = reader.readLine()) != null) {
			inputString.append(line);
		}
		request.getReader().close();
		log.info("微信回调时，获取参数getParametersByWeChatCallback:"+inputString.toString());
		InputStream in = new ByteArrayInputStream(inputString.toString().getBytes());
		XPathParser xpath = new XPathParser(in);
		in.close();
		return xpath;
	}

	/**
	 * 【微信支付】返回给微信的参数
	 * 
	 * @param return_code
	 *            返回编码
	 * @param return_msg
	 *            返回信息
	 * @return
	 */
	public  String setXML(final String return_code, final String return_msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("<xml><return_code><![CDATA[").append(return_code).append("]]></return_code><return_msg><![CDATA[")
				.append(return_msg).append("]]></return_msg></xml>");
		return sb.toString();
	}

	/**
	 * 【微信支付】 将请求参数转换为xml格式的string
	 * 
	 * @param parameters
	 *            请求参数
	 * @return
	 */
	public  String getRequestXml(final SortedMap<Object, Object> parameters) {
		StringBuilder sb = new StringBuilder();
		sb.append("<xml>");
		Set<Entry<Object, Object>> es = parameters.entrySet();
		Iterator<Entry<Object, Object>> it = es.iterator();
		while (it.hasNext()) {
			Entry<Object, Object> entry = it.next();
			String k = (String) entry.getKey();
			Object v = entry.getValue();
			if ("sign".equalsIgnoreCase(k)) {
				continue;
			}
			if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k)) {
				sb.append("<").append(k).append("><![CDATA[").append(v).append("]]></").append(k).append(">");
			} else {
				sb.append("<").append(k).append(">").append(v).append("</").append(k).append(">");
			}
		}
		sb.append("<sign>").append(parameters.get("sign")).append("</sign>").append("</xml>");
		return sb.toString();
	}

	/**
	 * 获取网页授权微信用户信息
	 * 
	 * @param openId
	 * @param access_token
	 */
	public  String getOauth2UserInfo(final String openId, final String access_token) {
		String requestUrl = MessageFormat.format(OAUTH2_USER_INFO_URL, access_token, openId);
		return httpsRequest(requestUrl, "GET", null);
	}

	/**
	 * 获取微信用户信息
	 * 
	 * @param openId
	 * @param access_token
	 * @return
	 */
	public  String getUserInfo(final String openId, final String access_token) {
		String requestUrl = MessageFormat.format(USER_INFO_URL, access_token, openId);
		return httpsRequest(requestUrl, "GET", null);
	}

	/**
	 * 获取open_id 和 网页授权access_token
	 * 
	 * @param appid
	 * @param appsecrect
	 * @param code
	 * @return
	 */
	public  AccessToken getAccessTokenByCode(final String appid, final String appsecrect, final String code){
		String requestUrl = MessageFormat.format(OPEN_ID_URL, appid, appsecrect, code);
		String result = null;
		Map<String, Object> amapMap = null;
		try{
			log.info("获取accessToken:url="+requestUrl);
			result = httpsRequest(requestUrl, "GET", null);
			amapMap = Jackson.map(result);
		}catch (Exception e){
			e.printStackTrace();
			log.info("获取accessToken失败,重试第一次:url="+requestUrl);
			try {
				result = httpsRequest(requestUrl, "GET", null);
				amapMap = Jackson.map(result);
				e.printStackTrace();
			}catch (Exception e2){
				e2.printStackTrace();
				log.info("获取accessToken失败,重试第二次:url="+requestUrl);
				result = httpsRequest(requestUrl, "GET", null);
				amapMap = Jackson.map(result);
				e2.printStackTrace();
			}
		}
		log.info("WeChatUtil getToken"+result);
		AccessToken accessToken = new AccessToken();
		accessToken.setAccessToken(String.valueOf(amapMap.get("access_token")));
		Integer expiresIn=Integer.valueOf(String.valueOf(amapMap.get("expires_in")));
		if(Objects.isNull(expiresIn)){
			expiresIn=7100;
		}
		accessToken.setExpiresIn(expiresIn);
		accessToken.setRefreshToken(String.valueOf(amapMap.get("refresh_token")));
		accessToken.setOpenId(String.valueOf(amapMap.get("openid")));
		accessToken.setScope(String.valueOf(amapMap.get("scope")));
		return accessToken;
	}

	/**
	 * 获得js signature
	 * 
	 * @param jsapi_ticket
	 * @param timestamp
	 * @param nonce
	 * @param jsurl
	 * @return signature
	 */
	public  String getSignature(final String jsapi_ticket, final String timestamp, final String nonce,
			final String jsurl) {
		String[] paramArr = new String[] { "jsapi_ticket=" + jsapi_ticket, "timestamp=" + timestamp,
				"noncestr=" + nonce, "url=" + jsurl };
		Arrays.sort(paramArr);
		String content = paramArr[0].concat("&" + paramArr[1]).concat("&" + paramArr[2]).concat("&" + paramArr[3]);
		String gensignature = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digest = md.digest(content.getBytes());
			gensignature = byteToStr(digest);
		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage(), e);
		}
		if (gensignature != null) {
			return gensignature;
		} else {
			return "false";
		}
	}

	/**
	 * 获取接口JsapiTicket访问凭证
	 * 
	 * @param accessToken
	 * @return JsapiTicket
	 */
	public  JsapiTicket getJsapiTicket(final String accessToken) {
		String requestUrl = MessageFormat.format(TICKET_URL, accessToken);
		String result = httpsRequest(requestUrl, "GET", null);
		if (result == null) {
			return null;
		}
		Map<String, Object> amapMap = Jackson.map(result);
		JsapiTicket ticket = new JsapiTicket();
		ticket.setTicket(String.valueOf(amapMap.get("ticket")));
		Integer expiresIn = Integer.valueOf(String.valueOf(amapMap.get("expires_in")));
		if(Objects.isNull(expiresIn)){
			expiresIn = 7100;
		}
		ticket.setExpiresIn(expiresIn);
		return ticket;
	}

	/**
	 * 获取接口访问凭证
	 * @return Token
	 */
	public Token getToken(){
		String requestUrl = MessageFormat.format(TOKEN_URL, this.wechatConfig.getAppId(), this.wechatConfig.getAppSecret());
		String result = httpsRequest(requestUrl, "GET", null);
		Map<String, Object> amapMap = Jackson.map(result);
		log.info("WeChatUtil getToken"+result);
		Token token = new Token();
		token.setAccessToken(String.valueOf(amapMap.get("access_token")));
		Integer expiresIn=Integer.valueOf(String.valueOf(amapMap.get("expires_in")));
		if(Objects.isNull(expiresIn)){
			expiresIn=7100;
		}
		token.setExpiresIn(expiresIn);
		token.setRefreshToken(String.valueOf(amapMap.get("refresh_token")));
		return token;
 	}
	
	/**
	 * 发送模板消息
	 * @param message 
	 * @return
	 */
	public String sendTemplateMessage(String message){
		String requestUrl = MessageFormat.format(SEND_TEMPLATE_MESSAGE, this.getToken().getAccessToken());
		String result = httpsRequest(requestUrl, "POST", message);
		System.out.println(result);
		return result;
	}

	/**
	 * 构建模板消息参数
	 * @param templateMessageEnum
	 * @param dataItem
	 * @return
	 */
	public  String sendMessageToWechat(TemplateMessageEnum templateMessageEnum,String openId, DataItem dataItem) {
		if (templateMessageEnum==null)
			return null;
		//构建发送内容
		TemplateParam templateParam = new TemplateParam();
		templateParam.setTouser(openId);
		templateParam.setTemplate_id(templateMessageEnum.getTemplate_id());
		templateParam.setUrl(templateMessageEnum.getUrl());
		templateParam.setData(dataItem);
		return this.sendTemplateMessage(Jackson.json(templateParam));
	}

	/*public static void main(String[] args) {
		TemplateMessageEnum xxx =TemplateMessageEnum.APPLY_FRUIT_DOCTOR;
		xxx.setTouser("openuseridxxx");
		List<DataItem> d=new ArrayList<>();
		d.add(new DataItem("abc","val1","#456785"));
		d.add(new DataItem("abcd","val2","#456905"));
		System.out.println(sendMessageToWechat(xxx,d));
	}*/
	/**
	 * 通过refreshAccessToken获取AccessToken
	 * @param refreshAccessToken
	 * @return AccessToken
	 */
	public  AccessToken refreshAccessToken(String refreshAccessToken){
		//如果refreshAccessToken是空，就需要重新授权
		if(StringUtils.isEmpty(refreshAccessToken))
			return null;
		String requestUrl = MessageFormat.format(OAUTH2_REFRESH_ACCESS_TOKEN, this.wechatConfig.getAppId(), refreshAccessToken);
		String result = null;
		Map<String, Object> amapMap = null;
		try{
			result = httpsRequest(requestUrl, "GET", null);
			amapMap = Jackson.map(result);
		}catch (Exception e){
			e.printStackTrace();
			try{
				result = httpsRequest(requestUrl, "GET", null);
				amapMap = Jackson.map(result);
			}catch (Exception e2){
				e2.printStackTrace();
				result = httpsRequest(requestUrl, "GET", null);
				amapMap = Jackson.map(result);
			}

		}
		log.info("WeChatUtil getToken"+result);
		AccessToken accessToken = new AccessToken();
		accessToken.setAccessToken(String.valueOf(amapMap.get("access_token")));
		Integer expiresIn=Integer.valueOf(String.valueOf(amapMap.get("expires_in")));
		if(Objects.isNull(expiresIn)){
			expiresIn=7100;
		}
		accessToken.setExpiresIn(expiresIn);
		accessToken.setRefreshToken(String.valueOf(amapMap.get("refresh_token")));
		accessToken.setOpenId(String.valueOf(amapMap.get("openid")));
		accessToken.setScope(String.valueOf(amapMap.get("scope")));
		return accessToken;

	}

	/**
	 * 发送https请求
	 * 
	 * @param requestUrl
	 *            请求地址
	 * @param requestMethod
	 *            请求方式（GET、POST）
	 * @param outputStr
	 *            提交的数据
	 * @return 返回微信服务器响应的JSON信息
	 */
	public  String httpsRequest(final String requestUrl, final String requestMethod, final String outputStr) {
		try {
			TrustManager[] tm = { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) {
				}

				public void checkClientTrusted(X509Certificate[] chain, String authType) {
				}
			} };
			SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
			sslContext.init(null, tm, new java.security.SecureRandom());
			SSLSocketFactory ssf = sslContext.getSocketFactory();
			URL url = new URL(requestUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setSSLSocketFactory(ssf);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			// 设置请求方式（GET/POST）
			conn.setRequestMethod(requestMethod);
			conn.setRequestProperty("content-type", "application/x-www-form-urlencoded");
			// 当outputStr不为null时向输出流写数据
			if (null != outputStr) {
				OutputStream outputStream = conn.getOutputStream();
				// 注意编码格式
				outputStream.write(outputStr.getBytes(StandardCharsets.UTF_8));
				outputStream.close();
			}
			// 从输入流读取返回内容
			InputStream inputStream = conn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str = null;
			StringBuffer buffer = new StringBuffer();
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			// 释放资源
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			conn.disconnect();
			return buffer.toString();
		} catch (ConnectException ce) {
			log.error("连接超时：{}", ce);
		} catch (Exception e) {
			log.error("https请求异常：{}", e);
		}
		return null;
	}

	public  String urlEncodeUTF8(final String source) {
		String result = source;
		try {
			result = java.net.URLEncoder.encode(source, "utf-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 将字节数组转换为十六进制字符串
	 *
	 * @param byteArray
	 * @return
	 */
	private  String byteToStr(final byte[] byteArray) {
		String strDigest = "";
		for (int i = 0; i < byteArray.length; i++) {
			strDigest += byteToHexStr(byteArray[i]);
		}
		return strDigest;
	}

	/**
	 * 将字节转换为十六进制字符串
	 *
	 * @param mByte
	 * @return
	 */
	private  String byteToHexStr(final byte mByte) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] tempArr = new char[2];
		tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
		tempArr[1] = Digit[mByte & 0X0F];
		String s = new String(tempArr);
		return s;
	}


	/**
	 * 取出一个指定长度大小的随机正整数.
	 * 
	 * @param length
	 *            int 设定所取出随机数的长度。length小于11
	 * @return int 返回生成的随机数。
	 */
	public  int buildRandom(final int length) {
		int num = 1;
		double random = Math.random();
		if (random < 0.1) {
			random = random + 0.1;
		}
		for (int i = 0; i < length; i++) {
			num = num * 10;
		}
		return (int) ((random * num));
	}
}
