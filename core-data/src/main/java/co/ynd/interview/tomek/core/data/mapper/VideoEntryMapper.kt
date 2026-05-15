package co.ynd.interview.tomek.core.data.mapper

import co.ynd.interview.tomek.core.domain.model.VideoEntry as DomainVideoEntry
import co.ynd.interview.tomek.core.database.VideoEntry as DbVideoEntry

fun DbVideoEntry.toDomain(): DomainVideoEntry = DomainVideoEntry(
    id = id,
    filePath = filePath,
    description = description,
    createdAt = createdAt,
    durationMs = durationMs,
    thumbnailPath = thumbnailPath
)
