@file:OptIn(ExperimentalMaterial3Api::class)

package com.jerboa.ui.components.common

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.jerboa.datatypes.PersonSafe
import com.jerboa.datatypes.api.GetUnreadCountResponse
import com.jerboa.datatypes.samplePersonSafe
import com.jerboa.datatypes.samplePost
import com.jerboa.db.Account
import com.jerboa.loginFirstToast
import com.jerboa.siFormat
import com.jerboa.ui.components.person.PersonProfileLink
import com.jerboa.ui.theme.*
import com.jerboa.unreadCountTotal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopAppBar(
    text: String,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = text
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

@Composable
fun BottomAppBarAll(
    navController: NavController = rememberNavController(),
    screen: String,
    unreadCounts: GetUnreadCountResponse? = null,
    onClickSaved: () -> Unit,
    onClickProfile: () -> Unit,
    onClickInbox: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                if(screen == "home") {
                    Icon(Icons.Filled.Home, "TODO")
                } else {
                    Icon(Icons.Outlined.Home, "TODO")
                }
            },
            selected = screen == "home",
            onClick = {
                navController.navigate("home")
            }
        )
        NavigationBarItem(
            icon = {
                if(screen == "saved") {
                    Icon(Icons.Filled.Bookmarks, "TODO")
                } else {
                    Icon(Icons.Outlined.Bookmarks, "TODO")
                }
            },
            onClick = {
                onClickSaved()
            },
            selected = screen == "saved"
        )
        NavigationBarItem(
            icon = {
                if(screen == "inbox") {
                    Icon(Icons.Filled.Inbox, "TODO")
                } else {
                    Icon(Icons.Outlined.Inbox, "TODO")
                }
            },
            onClick = {
                onClickInbox()
            },
            selected = screen == "inbox"
        )
        NavigationBarItem(
            icon = {
                if(screen == "profile") {
                    Icon(Icons.Filled.Person, "TODO")
                } else {
                    Icon(Icons.Outlined.Person, "TODO")
                }
            },
            onClick = onClickProfile,
            selected = screen == "profile"
        )
    }
}

@Preview
@Composable
fun BottomAppBarAllPreview() {
    BottomAppBarAll(

        onClickInbox = {},
        onClickProfile = {},
        onClickSaved = {},
        screen = "home"
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentOrPostNodeHeader(
    creator: PersonSafe,
    score: Int,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    myVote: Int?,
    published: String,
    updated: String?,
    deleted: Boolean,
    onPersonClick: (personId: Int) -> Unit,
    isPostCreator: Boolean,
    isModerator: Boolean,
    isCommunityBanned: Boolean,
    onLongClick: () -> Unit = {}
) {
    FlowRow(
        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
        crossAxisAlignment = FlowCrossAxisAlignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = LARGE_PADDING,
                bottom = MEDIUM_PADDING
            )
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = {}
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (deleted) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "TODO",
                    tint = MaterialTheme.colorScheme.error
                )
                DotSpacer(style = MaterialTheme.typography.bodyMedium)
            }

            PersonProfileLink(
                person = creator,
                onClick = { onPersonClick(creator.id) },
                showTags = true,
                isPostCreator = isPostCreator,
                isModerator = isModerator,
                style = style,
                isCommunityBanned = isCommunityBanned
            )
        }
        ScoreAndTime(score = score, myVote = myVote, published = published, updated = updated)
    }
}

@Preview
@Composable
fun CommentOrPostNodeHeaderPreview() {
    CommentOrPostNodeHeader(
        creator = samplePersonSafe,
        score = 23,
        myVote = 1,
        published = samplePost.published,
        updated = samplePost.updated,
        deleted = false,
        onPersonClick = {},
        isPostCreator = true,
        isModerator = true,
        isCommunityBanned = false
    )
}

@Composable
fun ActionBarButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String? = null,
    contentColor: Color = MaterialTheme.colorScheme.onBackground.muted,
    noClick: Boolean = false,
    account: Account?
) {
    val ctx = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = {
                if (!noClick) {
                    if (account !== null) {
                        onClick()
                    } else {
                        loginFirstToast(ctx)
                    }
                }
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "TODO",
                tint = contentColor,
            )
        }
        text?.also {
            Text(
                modifier = Modifier.offset(y = (-1).dp),
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DotSpacer(
    padding: Dp = SMALL_PADDING,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Text(
        text = "·",
        style = style,
        color = MaterialTheme.colorScheme.onBackground.muted,
        modifier = Modifier.padding(horizontal = padding)
    )
}

@Composable
fun scoreColor(myVote: Int?): Color {
    return when (myVote) {
        1 -> MaterialTheme.colorScheme.secondary
        -1 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onBackground.muted
    }
}

@Composable
fun InboxIconAndBadge(
    iconBadgeCount: Int?,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    if (iconBadgeCount !== null && iconBadgeCount > 0) {
        BadgedBox(
            modifier = modifier,
            badge = {
                Badge(
                    content = {
                        Text(
                            text = iconBadgeCount.toString()
                        )
                    }
                )
            },
            content = {
                Icon(
                    imageVector = icon,
                    contentDescription = "TODO",
                    tint = tint
                )
            }
        )
    } else {
        Icon(
            imageVector = icon,
            contentDescription = "TODO",
            tint = tint,
            modifier = modifier
        )
    }
}

@Composable
fun Sidebar(
    title: String?,
    banner: String?,
    icon: String?,
    content: String?,
    published: String,
    postCount: Int,
    commentCount: Int,
    usersActiveDay: Int,
    usersActiveWeek: Int,
    usersActiveMonth: Int,
    usersActiveHalfYear: Int,
    padding: PaddingValues
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .padding(padding)
            .simpleVerticalScrollbar(listState),
        verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING)
    ) {
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomStart
            ) {
                banner?.also {
                    PictrsBannerImage(
                        url = it,
                        modifier = Modifier.height(PROFILE_BANNER_SIZE)
                    )
                }
                Box(modifier = Modifier.padding(MEDIUM_PADDING)) {
                    icon?.also {
                        LargerCircularIcon(icon = it)
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier.padding(MEDIUM_PADDING),
                verticalArrangement = Arrangement.spacedBy(MEDIUM_PADDING)
            ) {
                title?.also {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                TimeAgo(
                    precedingString = "Created",
                    includeAgo = true,
                    published = published
                )
                CommentsAndPosts(
                    usersActiveDay = usersActiveDay,
                    usersActiveWeek = usersActiveWeek,
                    usersActiveMonth = usersActiveMonth,
                    usersActiveHalfYear = usersActiveHalfYear,
                    postCount = postCount,
                    commentCount = commentCount
                )
            }
        }
        item {
            Divider()
        }
        item {
            content?.also {
                Column(
                    modifier = Modifier.padding(MEDIUM_PADDING)
                ) {
                    MyMarkdownText(
                        markdown = it,
                        color = MaterialTheme.colorScheme.onBackground.muted
                    )
                }
            }
        }
    }
}

@Composable
fun CommentsAndPosts(
    usersActiveDay: Int,
    usersActiveWeek: Int,
    usersActiveMonth: Int,
    usersActiveHalfYear: Int,
    postCount: Int,
    commentCount: Int
) {
    FlowRow {
        Text(
            text = "${siFormat(usersActiveDay)} users / day",
            color = MaterialTheme.colorScheme.onBackground.muted
        )
        DotSpacer(style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${siFormat(usersActiveWeek)} users / week",
            color = MaterialTheme.colorScheme.onBackground.muted
        )
        DotSpacer(style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${siFormat(usersActiveMonth)} users / month",
            color = MaterialTheme.colorScheme.onBackground.muted
        )
        DotSpacer(style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${siFormat(usersActiveHalfYear)} users / 6 months",
            color = MaterialTheme.colorScheme.onBackground.muted
        )
        DotSpacer(style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${siFormat(postCount)} posts",
            color = MaterialTheme.colorScheme.onBackground.muted
        )
        DotSpacer(style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${siFormat(commentCount)} comments",
            color = MaterialTheme.colorScheme.onBackground.muted
        )
    }
}

@SuppressLint("ComposableModifierFactory")
@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 4.dp
): Modifier {
    val targetAlpha = if (state.isScrollInProgress) 0.5f else 0f
    val duration = if (state.isScrollInProgress) 150 else 500
    val color = MaterialTheme.colorScheme.onBackground

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = duration)
    )

    return drawWithContent {
        drawContent()

        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
        if (needDrawScrollbar && firstVisibleElementIndex != null) {
            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
            val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

            drawRect(
                color = color,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }
}
