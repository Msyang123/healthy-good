package com.lhiot.healthygood.wechat.backmsg;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DefaultSession extends Session {
    private List<HandleMessageListener> listeners = new ArrayList(3);

    private DefaultSession() {
    }

    public static DefaultSession newInstance() {
        return new DefaultSession();
    }

    public void addOnHandleMessageListener(HandleMessageListener handleMassge) {
        this.listeners.add(handleMassge);
    }

    public void removeOnHandleMessageListener(HandleMessageListener handleMassge) {
        this.listeners.remove(handleMassge);
    }

    public void onTextMsg(Msg4Text msg) {
        Iterator var3 = this.listeners.iterator();

        while(var3.hasNext()) {
            HandleMessageListener currentListener = (HandleMessageListener)var3.next();
            currentListener.onTextMsg(msg);
        }

    }

    public void onImageMsg(Msg4Image msg) {
        Iterator var3 = this.listeners.iterator();

        while(var3.hasNext()) {
            HandleMessageListener currentListener = (HandleMessageListener)var3.next();
            currentListener.onImageMsg(msg);
        }

    }

    public void onEventMsg(Msg4Event msg) {
        Iterator var3 = this.listeners.iterator();

        while(var3.hasNext()) {
            HandleMessageListener currentListener = (HandleMessageListener)var3.next();
            currentListener.onEventMsg(msg);
        }

    }

    public void onLinkMsg(Msg4Link msg) {
        Iterator var3 = this.listeners.iterator();

        while(var3.hasNext()) {
            HandleMessageListener currentListener = (HandleMessageListener)var3.next();
            currentListener.onLinkMsg(msg);
        }

    }

    public void onLocationMsg(Msg4Location msg) {
        Iterator var3 = this.listeners.iterator();

        while(var3.hasNext()) {
            HandleMessageListener currentListener = (HandleMessageListener)var3.next();
            currentListener.onLocationMsg(msg);
        }

    }

    public void onErrorMsg(int errorCode) {
        Iterator var3 = this.listeners.iterator();

        while(var3.hasNext()) {
            HandleMessageListener currentListener = (HandleMessageListener)var3.next();
            currentListener.onErrorMsg(errorCode);
        }

    }

    public void onVoiceMsg(Msg4Voice msg) {
        Iterator var3 = this.listeners.iterator();

        while(var3.hasNext()) {
            HandleMessageListener currentListener = (HandleMessageListener)var3.next();
            currentListener.onVoiceMsg(msg);
        }

    }

    public void onVideoMsg(Msg4Video msg) {
        Iterator var3 = this.listeners.iterator();

        while(var3.hasNext()) {
            HandleMessageListener currentListener = (HandleMessageListener)var3.next();
            currentListener.onVideoMsg(msg);
        }

    }
}
