package com.example.instagram.core_ui_components.video

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.SHOW_BUFFERING_ALWAYS

@Composable
fun VideoPlayer(
    videoUri: String,
    modifier: Modifier = Modifier,
    playWhenReady: Boolean = true,
) {
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    val context = LocalContext.current

    LifecycleStartEffect(Unit) {
        player = initializePlayer(
            videoUri = videoUri,
            context = context,
            isPlayWhenReady = playWhenReady
        )
        onStopOrDispose {
            player?.release()
            player = null
        }
    }

    player?.addListener(object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            Log.e("VideoPlayer", "Player Error: ", error)
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)
            Log.d("VideoPlayer", "Player playWhenReady: $playWhenReady, reason: $reason")
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            Log.d("VideoPlayer", "Player playbackState: $playbackState")
            // Check for Player.STATE_READY here
        }
    })

    Box( // todo use box with constraint
        modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(Color.Black)
    ) {
        player?.let {
            VideoPlayerSurface(
                exoPlayer = it,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPlayerSurface(
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                useController = false // todo pass In, from profile false single post yes
                setShowBuffering(SHOW_BUFFERING_ALWAYS)
            }
        },
        update = { view ->
            view.player = exoPlayer // Ensure player is always set
        },
        modifier = modifier
    )
}

/*
 TODO can this use in memory cache and not reload video each time
 */
private fun initializePlayer(
    videoUri: String,
    context: Context,
    isPlayWhenReady: Boolean,
): ExoPlayer =
    ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(videoUri))
        playWhenReady = isPlayWhenReady
        prepare()
    }

@Preview
@Composable
private fun VideoPlayerComposable() {
    VideoPlayer("")
}