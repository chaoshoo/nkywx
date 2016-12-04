package com.net.wx.core;

import com.net.wx.common.*;
import com.net.wx.util.*;
import com.net.wx.vo.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * WeChat common message interaction
 */
public class WxBase {

    private static final Logger log = LoggerFactory.getLogger(WxBase.class);
    // 消息模式(默认明文)
    private boolean aesEncrypt = false;
    // 定义公众号信息
    private MPAct mpAct;
    // 微信加密
    private WXBizMsgCrypt wxInMsgCrt;
    // XML解析准备
    private SAXParserFactory factory = SAXParserFactory.newInstance();
    private SAXParser xmlParser;
    private XMLHandler xmlHandler = new XMLHandler();
    // 微信交互参数
    private String signature;
    private String msgSignature;
    private String timeStamp;
    private String nonce;
    private String echostr;
    // 微信消息流
    private InputStream wxInMsg;
    // 微信消息处理器
    private WxHandler wxHandler;
    // 解析/响应微信消息
    private ReceiveMsg rm;
    private OutPutMsg om;    

    public WxBase() {
        try {
            xmlParser = factory.newSAXParser();
        } catch (ParserConfigurationException e) {
            log.error("SAXParse configuration file failed!!!");
            log.error(e.getLocalizedMessage(), e);
        } catch (SAXException e) {
            log.error("SAXabnormal!!!");
            log.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * WeChat basic function parameter initialization
     *
     * @param req request
     */
    public void init(HttpServletRequest req) {
        // 请求编码设置
        try {
            req.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("When setting up the WeChat server request for editing!!!");
            log.error(e.getLocalizedMessage(), e);
        }
        // 获取各请求参数值
        String sign = req.getParameter("signature");
        String time = req.getParameter("timestamp");
        String nonce = req.getParameter("nonce");
        String echo = req.getParameter("echostr");
        String encrypt = req.getParameter("encrypt_type");
        // 设置各参数值
        setSignature(sign);
        setTimeStamp(time);
        setNonce(nonce);
        setEchostr(echo);
        // 判断是否启用AES加密
        if ("aes".equals(encrypt)) {
            String msgSign = req.getParameter("msg_signature");
            setMsgSignature(msgSign);
            setAesEncrypt(true);
        }
        // 读取微信消息
        try {
            InputStream wxInMsg = req.getInputStream();
            this.wxInMsg = wxInMsg;
        } catch (IOException e) {
            log.error("Exception occurs when the WeChat message is received!!!");
            log.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * WeChatURLAccess check
     *
     * @return Random character
     * @throws AesException
     */
    public String check() throws AesException {

        boolean check = wxHandler.check(this.mpAct.getToken(),
                this.signature, this.timeStamp, this.nonce);
        return check ? echostr : "";
    }

    /**
     * WeChat news processing
     *
     * @return Reply message
     * @throws Exception
     */
    public String handler() throws Exception {
        // 释放缓存
        clear();
        String reply = "";
        this.rm = convert2VO(this.wxInMsg);
        System.out.println("====handler MsgInfo :"+this.rm.toString());
//        this.om.setToUserName(this.rm.getToUserName());
//        this.om.setFromUserName(this.rm.getFromUserName());
//        this.om.setCreateTime(this.rm.getCreateTime());
        // 处理消息
        String msg_type = this.rm.getMsgType();
        System.out.println("msg_type------------>"+msg_type);
        if ("event".equals(msg_type)) {
            this.om = handlerEvent();
        }else if("VIEW".equalsIgnoreCase(msg_type)){
        	wxHandler.eView(this.rm);
        }else if("subscribe".equalsIgnoreCase(msg_type)){
        	om = wxHandler.eSub(this.rm);
        } else {
            this.om = handlerMsg();
        }
        // 输出消息
        if (null != this.om) {
            reply = this.convert2XML(this.om);
        }
        return reply;
    }

    /**
     * Handle common messages
     *
     * @return Reply message entity
     * @throws Exception
     */
    private OutPutMsg handlerMsg() throws Exception {
        if (log.isInfoEnabled()) {
            log.info("[MPSDK4J-{}]Handle common messages...", MPSDK4J.version());
        }
        OutPutMsg om = null;
        WxMsgType type = WxMsgType.valueOf(this.rm.getMsgType());
        System.out.println("type-------------->"+type);
        switch (type) {
            case text:
                om = wxHandler.text(this.rm, mpAct.getDpid());
                break;
            case CLICK:
            	om = wxHandler.eClick(this.rm, mpAct.getDpid());
                break;
            //除了文本消息，其他消息都作为未识别处理
//            case image:
//                om = wxHandler.image(this.rm);
//                break;
//            case voice:
//                om = wxHandler.voice(this.rm);
//                break;
//            case video:
//                om = wxHandler.video(this.rm);
//                break;
//            case location:
//                om = wxHandler.location(this.rm);
//                break;
//            case link:
//                om = wxHandler.link(this.rm);
//                break;
            default:
                om = wxHandler.def(this.rm);
                break;
        }
        return om;
    }

    /**
     * Handle event push messages
     *
     * @return Reply message entity
     * @throws Exception
     */
    private OutPutMsg handlerEvent() throws Exception {
        if (log.isInfoEnabled()) {
            log.info("[MPSDK4J-{}]Handle event push messages...", MPSDK4J.version());
        }
        OutPutMsg om = null;
        WxEventType  type = WxEventType.valueOf(this.rm.getEvent());
        switch (type) {
            case subscribe:
                om = wxHandler.eSub(this.rm);
                break;
            case unsubscribe:
                wxHandler.eUnSub(this.rm);
                break;
            case SCAN:
                om = wxHandler.eScan(this.rm);
                break;
            case CLICK:
                om = wxHandler.eClick(this.rm, mpAct.getDpid());
                break;
            case VIEW:
                wxHandler.eView(this.rm);
                break;
            case scancode_push:
                om = wxHandler.eScanCodePush(this.rm);
                break;
            case scancode_waitmsg:
                om = wxHandler.eScanCodeWait(this.rm);
                break;
            case pic_sysphoto:
                om = wxHandler.ePicSysPhoto(this.rm);
                break;
            case pic_photo_or_album:
                om = wxHandler.ePicPhotoOrAlbum(this.rm);
                break;
            case pic_weixin:
                om = wxHandler.ePicWeixin(this.rm);
                break;
            case location_select:
                om = wxHandler.eLocationSelect(this.rm);
                break;
            case LOCATION:
                om = wxHandler.eLocation(this.rm);
                break;
            case TEMPLATESENDJOBFINISH:
                wxHandler.eTemplateFinish(this.rm);
                break;
            case MASSSENDJOBFINISH:
                wxHandler.eSendJobFinish(this.rm);
                break;
            default:
                om = wxHandler.def(this.rm);
                break;
        }
        return om;
    }


    /**
     * Handle WeChat open platform push messages
     *
     * @return Default return"success"
     * @throws Exception
     */
    public String handlerPush() throws Exception {
        if (log.isInfoEnabled()) {
            log.info("[MPSDK4J-{}]Handle open platform push messages...", MPSDK4J.version());
        }
        this.rm = convert2VO(this.wxInMsg);
        String info_type = this.rm.getInfoType();
        if(StringUtils.equals(info_type, "component_verify_ticket")){
        	wxHandler.eComponentVerifyTicket(this.rm);
        }else if(StringUtils.equals(info_type, "unauthorized")){
        	wxHandler.eUnAuthorizerMP(this.rm);
        }
        return "success";
    }

    /**
     * Convert WeChat messages to receive messagesVOobject
     *
     * @param msg WeChat message input stream
     * @return receive messagesVOobject
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws AesException
     */
    private ReceiveMsg convert2VO(InputStream msg)
            throws ParserConfigurationException,
            SAXException,
            IOException,
            AesException {

        if (!this.aesEncrypt) { // Express
            this.xmlParser.parse(msg, this.xmlHandler);
        } else {// ciphertext
            String dcrp_msg = this.wxInMsgCrt.decryptMsg(this.msgSignature,
                    this.timeStamp, this.nonce, StreamTool.toString(msg));
            this.wxInMsg = StreamTool.toStream(dcrp_msg);
            this.xmlParser.parse(this.wxInMsg, this.xmlHandler);
        }

        ReceiveMsg rm = this.xmlHandler.getMsgVO();
        this.xmlHandler.clear();
        // 调试信息
        if (log.isInfoEnabled()) {
            log.info("{}", rm);
        }

        return rm;
    }

    /**
     * takeVOConvert objectXMLMessage body
     *
     * @param msg Output messageVOobject
     * @return WeChat news
     * @throws AesException
     */
    private String convert2XML(OutPutMsg msg) throws AesException {

        String reply_msg = "";

        // 自定义内容回复
        if (null != msg.getCustomReply()
                && !msg.getCustomReply().isEmpty()) {
            return  msg.getCustomReply();
        }

        // 获取消息类型
        WxMsgType msg_type = WxMsgType.valueOf(msg.getMsgType());
        switch (msg_type) {
            case text:
                reply_msg = XmlMsgBuilder.create().text(msg).build();
                break;
            case image:
                reply_msg = XmlMsgBuilder.create().image(msg).build();
                break;
            case voice:
                reply_msg = XmlMsgBuilder.create().vioce(msg).build();
                break;
            case video:
                reply_msg = XmlMsgBuilder.create().video(msg).build();
                break;
            case music:
                reply_msg = XmlMsgBuilder.create().music(msg).build();
                break;
            case news:
                reply_msg = XmlMsgBuilder.create().news(msg).build();
                break;
            default:
                break;
        }

        // 调试信息
        if (log.isInfoEnabled()) {
            log.info(reply_msg);
        }

        if (this.aesEncrypt) {// encryption
            reply_msg = this.wxInMsgCrt.encryptMsg(reply_msg, this.timeStamp, this.nonce);
        }

        return reply_msg;
    }

    private void clear() {
        if (null != this.rm) {
            this.rm = null;
        }
        if (null != this.om) {
            this.om = null;
        }
    }

    public boolean isAesEncrypt() {
        return aesEncrypt;
    }

    public void setAesEncrypt(boolean aesEncrypt) {
        this.aesEncrypt = aesEncrypt;
        if (aesEncrypt) {
            try {
                this.wxInMsgCrt = new WXBizMsgCrypt(this.mpAct.getToken(),
                        this.mpAct.getAESKey(), this.mpAct.getAppId());
            } catch (AesException e) {
                log.error("EstablishAESEncryption failed!!!");
                log.error(e.getLocalizedMessage(), e);
                this.wxInMsgCrt = null;
            }
        }
    }

    public MPAct getMpAct() {
        return mpAct;
    }

    public void setMpAct(MPAct mpAct) {
        this.mpAct = mpAct;
        if (log.isInfoEnabled()) {
            log.info("WeChat public information...");
            log.info("{}", this.mpAct);
        }
    }

    public XMLHandler getXmlHandler() {
        return xmlHandler;
    }

    public String getSignature() {
        return signature;
    }

    public void setWxHandler(WxHandler wxHandler) {
        this.wxHandler = wxHandler;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getMsgSignature() {
        return msgSignature;
    }

    public void setMsgSignature(String msgSignature) {
        this.msgSignature = msgSignature;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getEchostr() {
        return echostr;
    }

    public void setEchostr(String echostr) {
        this.echostr = echostr;
    }

    public void setWxInMsg(InputStream wxInMsg) {
        this.wxInMsg = wxInMsg;
    }

    public ReceiveMsg getReceiveMsg() {
        return rm;
    }

    public OutPutMsg getOutPutMsg() {
        return om;
    }
}