package com.fkzhang.wechatunrecalled;

/**
 * Created by fkzhang on 1/16/2016.
 */
public class WechatPackageNames {
    public String packageNameBase;
    public String recallClass;
    public String packageName;
    public String recallMethod;
    public String snsClass;
    public String snsMethod;
    public String dbClass1;
    public String dbMethod1;
    public String dbMethod2;
    public String updateMsgId;
    public String contextGetter;
    public String iconClass;
    public String iconMethod;
    public String imageClass;
    public String imageMethod1;
    public String imageMethod2;
    public String avatarClass;
    public String avatarMethod1;
    public String avatarMethod2;
    public String blobDecodeMethod;
    public String commentContentField;
    public String commentClass;
    public String commentTimeField;
    public String snsContentClass;
    public String snsContentField;
    public String luckyRevealImageView;
    public String snsAttrClass;
    public String commentsListField;
    public String snsLuckyMoneyClass1;
    public String snsLuckyMoneyBlur;
    public String snsLuckyMoneyOrignal;
    public String snsLuckyMoneyClass2;
    public String storageClass1;
    public String storageMethod1;
    public String msgCountMethod1;
    public String msgCountMethod2;
    public String emojiClass;
    public String emojiMethod1;
    public String ChattingUI;
    public String snsCommentDetailUI;
    public String ImageGalleryUI;
    public String SnsMsgUI;
    public String launcherUI;
    public String notificationClass;
    public String dexFile;
    public String snsLuckyMoneyBlur2;
    public String snsLuckyMoneyBitmapDecoder;
    public String snsLuckyMoneySetBitmapMethod;
    public String snsLuckyMoneyMethod1;
    public String snsLuckyMoneyWantSeePhotoUIConstructor;
    public String snsLuckyMoneyWantSeePhotoUI;
    public String snsLuckyMoneyButton;
    public String snsLuckyMoneyBanner;
    public String snsLuckyMoneyBannerImageView;
    public String snsLuckyMoneyBannerTextView;
    public String snsLuckyMoneyBannerView1;
    public String snsLuckyMoneyBannerView2;
    public String snsLuckyMoneyRevealBigpicture;
    public String snsLuckyMoneyClass3;
    public String snsLuckyMoneyClass4;
    public String SQLiteDatabaseClass;

    public WechatPackageNames(String packageName, String versionName) {
        this.packageName = packageName;
        this.packageNameBase = packageName.substring(0, packageName.lastIndexOf("."));

        initNames();
        if (versionName.contains("6.3.13")) {
            set6313();
        } else if (versionName.contains("6.3.11")) {
            set6311();
        } else if (versionName.contains("6.3.9")) {
            set639();
        } else if (versionName.contains("6.3.8")) {
            set638();
        } else if (versionName.contains("6.3.7")) {
            set637();
        } else if (versionName.contains("6.3.5")) {
            set635();
        } else if (versionName.contains("6.3.0")) {
            set630();
        } else if (versionName.contains("6.2.5")) {
            set625();
        } else if (versionName.contains("6.0.0")) {
            set600();
        } else if (versionName.contains("6.0.2.58")) {
            set602_58();
        }
    }

    private void set600() {
        recallClass += "u";
        recallMethod = "D";
        snsClass += "d.g";
        snsMethod += "g.af";
        dbClass1 += "bh"; // look for: ("message")
        dbMethod1 = "sS";
        dbMethod2 = "qQ";
        contextGetter += "ai";
        iconClass += ".booter.notification.a.d"; // look for: sdk < 19
        iconMethod = "mX";
        imageClass += ".z.ar";
        imageMethod1 = "zZ";
        imageMethod2 = "g";
        avatarClass += ".p.af";
        avatarMethod1 = "uQ";
        avatarMethod2 = "b";
        commentClass += "aaq";
        blobDecodeMethod = "m";
        commentContentField = "fWd";
        snsContentClass += "adq";
        snsContentField = "iZr";
        snsAttrClass += "abb";
        commentsListField = "iWH";
        commentTimeField = "iuc";
        storageClass1 += "ay";
        storageMethod1 += ".at.h";
        msgCountMethod1 = "Cv";
        msgCountMethod2 = "aSC";
        notificationClass += "u";

    }

    private void set602_58() {
        recallClass += "n";
        recallMethod = "B";
        snsClass += "e.l";
        snsMethod += ".g.ae";
        dbClass1 += "au";
        dbMethod1 = "Cr";
        dbMethod2 = "Am";
        contextGetter += "x";
        iconClass += ".booter.notification.a.c";
        iconMethod = "wr";
        imageClass += ".y.af";
        imageMethod1 = "JA";
        imageMethod2 = "h";
        avatarClass += ".p.u";
        avatarMethod1 = "Eu";
        avatarMethod2 = "b";
        commentClass += "adb";
        blobDecodeMethod = "v";
        commentContentField = "gMw";
        snsContentClass += "agc";
        snsContentField = "kek";
        snsAttrClass += "adm";
        commentsListField = "kbJ";
        commentTimeField = "gJB";
        storageClass1 += "ap";
        storageMethod1 += ".ap.g";
        msgCountMethod1 = "ER";
        msgCountMethod2 = "bjt";
        notificationClass += "t";
    }

    private void set625() {
        recallClass += "p";
        recallMethod = "B";
        snsClass += "g.l";
        snsMethod += "g.d";
        dbClass1 += "ah";
        dbMethod1 = "tI";
        dbMethod2 = "rG";
        updateMsgId = "aNG";
        contextGetter += "x";
        iconClass += ".am.a";
        iconMethod = "aDA";
        imageClass += ".z.n";
        imageMethod1 = "Ao";
        imageMethod2 = "hs";
        avatarClass += ".p.n";
        avatarMethod1 = "vC";
        avatarMethod2 = "b";
        commentClass += "alf";
        blobDecodeMethod = "ag";
        commentContentField = "eGr";
        snsContentClass += "aok";
        snsContentField = "ixy";
        snsAttrClass += "alr";
        commentsListField = "ivq";
        commentTimeField = "eDv";
        notificationClass += "e.1";
        emojiClass += "c"; // look for main_conversation_chatroom_unread_digest
        emojiMethod1 = "Dw";
        storageClass1 += "h";
        storageMethod1 += ".av.g";
    }

    private void set630() {
        recallClass += "q";
        recallMethod = "C";
        snsClass += "h.l";
        snsMethod += "g.d";
        dbClass1 += "ah";
        dbMethod1 = "tL";
        dbMethod2 = "rJ";
        updateMsgId = "aPp";
        contextGetter += "y";
        iconClass += ".an.a";
        iconMethod = "aFK";
        imageClass += ".z.n";
        imageMethod1 = "Ar";
        imageMethod2 = "hv";
        avatarClass += ".p.n";
        avatarMethod1 = "vF";
        avatarMethod2 = "b";
        commentClass += "alx";
        blobDecodeMethod = "aj";
        commentContentField = "eNl";
        snsContentClass += "apb";
        snsContentField = "iIM";
        snsAttrClass += "amj";
        commentsListField = "iGA";
        commentTimeField = "eKp";
        notificationClass += "e.1";
        emojiClass += "d"; // look for main_conversation_chatroom_unread_digest
        emojiMethod1 = "DK";
        storageClass1 += "h";
        storageMethod1 += ".aw.g";
    }

    private void set635() {
        recallClass += "q";
        recallMethod = "C";
        snsClass += "h.l";
        snsMethod += "g.d";
        dbClass1 += "ai";
        dbMethod1 = "tO";
        dbMethod2 = "rM";
        updateMsgId = "aPy";
        contextGetter += "y";
        iconClass += ".an.a";
        iconMethod = "aFT";
        imageClass += ".z.n";
        imageMethod1 = "Aw";
        imageMethod2 = "hv";
        avatarClass += ".p.n";
        avatarMethod1 = "vI";
        avatarMethod2 = "b";
        commentClass += "alx";
        blobDecodeMethod = "ak";
        commentContentField = "eNp";
        snsContentClass += "apb";
        snsContentField = "iIO";
        snsAttrClass += "amj";
        commentsListField = "iGC";
        commentTimeField = "eKt";
        notificationClass += "e.1";
        emojiClass += "c"; // look for main_conversation_chatroom_unread_digest
        emojiMethod1 = "DL";
        storageClass1 += "h";
        storageMethod1 += ".aw.g";
    }

    private void set637() {
        recallClass += "r";
        recallMethod = "H";
        snsClass += "h.l";
        snsMethod += "g.d";
        dbClass1 += "ah";
        dbMethod1 = "tn";
        dbMethod2 = "rk";
        updateMsgId = "aRo";
        contextGetter += "z";
        iconClass += ".an.a";
        iconMethod = "aHx";
        imageClass += ".z.n";
        imageMethod1 = "zW";
        imageMethod2 = "hy";
        avatarClass += ".p.n";
        avatarMethod1 = "vh";
        avatarMethod2 = "b";
        commentClass += "anr";
        blobDecodeMethod = "ak";
        commentContentField = "eVT";
        snsContentClass += "aqx";
        snsContentField = "iXe";
        snsAttrClass += "aod";
        commentsListField = "iUS";
        commentTimeField = "eSX";
        notificationClass += "b$1";
        emojiClass += "d"; // look for main_conversation_chatroom_unread_digest
        emojiMethod1 = "EX";
        storageClass1 += "h";
        storageMethod1 += ".aw.g";
    }

    private void set638() {
        recallClass += "r";
        recallMethod = "I";
        snsClass += "h.l";
        snsMethod += "g.d";
        dbClass1 += "ah";
        dbMethod1 = "tl";
        dbMethod2 = "rj";
        updateMsgId = "aTT";
        contextGetter += "z";
        iconClass += ".an.a";
        iconMethod = "aKh";
        imageClass += ".z.n";
        imageMethod1 = "zV";
        imageMethod2 = "hC";
        avatarClass += ".p.n";
        avatarMethod1 = "vf";
        avatarMethod2 = "b";
        commentClass += "aoq";
        blobDecodeMethod = "ak";
        commentContentField = "fdy";
        snsContentClass += "asd";
        snsContentField = "jjP";
        snsAttrClass += "apc";
        commentsListField = "jhw";
        commentTimeField = "faB";
        notificationClass += "b$1";
        emojiClass += "d"; // look for main_conversation_chatroom_unread_digest
        emojiMethod1 = "FM";
        storageClass1 += "h";
        storageMethod1 += ".aw.g";
    }

    private void set639() {
        recallClass += "q";
        recallMethod = "J";
        snsClass += "h.l";
        snsMethod += "h.d";
        dbClass1 += "ah";
        dbMethod1 = "tr";
        dbMethod2 = "rk";
        updateMsgId = "aVP";
        contextGetter += "y";
        iconClass += ".ap.a";
        iconMethod = "aMW";
        imageClass += ".aa.n";
        imageMethod1 = "zZ";
        imageMethod2 = "hF";
        avatarClass += ".q.n";
        avatarMethod1 = "vi";
        avatarMethod2 = "b";
        commentClass += "api";
        blobDecodeMethod = "al";
        commentContentField = "fmw";
        snsContentClass += "asz";
        snsContentField = "jBA";
        snsAttrClass += "apw";
        commentsListField = "jzc";
        commentTimeField = "fjz";
        luckyRevealImageView = this.packageName +
                ".plugin.sns.lucky.ui.LuckyRevealImageView";
        notificationClass += "b$1";
        emojiClass += "c"; // look for main_conversation_chatroom_unread_digest
        emojiMethod1 = "GC";
        storageClass1 += "h";
        storageMethod1 += ".ay.g";
    }


    private void set6311() {
        recallClass += "q";
        recallMethod = "J";
        snsClass += "h.l";
        snsMethod += "h.d";
        dbClass1 += "ah";
        dbMethod1 = "tD";
        dbMethod2 = "rs";
        updateMsgId = "aXP";
        contextGetter += "y";
        iconClass += ".aq.a";
        iconMethod = "aOQ";
        imageClass += ".ab.n";
        imageMethod1 = "Ao";
        imageMethod2 = "hM";
        avatarClass += ".q.n";
        avatarMethod1 = "vu";
        avatarMethod2 = "b";
        commentClass += "apu";
        blobDecodeMethod = "am";
        commentContentField = "fsI";
        snsContentClass += "atp";
        snsContentField = "jMu";
        snsAttrClass += "aqi";
        commentsListField = "jJX";
        commentTimeField = "fpL";
        luckyRevealImageView = this.packageName +
                ".plugin.sns.lucky.ui.LuckyRevealImageView";
        emojiClass += "d"; // look for main_conversation_chatroom_unread_digest
        emojiMethod1 = "Hl";
        notificationClass += "b$1";
        dexFile = "secondary-1";
        storageClass1 += "h";
        storageMethod1 += ".az.g";

        snsLuckyMoneyClass1 = packageName + ".plugin.sns.data.h";
        snsLuckyMoneyClass2 += "add";
        snsLuckyMoneyOrignal = "b";
        snsLuckyMoneyBlur = "g";
        snsLuckyMoneyBlur2 = "e";

        snsLuckyMoneyBitmapDecoder = "uk";
        snsLuckyMoneySetBitmapMethod = "axU";
        snsLuckyMoneyMethod1 = "b";
        snsLuckyMoneyWantSeePhotoUIConstructor = "k";
        snsLuckyMoneyWantSeePhotoUI = packageName + ".plugin.sns.lucky.ui.SnsLuckyMoneyWantSeePhotoUI";
        snsLuckyMoneyButton = "dtX";
        snsLuckyMoneyBanner = "gKu";
        snsLuckyMoneyBannerImageView = "gKt";
        snsLuckyMoneyBannerTextView = "gKv";
        snsLuckyMoneyBannerView1 = "gKw";
        snsLuckyMoneyBannerView2 = "gKx";
        snsLuckyMoneyClass3 += "c.b";
        snsLuckyMoneyClass4 += "d.ac";
        snsLuckyMoneyRevealBigpicture = "getOriginBigBitmapFilePath";
    }

    private void set6313() {
        set6311();
        snsLuckyMoneyRevealBigpicture = "bpg";
    }

    private void initNames() {
        recallClass = this.packageName + ".sdk.platformtools.";
        recallMethod = "";
        snsClass = this.packageName + ".plugin.sns.";
        snsMethod = this.packageName + ".sdk.";
        dbClass1 = this.packageName + ".model.";
        dbMethod1 = "";
        dbMethod2 = "";
        updateMsgId = "";
        contextGetter = recallClass;
        iconClass = this.packageName;
        iconMethod = "";
        imageClass = this.packageName;
        imageMethod1 = "";
        imageMethod2 = "";
        avatarClass = this.packageName;
        avatarMethod1 = "";
        avatarMethod2 = "";
        commentClass = this.packageName + ".protocal.b.";
        blobDecodeMethod = "";
        commentContentField = "";
        commentTimeField = "";
        snsContentClass = commentClass;
        snsContentField = "";
        snsAttrClass = commentClass;
        commentsListField = "";
        luckyRevealImageView = "";
        snsLuckyMoneyClass1 = "";
        snsLuckyMoneyOrignal = "";
        snsLuckyMoneyBlur = "";
        snsLuckyMoneyClass2 = packageName + ".protocal.b.";
        storageClass1 = packageName + ".storage.";
        storageMethod1 = packageName;
        msgCountMethod1 = "";
        msgCountMethod2 = "";
        emojiClass = packageName + ".ui.conversation.";
        emojiMethod1 = "";
        ChattingUI = packageName + ".ui.chatting.ChattingUI";
        SnsMsgUI = packageName + ".plugin.sns.ui.SnsMsgUI";
        ImageGalleryUI = packageName + ".ui.chatting.gallery.ImageGalleryUI";
        snsCommentDetailUI = packageName + ".plugin.sns.ui.SnsCommentDetailUI";
        launcherUI = this.packageName + ".ui.LauncherUI";
        notificationClass = packageName + ".booter.notification.";
        dexFile = "";
        snsLuckyMoneyBlur2 = "";
        snsLuckyMoneyBitmapDecoder = "";
        snsLuckyMoneySetBitmapMethod = "";
        snsLuckyMoneyMethod1 = "";
        snsLuckyMoneyWantSeePhotoUIConstructor = "";
        snsLuckyMoneyWantSeePhotoUI = "";
        snsLuckyMoneyButton = "";
        snsLuckyMoneyBanner = "";
        snsLuckyMoneyBannerImageView = "";
        snsLuckyMoneyBannerTextView = "";
        snsLuckyMoneyBannerView1 = "";
        snsLuckyMoneyBannerView2 = "";
        snsLuckyMoneyRevealBigpicture = "";
        snsLuckyMoneyClass3 = packageName + ".plugin.sns.ui.";
        snsLuckyMoneyClass4 = packageName + ".plugin.sns.";
        SQLiteDatabaseClass = packageNameBase + ".kingkong.database.SQLiteDatabase";
    }
}
