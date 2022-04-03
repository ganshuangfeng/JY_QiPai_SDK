package com.jingyu.rrddz.wxapi;


import com.sdk.my.WeChatController;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendAuth;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONObject;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, WeChatController.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                goToShowMsg((ShowMessageFromWX.Req) req);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp resp) {
        JSONObject json = new JSONObject();
        try{
            json.put("errCode",resp.errCode);
            json.put("transaction", resp.transaction);
            switch (resp.transaction)
            {
                case WeChatController.Transaction.RequestLogin:
                    SendAuth.Resp auth = (SendAuth.Resp)resp;
                    json.put("userName", auth.userName);
                    json.put("state", auth.state);
                    json.put("resultUrl", auth.resultUrl);
                    json.put("token", auth.token);
                    break;
                case WeChatController.Transaction.ShareImage:
                case WeChatController.Transaction.ShareMusic:
                case WeChatController.Transaction.ShareText:
                case WeChatController.Transaction.ShareUrl:
                case WeChatController.Transaction.ShareVideo:

                    break;
            }
        }catch (Exception e)
        {

        }
        UnityPlayer.UnitySendMessage("Android", "CallBack", json.toString());

        finish();///
    }

    public void OnRespAuth(SendAuth.Resp resp)
    {
        String msg = resp.userName+","+resp.state+","+resp.errCode + "," + resp.resultUrl;
//        UnityPlayer.UnitySendMessage("CallBackCube","WeixinLoginCallBack",msg);
        UnityPlayer.UnitySendMessage("Android", "CallBack", "msg");
    }

    private void goToGetMsg() {
//        Intent intent = new Intent(this, GetFromWXActivity.class);
//        intent.putExtras(getIntent());
//        startActivity(intent);
//        finish();
    }

    private void goToShowMsg(ShowMessageFromWX.Req showReq) {
        WXMediaMessage wxMsg = showReq.message;
        WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;

        StringBuffer msg = new StringBuffer(); // ��֯һ������ʾ����Ϣ����
        msg.append("description: ");
        msg.append(wxMsg.description);
        msg.append("\n");
        msg.append("extInfo: ");
        msg.append(obj.extInfo);
        msg.append("\n");
        msg.append("filePath: ");
        msg.append(obj.filePath);

        finish();
    }
}