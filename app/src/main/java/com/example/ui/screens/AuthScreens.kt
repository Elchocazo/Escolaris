package com.example.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.auth.AuthService
import com.example.data.local.entity.UserEntity
import com.example.domain.model.UserRole
import com.example.ui.components.AuthBackgroundOffWhite
import com.example.ui.components.AuthBottomWaveBackground
import com.example.ui.components.AuthTextDark
import com.example.ui.components.AuthTextMuted
import com.example.ui.components.AuthThemePalette
import com.example.ui.components.AuthTopBubblesBackground
import com.example.ui.components.BlueAuthPalette
import com.example.ui.components.GetStartedBottomWaveBackground
import com.example.ui.components.MagentaAuthPalette
import com.example.utils.GoogleSignInHelper
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class AuthScreenState {
    object GetStarted : AuthScreenState()
    object SignUp : AuthScreenState()
    object SignIn : AuthScreenState()
}

@Composable
fun AuthFlowScreen(
    authService: AuthService,
    onAuthSuccess: (UserEntity) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf<AuthScreenState>(AuthScreenState.SignIn) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showGoogleOnboardingDialog by remember { mutableStateOf(false) }
    var pendingGoogleEmail by remember { mutableStateOf("") }
    var pendingGoogleName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Default palette: Electric Royal Blue
    val currentPalette = BlueAuthPalette

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Launcher del Selector Nativo de Cuentas de Google del Dispositivo
    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!selectedEmail.isNullOrBlank()) {
                val cleanEmail = selectedEmail.trim().lowercase()
                val isSuperAdminTeacher = cleanEmail == "moz658@gmail.com"

                scope.launch {
                    isLoading = true
                    val existing = authService.findExistingUserByEmail(cleanEmail)
                    if (existing != null) {
                        isLoading = false
                        onShowMessage("¡Bienvenido de nuevo, ${existing.name}!")
                        onAuthSuccess(existing)
                    } else if (isSuperAdminTeacher) {
                        val authResult = authService.signInWithGoogleAccount(
                            email = cleanEmail,
                            displayName = "Manuel Alejandro Muñoz",
                            role = UserRole.TEACHER.code
                        )
                        isLoading = false
                        authResult.onSuccess { user ->
                            onShowMessage("¡Bienvenido, ${user.name}!")
                            onAuthSuccess(user)
                        }.onFailure { err ->
                            onShowMessage(err.localizedMessage ?: "Error al autenticar")
                        }
                    } else {
                        isLoading = false
                        val defaultName = selectedEmail.substringBefore("@").replace(".", " ")
                            .split(" ").filter { it.isNotBlank() }
                            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                        pendingGoogleEmail = cleanEmail
                        pendingGoogleName = defaultName
                        showGoogleOnboardingDialog = true
                    }
                }
            }
        }
    }

    val handleGoogleSignInFlow = {
        scope.launch {
            isLoading = true
            try {
                val idToken = GoogleSignInHelper.getGoogleIdToken(context)
                val result = authService.signInWithGoogleIdToken(idToken, "MALE")
                isLoading = false
                result.onSuccess { user ->
                    onShowMessage("¡Bienvenido, ${user.name}!")
                    onAuthSuccess(user)
                }.onFailure {
                    // Fallback al selector nativo de cuentas de Google del celular
                    try {
                        val intent = AccountManager.newChooseAccountIntent(
                            null,
                            null,
                            arrayOf("com.google"),
                            null,
                            null,
                            null,
                            null
                        )
                        accountPickerLauncher.launch(intent)
                    } catch (e: Exception) {
                        onShowMessage("No se pudo iniciar el selector de cuentas del dispositivo.")
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                // Abrir directamente el selector nativo de Google del celular
                try {
                    val intent = AccountManager.newChooseAccountIntent(
                        null,
                        null,
                        arrayOf("com.google"),
                        null,
                        null,
                        null,
                        null
                    )
                    accountPickerLauncher.launch(intent)
                } catch (ex: Exception) {
                    onShowMessage("Selecciona una cuenta de Google en tu dispositivo.")
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuthBackgroundOffWhite)
    ) {
        Crossfade(targetState = currentStep, label = "auth_step_transition") { step ->
            when (step) {
                is AuthScreenState.GetStarted -> {
                    GetStartedView(
                        palette = currentPalette,
                        onNavigateSignUp = { currentStep = AuthScreenState.SignUp },
                        onNavigateSignIn = { currentStep = AuthScreenState.SignIn },
                        onGoogleSignIn = { handleGoogleSignInFlow() }
                    )
                }

                is AuthScreenState.SignUp -> {
                    SignUpView(
                        palette = currentPalette,
                        isLoading = isLoading,
                        onNavigateSignIn = { currentStep = AuthScreenState.SignIn },
                        onShowPrivacyPolicy = { showPrivacyPolicyDialog = true },
                        onSignUpSubmit = { name, lastName, email, pass, role, age, grade, codeToLink ->
                            scope.launch {
                                isLoading = true
                                val result = authService.signUpWithEmail(
                                    name = name,
                                    lastName = lastName,
                                    email = email,
                                    password = pass,
                                    role = role,
                                    age = age,
                                    gradeSectionInput = grade,
                                    studentCodeToLink = codeToLink
                                )
                                isLoading = false
                                result.onSuccess { user ->
                                    val extraMsg = if (user.studentCode.isNotBlank()) " | Tu Código: ${user.studentCode}" else ""
                                    onShowMessage("¡Cuenta creada exitosamente! Bienvenido, ${user.name}$extraMsg")
                                    onAuthSuccess(user)
                                }.onFailure { error ->
                                    onShowMessage(error.localizedMessage ?: "Error al registrar cuenta")
                                }
                            }
                        },
                        onGoogleSignUp = { handleGoogleSignInFlow() }
                    )
                }

                is AuthScreenState.SignIn -> {
                    SignInView(
                        palette = currentPalette,
                        isLoading = isLoading,
                        onNavigateSignUp = { currentStep = AuthScreenState.SignUp },
                        onForgotPassword = { showForgotPasswordDialog = true },
                        onSignInSubmit = { email, pass ->
                            scope.launch {
                                isLoading = true
                                val result = authService.signInWithEmail(email, pass)
                                isLoading = false
                                result.onSuccess { user ->
                                    onShowMessage("¡Bienvenido de nuevo, ${user.name}!")
                                    onAuthSuccess(user)
                                }.onFailure { error ->
                                    onShowMessage(error.localizedMessage ?: "Credenciales incorrectas")
                                }
                            }
                        },
                        onGoogleSignIn = { handleGoogleSignInFlow() }
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = currentPalette.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Conectando con Escolaris...",
                            fontWeight = FontWeight.Bold,
                            color = AuthTextDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    if (showGoogleOnboardingDialog) {
        GoogleOnboardingDialog(
            email = pendingGoogleEmail,
            initialName = pendingGoogleName,
            palette = currentPalette,
            onDismiss = { showGoogleOnboardingDialog = false },
            onComplete = { name, role, gradeOrCargo, codeToLink, age ->
                showGoogleOnboardingDialog = false
                scope.launch {
                    isLoading = true
                    val authResult = authService.signInWithGoogleAccount(
                        email = pendingGoogleEmail,
                        displayName = name,
                        role = role,
                        gradeSectionInput = gradeOrCargo,
                        studentCodeToLink = codeToLink,
                        age = age
                    )
                    isLoading = false
                    authResult.onSuccess { user ->
                        val codeMsg = if (user.studentCode.isNotBlank()) " | Tu Código: ${user.studentCode}" else ""
                        onShowMessage("¡Bienvenido a Escolaris, ${user.name}!$codeMsg")
                        onAuthSuccess(user)
                    }.onFailure { err ->
                        onShowMessage(err.localizedMessage ?: "Error al completar registro")
                    }
                }
            }
        )
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            palette = currentPalette,
            onDismiss = { showForgotPasswordDialog = false },
            onSendReset = { email ->
                scope.launch {
                    val result = authService.sendPasswordResetEmail(email)
                    result.onSuccess {
                        onShowMessage("Enlace de restablecimiento enviado a $email")
                    }.onFailure {
                        onShowMessage("Error al enviar correo: ${it.localizedMessage}")
                    }
                }
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        PrivacyPolicyDialog(
            palette = currentPalette,
            onDismiss = { showPrivacyPolicyDialog = false }
        )
    }
}

// ==========================================
// LOGO OFICIAL ESCOLARIS (Orgánico y Juvenil)
// ==========================================
@Composable
fun EscolarisAppLogo(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 86.dp
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        shadowElevation = 8.dp,
        modifier = modifier.size(size)
    ) {
        Image(
            painter = painterResource(id = R.drawable.escolaris_logo),
            contentDescription = "Escolaris Logo Oficial",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun EscolarisCoverLogo(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 150.dp
) {
    Surface(
        shape = RoundedCornerShape(42.dp),
        color = Color.White.copy(alpha = 0.20f),
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.45f)),
        shadowElevation = 18.dp,
        modifier = modifier.size(size)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.escolaris_logo),
                contentDescription = "Escolaris Logo Portada",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(34.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

// ==========================================
// 1. GET STARTED VIEW (Nueva Portada Escolaris)
// ==========================================
@Composable
fun GetStartedView(
    palette: AuthThemePalette = BlueAuthPalette,
    onNavigateSignUp: () -> Unit,
    onNavigateSignIn: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF172554), // blue-900
                        Color(0xFF2563EB), // blue-600
                        Color(0xFF38BDF8)  // cyan-400
                    )
                )
            )
    ) {
        // Contenido Principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera Centrada con Logo Flotante y Subtítulo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EscolarisCoverLogo(size = 152.dp)

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Escolaris",
                    fontSize = 46.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Tu futuro académico comienza aquí 🚀",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.95f),
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Área Inferior de Acciones (Botones Pill Modernos)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Botón CREAR CUENTA (Verde Menta Neón con Brillo)
                Button(
                    onClick = onNavigateSignUp,
                    shape = RoundedCornerShape(9999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4EDEA3),
                        contentColor = Color(0xFF002113)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(9999.dp), spotColor = Color(0xFF4EDEA3))
                        .testTag("get_started_signup_button")
                ) {
                    Text(
                        text = "CREAR CUENTA",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF002113)
                    )
                }

                // Botón INICIAR SESIÓN (Efecto Vidrio Esmerilado)
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.45f)),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .clickable { onNavigateSignIn() }
                        .testTag("get_started_signin_button")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "INICIAR SESIÓN",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Botón Continuar con Google (Dark Glass Minimalista)
                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = Color.Black.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .clickable { onGoogleSignIn() }
                        .testTag("get_started_google_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🌐", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Continuar con Google",
                            color = Color.White.copy(alpha = 0.95f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. SIGN UP VIEW (Registro Completo por Rol)
// ==========================================
@Composable
fun SignUpView(
    palette: AuthThemePalette = BlueAuthPalette,
    isLoading: Boolean,
    onNavigateSignIn: () -> Unit,
    onShowPrivacyPolicy: () -> Unit,
    onSignUpSubmit: (name: String, lastName: String, email: String, pass: String, role: String, age: Int, grade: String, codeToLink: String) -> Unit,
    onGoogleSignUp: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT.code) } // "STUDENT", "PARENT", "TEACHER"
    
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var gradeSection by remember { mutableStateOf("10° Grado") }
    var studentCodeToLink by remember { mutableStateOf("") }
    
    // Datos específicos de Docente
    var teacherSubject by remember { mutableStateOf("") }
    var teacherGrades by remember { mutableStateOf("") }
    var teacherInstitution by remember { mutableStateOf("Colegio Escolaris") }
    var teacherCode by remember { mutableStateOf("") }
    
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreePrivacy by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        AuthTopBubblesBackground(palette = palette)
        AuthBottomWaveBackground(heightRatio = 0.28f, palette = palette)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabecera
            Column(modifier = Modifier.padding(top = 65.dp)) {
                Text(
                    text = "Crear Cuenta",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AuthTextDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selecciona tu perfil y completa tus datos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AuthTextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selector de Rol (Estudiante, Padre/Tutor, Docente)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "¿Cuál es tu rol en la institución?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuthTextDark
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val roles = listOf(
                        Triple(UserRole.STUDENT.code, "🎓", "Estudiante"),
                        Triple(UserRole.PARENT.code, "👨‍👩‍👧", "Padre/Tutor"),
                        Triple(UserRole.TEACHER.code, "👨‍🏫", "Docente")
                    )

                    roles.forEach { (roleCode, emoji, label) ->
                        val isSelected = selectedRole == roleCode
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) palette.primary else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) palette.primary else Color(0xFFCBD5E1)),
                            shadowElevation = if (isSelected) 3.dp else 1.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    selectedRole = roleCode
                                    errorMessage = null
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(emoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else AuthTextDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campos del formulario
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Nombres y Apellidos en 2 columnas o filas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuthPillTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        placeholder = "Nombres",
                        cursorColor = palette.primary,
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AuthTextMuted, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                        testTag = "signup_name_input"
                    )

                    AuthPillTextField(
                        value = lastName,
                        onValueChange = { lastName = it; errorMessage = null },
                        placeholder = "Apellidos",
                        cursorColor = palette.primary,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        testTag = "signup_lastname_input"
                    )
                }

                // Campos según rol
                when (selectedRole) {
                    UserRole.STUDENT.code -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AuthPillTextField(
                                value = ageText,
                                onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) ageText = it },
                                placeholder = "Edad (ej. 15)",
                                cursorColor = palette.primary,
                                modifier = Modifier.weight(0.45f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) })
                            )

                            AuthPillTextField(
                                value = gradeSection,
                                onValueChange = { gradeSection = it },
                                placeholder = "Grado (ej. 10° A)",
                                cursorColor = palette.primary,
                                modifier = Modifier.weight(0.55f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                        }

                        // Info sobre código
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.primary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Se te generará un Código Único (ESC-XXXXXX) para que tus padres puedan vincularse a tu cuenta.",
                                    fontSize = 11.sp,
                                    color = palette.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    UserRole.PARENT.code -> {
                        AuthPillTextField(
                            value = studentCodeToLink,
                            onValueChange = { studentCodeToLink = it.uppercase(); errorMessage = null },
                            placeholder = "Código del Estudiante (ej. ESC-7K9M2P)",
                            cursorColor = palette.primary,
                            leadingIcon = {
                                Text("🔗", fontSize = 16.sp)
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            testTag = "signup_student_code_link_input"
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF059669).copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👨‍👩‍👧", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pide a tu hijo/a el Código Único que aparece en su perfil de Escolaris para vincular su progreso.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF065F46),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    UserRole.TEACHER.code -> {
                        AuthPillTextField(
                            value = teacherSubject,
                            onValueChange = { teacherSubject = it; errorMessage = null },
                            placeholder = "Materia o Especialidad (ej. Matemáticas y Física)",
                            cursorColor = palette.primary,
                            leadingIcon = {
                                Text("📐", fontSize = 16.sp)
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            testTag = "signup_teacher_subject_input"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AuthPillTextField(
                                value = teacherGrades,
                                onValueChange = { teacherGrades = it; errorMessage = null },
                                placeholder = "Grados a cargo (ej. 10° y 11°)",
                                cursorColor = palette.primary,
                                leadingIcon = {
                                    Text("👥", fontSize = 16.sp)
                                },
                                modifier = Modifier.weight(0.55f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                                testTag = "signup_teacher_grades_input"
                            )

                            AuthPillTextField(
                                value = teacherCode,
                                onValueChange = { teacherCode = it.uppercase(); errorMessage = null },
                                placeholder = "Código Docente (ej. DOC-901)",
                                cursorColor = palette.primary,
                                modifier = Modifier.weight(0.45f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                                testTag = "signup_teacher_code_input"
                            )
                        }

                        AuthPillTextField(
                            value = teacherInstitution,
                            onValueChange = { teacherInstitution = it; errorMessage = null },
                            placeholder = "Institución Educativa (ej. Colegio Escolaris)",
                            cursorColor = palette.primary,
                            leadingIcon = {
                                Text("🏫", fontSize = 16.sp)
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            testTag = "signup_teacher_institution_input"
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.primary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👨‍🏫", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tendrás acceso a Calificar Exámenes, Registrar Asistencia y Retardos, Crear Premios y Tablón Docente.",
                                    fontSize = 11.sp,
                                    color = palette.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                AuthPillTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    placeholder = "Correo Institucional o Personal",
                    cursorColor = palette.primary,
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = AuthTextMuted, modifier = Modifier.size(18.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    testTag = "signup_email_input"
                )

                AuthPillTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    placeholder = "Contraseña (mínimo 6 caracteres)",
                    cursorColor = palette.primary,
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = AuthTextMuted, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = AuthTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    testTag = "signup_password_input"
                )

                AuthPillTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    placeholder = "Confirmar Contraseña",
                    cursorColor = palette.primary,
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = AuthTextMuted, modifier = Modifier.size(18.dp))
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    testTag = "signup_confirm_password_input"
                )

                // Checkbox Política
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Checkbox(
                        checked = agreePrivacy,
                        onCheckedChange = { agreePrivacy = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = palette.primary,
                            uncheckedColor = AuthTextMuted
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acepto los Términos y la Política de Privacidad",
                        fontSize = 12.sp,
                        color = AuthTextMuted,
                        modifier = Modifier.clickable { onShowPrivacyPolicy() }
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Botón de Registro y Enlaces
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (name.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
                            errorMessage = "Por favor completa los nombres, apellidos, correo y contraseña."
                        } else if (selectedRole == UserRole.PARENT.code && studentCodeToLink.isBlank()) {
                            errorMessage = "Debes ingresar el Código del Estudiante (ESC-XXXXXX) para vincularte."
                        } else if (selectedRole == UserRole.TEACHER.code && teacherSubject.isBlank()) {
                            errorMessage = "Por favor ingresa la materia o especialidad que impartes."
                        } else if (selectedRole == UserRole.TEACHER.code && teacherGrades.isBlank()) {
                            errorMessage = "Por favor ingresa los grados o cursos a tu cargo."
                        } else if (password != confirmPassword) {
                            errorMessage = "Las contraseñas no coinciden."
                        } else if (password.length < 6) {
                            errorMessage = "La contraseña debe tener al menos 6 caracteres."
                        } else if (!agreePrivacy) {
                            errorMessage = "Debes aceptar la política de privacidad."
                        } else {
                            val ageInt = ageText.toIntOrNull() ?: 0
                            val finalGradeOrCargo = when (selectedRole) {
                                UserRole.TEACHER.code -> {
                                    val codeSuffix = if (teacherCode.isNotBlank()) " [$teacherCode]" else ""
                                    "$teacherSubject ($teacherGrades) - $teacherInstitution$codeSuffix"
                                }
                                UserRole.PARENT.code -> "Padre/Tutor"
                                else -> gradeSection.ifBlank { "10° Grado" }
                            }

                            val cleanName = com.example.domain.validation.ValidationUtils.formatProperNoun(name)
                            val cleanLastName = com.example.domain.validation.ValidationUtils.formatProperNoun(lastName)

                            onSignUpSubmit(
                                cleanName,
                                cleanLastName,
                                email.trim().lowercase(),
                                password,
                                selectedRole,
                                ageInt,
                                finalGradeOrCargo,
                                studentCodeToLink.trim()
                            )
                        }
                    },
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = AuthTextDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(32.dp))
                        .testTag("signup_submit_button")
                ) {
                    Text(
                        text = "REGISTRARME",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF334155)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onGoogleSignUp() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("🌐", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("O regístrate con Google", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿Ya tienes una cuenta? ",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Inicia sesión",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { onNavigateSignIn() }
                            .testTag("nav_to_signin_link")
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. SIGN IN VIEW (Inicio de Sesión en Español)
// ==========================================
@Composable
fun SignInView(
    palette: AuthThemePalette = BlueAuthPalette,
    isLoading: Boolean,
    onNavigateSignUp: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignInSubmit: (email: String, pass: String) -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        AuthTopBubblesBackground(palette = palette)
        AuthBottomWaveBackground(heightRatio = 0.34f, palette = palette)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabecera con Logo Oficial y Marca Escolaris
            Column(modifier = Modifier.padding(top = 54.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EscolarisAppLogo(size = 52.dp)
                    Text(
                        text = "Escolaris",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.primary,
                        letterSpacing = (-0.5).sp
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "¡Bienvenido!",
                    fontSize = 36.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AuthTextDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ingresa a tu cuenta en Escolaris",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AuthTextMuted
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Entradas de formulario
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AuthPillTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    placeholder = "Correo Electrónico",
                    cursorColor = palette.primary,
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = AuthTextMuted, modifier = Modifier.size(20.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    testTag = "signin_email_input"
                )

                AuthPillTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    placeholder = "Contraseña",
                    cursorColor = palette.primary,
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = AuthTextMuted, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = AuthTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    testTag = "signin_password_input"
                )

                // Recordar sesión + Olvidé contraseña
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = palette.primary,
                                uncheckedColor = AuthTextMuted
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Recordarme",
                            fontSize = 13.sp,
                            color = AuthTextMuted
                        )
                    }

                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuthTextDark,
                        modifier = Modifier
                            .clickable { onForgotPassword() }
                            .testTag("forgot_password_link")
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Botón INICIAR SESIÓN y opciones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "Por favor ingresa tu correo y contraseña."
                        } else {
                            onSignInSubmit(email, password)
                        }
                    },
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = AuthTextDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(32.dp))
                        .testTag("signin_submit_button")
                ) {
                    Text(
                        text = "INICIAR SESIÓN",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF334155)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onGoogleSignIn() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("🌐", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("O inicia sesión con Google", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿No tienes una cuenta? ",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Regístrate gratis",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { onNavigateSignUp() }
                            .testTag("nav_to_signup_link")
                    )
                }
            }
        }
    }
}

// ==========================================
// CUSTOM PILL TEXT FIELD
// ==========================================
@Composable
fun AuthPillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    cursorColor: Color = MagentaAuthPalette.primary,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            singleLine = true,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = cursorColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}

// ==========================================
// DIALOGS
// ==========================================
@Composable
fun ForgotPasswordDialog(
    palette: AuthThemePalette = MagentaAuthPalette,
    onDismiss: () -> Unit,
    onSendReset: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recuperar Contraseña", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ingresa tu correo registrado para recibir las instrucciones de restablecimiento de contraseña:",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuthTextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        onSendReset(email)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
            ) {
                Text("Enviar Enlace")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun PrivacyPolicyDialog(
    palette: AuthThemePalette = MagentaAuthPalette,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Términos & Política de Privacidad", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Escolaris - Plataforma de Monitoreo y Red Social Escolar\n\n" +
                            "1. Privacidad de Datos: Tu información académica, calificaciones y asistencia están cifradas y resguardadas bajo estándares de seguridad.\n\n" +
                            "2. Uso Académico: La plataforma está destinada al seguimiento del progreso estudiantil, colaboración en tareas y gamificación escolar.\n\n" +
                            "3. Compartición Responsable: Los contenidos publicados en el Muro y solicitudes de apuntes deben cumplir las normas de convivencia escolar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuthTextDark
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
            ) {
                Text("Entendido")
            }
        }
    )
}

@Composable
fun GoogleOnboardingDialog(
    email: String,
    initialName: String,
    palette: AuthThemePalette = BlueAuthPalette,
    onDismiss: () -> Unit,
    onComplete: (name: String, role: String, gradeOrCargo: String, codeToLink: String, age: Int) -> Unit
) {
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT.code) }
    val initialFirst = initialName.split(" ").firstOrNull() ?: initialName
    val initialLast = if (initialName.split(" ").size > 1) initialName.split(" ").drop(1).joinToString(" ") else ""
    var firstNames by remember { mutableStateOf(initialFirst) }
    var lastNames by remember { mutableStateOf(initialLast) }
    var preferredName by remember { mutableStateOf(initialFirst) }
    var ageText by remember { mutableStateOf("15") }
    var gradeSection by remember { mutableStateOf("10° Grado") }
    var studentCodeToLink by remember { mutableStateOf("") }

    // Teacher fields
    var teacherSubject by remember { mutableStateOf("") }
    var teacherGrades by remember { mutableStateOf("") }
    var teacherInstitution by remember { mutableStateOf("Colegio Escolaris") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Completa tu Perfil", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(email, fontSize = 12.sp, color = palette.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "¿Cuál es tu rol en la institución?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuthTextDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val roles = listOf(
                        Triple(UserRole.STUDENT.code, "🎓", "Estudiante"),
                        Triple(UserRole.PARENT.code, "👨‍👩‍👧", "Padre"),
                        Triple(UserRole.TEACHER.code, "👨‍🏫", "Docente")
                    )

                    roles.forEach { (roleCode, emoji, label) ->
                        val isSelected = selectedRole == roleCode
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) palette.primary.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) palette.primary else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedRole = roleCode }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) palette.primary else AuthTextDark
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = firstNames,
                        onValueChange = { 
                            firstNames = it
                            if (preferredName.isBlank() || preferredName == initialFirst) {
                                preferredName = it.split(" ").firstOrNull() ?: it
                            }
                            errorMsg = null 
                        },
                        label = { Text("Nombres") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = lastNames,
                        onValueChange = { lastNames = it; errorMsg = null },
                        label = { Text("Apellidos") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = preferredName,
                    onValueChange = { preferredName = it; errorMsg = null },
                    label = { Text("Nombre") },
                    placeholder = { Text("Tu nombre") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                when (selectedRole) {
                    UserRole.STUDENT.code -> {
                        OutlinedTextField(
                            value = gradeSection,
                            onValueChange = { gradeSection = it; errorMsg = null },
                            label = { Text("Grado") },
                            placeholder = { Text("Ej: 10° Grado") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { if (it.all { c -> c.isDigit() }) ageText = it },
                            label = { Text("Edad") },
                            placeholder = { Text("Años") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    UserRole.PARENT.code -> {
                        OutlinedTextField(
                            value = studentCodeToLink,
                            onValueChange = { studentCodeToLink = it.uppercase(); errorMsg = null },
                            label = { Text("Código del estudiante") },
                            placeholder = { Text("Ej: ESC-102938") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    UserRole.TEACHER.code -> {
                        OutlinedTextField(
                            value = teacherSubject,
                            onValueChange = { teacherSubject = it; errorMsg = null },
                            label = { Text("Materia") },
                            placeholder = { Text("Ej: Matemáticas") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = teacherGrades,
                            onValueChange = { teacherGrades = it; errorMsg = null },
                            label = { Text("Grados") },
                            placeholder = { Text("Ej: 9°, 10°, 11°") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = teacherInstitution,
                            onValueChange = { teacherInstitution = it; errorMsg = null },
                            label = { Text("Institución") },
                            placeholder = { Text("Nombre del colegio") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstNames.isBlank()) {
                        errorMsg = "Por favor ingresa tus nombres"
                        return@Button
                    }
                    val cleanFormattedPreferred = com.example.domain.validation.ValidationUtils.formatProperNoun(
                        preferredName.trim().ifBlank {
                            firstNames.trim().split(" ").firstOrNull() ?: firstNames.trim()
                        }
                    )
                    val finalGradeOrCargo = when (selectedRole) {
                        UserRole.TEACHER.code -> listOf(teacherSubject.trim(), teacherGrades.trim()).filter { it.isNotBlank() }.joinToString(" • ").ifBlank { "Docente Titular" }
                        UserRole.PARENT.code -> "Padre / Tutor"
                        else -> gradeSection.trim().ifBlank { "10° Grado" }
                    }
                    val parsedAge = ageText.toIntOrNull() ?: 15
                    onComplete(cleanFormattedPreferred, selectedRole, finalGradeOrCargo, studentCodeToLink.trim(), parsedAge)
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Completar e Ingresar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
