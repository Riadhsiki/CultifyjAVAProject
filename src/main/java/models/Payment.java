package models;

import java.sql.Timestamp;

public class Payment {
    private int idP; // Payment ID
    private int idE; // Event ID
    private int idU; // User ID
    private float amount; // Payment amount
    private Timestamp paymentDate; // Date and time of payment
    private String paymentMethod; // "CARD" or "PAYPAL"
    private String transactionReference; // Transaction ID
    private String status; // "COMPLETED", "PENDING", "FAILED", "Confirmé"
    private Event event; // Reference to the associated Event

    // Default constructor
    public Payment() {}

    // Constructor for payment processing (used in PaymentController)
    public Payment(int idE, int idU, float amount, String paymentMethod, String transactionReference, Event event, String status) {
        this.idE = idE;
        this.idU = idU;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.event = event;
        this.status = status;
        this.paymentDate = new Timestamp(System.currentTimeMillis());
    }

    // Constructor from the second provided version
    public Payment(int idE, int idU, float amount, String paymentMethod, String transactionReference) {
        this.idE = idE;
        this.idU = idU;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.status = "COMPLETED";
        this.paymentDate = new Timestamp(System.currentTimeMillis());
    }

    // Getters and setters
    public int getIdP() {
        return idP;
    }

    public void setIdP(int idP) {
        this.idP = idP;
    }

    public int getIdE() {
        return idE;
    }

    public void setIdE(int idE) {
        this.idE = idE;
    }

    public int getIdU() {
        return idU;
    }

    public void setIdU(int idU) {
        this.idU = idU;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    // Alias getters for compatibility with previous code
    public int getEventId() {
        return idE;
    }

    public int getUserId() {
        return idU;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "idP=" + idP +
                ", idE=" + idE +
                ", idU=" + idU +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", transactionReference='" + transactionReference + '\'' +
                ", status='" + status + '\'' +
                ", event=" + (event != null ? event.getTitre() : "null") +
                '}';
    }
}