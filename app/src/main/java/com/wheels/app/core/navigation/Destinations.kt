package com.wheels.app.core.navigation

import android.net.Uri

sealed class Destinations(val route: String) {
    data object SessionGate : Destinations("session_gate")
    data object SignIn : Destinations("sign_in")
    data object CreateAccount : Destinations("create_account")
    data object ForgotPassword : Destinations("forgot_password")
    data object Home : Destinations("home")
    data object Rides : Destinations("rides")
    data object CreateRideDrafts : Destinations("create_ride_drafts")
    data object PendingRideSync : Destinations("pending_ride_sync")
    data object Payments : Destinations("payments")
    data object QuickPayment : Destinations("quick_payment")
    data object MessagesInbox : Destinations("messages_inbox")
    data object MessageChat : Destinations("message_chat/{rideId}") {
        fun createRoute(rideId: String): String = "message_chat/${Uri.encode(rideId)}"
    }
    data object RideRequest : Destinations("ride_request/{rideId}") {
        fun createRoute(rideId: String): String = "ride_request/$rideId"
    }
    data object ReviewFeedback : Destinations("review_feedback/{rideId}/{driverId}/{driverName}") {
        fun createRoute(rideId: String, driverId: String, driverName: String): String {
            return "review_feedback/${Uri.encode(rideId)}/${Uri.encode(driverId)}/${Uri.encode(driverName)}"
        }
    }
    data object ActiveRideManagement : Destinations("active_ride_management/{rideId}") {
        fun createRoute(rideId: String): String = "active_ride_management/$rideId"
    }
    data object BookingConfirmation : Destinations("booking_confirmation/{rideId}/{seats}") {
        fun createRoute(rideId: String, seats: Int): String = "booking_confirmation/$rideId/$seats"
    }
    data object GroupChat : Destinations("group_chat")
    data object ReviewsRatings : Destinations("reviews_ratings/{origin}/{driverId}/{driverName}") {
        fun createRoute(origin: String, driverId: String, driverName: String): String {
            return "reviews_ratings/${Uri.encode(origin)}/${Uri.encode(driverId)}/${Uri.encode(driverName)}"
        }
    }
    data object Profile : Destinations("profile")
    data object TrustFairness : Destinations("trust_fairness")
    data object UiTheme : Destinations("ui_theme")

    companion object {
        const val RIDES_NEARBY_REQUESTED_KEY = "rides_nearby_requested"
        const val RIDES_NEARBY_LOCATION_NAME_KEY = "rides_nearby_location_name"
        const val MESSAGE_CHAT_RIDE_ID_KEY = "rideId"
        const val QUICK_PAY_RIDE_ID_KEY = "quick_pay_ride_id"
        const val QUICK_PAY_DRIVER_ID_KEY = "quick_pay_driver_id"
        const val QUICK_PAY_DRIVER_NAME_KEY = "quick_pay_driver_name"
        const val QUICK_PAY_FARE_KEY = "quick_pay_fare"
        const val QUICK_PAY_FROM_KEY = "quick_pay_from"
        const val QUICK_PAY_TO_KEY = "quick_pay_to"
        const val QUICK_PAY_DATE_KEY = "quick_pay_date"
        const val REVIEW_FEEDBACK_QUEUE_NOTICE_KEY = "review_feedback_queue_notice"
    }
}
