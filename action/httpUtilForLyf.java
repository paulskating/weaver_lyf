package weaver.lyf.action;


import weaver.zwl.common.ToolUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.net.URL;

/**
 * 丽婴房Http接口工具类
 * 
 * @author ywg
 */
public class httpUtilForLyf extends ToolUtil {
	/**
	 * 调用ERPhttp接口
	 *
	 */
	public String httpPostForbj(String content) {

        writeLog("content:"+content);
	    String result = "";
        URL u = null;
        HttpURLConnection connection = null;
        BufferedReader in = null;
        String url=getSystemParamValue("erp_url");
        writeLog("url:"+url);
        try {
            u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.connect();

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(),
                "utf-8");

            String buildExecuteParams = buildExecuteParams(content);

           writeLog("buildExecuteParams:" + buildExecuteParams);

            out.write(buildExecuteParams);
            out.flush();
            out.close();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            result = result.trim();
            writeLog("result:"+result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
	}

    private String buildExecuteParams(String content) {
        String back="";
        HashMap<String, String> params = new HashMap<String, String>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String callTimeStamp = format.format((new Date()));
        String buildCallRestfulSign = buildCallRestfulSign(callTimeStamp);
        writeLog("buildCallRestfulSign:" + buildCallRestfulSign);
        params.put("transactions", content);
        String appkey=getSystemParamValue("erp_appkey");
        back = params.toString().substring(1, params.toString().length() - 1)
            + "&sip_appkey="+appkey+"&sip_timestamp=" + callTimeStamp + "&sip_sign="
            + buildCallRestfulSign + "";
        return back;
    }

    private String buildCallRestfulSign(String callTimeStamp) {
        StringBuilder sb = new StringBuilder();
        String appkey=getSystemParamValue("erp_appkey");
        String password=getSystemParamValue("erp_password");
        writeLog("ERP_appkey:"+appkey+",password:"+password);
        sb.append(appkey);
        sb.append(callTimeStamp);
        String encryptAppSecret = MD5(password);
        sb.append(encryptAppSecret);
        String sipSign;
        try {
            String value = sb.toString();
            sipSign = MD5(value);
        } catch (Exception e) {
            sipSign = "encryptMD5 Wrong";
            e.printStackTrace();
        }
        return sipSign;
    }

    private String MD5(String s) {
        String r = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            r = buf.toString();
        } catch (Exception e) {
        }
        return r;
	}
}
