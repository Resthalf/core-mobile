package com.resthalflab.resthalfapp.feature.wholesale.data

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.core.network.safeApiCall
import com.resthalflab.resthalfapp.feature.wholesale.api.BillingForm
import com.resthalflab.resthalfapp.feature.wholesale.api.BookingRequest
import com.resthalflab.resthalfapp.feature.wholesale.api.BookingResult
import com.resthalflab.resthalfapp.feature.wholesale.api.CardForm
import com.resthalflab.resthalfapp.feature.wholesale.api.GuestForm
import com.resthalflab.resthalfapp.feature.wholesale.api.GuestType
import com.resthalflab.resthalfapp.feature.wholesale.api.PriceQuote
import com.resthalflab.resthalfapp.feature.wholesale.api.WholesaleBookingApi
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.BillingContactDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.BookAddressDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.BookRequestDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.BookResponseDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.CardContactDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.ContactDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.CreditCardDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.GuestDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.NameCodeDto
import com.resthalflab.resthalfapp.feature.wholesale.data.dto.RoomAllocationDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Maps the checkout form bundle to the Nexus booking DTOs and back. Visa is the only accepted card
 * issuer right now (issuer code "VI").
 */
class DefaultWholesaleBookingApi(
    private val nexusRemote: NexusRemote,
) : WholesaleBookingApi {

    override suspend fun priceCheck(
        hotelId: String,
        token: String,
        recommendationId: String,
    ): AppResult<PriceQuote> = safeApiCall {
        withContext(Dispatchers.Default) {
            val response = nexusRemote.priceCheck(hotelId, token, recommendationId)
            val rate = response.hotel?.rates?.firstOrNull()
            val occupancy = rate?.occupancies?.firstOrNull()
            val board = rate?.boardBasis?.type
            PriceQuote(
                token = response.token?.ifBlank { null } ?: token,
                rateId = rate?.id.orEmpty(),
                roomId = occupancy?.roomId.orEmpty(),
                totalRate = (rate?.totalRate ?: response.totalRate).roundToInt(),
                currency = response.currency?.ifBlank { null } ?: rate?.currency?.ifBlank { null } ?: CURRENCY,
                boardBasisLabel = boardBasisLabel(board),
                breakfastIncluded = board.includesBreakfast(),
                refundable = rate?.refundable ?: rate?.refundability.equals("Refundable", ignoreCase = true),
                adults = occupancy?.numOfAdults?.toIntOrNull() ?: 1,
                children = occupancy?.numOfChildren?.toIntOrNull() ?: 0,
                childAges = occupancy?.childAges.orEmpty(),
            )
        }
    }

    override suspend fun bookInit(request: BookingRequest): AppResult<BookingResult> = safeApiCall {
        withContext(Dispatchers.Default) {
            nexusRemote.bookInit(request.hotelId, request.token, request.toDto()).toResult()
        }
    }

    override suspend fun book(request: BookingRequest): AppResult<BookingResult> = safeApiCall {
        withContext(Dispatchers.Default) {
            nexusRemote.book(request.hotelId, request.token, request.toDto()).toResult()
        }
    }

    private companion object {
        const val CURRENCY = "IDR"
        const val VISA_ISSUER = "VI"
    }

    private fun BookingRequest.toDto(): BookRequestDto = BookRequestDto(
        rateIds = listOf(rateId),
        bookingRefId = bookingRefId,
        specialRequests = specialRequests,
        roomsAllocations = listOf(
            RoomAllocationDto(
                roomid = roomId,
                rateid = rateId,
                guests = guests.map { it.toDto() },
            ),
        ),
        billingContact = billing.toBillingDto(),
        creditCard = card.toCardDto(billing),
    )

    private fun GuestForm.toDto(): GuestDto = GuestDto(
        type = if (type == GuestType.Child) "Child" else "Adult",
        title = title,
        firstname = firstName,
        lastname = lastName,
        age = age,
        email = email.ifBlank { null },
    )

    private fun BillingForm.toBillingDto(): BillingContactDto = BillingContactDto(
        title = title,
        firstName = firstName,
        lastName = lastName,
        age = age,
        contact = ContactDto(phone = phone, address = addressDto(), email = email),
    )

    private fun CardForm.toCardDto(billing: BillingForm): CreditCardDto = CreditCardDto(
        issuer = VISA_ISSUER,
        number = number,
        cvv = cvv,
        nameoncard = nameOnCard,
        expirymonth = expiryMonth,
        expiryyear = expiryYear,
        contact = CardContactDto(
            email = billing.email,
            phone = billing.phone,
            billingaddress = billing.addressDto(),
        ),
    )

    private fun BillingForm.addressDto(): BookAddressDto = BookAddressDto(
        line1 = addressLine1,
        city = NameCodeDto(name = city),
        country = NameCodeDto(name = country),
        postalCode = postalCode,
    )

    private fun BookResponseDto.toResult(): BookingResult = BookingResult(
        bookingId = bookingId.orEmpty(),
        status = bookingStatus?.ifBlank { null } ?: "Unknown",
        providerConfirmationNumber = providerConfirmationNumber,
        hotelConfirmationNumber = hotelConfirmationNumber,
        cancellationToken = cancellationToken,
    )
}

private fun String?.includesBreakfast(): Boolean = when {
    this == null -> false
    equals("RoomOnly", ignoreCase = true) -> false
    equals("BedAndBreakfast", ignoreCase = true) -> true
    equals("HalfBoard", ignoreCase = true) -> true
    equals("FullBoard", ignoreCase = true) -> true
    equals("AllInclusive", ignoreCase = true) -> true
    else -> contains("breakfast", ignoreCase = true)
}

private fun boardBasisLabel(board: String?): String = when {
    board == null || board.isBlank() -> "Room only"
    board.equals("RoomOnly", ignoreCase = true) -> "Room only"
    board.equals("BedAndBreakfast", ignoreCase = true) -> "Breakfast included"
    board.equals("HalfBoard", ignoreCase = true) -> "Breakfast + dinner"
    board.equals("FullBoard", ignoreCase = true) -> "All meals included"
    board.equals("AllInclusive", ignoreCase = true) -> "All-inclusive"
    else -> board
}
