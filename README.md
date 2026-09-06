# 🚀 Escolaris - Red Social Escolar & Plataforma de Gestión Académica

**Escolaris** es una plataforma móvil moderna para la gestión escolar, gamificación y colaboración estudiantil desarrollada en **Android nativo** con **Jetpack Compose**, **Kotlin Coroutines / Flow**, **Room Database** y **Material 3 / Claymorphism UI**.

---

## 🏛️ Arquitectura del Sistema

El proyecto sigue los principios de **Clean Architecture** y **MVVM Reactivo** con separación estricta de responsabilidades en 3 capas principales:

```
┌─────────────────────────────────────────────────────────────┐
│                       UI / PRESENTATION                     │
│  Composables (Screens & Claymorphic Components) + ViewModels │
│              Reactive State: StateFlow & UiState<T>          │
└──────────────────────────────▲──────────────────────────────┘
                               │
┌──────────────────────────────┴──────────────────────────────┐
│                        DOMAIN LAYER                         │
│   Immutable Domain Models (User, Task, Exam, Badge, etc.)   │
│   Strongly-typed Enums (UserRole, PostType, TaskStatus)     │
│   Business Rules & Input Validation (ValidationUtils)       │
└──────────────────────────────▲──────────────────────────────┘
                               │
┌──────────────────────────────┴──────────────────────────────┐
│                         DATA LAYER                          │
│   ISchoolRepository (Contract) & SchoolRepository (Impl)    │
│   Room Database (11 Entities + Atomic Transactions)         │
│   Database Seeder & Native Export (PDF & Calendar .ics)     │
└─────────────────────────────────────────────────────────────┘
```

---

## 🌟 Características Principales

1. **Muro Escolar Colaborativo & "Auxilio Apuntes" (🚨)**:
   - Publicación de avisos, eventos y solicitudes de apuntes en caso de llegadas tarde o inasistencias.
   - Hilos de respuestas con evidencia fotográfica de pizarras y cuadernos.
   - Recompensas automáticas de créditos escolares por ayudar a compañeros (+25/+50 pts).

2. **Evaluaciones & Calificador Oficial (Escala 1.0 a 5.0)**:
   - Registro de exámenes con cálculo automático de promedio GPA ponderado.
   - Escáner simulado de pruebas físicas con OCR y digitalización de evidencias.
   - Exportación de informes académicos oficiales en **PDF** y sincronización con **Google Calendar / iCloud (.ics)**.

3. **Gamificación y Cuadro de Honor**:
   - Sistema de niveles, racha semanal de hábitos (🔥) y puntos XP.
   - Tabla de clasificación del curso (*Leaderboard*).
   - Insignias de honor 3D con estilo *Claymorphism* desbloqueadas por el Docente Titular.

4. **Portal Docente SuperAdmin & Acudientes**:
   - Registro y justificación de retardos con alerta instantánea a los padres.
   - Calificador interactivo y otorgamiento de condecoraciones institucionales.
   - Catálogo de recompensas y canje de pases con código QR y validación en aula.

5. **Diseño Adaptativo & Personalización**:
   - Soporte dinámico para **Modo Claro** y **Modo Oscuro**.
   - 4 paletas temáticas institucionales (*Azul Colegial, Esmeralda, Púrpura Creativo, Ámbar Solar*).
   - Adaptación responsiva para teléfonos móviles y tablets (*NavigationRail*).

---

## 📂 Estructura de Paquetes

```
com.example/
├── MainActivity.kt                 # Punto de entrada y host de navegación
├── domain/
│   ├── model/DomainModels.kt       # Modelos inmutables y mappers de entidades
│   └── validation/ValidationUtils.kt # Validaciones de dominio (1.0-5.0, textos)
├── data/
│   ├── local/                      # Room Database, Entidades y Seeder
│   │   ├── EscolarisDatabase.kt
│   │   ├── DatabaseInitializer.kt
│   │   ├── dao/SchoolDao.kt
│   │   └── entity/                 # 11 Entidades SQLite
│   └── repository/
│       └── SchoolRepository.kt     # ISchoolRepository y lógica de persistencia
├── ui/
│   ├── state/UiState.kt            # Modelos de estado reactivo y eventos UI
│   ├── navigation/Screen.kt        # Rutas de navegación por roles (RBAC)
│   ├── theme/                      # Paleta de colores, tipografías y tema M3
│   ├── components/                 # Tarjetas Claymorphism, barras y diálogos
│   ├── screens/                    # Pantallas de la aplicación
│   └── viewmodel/SchoolViewModel.kt# ViewModel centralizado y reactivo
└── utils/
    ├── CalendarSyncHelper.kt       # Sincronización .ics y CalendarContract
    ├── NotificationHelper.kt       # Canales de notificaciones push
    └── PdfExporter.kt              # Generación nativa de reportes PDF A4
```

---

## 🛠️ Requisitos y Compilación

- **Android Studio**: Ladybug / Meerkat o superior
- **JDK**: Java 17 o Java 21
- **Gradle**: 8.7+
- **Min SDK**: 24 (Android 7.0) | **Target SDK / Compile SDK**: 36 (Android 15+)

### Pasos para compilar:
```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/escolaris.git
cd escolaris

# Ejecutar pruebas unitarias
./gradlew testDebugUnitTest

# Compilar APK de depuración
./gradlew assembleDebug
```

---

## 🔒 Variables de Entorno y Seguridad

- Las credenciales y claves de API externas deben configurarse en el archivo `.env` en la raíz del proyecto (basado en `.env.example`).
- La persistencia local en Room utiliza transacciones atómicas para operaciones financieras de créditos y condecoraciones.
- Los reportes generados se comparten a través de `FileProvider` con permisos de solo lectura temporales (`FLAG_GRANT_READ_URI_PERMISSION`).

---

## 🧪 Estrategia de Pruebas

El proyecto cuenta con una suite de pruebas automatizadas:
- **`ValidationUtilsTest.kt`**: Validación estricta de la escala de notas (1.0 a 5.0), formatos de hora y límites de créditos.
- **`SchoolDaoTest.kt`**: Pruebas de integración de base de datos Room en memoria con Robolectric.
- **`GreetingScreenshotTest.kt`**: Pruebas visuales de interfaz mediante Roborazzi.
