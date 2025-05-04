package services.eventreservation;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSService {
    private static final String ACCOUNT_SID = "ACf2987dc291705b6ae845f11982cfe0f3";
    private static final String AUTH_TOKEN = "e9e56df56b458a55ec992c47af099c75";
    private static final String TWILIO_NUMBER = "+12569799141";

    public static boolean sendSMS(String destinationNumber, String messageContent) {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            Message message = Message.creator(
                            new PhoneNumber(destinationNumber),
                            new PhoneNumber(TWILIO_NUMBER),
                            messageContent)
                    .create();

            System.out.println("SMS envoyé avec l'ID: " + message.getSid());
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du SMS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
