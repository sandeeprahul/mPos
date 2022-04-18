package in.hng.mpos.gettersetter;

public class WalletMaster {

    String mkKey;
    String mkSMID;
    String mkGenOTP;
    String ppSaltKey;
    String ppSaltIndex;
    String ppMerchantID;
    String ppInsType;
    String ppExpiresIn;
    String ppStoreID;
    String ppTerminalID;
    String zagKey;

    public String getMkKey() {
        return mkKey;
    }
    public void setMkKey(String mkKey) {
        this.mkKey = mkKey;
    }

    public String getMkSMID() {
        return mkSMID;
    }
    public void setMkSMID(String mkSMID) {
        this.mkSMID = mkSMID;
    }


    public String getMkGenOTP() {
        return mkGenOTP;
    }
    public void setMkGenOTP(String mkGenOTP) {
        this.mkGenOTP = mkGenOTP;
    }


    public String getPpSaltKey() {
        return ppSaltKey;
    }
    public void setPpSaltKey(String ppSaltKey) {
        this.ppSaltKey = ppSaltKey;
    }


    public String getPpSaltIndex() {
        return ppSaltIndex;
    }
    public void setPpSaltIndex(String ppSaltIndex) {
        this.ppSaltIndex = ppSaltIndex;
    }


    public String getPpMerchantID() {
        return ppMerchantID;
    }
    public void setPpMerchantID(String ppMerchantID) {
        this.ppMerchantID = ppMerchantID;
    }


    public String getPpInsType() {
        return ppInsType;
    }
    public void setPpInsType(String ppInsType) {
        this.ppInsType = ppInsType;
    }


    public String getPpExpiresIn() {
        return ppExpiresIn;
    }
    public void setPpExpiresIn(String ppExpiresIn) {
        this.ppExpiresIn = ppExpiresIn;
    }


    public String getPpStoreID() {
        return ppStoreID;
    }
    public void setPpStoreID(String ppStoreID) {
        this.ppStoreID = ppStoreID;
    }


    public String getPpTerminalID() {
        return ppTerminalID;
    }
    public void setPpTerminalID(String ppTerminalID) {
        this.ppTerminalID = ppTerminalID;
    }


    public String getZagKey() {
        return zagKey;
    }
    public void setZagKey(String zagKey) {
        this.zagKey = zagKey;
    }

}
