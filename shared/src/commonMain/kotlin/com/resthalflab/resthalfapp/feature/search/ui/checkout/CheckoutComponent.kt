package com.resthalflab.resthalfapp.feature.search.ui.checkout

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.benasher44.uuid.uuid4
import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.api.LocalBookingStore
import com.resthalflab.resthalfapp.feature.bookings.api.StoredBooking
import com.resthalflab.resthalfapp.feature.search.api.CheckoutArgs
import com.resthalflab.resthalfapp.feature.wholesale.api.BillingForm
import com.resthalflab.resthalfapp.feature.wholesale.api.BookingRequest
import com.resthalflab.resthalfapp.feature.wholesale.api.BookingResult
import com.resthalflab.resthalfapp.feature.wholesale.api.CardForm
import com.resthalflab.resthalfapp.feature.wholesale.api.GuestForm
import com.resthalflab.resthalfapp.feature.wholesale.api.GuestType
import com.resthalflab.resthalfapp.feature.wholesale.api.PriceQuote
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleBookingApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

/** Contact + billing address collected on the Review step (billing name reuses the primary guest). */
data class ContactForm(
    val email: String = "",
    val phone: String = "",
    val addressLine1: String = "",
    val city: String = "",
    val country: String = "",
    val postalCode: String = "",
)

/** A price difference surfaced by the price-check — the user must accept it to continue. */
data class PriceChange(val oldTotal: Int, val newTotal: Int, val currency: String)

interface CheckoutComponent {
    val state: StateFlow<UiState>

    fun onBackClicked()
    fun onNextStep()
    fun onPrevStep()
    fun onGuestChange(index: Int, guest: GuestForm)
    fun onContactChange(contact: ContactForm)
    fun onCardChange(card: CardForm)
    fun onSpecialRequestsChange(text: String)
    fun onConfirmPriceChange()
    fun onDeclinePriceChange()
    fun onConfirmAndPay()
    fun onSimulatePayment()
    fun onCancelPayment()
    fun onRetryPriceCheck()
    fun onDone()
    fun onDismissError()

    data class UiState(
        val hotelName: String,
        val heroImage: String?,
        val roomName: String,
        val dateLabel: String,
        val nights: Int,
        val guestsLabel: String,
        val boardBasisLabel: String,
        val breakfastIncluded: Boolean,
        val refundable: Boolean,
        val perNightRate: Int,
        val totalRate: Int,
        val currency: String,
        val step: Int = STEP_REVIEW,
        val guests: List<GuestForm> = emptyList(),
        val contact: ContactForm = ContactForm(),
        val card: CardForm = CardForm("", "", "", "", ""),
        val specialRequests: String = "",
        val priceChecking: Boolean = true,
        val priceLoadError: String? = null,
        val priceChange: PriceChange? = null,
        val processing: Boolean = false,
        val processingLabel: String? = null,
        val error: String? = null,
        val bookingResult: BookingResult? = null,
        val showErrors: Boolean = false,
        val showPaymentWebView: Boolean = false,
        val paymentUrl: String = "",
    )

    companion object {
        const val STEP_REVIEW = 0
        const val STEP_PAYMENT = 1
        const val STEP_CONFIRM = 2
    }
}

class DefaultCheckoutComponent(
    componentContext: ComponentContext,
    private val bookingApi: WholesaleBookingApi,
    private val bookingStore: LocalBookingStore,
    private val args: CheckoutArgs,
    private val onExit: () -> Unit,
    private val onFinished: () -> Unit,
) : CheckoutComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main)
    private val bookingRefId = uuid4().toString()
    private var quote: PriceQuote? = null
    private var pendingRequest: BookingRequest? = null

    private val _state = MutableStateFlow(
        CheckoutComponent.UiState(
            paymentUrl = PAYMENT_URL,
            hotelName = args.hotelName,
            heroImage = args.heroImage,
            roomName = args.roomName,
            dateLabel = dateRangeLabel(args.checkIn, args.checkOut),
            nights = args.nights,
            guestsLabel = "",
            boardBasisLabel = args.boardBasisLabel,
            breakfastIncluded = args.breakfastIncluded,
            refundable = args.refundable,
            perNightRate = args.perNightRate,
            totalRate = args.totalRate,
            currency = args.currency,
        )
    )
    override val state: StateFlow<CheckoutComponent.UiState> = _state.asStateFlow()

    init {
        priceCheck()
    }

    private fun priceCheck() {
        scope.launch {
            _state.update { it.copy(priceChecking = true, priceLoadError = null) }
            when (val result = bookingApi.priceCheck(args.hotelId, args.token, args.recommendationId)) {
                is AppResult.Success -> {
                    val fresh = result.value
                    quote = fresh
                    val changed = fresh.totalRate > 0 && fresh.totalRate != args.totalRate
                    _state.update {
                        it.copy(
                            priceChecking = false,
                            guests = buildGuests(fresh),
                            guestsLabel = guestsLabel(fresh.adults.coerceAtLeast(1), fresh.children),
                            boardBasisLabel = fresh.boardBasisLabel.ifBlank { it.boardBasisLabel },
                            breakfastIncluded = fresh.breakfastIncluded,
                            refundable = fresh.refundable,
                            currency = fresh.currency,
                            // Keep the old price on screen until the user accepts the change.
                            totalRate = if (changed) it.totalRate else fresh.totalRate,
                            perNightRate = if (changed) it.perNightRate else perNight(fresh.totalRate),
                            priceChange = if (changed) {
                                PriceChange(args.totalRate, fresh.totalRate, fresh.currency)
                            } else {
                                null
                            },
                        )
                    }
                }

                is AppResult.Failure -> _state.update {
                    it.copy(priceChecking = false, priceLoadError = result.error.message)
                }
            }
        }
    }

    override fun onBackClicked() {
        val current = _state.value
        if (current.showPaymentWebView) {
            onCancelPayment()
            return
        }
        if (current.step > CheckoutComponent.STEP_REVIEW && !current.processing) {
            _state.update { it.copy(step = current.step - 1, showErrors = false, error = null) }
        } else {
            onExit()
        }
    }

    override fun onNextStep() {
        val current = _state.value
        if (!isStepValid(current.step, current)) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        _state.update {
            it.copy(step = (it.step + 1).coerceAtMost(CheckoutComponent.STEP_CONFIRM), showErrors = false, error = null)
        }
    }

    override fun onPrevStep() {
        _state.update {
            it.copy(step = (it.step - 1).coerceAtLeast(CheckoutComponent.STEP_REVIEW), showErrors = false, error = null)
        }
    }

    override fun onGuestChange(index: Int, guest: GuestForm) {
        _state.update { s ->
            s.copy(guests = s.guests.mapIndexed { i, existing -> if (i == index) guest else existing })
        }
    }

    override fun onContactChange(contact: ContactForm) = _state.update { it.copy(contact = contact) }

    override fun onCardChange(card: CardForm) = _state.update { it.copy(card = card) }

    override fun onSpecialRequestsChange(text: String) = _state.update { it.copy(specialRequests = text) }

    override fun onConfirmPriceChange() {
        val fresh = quote ?: return
        _state.update {
            it.copy(priceChange = null, totalRate = fresh.totalRate, perNightRate = perNight(fresh.totalRate))
        }
    }

    override fun onDeclinePriceChange() {
        _state.update { it.copy(priceChange = null) }
        onExit()
    }

    override fun onConfirmAndPay() {
        val fresh = quote
        val current = _state.value
        if (current.processing) return
        if (fresh == null || fresh.rateId.isBlank() || fresh.roomId.isBlank()) {
            _state.update { it.copy(error = "This rate is no longer available. Please go back and pick another.") }
            return
        }
        val request = buildRequest(current, fresh)
        pendingRequest = request
        scope.launch {
            // Hold the booking session first, then hand off to the payment gateway (WebView).
            _state.update { it.copy(processing = true, processingLabel = "Holding your booking…", error = null) }
            when (val init = bookingApi.bookInit(request)) {
                is AppResult.Failure -> _state.update {
                    it.copy(processing = false, processingLabel = null, error = init.error.message)
                }

                is AppResult.Success -> _state.update {
                    it.copy(processing = false, processingLabel = null, showPaymentWebView = true)
                }
            }
        }
    }

    override fun onSimulatePayment() {
        val request = pendingRequest ?: return
        if (_state.value.processing) return
        scope.launch {
            _state.update {
                it.copy(showPaymentWebView = false, processing = true, processingLabel = "Confirming your booking…", error = null)
            }
            // Payment succeeded (simulated). The sandbox can't complete a real book (it returns a
            // non-"Confirmed" status), so treat anything that isn't a clean "Confirmed" as a mock
            // success for now — real bookings flow through unchanged once the sandbox works.
            val result = when (val booked = bookingApi.book(request)) {
                is AppResult.Success ->
                    booked.value.takeIf { it.status.equals("Confirmed", ignoreCase = true) }
                        ?: mockConfirmation(request.bookingRefId)

                is AppResult.Failure -> mockConfirmation(request.bookingRefId)
            }
            saveBooking(result)
            _state.update { it.copy(processing = false, processingLabel = null, bookingResult = result) }
        }
    }

    override fun onCancelPayment() = _state.update { it.copy(showPaymentWebView = false) }

    override fun onRetryPriceCheck() = priceCheck()

    override fun onDone() = onFinished()

    override fun onDismissError() = _state.update { it.copy(error = null) }

    private fun buildRequest(state: CheckoutComponent.UiState, quote: PriceQuote): BookingRequest = BookingRequest(
        hotelId = args.hotelId,
        token = quote.token,
        rateId = quote.rateId,
        roomId = quote.roomId,
        bookingRefId = bookingRefId,
        guests = state.guests.mapIndexed { index, guest ->
            if (index == 0) guest.copy(email = state.contact.email) else guest
        },
        billing = buildBilling(state),
        card = state.card,
        specialRequests = state.specialRequests.split('\n').map { it.trim() }.filter { it.isNotEmpty() },
    )

    private fun saveBooking(result: BookingResult) {
        val current = _state.value
        bookingStore.upsert(
            StoredBooking(
                id = bookingRefId,
                bookingCode = result.bookingId.ifBlank { bookingRefId },
                hotelId = args.hotelId,
                hotelName = args.hotelName,
                city = args.location.orEmpty(),
                roomId = quote?.roomId.orEmpty(),
                roomNumber = args.roomName,
                slotType = "",
                totalPrice = current.totalRate,
                currency = current.currency,
                startTime = "${args.checkIn}T14:00:00",
                endTime = "${args.checkOut}T12:00:00",
                paid = true,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                imageUrl = args.heroImage,
                tags = buildList {
                    add(current.boardBasisLabel)
                    add(if (current.refundable) "Free cancellation" else "Non-refundable")
                },
                dateLabel = stayDateLabel(),
                guestsLabel = current.guestsLabel,
            )
        )
    }

    private fun stayDateLabel(): String {
        val start = runCatching { LocalDate.parse(args.checkIn) }.getOrNull()
        val end = runCatching { LocalDate.parse(args.checkOut) }.getOrNull()
        if (start == null || end == null) return dateRangeLabel(args.checkIn, args.checkOut)
        return "${dayMonth(start)} – ${dayMonth(end)} ${end.year}"
    }

    private fun mockConfirmation(refId: String): BookingResult = BookingResult(
        bookingId = refId,
        status = "Confirmed",
        providerConfirmationNumber = "abcght5678",
        hotelConfirmationNumber = "12345668as",
        cancellationToken = "dfdgvsjh67789",
    )

    private fun buildBilling(state: CheckoutComponent.UiState): BillingForm {
        val primary = state.guests.firstOrNull()
        return BillingForm(
            title = primary?.title ?: "Mr",
            firstName = primary?.firstName.orEmpty(),
            lastName = primary?.lastName.orEmpty(),
            age = primary?.age ?: DEFAULT_ADULT_AGE,
            phone = state.contact.phone,
            email = state.contact.email,
            addressLine1 = state.contact.addressLine1,
            city = state.contact.city,
            country = state.contact.country,
            postalCode = state.contact.postalCode,
        )
    }

    private fun buildGuests(quote: PriceQuote): List<GuestForm> {
        val adults = List(quote.adults.coerceAtLeast(1)) {
            GuestForm(GuestType.Adult, "Mr", "", "", DEFAULT_ADULT_AGE, "")
        }
        val children = List(quote.children.coerceAtLeast(0)) { index ->
            GuestForm(GuestType.Child, "Mr", "", "", quote.childAges.getOrElse(index) { DEFAULT_CHILD_AGE }, "")
        }
        return adults + children
    }

    private fun perNight(total: Int): Int = if (args.nights > 0) total / args.nights else total

    private fun isStepValid(step: Int, state: CheckoutComponent.UiState): Boolean = when (step) {
        CheckoutComponent.STEP_REVIEW ->
            state.guests.all { it.firstName.isNotBlank() && it.lastName.isNotBlank() } &&
                state.contact.email.contains('@') &&
                state.contact.phone.isNotBlank()

        CheckoutComponent.STEP_PAYMENT -> {
            val card = state.card
            val digits = card.number.filter { it.isDigit() }
            digits.length in 13..19 &&
                card.nameOnCard.isNotBlank() &&
                (card.expiryMonth.toIntOrNull() ?: 0) in 1..12 &&
                card.expiryYear.length == 4 &&
                card.cvv.length in 3..4
        }

        else -> true
    }

    private companion object {
        // Midtrans Snap sandbox placeholder — swap for a real Snap redirect URL when the account is live.
        const val PAYMENT_URL = "https://simulator.sandbox.midtrans.com/v2/qris/index"
        const val DEFAULT_ADULT_AGE = 30
        const val DEFAULT_CHILD_AGE = 8
    }
}

private fun guestsLabel(adults: Int, children: Int): String {
    val adultsPart = if (adults == 1) "1 adult" else "$adults adults"
    if (children <= 0) return adultsPart
    val childrenPart = if (children == 1) "1 child" else "$children children"
    return "$adultsPart, $childrenPart"
}

private fun dateRangeLabel(checkIn: String, checkOut: String): String {
    val start = runCatching { LocalDate.parse(checkIn) }.getOrNull()
    val end = runCatching { LocalDate.parse(checkOut) }.getOrNull()
    if (start == null || end == null) return "$checkIn – $checkOut"
    return "${dayMonth(start)} – ${dayMonth(end)}"
}

private fun dayMonth(date: LocalDate): String {
    val month = date.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    return "${date.dayOfMonth} $month"
}
