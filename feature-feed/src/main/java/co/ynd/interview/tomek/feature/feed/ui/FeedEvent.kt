package co.ynd.interview.tomek.feature.feed.ui

sealed interface FeedEvent {
    data object DeleteFailed : FeedEvent
}
