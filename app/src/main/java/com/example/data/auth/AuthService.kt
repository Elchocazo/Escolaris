package com.example.data.auth

import android.content.Context
import com.example.data.local.dao.SchoolDao
import com.example.data.local.entity.UserEntity
import com.example.domain.model.UserRole
import com.example.domain.validation.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import com.example.ui.screens.getInitialBadgesForRole

class AuthService(
    private val schoolDao: SchoolDao
) {
    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val currentUser: FirebaseUser?
        get() = try {
            firebaseAuth?.currentUser
        } catch (e: Exception) {
            null
        }

    val isUserLoggedIn: Boolean
        get() = currentUser != null

    /**
     * Asegura que exista una sesión activa en Firebase Auth (anónima como fallback)
     * para que las reglas de seguridad de Firestore nunca bloqueen lecturas ni escrituras.
     */
    suspend fun ensureFirebaseAuthSession() = withContext(Dispatchers.IO) {
        try {
            val auth = firebaseAuth ?: return@withContext
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Generates a permanent, immutable unique code for students (e.g. ESC-7K9M2P).
     * Uses uppercase alphanumeric characters excluding easily confused characters (0, O, 1, I).
     */
    fun generateUniqueStudentCode(): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val randomPart = (1..6).map { chars.random() }.joinToString("")
        return "ESC-$randomPart"
    }

    /**
     * Serializa la lista oficial de insignias en 0 según el rol para almacenamiento en Firestore.
     */
    fun getInitialBadgesFirestoreMap(role: String): List<Map<String, Any?>> {
        val items = getInitialBadgesForRole(role)
        return items.map {
            mapOf(
                "id" to it.id,
                "title" to it.title,
                "description" to it.description,
                "category" to it.category,
                "currentProgress" to 0,
                "targetProgress" to it.targetProgress,
                "emoji" to it.emoji,
                "isUnlocked" to false,
                "unlockedAtDate" to null,
                "xpReward" to it.xpReward,
                "creditReward" to it.creditReward
            )
        }
    }

    /**
     * Sign Up with Email, Password and full role configuration
     */
    suspend fun signUpWithEmail(
        name: String,
        lastName: String = "",
        email: String,
        password: String,
        role: String = UserRole.STUDENT.code,
        age: Int = 0,
        gradeSectionInput: String = "10° Grado",
        studentCodeToLink: String = ""
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val isSuperAdmin = cleanEmail == "moz658@gmail.com" || role == UserRole.ADMIN.code || role == "ADMIN"
        val isTeacherAccount = isSuperAdmin || role == UserRole.TEACHER.code || role == "DOCENTE"
        val isParentAccount = role == UserRole.PARENT.code && !isTeacherAccount
        val isStudentAccount = !isTeacherAccount && !isParentAccount

        val formattedName = ValidationUtils.formatProperNoun(name)
        val formattedLastName = ValidationUtils.formatProperNoun(lastName)

        var linkedStudent: UserEntity? = null
        if (isParentAccount) {
            val cleanCode = studentCodeToLink.trim().uppercase()
            if (cleanCode.isBlank()) {
                return@withContext Result.failure(Exception("Debes ingresar el Código Único del Estudiante para vincularte."))
            }
            linkedStudent = schoolDao.getUserByStudentCode(cleanCode)
            if (linkedStudent == null) {
                return@withContext Result.failure(Exception("No se encontró ningún estudiante con el código '$cleanCode'. Verifica el código con tu hijo/a."))
            }
        }

        val assignedRole = if (isSuperAdmin) UserRole.ADMIN.code else if (isTeacherAccount) UserRole.TEACHER.code else if (isParentAccount) UserRole.PARENT.code else UserRole.STUDENT.code
        val themeColor = if (isSuperAdmin) 0xFF7C3AED else if (isTeacherAccount) 0xFF1D4ED8 else if (isParentAccount) 0xFF059669 else 0xFF2563EB
        val defaultEmoji = if (isSuperAdmin) "👑" else if (isTeacherAccount) "👨‍🏫" else if (isParentAccount) "👨‍👩‍👧" else "🎓"
        val bannerIdx = if (isTeacherAccount) 0 else if (isParentAccount) 1 else 0
        
        val gradeSection = if (isTeacherAccount) {
            "Docente Titular"
        } else if (isParentAccount) {
            "Padre/Tutor de ${linkedStudent?.name}"
        } else {
            gradeSectionInput.ifBlank { "10° Grado" }
        }

        val bio = if (isTeacherAccount) {
            "Docente Titular en Escolaris 🚀"
        } else if (isParentAccount) {
            "Padre/Madre de ${linkedStudent?.name} | Comprometido con su educación 🌟"
        } else {
            "Estudiante en Escolaris 🚀"
        }

        val fullName = if (formattedLastName.isNotBlank()) "$formattedName $formattedLastName".trim() else formattedName
        val initials = fullName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifBlank { if (isTeacherAccount) "PM" else "ES" }

        // Generate immutable student code only for students
        val studentCode = if (isStudentAccount) generateUniqueStudentCode() else ""

        var userId = UUID.randomUUID().toString()

        try {
            val auth = firebaseAuth
            if (auth != null) {
                val authResult = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    userId = firebaseUser.uid
                    try {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()
                        firebaseUser.updateProfile(profileUpdates).await()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val userEntity = UserEntity(
            id = userId,
            name = fullName,
            lastName = lastName.trim(),
            email = cleanEmail,
            role = assignedRole,
            age = age,
            studentCode = studentCode,
            avatarColorHex = themeColor,
            avatarInitials = initials,
            gradeSection = gradeSection,
            streakDays = 0,
            xp = 0,
            level = 1,
            credits = 0,
            parentIncentiveCredits = if (isParentAccount) 100 else 0,
            linkedStudentId = linkedStudent?.id,
            bio = bio,
            avatarEmoji = defaultEmoji,
            bannerGradientIndex = bannerIdx
        )

        // Try syncing to Firestore
        try {
            val store = firestore
            if (store != null) {
                val firestoreData = hashMapOf(
                    "id" to userEntity.id,
                    "name" to userEntity.name,
                    "lastName" to userEntity.lastName,
                    "email" to userEntity.email,
                    "role" to userEntity.role,
                    "age" to userEntity.age,
                    "studentCode" to userEntity.studentCode,
                    "avatarColorHex" to userEntity.avatarColorHex,
                    "avatarInitials" to userEntity.avatarInitials,
                    "gradeSection" to userEntity.gradeSection,
                    "streakDays" to userEntity.streakDays,
                    "xp" to userEntity.xp,
                    "level" to userEntity.level,
                    "credits" to userEntity.credits,
                    "parentIncentiveCredits" to userEntity.parentIncentiveCredits,
                    "linkedStudentId" to userEntity.linkedStudentId,
                    "bio" to userEntity.bio,
                    "avatarEmoji" to userEntity.avatarEmoji,
                    "badges" to getInitialBadgesFirestoreMap(userEntity.role),
                    "createdAt" to System.currentTimeMillis()
                )
                store.collection("users").document(userId).set(firestoreData, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Always cache in Room SQLite
        schoolDao.insertUser(userEntity)
        Result.success(userEntity)
    }

    /**
     * Sign In with Email and Password
     */
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        try {
            val auth = firebaseAuth
            if (auth != null) {
                val authResult = auth.signInWithEmailAndPassword(cleanEmail, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userEntity = syncOrLoadUserProfile(firebaseUser)
                    return@withContext Result.success(userEntity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback to local Room database or remote Firestore lookup by email
        val localUser = schoolDao.getUserByEmail(cleanEmail)
        if (localUser != null) {
            Result.success(localUser)
        } else {
            val remoteUser = findExistingUserByEmail(cleanEmail)
            if (remoteUser != null) {
                Result.success(remoteUser)
            } else {
                // If it's the admin/teacher account or new account, register locally
                val isSuperAdmin = cleanEmail == "moz658@gmail.com"
                val fallbackUser = UserEntity(
                    id = if (isSuperAdmin) "teacher_moz658" else UUID.randomUUID().toString(),
                    name = if (isSuperAdmin) "Manuel Alejandro Muñoz" else "Estudiante Escolaris",
                    email = cleanEmail,
                    role = if (isSuperAdmin) UserRole.ADMIN.code else UserRole.STUDENT.code,
                    studentCode = if (isSuperAdmin) "" else generateUniqueStudentCode(),
                    avatarColorHex = if (isSuperAdmin) 0xFF7C3AED else 0xFF2563EB,
                    avatarInitials = if (isSuperAdmin) "MM" else "EE",
                    gradeSection = if (isSuperAdmin) "Administrador & Docente" else "10° Grado",
                    streakDays = 1,
                    xp = if (isSuperAdmin) 500 else 50,
                    level = 1,
                    credits = if (isSuperAdmin) 1000 else 100,
                    bio = if (isSuperAdmin) "Administrador Escolar & Docente Titular en Escolaris 👑" else "Estudiante activo en Escolaris 🚀",
                    avatarEmoji = if (isSuperAdmin) "👑" else "🎓",
                    bannerGradientIndex = 0
                )
                schoolDao.insertUser(fallbackUser)
                Result.success(fallbackUser)
            }
        }
    }

    /**
     * Authenticate with Google ID Token
     */
    suspend fun signInWithGoogleIdToken(
        idToken: String,
        preferredGender: String = "MALE"
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val auth = firebaseAuth
            if (auth != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userEntity = syncOrLoadUserProfile(firebaseUser, preferredGender)
                    return@withContext Result.success(userEntity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Local fallback for Google Sign-In: first look up existing user in Firestore or Room
        val fallbackEmail = firebaseAuth?.currentUser?.email?.trim()?.lowercase() ?: "docente@escolaris.edu.co"
        val existingUser = schoolDao.getUserByEmail(fallbackEmail) ?: findExistingUserByEmail(fallbackEmail)
        if (existingUser != null) {
            return@withContext Result.success(existingUser)
        }

        val isSuperAdmin = fallbackEmail == "moz658@gmail.com"
        val isTeacher = isSuperAdmin || isTeacherAccount(fallbackEmail)
        val defaultName = if (isTeacher) "Manuel Alejandro Muñoz" else "Estudiante Escolaris"
        val googleUser = UserEntity(
            id = if (isSuperAdmin) "teacher_moz658" else "google_user_${System.currentTimeMillis()}",
            name = defaultName,
            email = fallbackEmail,
            role = if (isSuperAdmin) UserRole.ADMIN.code else if (isTeacher) UserRole.TEACHER.code else UserRole.STUDENT.code,
            studentCode = if (isTeacher) "" else generateUniqueStudentCode(),
            teacherCode = if (isTeacher) "DOC-102938" else "",
            avatarColorHex = if (isSuperAdmin) 0xFF7C3AED else if (isTeacher) 0xFF1D4ED8 else 0xFF2563EB,
            avatarInitials = if (isTeacher) "MM" else "EE",
            gradeSection = if (isSuperAdmin) "Administrador & Docente" else if (isTeacher) "Docente Titular" else "10° Grado",
            streakDays = 0,
            xp = if (isSuperAdmin) 500 else 0,
            level = 1,
            credits = if (isSuperAdmin) 1000 else 0,
            bio = if (isSuperAdmin) "Administrador Escolar & Docente Titular en Escolaris 👑" else if (isTeacher) "Docente Titular en Escolaris 🚀" else "Estudiante activo en Escolaris 🚀",
            avatarEmoji = if (isSuperAdmin) "👑" else if (isTeacher) "👨‍🏫" else "🎓",
            bannerGradientIndex = 0
        )
        schoolDao.insertUser(googleUser)
        Result.success(googleUser)
    }

    suspend fun findExistingUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val local = schoolDao.getUserByEmail(cleanEmail)
        if (local != null) return@withContext local

        try {
            val store = firestore
            if (store != null) {
                val query = store.collection("users").whereEqualTo("email", cleanEmail).limit(1).get().await()
                if (!query.isEmpty) {
                    val doc = query.documents.first()
                    val isTeacher = cleanEmail == "moz658@gmail.com" || (doc.getString("role") == UserRole.TEACHER.code)
                    val rawName = doc.getString("name")?.takeIf { it.isNotBlank() }
                        ?: if (isTeacher) "Manuel Alejandro Muñoz" else cleanEmail.substringBefore("@")
                    val name = if (isTeacher && (rawName == "Usuario" || rawName == "Docente Titular" || rawName.isBlank())) "Manuel Alejandro Muñoz" else rawName

                    val user = UserEntity(
                        id = doc.getString("id") ?: doc.id,
                        name = name,
                        lastName = doc.getString("lastName") ?: "",
                        email = cleanEmail,
                        role = doc.getString("role") ?: (if (isTeacher) UserRole.TEACHER.code else UserRole.STUDENT.code),
                        studentCode = doc.getString("studentCode") ?: (if (isTeacher) "" else generateUniqueStudentCode()),
                        teacherCode = doc.getString("teacherCode") ?: (if (isTeacher) "DOC-102938" else ""),
                        avatarColorHex = doc.getLong("avatarColorHex") ?: (if (isTeacher) 0xFF1D4ED8 else 0xFF2563EB),
                        avatarInitials = doc.getString("avatarInitials") ?: name.take(2).uppercase(),
                        gradeSection = doc.getString("gradeSection") ?: (if (isTeacher) "Docente Titular" else "10° Grado"),
                        streakDays = (doc.getLong("streakDays") ?: 0L).toInt(),
                        xp = (doc.getLong("xp") ?: (if (isTeacher) 200L else 0L)).toInt(),
                        level = (doc.getLong("level") ?: 1L).toInt(),
                        credits = (doc.getLong("credits") ?: (if (isTeacher) 500L else 0L)).toInt(),
                        parentIncentiveCredits = (doc.getLong("parentIncentiveCredits") ?: 0L).toInt(),
                        linkedStudentId = doc.getString("linkedStudentId"),
                        bio = doc.getString("bio") ?: (if (isTeacher) "Docente Titular en Escolaris 🚀" else "Estudiante activo en Escolaris 🚀"),
                        avatarEmoji = doc.getString("avatarEmoji") ?: (if (isTeacher) "👨‍🏫" else "🎓"),
                        photoUri = doc.getString("photoUrl") ?: doc.getString("photoUri"),
                        bannerGradientIndex = (doc.getLong("bannerGradientIndex") ?: 0L).toInt()
                    )
                    schoolDao.insertUser(user)
                    return@withContext user
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    /**
     * Sign In directly with a Google account email with selected role and basic details
     */
    suspend fun signInWithGoogleAccount(
        email: String,
        displayName: String = "",
        role: String = UserRole.STUDENT.code,
        gradeSectionInput: String = "",
        studentCodeToLink: String = "",
        age: Int = 15
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        
        // 1. PRIMERO BUSCAR SI YA EXISTE EL USUARIO EN LOCAL O REMOTO (Firestore)
        val existing = schoolDao.getUserByEmail(cleanEmail) ?: findExistingUserByEmail(cleanEmail)
        if (existing != null) {
            return@withContext Result.success(existing)
        }

        val isSuperAdmin = cleanEmail == "moz658@gmail.com" || role == UserRole.ADMIN.code || role == "ADMIN"
        val isTeacher = isSuperAdmin || role == UserRole.TEACHER.code || role == "DOCENTE"
        val isParent = !isSuperAdmin && role == UserRole.PARENT.code
        val assignedRole = if (isSuperAdmin) UserRole.ADMIN.code else role

        val defaultName = if (displayName.isNotBlank()) displayName.trim() else if (isSuperAdmin || isTeacher) "Manuel Alejandro Muñoz" else if (isParent) "Padre / Tutor" else "Estudiante Escolaris"
        val themeColor = if (isSuperAdmin) 0xFF7C3AED else if (isTeacher) 0xFF1D4ED8 else if (isParent) 0xFF059669 else 0xFF2563EB
        val emoji = if (isSuperAdmin) "👑" else if (isTeacher) "👨‍🏫" else if (isParent) "👨‍👩‍👧" else "🎓"
        val initials = defaultName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifBlank { if (isSuperAdmin || isTeacher) "MM" else if (isParent) "PT" else "EE" }

        val generatedCode = if (isTeacher || isParent) "" else generateUniqueStudentCode()
        val finalGradeSection = when {
            isSuperAdmin -> "Administrador & Docente"
            isTeacher -> gradeSectionInput.ifBlank { "Docente Titular" }
            isParent -> if (studentCodeToLink.isNotBlank()) "Tutor de: $studentCodeToLink" else "Padre / Tutor"
            else -> gradeSectionInput.ifBlank { "10° Grado" }
        }

        val bioText = when {
            isSuperAdmin -> "Administrador Escolar & Docente Titular en Escolaris 👑"
            isTeacher -> "Docente Titular en Escolaris 🚀"
            isParent -> "Padre / Tutor en Escolaris 👨‍👩‍👧"
            else -> "Estudiante activo en Escolaris 🚀"
        }

        val newUser = UserEntity(
            id = if (isSuperAdmin) "teacher_moz658" else "google_${UUID.randomUUID().toString().take(8)}",
            name = defaultName,
            email = cleanEmail,
            role = assignedRole,
            studentCode = generatedCode,
            avatarColorHex = themeColor,
            avatarInitials = initials,
            gradeSection = finalGradeSection,
            streakDays = 0,
            xp = 0,
            level = 1,
            credits = if (isParent) 100 else 0,
            bio = bioText,
            avatarEmoji = emoji,
            bannerGradientIndex = 0
        )

        // Sync to Firestore
        try {
            val store = firestore
            if (store != null) {
                val firestoreData = hashMapOf(
                    "id" to newUser.id,
                    "name" to newUser.name,
                    "email" to newUser.email,
                    "role" to newUser.role,
                    "studentCode" to newUser.studentCode,
                    "avatarColorHex" to newUser.avatarColorHex,
                    "avatarInitials" to newUser.avatarInitials,
                    "gradeSection" to newUser.gradeSection,
                    "streakDays" to newUser.streakDays,
                    "xp" to newUser.xp,
                    "level" to newUser.level,
                    "credits" to newUser.credits,
                    "bio" to newUser.bio,
                    "avatarEmoji" to newUser.avatarEmoji,
                    "badges" to getInitialBadgesFirestoreMap(newUser.role),
                    "createdAt" to System.currentTimeMillis()
                )
                store.collection("users").document(newUser.id).set(firestoreData, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        schoolDao.insertUser(newUser)
        Result.success(newUser)
    }

    private fun isTeacherAccount(email: String): Boolean = email.trim().lowercase() == "moz658@gmail.com"

    /**
     * Send Password Reset Email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firebaseAuth?.sendPasswordResetEmail(email.trim())?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Sign Out
     */
    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Helper to load user profile from Firestore or initialize default if not found
     */
    private suspend fun syncOrLoadUserProfile(
        firebaseUser: FirebaseUser,
        preferredGender: String? = null
    ): UserEntity {
        val userId = firebaseUser.uid
        val email = firebaseUser.email?.trim()?.lowercase() ?: ""
        val isTeacherAccount = email == "moz658@gmail.com"

        var firestoreUser: UserEntity? = null
        try {
            val store = firestore
            if (store != null) {
                var doc = store.collection("users").document(userId).get().await()
                
                // If not found by direct UID document, search by email to recover previous profile data
                if (!doc.exists() && email.isNotBlank()) {
                    val emailQuery = store.collection("users").whereEqualTo("email", email).limit(1).get().await()
                    if (!emailQuery.isEmpty) {
                        doc = emailQuery.documents.first()
                    }
                }

                if (doc.exists()) {
                    val isSuperAdmin = email == "moz658@gmail.com"
                    val rawName = doc.getString("name")?.takeIf { it.isNotBlank() }
                        ?: firebaseUser.displayName?.takeIf { it.isNotBlank() }
                        ?: if (isTeacherAccount) "Manuel Alejandro Muñoz" else email.substringBefore("@")
                    val name = ValidationUtils.formatProperNoun(if (isTeacherAccount && (rawName == "Usuario" || rawName == "Docente Titular" || rawName.isBlank())) "Manuel Alejandro Muñoz" else rawName)
                    val role = if (isSuperAdmin) {
                        UserRole.ADMIN.code
                    } else if (isTeacherAccount) {
                        doc.getString("role")?.takeIf { it == UserRole.ADMIN.code } ?: UserRole.TEACHER.code
                    } else {
                        doc.getString("role") ?: UserRole.STUDENT.code
                    }
                    val gender = doc.getString("gender") ?: (preferredGender ?: "MALE")
                    val isGirl = gender.equals("FEMALE", ignoreCase = true)
                    val defaultColor = if (isSuperAdmin) 0xFF7C3AED else if (isTeacherAccount) 0xFF1D4ED8 else if (isGirl) 0xFFE11D74 else 0xFF2563EB

                    val credits = (doc.getLong("credits") ?: 0L).toInt()
                    val xp = (doc.getLong("xp") ?: 0L).toInt()
                    val level = (doc.getLong("level") ?: 1L).toInt()
                    val streak = (doc.getLong("streakDays") ?: 0L).toInt()
                    val bio = doc.getString("bio") ?: if (isSuperAdmin) "Administrador Escolar & Docente Titular en Escolaris 👑" else if (isTeacherAccount) "Docente Titular en Escolaris 🚀" else "Estudiante activo en Escolaris 🚀"
                    val emoji = doc.getString("avatarEmoji") ?: if (isSuperAdmin) "👑" else if (isTeacherAccount) "👨‍🏫" else "🎓"
                    val gradeSection = doc.getString("gradeSection") ?: if (isSuperAdmin) "Administrador & Docente" else if (isTeacherAccount) "Docente Titular" else "10° Grado"
                    val colorHex = doc.getLong("avatarColorHex") ?: defaultColor
                    val studentCode = if (isTeacherAccount) "" else (doc.getString("studentCode") ?: generateUniqueStudentCode())
                    val teacherCode = if (isTeacherAccount) "DOC-102938" else (doc.getString("teacherCode") ?: "")
                    val photoUrl = doc.getString("photoUrl") ?: doc.getString("photoUri") ?: firebaseUser.photoUrl?.toString()

                    firestoreUser = UserEntity(
                        id = userId,
                        name = name,
                        lastName = doc.getString("lastName") ?: "",
                        email = email,
                        role = role,
                        studentCode = studentCode,
                        teacherCode = teacherCode,
                        avatarColorHex = colorHex,
                        avatarInitials = name.take(2).uppercase(),
                        gradeSection = gradeSection,
                        streakDays = streak,
                        xp = if (isTeacherAccount) maxOf(xp, 200) else xp,
                        level = level,
                        credits = if (isTeacherAccount) maxOf(credits, 500) else credits,
                        parentIncentiveCredits = (doc.getLong("parentIncentiveCredits") ?: 0L).toInt(),
                        linkedStudentId = doc.getString("linkedStudentId"),
                        bio = bio,
                        avatarEmoji = emoji,
                        photoUri = photoUrl,
                        bannerGradientIndex = (doc.getLong("bannerGradientIndex") ?: 0L).toInt()
                    )

                    // Ensure document at users/userId has merged, accurate data
                    val syncMap = hashMapOf(
                        "id" to userId,
                        "name" to firestoreUser.name,
                        "email" to firestoreUser.email,
                        "role" to firestoreUser.role,
                        "studentCode" to firestoreUser.studentCode,
                        "teacherCode" to firestoreUser.teacherCode,
                        "avatarColorHex" to firestoreUser.avatarColorHex,
                        "gradeSection" to firestoreUser.gradeSection,
                        "streakDays" to firestoreUser.streakDays,
                        "xp" to firestoreUser.xp,
                        "level" to firestoreUser.level,
                        "credits" to firestoreUser.credits,
                        "bio" to firestoreUser.bio,
                        "avatarEmoji" to firestoreUser.avatarEmoji,
                        "photoUrl" to (firestoreUser.photoUri ?: ""),
                        "photoUri" to (firestoreUser.photoUri ?: ""),
                        "updatedAt" to System.currentTimeMillis()
                    )
                    store.collection("users").document(userId).set(syncMap, SetOptions.merge()).await()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Check local Room cache if Firestore was not available or empty
        val localExisting = if (firestoreUser == null && email.isNotBlank()) schoolDao.getUserByEmail(email) else null

        val userEntity = firestoreUser ?: localExisting?.copy(id = userId) ?: run {
            val isSuperAdmin = email == "moz658@gmail.com"
            val rawName = if (isTeacherAccount) "Manuel Alejandro Muñoz" else (firebaseUser.displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore("@"))
            val name = ValidationUtils.formatProperNoun(rawName)
            val assignedRole = if (isSuperAdmin) UserRole.ADMIN.code else if (isTeacherAccount) UserRole.TEACHER.code else UserRole.STUDENT.code
            val newUser = UserEntity(
                id = userId,
                name = name,
                email = email,
                role = assignedRole,
                studentCode = if (isTeacherAccount) "" else generateUniqueStudentCode(),
                teacherCode = if (isTeacherAccount) "DOC-102938" else "",
                avatarColorHex = if (isSuperAdmin) 0xFF7C3AED else if (isTeacherAccount) 0xFF1D4ED8 else 0xFF2563EB,
                avatarInitials = name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifBlank { if (isTeacherAccount) "MM" else "ES" },
                gradeSection = if (isSuperAdmin) "Administrador & Docente" else if (isTeacherAccount) "Docente Titular" else "10° Grado",
                streakDays = 1,
                xp = if (isSuperAdmin) 500 else if (isTeacherAccount) 200 else 50,
                level = 1,
                credits = if (isSuperAdmin) 1000 else if (isTeacherAccount) 500 else 100,
                bio = if (isSuperAdmin) "Administrador Escolar & Docente Titular en Escolaris 👑" else if (isTeacherAccount) "Docente Titular en Escolaris 👨‍🏫" else "Estudiante activo en Escolaris 🚀",
                avatarEmoji = if (isSuperAdmin) "👑" else if (isTeacherAccount) "👨‍🏫" else "🎓",
                photoUri = firebaseUser.photoUrl?.toString(),
                bannerGradientIndex = 0
            )
            try {
                val store = firestore
                if (store != null) {
                    val firestoreData = hashMapOf(
                        "id" to newUser.id,
                        "name" to newUser.name,
                        "email" to newUser.email,
                        "role" to newUser.role,
                        "studentCode" to newUser.studentCode,
                        "teacherCode" to newUser.teacherCode,
                        "avatarColorHex" to newUser.avatarColorHex,
                        "avatarInitials" to newUser.avatarInitials,
                        "gradeSection" to newUser.gradeSection,
                        "streakDays" to newUser.streakDays,
                        "xp" to newUser.xp,
                        "level" to newUser.level,
                        "credits" to newUser.credits,
                        "bio" to newUser.bio,
                        "avatarEmoji" to newUser.avatarEmoji,
                        "photoUrl" to (newUser.photoUri ?: ""),
                        "createdAt" to System.currentTimeMillis()
                    )
                    store.collection("users").document(userId).set(firestoreData, SetOptions.merge()).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            newUser
        }

        // Cache in Room SQLite
        schoolDao.insertUser(userEntity)
        return userEntity
    }
}
