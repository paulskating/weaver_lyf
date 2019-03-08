package weaver.lyf.action;

import org.json.JSONArray;
import weaver.zwl.common.ToolUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


/**
 * 接口工具类
 *
 * @author jason_min
 */

public class UtilJason extends ToolUtil {


    public String httpPostToERP(String content, String url) {
        writeLog("content:" + content);
        String result = "";
        URL u;
        HttpURLConnection connection = null;
        BufferedReader in = null;
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

            out.write(content);
            out.flush();
            out.close();

            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            result = result.trim();
        } catch (Exception e1) {
            e1.printStackTrace();

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


    public String buildMD5Sign(String appKey, String password, String timestamp) {
        String result = "";

        StringBuffer sb = new StringBuffer();
        sb.append(appKey);
        sb.append(timestamp);
        sb.append(MD5(password));
        try {
            String value = sb.toString();
            result = MD5(value);
        } catch (Exception e) {
            result = "encryptMD5 Wrong";
            e.printStackTrace();
        }
        return result;
    }


    public String buildExecuteParams(String appKey, String timestamp, String sipSign, HashMap params){
        String result = "";
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(params);
        HashMap<String, String> content = new HashMap<String, String>();
        content.put("transactions", jsonArray.toString());
        String transaction = content.toString();
        result = transaction.substring(1, transaction.length()-1)+ "&sip_appkey="+appKey+"&sip_timestamp=" + timestamp + "&sip_sign="
                + sipSign + "";
        return result;
    }


    public String MD5(String s) {
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
