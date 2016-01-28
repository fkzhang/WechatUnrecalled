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
    public String dbField;
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
    public String commentMethod;
    public String commentField;
    public String commentClass;

    public WechatPackageNames(String packageName, String version) {
        this.packageName = packageName;
        this.packageNameBase = packageName.substring(0, packageName.lastIndexOf("."));

        initNames();
        switch (version) {
            case "6.3.11":
                set6311();
                break;
            case "6.3.9":
                set639();
                break;
            case "6.3.8":
                set638();
                break;
            case "6.3.5":
                set635();
                break;
            case "6.0.0":
                set600();
                break;
        }
    }

    private void set600() {
        recallClass += "u";
        recallMethod = "D";
        snsClass += "d.g";
        snsMethod += "g.af";
        dbClass1 += "bh";
        dbMethod1 = "sS";
        dbMethod2 = "qQ";
        dbField = "dGo";
        contextGetter += "ai";
        iconClass += ".booter.notification.a.d";
        iconMethod = "mX";
        imageClass += ".z.ar";
        imageMethod1 = "zZ";
        imageMethod2 = "g";
        avatarClass += ".p.af";
        avatarMethod1 = "uQ";
        avatarMethod2 = "b";
        commentClass += "aaq";
        commentMethod = "m";
        commentField = "fWd";
    }

    private void set635() {
        recallClass += "q";
        recallMethod = "C";
        snsClass += "h.l";
        snsMethod += "g.d";
        dbClass1 += "ai";
        dbMethod1 = "tO";
        dbMethod2 = "rM";
        dbField = "bts";
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
        commentMethod = "ak";
        commentField = "eNp";
    }

    private void set638() {
        recallClass += "r";
        recallMethod = "I";
        snsClass += "h.l";
        snsMethod += "g.d";
        dbClass1 += "ah";
        dbMethod1 = "tl";
        dbMethod2 = "rj";
        dbField = "bww";
        updateMsgId = "aTT";
        contextGetter += "z";
        iconClass += ".an.a";
        iconMethod = "aKh";
        imageClass += ".z.n";
        imageMethod1 = "zV";
        imageMethod2 = "hC";
        avatarClass += ".p.n";
        avatarMethod1 = "vF";
        avatarMethod2 = "b";
        commentClass += "aoq";
        commentMethod = "ak";
        commentField = "fdy";
    }

    private void set639() {
        recallClass += "q";
        recallMethod = "J";
        snsClass += "h.l";
        snsMethod += "h.d";
        dbClass1 += "ah";
        dbMethod1 = "tr";
        dbMethod2 = "rk";
        dbField = "bzj";
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
        commentMethod = "al";
        commentField = "fmw";
    }


    private void set6311() {
        recallClass += "q";
        recallMethod = "J";
        snsClass += "h.l";
        snsMethod += "h.d";
        dbClass1 += "ah";
        dbMethod1 = "tD";
        dbMethod2 = "rs";
        dbField = "bCw";// look for: ah.tD().rs() or in com.tencent.mm.storage.ah
        updateMsgId = "aXP";
        contextGetter += "y";
        iconClass += ".aq.a";
        iconMethod = "aOQ";
        imageClass += ".ab.n";
        imageMethod1 = "Ao";
        imageMethod2 = "hM";
        avatarClass += ".q.n";
        avatarMethod1 = "vi";
        avatarMethod1 = "vu";
        commentClass += "apu";
        commentMethod = "am";
        commentField = "fsI";
    }

    private void initNames() {
        recallClass = this.packageName + ".sdk.platformtools.";
        recallMethod = "";
        snsClass = this.packageName + ".plugin.sns.";
        snsMethod = this.packageName + ".sdk.";
        dbClass1 = this.packageName + ".model.";
        dbMethod1 = "";
        dbMethod2 = "";
        dbField = "";
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
        commentMethod = "";
        commentField = "";
    }
}
