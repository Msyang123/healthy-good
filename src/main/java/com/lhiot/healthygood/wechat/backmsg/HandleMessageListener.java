package com.lhiot.healthygood.wechat.backmsg;



public interface HandleMessageListener {
    void onTextMsg(Msg4Text var1);

    void onImageMsg(Msg4Image var1);

    void onEventMsg(Msg4Event var1);

    void onLinkMsg(Msg4Link var1);

    void onLocationMsg(Msg4Location var1);

    void onVoiceMsg(Msg4Voice var1);

    void onErrorMsg(int var1);

    void onVideoMsg(Msg4Video var1);
}
