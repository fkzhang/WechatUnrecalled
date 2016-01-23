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

    public WechatPackageNames(String packageName, String version) {
        this.packageName = packageName;
        this.packageNameBase = packageName.substring(0, packageName.lastIndexOf("."));

        initNames();
        switch (version) {
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
    }

    private void set635() {
        recallClass += "q";
        recallMethod = "C";
        snsClass += "h.l";
        snsMethod += "g.d";
        dbClass1 += "ai";
        dbMethod1 = "t0";
        dbMethod2 = "rM";
        dbField = "bts";
        updateMsgId = "aPy";
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
    }
}
