package com.example.instagram.comments

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.instagram.IgViewModel
import com.example.instagram.core_ui_components.BlackTransparentTextContainer
import com.example.instagram.core_ui_components.CommonProgressSpinner
import com.example.instagram.models.CommentData

@Composable
fun CommentsRoute(
    navController: NavHostController,
    viewModel: IgViewModel,
    postId: String,
    modifier: Modifier = Modifier,
) {
    CommentsScreen(
        viewModel = viewModel,
        postId = postId,
        modifier = modifier
    )
}

@Composable
fun CommentsScreen(
    viewModel: IgViewModel,
    postId: String,
    modifier: Modifier = Modifier,
) {
    var commentText by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val comments = viewModel.comments.value
    val commentsProgress = viewModel.commentsProgress.value

    Column(modifier = modifier.fillMaxSize()) {
        when {
            commentsProgress -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CommonProgressSpinner()
                }
            }

            comments.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "No Comment available")
                }
            }

            else -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(items = comments) { comment ->
                        CommentRow(comment = comment)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color.LightGray),
                colors = BlackTransparentTextContainer(
                    focusedLabelColor = Color.Transparent,
                    unfocusedLabelColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                )
            )
            Button(
                onClick = {
                    viewModel.createComment(text = commentText, postId = postId)
                    commentText = ""
                    focusManager.clearFocus()
                },
                modifier.padding(start = 8.dp)
            ) {
                Text(text = "Comment")
            }
        }
    }
}

@Composable
fun CommentRow(comment: CommentData, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = comment.userName.orEmpty(), fontWeight = FontWeight.Bold)
        Text(text = comment.text.orEmpty(), modifier = Modifier.padding(8.dp))
    }
}