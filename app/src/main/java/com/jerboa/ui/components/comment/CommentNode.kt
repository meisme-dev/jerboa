package com.jerboa.ui.components.comment

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Textsms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.jerboa.Border
import com.jerboa.CommentNodeData
import com.jerboa.InstantScores
import com.jerboa.VoteType
import com.jerboa.border
import com.jerboa.buildCommentsTree
import com.jerboa.calculateCommentOffset
import com.jerboa.calculateNewInstantScores
import com.jerboa.datatypes.Comment
import com.jerboa.datatypes.CommentView
import com.jerboa.datatypes.CommunityModeratorView
import com.jerboa.datatypes.CommunitySafe
import com.jerboa.datatypes.PersonSafe
import com.jerboa.datatypes.Post
import com.jerboa.datatypes.sampleCommentView
import com.jerboa.datatypes.sampleCommunitySafe
import com.jerboa.datatypes.samplePost
import com.jerboa.datatypes.sampleReplyCommentView
import com.jerboa.datatypes.sampleSecondReplyCommentView
import com.jerboa.db.Account
import com.jerboa.isModerator
import com.jerboa.isPostCreator
import com.jerboa.ui.components.common.ActionBarButton
import com.jerboa.ui.components.common.CommentOrPostNodeHeader
import com.jerboa.ui.components.common.IconAndTextDrawerItem
import com.jerboa.ui.components.common.MyMarkdownText
import com.jerboa.ui.components.common.VoteGeneric
import com.jerboa.ui.components.community.CommunityLink
import com.jerboa.ui.theme.LARGE_PADDING
import com.jerboa.ui.theme.MEDIUM_PADDING
import com.jerboa.ui.theme.SMALL_PADDING
import com.jerboa.ui.theme.XXL_PADDING
import com.jerboa.ui.theme.colorList
import com.jerboa.ui.theme.muted

@Composable
fun CommentNodeHeader(
    commentView: CommentView,
    onPersonClick: (personId: Int) -> Unit,
    score: Int,
    myVote: Int?,
    isModerator: Boolean,
    onLongClick: () -> Unit = {}
) {
    CommentOrPostNodeHeader(
        creator = commentView.creator,
        score = score,
        myVote = myVote,
        published = commentView.comment.published,
        updated = commentView.comment.updated,
        deleted = commentView.comment.deleted,
        onPersonClick = onPersonClick,
        isPostCreator = isPostCreator(commentView),
        isModerator = isModerator,
        isCommunityBanned = commentView.creator_banned_from_community,
        style = MaterialTheme.typography.titleMedium,
        onLongClick = onLongClick
    )
}

@Preview
@Composable
fun CommentNodeHeaderPreview() {
    CommentNodeHeader(
        commentView = sampleCommentView,
        score = 23,
        myVote = 26,
        isModerator = false,
        onPersonClick = {}
    )
}

@Composable
fun CommentBody(
    comment: Comment,
    viewSource: Boolean
) {
    val content = if (comment.removed) {
        "*Removed*"
    } else if (comment.deleted) {
        "*Deleted*"
    } else {
        comment.content
    }

    if (viewSource) {
        SelectionContainer {
            Text(
                text = comment.content
            )
        }
    } else {
        MyMarkdownText(markdown = content, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
fun CommentBodyPreview() {
    CommentBody(comment = sampleCommentView.comment, viewSource = false)
}

fun LazyListScope.commentNodeItem(
    node: CommentNodeData,
    isFlat: Boolean,
    isExpanded: (commentId: Int) -> Boolean,
    toggleExpanded: (commentId: Int) -> Unit,
    moderators: List<CommunityModeratorView>,
    onUpvoteClick: (commentView: CommentView) -> Unit,
    onDownvoteClick: (commentView: CommentView) -> Unit,
    onReplyClick: (commentView: CommentView) -> Unit,
    onSaveClick: (commentView: CommentView) -> Unit,
    onMarkAsReadClick: (commentView: CommentView) -> Unit,
    onEditCommentClick: (commentView: CommentView) -> Unit,
    onDeleteCommentClick: (commentView: CommentView) -> Unit,
    onPersonClick: (personId: Int) -> Unit,
    onCommunityClick: (community: CommunitySafe) -> Unit,
    onPostClick: (postId: Int) -> Unit,
    onReportClick: (commentView: CommentView) -> Unit,
    onCommentLinkClick: (commentView: CommentView) -> Unit,
    onBlockCreatorClick: (creator: PersonSafe) -> Unit,
    onFetchChildrenClick: (commentView: CommentView) -> Unit,
    showPostAndCommunityContext: Boolean = false,
    account: Account?
) {
    val commentView = node.commentView
    val commentId = commentView.comment.id

    val offset = calculateCommentOffset(node.depth, 4) // The ones with a border on
    val offset2 = if (node.depth == 0) {
        MEDIUM_PADDING
    } else {
        XXL_PADDING
    }

    val showMoreChildren = isExpanded(commentId) && node.children.isNullOrEmpty() && node
        .commentView.counts.child_count > 0 && !isFlat
    item(key = commentId) {
        var viewSource by remember { mutableStateOf(false) }

        val backgroundColor = MaterialTheme.colorScheme.background
        val borderColor = calculateBorderColor(backgroundColor, node.depth)
        val border = Border(SMALL_PADDING, borderColor)

        val instantScores = remember {
            mutableStateOf(
                InstantScores(
                    myVote = commentView.my_vote,
                    score = commentView.counts.score,
                    upvotes = commentView.counts.upvotes,
                    downvotes = commentView.counts.downvotes
                )
            )
        }

        Column(
            modifier = Modifier
                .padding(
                    start = offset
                )
        ) {
//            Divider()
            Column(
                modifier = Modifier.border(start = border)
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = offset2,
                        end = MEDIUM_PADDING
                    )
                ) {
                    if (showPostAndCommunityContext) {
                        PostAndCommunityContextHeader(
                            post = commentView.post,
                            community = commentView.community,
                            onCommunityClick = onCommunityClick,
                            onPostClick = onPostClick
                        )
                    }
                    CommentNodeHeader(
                        commentView = commentView,
                        onPersonClick = onPersonClick,
                        score = instantScores.value.score,
                        myVote = instantScores.value.myVote,
                        isModerator = isModerator(commentView.creator, moderators),
                        onLongClick = {
                            toggleExpanded(commentId)
                        }
                    )
                    AnimatedVisibility(
                        visible = isExpanded(commentId),
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            CommentBody(
                                comment = commentView.comment,
                                viewSource = viewSource
                            )
                            CommentFooterLine(
                                commentView = commentView,
                                instantScores = instantScores.value,
                                onUpvoteClick = {
                                    instantScores.value = calculateNewInstantScores(
                                        instantScores.value,
                                        voteType = VoteType.Upvote
                                    )
                                    onUpvoteClick(it)
                                },
                                onDownvoteClick = {
                                    instantScores.value = calculateNewInstantScores(
                                        instantScores.value,
                                        voteType = VoteType.Downvote
                                    )
                                    onDownvoteClick(it)
                                },
                                onViewSourceClick = {
                                    viewSource = !viewSource
                                },
                                onEditCommentClick = onEditCommentClick,
                                onDeleteCommentClick = onDeleteCommentClick,
                                onReplyClick = onReplyClick,
                                onSaveClick = onSaveClick,
                                onReportClick = onReportClick,
                                onCommentLinkClick = onCommentLinkClick,
                                onBlockCreatorClick = onBlockCreatorClick,
                                account = account
                            )
                        }
                    }
                }
            }
        }
    }

    if (showMoreChildren) {
        item(key = "${commentId}_children") {
            ShowMoreChildrenNode(node.depth, commentView, onFetchChildrenClick)
        }
    }

    if (isExpanded(commentId)) {
        node.children?.also { nodes ->
            commentNodeItems(
                nodes = nodes,
                isFlat = isFlat,
                toggleExpanded = toggleExpanded,
                isExpanded = isExpanded,
                onUpvoteClick = onUpvoteClick,
                onDownvoteClick = onDownvoteClick,
                onSaveClick = onSaveClick,
                onMarkAsReadClick = onMarkAsReadClick,
                onEditCommentClick = onEditCommentClick,
                onDeleteCommentClick = onDeleteCommentClick,
                onPersonClick = onPersonClick,
                onCommunityClick = onCommunityClick,
                onPostClick = onPostClick,
                showPostAndCommunityContext = showPostAndCommunityContext,
                onReportClick = onReportClick,
                onCommentLinkClick = onCommentLinkClick,
                onFetchChildrenClick = onFetchChildrenClick,
                onReplyClick = onReplyClick,
                onBlockCreatorClick = onBlockCreatorClick,
                account = account,
                moderators = moderators
            )
        }
    }
}

@Composable
private fun ShowMoreChildrenNode(
    depth: Int,
    commentView: CommentView,
    onFetchChildrenClick: (commentView: CommentView) -> Unit
) {
    val newDepth = depth + 1

    val offset = calculateCommentOffset(newDepth, 4) // The ones with a border on
    val offset2 = if (newDepth == 0) {
        MEDIUM_PADDING
    } else {
        XXL_PADDING
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val borderColor = calculateBorderColor(backgroundColor, newDepth)
    val border = Border(SMALL_PADDING, borderColor)

    Column(
        modifier = Modifier
            .padding(
                start = offset
            )
    ) {
        Divider()
        Column(
            modifier = Modifier.border(start = border)
        ) {
            Column(
                modifier = Modifier.padding(start = offset2, end = MEDIUM_PADDING)
            ) {
                ShowMoreChildren(
                    commentView = commentView,
                    onFetchChildrenClick = onFetchChildrenClick
                )
            }
        }
    }
}

@Composable
fun PostAndCommunityContextHeader(
    post: Post,
    community: CommunitySafe,
    onCommunityClick: (community: CommunitySafe) -> Unit,
    onPostClick: (postId: Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = LARGE_PADDING)
    ) {
        Text(
            text = post.name,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.clickable { onPostClick(post.id) }
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "in ", color = MaterialTheme.colorScheme.onBackground.muted)
            CommunityLink(
                community = community,
                onClick = onCommunityClick
            )
        }
    }
}

@Preview
@Composable
fun PostAndCommunityContextHeaderPreview() {
    PostAndCommunityContextHeader(
        post = samplePost,
        community = sampleCommunitySafe,
        onCommunityClick = {},
        onPostClick = {}
    )
}

@Composable
fun CommentFooterLine(
    commentView: CommentView,
    instantScores: InstantScores,
    onUpvoteClick: (commentView: CommentView) -> Unit,
    onDownvoteClick: (commentView: CommentView) -> Unit,
    onReplyClick: (commentView: CommentView) -> Unit,
    onSaveClick: (commentView: CommentView) -> Unit,
    onViewSourceClick: () -> Unit,
    onEditCommentClick: (commentView: CommentView) -> Unit,
    onDeleteCommentClick: (commentView: CommentView) -> Unit,
    onReportClick: (commentView: CommentView) -> Unit,
    onCommentLinkClick: (commentView: CommentView) -> Unit,
    onBlockCreatorClick: (creator: PersonSafe) -> Unit,
    account: Account?
) {
    var showMoreOptions by remember { mutableStateOf(false) }

    if (showMoreOptions) {
        CommentOptionsDialog(
            commentView = commentView,
            onDismissRequest = { showMoreOptions = false },
            onViewSourceClick = {
                showMoreOptions = false
                onViewSourceClick()
            },
            onEditCommentClick = {
                showMoreOptions = false
                onEditCommentClick(commentView)
            },
            onDeleteCommentClick = {
                showMoreOptions = false
                onDeleteCommentClick(commentView)
            },
            onReportClick = {
                showMoreOptions = false
                onReportClick(commentView)
            },
            onBlockCreatorClick = {
                showMoreOptions = false
                onBlockCreatorClick(commentView.creator)
            },
            onCommentLinkClick = {
                showMoreOptions = false
                onCommentLinkClick(commentView)
            },
            isCreator = account?.id == commentView.creator.id
        )
    }

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = LARGE_PADDING, bottom = SMALL_PADDING)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(XXL_PADDING)
        ) {
            VoteGeneric(
                myVote = instantScores.myVote,
                votes = instantScores.upvotes,
                item = commentView,
                type = VoteType.Upvote,
                onVoteClick = onUpvoteClick,
                showNumber = (instantScores.downvotes != 0),
                account = account
            )
            VoteGeneric(
                myVote = instantScores.myVote,
                votes = instantScores.downvotes,
                item = commentView,
                type = VoteType.Downvote,
                onVoteClick = onDownvoteClick,
                account = account
            )
            ActionBarButton(
                icon = if (commentView.saved) { Icons.Filled.Bookmark } else {
                    Icons.Outlined.BookmarkBorder
                },
                onClick = { onSaveClick(commentView) },
                contentColor = if (commentView.saved) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground.muted
                },
                account = account
            )
            // Don't let you respond to your own comment.
            if (commentView.creator.id != account?.id) {
                ActionBarButton(
                    icon = Icons.Outlined.Textsms,
                    onClick = { onReplyClick(commentView) },
                    account = account
                )
            }
            ActionBarButton(
                icon = Icons.Outlined.MoreVert,
                account = account,
                onClick = { showMoreOptions = !showMoreOptions }
            )
        }
    }
}

@Preview
@Composable
fun CommentNodesPreview() {
    val comments = listOf(
        sampleSecondReplyCommentView,
        sampleCommentView,
        sampleReplyCommentView
    )
    val tree = buildCommentsTree(comments, false)
    CommentNodes(
        nodes = tree,
        isFlat = false,
        onUpvoteClick = {},
        onDownvoteClick = {},
        onReplyClick = {},
        onFetchChildrenClick = {},
        onSaveClick = {},
        onMarkAsReadClick = {},
        onEditCommentClick = {},
        onDeleteCommentClick = {},
        onReportClick = {},
        onCommentLinkClick = {},
        onPersonClick = {},
        onCommunityClick = {},
        onBlockCreatorClick = {},
        onPostClick = {},
        moderators = listOf(),
        listState = rememberLazyListState()
    )
}

@Composable
fun CommentOptionsDialog(
    onDismissRequest: () -> Unit,
    onViewSourceClick: () -> Unit,
    onEditCommentClick: () -> Unit,
    onDeleteCommentClick: () -> Unit,
    onReportClick: () -> Unit,
    onBlockCreatorClick: () -> Unit,
    onCommentLinkClick: () -> Unit,
    isCreator: Boolean,
    commentView: CommentView
) {
    val localClipboardManager = LocalClipboardManager.current
    val ctx = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Column {
                IconAndTextDrawerItem(
                    text = "Goto Comment",
                    icon = Icons.Outlined.Link,
                    onClick = onCommentLinkClick
                )
                IconAndTextDrawerItem(
                    text = "View Source",
                    icon = Icons.Outlined.Description,
                    onClick = onViewSourceClick
                )
                IconAndTextDrawerItem(
                    text = "Copy Permalink",
                    icon = Icons.Outlined.ContentCopy,
                    onClick = {
                        val permalink = commentView.comment.ap_id
                        localClipboardManager.setText(AnnotatedString(permalink))
                        Toast.makeText(ctx, "Permalink Copied", Toast.LENGTH_SHORT).show()
                        onDismissRequest()
                    }
                )
                if (!isCreator) {
                    IconAndTextDrawerItem(
                        text = "Report Comment",
                        icon = Icons.Outlined.Flag,
                        onClick = onReportClick
                    )
                    IconAndTextDrawerItem(
                        text = "Block ${commentView.creator.name}",
                        icon = Icons.Outlined.Block,
                        onClick = onBlockCreatorClick
                    )
                }
                if (isCreator) {
                    IconAndTextDrawerItem(
                        text = "Edit",
                        icon = Icons.Outlined.Edit,
                        onClick = onEditCommentClick
                    )
                    val deleted = commentView.comment.deleted
                    if (deleted) {
                        IconAndTextDrawerItem(
                            text = "Restore",
                            icon = Icons.Outlined.Restore,
                            onClick = onDeleteCommentClick
                        )
                    } else {
                        IconAndTextDrawerItem(
                            text = "Delete",
                            icon = Icons.Outlined.Delete,
                            onClick = onDeleteCommentClick
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Preview
@Composable
fun CommentOptionsDialogPreview() {
    CommentOptionsDialog(
        isCreator = true,
        commentView = sampleCommentView,
        onDismissRequest = {},
        onEditCommentClick = {},
        onDeleteCommentClick = {},
        onReportClick = {},
        onViewSourceClick = {},
        onCommentLinkClick = {},
        onBlockCreatorClick = {}
    )
}

@Composable
fun ShowMoreChildren(
    commentView: CommentView,
    onFetchChildrenClick: (commentView: CommentView) -> Unit
) {
    TextButton(
        content = {
            Text("${commentView.counts.child_count} more replies")
        },
        onClick = { onFetchChildrenClick(commentView) }
    )
}

@Composable
@Preview
fun ShowMoreChildrenPreview() {
    ShowMoreChildren(
        commentView = sampleCommentView,
        onFetchChildrenClick = {}
    )
}

fun calculateBorderColor(defaultBackground: Color, depth: Int): Color {
    return if (depth == 0) {
        defaultBackground
    } else {
        colorList[depth.minus(1).mod(colorList.size)]
    }
}

@Composable
fun ShowCommentContextButtons(
    postId: Int,
    commentParentId: Int?,
    showContextButton: Boolean,
    onPostClick: (postId: Int) -> Unit,
    onCommentClick: (commentId: Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
        modifier = Modifier.padding(MEDIUM_PADDING)
    ) {
        OutlinedButton(
            content = {
                Text("View Post")
            },
            onClick = { onPostClick(postId) }
        )
        if (showContextButton && commentParentId != null) {
            OutlinedButton(
                content = {
                    Text("View Context")
                },
                onClick = { onCommentClick(commentParentId) }
            )
        }
    }
}

@Composable
@Preview
fun ShowCommentContextButtonsPreview() {
    ShowCommentContextButtons(
        postId = 0,
        commentParentId = 0,
        showContextButton = true,
        onPostClick = {},
        onCommentClick = {}
    )
}
