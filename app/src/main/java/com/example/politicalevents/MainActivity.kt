package com.example.politicalevents

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.politicalevents.ui.theme.EngagementAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EngagementAppTheme {
                RootNavigation()
            }
        }
    }
}

/* ---------------- ROOT NAV (SINGLE SOURCE OF TRUTH) ---------------- */

@Composable
fun RootNavigation() {
    val rootNavController = rememberNavController()
    val authUser by rememberFirebaseAuthState()

    // 🔥 SINGLE ViewModel for entire app
    val engagementViewModel: EngagementViewModel = viewModel()

    // 🔑 Load Firestore profile WHEN user becomes non-null
    LaunchedEffect(authUser) {
        if (authUser != null) {
            engagementViewModel.loadUserProfile()
        }
    }

    NavHost(
        navController = rootNavController,
        startDestination = if (authUser == null) "auth" else "main"
    ) {

        composable("auth") {
            AuthNav(
                onAuthSuccess = {
                    rootNavController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainAppNav(
                viewModel = engagementViewModel,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    rootNavController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}

/* ---------------- AUTH STATE OBSERVER ---------------- */

@Composable
fun rememberFirebaseAuthState(): State<FirebaseUser?> {
    val auth = FirebaseAuth.getInstance()
    return produceState<FirebaseUser?>(initialValue = auth.currentUser) {
        val listener = FirebaseAuth.AuthStateListener {
            value = it.currentUser
        }
        auth.addAuthStateListener(listener)
        awaitDispose { auth.removeAuthStateListener(listener) }
    }
}

/* ---------------- MAIN APP NAV (BOTTOM NAV) ---------------- */

@Composable
fun MainAppNav(
    viewModel: EngagementViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(NavigationItem.Home.route) }

    navController.addOnDestinationChangedListener { _, dest, _ ->
        currentRoute = dest.route ?: NavigationItem.Home.route
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = NavigationItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable(NavigationItem.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToQuiz = { navController.navigate(NavigationItem.Quiz.route) },
                    onNavigateToPolls = { navController.navigate(NavigationItem.Polls.route) },
                    onNavigateToCommunity = { navController.navigate(NavigationItem.Community.route) },
                    onNavigateToProfile = { navController.navigate(NavigationItem.Profile.route) }
                )
            }

            composable(NavigationItem.Profile.route) {
                ProfileScreen(
                    viewModel,
                    onLogout = onLogout
                )
            }

            composable(NavigationItem.Community.route) {
                CommunityScreen(viewModel)
            }

            composable(NavigationItem.Quiz.route) {
                val quizzes by viewModel.quizzes.collectAsStateWithLifecycle()

                QuizScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )

            }

            composable(NavigationItem.Polls.route) {
                PollsScreen(viewModel)
            }
        }
    }
}

/* ---------------- AUTH FLOW ---------------- */

@Composable
fun AuthNav(
    onAuthSuccess: () -> Unit
) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val context = LocalContext.current
    val activity = context as Activity

    val googleAuthManager = remember { GoogleAuthManager(activity) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleAuthManager.handleResult(
            data = result.data,
            onSuccess = {
                val uid = FirebaseAuth.getInstance().currentUser!!.uid

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .set(
                        mapOf(
                            "username" to "User",
                            "email" to FirebaseAuth.getInstance().currentUser?.email,
                            "totalPoints" to 0,
                            "level" to 1,
                            "streak" to 0
                        )
                    )

                onAuthSuccess()
            },
            onError = { it.printStackTrace() }
        )

    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onLoginClick = { email, password ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener { onAuthSuccess() }
                        .addOnFailureListener { it.printStackTrace() }
                },
                onGoogleLoginClick = {
                    launcher.launch(googleAuthManager.getSignInIntent())
                },
                onPhoneLoginClick = {
                    // phone auth next step
                },
                onNavigateToSignup = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupClick = { name, email, password ->
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            val uid = auth.currentUser!!.uid
                            db.collection("users").document(uid).set(
                                mapOf(
                                    "username" to name,
                                    "email" to email,
                                    "totalPoints" to 0,
                                    "level" to 1,
                                    "streak" to 0
                                )
                            )
                            onAuthSuccess()
                        }
                        .addOnFailureListener { it.printStackTrace() }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}
