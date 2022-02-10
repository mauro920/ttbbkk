package app.maironstudios.com.trasbankintegration;

import java.io.Serializable;

/**
 * Created by MaironApps.
 */
public class VtexData implements Serializable {
    private double amount;
    private String paymentId;
    private String paymentDescription;
    private String paymentType;
    private Integer installments;
    private Long payerIdentification;
    private String payerEmail;
    private String acquirerId;
    private String acquirerSecret;
    private double acquirerFee;
    private String aquirerAccessToken;
    private String urlCallBack;
    private String scheme;
    private String action;
    private String transactionId;
    private String storeCurrency;
    private String accountName;
    private String target;

    public VtexData() {
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentDescription() {
        return paymentDescription;
    }

    public void setPaymentDescription(String description) {
        this.paymentDescription = description;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }

    public String getAcquirerId() {
        return acquirerId;
    }

    public void setAcquirerId(String acquirerId) {
        this.acquirerId = acquirerId;
    }

    public String getAcquirerSecret() {
        return acquirerSecret;
    }

    public void setAcquirerSecret(String acquirerSecret) {
        this.acquirerSecret = acquirerSecret;
    }

    public double getAcquirerFee() {
        return acquirerFee;
    }

    public void setAcquirerFee(double acquirerFee) {
        this.acquirerFee = acquirerFee;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getUrlCallBack() {
        return urlCallBack;
    }

    public void setUrlCallBack(String urlCallBack) {
        this.urlCallBack = urlCallBack;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public Long getPayerIdentification() {
        return payerIdentification;
    }

    public void setPayerIdentification(Long payerIdentification) {
        this.payerIdentification = payerIdentification;
    }

    @Override
    public String toString() {
        return "paymentId: "+getPaymentId()+" / amount: "+getAmount()+" / payerIdentification: "+getPayerIdentification();
    }

    public String getAquirerAccessToken() {
        return aquirerAccessToken;
    }

    public void setAquirerAccessToken(String aquirerAccessToken) {
        this.aquirerAccessToken = aquirerAccessToken;
    }

    public String getStoreCurrency() {
        return storeCurrency;
    }

    public void setStoreCurrency(String storeCurrency) {
        this.storeCurrency = storeCurrency;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
