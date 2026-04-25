package com.wheels.app.features.reviews.presentation.mock

import com.wheels.app.features.reviews.domain.model.ReviewAspectDefinition
import com.wheels.app.features.reviews.domain.model.ReviewAudience
import com.wheels.app.features.reviews.domain.model.ReviewComposerCopy
import com.wheels.app.features.reviews.domain.model.ReviewInsight
import com.wheels.app.features.reviews.domain.model.ReviewTagDefinition
import com.wheels.app.features.reviews.domain.model.ReviewVisibility
import com.wheels.app.features.reviews.domain.model.ReviewSubmission
import com.wheels.app.features.reviews.domain.model.ReviewDraft
import com.wheels.app.features.reviews.domain.model.ReviewChecklistItem
import com.wheels.app.features.reviews.domain.model.ReviewToneGuide
import com.wheels.app.features.reviews.domain.model.ReviewMetric
import com.wheels.app.features.reviews.domain.model.ReviewStatus
import com.wheels.app.features.reviews.domain.model.TripContext
import com.wheels.app.features.reviews.domain.model.buildReviewChecklist
import com.wheels.app.features.reviews.domain.model.buildToneGuideForRating

object ReviewComposerPreviewData {

    val copy = ReviewComposerCopy(
        title = "Add review and rating",
        subtitle = "Tell the next rider or driver what this trip felt like in a way that is specific, fair, and useful.",
        completionMessage = "Thanks for taking a minute to help the community improve one trip at a time.",
        draftReminder = "Your draft is saved locally in this isolated feature.",
        emptyHistoryTitle = "No review history yet",
        emptyHistorySubtitle = "Once the feature is connected, your submitted reviews will appear here."
    )

    val aspects = listOf(
        ReviewAspectDefinition(
            id = "punctuality",
            title = "Punctuality",
            description = "How close the pickup was to the expected time.",
            iconKey = "clock",
            highlightColorHex = 0xFF2563EB,
            positiveHint = "Arrived on time and kept me updated.",
            negativeHint = "Pickup was delayed without enough notice."
        ),
        ReviewAspectDefinition(
            id = "communication",
            title = "Communication",
            description = "Clarity, responsiveness, and ride coordination.",
            iconKey = "chat",
            highlightColorHex = 0xFF1A3A5C,
            positiveHint = "Clear messages before and during the ride.",
            negativeHint = "I had to chase for updates."
        ),
        ReviewAspectDefinition(
            id = "safety",
            title = "Safety",
            description = "Driving style and confidence on the road.",
            iconKey = "shield",
            highlightColorHex = 0xFF10B981,
            positiveHint = "Calm, steady, and attentive driving.",
            negativeHint = "Speeding or abrupt maneuvers made it uncomfortable."
        ),
        ReviewAspectDefinition(
            id = "cleanliness",
            title = "Cleanliness",
            description = "Car interior and overall ride comfort.",
            iconKey = "sparkles",
            highlightColorHex = 0xFFF59E0B,
            positiveHint = "Clean cabin and a tidy seating area.",
            negativeHint = "The vehicle felt neglected or cluttered."
        ),
        ReviewAspectDefinition(
            id = "route",
            title = "Route clarity",
            description = "Whether the route felt efficient and well explained.",
            iconKey = "route",
            highlightColorHex = 0xFF7C3AED,
            positiveHint = "Good route choice and easy to follow.",
            negativeHint = "The route felt confusing or unnecessarily long."
        ),
        ReviewAspectDefinition(
            id = "respect",
            title = "Respect",
            description = "Courtesy, boundaries, and general behavior.",
            iconKey = "handshake",
            highlightColorHex = 0xFF0F766E,
            positiveHint = "Friendly and respectful from start to finish.",
            negativeHint = "Tone or behavior felt dismissive."
        )
    )

    val tags = listOf(
        ReviewTagDefinition("on_time", "On time", "Great when the ride matched the schedule.", "clock", 4..5),
        ReviewTagDefinition("friendly", "Friendly", "Warm and easy to talk to.", "smile", 4..5),
        ReviewTagDefinition("clean_car", "Clean car", "Interior looked cared for and comfortable.", "car", 4..5),
        ReviewTagDefinition("safe_drive", "Safe driving", "Smooth and steady driving style.", "shield", 4..5),
        ReviewTagDefinition("good_chat", "Good conversation", "Helpful when you want a chat-friendly trip.", "chat", 4..5),
        ReviewTagDefinition("quiet_ride", "Quiet ride", "Perfect if you prefer minimal conversation.", "volume_off", 3..5),
        ReviewTagDefinition("clear_pickup", "Clear pickup", "Easy to find and coordinate.", "map", 4..5),
        ReviewTagDefinition("fast_reply", "Fast replies", "Messages were answered quickly.", "send", 4..5),
        ReviewTagDefinition("respectful", "Respectful", "Boundaries were handled well.", "badge", 4..5),
        ReviewTagDefinition("helpful", "Helpful", "Went beyond the basics to help out.", "support", 4..5),
        ReviewTagDefinition("needs_followup", "Needs follow up", "Use when support should review the trip.", "report", 1..3),
        ReviewTagDefinition("late_notice", "Late notice", "Pickup delay happened with little warning.", "warning", 1..3),
        ReviewTagDefinition("route_issue", "Route issue", "Route or navigation seemed off.", "route", 1..3),
        ReviewTagDefinition("payment_clear", "Payment clear", "Price and payment flow were straightforward.", "paid", 4..5),
        ReviewTagDefinition("comfortable", "Comfortable", "Seat, space, and cabin felt good.", "seat", 4..5)
    )

    val seedTrips = listOf(
        TripContext(
            id = "trip_001",
            routeName = "North Campus to Downtown",
            origin = "North Campus Gate",
            destination = "Central Library",
            dateLabel = "Apr 24, 2026",
            timeLabel = "7:30 AM",
            seats = 2,
            pricePerSeat = 9800,
            driverName = "Laura Gomez",
            driverInitials = "LG",
            driverRating = 4.9,
            vehicleLabel = "Kia Rio - Silver",
            verifiedDriver = true,
            completedMinutesAgo = 36,
            note = "Morning commute with two passengers and light traffic.",
            badgeLabel = "Ready to review"
        ),
        TripContext(
            id = "trip_002",
            routeName = "Main Hall to South Gate",
            origin = "Main Hall",
            destination = "South Gate",
            dateLabel = "Apr 23, 2026",
            timeLabel = "5:15 PM",
            seats = 3,
            pricePerSeat = 11200,
            driverName = "Andres Molina",
            driverInitials = "AM",
            driverRating = 4.8,
            vehicleLabel = "Chevrolet Onix - White",
            verifiedDriver = true,
            completedMinutesAgo = 250,
            note = "Smooth afternoon ride after class.",
            badgeLabel = "Recently completed"
        ),
        TripContext(
            id = "trip_003",
            routeName = "Library Loop",
            origin = "Library Plaza",
            destination = "Engineering Block",
            dateLabel = "Apr 22, 2026",
            timeLabel = "1:05 PM",
            seats = 1,
            pricePerSeat = 6500,
            driverName = "Camila Rojas",
            driverInitials = "CR",
            driverRating = 5.0,
            vehicleLabel = "Renault Sandero - Blue",
            verifiedDriver = true,
            completedMinutesAgo = 1_070,
            note = "Short midday trip with one passenger.",
            badgeLabel = "Popular route"
        ),
        TripContext(
            id = "trip_004",
            routeName = "Sports Center Shuttle",
            origin = "Sports Center",
            destination = "Residence Halls",
            dateLabel = "Apr 21, 2026",
            timeLabel = "8:45 PM",
            seats = 4,
            pricePerSeat = 7600,
            driverName = "Sebastian Perez",
            driverInitials = "SP",
            driverRating = 4.7,
            vehicleLabel = "Mazda 2 - Gray",
            verifiedDriver = true,
            completedMinutesAgo = 1_920,
            note = "Evening shuttle after training.",
            badgeLabel = "Archived"
        ),
        TripContext(
            id = "trip_005",
            routeName = "Airport Connector",
            origin = "Campus Exit",
            destination = "Airport Terminal 1",
            dateLabel = "Apr 19, 2026",
            timeLabel = "4:10 AM",
            seats = 2,
            pricePerSeat = 25500,
            driverName = "Valentina Castro",
            driverInitials = "VC",
            driverRating = 5.0,
            vehicleLabel = "Toyota Corolla - Black",
            verifiedDriver = true,
            completedMinutesAgo = 4_080,
            note = "Long trip with luggage and early departure.",
            badgeLabel = "Archived"
        ),
        TripContext(
            id = "trip_006",
            routeName = "Weekend Grocery Run",
            origin = "North Residences",
            destination = "Mall Plaza",
            dateLabel = "Apr 18, 2026",
            timeLabel = "3:40 PM",
            seats = 3,
            pricePerSeat = 9100,
            driverName = "Diego Herrera",
            driverInitials = "DH",
            driverRating = 4.6,
            vehicleLabel = "Hyundai Accent - Blue",
            verifiedDriver = false,
            completedMinutesAgo = 5_530,
            note = "Afternoon trip for errands and groceries.",
            badgeLabel = "Archived"
        )
    )

    val seedDraft = ReviewDraft(
        tripId = "trip_001",
        rating = 4,
        audience = ReviewAudience.DRIVER,
        visibility = ReviewVisibility.PUBLIC,
        selectedAspectIds = setOf("punctuality", "communication"),
        selectedTagIds = setOf("on_time", "friendly", "clear_pickup"),
        comment = "The ride was smooth, the driver kept me updated, and pickup happened on time. A tiny delay at the entrance was handled well.",
        privateNote = "Mention how clean the car felt and that the route stayed efficient.",
        isAnonymous = false,
        followUpRequested = false,
        createdAtLabel = "2 min ago",
        updatedAtLabel = "Just now",
        status = ReviewStatus.READY_TO_SUBMIT
    )

    val seedSubmissions = listOf(
        ReviewSubmission(
            id = "rev_001",
            tripId = "trip_003",
            tripRouteLabel = "Library Loop",
            driverName = "Camila Rojas",
            driverInitials = "CR",
            rating = 5,
            comment = "Perfect short trip. The car was spotless, pickup was clear, and the conversation was easy without feeling forced.",
            audience = ReviewAudience.DRIVER,
            visibility = ReviewVisibility.PUBLIC,
            selectedAspectIds = listOf("cleanliness", "communication", "respect"),
            selectedTagIds = listOf("clean_car", "quiet_ride", "respectful"),
            isAnonymous = false,
            submittedAtLabel = "1 hour ago",
            helpfulVotes = 18,
            replyCount = 2
        ),
        ReviewSubmission(
            id = "rev_002",
            tripId = "trip_005",
            tripRouteLabel = "Airport Connector",
            driverName = "Valentina Castro",
            driverInitials = "VC",
            rating = 5,
            comment = "Early airport ride and zero stress. Great communication the night before, careful driving, and plenty of room for luggage.",
            audience = ReviewAudience.DRIVER,
            visibility = ReviewVisibility.PUBLIC,
            selectedAspectIds = listOf("punctuality", "safety", "route"),
            selectedTagIds = listOf("safe_drive", "fast_reply", "helpful"),
            isAnonymous = false,
            submittedAtLabel = "3 hours ago",
            helpfulVotes = 31,
            replyCount = 4
        ),
        ReviewSubmission(
            id = "rev_003",
            tripId = "trip_002",
            tripRouteLabel = "Main Hall to South Gate",
            driverName = "Andres Molina",
            driverInitials = "AM",
            rating = 4,
            comment = "Solid ride overall. We left a few minutes late, but the driver kept me informed and the rest of the trip was comfortable.",
            audience = ReviewAudience.DRIVER,
            visibility = ReviewVisibility.TRIP_ONLY,
            selectedAspectIds = listOf("communication", "comfort", "punctuality"),
            selectedTagIds = listOf("good_chat", "comfortable", "late_notice"),
            isAnonymous = true,
            submittedAtLabel = "Yesterday",
            helpfulVotes = 5,
            replyCount = 1
        ),
        ReviewSubmission(
            id = "rev_004",
            tripId = "trip_004",
            tripRouteLabel = "Sports Center Shuttle",
            driverName = "Sebastian Perez",
            driverInitials = "SP",
            rating = 5,
            comment = "He handled a full car politely and kept everything calm. Nice atmosphere, safe driving, and great timing at the pickup spot.",
            audience = ReviewAudience.PASSENGER,
            visibility = ReviewVisibility.PUBLIC,
            selectedAspectIds = listOf("respect", "safety", "route"),
            selectedTagIds = listOf("friendly", "safe_drive", "on_time"),
            isAnonymous = false,
            submittedAtLabel = "2 days ago",
            helpfulVotes = 12,
            replyCount = 0
        ),
        ReviewSubmission(
            id = "rev_005",
            tripId = "trip_006",
            tripRouteLabel = "Weekend Grocery Run",
            driverName = "Diego Herrera",
            driverInitials = "DH",
            rating = 3,
            comment = "The trip got there, but the pickup instructions were not very clear and the arrival window changed twice. The ride itself was fine.",
            audience = ReviewAudience.DRIVER,
            visibility = ReviewVisibility.PRIVATE,
            selectedAspectIds = listOf("communication", "punctuality"),
            selectedTagIds = listOf("route_issue", "needs_followup"),
            isAnonymous = true,
            submittedAtLabel = "4 days ago",
            helpfulVotes = 2,
            replyCount = 0
        )
    )

    val toneGuideForSelectedDraft: ReviewToneGuide
        get() = buildToneGuideForRating(seedDraft.rating)

    val checklistForSelectedDraft: List<ReviewChecklistItem>
        get() = buildReviewChecklist(seedDraft)

    val overviewMetrics = listOf(
        ReviewMetric("Trips reviewed", "5", "One review for each completed trip"),
        ReviewMetric("Draft status", seedDraft.status.label, "Local isolated state"),
        ReviewMetric("Selected audience", seedDraft.audience.label, "Targeted feedback mode"),
        ReviewMetric("Visibility", seedDraft.visibility.label, "How the review is shared")
    )

    val spotlightInsights = listOf(
        ReviewInsight(
            title = "Average rating",
            value = "4.4",
            subtitle = "Based on the five sample submissions included with the isolated feature.",
            trendLabel = "Strong",
            positiveTrend = true
        ),
        ReviewInsight(
            title = "Reply rate",
            value = "60%",
            subtitle = "Three of the five reviews received replies.",
            trendLabel = "Healthy",
            positiveTrend = true
        ),
        ReviewInsight(
            title = "Anonymous reviews",
            value = "2",
            subtitle = "Useful when the person wants feedback without public attribution.",
            trendLabel = "Privacy aware",
            positiveTrend = true
        )
    )

    val sampleCopyFlow = listOf(
        copy.title,
        copy.subtitle,
        copy.completionMessage,
        copy.draftReminder,
        copy.emptyHistoryTitle,
        copy.emptyHistorySubtitle
    )

    fun checklistForDraft(draft: ReviewDraft): List<ReviewChecklistItem> = buildReviewChecklist(draft)

    fun toneGuideForRating(rating: Int): ReviewToneGuide = buildToneGuideForRating(rating)
}

