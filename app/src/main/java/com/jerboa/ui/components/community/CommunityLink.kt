package com.jerboa.ui.components.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.jerboa.communityNameShown
import com.jerboa.datatypes.CommunitySafe
import com.jerboa.datatypes.CommunityView
import com.jerboa.datatypes.sampleCommunitySafe
import com.jerboa.datatypes.sampleCommunityView
import com.jerboa.ui.components.common.CircularIcon
import com.jerboa.ui.theme.DRAWER_ITEM_SPACING
import com.jerboa.ui.theme.ICON_SIZE
import com.jerboa.ui.theme.ICON_THUMBNAIL_SIZE
import com.jerboa.ui.theme.LARGER_ICON_THUMBNAIL_SIZE
import com.jerboa.ui.theme.LARGE_PADDING
import com.jerboa.ui.theme.LINK_ICON_SIZE
import com.jerboa.ui.theme.SMALL_PADDING

@Composable
fun CommunityName(
    community: CommunitySafe,
    color: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier
) {
    Text(
        text = communityNameShown(community),
        style = style,
        color = color,
        modifier = modifier,
        overflow = TextOverflow.Clip,
        maxLines = 1
    )
}

@Preview
@Composable
fun CommunityNamePreview() {
    CommunityName(community = sampleCommunitySafe)
}

@Composable
fun CommunityLink(
    modifier: Modifier = Modifier,
    community: CommunitySafe,
    usersPerMonth: Int? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    spacing: Dp = SMALL_PADDING,
    size: Dp = ICON_SIZE,
    thumbnailSize: Int = ICON_THUMBNAIL_SIZE,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    onClick: (community: CommunitySafe) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier.clickable { onClick(community) }
    ) {
        community.icon?.let {
            CircularIcon(
                icon = it,
                size = size,
                thumbnailSize = thumbnailSize
            )
        }
        Column {
            CommunityName(community = community, color = color, style = style)
            usersPerMonth?.also {
                Text(
                    text = "$usersPerMonth users / month",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun CommunityLinkLarger(
    community: CommunitySafe,
    onClick: (community: CommunitySafe) -> Unit
) {
    NavigationDrawerItem(
        label = { Text(community.name) },
        selected = false,
        icon = { community.icon?.let { CircularIcon(icon = it) } },
        onClick = { onClick.invoke(community) }
    )
//    CommunityLink(
//        community = community,
//        color = MaterialTheme.colorScheme.onSurface,
//        style = MaterialTheme.typography.titleLarge,
//        size = LINK_ICON_SIZE,
//        thumbnailSize = LARGER_ICON_THUMBNAIL_SIZE,
//        spacing = DRAWER_ITEM_SPACING,
//        modifier = Modifier
//            .padding(LARGE_PADDING)
//            .fillMaxWidth(),
//        onClick = onClick
//    )
}

@Composable
fun CommunityLinkLargerWithUserCount(
    communityView: CommunityView,
    onClick: (community: CommunitySafe) -> Unit
) {
    CommunityLink(
        community = communityView.community,
        usersPerMonth = communityView.counts.users_active_month,
        color = MaterialTheme.colorScheme.onSurface,
        size = LINK_ICON_SIZE,
        thumbnailSize = LARGER_ICON_THUMBNAIL_SIZE,
        spacing = DRAWER_ITEM_SPACING,
        modifier = Modifier
            .padding(LARGE_PADDING)
            .fillMaxWidth(),
        style = MaterialTheme.typography.titleLarge,
        onClick = onClick
    )
}

@Preview
@Composable
fun CommunityLinkPreview() {
    CommunityLink(
        community = sampleCommunitySafe,
        onClick = {}
    )
}

@Preview
@Composable
fun CommunityLinkWithUsersPreview() {
    CommunityLinkLargerWithUserCount(
        communityView = sampleCommunityView,
        onClick = {}
    )
}
