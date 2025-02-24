package com.jerboa.ui.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.jerboa.R
import com.jerboa.VoteType
import com.jerboa.db.Account
import com.jerboa.ui.theme.muted

@Composable
fun <T> VoteGeneric(
    myVote: Int?,
    votes: Int,
    item: T,
    type: VoteType,
    onVoteClick: (item: T) -> Unit,
    showNumber: Boolean = true,
    account: Account?
) {
    val iconAndColor = when (type) {
        VoteType.Upvote -> upvoteIconAndColor(myVote = myVote)
        else -> downvoteIconAndColor(myVote = myVote)
    }

    val votesStr = if (showNumber) {
        if (type == VoteType.Downvote && votes == 0) {
            null
        } else {
            votes.toString()
        }
    } else {
        null
    }
    ActionBarButton(
        onClick = { onVoteClick(item) },
        contentColor = iconAndColor.second,
        icon = iconAndColor.first,
        text = votesStr,
        account = account
    )
}

@Composable
fun upvoteIconAndColor(myVote: Int?): Pair<ImageVector, Color> {
    return when (myVote) {
        1 -> Pair(
            Icons.Filled.ArrowUpward,
            scoreColor(myVote = myVote)
        )
        else -> Pair(
            Icons.Outlined.ArrowUpward,
            MaterialTheme
                .colorScheme.onBackground.muted
        )
    }
}

@Composable
fun downvoteIconAndColor(myVote: Int?): Pair<ImageVector, Color> {
    return when (myVote) {
        -1 -> Pair(
            Icons.Filled.ArrowDownward,
            scoreColor(myVote = myVote)
        )
        else -> Pair(
            Icons.Outlined.ArrowDownward,
            MaterialTheme
                .colorScheme.onBackground.muted
        )
    }
}
