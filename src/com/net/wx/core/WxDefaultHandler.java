package com.net.wx.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.net.wx.Constants;
import com.net.wx.common.WxMsgType;
import com.net.wx.service.ArticleService;
import com.net.wx.util.AesException;
import com.net.wx.util.SHA1;
import com.net.wx.vo.OutPutMsg;
import com.net.wx.vo.PicInfo;
import com.net.wx.vo.ReceiveMsg;


/**
 * 默认的微信消息处理器
 */
public class WxDefaultHandler implements WxHandler {

    private static final Logger log = LoggerFactory.getLogger(WxDefaultHandler.class);
    
    
    @Override
    public boolean check(String token,
                        String signature,
                        String timestamp,
                        String nonce) throws AesException {

        if (null == signature
                || signature.length() > 128
                || null == timestamp
                || timestamp.length() > 128
                || null == nonce
                || nonce.length() > 128) {
            log.error("验证签名参数失败!!!");
            return false;
        }

        if (log.isInfoEnabled()) {
           log.info("微信接入URL验证成功...");
        }
        String s = SHA1.calculate(token, timestamp, nonce);
        if (s.equals(signature)){
            return true;
        }

        return false;
    }


    @Override
    public OutPutMsg def(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        //进入未识别关键字
//    	WxUnknowKetword wxUnknowKetword = getWxUnknowKeword();
//    	if(null != wxUnknowKetword){
//    		setOutPutMsg(wxUnknowKetword.getUwReplyType(),wxUnknowKetword.getUwReplyContent(),om);
//    	}else{
//	        om.setMsgType(WxMsgType.text.name());
//	        om.setContent("系统正在维护中,请稍候再试[微笑].");
//    	}
//        if (log.isInfoEnabled()) {
//            log.info("微信新类型消息!!!");
//        }
        return om;
    }

 

    @Override
    public OutPutMsg image(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        om.setMsgType(rm.getMsgType());
        om.setMediaId(rm.getMediaId());
        if (log.isInfoEnabled()) {
            log.info("接收到微信图像消息...");
        }
        return om;
    }

    @Override
    public OutPutMsg voice(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        if (null != rm.getRecognition()) {
            om.setMsgType(WxMsgType.text.name());
            om.setContent("Your voice message has been received..[Smile]\nContent as："+rm.getRecognition());
        } else {
            om.setMsgType(rm.getMsgType());
            om.setMediaId(rm.getMediaId());
        }
        if (log.isInfoEnabled()) {
            log.info("接收到音频消息...");
        }
        return om;
    }

    @Override
    public OutPutMsg video(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        om.setMsgType(rm.getMsgType());
        om.setMediaId(rm.getMediaId());
        if (log.isInfoEnabled()) {
            log.info("接收到视频消息...");
        }
        return om;
    }

    @Override
    public OutPutMsg location(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        om.setMsgType(WxMsgType.text.name());
        om.setContent("Your current location:"+rm.getLabel()+
                ",coordinate:["+rm.getLatitude()+","+rm.getLongitude()+
                "],Map zoom level:"+rm.getScale());
        if (log.isInfoEnabled()) {
            log.info("接收到地理位置消息...");
        }
        return om;
    }

    @Override
    public OutPutMsg link(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        om.setMsgType(WxMsgType.text.name());
        om.setContent(rm.getTitle()+"\n<a href=\""+rm.getUrl()+"\">Click to open</a>");
        if (log.isInfoEnabled()) {
            log.info("接收到链接消息...");
        }
        return om;
    }

    @Override
    public OutPutMsg eClick(ReceiveMsg rm, String dpid) {
    	OutPutMsg om = new OutPutMsg(rm);
    	int articleId = Integer.parseInt(rm.getEventKey());
		JSONObject obj = new JSONObject();
		obj.put("fromUserName", rm.getFromUserName());
		obj.put("toUserName", rm.getToUserName());
		obj.put("dpid", dpid);
		om = ArticleService.getCunjiankang(articleId,obj);
        //om.setMsgType(WxMsgType.text.name());
        //om.setContent("MENU_CLICK:"+rm.getEventKey());
        if (log.isInfoEnabled()) {
            log.info("接收到菜单点击消息...");
        }
        System.out.println("om---->"+om);
        return om;
    }

    @Override
    public void eView(ReceiveMsg rm) {
        log.info("接收到菜单视图跳转消息...");
    }

    @Override
    public OutPutMsg eSub(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
    	
	        om.setMsgType(WxMsgType.text.name());
	        om.setContent("Welcome platform system.");
        
        if (log.isInfoEnabled()) {
            log.info("接收到订阅消息...");
        }
        return om;
    }

    @Override
    public void eUnSub(ReceiveMsg rm) {
        if (log.isInfoEnabled()) {
            log.info("接收到退订消息...");
        }
    }

    @Override
    public OutPutMsg eScan(ReceiveMsg rm) {
        if (log.isInfoEnabled()) {
            log.info("接收到扫描消息...");
        }
        return null;
    }

    @Override
    public OutPutMsg eLocation(ReceiveMsg rm) {
        if (log.isInfoEnabled()) {
            log.info("接收到地理位置消息...");
        }
        return null;
    }

    @Override
    public OutPutMsg eScanCodePush(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        om.setMsgType(WxMsgType.text.name());
        String content = "You use QR code scanning menu["+rm.getEventKey()+"],Scan results for: "+rm.getScanResult();
        om.setContent(content);
        if (log.isInfoEnabled()) {
            log.info("接收到二维码扫描事件消息...");
        }
        return om;
    }

    @Override
    public OutPutMsg eScanCodeWait(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        om.setMsgType(WxMsgType.text.name());
        String content = "You use the scan to wait for the menu["+rm.getEventKey()+"],Scan results for: "+rm.getScanResult();
        om.setContent(content);
        if (log.isInfoEnabled()) {
            log.info("接收到扫码推事件且弹出“消息接收中”提示消息...");
        }
        return om;
    }

    @Override
    public OutPutMsg ePicSysPhoto(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        om.setMsgType(WxMsgType.text.name());
        String content = "You use the system to take pictures["+rm.getEventKey()+"]Co hair"+rm.getCount()+"Zhang pictures,PictureMD5Value: ";
        for (PicInfo pic : rm.getPicList()) {
            content += pic.getPicMd5Sum() + ", ";
        }
        om.setContent(content.substring(0, content.lastIndexOf(",")));
        if (log.isInfoEnabled()) {
            log.info("接收到菜单弹出系统拍照发图消息...");
        }
        return om;
    }

    @Override
    public OutPutMsg ePicPhotoOrAlbum(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        om.setMsgType(WxMsgType.text.name());
        String content = "You used to take pictures or album["+rm.getEventKey()+"]Co hair"+rm.getCount()+"Zhang pictures,PictureMD5Value: ";
        for (PicInfo pic : rm.getPicList()) {
            content += pic.getPicMd5Sum() + ", ";
        }
        om.setContent(content.substring(0, content.lastIndexOf(",")));
        if (log.isInfoEnabled()) {
            log.info("接收到菜单弹出拍照或者相册发图消息...");
        }
        return om;
    }

    @Override
    public OutPutMsg ePicWeixin(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        om.setMsgType(WxMsgType.text.name());
        String content = "You use WeChat album["+rm.getEventKey()+"]Co hair"+rm.getCount()+"Zhang pictures,PictureMD5Value: ";
        for (PicInfo pic : rm.getPicList()) {
            content += pic.getPicMd5Sum() + ", ";
        }
        om.setContent(content.substring(0, content.lastIndexOf(",")));
        if (log.isInfoEnabled()) {
            log.info("接收到菜单微信相册发图消息...");
        }
        return om;
    }

    @Override
    public OutPutMsg eLocationSelect(ReceiveMsg rm) {
        OutPutMsg om = new OutPutMsg(rm);
        om.setMsgType(WxMsgType.text.name());
        om.setContent("Menu value:"+rm.getEventKey()+",Your current location:"+rm.getLabel()+
                ",coordinate:["+rm.getLatitude()+","+rm.getLongitude()+
                "],Map zoom level:"+rm.getScale()+",Moments:"+rm.getPoiName());
        if (log.isInfoEnabled()) {
            log.info("接收到菜单地理位置消息...");
        }
        return om;
    }

    @Override
    public void eTemplateFinish(ReceiveMsg rm) {
       if (log.isInfoEnabled()) {
            log.info("接收到模板推送消息...");
        }
    }

    @Override
    public void eSendJobFinish(ReceiveMsg rm) {
        if (log.isInfoEnabled()) {
            log.info("接收到群发推送消息...");
        }
    }

    @Override
    public void eComponentVerifyTicket(ReceiveMsg rm) {
        if (log.isDebugEnabled()) {
            log.info("接收到微信开放平台推送的组件TicketMessage...");
        }
    }

    @Override
    public void eUnAuthorizerMP(ReceiveMsg rm) {
        if (log.isDebugEnabled()) {
            log.info("接收到微信开放平台推送的公众号取消授权消息...");
        }
    }
    
    //设置回复的消息内容 
    public void setOutPutMsg(String msgType,String msgContent,OutPutMsg om){
    	if(Constants.SEND_TEXT_TYPE.equals(msgType)){
    		om.setMsgType(WxApi.TEXT);
    		om.setContent(msgContent);
    	}else if(Constants.SEND_MPNEWS_TYPE.equals(msgType)){
    		om.setMsgType(WxApi.NEWS);
    		//获取图文消息的数据
//    		WxPicTxtMsgArticle wxPtma = getWxptma(msgContent);
//    		if(null != wxPtma){
//    			WxPicMsg wxPm = getWxpm(wxPtma.getPicMediaId());
//    			if(null != wxPm){
//    				Article art = new Article(wxPtma.getPtmaTitle(),wxPtma.getPtmaSummary(),wxPm.getMediaUrl(),wxPtma.getPtmaUrl());
//    				om.getArticles().add(art);
//    			}
//    		}
    	}else if(Constants.SEND_IMAGE_TYPE.equals(msgType)){
    		om.setMsgType(WxApi.IMAGE);
    		om.setMediaId(msgContent);
    	}
    }


	@Override
	public OutPutMsg text(ReceiveMsg rm,String dpid) {
		// TODO Auto-generated method stub
		
		OutPutMsg om = new OutPutMsg(rm);
    	int articleId = Integer.parseInt(rm.getContent());
		JSONObject obj = new JSONObject();
		obj.put("fromUserName", rm.getFromUserName());
		obj.put("toUserName", rm.getToUserName());
		obj.put("dpid", dpid);
		om = ArticleService.getCunjiankang(articleId,obj);
        //om.setMsgType(WxMsgType.text.name());
        //om.setContent("MENU_CLICK:"+rm.getEventKey());
        if (log.isInfoEnabled()) {
            log.info("接收到菜单点击消息...");
        }
        System.out.println("om---->"+om);
        return om;
	}
    
    //获取关注欢迎语消息的单条记录 
    
   
}