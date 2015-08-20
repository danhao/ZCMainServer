import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TestClient {

	public static final String POST_URL = "http://127.0.0.1:30001";

	public static String post(String req) throws IOException {
		// Post请求的url，与get不同的是不需要带参数
		URL postUrl = new URL(POST_URL);

		// 打开连接
		HttpURLConnection connection = (HttpURLConnection) postUrl
				.openConnection();

		// 打开读写属性，默认均为false
		connection.setDoOutput(true);
		connection.setDoInput(true);

		// 设置请求方式，默认为GET
		connection.setRequestMethod("POST");

		// Post 请求不能使用缓存
		connection.setUseCaches(false);
		connection.setInstanceFollowRedirects(true);

		// 配置连接的Content-type，配置为application/x-
		// www-form-urlencoded的意思是正文是urlencoded编码过的form参数，下面我们可以看到我们对正文内容使用URLEncoder.encode进行编码
		connection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");

		// 连接，从postUrl.openConnection()至此的配置必须要在 connect之前完成，
		// 要注意的是connection.getOutputStream()会隐含的进行调用 connect()，所以这里可以省略
		DataOutputStream out = new DataOutputStream(
				connection.getOutputStream());

		// 正文内容其实跟get的URL中'?'后的参数字符串一致

		// DataOutputStream.writeBytes将字符串中的16位的 unicode字符以8位的字符形式写道流里面
		out.writeBytes(URLEncoder.encode(req, "utf-8"));
		out.flush();
		out.close(); // flush and close

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));

		String line;
		String ret = "";

		while ((line = reader.readLine()) != null) {
			ret += line;
		}

		reader.close();
		return ret;
	}

	public static void main(String[] args) {
		try {
			String createUser = "{'code':2,'req':{'mobile':'133','email':'中文'}}";
			String login = "{'code':1,'req':{'mobile':'127','passwd':'1111'}}";
			String bid = "{'code':104,'pid':607007323960905728,'sid':'9b04640a-63ac-49fd-acdc-e948e936d752','req':{'id':'619787660692557824','rate':30}}";
			String createDebt = "{'code':100,'pid':606342446812499968,'sid':'8c9e6ff5-dd09-4883-9499-40ad14d353c5','req':{type:2,'money':1014,'creditorName':'haha','contacts':{'name':'11','phone':'test'}}}";
			String listViewDebts = "{'code':103}"; 
			String listDebts = "{'code':101,'pid':'619797168072429568','sid':'a0030677-1d65-4bf2-b7d3-891c6e2ca667','req':{type:2,state:1,moneyLow:100,moneyUp:1000000,page:20}}";
			String viewDebt = "{'code':102,'pid':'607007323960905728','sid':'9b04640a-63ac-49fd-acdc-e948e936d752','req':{'param':'619787660692557824'}}";
			String addMessage = "{'code':106,'pid':'607007323960905728','sid':'9b04640a-63ac-49fd-acdc-e948e936d752','req':{'id':'619787660692557824','files':[{'id':'11','name':'xxx'}]}}";
			String listSelfDebts = "{'code':107,'pid':'607120585767522305','sid':'76085f2b-c46f-4575-855e-6fed59e54d47','req':{'param':'1'}}";
			String updatePlayer = "{'code':6,'pid':608932921935400961,'sid':'e4162113-61c0-4e8a-83b2-974cc3f1539a','req':{'address':'xxcxx'}}";
			String changePwdOne = "{'code':9,'req':{'email':'tohaodan@163.com'}}";
			String changePwdTwo = "{'code':10,'req':{'email':'tohaodan@163.com','code':391919}}";
			String changePwdThree = "{'code':11,'req':{'email':'tohaodan@163.com','code':391919, 'passwd':'333'}}";
			String listMoneyHistory = "{'code':13,'pid':'611160765428142080','sid':'c6b07848-835e-4dab-a847-f2232be581bd','req':{'timeFrom':0,'timeTo':2000000000}}";
			String createOrder = "{'code':200,'pid':'614766165704577025','sid':'3cbf0fa0-00cd-4aa2-a94b-6306dd694d3f','req':{"
					+ "'version':'v1.0',"
					+ "'pickupUrl':'http://203.195.133.243/#/member/',"
					+ "'language':'1',"
					+ "'inputCharset':'1',"
					+ "'payType':'0',"
					+ "'signType':'1',"
					+ "'orderAmount':'200',"
					+ "'orderCurrency':'0',"
					+ "'orderExpireDatetime':'60',"
					+ "'payerTelephone':'',"
					+ "'payerEmail':'',"
					+ "'payerName':'',"
					+ "'payerIDCard':'',"
					+ "'pid':'',"
					+ "'productName':'',"
					+ "'productId':'',"
					+ "'productNum':'',"
					+ "'productPrice':'',"
					+ "'productDesc':'',"
					+ "'ext2':'',"
					+ "'extTL':'',"
					+ "'issuerId':'',"
					+ "'pan':'',"
					+ "'tradeNature':''}}";

			String drawCash = "{'code':201,'pid':'608932921935400961','sid':'e4162113-61c0-4e8a-83b2-974cc3f1539a','req':{'amount':100}}";
			String updatPlayer = "{'code':6,'pid':'608932921935400961','sid':'e4162113-61c0-4e8a-83b2-974cc3f1539a','req':{'idFile':{'id':'615526745793105920/252d1021-bcc6-2962-f899-e74e54444a8e.png','name':'test'}}}";
			String bidWin = "{'code':105,'pid':619034302981607425,'sid':'f0f171dd-6ecf-4266-a619-79ffe1381b9c','req':{'debtId':'619787660692557824','playerId':'614728266409840640'}}";
			String batchBbid = "{'code':108,'pid':619797168072429568,'sid':'a0030677-1d65-4bf2-b7d3-891c6e2ca667','req':{'id':['619787660692557824'],'bond':101,'rate':10}}";
			String upload = "{'code':18,'pid':619797168072429568,'sid':'a0030677-1d65-4bf2-b7d3-891c6e2ca667','req':{'id':['1111'],'name':'test'}}";
			String returnDebt = "{'code':109,'pid':619797168072429568,'sid':'a0030677-1d65-4bf2-b7d3-891c6e2ca667','req':{'param':'619787660692557824'}}";
			String applyEndDebt = "{'code':110,'pid':614729464269508609,'sid':'d0423b45-7491-4412-a401-db459cdb39d9','req':{'param':'625289450133393408'}}";
			String addContact = "{'code':111,'pid':614729464269508609,'sid':'d0423b45-7491-4412-a401-db459cdb39d9','req':{'id':'625289450133393408','phone':'123','name':'test','type':1,'memo':'xxx'}}";
			String stat = "{'code':112,'pid':606342446812499968,'sid':'8c9e6ff5-dd09-4883-9499-40ad14d353c5','req':{}}";

			String rsp = post(createDebt);
			System.out.println(rsp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}