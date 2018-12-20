package com.lhiot.healthygood.wechat.backmsg;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

public abstract class Session {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private InputStream is;
    private OutputStream os;
    private static DocumentBuilder builder;
    private static TransformerFactory tffactory;

    static {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException var2) {
            var2.printStackTrace();
        }

        tffactory = TransformerFactory.newInstance();
    }

    public Session() {
    }

    public void process(InputStream is, OutputStream os) {
        this.os = os;

        try {
            Document document = builder.parse(is);
            Msg4Head head = new Msg4Head();
            head.read(document);
            String type = head.getMsgType();
            if ("text".equals(type)) {
                Msg4Text msg = new Msg4Text(head);
                msg.read(document);
                this.onTextMsg(msg);
            } else if ("image".equals(type)) {
                Msg4Image msg = new Msg4Image(head);
                msg.read(document);
                this.onImageMsg(msg);
            } else if ("event".equals(type)) {
                Msg4Event msg = new Msg4Event(head);
                msg.read(document);
                this.onEventMsg(msg);
            } else if ("link".equals(type)) {
                Msg4Link msg = new Msg4Link(head);
                msg.read(document);
                this.onLinkMsg(msg);
            } else if ("location".equals(type)) {
                Msg4Location msg = new Msg4Location(head);
                msg.read(document);
                this.onLocationMsg(msg);
            } else if ("voice".equals(type)) {
                Msg4Voice msg = new Msg4Voice(head);
                msg.read(document);
                this.onVoiceMsg(msg);
            } else if ("video".equals(type)) {
                Msg4Video msg = new Msg4Video(head);
                msg.read(document);
                this.onVideoMsg(msg);
            } else {
                this.onErrorMsg(-1);
            }
        } catch (SAXException var7) {
            var7.printStackTrace();
        } catch (IOException var8) {
            var8.printStackTrace();
        }

    }

    public void callback(Msg msg) {
        Document document = builder.newDocument();
        msg.write(document);

        try {
            Transformer transformer = tffactory.newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(new OutputStreamWriter(this.os, StandardCharsets.UTF_8)));
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public void close() {
        try {
            if (this.is != null) {
                this.is.close();
            }

            if (this.os != null) {
                this.os.flush();
                this.os.close();
            }
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public abstract void onTextMsg(Msg4Text var1);

    public abstract void onImageMsg(Msg4Image var1);

    public abstract void onEventMsg(Msg4Event var1);

    public abstract void onLinkMsg(Msg4Link var1);

    public abstract void onLocationMsg(Msg4Location var1);

    public abstract void onVoiceMsg(Msg4Voice var1);

    public abstract void onVideoMsg(Msg4Video var1);

    public abstract void onErrorMsg(int var1);
}
