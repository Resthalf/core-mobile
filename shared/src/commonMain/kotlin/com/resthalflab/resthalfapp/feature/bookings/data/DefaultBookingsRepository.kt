package com.resthalflab.resthalfapp.feature.bookings.data

import com.resthalflab.resthalfapp.core.domain.AppResult
import com.resthalflab.resthalfapp.feature.bookings.domain.BookingsRepository
import com.resthalflab.resthalfapp.feature.bookings.domain.model.Booking
import com.resthalflab.resthalfapp.feature.bookings.domain.model.BookingStatus
import kotlinx.coroutines.delay

// Phase 3 stub. Replace with real API calls.
class DefaultBookingsRepository : BookingsRepository {
    override suspend fun getBookings(): AppResult<List<Booking>> {
        delay(300)
        return AppResult.Success(stub)
    }

    override suspend fun getBookingById(id: String): AppResult<Booking> {
        delay(100)
        return stub.find { it.id == id }
            ?.let { AppResult.Success(it) }
            ?: AppResult.Failure(com.resthalflab.resthalfapp.core.domain.AppError.Unknown("Booking not found"))
    }

    private val stub = listOf(
        Booking(
            id = "0",
            bookingCode = "RH2405241234",
            hotelName = "Amaris Hotel Thamrin",
            city = "Jakarta",
            dateLabel = "24 May 2024",
            stayWindow = "12:00 AM – 12:00 PM",
            totalPrice = 260_000,
            currency = "IDR",
            status = BookingStatus.Active,
            thumbnailUrl = "https://picsum.photos/seed/aria_1/300/200",
        ),
        Booking(
            id = "1",
            bookingCode = "RH2405241234",
            hotelName = "Amaris Hotel Thamrin",
            city = "Jakarta",
            dateLabel = "24 May 2024",
            stayWindow = "12:00 AM – 12:00 PM",
            totalPrice = 260_000,
            currency = "IDR",
            status = BookingStatus.Completed,
            thumbnailUrl = "https://picsum.photos/seed/aria_1/300/200",
        ),
        Booking(
            id = "2",
            bookingCode = "RH1005240987",
            hotelName = "Yello Hotel Harmoni",
            city = "Jakarta",
            dateLabel = "10 May 2024",
            stayWindow = "12:00 AM – 12:00 PM",
            totalPrice = 220_000,
            currency = "IDR",
            status = BookingStatus.Completed,
            thumbnailUrl = "https://picsum.photos/seed/mawar_1/300/200",
        ),
        Booking(
            id = "3",
            bookingCode = "RH0205240771",
            hotelName = "favehotel LTC Glodok",
            city = "Jakarta",
            dateLabel = "02 May 2024",
            stayWindow = "12:00 AM – 12:00 PM",
            totalPrice = 180_000,
            currency = "IDR",
            status = BookingStatus.Cancelled,
            thumbnailUrl = "https://picsum.photos/seed/samudra_1/300/200",
        ),
        Booking(
            id = "4",
            bookingCode = "RH2804240660",
            hotelName = "G Suites Hotel",
            city = "Jakarta",
            dateLabel = "28 Apr 2024",
            stayWindow = "12:00 AM – 12:00 PM",
            totalPrice = 300_000,
            currency = "IDR",
            status = BookingStatus.Overstayed,
            thumbnailUrl = "https://picsum.photos/seed/rinjani_1/300/200",
        ),
        Booking(
            id = "5",
            bookingCode = "RH2804240660",
            hotelName = "Marriot Hotel",
            city = "Jakarta",
            dateLabel = "28 Apr 2024",
            stayWindow = "12:00 AM – 12:00 PM",
            totalPrice = 300_000,
            currency = "IDR",
            status = BookingStatus.Active,
            thumbnailUrl = "https://picsum.photos/seed/rinjani_1/300/200",
        ),
        Booking(
            id = "6",
            bookingCode = "RH2804240660",
            hotelName = "Aston Inn Yogyakarta",
            city = "Yogyakarta",
            dateLabel = "28 Apr 2024",
            stayWindow = "12:00 AM – 12:00 PM",
            totalPrice = 300_000,
            currency = "IDR",
            status = BookingStatus.Active,
            thumbnailUrl = "https://picsum.photos/seed/rinjani_1/300/200",
        ),
        Booking(
            id = "7",
            bookingCode = "RH2804240660",
            hotelName = "G Suites Hotel",
            city = "Jakarta",
            dateLabel = "28 Apr 2024",
            stayWindow = "12:00 AM – 12:00 PM",
            totalPrice = 300_000,
            currency = "IDR",
            status = BookingStatus.Overstayed,
            thumbnailUrl = "https://picsum.photos/seed/rinjani_1/300/200",
        ),
        Booking(
            id = "8",
            bookingCode = "RH2804240660",
            hotelName = "G Suites Hotel",
            city = "Jakarta",
            dateLabel = "28 Apr 2024",
            stayWindow = "12:00 AM – 12:00 PM",
            totalPrice = 300_000,
            currency = "IDR",
            status = BookingStatus.Overstayed,
            thumbnailUrl = "https://picsum.photos/seed/rinjani_1/300/200",
        ),
        Booking(
            id = "9",
            bookingCode = "RH2804240660",
            hotelName = "G Suites Hotel",
            city = "Jakarta",
            dateLabel = "28 Apr 2024",
            stayWindow = "12:00 AM – 12:00 PM",
            totalPrice = 300_000,
            currency = "IDR",
            status = BookingStatus.Overstayed,
            thumbnailUrl = "https://picsum.photos/seed/rinjani_1/300/200",
        ),
    )
}
