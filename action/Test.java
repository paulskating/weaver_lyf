package weaver.lyf.action;

import org.json.JSONArray;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.List;

public class Test {

    public static void main(String args[]) {
        String sip_timestamp = "2019-03-08 15:54:50.542";
        String app_key="nea@burgeon.com.cn";
        String password = "portal20";
//
        Test test = new Test();
        String password_md5 = test.MD5(password);
        String result = app_key+sip_timestamp+password_md5;
        System.out.println(test.MD5(result));
//
//        String result1 = test.buildMD5Sign(app_key,password,sip_timestamp);
//        System.out.println(result1);

//        HashMap<String, String> params = new HashMap<String, String>();
//
//        System.out.println(content);
//        params.put("transactions",content);
//        JSONArray jsonArray = new JSONArray();
//        jsonArray.put(content);
//        System.out.println(jsonArray.toString());

//        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("id","123");
//        params.put("name","Job");
//        JSONArray jsonArray = new JSONArray();
//        jsonArray.put(params);
//        System.out.println(jsonArray.toString());
//        HashMap<String, String> content = new HashMap<String, String>();
//        content.put("transactions",jsonArray.toString());
//        System.out.println(content.toString());


        HashMap params = new HashMap();

        params.put("condition","=123");

        JSONArray sendjsonArray = new JSONArray();
        sendjsonArray.put(params);

        System.out.println(sendjsonArray);


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

    public String buildMD5Sign(String appKey, String password, String time) {
        String result = "";

        HashMap<String, String> params = new HashMap<String, String>();
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        String sip_timestamp = format.format((new Date()));
        String sip_timestamp = time;

        StringBuffer sb = new StringBuffer();
        sb.append(appKey);
        sb.append(sip_timestamp);
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



}
