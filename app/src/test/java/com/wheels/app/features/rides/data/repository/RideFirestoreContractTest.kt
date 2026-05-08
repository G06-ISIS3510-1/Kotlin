package com.wheels.app.features.rides.data.repository

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RideFirestoreContractTest {

    @Test
    fun `published rides normalize to open and legacy seat and price fields are read`() {
        val ride = mapRideDocument(
            rideId = "ride-123",
            data = mapOf(
                "driverId" to "driver-1",
                "driverName" to "Carlos",
                "driverEmail" to "carlos@example.com",
                "origin" to "Campus",
                "destination" to "Usaquen",
                "departureAt" to Timestamp(1_700_000_000, 0),
                "seats" to 4,
                "price" to 3500,
                "availableSeats" to 3,
                "passengerIds" to listOf("passenger-1", "passenger-2"),
                "status" to "published",
                "paymentOption" to "bank_transfer"
            )
        )

        requireNotNull(ride)
        assertEquals("open", ride.status)
        assertEquals(4, ride.totalSeats)
        assertEquals(3, ride.availableSeats)
        assertEquals(3500.0, ride.pricePerSeat, 0.0)
        assertEquals("bank_transfer", ride.paymentOption)
        assertEquals(listOf("passenger-1", "passenger-2"), ride.passengerIds)
    }

    @Test
    fun `driver ride mapping also supports legacy seat and price aliases`() {
        val ride = mapDriverRideDocument(
            rideId = "ride-456",
            data = mapOf(
                "driverId" to "driver-1",
                "driverName" to "Carlos",
                "driverEmail" to "carlos@example.com",
                "origin" to "Campus",
                "destination" to "Suba",
                "departureAt" to Timestamp(1_700_000_000, 0),
                "seats" to 2,
                "price" to 4200,
                "status" to "open"
            )
        )

        requireNotNull(ride)
        assertEquals("open", ride.status)
        assertEquals(2, ride.totalSeats)
        assertEquals(4200, ride.pricePerSeat)
    }

    @Test
    fun `application decision blocks self application and overbooking and no-ops when application exists`() {
        val rideData = mapOf(
            "driverId" to "driver-1",
            "status" to "open",
            "availableSeats" to 1,
            "paymentOption" to "card"
        )

        val selfApply = decideRideApplication(
            rideId = "ride-1",
            rideData = rideData,
            passengerId = "driver-1",
            passengerName = "Carlos",
            passengerEmail = "carlos@example.com",
            existingApplicationExists = false
        )
        assertTrue(selfApply is ApplyRideDecision.Apply)
        assertEquals("driver_cannot_apply", (selfApply as ApplyRideDecision.Apply).application.statusDetail)

        val fullRide = decideRideApplication(
            rideId = "ride-1",
            rideData = rideData + ("availableSeats" to 0),
            passengerId = "passenger-1",
            passengerName = "Ana",
            passengerEmail = "ana@example.com",
            existingApplicationExists = false
        )
        assertTrue(fullRide is ApplyRideDecision.Apply)
        assertEquals("ride_is_full", (fullRide as ApplyRideDecision.Apply).application.statusDetail)

        val noOp = decideRideApplication(
            rideId = "ride-1",
            rideData = rideData,
            passengerId = "passenger-1",
            passengerName = "Ana",
            passengerEmail = "ana@example.com",
            existingApplicationExists = true
        )
        assertTrue(noOp is ApplyRideDecision.NoOp)
    }

    @Test
    fun `status transitions only allow the published contract`() {
        assertTrue(
            validateRideStatusTransition("open", "in_progress") is RideStatusTransitionDecision.Allowed
        )
        assertTrue(
            validateRideStatusTransition("in_progress", "completed") is RideStatusTransitionDecision.Allowed
        )
        assertTrue(
            validateRideStatusTransition("open", "cancelled") is RideStatusTransitionDecision.Allowed
        )
        assertTrue(
            validateRideStatusTransition("published", "cancelled") is RideStatusTransitionDecision.Allowed
        )
        assertTrue(
            validateRideStatusTransition("completed", "open") is RideStatusTransitionDecision.Rejected
        )
    }

    @Test
    fun `passenger home ride stays visible through completed but not cancelled`() {
        assertTrue(isPassengerHomeRideVisible("published"))
        assertTrue(isPassengerHomeRideVisible("open"))
        assertTrue(isPassengerHomeRideVisible("in_progress"))
        assertTrue(isPassengerHomeRideVisible("completed"))
        assertTrue(!isPassengerHomeRideVisible("cancelled"))
        assertTrue(!isPassengerHomeRideVisible("canceled"))
    }
}
