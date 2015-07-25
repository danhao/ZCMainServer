
import java.text.SimpleDateFormat;
import java.util.Date;

import com.aliyun.oss.OSSClient;
import com.googlecode.protobuf.format.JsonFormat;
import com.zc.web.message.player.UpdateReqProto.UpdateReq;

public class Test {

	public static void main(String[] args) throws Exception {

		// String s = "{\"code\":1,\"req\":{\"name\":\"test\"}}";
		// JSONObject json = JSON.parseObject(s);
		// System.out.println(json.getString("req"));

//		String accessKeyId = "1P160AxzpPoOG6Ej";
//		String accessKeySecret = "3ZYEqfI5JKqaQo2P9zS3HlrJIQDWHd";
//		// 以杭州为例
//		String endpoint = "http://oss-cn-shenzhen.aliyuncs.com";
//
//		// 初始化一个OSSClient
//		OSSClient client = new OSSClient(endpoint, accessKeyId, accessKeySecret);

		// 下面是一些调用代码...
	    // 获取指定文件的输入流
//	    File file = new File("/Users/dan/Downloads/log.txt");
//	    InputStream content = new FileInputStream(file);
//
//	    // 创建上传Object的Metadata
//	    ObjectMetadata meta = new ObjectMetadata();
//
//	    // 必须设置ContentLength
//	    meta.setContentLength(file.length());
//
//	    // 上传Object.
//	    String key = "test.log";
//	    PutObjectResult result = client.putObject("zichan", key, content, meta);
//
//	    // 打印ETag
//	    System.out.println(result.getETag());
		
//		String bucketName = "zichan";
//		String key = "test.log";
//
//		// 设置URL过期时间为1小时
//		Date expiration = new Date(new Date().getTime() + 3600 * 1000);
//
//		// 生成URL
//		URL url = client.generatePresignedUrl(bucketName, key, expiration);
//		System.out.println(url.toString());
//		System.out.println(Base64.encodeBase64String(HMACSHA1.getSignature(Constant.POLICY, Constant.OSSAccessKeySecret)));
//		
//		Debt debt = new Debt();
//		debt.setDebtorName("test");
//		debt.setId(123);
//		SimpleDebtMsg.Builder simpleDebt = SimpleDebtMsg.newBuilder();
//		PropUtil.copyProperties(simpleDebt, debt, SimpleDebtMsg.Builder.getDescriptor());
//		System.out.println(simpleDebt.build().toString());
		
//		ErrorCode err = ErrorCode.ERR_NO_MONEY;
//		System.out.println(err.getNumber());
//		Integer i = 1;
//		Integer j = 1;
//		System.out.println(i == j);
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//		Date before = format.parse("2014-02-28 10:28:00");
//		System.out.println(before);

		UpdateReq.Builder builder = UpdateReq.newBuilder();
		JsonFormat.merge("{\"address\":\"\"}", builder);
		UpdateReq req = builder.build();
		System.out.println(req.hasAddress() + " " + req.hasAccountPermit());

	}

}
