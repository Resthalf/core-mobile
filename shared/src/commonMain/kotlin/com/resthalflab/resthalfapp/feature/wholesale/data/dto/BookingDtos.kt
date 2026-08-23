package com.resthalflab.resthalfapp.feature.wholesale.data.dto

import kotlinx.serialization.Serializable

// ---- Price check (GET hotel/{hotelId}/{token}/price/recommendation/{recommendationId}) -----------
// Re-prices the chosen recommendation just before booking. The returned rate id + occupancy roomId
// are the authoritative ids to book with, and [totalRate] is compared against what the user saw.

@Serializable
data class PriceCheckResponseDto(
    val token: String? = null,
    val currency: String? = null,
    val totalRate: Double = 0.0,
    val hotel: PriceHotelDto? = null,
)

@Serializable
data class PriceHotelDto(
    val id: String? = null,
    val rates: List<PriceRateDto> = emptyList(),
)

@Serializable
data class PriceRateDto(
    val id: String? = null,
    val totalRate: Double = 0.0,
    val baseRate: Double = 0.0,
    val currency: String? = null,
    val refundable: Boolean = false,
    val refundability: String? = null,
    val boardBasis: BoardBasisDto? = null,
    val occupancies: List<PriceOccupancyDto> = emptyList(),
)

@Serializable
data class PriceOccupancyDto(
    val roomId: String? = null,
    val numOfAdults: String? = null,
    val numOfChildren: String? = null,
    val childAges: List<Int> = emptyList(),
)

// ---- Book init / Book (POST hotel/{hotelId}/{token}/bookinit and .../book) -----------------------
// Both endpoints share the same request shape; bookinit only holds the session, book confirms.

@Serializable
data class BookRequestDto(
    val rateIds: List<String>,
    val bookingRefId: String,
    val specialRequests: List<String> = emptyList(),
    val roomsAllocations: List<RoomAllocationDto>,
    val billingContact: BillingContactDto,
    val creditCard: CreditCardDto,
)

@Serializable
data class RoomAllocationDto(
    val roomid: String,
    val rateid: String,
    val guests: List<GuestDto>,
)

@Serializable
data class GuestDto(
    val type: String,
    val title: String,
    val firstname: String,
    val lastname: String,
    val age: Int,
    val email: String? = null,
)

@Serializable
data class BillingContactDto(
    val title: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val contact: ContactDto,
)

@Serializable
data class ContactDto(
    val phone: String,
    val address: BookAddressDto,
    val email: String,
)

@Serializable
data class CreditCardDto(
    val issuer: String,
    val number: String,
    val cvv: String,
    val nameoncard: String,
    val expirymonth: String,
    val expiryyear: String,
    val contact: CardContactDto,
)

@Serializable
data class CardContactDto(
    val email: String,
    val phone: String,
    val billingaddress: BookAddressDto,
)

@Serializable
data class BookAddressDto(
    val line1: String = "",
    val line2: String = "",
    val city: NameCodeDto = NameCodeDto(),
    val state: NameCodeDto = NameCodeDto(),
    val country: NameCodeDto = NameCodeDto(),
    val postalCode: String = "",
)

@Serializable
data class NameCodeDto(val name: String = "", val code: String = "")

@Serializable
data class BookResponseDto(
    val token: String? = null,
    val bookingStatus: String? = null,
    val bookingId: String? = null,
    val providerConfirmationNumber: String? = null,
    val hotelConfirmationNumber: String? = null,
    val cancellationToken: String? = null,
)
