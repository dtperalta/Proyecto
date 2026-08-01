package com.example.amls.ui.screens.learning

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.TypedValue
import android.view.OrientationEventListener
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.sample
import com.example.amls.ml.DecisionAdaptacion
import com.example.amls.ui.AmlsViewModel
import com.example.amls.ui.LearningViewModel
import com.example.amls.ui.PerfilViewModel
import com.example.amls.ui.navigation.DestinoAmls

@androidx.media3.common.util.UnstableApi
@kotlin.OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, kotlinx.coroutines.FlowPreview::class)
@Composable
fun PlaybackScreen(
    navController: NavController,
    recursoId: String,
    perfilViewModel: PerfilViewModel = hiltViewModel(),
    learningViewModel: LearningViewModel = hiltViewModel()
) {
    val perfil by perfilViewModel.perfilReal.collectAsState()
    val recursos by learningViewModel.recursos.collectAsState()
    val leccionActual = recursos.find { it.id == recursoId }
    val context = LocalContext.current

    val configuracion = LocalConfiguration.current
    val esHorizontal = configuracion.orientation == Configuration.ORIENTATION_LANDSCAPE

    val subtitulosUrl = leccionActual?.subtitulosUrl
    var yaRegistroReproduccion by remember { mutableStateOf(false) }
    var mostrarTranscripcion by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val amlsViewModel: AmlsViewModel = hiltViewModel()
    var decisionAdaptacion by remember { mutableStateOf<DecisionAdaptacion?>(null) }
    var pantallaCompleta by remember { mutableStateOf(false) }
    var categoriaFisicaAnterior by remember { mutableStateOf<String?>(null) }

    DisposableEffect(context) {
        val listener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                val categoriaActual = when {
                    orientation in 0..30 || orientation in 330..360 -> "vertical"
                    orientation in 60..120 || orientation in 240..300 -> "horizontal"
                    else -> return // zona ambigua (diagonal) — se ignora
                }

                // Primera lectura: solo establece el punto de partida,
                // sin disparar ningún cambio todavía.
                if (categoriaFisicaAnterior == null) {
                    categoriaFisicaAnterior = categoriaActual
                    return
                }

                // Si no cambió respecto a la lectura anterior, no hacer NADA
                // — esto es lo que evita que un clic manual se deshaga solo,
                // ya que el celular no se mueve físicamente al tocar un botón.
                if (categoriaActual == categoriaFisicaAnterior) return
                categoriaFisicaAnterior = categoriaActual

                if (categoriaActual == "vertical" && pantallaCompleta) {
                    pantallaCompleta = false
                    (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else if (categoriaActual == "horizontal" && !pantallaCompleta) {
                    pantallaCompleta = true
                    (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
        }
        if (listener.canDetectOrientation()) listener.enable()
        onDispose { listener.disable() }
    }

    fun alternarPantallaCompleta() {
        val activity = context as? Activity ?: return
        if (pantallaCompleta) {
            pantallaCompleta = false
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            pantallaCompleta = true
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    DisposableEffect(Unit) {
        amlsViewModel.startSensors()
        onDispose { amlsViewModel.stopSensors() }
    }

    DisposableEffect(Unit) {
        onDispose {
            (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    LaunchedEffect(Unit) {
        var contadorMovimientoAlto = 0

        combine(
            amlsViewModel.nivelLuzNormalizado,
            amlsViewModel.nivelMovimientoNormalizado,
            perfilViewModel.perfilReal
        ) { luz, movimiento, p -> Triple(luz, movimiento, p) }
            .sample(1000)
            .collect { (luz, movimiento, p) ->
                if (p == null) return@collect
                val tamanoNormalizado = (p.tamanoSubtitulos - 12f) / (36f - 12f)

                val prediccionCruda = learningViewModel.predecirAdaptacion(
                    nivelLuz = luz,
                    nivelMovimiento = movimiento,
                    altoContrasteBase = p.requiereAltoContraste,
                    tamanoFuenteBase = tamanoNormalizado
                )

                contadorMovimientoAlto = if (prediccionCruda.agrandarFuente) {
                    contadorMovimientoAlto + 1
                } else {
                    0
                }

                // Solo se confirma "movimiento alto" tras 3 lecturas seguidas
                // (≈3 segundos sostenidos), evitando que un golpe breve dispare
                // el cambio.
                val movimientoConfirmado = contadorMovimientoAlto >= 3

                decisionAdaptacion = DecisionAdaptacion(
                    activarAltoContraste = prediccionCruda.activarAltoContraste,
                    agrandarFuente = movimientoConfirmado,
                    ofrecerTranscripcion = movimientoConfirmado
                )
            }
    }

    LaunchedEffect(decisionAdaptacion?.ofrecerTranscripcion) {
        if (decisionAdaptacion?.ofrecerTranscripcion == true && !leccionActual?.transcripcion.isNullOrBlank()) {
            mostrarTranscripcion = true
        }
    }

    // Oculta/restaura la barra de estado y navegación según la orientación
    DisposableEffect(pantallaCompleta) {
        val activity = context as? Activity
        val ventana = activity?.window
        if (ventana != null) {
            WindowCompat.setDecorFitsSystemWindows(ventana, !pantallaCompleta)
            val controlador = WindowInsetsControllerCompat(ventana, ventana.decorView)
            if (pantallaCompleta) {
                controlador.hide(WindowInsetsCompat.Type.systemBars())
                controlador.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controlador.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            // Al salir de la pantalla, siempre restaura las barras del sistema
            val vent = (context as? Activity)?.window
            if (vent != null) {
                WindowCompat.setDecorFitsSystemWindows(vent, true)
                WindowInsetsControllerCompat(vent, vent.decorView).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    LaunchedEffect(leccionActual?.id) {
        leccionActual?.let {
            learningViewModel.registrarEvento(it.id, "leccion_iniciada")
        }
    }

    val exoPlayer = remember {
        val cacheDataSourceFactory = androidx.media3.datasource.cache.CacheDataSource.Factory()
            .setCache(learningViewModel.videoCacheManager.cache)
            .setUpstreamDataSourceFactory(androidx.media3.datasource.DefaultHttpDataSource.Factory())

        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    val exoPlayerSenas = remember {
        val cacheDataSourceFactory = androidx.media3.datasource.cache.CacheDataSource.Factory()
            .setCache(learningViewModel.videoCacheManager.cache)
            .setUpstreamDataSourceFactory(androidx.media3.datasource.DefaultHttpDataSource.Factory())

        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
                volume = 0f
            }
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) {
                exoPlayer.pause()
                exoPlayerSenas.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayerSenas.release() }
    }

    BackHandler {
        if (pantallaCompleta) {
            pantallaCompleta = false
            (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            exoPlayer.stop()
            exoPlayerSenas.stop()
            navController.popBackStack()
        }
    }

    LaunchedEffect(leccionActual?.urlLenguaSenas) {
        val urlReal = leccionActual?.urlLenguaSenas
        if (urlReal.isNullOrBlank()) return@LaunchedEffect

        exoPlayerSenas.setMediaItem(MediaItem.fromUri(urlReal))
        exoPlayerSenas.prepare()
        exoPlayerSenas.playWhenReady = true
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying && !yaRegistroReproduccion) {
                    yaRegistroReproduccion = true
                    leccionActual?.let {
                        learningViewModel.registrarEvento(it.id, "leccion_reproducida")
                    }
                }
                // Sincroniza el video de señas con el estado del video principal
                exoPlayerSenas.playWhenReady = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    exoPlayerSenas.pause()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    val preferenciaComunicativa = perfil?.preferenciaComunicativa ?: "Subtítulos"
    val mostrarRecuadroSenas = (preferenciaComunicativa == "Lengua de Señas" || preferenciaComunicativa == "Mixto") &&
            !leccionActual?.urlLenguaSenas.isNullOrBlank()
    val mostrarSubtitulosVisual = preferenciaComunicativa == "Subtítulos" || preferenciaComunicativa == "Mixto"

    // Carga el video (y los subtítulos, si ya se resolvió la URL) en el reproductor
    LaunchedEffect(leccionActual?.url_descarga, subtitulosUrl, mostrarSubtitulosVisual) {
        val videoUrl = leccionActual?.url_descarga
        if (videoUrl.isNullOrBlank()) return@LaunchedEffect

        val mediaItemBuilder = MediaItem.Builder().setUri(videoUrl)

        if (mostrarSubtitulosVisual) {
            subtitulosUrl?.let { url ->
                val configSubtitulos = MediaItem.SubtitleConfiguration
                    .Builder(android.net.Uri.parse(url))
                    .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                    .setLanguage("es")
                    .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                    .build()
                mediaItemBuilder.setSubtitleConfigurations(listOf(configSubtitulos))
            }
        }

        exoPlayer.setMediaItem(mediaItemBuilder.build())
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    val altoContraste = decisionAdaptacion?.activarAltoContraste ?: (perfil?.requiereAltoContraste ?: false)
    val tamanoFuente = if (decisionAdaptacion?.agrandarFuente == true) {
        (perfil?.tamanoSubtitulos ?: 18) + 6
    } else {
        perfil?.tamanoSubtitulos ?: 18
    }

    Scaffold(
        topBar = {
            if (!pantallaCompleta) {
                TopAppBar(
                    title = {
                        Text(leccionActual?.titulo ?: "Cargando lección...", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            exoPlayer.stop()
                            exoPlayerSenas.stop()
                            navController.popBackStack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(DestinoAmls.ConfiguracionAccesibilidad.ruta) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración de Accesibilidad")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF005179),
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val modifier = if (pantallaCompleta) Modifier.fillMaxSize() else Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)

        Box(
            modifier = modifier,
            contentAlignment = if (pantallaCompleta) Alignment.Center else Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .then(if (pantallaCompleta) Modifier.fillMaxSize() else Modifier.fillMaxWidth().aspectRatio(16f / 9f))
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (!pantallaCompleta) Modifier.clip(RoundedCornerShape(12.dp)) else Modifier),
                        factory = { ctx ->
                            (android.view.LayoutInflater.from(ctx)
                                .inflate(com.example.amls.R.layout.reproductor_recortable, null) as PlayerView)
                                .apply {
                                    player = exoPlayer
                                    useController = true
                                    aplicarEstiloSubtitulos(this, altoContraste, tamanoFuente)
                                    setShowSubtitleButton(false)
                                    findViewById<android.view.View>(androidx.media3.ui.R.id.exo_settings)?.visibility = android.view.View.GONE
                                    setFullscreenButtonClickListener { alternarPantallaCompleta() }
                                }
                        },
                        update = { view ->
                            aplicarEstiloSubtitulos(view, altoContraste, tamanoFuente)
                        }
                    )

                    if (mostrarRecuadroSenas) {
                        AndroidView(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(if (pantallaCompleta) 16.dp else 8.dp)
                                .size(
                                    width = if (pantallaCompleta) 120.dp else 90.dp,
                                    height = if (pantallaCompleta) 160.dp else 120.dp
                                )
                                .clip(RoundedCornerShape(8.dp)),
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayerSenas
                                    useController = false
                                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                }
                            }
                        )
                    }
                }

                if (!pantallaCompleta) {
                    Spacer(modifier = Modifier.height(24.dp))

                    leccionActual?.let {
                        Text(
                            "Nivel de Dificultad: ${it.nivel_dificultad}",
                            color = Color(0xFF005179),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!leccionActual?.transcripcion.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = { mostrarTranscripcion = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver transcripción completa")
                        }
                    }
                }
            }
        }
    }

    if (mostrarTranscripcion) {
        ModalBottomSheet(
            onDismissRequest = { mostrarTranscripcion = false },
            sheetState = sheetState
        ) {
            val fondoTranscripcion = if (altoContraste) Color.Black else MaterialTheme.colorScheme.surface
            val textoTranscripcion = if (altoContraste) Color.Yellow else MaterialTheme.colorScheme.onSurface

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(fondoTranscripcion)
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    "Transcripción completa",
                    fontWeight = FontWeight.Bold,
                    fontSize = (tamanoFuente + 2).sp,
                    color = textoTranscripcion
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    leccionActual?.transcripcion ?: "",
                    fontSize = tamanoFuente.sp,
                    color = textoTranscripcion
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Aplica el perfil de accesibilidad (RF-2) directamente a los subtítulos
 * reales del video, usando el mismo criterio que ya usábamos en el mockup:
 * alto contraste = fondo amarillo/texto negro; tamaño de fuente configurable.
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private fun aplicarEstiloSubtitulos(playerView: PlayerView, altoContraste: Boolean, tamano: Int) {
    val colorTexto = if (altoContraste) Color.Black.toArgb() else Color.White.toArgb()
    val colorFondo = if (altoContraste) Color.Yellow.toArgb() else android.graphics.Color.argb(160, 0, 0, 0)
    val tipografia = if (altoContraste) {
        android.graphics.Typeface.DEFAULT_BOLD
    } else {
        android.graphics.Typeface.DEFAULT
    }

    playerView.subtitleView?.setStyle(
        CaptionStyleCompat(
            colorTexto,
            colorFondo,
            android.graphics.Color.TRANSPARENT,
            CaptionStyleCompat.EDGE_TYPE_NONE,
            android.graphics.Color.TRANSPARENT,
            tipografia
        )
    )
    playerView.subtitleView?.setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, tamano.toFloat())
}
