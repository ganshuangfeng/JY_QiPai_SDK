package com.bolaa.douniu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.bolaa.douniu.Bean.WXAccessTokenInfo;
import com.bolaa.douniu.Bean.WXErrorInfo;
import com.bolaa.douniu.Bean.WXUserInfo;
import com.bolaa.douniu.wxapi.AppConst;
import com.bolaa.douniu.wxapi.WXEntryActivity;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXMusicObject;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import my.Util;
import my.WeChatController;

public class MainActivity extends UnityPlayerActivity {

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
    private static final String WEIXIN_ACCESS_TOKEN_KEY = "wx_access_token_key";
    private static final String WEIXIN_OPENID_KEY = "wx_openid_key";
    private static final String WEIXIN_REFRESH_TOKEN_KEY = "wx_refresh_token_key";

    public static final int Get_TOKEN = 0;
    public static final int Refresh_TOKEN = 1;
    public static final int Get_UserInfo = 2;
    public static final int Check_TOKEN = 3;

    String accessToken = "";
    String openid = "";

    public static MainActivity Instance;
    Context mContext = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Instance = this;
        mContext = this;
    }

    public void RegisterToWeChat(String appId,String appSecret)
    {
        AppConst.WEIXIN_APP_ID = appId;
        AppConst.WEIXIN_APP_SECRET = appSecret;
        WeChatController.GetInstance().RegisterToWeChat(this, appId);
        WXEntryActivity.initWeiXin(this, appId);
    }
    public void StartAc(String appId)
    {
        Toast.makeText(MainActivity.Instance, "////////////", Toast.LENGTH_SHORT).show();
    }
    public void WeChat(String param)
    {
        try {
            JSONObject jsonObject = new JSONObject(param);
            int _type =  jsonObject.getInt("type");
            WeChatController con = WeChatController.GetInstance();
            switch (_type)
            {
                case WeChatController.Type.WeiChatInterfaceType_IsWeiChatInstalled:
                    break;
                case WeChatController.Type.WeiChatInterfaceType_RequestLogin:
                    //con.WeChatLogin();
                    weiLogin();
                    break;
                case WeChatController.Type.WeiChatInterfaceType_ShareUrl:
                    ShareLinkUrl(jsonObject);
                    break;
                case WeChatController.Type.WeiChatInterfaceType_ShareImage:
                    ShareImage(jsonObject);
                    break;
                case WeChatController.Type.WeiChatInterfaceType_ShareText:
                    ShareText(jsonObject);
                    break;
                case WeChatController.Type.WeiChatInterfaceType_ShareVideo:
                    ShareVideo(jsonObject);
                    break;
                case WeChatController.Type.WeiChatInterfaceType_ShareMusic:
                    ShareMusic(jsonObject);
                    break;
            }
        }catch (Exception e) {
            UnityPlayer.UnitySendMessage("Android", "CallBack", e.toString());
        }

    }

    //????????????
    public void weiLogin() {
        WXEntryActivity.loginWeixin();
        /*// ?????????????????????????????????????????????????????????????????????access_token
        accessToken = (String) ShareUtils.getValue(this, WEIXIN_ACCESS_TOKEN_KEY,
                "none");
        openid = (String) ShareUtils.getValue(this, WEIXIN_OPENID_KEY, "");
        if (!"none".equals(accessToken)) {
            // ???access_token???????????????????????????
            isExpireAccessToken(accessToken, openid);
        } else {
            // ??????access_token
        WXEntryActivity.loginWeixin();
        }*/
    }


    public void getResponse(String code) {
        // ??????code??????????????????access_token
        getAccessToken(code);
        /*// ?????????????????????????????????????????????????????????????????????access_token??????????????????????????????????????????????????????
        accessToken = (String) ShareUtils.getValue(this, WEIXIN_ACCESS_TOKEN_KEY,
                "none");
        openid = (String) ShareUtils.getValue(this, WEIXIN_OPENID_KEY, "");
        if (!"none".equals(accessToken)) {
            // ???access_token???????????????????????????
            isExpireAccessToken(accessToken, openid);
        } else {
            // ??????access_token
            getAccessToken(code);
        }*/
    }

    /**
     * ??????????????????????????????
     */
    private void getAccessToken(String code) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + AppConst.WEIXIN_APP_ID +
                "&secret=" + AppConst.WEIXIN_APP_SECRET +
                "&code=" + code +
                "&grant_type=authorization_code";
        sendRequestWithHttpClient(url,Get_TOKEN);
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param response ??????????????????
     */
    public void processGetAccessTokenResult(String response) {
        UnityPlayer.UnitySendMessage("Android", "CallBack", response);
        Gson mGson = new Gson();
        // ???????????????????????????????????????????????????
        if (validateSuccess(response)) {
            // ??????Gson?????????????????????????????????
            WXAccessTokenInfo tokenInfo = mGson.fromJson(response, WXAccessTokenInfo.class);
            // ???????????????????????????
            saveAccessInfotoLocation(tokenInfo);
            // ??????????????????
            getUserInfo(tokenInfo.getAccess_token(), tokenInfo.getOpenid());
        } else {
            // ???????????????????????????????????????????????????
            WXErrorInfo wxErrorInfo = mGson.fromJson(response, WXErrorInfo.class);

        }
    }

    /**
     *??????????????????tokenInfo???WEIXIN_OPENID_KEY???WEIXIN_ACCESS_TOKEN_KEY???WEIXIN_REFRESH_TOKEN_KEY?????????shareprephence???
     * @param tokenInfo
     */
    private void saveAccessInfotoLocation(WXAccessTokenInfo tokenInfo) {
        ShareUtils.saveValue(WXEntryActivity.mContext,WEIXIN_OPENID_KEY,tokenInfo.getOpenid());
        ShareUtils.saveValue(WXEntryActivity.mContext,WEIXIN_ACCESS_TOKEN_KEY,tokenInfo.getAccess_token());
        ShareUtils.saveValue(WXEntryActivity.mContext,WEIXIN_REFRESH_TOKEN_KEY,tokenInfo.getRefresh_token());
    }

    /**
     * ??????????????????
     *
     * @param response ????????????
     * @return ????????????
     */
    private boolean validateSuccess(String response) {
        String errFlag = "errmsg";
        return (errFlag.contains(response) && "ok".equals(response))
                || (!errFlag.contains(response));
    }


    /**
     * ??????????????????accesstoken?????????
     *
     * @param accessToken token
     * @param openid      ????????????????????????
     */
    private void isExpireAccessToken(final String accessToken, final String openid) {
        String url = "https://api.weixin.qq.com/sns/auth?" +
                "access_token=" + accessToken +
                "&openid=" + openid;
        sendRequestWithHttpClient(url,Check_TOKEN);
    }
    /**
     * ??????????????????????????????access_token
     */
    private void refreshAccessToken() {
        // ???????????????????????????refresh_token
        final String refreshToken = (String) ShareUtils.getValue(this, WEIXIN_REFRESH_TOKEN_KEY,
                "");
        if (TextUtils.isEmpty(refreshToken)) {
            return;
        }
        // ????????????access_token???url????????????
        String url = "https://api.weixin.qq.com/sns/oauth2/refresh_token?" +
                "appid=" + AppConst.WEIXIN_APP_ID +
                "&grant_type=refresh_token" +
                "&refresh_token=" + refreshToken;
        sendRequestWithHttpClient(url,Refresh_TOKEN);
    }
    /**
     * ??????token??????????????????????????????????????????
     * @param access_token
     * @param openid
     */
    private void getUserInfo(String access_token, String openid) {
        final Gson mGson = new Gson();
        String url = "https://api.weixin.qq.com/sns/userinfo?" +
                "access_token=" + access_token +
                "&openid=" + openid;
        sendRequestWithHttpClient(url,Get_UserInfo);
    }


    //??????Handler???????????????????????????Message
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String response = "";
            switch (msg.what) {
                case Get_TOKEN:
                    response = (String) msg.obj;
                    processGetAccessTokenResult(response);
                    break;
                case Get_UserInfo:
                    response = (String) msg.obj;
                    final Gson mGson = new Gson();
                    // ???????????????????????????
                    WXUserInfo userInfo =  mGson.fromJson(response, WXUserInfo.class);
                    UnityPlayer.UnitySendMessage("Android", "CallBack", response);
                    UnityPlayer.UnitySendMessage("Android", "UserInfo", response);
                    break;
                case Check_TOKEN:
                    if (validateSuccess(response)) {
                        // accessToken?????????????????????????????????
                        getUserInfo(accessToken, openid);

                    } else {
                        // ??????????????????refresh_token?????????accesstoken
                        refreshAccessToken();
                    }
                    break;
                case Refresh_TOKEN:
                    if (validateSuccess(response)) {
                        processGetAccessTokenResult(response);

                    } else {
                        // ?????????
                        WXEntryActivity.loginWeixin();
                    }

                    break;
                default:
                    break;
            }
        }

    };

    //????????????????????????????????????url?????????????????????????????????
    private void sendRequestWithHttpClient(final String url, final int state) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //???HttpClient???????????????????????????
                //??????????????????HttpClient??????
                HttpClient httpCient = new DefaultHttpClient();
                //???????????????????????????????????????,?????????????????????????????????
                HttpGet httpGet = new HttpGet(url);

                try {
                    //???????????????????????????????????????????????????????????????
                    HttpResponse httpResponse = httpCient.execute(httpGet);
                    //????????????????????????????????????????????????????????????????????????200????????????
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        //??????????????????????????????????????????????????????entity??????
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity,"utf-8");//???entity?????????????????????????????????
                        //??????????????????Message???????????????
                        Message message = new Message();
                        message.what = state;
                        message.obj = response.toString();
                        handler.sendMessage(message);

                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    UnityPlayer.UnitySendMessage("Android", "CallBack", e.getMessage());
                }

            }
        }).start();
    }


    //????????????
    public void ShareText(JSONObject jsonObject) {
        String description = "";
        String text = "";
        boolean isCircleOfFriends = false;
        try {
            description = jsonObject.getString("description");
            text = jsonObject.getString("text");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        // ???WXTextObject?????????????????????WXMediaMessage??????
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // ?????????????????????????????????title??????????????????
//         msg.title = "Will be ignored";
        msg.description = description;
        // ????????????Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = WeChatController.Transaction.ShareText; // transaction????????????????????????????????????
        req.message = msg;
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        // ??????api???????????????????????????
        WXEntryActivity.SendReq(req);
    }

    public void ShareImage (JSONObject jsonObject) {
        boolean isCircleOfFriends = false;
        try {
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }
        Resources re = MainActivity.Instance.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));
        WXImageObject imgObj = new WXImageObject(bmp);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        // ????????????????????????
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        req.transaction = WeChatController.Transaction.ShareImage;
        req.message = msg;
        WXEntryActivity.SendReq(req);
    }

    public void ShareVideo (JSONObject jsonObject) {
        String url = "";
        String title = "";
        String description = "";
        boolean isCircleOfFriends = false;
        try {
            url = jsonObject.getString("url");
            title = jsonObject.getString("title");
            description = jsonObject.getString("description");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }

        WXVideoObject video = new WXVideoObject();
        video.videoUrl = url;

        Resources re = MainActivity.Instance.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));

        WXMediaMessage msg = new WXMediaMessage();
        msg.title = title;
        msg.description = description;
        msg.mediaObject = video;

        // ????????????????????????
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        req.transaction = WeChatController.Transaction.ShareVideo;
        req.message = msg;
        WXEntryActivity.SendReq(req);
    }

    public void ShareMusic (JSONObject jsonObject) {
        String url = "";
        String title = "";
        String description = "";
        boolean isCircleOfFriends = false;
        try {
            url = jsonObject.getString("url");
            title = jsonObject.getString("title");
            description = jsonObject.getString("description");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }
        WXMusicObject music = new WXMusicObject();
        music.musicUrl = "url";

        Resources re = MainActivity.Instance.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));

        WXMediaMessage msg = new WXMediaMessage();
        msg.title = title;
        msg.description = description;

        msg.mediaObject = music;

        // ????????????????????????
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        req.transaction = WeChatController.Transaction.ShareMusic;
        req.message = msg;
        WXEntryActivity.SendReq(req);
    }

    public void ShareLinkUrl(JSONObject jsonObject) {
        String url = "";
        String title = "";
        String description = "";
        boolean isCircleOfFriends = false;
        try {
            url = jsonObject.getString("url");
            title = jsonObject.getString("title");
            description = jsonObject.getString("description");
            isCircleOfFriends = jsonObject.getBoolean("isCircleOfFriends");
        }catch (Exception e) {
            UnityPlayer.UnitySendMessage("Android", "CallBack", "??????");
            Toast.makeText(MainActivity.Instance, e.toString(), Toast.LENGTH_SHORT).show();
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        //???WXMebpageObject ?????????????????????WXMediaMessage??????????????????????????????

        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;  //??????????????????????????????????????????????????????????????????
        //????????????
        Resources re = MainActivity.Instance.getResources();  //?????????????????????Activity  (UnityPlayerActivity._instance)??????????????????Activity

        Bitmap bmp = BitmapFactory.decodeResource(re, re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName()));
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);
//
//        int id = re.getIdentifier("app_icon", "drawable", MainActivity.Instance.getPackageName());
//        if (id == 0 )
//        {
//            Toast.makeText(MainActivity.Instance, "et app_icon fail ", Toast.LENGTH_SHORT).show();
//        }else
//        {
//            Bitmap thumb = BitmapFactory.decodeResource(re,id); //????????????32k
//            msg.thumbData = Util.bmpToByteArray(thumb, true);
//        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = WeChatController.Transaction.ShareUrl;
        req.message = msg;
        req.scene = isCircleOfFriends ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        WXEntryActivity.SendReq(req);
    }

}
