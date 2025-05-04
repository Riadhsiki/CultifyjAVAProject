package controllers.DonControllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.param.ChargeCreateParams;
import com.stripe.param.PaymentIntentCreateParams;

public class StripePaymentService {
    private static final String API_KEY = "sk_test_51RDtYPHBCG8LTUbgqBWsy8yycBFkbztGwVVlTUCOtJsJRlHWGLxP4KaoUWVscLtMY6ZNVsfNGIjCGoisEr11UEgZ00AFkPMxL4";

    // Cartes de test Stripe
    public static final String CARD_SUCCESS = "4242424242424242"; // Carte qui réussit toujours
    public static final String CARD_DECLINE = "4000000000000002"; // Carte qui échoue toujours

    static {
        Stripe.apiKey = API_KEY;
    }

    /**
     * Crée une intention de paiement qui peut être utilisée pour le mode test sans carte bancaire.
     *
     * @param amount Montant en centimes (100 = 1 TND)
     * @param description Description du paiement
     * @param currency Devise (par défaut "tnd")
     * @return L'intention de paiement créée
     * @throws StripeException En cas d'erreur avec l'API Stripe
     */
    public static PaymentIntent createPaymentIntent(long amount, String description, String currency) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setDescription(description)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        return PaymentIntent.create(params);
    }

    /**
     * Crée directement un paiement simulé (utile pour les tests).
     *
     * @param amount Montant en centimes (100 = 1 TND)
     * @param description Description
     * @param currency Devise
     * @param cardNumber Numéro de carte à utiliser
     * @return L'objet Charge créé ou null si erreur
     */
    public static Charge createTestCharge(long amount, String description, String currency, String cardNumber) throws StripeException {
        // Déterminer le token en fonction du numéro de carte
        String token;
        if (cardNumber.equals(CARD_SUCCESS)) {
            token = "tok_visa"; // Carte qui réussit toujours
        } else if (cardNumber.equals(CARD_DECLINE)) {
            token = "tok_chargeDeclined"; // Carte qui échoue toujours
        } else {

            token = "tok_visa";
        }

        ChargeCreateParams params = ChargeCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setSource(token)
                .setDescription(description)
                .build();

        try {
            return Charge.create(params);
        } catch (StripeException e) {
            System.err.println("Erreur Stripe: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Vérifie si un paiement a réussi.
     *
     * @param paymentIntentId ID de l'intention de paiement
     * @return true si le paiement a réussi
     * @throws StripeException En cas d'erreur avec l'API Stripe
     */
    public static boolean isPaymentSuccessful(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return "succeeded".equals(paymentIntent.getStatus());
    }
}