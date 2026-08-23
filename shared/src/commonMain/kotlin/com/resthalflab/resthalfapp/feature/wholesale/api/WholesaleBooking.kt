package com.resthalflab.resthalfapp.feature.wholesale.api

/**
 * Fresh price + authoritative ids returned by the price-check, used to drive book init / book.
 * [totalRate] is compared against what the user last saw to detect a price change.
 */
data class PriceQuote(
    val token: String,
    val rateId: String,
    val roomId: String,
    val totalRate: Int,
    val currency: String,
    val boardBasisLabel: String,
    val breakfastIncluded: Boolean,
    val refundable: Boolean,
    val adults: Int,
    val children: Int,
    val childAges: List<Int>,
)

enum class GuestType { Adult, Child }

data class GuestForm(
    val type: GuestType,
    val title: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val email: String,
)

data class BillingForm(
    val title: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val phone: String,
    val email: String,
    val addressLine1: String,
    val city: String,
    val country: String,
    val postalCode: String,
)

/** Credit card (Visa only for now). [expiryMonth] is "MM", [expiryYear] is "YYYY". */
data class CardForm(
    val number: String,
    val nameOnCard: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvv: String,
)

/** The whole booking bundle assembled from the checkout form — shared by book init and book. */
data class BookingRequest(
    val hotelId: String,
    val token: String,
    val rateId: String,
    val roomId: String,
    val bookingRefId: String,
    val guests: List<GuestForm>,
    val billing: BillingForm,
    val card: CardForm,
    val specialRequests: List<String> = emptyList(),
)

data class BookingResult(
    val bookingId: String,
    val status: String,
    val providerConfirmationNumber: String?,
    val hotelConfirmationNumber: String?,
    val cancellationToken: String?,
)
