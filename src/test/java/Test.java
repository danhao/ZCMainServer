import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.alibaba.fastjson.JSONObject;

public class Test {

	public static void main(String[] args) throws Exception {

		// String s = "{\"code\":1,\"req\":{\"name\":\"test\"}}";
		// JSONObject json = JSON.parseObject(s);
		// System.out.println(json.getString("req"));

		// String accessKeyId = "1P160AxzpPoOG6Ej";
		// String accessKeySecret = "3ZYEqfI5JKqaQo2P9zS3HlrJIQDWHd";
		// // 以杭州为例
		// String endpoint = "http://oss-cn-shenzhen.aliyuncs.com";
		//
		// // 初始化一个OSSClient
		// OSSClient client = new OSSClient(endpoint, accessKeyId,
		// accessKeySecret);

		// 下面是一些调用代码...
		// 获取指定文件的输入流
		// File file = new File("/Users/dan/Downloads/log.txt");
		// InputStream content = new FileInputStream(file);
		//
		// // 创建上传Object的Metadata
		// ObjectMetadata meta = new ObjectMetadata();
		//
		// // 必须设置ContentLength
		// meta.setContentLength(file.length());
		//
		// // 上传Object.
		// String key = "test.log";
		// PutObjectResult result = client.putObject("zichan", key, content,
		// meta);
		//
		// // 打印ETag
		// System.out.println(result.getETag());

		// String bucketName = "zichan";
		// String key = "test.log";
		//
		// // 设置URL过期时间为1小时
		// Date expiration = new Date(new Date().getTime() + 3600 * 1000);
		//
		// // 生成URL
		// URL url = client.generatePresignedUrl(bucketName, key, expiration);
		// System.out.println(url.toString());
		// System.out.println(Base64.encodeBase64String(HMACSHA1.getSignature(Constant.POLICY,
		// Constant.OSSAccessKeySecret)));
		//
		// Debt debt = new Debt();
		// debt.setDebtorName("test");
		// debt.setId(123);
		// SimpleDebtMsg.Builder simpleDebt = SimpleDebtMsg.newBuilder();
		// PropUtil.copyProperties(simpleDebt, debt,
		// SimpleDebtMsg.Builder.getDescriptor());
		// System.out.println(simpleDebt.build().toString());

		// ErrorCode err = ErrorCode.ERR_NO_MONEY;
		// System.out.println(err.getNumber());
		// Integer i = 1;
		// Integer j = 1;
		// System.out.println(i == j);
		// SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		// Date before = format.parse("2014-02-28 10:28:00");
		// System.out.println(before);

		// UpdateReq.Builder builder = UpdateReq.newBuilder();
		// JsonFormat.merge("{\"address\":\"\"}", builder);
		// UpdateReq req = builder.build();
		// System.out.println(req.hasAddress() + " " + req.hasAccountPermit());

//		HttpClient client = new HttpClient();
//		PostMethod method = new PostMethod(
//				"https://p.apicloud.com/api/push/message");
//
//		String now = String.valueOf(System.currentTimeMillis());
//		String appKey = DigestUtils.sha1Hex("A6981212595108"+"UZ"+ "AF12E5F0-F2E2-A95A-9034-205350752240"+"UZ"+now)+"."+now;
//		
//		method.addRequestHeader("X-APICloud-AppId", "A6981212595108");
//		method.addRequestHeader("X-APICloud-AppKey", appKey);
//		method.addRequestHeader("Content-Type", "application/json;charset=utf-8");
//		
//		JSONObject json = new JSONObject();
//		json.put("title", "test123中文");
//		json.put("content", "t");
//		json.put("type", "1");
//		json.put("platform", "0");
//		
//		RequestEntity requestEntity = new StringRequestEntity(json.toJSONString(), "application/json","utf-8");
//		method.setRequestEntity(requestEntity);
////		method.setRequestBody(json.toJSONString());
//		
//		client.executeMethod(method);
//		
//		// 打印服务器返回的状态
//		System.out.println(method.getStatusLine());
//		// 打印返回的信息
//		System.out.println(method.getResponseBodyAsString());
//		// 释放连接
//		method.releaseConnection();
		
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(
				"http://119.29.139.33:33001");

		String s = "<?xml version='1.0' encoding='utf-8'?><Request><action>AccountLookup</action><id>614729464269508609</id><type>token</type></Request>";
		RequestEntity requestEntity = new StringRequestEntity(s, "application/json","utf-8");
		method.setRequestEntity(requestEntity);
		
		client.executeMethod(method);
		
		// 打印服务器返回的状态
		System.out.println(method.getStatusLine());
		// 打印返回的信息
		System.out.println(method.getResponseBodyAsString());
		// 释放连接
		method.releaseConnection();
	}

}
