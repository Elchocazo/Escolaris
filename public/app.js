// =========================================================================
// ESCOLARIS PWA CLIENT APPLICATION LOGIC (FIREBASE & LIVE SYNC)
// =========================================================================

// 1. Firebase Configuration & Init
let app, auth, db, messaging;
try {
  if (firebase.apps.length > 0) {
    app = firebase.app();
  } else {
    app = firebase.initializeApp({
      apiKey: window.__FIREBASE_API_KEY__ || "FIREBASE_API_KEY_PLACEHOLDER",
      authDomain: "escolaris-ab151.firebaseapp.com",
      projectId: "escolaris-ab151",
      storageBucket: "escolaris-ab151.firebasestorage.app",
      messagingSenderId: "1039847291823",
      appId: "1:1039847291823:web:escolaris_pwa_id"
    });
  }
  auth = firebase.auth();
  try {
    auth.setPersistence(firebase.auth.Auth.Persistence.LOCAL).catch(() => {});
  } catch (e) {}
  db = firebase.firestore();
  db.enablePersistence().catch(() => {});

  if (firebase.messaging && firebase.messaging.isSupported()) {
    try {
      messaging = firebase.messaging();
    } catch (e) {
      console.warn("FCM init warning:", e);
    }
  }
} catch (e) {
  console.warn("Firebase Init fallback:", e);
}

// 2. Global Application State
let currentUser = null;
let currentTab = 'feed';
let navHistory = ['feed'];
let activeTaskFilter = 'pending';
let activeAdminTab = 'users';
let activeScheduleDay = 'Lunes';
let allUsers = [];
let userTasks = [];
let feedPosts = [];
let currentFeedFilter = 'todos';
let allTardies = [];
let allBadges = [];
let allPenalties = [];
let allParentObligations = [];
let activeObligationFilter = 'pending';
let leaderboardUsers = [];
let userRedemptions = [];

let selectedSignupRole = 'STUDENT';
let selectedOnboardRole = 'STUDENT';
let selectedOnboardAvatar = '🎓';
let selectedBadgeType = 'FAMILY';

// Rangos Escolares Oficiales (Contexto Escolar Casual y Motivacional)
const SCHOOL_RANKS = [
  { level: 1, title: 'Explorador Escolar', emoji: '🎒', minXp: 0, maxXp: 299, desc: 'Iniciando el año escolar con entusiasmo, útiles listos y curiosidad diaria.' },
  { level: 2, title: 'Estudiante Dedicado', emoji: '📚', minXp: 300, maxXp: 599, desc: 'Constancia diaria, cuadernos organizados y tareas entregadas a tiempo.' },
  { level: 3, title: 'Compañero Guía', emoji: '💡', minXp: 600, maxXp: 899, desc: 'Ejemplo de convivencia, apoyo a sus compañeros y trabajo en equipo.' },
  { level: 4, title: 'Líder de Clase', emoji: '⭐', minXp: 900, maxXp: 1199, desc: 'Rendimiento académico destacado y alto compromiso institucional.' },
  { level: 5, title: 'Orgullo Elisiano', emoji: '🏆', minXp: 1200, maxXp: Infinity, desc: 'Máximo honor escolar: excelencia académica, humana y comunitaria.' }
];

function getSchoolRank(xp) {
  const userXp = xp || 0;
  for (let i = SCHOOL_RANKS.length - 1; i >= 0; i--) {
    if (userXp >= SCHOOL_RANKS[i].minXp) {
      return SCHOOL_RANKS[i];
    }
  }
  return SCHOOL_RANKS[0];
}

// -----------------------------------------------------------------------------
// LISTA MAESTRA OFICIAL DE INSIGNIAS BASE (PROGRESO INICIAL EN CERO)
// -----------------------------------------------------------------------------
function getInitialBadgesForRole(role) {
  const normRole = (role || '').trim().toUpperCase();
  const isParent = normRole === 'PARENT' || normRole === 'TUTOR' || normRole === 'ACUDIENTE';

  if (isParent) {
    return [
      {
        id: 'badge_parent_report_cards',
        title: 'Entrega de Boletines',
        description: 'Asistir puntualmente a las 4 entregas oficiales de informes académicos.',
        category: 'PARENT',
        currentProgress: 0,
        targetProgress: 4,
        emoji: '📋',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 250,
        creditReward: 120
      },
      {
        id: 'badge_parent_workshops',
        title: 'Escuela de Padres',
        description: 'Participar activamente en 2 talleres de formación y escuela de padres.',
        category: 'PARENT',
        currentProgress: 0,
        targetProgress: 2,
        emoji: '👨‍👩‍👧',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 300,
        creditReward: 150
      },
      {
        id: 'badge_parent_pension',
        title: 'Compromiso de Pensión',
        description: 'Pagar puntualmente la pensión durante 3 meses consecutivos dentro de los primeros 5 días del mes.',
        category: 'PARENT',
        currentProgress: 0,
        targetProgress: 3,
        emoji: '💳',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 350,
        creditReward: 200
      },
      {
        id: 'badge_parent_meetings',
        title: 'Reunión de Padres',
        description: 'Asistir y participar en las asambleas generales y reuniones informativas de curso con docentes.',
        category: 'PARENT',
        currentProgress: 0,
        targetProgress: 2,
        emoji: '🤝',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 200,
        creditReward: 100
      },
      {
        id: 'badge_parent_support',
        title: 'Acompañamiento',
        description: 'Supervisión diaria y apoyo formativo en las tareas y deberes escolares en casa.',
        category: 'PARENT',
        currentProgress: 0,
        targetProgress: 5,
        emoji: '🏡',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 180,
        creditReward: 90
      }
    ];
  } else {
    return [
      {
        id: 'badge_student_tasks_10',
        title: 'Cumplimiento de Tareas',
        description: 'Entregar 10 tareas a tiempo y completas antes del cierre de periodo escolar.',
        category: 'ACADEMIC',
        currentProgress: 0,
        targetProgress: 10,
        emoji: '📚',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 200,
        creditReward: 100
      },
      {
        id: 'badge_student_attendance_20',
        title: 'Asistencia Ejemplar',
        description: 'Acumular 20 días de asistencia continua sin retardos ni ausencias.',
        category: 'STUDENT',
        currentProgress: 0,
        targetProgress: 20,
        emoji: '⏰',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 220,
        creditReward: 110
      },
      {
        id: 'badge_student_academic_excellence_5',
        title: 'Excelencia Académica',
        description: 'Aprobar 5 evaluaciones o exámenes bimestrales con calificación sobresaliente (4.5 o más).',
        category: 'ACADEMIC',
        currentProgress: 0,
        targetProgress: 5,
        emoji: '⭐',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 300,
        creditReward: 150
      },
      {
        id: 'badge_student_steam',
        title: 'Innovador STEAM',
        description: 'Presentar un proyecto destacado en la Feria de Ciencia, Tecnología y Robótica.',
        category: 'ACADEMIC',
        currentProgress: 0,
        targetProgress: 1,
        emoji: '🔬',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 400,
        creditReward: 250
      },
      {
        id: 'badge_student_reading',
        title: 'Lector Voraz',
        description: 'Completar la lectura y análisis de 6 obras literarias en el Plan Lector escolar.',
        category: 'ACADEMIC',
        currentProgress: 0,
        targetProgress: 6,
        emoji: '📖',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 180,
        creditReward: 90
      },
      {
        id: 'badge_student_civic',
        title: 'Líder de Paz',
        description: 'Participar en 3 jornadas de mediación escolar y sana convivencia.',
        category: 'STUDENT',
        currentProgress: 0,
        targetProgress: 3,
        emoji: '🕊️',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 200,
        creditReward: 100
      },
      {
        id: 'badge_student_solidarity',
        title: 'Compañero Solidario',
        description: 'Reconocimiento por trabajo en equipo, empatía y apoyo a compañeros de clase.',
        category: 'STUDENT',
        currentProgress: 0,
        targetProgress: 2,
        emoji: '🤝',
        isUnlocked: false,
        unlockedAtDate: null,
        xpReward: 250,
        creditReward: 120
      }
    ];
  }
}

const familyBadgePresets = [
  { key: 'PARENT_REPORT_CARDS', id: 'badge_parent_report_cards', title: 'Entrega de Boletines', desc: 'Asistir puntualmente a las 4 entregas oficiales de informes académicos.', emoji: '📋', targetProgress: 4, currentProgress: 0 },
  { key: 'PARENT_WORKSHOPS', id: 'badge_parent_workshops', title: 'Escuela de Padres', desc: 'Participar activamente en 2 talleres de formación y escuela de padres.', emoji: '👨‍👩‍👧', targetProgress: 2, currentProgress: 0 },
  { key: 'PARENT_PENSION', id: 'badge_parent_pension', title: 'Compromiso de Pensión', desc: 'Pagar puntualmente la pensión durante 3 meses consecutivos dentro de los primeros 5 días del mes.', emoji: '💳', targetProgress: 3, currentProgress: 0 },
  { key: 'PARENT_MEETINGS', id: 'badge_parent_meetings', title: 'Reunión de Padres', desc: 'Asistir y participar en las asambleas generales y reuniones informativas de curso con docentes.', emoji: '🤝', targetProgress: 2, currentProgress: 0 },
  { key: 'PARENT_SUPPORT', id: 'badge_parent_support', title: 'Acompañamiento', desc: 'Supervisión diaria y apoyo formativo en las tareas y deberes escolares en casa.', emoji: '🏡', targetProgress: 5, currentProgress: 0 },
  { key: 'PARENT_PENSION_OCTUBRE', title: 'Pago Oportuno de Pensión (Octubre)', desc: 'Cancelación oportuna de la pensión escolar en los 5 primeros días del mes.', emoji: '💳', targetProgress: 1, currentProgress: 0 },
  { key: 'PARENT_PENSION_SEMESTER', title: 'Pensión al Día - Semestre', desc: 'Cumplimiento mensual impecable de pensiones escolares.', emoji: '💎', targetProgress: 5, currentProgress: 0 },
  { key: 'PARENT_EXEMPLARY_TUTOR', title: 'Tutor Ejemplar & Puntual', desc: 'Acompañamiento integral, puntualidad y constante apoyo formativo.', emoji: '👑', targetProgress: 1, currentProgress: 0 },
  { key: 'FAMILY_EXCELLENCE', title: 'Familia Ejemplar', desc: 'Acompañamiento formativo, disciplina y apoyo educativo en casa.', emoji: '🏆', targetProgress: 1, currentProgress: 0 },
  { key: 'PUNCTUAL_FAMILY', title: 'Familia Puntual', desc: 'Puntualidad intachable y cero retardos escolares.', emoji: '⏰', targetProgress: 1, currentProgress: 0 },
  { key: 'READING_AT_HOME', title: 'Lectura en Familia', desc: 'Fomento de la lectura compartida y hábito lector en el hogar.', emoji: '📖', targetProgress: 1, currentProgress: 0 },
  { key: 'HOMEWORK_SUPPORT', title: 'Apoyo en Tareas', desc: 'Acompañamiento positivo en el cumplimiento de deberes escolares.', emoji: '✍️', targetProgress: 1, currentProgress: 0 }
];

const studentBadgePresets = [
  { key: 'STUDENT_TASKS_10', id: 'badge_student_tasks_10', title: 'Cumplimiento de Tareas', desc: 'Entregar 10 tareas a tiempo y completas antes del cierre de periodo escolar.', emoji: '📚', targetProgress: 10, currentProgress: 0 },
  { key: 'STUDENT_ATTENDANCE_20', id: 'badge_student_attendance_20', title: 'Asistencia Ejemplar', desc: 'Acumular 20 días de asistencia continua sin retardos ni ausencias.', emoji: '⏰', targetProgress: 20, currentProgress: 0 },
  { key: 'STUDENT_ACADEMIC_EXCELLENCE_5', id: 'badge_student_academic_excellence_5', title: 'Excelencia Académica', desc: 'Aprobar 5 evaluaciones o exámenes bimestrales con calificación sobresaliente (4.5 o más).', emoji: '⭐', targetProgress: 5, currentProgress: 0 },
  { key: 'STUDENT_STEAM', id: 'badge_student_steam', title: 'Innovador STEAM', desc: 'Presentar un proyecto destacado en la Feria de Ciencia, Tecnología y Robótica.', emoji: '🔬', targetProgress: 1, currentProgress: 0 },
  { key: 'STUDENT_READING', id: 'badge_student_reading', title: 'Lector Voraz', desc: 'Completar la lectura y análisis de 6 obras literarias en el Plan Lector escolar.', emoji: '📖', targetProgress: 6, currentProgress: 0 },
  { key: 'STUDENT_CIVIC', id: 'badge_student_civic', title: 'Líder de Paz', desc: 'Participar en 3 jornadas de mediación escolar y sana convivencia.', emoji: '🕊️', targetProgress: 3, currentProgress: 0 },
  { key: 'PEER_HELPER', id: 'badge_student_solidarity', title: 'Compañero Solidario', desc: 'Reconocimiento por trabajo en equipo, empatía y apoyo a compañeros de clase.', emoji: '🤝', targetProgress: 2, currentProgress: 0 },
  { key: 'FLAG_RAISING', title: 'Izada de Bandera', desc: 'Honor patrio, rendimiento académico y convivencia escolar ejemplar.', emoji: '🇨🇴', targetProgress: 1, currentProgress: 0 },
  { key: 'FIRST_GRADE_5', title: 'Nota Sobresaliente (5.0)', desc: 'Calificación excelente en una evaluación o taller formativo.', emoji: '⭐', targetProgress: 1, currentProgress: 0 },
  { key: 'PERFECT_ATTENDANCE', title: 'Puntualidad de Oro', desc: 'Asistencia impecable y llegada a tiempo a todas las clases.', emoji: '⏰', targetProgress: 1, currentProgress: 0 },
  { key: 'BOOK_DEVOURER', title: 'Lector Entusiasta', desc: 'Compromiso constante con la lectura y comprensión crítica de textos.', emoji: '📖', targetProgress: 1, currentProgress: 0 },
  { key: 'SCIENCE_EXPLORER', title: 'Científico Escolar', desc: 'Curiosidad e investigación destacada en experimentos de ciencias y química.', emoji: '🔬', targetProgress: 1, currentProgress: 0 },
  { key: 'STREAK_7_DAYS', title: 'Semana Impecable', desc: 'Una semana completa con tareas entregadas y disciplina escolar constante.', emoji: '🔥', targetProgress: 1, currentProgress: 0 }
];

let activeRewardsCategory = 'ALL';

const OFFICIAL_DEFAULT_REWARDS = [
  // NIVEL 1: PRIVILEGIOS DE AULA & RECREO (40 - 70 🪙 • 1-2 días de esfuerzo)
  { id: 'r1', title: 'Salida Anticipada al Receso (3 min)', description: 'Sal 3 minutos antes al descanso para evitar filas en la cafetería escolar.', costCredits: 45, icon: '🥪', stock: 25, category: 'AULA', tier: 1 },
  { id: 'r2', title: 'Elegir Puesto de Clase por 1 Día', description: 'Escoge tu lugar preferido en el aula de clases durante toda la jornada escolar.', costCredits: 50, icon: '🪑', stock: 15, category: 'AULA', tier: 1 },
  { id: 'r3', title: 'Turno Preferencial en Cancha Escolar', description: 'Primer turno de elección deportiva en la cancha durante el descanso.', costCredits: 55, icon: '⚽', stock: 20, category: 'RECREO', tier: 1 },
  { id: 'r4', title: 'Música de Fondo en Taller (DJ de Aula)', description: 'Selecciona la lista de música de estudio instrumental durante el trabajo en clase.', costCredits: 65, icon: '🎵', stock: 12, category: 'AULA', tier: 1 },
  { id: 'r5', title: 'Préstamo de Juego de Mesa en Descanso', description: 'Acceso prioritario a ajedrez, jenga o dominó escolar durante el recreo.', costCredits: 40, icon: '🎲', stock: 15, category: 'RECREO', tier: 1 },
  { id: 'r6', title: 'Uso de Marcadores Especiales de Tablero', description: 'Participa escribiendo o dibujando soluciones en el tablero con marcadores de color.', costCredits: 45, icon: '🖍️', stock: 20, category: 'AULA', tier: 1 },

  // NIVEL 2: BENEFICIOS ACADÉMICOS FORMATIVOS (85 - 160 🪙 • 4-7 días de constancia)
  { id: 'r7', title: 'Pase de Prórroga en Tarea (+24h)', description: 'Extiende 24h el plazo de entrega de 1 tarea escolar sin penalización sobre la nota.', costCredits: 110, icon: '⏳', stock: 30, category: 'ACADEMICO', tier: 2 },
  { id: 'r8', title: 'Profesor Asistente / Monitor por 1 Día', description: 'Colabora con el docente repartiendo guías y coordinando dinámicas pedagógicas.', costCredits: 130, icon: '🌟', stock: 8, category: 'ACADEMICO', tier: 2 },
  { id: 'r9', title: 'Descarte de Peor Nota en Taller Corto', description: 'Elimina la calificación más baja de un quiz corto o taller formativo del periodo.', costCredits: 150, icon: '📝', stock: 10, category: 'ACADEMICO', tier: 2 },
  { id: 'r10', title: 'Tolerancia de Retardo (Emergencia)', description: '1 llegada tarde justificada oficialmente sin registro de falta disciplinaria.', costCredits: 95, icon: '⏰', stock: 25, category: 'ACADEMICO', tier: 2 },
  { id: 'r11', title: 'Elegir Compañero en Trabajo Grupal', description: 'Garantiza trabajar con tu compañero/a preferido en la próxima actividad grupal.', costCredits: 120, icon: '🤝', stock: 15, category: 'ACADEMICO', tier: 2 },
  { id: 'r12', title: 'Elegir Tema o Dinámica de Clase', description: 'Propón la temática de debate o dinámica lúdica para la siguiente sesión escolar.', costCredits: 85, icon: '💡', stock: 10, category: 'ACADEMICO', tier: 2 },

  // NIVEL 3: PRIVILEGIOS DESTACADOS & CAFETERÍA (200 - 320 🪙 • 1.5 - 2 semanas)
  { id: 'r13', title: 'Punto Extra (+0.5) en Evaluación Formativa', description: 'Suma +0.5 sobre la nota final de una prueba formativa o quiz pedagógico.', costCredits: 240, icon: '⭐', stock: 15, category: 'ACADEMICO', tier: 3 },
  { id: 'r14', title: 'Bono de Merienda en Cafetería Escolar', description: 'Vale canjeable por un refrigerio, sándwich o snack saludable en la cafetería.', costCredits: 260, icon: '🥪', stock: 20, category: 'CAFETERIA', tier: 3 },
  { id: 'r15', title: 'Elegir Puesto Fijo por 1 Semana', description: 'Tu lugar preferido reservado de lunes a viernes en el salón de clases.', costCredits: 220, icon: '👑', stock: 10, category: 'AULA', tier: 3 },
  { id: 'r16', title: 'Multiplicador x2 de Créditos en Tarea', description: 'Duplica los créditos y XP obtenidos en tu siguiente entrega sobresaliente.', costCredits: 200, icon: '⚡', stock: 12, category: 'ACADEMICO', tier: 3 },
  { id: 'r17', title: 'Kit Escolar de Creatividad & Útiles', description: 'Set institucional de bolígrafos de colores, notas adhesivas y libreta escolar.', costCredits: 290, icon: '🎨', stock: 10, category: 'AULA', tier: 3 },

  // NIVEL 4: GRANDES DISTINCIONES INSTITUCIONALES (450 - 600 🪙 • 3-4 semanas de constancia)
  { id: 'r18', title: 'Pase Dorado: Exención de 1 Tarea Menor', description: 'Exoneración oficial de 1 tarea formativa menor con nota máxima registrada (5.0).', costCredits: 520, icon: '🎟️', stock: 6, category: 'DISTINCION', tier: 4 },
  { id: 'r19', title: 'Combo Almuerzo Especial en Cafetería', description: 'Menú especial completo con jugo natural y postre en el comedor escolar.', costCredits: 580, icon: '🍕', stock: 8, category: 'CAFETERIA', tier: 4 },
  { id: 'r20', title: 'Diploma de Honor Elisiano en Bandera', description: 'Certificado oficial de honor entregado en formación escolar por Rectoría.', costCredits: 480, icon: '🏆', stock: 15, category: 'DISTINCION', tier: 4 }
];

let rewardsList = [...OFFICIAL_DEFAULT_REWARDS];

// 3. Service Worker Registration
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js').catch(() => {});
  });
}

// 3.1 Restauración Inmediata de Sesión en Caché (Sin Destello de Login en Web/iPhone)
function tryRestoreCachedSession() {
  try {
    const cached = localStorage.getItem('escolaris_cached_user');
    if (cached) {
      const parsedUser = JSON.parse(cached);
      if (parsedUser && parsedUser.id) {
        currentUser = parsedUser;
        const splash = document.getElementById('app-boot-splash');
        if (splash) splash.style.display = 'none';
        const authView = document.getElementById('auth-view');
        if (authView) {
          authView.classList.add('hidden');
          authView.style.display = 'none';
        }
        const mainApp = document.getElementById('main-app');
        if (mainApp) {
          mainApp.classList.remove('hidden');
          mainApp.style.display = '';
        }
        renderNavigationForRole(currentUser.role);
        renderUserProfile();
        renderRoleSpecificScreens();

        const savedTab = sessionStorage.getItem('escolaris_active_tab') || 
          (currentUser.role === 'TEACHER' ? 'teacher-admin' : (currentUser.role === 'PARENT' ? 'parent-dashboard' : 'feed'));
        switchNav(savedTab);
        return true;
      }
    }
  } catch (e) {
    console.warn("Cached session restore fallback:", e);
  }
  return false;
}

// Ejecutar de inmediato al cargar el script
tryRestoreCachedSession();

// 4. Auth State Listener
auth.onAuthStateChanged(async (user) => {
  const splash = document.getElementById('app-boot-splash');
  const authView = document.getElementById('auth-view');
  const mainApp = document.getElementById('main-app');

  if (user) {
    document.documentElement.classList.remove('auth-restoring');
    if (splash) splash.style.display = 'none';
    if (authView) {
      authView.classList.add('hidden');
      authView.style.display = 'none';
    }
    if (mainApp) {
      mainApp.classList.remove('hidden');
      mainApp.style.display = '';
    }
    setupUserListener(user.uid, user.email, user.displayName, user.photoURL);
    setupDataListeners();
    checkIosPrompt();
    requestNotificationPermission(user.uid);
  } else {
    // Si existe sesión previa en caché, esperar un momento antes de conmutar al login
    // para evitar el destello mientras Firebase Auth lee el token de IndexedDB.
    const hasCachedSession = !!localStorage.getItem('escolaris_cached_user') || !!localStorage.getItem('escolaris_session_uid');
    if (hasCachedSession && !user) {
      setTimeout(() => {
        if (!auth.currentUser) {
          document.documentElement.classList.remove('auth-restoring');
          currentUser = null;
          localStorage.removeItem('escolaris_cached_user');
          localStorage.removeItem('escolaris_session_uid');
          if (splash) splash.style.display = 'none';
          if (authView) {
            authView.classList.remove('hidden');
            authView.style.display = '';
          }
          if (mainApp) {
            mainApp.classList.add('hidden');
            mainApp.style.display = 'none';
          }
        }
      }, 600);
      return;
    }

    document.documentElement.classList.remove('auth-restoring');
    currentUser = null;
    dataListenersAttached = false;
    localStorage.removeItem('escolaris_cached_user');
    localStorage.removeItem('escolaris_session_uid');
    if (splash) splash.style.display = 'none';
    if (authView) {
      authView.classList.remove('hidden');
      authView.style.display = '';
    }
    if (mainApp) {
      mainApp.classList.add('hidden');
      mainApp.style.display = 'none';
    }
  }
});

// Setup FCM Web Push Notifications & Token Sync
async function requestNotificationPermission(userId) {
  if (!('Notification' in window) || !('serviceWorker' in navigator)) {
    console.warn('[FCM] Las notificaciones no son compatibles con este navegador.');
    return null;
  }
  if (!messaging) {
    console.warn('[FCM] Firebase Messaging no está inicializado.');
    return null;
  }
  const targetUid = userId || (currentUser ? currentUser.id : auth.currentUser?.uid);
  if (!targetUid) {
    console.warn('[FCM] No hay usuario activo para vincular el token.');
    return null;
  }

  try {
    const permission = await Notification.requestPermission();
    if (permission !== 'granted') {
      console.log('[FCM] Permiso de notificaciones:', permission);
      return null;
    }

    // Registrar o recuperar el Service Worker específico de FCM
    const registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
    console.log('[FCM] ServiceWorker registrado con alcance:', registration.scope);

    // Obtener token FCM para la Web
    const tokenOptions = {
      serviceWorkerRegistration: registration
    };
    if (window.__VAPID_KEY__) {
      tokenOptions.vapidKey = window.__VAPID_KEY__;
    }

    const currentToken = await messaging.getToken(tokenOptions);

    if (currentToken) {
      console.log('[FCM] Web Token obtenido con éxito:', currentToken);

      // 1. Guardar en subcolección users/{userId}/fcmTokens/{tokenId} (homologado con Android)
      await db.collection('users')
        .doc(targetUid)
        .collection('fcmTokens')
        .doc(currentToken)
        .set({
          token: currentToken,
          platform: 'web',
          userAgent: navigator.userAgent,
          createdAt: firebase.firestore.FieldValue.serverTimestamp(),
          updatedAt: Date.now()
        }, { merge: true });

      // 2. Guardar también en el documento del usuario para consultas directas y arrays
      await db.collection('users').doc(targetUid).update({
        fcmWebToken: currentToken,
        fcmWebUpdatedAt: Date.now(),
        fcmTokens: firebase.firestore.FieldValue.arrayUnion(currentToken)
      }).catch(() => {});

      showToast("🔔 Notificaciones push activadas correctamente");
      return currentToken;
    } else {
      console.warn('[FCM] No se pudo obtener el token. Verifica los permisos o VAPID key en Firebase Console.');
    }
  } catch (err) {
    console.warn('[FCM] Error solicitando permisos o generando token:', err);
  }
  return null;
}

// Alias para compatibilidad hacia atrás
const initWebPushMessaging = requestNotificationPermission;

// Escuchar notificaciones cuando la app está abierta en primer plano
if (messaging) {
  try {
    messaging.onMessage((payload) => {
      console.log('[FCM] Mensaje recibido en primer plano:', payload);
      const title = payload.notification?.title || payload.data?.title || 'Escolaris';
      const body = payload.notification?.body || payload.data?.body || payload.data?.message || 'Nueva notificación escolar';
      showToast(`🔔 ${title}: ${body}`);
    });
  } catch (e) {
    console.warn('[FCM] Error registrando onMessage:', e);
  }
}

// Setup Live User Profile Listener (Unified Mobile & Web Sync with Data Preservation)
function setupUserListener(uid, email, displayName, photoURL) {
  const cleanEmail = (email || '').trim().toLowerCase();
  const isSuperTeacher = cleanEmail === 'moz658@gmail.com';
  const userRef = db.collection('users').doc(uid);

  // Save session info to localStorage for instant recovery
  if (cleanEmail) localStorage.setItem('escolaris_session_email', cleanEmail);
  if (uid) localStorage.setItem('escolaris_session_uid', uid);

  userRef.onSnapshot(async (doc) => {
    let activeDoc = doc;
    let isPointer = false;

    // Si el documento en users/{uid} tiene un puntero canonicalUserId, resolver al canónico
    if (doc.exists && doc.data() && doc.data().canonicalUserId) {
      const canonicalId = doc.data().canonicalUserId;
      try {
        const canonicalDoc = await db.collection('users').doc(canonicalId).get();
        if (canonicalDoc.exists) {
          activeDoc = canonicalDoc;
          isPointer = true;
        }
      } catch (e) {
        console.warn("Error leyendo documento canónico:", e);
      }
    }

    if (activeDoc.exists && (!activeDoc.data().canonicalUserId || isPointer)) {
      // User profile already in Firestore
      const canonicalId = activeDoc.id;
      currentUser = { id: canonicalId, ...activeDoc.data() };

      const updates = {};
      // 1. Safeguard super teacher account
      if (isSuperTeacher) {
        if (currentUser.role !== 'TEACHER') {
          currentUser.role = 'TEACHER';
          updates.role = 'TEACHER';
        }
        if (!currentUser.name || currentUser.name === 'Usuario' || currentUser.name === 'Docente') {
          currentUser.name = 'Manuel Muñoz';
          updates.name = 'Manuel Muñoz';
        }
        if (!currentUser.teacherCode) {
          currentUser.teacherCode = 'DOC-102938';
          updates.teacherCode = 'DOC-102938';
        }
        if (!currentUser.teacherSubject) {
          currentUser.teacherSubject = 'Docente Titular';
          updates.teacherSubject = 'Docente Titular';
        }
        if ((currentUser.credits || 0) < 500) {
          currentUser.credits = 500;
          updates.credits = 500;
        }
      } else {
        // Auto-assign immutable codes if missing for other roles
        if (currentUser.role === 'TEACHER' && !currentUser.teacherCode) {
          updates.teacherCode = 'DOC-' + Math.random().toString(36).substring(2, 8).toUpperCase();
          currentUser.teacherCode = updates.teacherCode;
        }
        if (currentUser.role === 'STUDENT' && !currentUser.studentCode) {
          updates.studentCode = 'ESC-' + Math.random().toString(36).substring(2, 8).toUpperCase();
          currentUser.studentCode = updates.studentCode;
        }
        if (!currentUser.badges || !Array.isArray(currentUser.badges) || currentUser.badges.length === 0) {
          const initBadges = getInitialBadgesForRole(currentUser.role);
          updates.badges = initBadges;
          currentUser.badges = initBadges;
        }
      }

      if (Object.keys(updates).length > 0) {
        activeDoc.ref.set(updates, { merge: true }).catch(() => {});
      }

      closeModal('modal-onboarding');
    } else {
      // Document with this UID not found directly -> search by email in Firestore to preserve prior data
      let existingDoc = null;
      if (cleanEmail) {
        try {
          const snapshot = await db.collection('users').where('email', '==', cleanEmail).get();
          if (!snapshot.empty) {
            existingDoc = snapshot.docs.find(d => d.id !== uid && d.data().roleConfigured) || snapshot.docs[0];
          }
        } catch (e) {
          console.error("Error finding user by email:", e);
        }
      }

      if (existingDoc && existingDoc.exists) {
        // PRESERVAR EL ID CANÓNICO para mantener intactas tareas, notas, asistencias y vínculos familiares
        const canonicalId = existingDoc.id;
        const existingData = existingDoc.data();
        const mergedUser = {
          ...existingData,
          id: canonicalId,
          authUid: uid,
          email: cleanEmail,
          photoUri: photoURL || existingData.photoUri || null,
          roleConfigured: true,
          updatedAt: firebase.firestore.FieldValue.serverTimestamp()
        };

        if (isSuperTeacher) {
          mergedUser.role = 'TEACHER';
          if (!mergedUser.name || mergedUser.name === 'Usuario' || mergedUser.name === 'Docente') {
            mergedUser.name = 'Manuel Muñoz';
          }
          if (!mergedUser.teacherCode) {
            mergedUser.teacherCode = 'DOC-102938';
          }
          if (!mergedUser.teacherSubject) {
            mergedUser.teacherSubject = 'Docente Titular';
          }
          if ((mergedUser.credits || 0) < 500) {
            mergedUser.credits = 500;
          }
        }

        // Guardar cambios en el documento canónico
        await existingDoc.ref.set(mergedUser, { merge: true });

        // Si el UID de Auth es distinto del id canónico, guardar un puntero en users/{uid}
        if (canonicalId !== uid) {
          await userRef.set({
            canonicalUserId: canonicalId,
            authUid: uid,
            email: cleanEmail,
            updatedAt: firebase.firestore.FieldValue.serverTimestamp()
          }, { merge: true });
        }

        currentUser = mergedUser;
      } else {
        // Brand new account with no previous data on any platform
        const defaultRole = isSuperTeacher ? 'TEACHER' : 'STUDENT';
        const defaultName = isSuperTeacher ? 'Manuel Muñoz' : (displayName || (cleanEmail ? cleanEmail.split('@')[0] : 'Usuario'));
        const defaultUser = {
          name: defaultName,
          email: cleanEmail,
          role: defaultRole,
          gradeSection: defaultRole === 'TEACHER' ? 'Docente Titular' : '10° Grado',
          teacherSubject: defaultRole === 'TEACHER' ? 'Docente Titular' : '',
          teacherCode: defaultRole === 'TEACHER' ? 'DOC-102938' : '',
          linkedTeacherCode: null,
          linkedStudentId: '',
          bio: defaultRole === 'TEACHER' ? 'Docente Titular en Escolaris 👨‍🏫' : 'Miembro de la comunidad Escolaris 🚀',
          credits: defaultRole === 'TEACHER' ? 500 : 100,
          xp: defaultRole === 'TEACHER' ? 200 : 50,
          streakDays: 1,
          studentCode: defaultRole === 'STUDENT' ? ('ESC-' + Math.random().toString(36).substring(2, 8).toUpperCase()) : '',
          avatarEmoji: defaultRole === 'TEACHER' ? '👨‍🏫' : '🎓',
          avatarColorHex: defaultRole === 'TEACHER' ? 0xFF1D4ED8 : 0xFF2563EB,
          photoUri: photoURL || null,
          roleConfigured: true,
          badges: getInitialBadgesForRole(defaultRole),
          createdAt: firebase.firestore.FieldValue.serverTimestamp()
        };
        await userRef.set(defaultUser);
        currentUser = { id: uid, ...defaultUser };
      }
    }

    if (currentUser) {
      try {
        localStorage.setItem('escolaris_cached_user', JSON.stringify(currentUser));
      } catch (e) {}
    }

    renderUserProfile();
    renderNavigationForRole();
    renderRoleSpecificScreens();
    renderFeed();
    renderTasks();
    renderSchedule();
    renderRewards();
    renderLeaderboard();
    renderProfileBadges();
    renderPasses();
    
    // Set initial active tab based on user role if on default feed
    if (!currentTab || currentTab === 'feed') {
      const initialTab = currentUser.role === 'TEACHER' ? 'teacher-admin' : (currentUser.role === 'PARENT' ? 'parent-dashboard' : 'feed');
      switchNav(initialTab);
    } else {
      switchNav(currentTab);
    }
  });
}

let dataListenersAttached = false;

// Setup Live Firestore Listeners
function setupDataListeners() {
  if (dataListenersAttached) {
    console.log("[Firestore] Data listeners ya fueron inicializados.");
    return;
  }
  dataListenersAttached = true;
  console.log("[Firestore] Inicializando data listeners en tiempo real...");

  db.collection('users').onSnapshot((snapshot) => {
    allUsers = [];
    snapshot.forEach(doc => {
      allUsers.push({ id: doc.id, ...doc.data() });
    });
    leaderboardUsers = [...allUsers].sort((a, b) => ((b.xp || 0) - (a.xp || 0)) || ((b.credits || 0) - (a.credits || 0)));
    console.log(`[Firestore] Usuarios cargados: ${allUsers.length}`);

    if (currentUser) {
      const cleanEmail = (currentUser.email || '').trim().toLowerCase();
      const updatedMe = allUsers.find(u => u.id === currentUser.id || (cleanEmail && (u.email || '').trim().toLowerCase() === cleanEmail));
      if (updatedMe) {
        const previousRole = currentUser.role;
        currentUser = { ...currentUser, ...updatedMe, id: currentUser.id || updatedMe.id };
        try {
          localStorage.setItem('escolaris_cached_user', JSON.stringify(currentUser));
        } catch (e) {}
        renderUserProfile();
        if (previousRole !== currentUser.role) {
          console.log(`[Role Switch] El rol del usuario cambió de ${previousRole} a ${currentUser.role}. Reconfigurando vistas...`);
          renderNavigationForRole();
          if (currentUser.role === 'PARENT') {
            renderParentDashboard();
            switchNav('parent-dashboard');
          } else if (currentUser.role === 'TEACHER') {
            switchNav('teacher-admin');
          } else {
            switchNav('feed');
          }
        }
      }
    }

    renderLeaderboard();
    renderAdminUsers();
    renderRoleSpecificScreens();
    populateStudentSelects();
    if (allUsers.length === 0) {
      seedInstitutionalUsers();
    }
    renderFeed(); // Actualiza preview de ranking en el lateral del muro
  }, (err) => {
    console.error("Error detallado en Users listener:", err);
  });

  db.collection('tasks').onSnapshot((snapshot) => {
    userTasks = [];
    snapshot.forEach(doc => {
      userTasks.push({ id: doc.id, ...doc.data() });
    });
    renderTasks();
    renderRoleSpecificScreens();
    if (currentTab === 'calendar') renderCalendar();
  }, (err) => console.error("Error detallado en Tasks:", err));

  db.collection('tardies').onSnapshot((snapshot) => {
    allTardies = [];
    snapshot.forEach(doc => {
      allTardies.push({ id: doc.id, ...doc.data() });
    });
    renderAdminTardies();
    renderParentTardies();
  }, (err) => console.error("Error detallado en Tardies:", err));

  db.collection('badges').onSnapshot((snapshot) => {
    allBadges = [];
    snapshot.forEach(doc => {
      allBadges.push({ id: doc.id, ...doc.data() });
    });
    renderProfileBadges();
    renderParentFamilyBadges();
    renderAdminBadges();
  }, (err) => console.error("Error detallado en Badges:", err));

  // Escucha del Muro Escolar: sin forzar orderBy en Firestore para que funcione
  // inmediatamente incluso si faltan campos/índices en algún documento.
  db.collection('feed_posts').onSnapshot((snapshot) => {
    feedPosts = [];
    snapshot.forEach(doc => {
      feedPosts.push({ id: doc.id, ...doc.data() });
    });
    // Ordenar de forma descendente en memoria por fecha
    feedPosts.sort((a, b) => {
      const tsA = a.timestampMillis || a.timestamp || 0;
      const tsB = b.timestampMillis || b.timestamp || 0;
      return tsB - tsA;
    });
    console.log(`[Firestore] Publicaciones recibidas del muro: ${feedPosts.length}`);
    renderFeed();
  }, (err) => {
    console.error("Error detallado en Feed:", err);
    const container = document.getElementById('feed-posts-list');
    if (container) {
      container.innerHTML = `<p style="font-size:12px; color:var(--danger); text-align:center; padding:20px;">Error al cargar publicaciones: ${err.message}</p>`;
    }
  });

  db.collection('redemptions').onSnapshot((snapshot) => {
    userRedemptions = [];
    snapshot.forEach(doc => {
      userRedemptions.push({ id: doc.id, ...doc.data() });
    });
    renderPasses();
    renderAdminPasses();
  }, (err) => console.log("Redemptions listener error:", err));

  db.collection('penalties').orderBy('timestamp', 'desc').onSnapshot((snapshot) => {
    allPenalties = [];
    snapshot.forEach(doc => {
      allPenalties.push({ id: doc.id, ...doc.data() });
    });
    renderAdminPenalties();
    renderParentPenalties();
  }, (err) => console.log("Penalties listener error:", err));

  db.collection('rewards').onSnapshot((snapshot) => {
    rewardsList = [];
    snapshot.forEach(doc => {
      rewardsList.push({ id: doc.id, ...doc.data() });
    });
    if (rewardsList.length < 20) {
      seedInitialRewards();
    }
    renderRewards();
  }, (err) => console.log("Rewards listener error:", err));

  db.collection('parent_obligations').onSnapshot((snapshot) => {
    allParentObligations = [];
    snapshot.forEach(doc => {
      allParentObligations.push({ id: doc.id, ...doc.data() });
    });
    if (allParentObligations.length === 0) {
      seedInitialParentObligations();
    }
    renderParentDashboard();
    renderAdminObligations();
    checkPensionReminder();
  }, (err) => console.log("Parent Obligations listener error:", err));
}

async function seedInitialRewards() {
  try {
    const existingTitles = new Set((rewardsList || []).map(r => (r.title || '').toLowerCase().trim()));
    const missingRewards = OFFICIAL_DEFAULT_REWARDS.filter(r => !existingTitles.has(r.title.toLowerCase().trim()));
    for (const rew of missingRewards) {
      await db.collection('rewards').add({
        title: rew.title,
        description: rew.description,
        costCredits: rew.costCredits,
        stockAvailable: rew.stock || 15,
        icon: rew.icon || '🎁',
        category: rew.category || 'ACADEMICO',
        teacherName: 'Manuel Muñoz',
        createdAt: firebase.firestore.FieldValue.serverTimestamp()
      });
    }
  } catch (e) {
    console.error("Error seeding initial rewards:", e);
  }
}

async function seedInitialParentObligations() {
  try {
    const initObligations = [
      {
        title: "Firma de Circular Informativa No. 04",
        description: "Revisión y firma digital de la circular de convivencia escolar institucional.",
        category: "DOCUMENTATION",
        month: "Octubre",
        dueDayOfMonth: 10,
        dueDateMillis: Date.now() + (10 * 24 * 3600 * 1000),
        isCompleted: false,
        rewardBadgeKey: "PARENT_CIRCULAR_OCTUBRE",
        rewardBadgeTitle: "Acudiente Informado & Diligente",
        rewardBadgeEmoji: "📄",
        rewardCredits: 60,
        rewardXp: 100,
        whatsappMessage: "¡Hola estimado acudiente! 📄 Recuerde firmar la circular escolar No. 04 antes del 10 de octubre para condecorar a su hijo/a con +60 créditos Escolaris.",
        createdByTeacher: "Coordinación de Convivencia",
        createdAt: firebase.firestore.FieldValue.serverTimestamp()
      },
      {
        title: "Asamblea General de Padres de Familia",
        description: "Asistencia presencial o virtual a la reunión trimestral de seguimiento formativo.",
        category: "EVENT",
        month: "Octubre",
        dueDayOfMonth: 15,
        dueDateMillis: Date.now() + (15 * 24 * 3600 * 1000),
        isCompleted: false,
        rewardBadgeKey: "PARENT_MEETING_1",
        rewardBadgeTitle: "Compromiso Familiar en Asamblea",
        rewardBadgeEmoji: "🏛️",
        rewardCredits: 90,
        rewardXp: 140,
        whatsappMessage: "¡Hola familia Escolaris! 🏛️ Los esperamos con entusiasmo en la Asamblea General este mes. ¡Tu asistencia condecora a tu hijo/a con +90 créditos!",
        createdByTeacher: "Rectoría",
        createdAt: firebase.firestore.FieldValue.serverTimestamp()
      }
    ];

    for (const obl of initObligations) {
      await db.collection('parent_obligations').add(obl);
    }
  } catch (e) {
    console.warn("Seed parent obligations error:", e);
  }
}

// 4.3 Institutional Roster Pre-seeding (7th Grade Students & Parents from Institutional Form)
const OFFICIAL_INSTITUTIONAL_ROSTER = [
  // 1. Juan Andrés Narváez Calambas & Acudiente
  {
    id: "std_1059247264",
    name: "Juan Andrés Narváez Calambas",
    email: "juan.narvaez1059247264@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-247264",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF2563EB,
    avatarEmoji: "🎓",
    avatarInitials: "JN",
    bio: "Estudiante 7° Grado | EPS: Sanitas | RH: A+ | TI: 1059247264 | F.Nac: 15/07/2015",
    phoneNumber: "3019384187",
    roleConfigured: true
  },
  {
    id: "par_1059247264_1",
    name: "Cristina Isabel Calambas Erazo",
    email: "cristinacalambas96@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Juan Andrés Narváez",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "CC",
    linkedStudentId: "std_1059247264",
    bio: "Acudiente de Juan Andrés Narváez Calambas | Tel: 3019384187 | Calle 5 #27-24",
    phoneNumber: "3019384187",
    roleConfigured: true
  },
  // 2. Paula Andrea Urrea Sandoval & Acudientes
  {
    id: "std_1058937460",
    name: "Paula Andrea Urrea Sandoval",
    email: "paula.urrea1058937460@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-937460",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "PU",
    bio: "Estudiante 7° Grado | EPS: Sanitas | RH: A+ | TI: 1058937460 | F.Nac: 06/11/2014",
    phoneNumber: "3206374657",
    roleConfigured: true
  },
  {
    id: "par_1058937460_1",
    name: "Sandra Milena Sandoval",
    email: "smsandoval7@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Paula Andrea Urrea",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "SS",
    linkedStudentId: "std_1058937460",
    bio: "Acudiente de Paula Andrea Urrea Sandoval | Tel: 3206374657 | Tv. 9 Nte. #56N-78 Condominio Monserrat",
    phoneNumber: "3206374657",
    roleConfigured: true
  },
  {
    id: "par_1058937460_2",
    name: "Juan Urrea Murillo",
    email: "juan.urrea.murillo@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Paula Andrea Urrea",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "JU",
    linkedStudentId: "std_1058937460",
    bio: "Acudiente de Paula Andrea Urrea Sandoval | Tel: 3206374657 | Tv. 9 Nte. #56N-78 Condominio Monserrat",
    phoneNumber: "3206374657",
    roleConfigured: true
  },
  // 3. Angelly Daniela González Mosquera & Acudiente
  {
    id: "std_1058551766",
    name: "Angelly Daniela González Mosquera",
    email: "angelly.gonzalez1058551766@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-551766",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "AG",
    bio: "Estudiante 7° Grado | EPS: Fomag | RH: O+ | TI: 1058551766 | F.Nac: 03/03/2014",
    phoneNumber: "3165799079",
    roleConfigured: true
  },
  {
    id: "par_1058551766_1",
    name: "Yency Mosquera Lopez",
    email: "yency.mosquera1058551766@escolaris.edu.co",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Angelly Daniela González",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "YM",
    linkedStudentId: "std_1058551766",
    bio: "Acudiente de Angelly Daniela González Mosquera | Tel: 3165799079 | Santana Cajete",
    phoneNumber: "3165799079",
    roleConfigured: true
  },
  // 4. Anny Sofía Astaiza López & Acudiente
  {
    id: "std_1166465051",
    name: "Anny Sofía Astaiza López",
    email: "anny.astaiza1166465051@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-465051",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "AA",
    bio: "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1166465051 | F.Nac: 26/10/2013",
    phoneNumber: "3103618032",
    roleConfigured: true
  },
  {
    id: "par_1166465051_1",
    name: "Nohemi Lopez Tobar",
    email: "nelcynohemilopez2705@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Anny Sofía Astaiza",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "NL",
    linkedStudentId: "std_1166465051",
    bio: "Acudiente de Anny Sofía Astaiza López | Tel: 3103618032 | Torres de San Eduardo Torre 2a apto 403",
    phoneNumber: "3103618032",
    roleConfigured: true
  },
  // 5. Ana Sofía Bonilla Camacho & Acudiente
  {
    id: "std_1061791248",
    name: "Ana Sofía Bonilla Camacho",
    email: "ana.bonilla1061791248@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-791248",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "AB",
    bio: "Estudiante 7° Grado | EPS: Nueva EPS | RH: B+ | TI: 1061791248 | F.Nac: 10/06/2014",
    phoneNumber: "3137636281",
    roleConfigured: true
  },
  {
    id: "par_1061791248_1",
    name: "Nelson Eduardo Bonilla González",
    email: "nelson13bonilla@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Ana Sofía Bonilla",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarInitials: "NB",
    linkedStudentId: "std_1061791248",
    bio: "Acudiente de Ana Sofía Bonilla Camacho | Tel: 3137636281 - 3135313054 | Conjunto arrayanes T1 Apto 301 / Calle 3A #13-58",
    phoneNumber: "3137636281",
    roleConfigured: true
  },
  // 6. Mariangel Castillo Pino & Acudiente
  {
    id: "std_1058974668",
    name: "Mariangel Castillo Pino",
    email: "castillopinomariangel@gmail.com",
    role: "STUDENT",
    studentCode: "ESC-897468",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "MC",
    bio: "Estudiante 7° Grado | EPS: Nueva EPS | RH: A+ | TI: 1058974668 | F.Nac: 13/12/2013",
    phoneNumber: "3127764316",
    roleConfigured: true
  },
  {
    id: "par_1058974668_1",
    name: "Liseth Natalia Pino Perez",
    email: "liseth.pino1058974668@escolaris.edu.co",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Mariangel Castillo",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "LP",
    linkedStudentId: "std_1058974668",
    bio: "Acudiente de Mariangel Castillo Pino | Tel: 3127764316 | Cra 12 # 7A-29 B/Valencia",
    phoneNumber: "3127764316",
    roleConfigured: true
  },
  // 7. María Isabella Samboní Orozco & Acudiente
  {
    id: "std_1058937457",
    name: "María Isabella Samboní Orozco",
    email: "maria.samboni1058937457@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-937457",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "MS",
    bio: "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1058937457 | F.Nac: 24/10/2014",
    phoneNumber: "3136659212",
    roleConfigured: true
  },
  {
    id: "par_1058937457_1",
    name: "Erika Yoslany Orozco Mampotes",
    email: "eyorozcom@ut.edu.co",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de María Isabella Samboní",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "EO",
    linkedStudentId: "std_1058937457",
    bio: "Acudiente de María Isabella Samboní Orozco | Tel: 3136659212 - 3164252273 | Cra 13 b # 12 a 39",
    phoneNumber: "3136659212",
    roleConfigured: true
  },
  // 8. Megan Luciana Olaya Caicedo & Acudiente
  {
    id: "std_1058552648",
    name: "Megan Luciana Olaya Caicedo",
    email: "megan.olaya1058552648@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-552648",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "MO",
    bio: "Estudiante 7° Grado | EPS: S.O.S | RH: O+ | TI: 1058552648 | F.Nac: 16/12/2015",
    phoneNumber: "3102841302",
    roleConfigured: true
  },
  {
    id: "par_1058552648_1",
    name: "Claudia Caicedo",
    email: "claudiacaicedo22@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Megan Luciana Olaya",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "CC",
    linkedStudentId: "std_1058552648",
    bio: "Acudiente de Megan Luciana Olaya Caicedo | Tel: 3102841302 | Calle 8b # 21a 32",
    phoneNumber: "3102841302",
    roleConfigured: true
  },
  // 9. David Santiago Ruiz Bolaños & Acudiente
  {
    id: "std_1061796113",
    name: "David Santiago Ruiz Bolaños",
    email: "david.ruiz1061796113@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-796113",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF2563EB,
    avatarEmoji: "🎓",
    avatarInitials: "DR",
    bio: "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1061796113 | F.Nac: 30/11/2014",
    phoneNumber: "3138618318",
    roleConfigured: true
  },
  {
    id: "par_1061796113_1",
    name: "Lisseth Bolaños",
    email: "lissbolanos93@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de David Santiago Ruiz",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "LB",
    linkedStudentId: "std_1061796113",
    bio: "Acudiente de David Santiago Ruiz Bolaños | Tel: 3138618318 | Calle 2c # 58-02",
    phoneNumber: "3138618318",
    roleConfigured: true
  },
  // 10. María Paula Burbano Chate & Acudiente
  {
    id: "std_1058552020",
    name: "María Paula Burbano Chate",
    email: "maria.burbano1058552020@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-552020",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "MB",
    bio: "Estudiante 7° Grado | EPS: Sanitas | RH: A+ | TI: 1058552020 | F.Nac: 06/08/2014",
    phoneNumber: "3113217331",
    roleConfigured: true
  },
  {
    id: "par_1058552020_1",
    name: "María Chate",
    email: "malychate69@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de María Paula Burbano",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "MC",
    linkedStudentId: "std_1058552020",
    bio: "Acudiente de María Paula Burbano Chate | Tel: 3113217331 | Calle 15 # 17-97",
    phoneNumber: "3113217331",
    roleConfigured: true
  },
  // 11. Ihara Daniella Riascos Tróchez & Acudiente
  {
    id: "std_1058552265",
    name: "Ihara Daniella Riascos Tróchez",
    email: "ihara.riascos1058552265@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-552265",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "IR",
    bio: "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1058552265 | F.Nac: 17/04/2015",
    phoneNumber: "3216031855",
    roleConfigured: true
  },
  {
    id: "par_1058552265_1",
    name: "Alba Miryam Tróchez Tombé",
    email: "miryamtrochez@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Ihara Daniella Riascos",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "AT",
    linkedStudentId: "std_1058552265",
    bio: "Acudiente de Ihara Daniella Riascos Tróchez | Tel: 3216031855 | Cra 7ma # 16-20 1ro de Mayo",
    phoneNumber: "3216031855",
    roleConfigured: true
  },
  // 12. Emily Daniela Caicedo Navia & Acudiente
  {
    id: "std_1061791400",
    name: "Emily Daniela Caicedo Navia",
    email: "emily.caicedo1061791400@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-791400",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "EC",
    bio: "Estudiante 7° Grado | EPS: Sanitas | RH: B+ | TI: 1061791400 | F.Nac: 21/06/2014",
    phoneNumber: "3217510339",
    roleConfigured: true
  },
  // 13. Valeria Orozco Gutiérrez & Acudiente
  {
    id: "std_1166464830",
    name: "Valeria Orozco Gutiérrez",
    email: "valeria.orozco1166464830@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-464830",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "VO",
    bio: "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1166464830 | F.Nac: 21/07/2013",
    phoneNumber: "3233835048",
    roleConfigured: true
  },
  {
    id: "par_1166464830_1",
    name: "Sandra Magali Gutiérrez",
    email: "samidrobo@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Valeria Orozco",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "SG",
    linkedStudentId: "std_1166464830",
    bio: "Acudiente de Valeria Orozco Gutiérrez | Tel: 3233835048 - 3104356309 | Vereda Alto Puelenje",
    phoneNumber: "3233835048",
    roleConfigured: true
  },
  // 14. Danna Salomé Bolaños Ordóñez & Acudiente
  {
    id: "std_129621021",
    name: "Danna Salomé Bolaños Ordóñez",
    email: "danna.bolanos129621021@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-621021",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFFE11D74,
    avatarEmoji: "🎓",
    avatarInitials: "DB",
    bio: "Estudiante 7° Grado | EPS: Sanitas | RH: A+ | TI: 129621021 | F.Nac: 23/09/2013",
    phoneNumber: "3103571787",
    roleConfigured: true
  },
  {
    id: "par_129621021_1",
    name: "Lina Ordoñez",
    email: "marceordo9521@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Danna Salomé Bolaños",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "LO",
    linkedStudentId: "std_129621021",
    bio: "Acudiente de Danna Salomé Bolaños Ordóñez | Tel: 3103571787 | Altos de Santa Inés Torre C Apto 803",
    phoneNumber: "3103571787",
    roleConfigured: true
  },
  // 15. Samuel Echavarria Pizo & Acudiente
  {
    id: "std_1059246862",
    name: "Samuel Echavarria Pizo",
    email: "samuel.echavarria1059246862@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-246862",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF2563EB,
    avatarEmoji: "🎓",
    avatarInitials: "SE",
    bio: "Estudiante 7° Grado | EPS: Emssanar | RH: A+ | TI: 1059246862 | F.Nac: 22/06/2014",
    phoneNumber: "3158908125",
    roleConfigured: true
  },
  {
    id: "45ffface-2f81-4fbd-978d-f7ba59663232",
    name: "María Mercedes Echavarria Pizo",
    email: "mekisa192227@gmail.com",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Samuel Echavarria",
    credits: 300,
    parentIncentiveCredits: 200,
    xp: 350,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "ME",
    linkedStudentId: "std_1059246862",
    bio: "Acudiente de Samuel Echavarria Pizo | Tel: 3235421090 | Calle 7A # 12-36",
    phoneNumber: "3235421090",
    roleConfigured: true
  },
  // 16. Andres Felipe Vaca Bahos & Acudiente
  {
    id: "std_1166465828",
    name: "Andres Felipe Vaca Bahos",
    email: "andres.vaca1166465828@escolaris.edu.co",
    role: "STUDENT",
    studentCode: "ESC-465828",
    gradeSection: "7° Grado",
    credits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF2563EB,
    avatarInitials: "AV",
    bio: "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1166465828 | F.Nac: 20/01/2015",
    phoneNumber: "3134068425",
    roleConfigured: true
  },
  {
    id: "par_1166465828_1",
    name: "Sandra Ximena Bahos Quinayás",
    email: "sandra.bahos1166465828@escolaris.edu.co",
    role: "PARENT",
    studentCode: "",
    gradeSection: "Padre/Tutor de Andres Felipe Vaca",
    credits: 100,
    parentIncentiveCredits: 100,
    xp: 50,
    level: 1,
    streakDays: 1,
    avatarColorHex: 0xFF059669,
    avatarEmoji: "👨‍👩‍👧",
    avatarInitials: "SB",
    linkedStudentId: "std_1166465828",
    bio: "Acudiente de Andres Felipe Vaca Bahos | Tel: 3134068425 | Cra 15 # 8N-188",
    phoneNumber: "3134068425",
    roleConfigured: true
  }
];

async function seedInstitutionalUsers() {
  try {
    if (!db) return;
    // Cloud Firestore es la Fuente Única de Verdad: NUNCA sembrar si ya existen usuarios
    if (allUsers && allUsers.length > 0) return;
    
    for (const u of OFFICIAL_INSTITUTIONAL_ROSTER) {
      await db.collection('users').doc(u.id).set({
        ...u,
        createdAt: firebase.firestore.FieldValue.serverTimestamp()
      }, { merge: true });
    }
  } catch (e) {
    console.warn("Seed institutional users error:", e);
  }
}

// 5. Role & Navigation Manager (Dual Mode: Mobile Bottom Nav + Desktop Left Sidebar)
function renderNavigationForRole() {
  if (!currentUser) return;
  const role = currentUser.role || 'STUDENT';

  // 5.1 Mobile Bottom Navigation
  const navContainer = document.getElementById('dynamic-bottom-nav');
  if (navContainer) {
    let mobileNavHtml = '';
    if (role === 'TEACHER') {
      mobileNavHtml = `
        <button class="nav-item ${currentTab === 'feed' ? 'active' : ''}" onclick="switchNav('feed')" id="nav-feed">
          <span class="nav-icon">📢</span>
          <span>Muro</span>
        </button>
        <button class="nav-item ${currentTab === 'teacher-admin' ? 'active' : ''}" onclick="switchNav('teacher-admin')" id="nav-teacher-admin">
          <span class="nav-icon">🛠️</span>
          <span>Admin</span>
        </button>
        <button class="nav-item center-profile ${currentTab === 'profile' ? 'active' : ''}" onclick="switchNav('profile')" id="nav-profile" title="Mi Perfil">
          <span class="nav-icon">${currentUser.avatarEmoji || '👨‍🏫'}</span>
        </button>
        <button class="nav-item ${currentTab === 'calendar' ? 'active' : ''}" onclick="switchNav('calendar')" id="nav-calendar">
          <span class="nav-icon">📅</span>
          <span>Calendario</span>
        </button>
        <button class="nav-item ${currentTab === 'tasks' ? 'active' : ''}" onclick="switchNav('tasks')" id="nav-tasks">
          <span class="nav-icon">📝</span>
          <span>Tareas</span>
        </button>
      `;
    } else if (role === 'PARENT') {
      const parentPendingCount = allParentObligations.filter(o => !o.isCompleted).length;
      const pendingBadgeHtml = parentPendingCount > 0 
        ? `<span class="badge" style="background:#EF4444; color:#ffffff; font-size:9px; font-weight:900; padding:1px 5px; border-radius:8px; margin-left:4px;">${parentPendingCount}</span>` 
        : '';

      mobileNavHtml = `
        <button class="nav-item ${currentTab === 'parent-dashboard' ? 'active' : ''}" onclick="switchNav('parent-dashboard')" id="nav-parent-dashboard" style="position:relative;">
          <span class="nav-icon">👨‍👩‍👧</span>
          <span>Padres ${pendingBadgeHtml}</span>
        </button>
        <button class="nav-item ${currentTab === 'feed' ? 'active' : ''}" onclick="switchNav('feed')" id="nav-feed">
          <span class="nav-icon">📢</span>
          <span>Avisos</span>
        </button>
        <button class="nav-item center-profile ${currentTab === 'profile' ? 'active' : ''}" onclick="switchNav('profile')" id="nav-profile" title="Mi Perfil">
          <span class="nav-icon">${currentUser.avatarEmoji || '👨‍👩‍👧'}</span>
        </button>
        <button class="nav-item ${currentTab === 'calendar' ? 'active' : ''}" onclick="switchNav('calendar')" id="nav-calendar">
          <span class="nav-icon">📅</span>
          <span>Calendario</span>
        </button>
        <button class="nav-item ${currentTab === 'tasks' ? 'active' : ''}" onclick="switchNav('tasks')" id="nav-tasks">
          <span class="nav-icon">📝</span>
          <span>Tareas</span>
        </button>
      `;
    } else {
      // STUDENT
      mobileNavHtml = `
        <button class="nav-item ${currentTab === 'feed' ? 'active' : ''}" onclick="switchNav('feed')" id="nav-feed">
          <span class="nav-icon">📢</span>
          <span>Muro</span>
        </button>
        <button class="nav-item ${currentTab === 'schedule' ? 'active' : ''}" onclick="switchNav('schedule')" id="nav-schedule">
          <span class="nav-icon">⏰</span>
          <span>Horario</span>
        </button>
        <button class="nav-item ${currentTab === 'journal' ? 'active' : ''}" onclick="switchNav('journal')" id="nav-journal">
          <span class="nav-icon">📖</span>
          <span>Bitácora</span>
        </button>
        <button class="nav-item ${currentTab === 'rewards' ? 'active' : ''}" onclick="switchNav('rewards')" id="nav-rewards">
          <span class="nav-icon">🎁</span>
          <span>Tienda</span>
        </button>
        <button class="nav-item center-profile ${currentTab === 'profile' ? 'active' : ''}" onclick="switchNav('profile')" id="nav-profile" title="Mi Perfil">
          <span class="nav-icon">${currentUser.avatarEmoji || '🎓'}</span>
        </button>
      `;
    }
    navContainer.innerHTML = mobileNavHtml;
  }

  // Render Mobile Student Quick Hub (Pills)
  renderMobileQuickHub();

  // 5.2 Desktop Left Sidebar Navigation
  const desktopSidebarNav = document.getElementById('desktop-sidebar-nav');
  if (desktopSidebarNav) {
    let sidebarNavHtml = '';
    if (role === 'TEACHER') {
      sidebarNavHtml = `
        <button class="sidebar-nav-item ${currentTab === 'feed' ? 'active' : ''}" onclick="switchNav('feed')">
          <span class="sidebar-nav-icon">📢</span>
          <span>Muro & Avisos</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'teacher-admin' ? 'active' : ''}" onclick="switchNav('teacher-admin')">
          <span class="sidebar-nav-icon">🛠️</span>
          <span>Gestión Docente</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'calendar' ? 'active' : ''}" onclick="switchNav('calendar')">
          <span class="sidebar-nav-icon">📅</span>
          <span>Calendario</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'schedule' ? 'active' : ''}" onclick="switchNav('schedule')">
          <span class="sidebar-nav-icon">⏰</span>
          <span>Horario de Clases</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'tasks' ? 'active' : ''}" onclick="switchNav('tasks')">
          <span class="sidebar-nav-icon">📝</span>
          <span>Tareas & Evaluaciones</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'journal' ? 'active' : ''}" onclick="switchNav('journal')">
          <span class="sidebar-nav-icon">📖</span>
          <span>Bitácoras Estudiantes</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'rewards' ? 'active' : ''}" onclick="switchNav('rewards')">
          <span class="sidebar-nav-icon">🎁</span>
          <span>Tienda de Recompensas</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'gamification' ? 'active' : ''}" onclick="switchNav('gamification')">
          <span class="sidebar-nav-icon">🏆</span>
          <span>Ranking STEAM</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'profile' ? 'active' : ''}" onclick="switchNav('profile')">
          <span class="sidebar-nav-icon">👤</span>
          <span>Mi Perfil & Ajustes</span>
        </button>
      `;
    } else if (role === 'PARENT') {
      const parentPendingCount = allParentObligations.filter(o => !o.isCompleted).length;
      const pendingBadgeHtml = parentPendingCount > 0 
        ? `<span class="badge" style="background:#EF4444; color:#ffffff; font-size:10px; font-weight:900; padding:2px 7px; border-radius:10px; margin-left:auto;">${parentPendingCount} pend.</span>` 
        : '';

      sidebarNavHtml = `
        <button class="sidebar-nav-item ${currentTab === 'parent-dashboard' ? 'active' : ''}" onclick="switchNav('parent-dashboard')">
          <span class="sidebar-nav-icon">👨‍👩‍👧</span>
          <span style="flex:1;">Portal del Acudiente</span>
          ${pendingBadgeHtml}
        </button>
        <button class="sidebar-nav-item ${currentTab === 'feed' ? 'active' : ''}" onclick="switchNav('feed')">
          <span class="sidebar-nav-icon">📢</span>
          <span>Muro & Comunicados</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'calendar' ? 'active' : ''}" onclick="switchNav('calendar')">
          <span class="sidebar-nav-icon">📅</span>
          <span>Calendario</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'schedule' ? 'active' : ''}" onclick="switchNav('schedule')">
          <span class="sidebar-nav-icon">⏰</span>
          <span>Horario de Clases</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'journal' ? 'active' : ''}" onclick="switchNav('journal')">
          <span class="sidebar-nav-icon">📖</span>
          <span>Bitácora de mi Hijo/a</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'tasks' ? 'active' : ''}" onclick="switchNav('tasks')">
          <span class="sidebar-nav-icon">📝</span>
          <span>Tareas del Hijo/a</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'profile' ? 'active' : ''}" onclick="switchNav('profile')">
          <span class="sidebar-nav-icon">👤</span>
          <span>Mi Perfil & Ajustes</span>
        </button>
      `;
    } else {
      // STUDENT
      sidebarNavHtml = `
        <button class="sidebar-nav-item ${currentTab === 'feed' ? 'active' : ''}" onclick="switchNav('feed')">
          <span class="sidebar-nav-icon">📢</span>
          <span>Muro de Avisos</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'schedule' ? 'active' : ''}" onclick="switchNav('schedule')">
          <span class="sidebar-nav-icon">⏰</span>
          <span>Horario de Clases</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'rewards' ? 'active' : ''}" onclick="switchNav('rewards')">
          <span class="sidebar-nav-icon">🎁</span>
          <span>Tienda Escolar</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'journal' ? 'active' : ''}" onclick="switchNav('journal')">
          <span class="sidebar-nav-icon">📖</span>
          <span>Mi Bitácora Escolar</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'calendar' ? 'active' : ''}" onclick="switchNav('calendar')">
          <span class="sidebar-nav-icon">📅</span>
          <span>Calendario Escolar</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'tasks' ? 'active' : ''}" onclick="switchNav('tasks')">
          <span class="sidebar-nav-icon">📝</span>
          <span>Tareas & Evaluaciones</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'gamification' ? 'active' : ''}" onclick="switchNav('gamification')">
          <span class="sidebar-nav-icon">🏆</span>
          <span>Tabla de Honor STEAM</span>
        </button>
        <button class="sidebar-nav-item ${currentTab === 'profile' ? 'active' : ''}" onclick="switchNav('profile')">
          <span class="sidebar-nav-icon">👤</span>
          <span>Mi Perfil & Ajustes</span>
        </button>
      `;
    }
    desktopSidebarNav.innerHTML = sidebarNavHtml;
  }
}

function switchNav(tabId, isBack = false) {
  if (!isBack && currentTab !== tabId) {
    navHistory.push(currentTab);
  }
  currentTab = tabId;
  try { sessionStorage.setItem('escolaris_active_tab', tabId); } catch(e) {}

  const allViews = [
    'view-feed',
    'view-teacher-admin',
    'view-parent-dashboard',
    'view-tasks',
    'view-calendar',
    'view-schedule',
    'view-journal',
    'view-rewards',
    'view-gamification',
    'view-profile'
  ];

  allViews.forEach(v => {
    const el = document.getElementById(v);
    if (el) el.classList.add('hidden');
  });

  const targetView = document.getElementById('view-' + tabId);
  if (targetView) targetView.classList.remove('hidden');

  // Update Back Buttons Visibility
  const mobileBackBtn = document.getElementById('mobile-btn-back');
  const desktopBackBtn = document.getElementById('desktop-btn-back');
  const showBack = tabId !== 'feed' && navHistory.length > 0;
  if (mobileBackBtn) {
    if (showBack) mobileBackBtn.classList.remove('hidden');
    else mobileBackBtn.classList.add('hidden');
  }
  if (desktopBackBtn) {
    if (showBack) desktopBackBtn.classList.remove('hidden');
    else desktopBackBtn.classList.add('hidden');
  }

  // Update Desktop Header Titles
  const titleMap = {
    'feed': { title: '📢 Muro de Avisos y Comunidad', subtitle: 'Comunidad académica institucional y apuntes compartidos' },
    'teacher-admin': { title: '🛠️ Panel de Gestión Docente', subtitle: 'Control de llegadas tarde, condecoraciones, sanciones y calificaciones' },
    'parent-dashboard': { title: '👨‍👩‍👧 Portal del Acudiente', subtitle: 'Seguimiento académico, asistencia, sanciones y logros compartidos' },
    'tasks': { title: '📝 Tareas y Evaluaciones', subtitle: 'Control de deberes escolares, proyectos y calendario de exámenes' },
    'calendar': { title: '📅 Calendario Escolar', subtitle: 'Eventos institucionales, fechas importantes, evaluaciones y agenda de actividades' },
    'schedule': { title: '⏰ Horario de Clases', subtitle: 'Distribución semanal de materias y directorio de docentes' },
    'journal': { title: '📖 Mi Bitácora Escolar', subtitle: 'Reflexiones diarias, aprendizajes, emociones y recuerdos de clase' },
    'rewards': { title: '🎁 Tienda de Recompensas', subtitle: 'Canjea tus créditos escolares por pases e incentivos académicos' },
    'gamification': { title: '🏆 Cuadro de Honor & Ranking STEAM', subtitle: 'Reconocimiento a la constancia, excelencia y participación' },
    'profile': { title: '👤 Mi Perfil & Ajustes', subtitle: 'Gestión de cuenta, código de vinculación familiar y preferencias' }
  };

  const info = titleMap[tabId] || { title: 'Escolaris', subtitle: 'Plataforma Escolar' };
  const dTitle = document.getElementById('desktop-view-title');
  const dSubtitle = document.getElementById('desktop-view-subtitle');
  if (dTitle) dTitle.textContent = info.title;
  if (dSubtitle) dSubtitle.textContent = info.subtitle;

  renderNavigationForRole();

  if (tabId === 'feed') renderFeed();
  else if (tabId === 'calendar') renderCalendar();
  else if (tabId === 'journal') renderStudentJournal();
  else if (tabId === 'tasks') renderTasks();
  else if (tabId === 'schedule') {
    if (activeScheduleSubtab === 'classes') renderSchedule();
    else if (activeScheduleSubtab === 'directory') renderTeacherDirectory();
  }
  else if (tabId === 'rewards') renderRewards();
  else if (tabId === 'gamification') renderLeaderboard();
  else if (tabId === 'parent-dashboard') renderParentDashboard();
  else if (tabId === 'teacher-admin') {
    renderAdminTardies();
    renderAdminBadges();
    renderAdminPasses();
    renderAdminPenalties();
    renderAdminUsers();
  } else if (tabId === 'profile') {
    renderUserProfile();
    renderProfileBadges();
    renderPasses();
  }

  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function goBack() {
  if (navHistory.length > 0) {
    const prev = navHistory.pop();
    switchNav(prev || 'feed', true);
  } else {
    switchNav('feed', true);
  }
}

// Browser back button support
window.addEventListener('popstate', () => {
  goBack();
});

// 6. Onboarding Flow & Questionnaire
function openOnboardingModal(allowCancel = true) {
  const modal = document.getElementById('modal-onboarding');
  if (!modal) return;

  if (currentUser) {
    document.getElementById('onboard-name').value = currentUser.name || '';
    if (currentUser.role) selectOnboardRole(currentUser.role);
    if (currentUser.gradeSection && currentUser.role === 'STUDENT') document.getElementById('onboard-grade').value = currentUser.gradeSection;
    if (currentUser.teacherSubject && currentUser.role === 'TEACHER') document.getElementById('onboard-teacher-subject').value = currentUser.teacherSubject;
    if (currentUser.linkedStudentId && currentUser.role === 'PARENT') document.getElementById('onboard-child-code').value = currentUser.linkedStudentId;
    if (currentUser.avatarEmoji) selectOnboardAvatar(currentUser.avatarEmoji);
  }

  modal.classList.add('open');
}

function selectOnboardRole(role) {
  selectedOnboardRole = role;
  ['student', 'teacher', 'parent'].forEach(r => {
    const card = document.getElementById(`onboard-role-${r}`);
    if (card) {
      if (r === role.toLowerCase()) card.classList.add('selected');
      else card.classList.remove('selected');
    }
  });

  document.getElementById('onboard-group-student').classList.toggle('hidden', role !== 'STUDENT');
  document.getElementById('onboard-group-teacher').classList.toggle('hidden', role !== 'TEACHER');
  document.getElementById('onboard-group-parent').classList.toggle('hidden', role !== 'PARENT');

  if (role === 'TEACHER' && selectedOnboardAvatar === '🎓') selectOnboardAvatar('👨‍🏫');
  if (role === 'PARENT' && selectedOnboardAvatar === '🎓') selectOnboardAvatar('👨‍👩‍👧');
}

function selectOnboardAvatar(emoji) {
  selectedOnboardAvatar = emoji;
  const buttons = document.querySelectorAll('.avatar-emoji-btn');
  buttons.forEach(btn => {
    if (btn.textContent.trim() === emoji) btn.classList.add('selected');
    else btn.classList.remove('selected');
  });
}

async function handleSaveOnboarding(e) {
  e.preventDefault();
  const name = document.getElementById('onboard-name').value.trim();
  const grade = document.getElementById('onboard-grade').value;
  const teacherSubject = document.getElementById('onboard-teacher-subject').value.trim();
  const childCode = document.getElementById('onboard-child-code').value.trim().toUpperCase();

  if (!currentUser || !currentUser.id) return;

  try {
    const updateData = {
      name: name || 'Usuario',
      role: selectedOnboardRole,
      gradeSection: selectedOnboardRole === 'STUDENT' ? grade : (selectedOnboardRole === 'TEACHER' ? (teacherSubject || 'Docente General') : 'Familiar'),
      teacherSubject: selectedOnboardRole === 'TEACHER' ? teacherSubject : '',
      linkedStudentId: selectedOnboardRole === 'PARENT' ? childCode : '',
      bio: selectedOnboardRole === 'TEACHER' ? 'Docente Titular en Escolaris 👨‍🏫' : (selectedOnboardRole === 'PARENT' ? 'Acudiente comprometido en Escolaris 👨‍👩‍👧' : 'Estudiante activo en Escolaris 🚀'),
      avatarEmoji: selectedOnboardAvatar,
      credits: currentUser.credits || (selectedOnboardRole === 'TEACHER' ? 500 : (selectedOnboardRole === 'PARENT' ? 200 : 100)),
      xp: currentUser.xp || (selectedOnboardRole === 'STUDENT' ? 50 : 100),
      streakDays: currentUser.streakDays || 1,
      studentCode: currentUser.studentCode || ('ESC-' + Math.random().toString(36).substring(2, 8).toUpperCase()),
      roleConfigured: true,
      updatedAt: firebase.firestore.FieldValue.serverTimestamp()
    };

    await db.collection('users').doc(currentUser.id).set(updateData, { merge: true });
    currentUser = { ...currentUser, ...updateData };

    closeModal('modal-onboarding');
    showToast(`✨ ¡Perfil configurado con éxito como ${selectedOnboardRole === 'TEACHER' ? 'Docente' : (selectedOnboardRole === 'PARENT' ? 'Acudiente' : 'Estudiante')}!`);
    
    // Switch default screen according to role
    if (selectedOnboardRole === 'TEACHER') switchNav('teacher-admin');
    else if (selectedOnboardRole === 'PARENT') switchNav('parent-dashboard');
    else switchNav('feed');

    renderUserProfile();
    renderNavigationForRole();
    renderRoleSpecificScreens();
  } catch (err) {
    alert("Error al guardar perfil: " + err.message);
  }
}

// 7. Auth Actions (Signup Role Picker & Login)
function selectSignupRole(role) {
  selectedSignupRole = role;
  ['student', 'teacher', 'parent'].forEach(r => {
    const card = document.getElementById(`signup-role-${r}`);
    if (card) {
      if (r === role.toLowerCase()) card.classList.add('selected');
      else card.classList.remove('selected');
    }
  });

  document.getElementById('signup-group-grade').classList.toggle('hidden', role !== 'STUDENT');
  document.getElementById('signup-group-teacher').classList.toggle('hidden', role !== 'TEACHER');
  document.getElementById('signup-group-parent').classList.toggle('hidden', role !== 'PARENT');
}

function switchAuthTab(tab) {
  const loginBtn = document.getElementById('tab-login-btn');
  const signupBtn = document.getElementById('tab-signup-btn');
  const loginForm = document.getElementById('login-form');
  const signupForm = document.getElementById('signup-form');

  if (tab === 'login') {
    loginBtn.className = 'btn-primary tab-btn active';
    signupBtn.className = 'btn-secondary tab-btn';
    loginForm.classList.remove('hidden');
    signupForm.classList.add('hidden');
  } else {
    loginBtn.className = 'btn-secondary tab-btn';
    signupBtn.className = 'btn-primary tab-btn active';
    loginForm.classList.add('hidden');
    signupForm.classList.remove('hidden');
  }
}

async function handleGoogleSignIn() {
  try {
    showToast("Conectando con Google...");
    const provider = new firebase.auth.GoogleAuthProvider();

    // Si ya existe una sesión activa con correo/contraseña institucional, vincular credencial de Google
    if (auth.currentUser && !auth.currentUser.isAnonymous) {
      try {
        await auth.currentUser.linkWithPopup(provider);
        showToast("✅ Cuenta de Google vinculada con éxito a tu perfil");
        return;
      } catch (linkErr) {
        if (linkErr.code === 'auth/credential-already-in-use' || linkErr.code === 'auth/email-already-in-use') {
          console.log("[Auth] Credencial de Google ya en uso. Iniciando sesión directamente con Google...");
        } else if (linkErr.code === 'auth/provider-already-linked') {
          showToast("ℹ️ Esta cuenta ya está vinculada con Google");
          return;
        } else {
          console.warn("Aviso al vincular credencial de Google:", linkErr);
        }
      }
    }

    await auth.signInWithPopup(provider);
  } catch (err) {
    if (err.code === 'auth/popup-blocked' || err.code === 'auth/popup-closed-by-user') {
      const provider = new firebase.auth.GoogleAuthProvider();
      await auth.signInWithRedirect(provider);
    } else {
      alert("Error al ingresar con Google: " + err.message);
    }
  }
}

async function handleLogin(e) {
  e.preventDefault();
  const email = document.getElementById('login-email').value.trim();
  const pass = document.getElementById('login-password').value;
  try {
    showToast("Iniciando sesión...");
    await auth.signInWithEmailAndPassword(email, pass);
  } catch (err) {
    alert("Error de ingreso: " + err.message);
  }
}

async function handleSignup(e) {
  e.preventDefault();
  const name = document.getElementById('signup-name').value.trim();
  const email = document.getElementById('signup-email').value.trim();
  const pass = document.getElementById('signup-password').value;
  const grade = document.getElementById('signup-grade').value;
  const teacherSubject = document.getElementById('signup-teacher-subject').value.trim();
  const childCode = document.getElementById('signup-child-code').value.trim().toUpperCase();

  try {
    showToast("Creando cuenta...");
    const cred = await auth.createUserWithEmailAndPassword(email, pass);
    const user = cred.user;

    const initialUser = {
      name: name,
      email: email,
      role: selectedSignupRole,
      gradeSection: selectedSignupRole === 'STUDENT' ? grade : (selectedSignupRole === 'TEACHER' ? (teacherSubject || 'Docente') : 'Familiar'),
      teacherSubject: selectedSignupRole === 'TEACHER' ? teacherSubject : '',
      linkedStudentId: selectedSignupRole === 'PARENT' ? childCode : '',
      bio: selectedSignupRole === 'TEACHER' ? 'Docente en Escolaris 👨‍🏫' : (selectedSignupRole === 'PARENT' ? 'Acudiente en Escolaris 👨‍👩‍👧' : 'Estudiante activo 🚀'),
      credits: selectedSignupRole === 'TEACHER' ? 500 : (selectedSignupRole === 'PARENT' ? 200 : 100),
      xp: selectedSignupRole === 'STUDENT' ? 50 : 100,
      streakDays: 1,
      studentCode: 'ESC-' + Math.random().toString(36).substring(2, 8).toUpperCase(),
      avatarEmoji: selectedSignupRole === 'TEACHER' ? '👨‍🏫' : (selectedSignupRole === 'PARENT' ? '👨‍👩‍👧' : '🎓'),
      avatarColorHex: 0xFF2563EB,
      roleConfigured: true,
      badges: getInitialBadgesForRole(selectedSignupRole),
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    };

    await db.collection('users').doc(user.uid).set(initialUser);
    currentUser = { id: user.uid, ...initialUser };
    showToast("🎉 ¡Cuenta creada con éxito!");
  } catch (err) {
    alert("Error al registrar: " + err.message);
  }
}

async function handleSignOut() {
  if (confirm("¿Deseas cerrar tu sesión en Escolaris?")) {
    document.documentElement.classList.remove('auth-restoring');
    dataListenersAttached = false;
    localStorage.removeItem('escolaris_cached_user');
    localStorage.removeItem('escolaris_session_uid');
    sessionStorage.removeItem('escolaris_active_tab');
    await auth.signOut();
    location.reload();
  }
}

// 8. Profile & Header Rendering (Dual Mode: Mobile + Desktop)
function renderUserProfile() {
  if (!currentUser) return;

  const role = currentUser.role || 'STUDENT';
  const roleLabel = role === 'TEACHER' ? `👨‍🏫 Docente • ${currentUser.teacherSubject || 'Titular'}` : (role === 'PARENT' ? '👨‍👩‍👧 Acudiente / Padre' : `🎓 Estudiante • ${currentUser.gradeSection || '10° Grado'}`);
  const emoji = currentUser.avatarEmoji || (role === 'TEACHER' ? '👨‍🏫' : (role === 'PARENT' ? '👨‍👩‍👧' : '🎓'));
  const hasPhoto = !!currentUser.photoUri;
  const avatarHtml = hasPhoto ? `<img src="${currentUser.photoUri}" alt="Foto de perfil" class="avatar-img-fit">` : emoji;

  // 8.1 Mobile Top Header
  const topAvatar = document.getElementById('top-avatar-emoji');
  const topRole = document.getElementById('top-role-badge');
  const headerCoins = document.getElementById('header-coins');
  const headerStreak = document.getElementById('header-streak');
  if (topAvatar) topAvatar.innerHTML = avatarHtml;
  if (topRole) topRole.textContent = roleLabel;
  if (headerCoins) headerCoins.textContent = '🪙 ' + (currentUser.credits || 0);
  if (headerStreak) headerStreak.textContent = '🔥 ' + (currentUser.streakDays || 0);

  // 8.2 Desktop Sidebar User Card
  const sidebarAvatar = document.getElementById('sidebar-avatar-emoji');
  const sidebarName = document.getElementById('sidebar-user-name');
  const sidebarRole = document.getElementById('sidebar-role-badge');
  const sidebarXpText = document.getElementById('sidebar-xp-text');
  const sidebarLevelText = document.getElementById('sidebar-level-text');
  const sidebarXpBar = document.getElementById('sidebar-xp-bar');

  const xp = currentUser.xp || 0;
  const level = Math.floor(xp / 300) + 1;
  const xpInLevel = xp % 300;
  const pct = Math.min(100, Math.round((xpInLevel / 300) * 100));

  if (sidebarAvatar) sidebarAvatar.innerHTML = avatarHtml;
  if (sidebarName) sidebarName.textContent = currentUser.name || "Usuario";
  if (sidebarRole) sidebarRole.textContent = roleLabel;
  if (sidebarXpText) sidebarXpText.textContent = `${xpInLevel} / 300 XP`;
  if (sidebarLevelText) sidebarLevelText.textContent = `Nivel ${level}`;
  if (sidebarXpBar) sidebarXpBar.style.width = pct + '%';

  // 8.3 Desktop Top Header Bar
  const dHeaderStreak = document.getElementById('desktop-header-streak');
  const dHeaderCoins = document.getElementById('desktop-header-coins');
  const dHeaderAvatar = document.getElementById('desktop-header-avatar');
  if (dHeaderStreak) dHeaderStreak.textContent = `🔥 ${currentUser.streakDays || 0} días`;
  if (dHeaderCoins) dHeaderCoins.textContent = `🪙 ${currentUser.credits || 0} créditos`;
  if (dHeaderAvatar) dHeaderAvatar.innerHTML = avatarHtml;

  // 8.4 Store Balance & Profile Screen
  const storeBal = document.getElementById('store-balance-text');
  if (storeBal) storeBal.textContent = '🪙 ' + (currentUser.credits || 0);

  const profName = document.getElementById('profile-name');
  const profGrade = document.getElementById('profile-grade');
  const profBio = document.getElementById('profile-bio');
  const profAvatarContent = document.getElementById('profile-avatar-content');
  const profCoins = document.getElementById('profile-coins-chip');
  const profStreak = document.getElementById('profile-streak-chip');
  const btnRemovePhoto = document.getElementById('btn-remove-photo');
  
  if (profName) profName.textContent = currentUser.name || "Usuario";
  if (profGrade) profGrade.textContent = roleLabel;
  if (profBio) profBio.textContent = currentUser.bio || "Usuario activo en Escolaris 🚀";
  if (profAvatarContent) profAvatarContent.innerHTML = avatarHtml;
  if (profCoins) profCoins.textContent = '🪙 ' + (currentUser.credits || 0);
  if (profStreak) profStreak.textContent = '🔥 ' + (currentUser.streakDays || 0) + ' días';
  if (btnRemovePhoto) btnRemovePhoto.classList.toggle('hidden', !hasPhoto);

  // 8.5 Teacher & Student Class Code Cards
  const teacherCard = document.getElementById('card-teacher-code-box');
  const teacherCodeText = document.getElementById('teacher-code-text');
  const adminTeacherCodeText = document.getElementById('admin-teacher-code-text');
  const studentCodeCard = document.getElementById('card-student-code-box');
  const studentCodeText = document.getElementById('student-code-text');
  const studentClassroomCard = document.getElementById('card-student-classroom-box');
  const studentClassroomStatus = document.getElementById('student-classroom-status');

  if (role === 'TEACHER') {
    if (teacherCard) teacherCard.classList.remove('hidden');
    if (teacherCodeText) teacherCodeText.textContent = currentUser.teacherCode || "DOC-102938";
    if (adminTeacherCodeText) adminTeacherCodeText.textContent = currentUser.teacherCode || "DOC-102938";
    if (studentCodeCard) studentCodeCard.classList.add('hidden');
    if (studentClassroomCard) studentClassroomCard.classList.add('hidden');
  } else if (role === 'STUDENT') {
    if (teacherCard) teacherCard.classList.add('hidden');
    if (studentCodeCard) studentCodeCard.classList.remove('hidden');
    if (studentCodeText) studentCodeText.textContent = currentUser.studentCode || "ESC-000000";
    if (studentClassroomCard) studentClassroomCard.classList.remove('hidden');

    if (studentClassroomStatus) {
      if (currentUser.linkedTeacherCode) {
        const teacher = allUsers.find(u => u.teacherCode === currentUser.linkedTeacherCode || u.id === currentUser.linkedTeacherCode);
        studentClassroomStatus.innerHTML = `
          <div class="code-box" style="background:rgba(16, 185, 129, 0.08); border-color:#86efac;">
            <div>
              <div class="code-label" style="color:#16a34a;">AULA VINCULADA</div>
              <div class="code-value" style="color:#166534; font-size:14px;">${teacher ? teacher.name : 'Docente'} (${currentUser.linkedTeacherCode})</div>
            </div>
            <button class="btn-secondary btn-sm btn-danger-outline" onclick="handleLeaveTeacherClass()">Cambiar</button>
          </div>
        `;
      } else {
        studentClassroomStatus.innerHTML = `
          <div style="display:flex; gap:8px; margin-top:8px;">
            <input type="text" id="input-join-class-code" class="form-input" placeholder="Ej: DOC-8K2P10" style="text-transform:uppercase; font-weight:800;">
            <button class="btn-primary btn-sm" onclick="handleJoinTeacherClass()">Unirme 🚀</button>
          </div>
        `;
      }
    }
  } else {
    // PARENT
    if (teacherCard) teacherCard.classList.add('hidden');
    if (studentCodeCard) studentCodeCard.classList.add('hidden');
    if (studentClassroomCard) studentClassroomCard.classList.add('hidden');
  }

  // Hide post creation for parent
  const postBtnFeed = document.getElementById('btn-create-post-feed');
  if (postBtnFeed) postBtnFeed.style.display = role === 'PARENT' ? 'none' : 'block';

  // XP / Level Hero Card (Rangos Escolares Casuales)
  const rankInfo = getSchoolRank(xp);
  const heroRankTitle = document.getElementById('hero-rank-title');
  const heroLevel = document.getElementById('hero-level-badge');
  const heroXpText = document.getElementById('hero-xp-text');
  const heroXpBar = document.getElementById('hero-xp-bar');
  if (heroRankTitle) heroRankTitle.textContent = `${rankInfo.emoji} ${rankInfo.title}`;
  if (heroLevel) heroLevel.textContent = 'Nivel ' + level;
  if (heroXpText) heroXpText.textContent = xpInLevel + ' / 300 XP';
  if (heroXpBar) heroXpBar.style.width = pct + '%';

  // Sincronizar subpestaña de tienda en perfil
  if (activeProfileSubtab === 'store') {
    renderProfileStore();
  }
}

function openStudentRanksModal() {
  const container = document.getElementById('student-ranks-tiers-list');
  if (!container) return;

  const currentXp = currentUser ? (currentUser.xp || 0) : 0;
  const currentRank = getSchoolRank(currentXp);

  container.innerHTML = SCHOOL_RANKS.map(rk => {
    const isCurrent = currentRank.level === rk.level;
    const isUnlocked = currentXp >= rk.minXp;
    return `
      <div class="rank-tier-card ${isCurrent ? 'active' : ''}">
        <div class="rank-tier-emoji">${rk.emoji}</div>
        <div class="rank-tier-info">
          <div class="rank-tier-title">
            <span>${rk.title}</span>
            ${isCurrent ? `<span class="badge" style="background:#fef3c7; color:#b45309;">⭐ Tu Rango Actual</span>` : (isUnlocked ? `<span class="badge" style="background:#dcfce7; color:#15803d;">✓ Alcanzado</span>` : '')}
          </div>
          <p class="rank-tier-desc">${rk.desc}</p>
        </div>
        <div class="rank-tier-xp-tag">
          ${rk.minXp === 0 ? 'Nivel 1' : `${rk.minXp}+ XP`}
        </div>
      </div>
    `;
  }).join('');

  openModal('modal-student-ranks');
}

let activeProfileSubtab = 'badges';

function switchProfileSubtab(subtab) {
  activeProfileSubtab = subtab;
  const badgesSec = document.getElementById('profile-subtab-content-badges');
  const storeSec = document.getElementById('profile-subtab-content-store');
  const btnBadges = document.getElementById('btn-profile-subtab-badges');
  const btnStore = document.getElementById('btn-profile-subtab-store');

  if (subtab === 'store') {
    if (badgesSec) badgesSec.classList.add('hidden');
    if (storeSec) storeSec.classList.remove('hidden');
    if (btnBadges) btnBadges.className = 'settings-subtab-chip';
    if (btnStore) btnStore.className = 'settings-subtab-chip active';
    renderProfileStore();
  } else {
    if (badgesSec) badgesSec.classList.remove('hidden');
    if (storeSec) storeSec.classList.add('hidden');
    if (btnBadges) btnBadges.className = 'settings-subtab-chip active';
    if (btnStore) btnStore.className = 'settings-subtab-chip';
    renderProfileBadges();
  }
}

function renderProfileStore() {
  const catalogCont = document.getElementById('profile-store-catalog-container');
  const purchasesCont = document.getElementById('profile-store-purchases-container');
  const balanceBadge = document.getElementById('profile-store-balance-badge');

  const credits = currentUser ? (currentUser.credits || 0) : 0;
  if (balanceBadge) balanceBadge.textContent = `🪙 ${credits}`;

  if (catalogCont) {
    if (rewardsList.length === 0) {
      catalogCont.innerHTML = `<p style="font-size:12px; color:var(--text-muted); text-align:center;">No hay recompensas escolares registradas.</p>`;
    } else {
      const categoryLabels = {
        'AULA': '🪑 Aula',
        'ACADEMICO': '📚 Académico',
        'RECREO': '⚽ Recreo',
        'CAFETERIA': '🥪 Cafetería',
        'DISTINCION': '🏆 Distinción'
      };

      const topRewards = rewardsList.slice(0, 8);
      catalogCont.innerHTML = `
        <div style="display:flex; flex-direction:column; gap:8px;">
          ${topRewards.map(r => {
            const canAfford = credits >= r.costCredits;
            const catLabel = categoryLabels[(r.category || 'ACADEMICO').toUpperCase()] || '🎟️ Escolar';
            return `
              <div class="profile-store-item">
                <div style="display:flex; align-items:center; gap:10px; flex:1; min-width:180px;">
                  <span style="font-size:24px; flex-shrink:0;">${r.icon || '🎁'}</span>
                  <div style="min-width:0;">
                    <div style="display:flex; align-items:center; gap:6px; flex-wrap:wrap;">
                      <strong style="font-size:13px; color:var(--text-main); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${escapeHtml(r.title)}</strong>
                      <span class="badge" style="font-size:9.5px; background:var(--surface-variant); color:var(--text-muted); font-weight:800; white-space:nowrap !important;">${catLabel}</span>
                    </div>
                    <span style="font-size:11px; color:var(--text-muted); display:block; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${escapeHtml(r.description)}</span>
                  </div>
                </div>
                <div style="display:flex; align-items:center; gap:8px; flex-shrink:0;">
                  <span style="font-size:12px; font-weight:800; color:#b45309; white-space:nowrap;">🪙 ${r.costCredits}</span>
                  <button class="btn-primary btn-sm" style="font-size:11px; padding:4px 10px; font-weight:800; white-space:nowrap;" 
                    onclick="redeemReward('${r.id}', '${escapeHtml(r.title)}', ${r.costCredits})"
                    ${!canAfford ? 'disabled style="opacity:0.5; cursor:not-allowed;"' : ''}>
                    ${canAfford ? 'Canjear 🎟️' : 'Faltan 🪙'}
                  </button>
                </div>
              </div>
            `;
          }).join('')}
          <div style="text-align:center; margin-top:8px;">
            <button class="btn-secondary btn-sm" onclick="switchNav('rewards')" style="font-size:11.5px; font-weight:800; width:100%;">
              Ver las ${rewardsList.length} Recompensas en la Tienda Escolar ➔
            </button>
          </div>
        </div>
      `;
    }
  }

  if (purchasesCont) {
    const myRedemptions = userRedemptions.filter(p => currentUser && p.studentId === currentUser.id);
    if (myRedemptions.length === 0) {
      purchasesCont.innerHTML = `
        <div style="text-align:center; padding:14px; background:var(--surface-variant); border-radius:10px; border:1px dashed var(--border-color);">
          <span style="font-size:22px;">🎟️</span>
          <p style="font-size:11.5px; color:var(--text-muted); margin:4px 0 0 0;">Aún no has solicitado canjes. Cuando canjees una recompensa, aparecerá aquí.</p>
        </div>
      `;
    } else {
      purchasesCont.innerHTML = myRedemptions.map(p => {
        let statusBadge = '';
        if (p.status === 'PENDING_APPROVAL') {
          statusBadge = `<span class="redemption-status-pill pending">⏳ Pendiente de aprobación</span>`;
        } else if (p.status === 'USED') {
          statusBadge = `<span class="redemption-status-pill approved">✅ Utilizado en clase</span>`;
        } else if (p.status === 'REJECTED') {
          statusBadge = `<span class="redemption-status-pill rejected">❌ Rechazado (Puntos devueltos)</span>`;
        } else {
          statusBadge = `<span class="redemption-status-pill approved">🎟️ Aprobado / Activo</span>`;
        }

        return `
          <div class="profile-store-item">
            <div>
              <div style="display:flex; align-items:center; gap:6px; flex-wrap:wrap;">
                <strong style="font-size:13px; color:var(--text-main);">${escapeHtml(p.rewardTitle || 'Pase Escolar')}</strong>
                ${statusBadge}
              </div>
              <div style="font-size:11px; color:var(--text-muted); margin-top:2px;">
                Código: <strong style="color:var(--primary);">${p.voucherCode || 'ESC-000'}</strong> • Costo: 🪙 ${p.costCredits || 0}
              </div>
            </div>
            ${(p.status === 'APPROVED' || p.status === 'ACTIVE') ? `
              <button class="btn-secondary btn-sm" style="font-size:11px;" onclick="showQrCode('${escapeHtml(p.rewardTitle || '')}', '${p.voucherCode}')">Ver QR 📱</button>
            ` : ''}
          </div>
        `;
      }).join('');
    }
  }
}

function switchSettingsSubtab(subtab) {
  const codesSec = document.getElementById('settings-section-codes');
  const visualSec = document.getElementById('settings-section-visual');
  const accountSec = document.getElementById('settings-section-account');
  const btnCodes = document.getElementById('btn-settings-tab-codes');
  const btnVisual = document.getElementById('btn-settings-tab-visual');
  const btnAccount = document.getElementById('btn-settings-tab-account');

  if (codesSec) codesSec.classList.toggle('hidden', subtab !== 'codes');
  if (visualSec) visualSec.classList.toggle('hidden', subtab !== 'visual');
  if (accountSec) accountSec.classList.toggle('hidden', subtab !== 'account');

  if (btnCodes) btnCodes.className = subtab === 'codes' ? 'settings-subtab-chip active' : 'settings-subtab-chip';
  if (btnVisual) btnVisual.className = subtab === 'visual' ? 'settings-subtab-chip active' : 'settings-subtab-chip';
  if (btnAccount) btnAccount.className = subtab === 'account' ? 'settings-subtab-chip active' : 'settings-subtab-chip';
}

function renderRoleSpecificScreens() {
  if (!currentUser) return;
  if (currentUser.role === 'TEACHER') {
    renderAdminMetrics();
    renderAdminUsers();
    renderAdminObligations();
    renderAdminBadges();
    renderAdminPasses();
    renderAdminTardies();
    populateStudentSelects();
  } else if (currentUser.role === 'PARENT') {
    renderParentDashboard();
  }
}

// 9. Parent Portal Screen Logic & Interactive Obligations Checklist
let activeParentSubtab = 'pending';

function switchParentSubtab(subtab) {
  activeParentSubtab = subtab;
  ['pending', 'completed', 'academic'].forEach(tab => {
    const btn = document.getElementById(`btn-parent-subtab-${tab}`);
    const section = document.getElementById(`parent-subtab-${tab}`);
    if (btn && section) {
      if (tab === subtab) {
        btn.className = 'btn-primary btn-filter active';
        section.classList.remove('hidden');
      } else {
        btn.className = 'btn-secondary btn-filter';
        section.classList.add('hidden');
      }
    }
  });

  renderParentDashboard();
}

function renderParentDashboard() {
  const nameEl = document.getElementById('parent-student-name');
  const gradeEl = document.getElementById('parent-student-grade');
  if (!nameEl || !currentUser) return;

  const linkedCode = currentUser.linkedStudentId || '';
  const child = allUsers.find(u => u.studentCode === linkedCode || u.id === linkedCode);

  if (child) {
    nameEl.textContent = `Seguimiento de: ${child.name}`;
    gradeEl.textContent = `${child.gradeSection || '10° Grado'} • Código: ${child.studentCode || linkedCode} • Saldo hijo/a: 🪙 ${child.credits || 0}`;
  } else if (linkedCode) {
    nameEl.textContent = `Código vinculado: ${linkedCode}`;
    gradeEl.textContent = 'Buscando información del estudiante en la plataforma...';
  } else {
    nameEl.textContent = 'Sin Estudiante Vinculado';
    gradeEl.textContent = 'Toca "Vincular" e ingresa el código único de tu hijo/a (ESC-XXXXXX).';
  }

  // Pending count badge update
  const pendingCount = allParentObligations.filter(o => !o.isCompleted).length;
  const pendingBadge = document.getElementById('parent-badge-pending-count');
  if (pendingBadge) {
    pendingBadge.textContent = pendingCount;
    if (pendingCount > 0) {
      pendingBadge.classList.remove('hidden');
    } else {
      pendingBadge.classList.add('hidden');
    }
  }

  // 1. WhatsApp Pension Reminder Card Logic
  const whatsappCard = document.getElementById('parent-whatsapp-pension-card');
  const whatsappMsgText = document.getElementById('parent-whatsapp-message-text');

  // Check if there is any pending pension obligation
  const pendingPension = allParentObligations.find(o => o.category === 'PENSION' && !o.isCompleted);
  if (whatsappCard) {
    if (pendingPension) {
      whatsappCard.classList.remove('hidden');
      if (whatsappMsgText) {
        whatsappMsgText.innerHTML = pendingPension.whatsappMessage 
          ? escapeHtml(pendingPension.whatsappMessage)
          : `¡Hola estimado acudiente! 👋 Les recordamos que la pensión escolar de <strong>${pendingPension.month || 'este mes'}</strong> se cancela dentro de los 5 primeros días. ¡Paga a tiempo para ganar la insignia de <strong>${pendingPension.rewardBadgeTitle || 'Pago Oportuno 💳'}</strong> y <strong>+${pendingPension.rewardCredits || 100} créditos Escolaris</strong> para tu hijo/a! ⭐`;
      }
    } else {
      whatsappCard.classList.add('hidden');
    }
  }

  // 2. Render Checklist & Subsections
  renderParentObligations();
  renderParentFamilyBadges();
  renderParentTasks();
  renderParentTardies();
}

function renderParentObligations() {
  const pendingContainer = document.getElementById('parent-obligations-list');
  const completedContainer = document.getElementById('parent-completed-obligations-list');
  const progressText = document.getElementById('parent-obligations-progress-text');
  const percentPill = document.getElementById('parent-obligations-percent-pill');
  const progressBar = document.getElementById('parent-obligations-progress-bar');

  if (!currentUser) return;

  const total = allParentObligations.length;
  const completedList = allParentObligations.filter(o => o.isCompleted);
  const pendingList = allParentObligations.filter(o => !o.isCompleted);
  const completed = completedList.length;
  const percent = total > 0 ? Math.round((completed / total) * 100) : 100;

  if (progressText) progressText.textContent = `${completed} de ${total} deberes cumplidos este mes`;
  if (percentPill) {
    percentPill.textContent = `${percent}%`;
    percentPill.style.background = percent === 100 ? 'rgba(16,185,129,0.2)' : 'rgba(37,99,235,0.15)';
    percentPill.style.color = percent === 100 ? '#059669' : 'var(--primary)';
  }
  if (progressBar) progressBar.style.width = `${percent}%`;

  // Render Pending List
  if (pendingContainer) {
    if (pendingList.length === 0) {
      pendingContainer.innerHTML = `
        <div class="clay-card" style="text-align:center; padding:24px; background:#f0fdf4; border:1px solid #86efac;">
          <span style="font-size:32px;">🎉</span>
          <h4 style="color:#15803d; font-weight:800; margin:6px 0 2px 0;">¡Todo al Día!</h4>
          <p style="font-size:12px; color:#166534; margin:0;">
            No tienes pagos de pensión ni obligaciones pendientes por realizar este mes. ¡Excelente puntualidad familiar!
          </p>
        </div>
      `;
    } else {
      pendingContainer.innerHTML = pendingList.map(obl => renderSingleObligationCard(obl)).join('');
    }
  }

  // Render Completed List
  if (completedContainer) {
    if (completedList.length === 0) {
      completedContainer.innerHTML = `
        <p style="font-size:12px; color:var(--text-muted); text-align:center; padding:16px;">
          Aún no has completado deberes este mes. Al pagar la pensión a tiempo, aparecerá aquí con su logro.
        </p>
      `;
    } else {
      completedContainer.innerHTML = completedList.map(obl => renderSingleObligationCard(obl)).join('');
    }
  }
}

function renderSingleObligationCard(obl) {
  const isCompleted = obl.isCompleted;
  const isPension = obl.category === 'PENSION';
  const categoryTag = isPension 
    ? `<span class="badge" style="background:#dcfce7; color:#15803d; font-weight:800;">💳 Pensión Escolar</span>` 
    : (obl.category === 'DOCUMENTATION' ? `<span class="badge" style="background:#e0e7ff; color:#4338ca; font-weight:800;">📄 Circular / Documento</span>` : `<span class="badge" style="background:#fef3c7; color:#b45309; font-weight:800;">🏛️ Evento Institucional</span>`);

  return `
    <div class="obligation-card ${isPension ? 'pension' : ''} ${isCompleted ? 'completed' : ''}">
      <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:8px;">
        <div>
          <div style="display:flex; align-items:center; gap:6px; flex-wrap:wrap;">
            ${categoryTag}
            <span style="font-size:11px; font-weight:800; color:var(--text-muted);">
              ⏰ ${isPension ? '5 Primeros Días' : `Plazo: Día ${obl.dueDayOfMonth || 15}`} • ${obl.month || 'Octubre'}
            </span>
          </div>
          <strong style="font-size:14.5px; color:var(--text-main); margin-top:4px; display:block;">${escapeHtml(obl.title)}</strong>
          <p style="font-size:12px; color:var(--text-muted); margin-top:2px;">${escapeHtml(obl.description)}</p>
        </div>
        <span style="font-size:22px;">${isCompleted ? '✅' : '⏳'}</span>
      </div>

      <div class="obligation-badge-preview">
        <div style="display:flex; align-items:center; gap:6px;">
          <span style="font-size:18px;">${obl.rewardBadgeEmoji || '💳'}</span>
          <span>Logro: <strong>${escapeHtml(obl.rewardBadgeTitle || 'Pago Oportuno')}</strong></span>
        </div>
        <div style="display:flex; align-items:center; gap:6px;">
          <span class="reward-price" style="font-size:11px;">+${obl.rewardCredits || 100} 🪙</span>
          <span style="font-size:11px; font-weight:800; color:var(--primary);">+${obl.rewardXp || 150} ⚡</span>
        </div>
      </div>

      <div style="display:flex; justify-content:space-between; align-items:center; margin-top:4px;">
        <span style="font-size:10.5px; color:var(--text-muted);">
          ${isCompleted ? `✅ Cumplida el ${obl.completedAtMillis ? new Date(obl.completedAtMillis).toLocaleDateString('es-CO') : 'Reciente'}` : 'Recompensa directa para tu hijo/a'}
        </span>
        ${!isCompleted ? `
          <button class="btn-primary btn-sm" style="background:#059669; font-weight:800;" onclick="openConfirmCompleteObligation('${obl.id}')">
            Marcar como Realizado ✅
          </button>
        ` : `
          <span style="font-size:11px; font-weight:800; color:#059669; background:#dcfce7; padding:3px 8px; border-radius:6px;">
            ¡Completada y Condecorada! 🎉
          </span>
        `}
      </div>
    </div>
  `;
}

function filterParentObligations(filter) {
  activeObligationFilter = filter;
  document.querySelectorAll('.btn-obl-filter').forEach(b => b.className = 'btn-secondary btn-sm btn-obl-filter');
  const activeBtn = document.getElementById(`btn-obl-filter-${filter}`);
  if (activeBtn) activeBtn.className = 'btn-primary btn-sm btn-obl-filter active';
  renderParentObligations();
}

function scrollToObligationsChecklist() {
  const checklistSec = document.getElementById('parent-obligations-section');
  if (checklistSec) {
    checklistSec.scrollIntoView({ behavior: 'smooth' });
    checklistSec.style.transform = 'scale(1.02)';
    checklistSec.style.borderColor = '#25D366';
    setTimeout(() => {
      checklistSec.style.transform = 'scale(1)';
      checklistSec.style.borderColor = 'rgba(37,99,235,0.3)';
    }, 800);
  }
}

function openConfirmCompleteObligation(oblId) {
  const obl = allParentObligations.find(o => o.id === oblId);
  if (!obl) return;

  document.getElementById('confirm-obl-id').value = obl.id;
  document.getElementById('confirm-obl-title').textContent = obl.title;
  document.getElementById('confirm-obl-emoji').textContent = obl.rewardBadgeEmoji || '💳';
  document.getElementById('confirm-obl-desc').textContent = `¿Confirmas que has realizado ${obl.title}? Se desbloqueará el logro institucional y se otorgarán los incentivos a tu hijo/a.`;
  
  const rewardsEl = document.getElementById('confirm-obl-rewards-text');
  if (rewardsEl) {
    rewardsEl.innerHTML = `
      • Logro Escolar: <strong>${obl.rewardBadgeTitle || 'Pago Oportuno'}</strong> ${obl.rewardBadgeEmoji || '💳'}<br>
      • Recompensa: <strong>+${obl.rewardCredits || 100} Créditos Escolaris 🪙</strong><br>
      • Progreso: <strong>+${obl.rewardXp || 150} XP ⚡</strong>
    `;
  }

  openModal('modal-confirm-obligation');
}

async function executeCompleteObligation() {
  const oblId = document.getElementById('confirm-obl-id').value;
  const obl = allParentObligations.find(o => o.id === oblId);
  if (!obl || !currentUser) return;

  const linkedCode = currentUser.linkedStudentId || '';
  const child = allUsers.find(u => u.studentCode === linkedCode || u.id === linkedCode);
  const childId = child ? child.id : (currentUser.id || '');
  const childName = child ? child.name : (currentUser.name || 'Estudiante');

  try {
    // 1. Update Obligation in Firestore
    await db.collection('parent_obligations').doc(oblId).update({
      isCompleted: true,
      completedAtMillis: Date.now(),
      completedByParentName: currentUser.name || 'Acudiente'
    });

    // 2. Grant Badge to Student in Firestore
    await db.collection('badges').add({
      studentId: childId,
      studentName: childName,
      badgeKey: obl.rewardBadgeKey || 'PARENT_PENSION_OCTUBRE',
      title: obl.rewardBadgeTitle || 'Pago Oportuno de Pensión',
      description: `Obligación cumplida por la familia: ${obl.title}`,
      emoji: obl.rewardBadgeEmoji || '💳',
      category: 'FAMILY',
      teacherNote: '¡Excelente cumplimiento y puntualidad familiar!',
      unlockedByTeacher: currentUser.name || 'Acudiente',
      creditReward: obl.rewardCredits || 100,
      xpReward: obl.rewardXp || 150,
      unlockedAtMillis: Date.now(),
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    // 3. Reward the child with Escolaris Credits and XP
    if (childId) {
      const studentDoc = await db.collection('users').doc(childId).get();
      if (studentDoc.exists) {
        const sData = studentDoc.data();
        await db.collection('users').doc(childId).update({
          credits: (sData.credits || 0) + (obl.rewardCredits || 100),
          xp: (sData.xp || 0) + (obl.rewardXp || 150)
        });
      }
    }

    closeModal('modal-confirm-obligation');

    // 4. Open Festive Celebration Modal
    const celebEmoji = document.getElementById('celebration-badge-emoji');
    const celebTitle = document.getElementById('celebration-badge-title');
    const celebDesc = document.getElementById('celebration-badge-desc');

    if (celebEmoji) celebEmoji.textContent = obl.rewardBadgeEmoji || '💳';
    if (celebTitle) celebTitle.textContent = `¡${obl.rewardBadgeTitle || 'Pago Oportuno'}!`;
    if (celebDesc) {
      celebDesc.innerHTML = `¡Felicitaciones ${currentUser.name || 'Familia'}! Has completado <strong>${escapeHtml(obl.title)}</strong>.<br><br>Tu hijo/a <strong>${childName}</strong> ha recibido <strong>+${obl.rewardCredits || 100} Créditos Escolaris 🪙</strong> y <strong>+${obl.rewardXp || 150} XP ⚡</strong> en su perfil para canjear en la tienda escolar.`;
    }

    openModal('modal-celebration-badge');
    showToast(`🎉 ¡Insignia desbloqueada y +${obl.rewardCredits || 100} 🪙 otorgados a ${childName}!`);

    // Vibrate device if supported on mobile
    if (navigator.vibrate) navigator.vibrate([100, 50, 200]);
  } catch (err) {
    alert("Error al completar obligación: " + err.message);
  }
}

// Teacher / Admin Obligations Management
function renderAdminObligations() {
  const container = document.getElementById('admin-obligations-list');
  if (!container) return;

  if (allParentObligations.length === 0) {
    container.innerHTML = `
      <div class="clay-card" style="text-align:center; padding:24px;">
        <span style="font-size:32px;">📋</span>
        <p style="font-size:13px; color:var(--text-muted); margin-top:8px;">No hay deberes ni pensiones configuradas actualmente. Toca "+ Asignar Deber / Pensión".</p>
      </div>
    `;
    return;
  }

  container.innerHTML = allParentObligations.map(obl => {
    const isPension = obl.category === 'PENSION';
    const categoryBadge = isPension 
      ? `<span class="badge" style="background:#dcfce7; color:#15803d; font-weight:800;">💳 Pensión Escolar</span>` 
      : (obl.category === 'DOCUMENTATION' ? `<span class="badge" style="background:#e0e7ff; color:#4338ca; font-weight:800;">📄 Circular / Documento</span>` : `<span class="badge" style="background:#fef3c7; color:#b45309; font-weight:800;">🏛️ Evento</span>`);

    return `
      <div class="clay-card" style="padding:14px; border-left:4px solid ${isPension ? '#10B981' : 'var(--primary)'};">
        <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:10px;">
          <div style="flex:1; min-width:240px;">
            <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
              ${categoryBadge}
              <span style="font-size:11px; font-weight:800; color:var(--text-muted);">Mes: ${obl.month || 'Octubre'} • Límite: Día ${obl.dueDayOfMonth || 5} (2:00 PM)</span>
            </div>
            <strong style="font-size:15px; color:var(--text-main); margin-top:4px; display:block;">${escapeHtml(obl.title)}</strong>
            <p style="font-size:12px; color:var(--text-muted); margin-top:2px;">${escapeHtml(obl.description)}</p>

            <div style="margin-top:8px; background:rgba(0,0,0,0.03); border-radius:8px; padding:6px 10px; font-size:11px; color:var(--text-main); display:flex; align-items:center; justify-content:space-between;">
              <div><strong>Recompensa:</strong> Insignia ${obl.rewardBadgeEmoji || '💳'} <em>"${obl.rewardBadgeTitle || 'Insignia'}"</em> • ⚡ +${obl.rewardXp || 150} XP</div>
              <strong style="color:#b45309; font-size:12px;">+${obl.rewardCredits || 100} 🪙</strong>
            </div>

            <div style="margin-top:6px; font-size:11px; color:#1e40af; background:#eff6ff; padding:6px 10px; border-radius:8px; border:1px solid #bfdbfe;">
              <strong>🔔 Recordatorio Escolar para Padres:</strong> "${escapeHtml(obl.whatsappMessage || '')}"
            </div>
          </div>

          <div style="display:flex; flex-direction:column; gap:6px;">
            <button class="btn-primary btn-sm" style="font-weight:800;" onclick="handleBroadcastWhatsAppReminder('${obl.id}')" title="Enviar recordatorio escolar a los acudientes">
              🔔 Enviar Recordatorio
            </button>
            <button class="btn-secondary btn-sm" onclick="openEditObligationModal('${obl.id}')" title="Editar detalles de la obligación">
              ✏️ Editar
            </button>
            <button class="btn-secondary btn-sm btn-danger-outline" onclick="handleDeleteObligation('${obl.id}', '${escapeHtml(obl.title)}')">
              🗑️ Eliminar
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function openEditObligationModal(oblId) {
  const obl = allParentObligations.find(o => o.id === oblId);
  if (!obl) return;

  const idxIn = document.getElementById('edit-obl-index');
  const titleIn = document.getElementById('edit-obl-title');
  const descIn = document.getElementById('edit-obl-desc');
  const monthIn = document.getElementById('edit-obl-month');
  const dueDayIn = document.getElementById('edit-obl-due-day');
  const badgeTitleIn = document.getElementById('edit-obl-badge-title');
  const pointsIn = document.getElementById('edit-obl-points');

  if (idxIn) idxIn.value = oblId;
  if (titleIn) titleIn.value = obl.title || '';
  if (descIn) descIn.value = obl.description || '';
  if (monthIn) monthIn.value = obl.month || 'Octubre';
  if (dueDayIn) dueDayIn.value = obl.dueDayOfMonth || 5;
  if (badgeTitleIn) badgeTitleIn.value = obl.rewardBadgeTitle || '';
  if (pointsIn) pointsIn.value = obl.rewardCredits || 100;

  openModal('modal-edit-obligation');
}

async function handleSaveObligation(e) {
  e.preventDefault();
  const oblId = document.getElementById('edit-obl-index')?.value;
  const title = document.getElementById('edit-obl-title')?.value.trim();
  const description = document.getElementById('edit-obl-desc')?.value.trim();
  const month = document.getElementById('edit-obl-month')?.value.trim();
  const dueDayOfMonth = parseInt(document.getElementById('edit-obl-due-day')?.value, 10) || 5;
  const rewardBadgeTitle = document.getElementById('edit-obl-badge-title')?.value.trim();
  const rewardCredits = parseInt(document.getElementById('edit-obl-points')?.value, 10) || 100;

  if (!oblId) return;

  try {
    if (db) {
      await db.collection('parent_obligations').doc(oblId).update({
        title,
        description,
        month,
        dueDayOfMonth,
        rewardBadgeTitle,
        rewardCredits
      });
    }

    const localObl = allParentObligations.find(o => o.id === oblId);
    if (localObl) {
      localObl.title = title;
      localObl.description = description;
      localObl.month = month;
      localObl.dueDayOfMonth = dueDayOfMonth;
      localObl.rewardBadgeTitle = rewardBadgeTitle;
      localObl.rewardCredits = rewardCredits;
    }

    closeModal('modal-edit-obligation');
    showToast("💾 Deber familiar actualizado correctamente");
    renderParentObligations();
  } catch (err) {
    alert("Error al actualizar deber: " + err.message);
  }
}

function applyObligationPreset(type) {
  document.querySelectorAll('.preset-obl-btn').forEach(b => b.classList.remove('active'));
  const targetBtn = event ? event.currentTarget : null;
  if (targetBtn) targetBtn.classList.add('active');

  const titleIn = document.getElementById('obl-title');
  const descIn = document.getElementById('obl-desc');
  const dueDayIn = document.getElementById('obl-due-day');
  const badgeTitleIn = document.getElementById('obl-badge-title');
  const badgeEmojiIn = document.getElementById('obl-badge-emoji');
  const creditsIn = document.getElementById('obl-credits');
  const xpIn = document.getElementById('obl-xp');
  const msgIn = document.getElementById('obl-whatsapp-msg');
  const catIn = document.getElementById('obl-category');
  const keyIn = document.getElementById('obl-badge-key');

  if (type === 'PENSION') {
    if (titleIn) titleIn.value = 'Pago Oportuno de Pensión Escolar';
    if (descIn) descIn.value = 'Pago de la pensión escolar correspondiente al mes durante los primeros 5 días.';
    if (dueDayIn) dueDayIn.value = 5;
    if (badgeTitleIn) badgeTitleIn.value = 'Pago Oportuno de Pensión (Octubre)';
    if (badgeEmojiIn) badgeEmojiIn.value = '💳';
    if (creditsIn) creditsIn.value = 100;
    if (xpIn) xpIn.value = 150;
    if (msgIn) msgIn.value = '¡Hola estimado acudiente! 👋 Les recordamos que a partir de octubre la pensión se paga los 5 primeros días del mes. ¡Paga a tiempo para ganar la insignia de Pago Oportuno y +100 créditos Escolaris para tu hijo/a! ⭐';
    if (catIn) catIn.value = 'PENSION';
    if (keyIn) keyIn.value = 'PARENT_PENSION_OCTUBRE';
  } else if (type === 'DOCUMENTATION') {
    if (titleIn) titleIn.value = 'Firma Digital de Circular Informativa';
    if (descIn) descIn.value = 'Revisión y firma digital de la circular de convivencia institucional.';
    if (dueDayIn) dueDayIn.value = 10;
    if (badgeTitleIn) badgeTitleIn.value = 'Acudiente Informado & Diligente';
    if (badgeEmojiIn) badgeEmojiIn.value = '📄';
    if (creditsIn) creditsIn.value = 60;
    if (xpIn) xpIn.value = 100;
    if (msgIn) msgIn.value = '¡Hola estimado acudiente! 📄 Recuerde firmar la circular escolar antes del día 10 para condecorar a su hijo/a con +60 créditos Escolaris.';
    if (catIn) catIn.value = 'DOCUMENTATION';
    if (keyIn) keyIn.value = 'PARENT_CIRCULAR_OCTUBRE';
  } else if (type === 'EVENT') {
    if (titleIn) titleIn.value = 'Asamblea General de Padres de Familia';
    if (descIn) descIn.value = 'Asistencia a la reunión trimestral de seguimiento formativo y pedagógico.';
    if (dueDayIn) dueDayIn.value = 15;
    if (badgeTitleIn) badgeTitleIn.value = 'Compromiso Familiar en Asamblea';
    if (badgeEmojiIn) badgeEmojiIn.value = '🏛️';
    if (creditsIn) creditsIn.value = 80;
    if (xpIn) xpIn.value = 120;
    if (msgIn) msgIn.value = '¡Hola familia Escolaris! 🏛️ Los esperamos en la Asamblea General de Padres. ¡Tu asistencia condecora a tu hijo/a con +80 créditos!';
    if (catIn) catIn.value = 'EVENT';
    if (keyIn) keyIn.value = 'PARENT_MEETING_1';
  }
}

async function handleCreateObligation(e) {
  e.preventDefault();
  const title = document.getElementById('obl-title').value.trim();
  const description = document.getElementById('obl-desc').value.trim();
  const month = document.getElementById('obl-month').value.trim();
  const dueDay = parseInt(document.getElementById('obl-due-day').value, 10) || 5;
  const badgeTitle = document.getElementById('obl-badge-title').value.trim();
  const badgeEmoji = document.getElementById('obl-badge-emoji').value.trim();
  const credits = parseInt(document.getElementById('obl-credits').value, 10) || 100;
  const xp = parseInt(document.getElementById('obl-xp').value, 10) || 150;
  const whatsappMessage = document.getElementById('obl-whatsapp-msg').value.trim();
  const category = document.getElementById('obl-category').value;
  const badgeKey = document.getElementById('obl-badge-key').value;

  try {
    const nowId = Date.now();
    await db.collection('parent_obligations').add({
      id: nowId,
      title: title,
      description: description,
      category: category,
      month: month,
      dueDayOfMonth: dueDay,
      isCompleted: false,
      rewardBadgeKey: badgeKey,
      rewardBadgeTitle: badgeTitle,
      rewardBadgeEmoji: badgeEmoji,
      rewardCredits: credits,
      rewardXp: xp,
      whatsappMessage: whatsappMessage,
      createdByTeacher: currentUser ? currentUser.name : 'Docente Titular',
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    closeModal('modal-add-obligation');
    showToast("📋 Deber asignado y programado con recordatorios WhatsApp");
  } catch (err) {
    alert("Error al crear deber: " + err.message);
  }
}

async function handleDeleteObligation(oblId, oblTitle) {
  if (confirm(`¿Estás seguro de que deseas eliminar el deber "${oblTitle}"?`)) {
    try {
      await db.collection('parent_obligations').doc(oblId).delete();
      showToast("🗑️ Deber eliminado correctamente");
    } catch (err) {
      alert("Error al eliminar deber: " + err.message);
    }
  }
}

async function handleBroadcastWhatsAppReminder(oblId) {
  const obl = allParentObligations.find(o => o.id === oblId);
  if (!obl) return;

  try {
    await db.collection('parent_obligations').doc(oblId).update({
      lastReminderSentMillis: Date.now()
    });

    // Display WhatsApp banner locally and dispatch notification
    const alertEl = document.getElementById('whatsapp-live-alert');
    const alertText = document.getElementById('whatsapp-alert-text');
    if (alertEl && alertText) {
      alertText.textContent = obl.whatsappMessage || obl.title;
      alertEl.classList.add('show');
      setTimeout(() => alertEl.classList.remove('show'), 6000);
    }

    if ("Notification" in window && Notification.permission === "granted") {
      new Notification("Colegio Escolaris • Tesorería", {
        body: obl.whatsappMessage || obl.title,
        icon: "/icons/icon-192.png"
      });
    }

    showToast("📲 ¡Recordatorio WhatsApp difundido a todos los acudientes!");
  } catch (err) {
    alert("Error al difundir recordatorio: " + err.message);
  }
}

// Automatic Monthly WhatsApp Pension Reminder Engine (Solo el día 1 de cada nuevo mes)
function checkPensionReminder() {
  if (!currentUser || currentUser.role !== 'PARENT') return;

  const today = new Date();
  const dayOfMonth = today.getDate();

  // Regla: la app solo envía un mensaje el 1 de cada nuevo mes avisando que la pensión se paga los primeros 5 días
  if (dayOfMonth !== 1) return;

  const months = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
  const monthName = months[today.getMonth()];
  const msg = `¡Hola estimado acudiente! 👋 Les recordamos que la pensión escolar de ${monthName} se cancela dentro de los primeros 5 días del mes. ¡Gracias por tu puntualidad! ⭐`;

  const alertEl = document.getElementById('whatsapp-live-alert');
  const alertText = document.getElementById('whatsapp-alert-text');
  if (alertEl && alertText) {
    alertText.textContent = msg;
    alertEl.classList.add('show');
    setTimeout(() => alertEl.classList.remove('show'), 7000);
  }

  // Notificación del navegador si está permitida
  if ("Notification" in window && Notification.permission === "granted") {
    try {
      new Notification("Colegio Escolaris • Recordatorio de Pensión", {
        body: msg,
        icon: "/icons/icon-192.png"
      });
    } catch (e) {}
  }
}

function handleWhatsAppLiveAlertClick() {
  const alertEl = document.getElementById('whatsapp-live-alert');
  if (alertEl) alertEl.classList.remove('show');
  
  if (currentUser && currentUser.role === 'PARENT') {
    switchNav('parent-dashboard');
    scrollToObligationsChecklist();
  }
}

function renderParentFamilyBadges() {
  const container = document.getElementById('parent-family-badges-list');
  if (!container || !currentUser) return;

  const linkedCode = currentUser.linkedStudentId || '';
  const child = allUsers.find(u => u.studentCode === linkedCode || u.id === linkedCode);
  const childId = child ? child.id : '';

  const familyBadges = allBadges.filter(b => (b.studentId === childId || b.category === 'FAMILY'));

  if (familyBadges.length === 0) {
    container.innerHTML = `
      <div class="clay-card family-badge-card">
        <div style="display:flex; align-items:center; gap:10px;">
          <span style="font-size:26px;">👨‍👩‍👧</span>
          <div>
            <strong>¡Gana medallas familiares con tu hijo/a!</strong>
            <p style="font-size:11px; color:var(--text-muted); margin-top:2px;">
              Asiste a la primera reunión de padres, colabora en eventos escolares y apoya las tareas en casa para ser condecorados.
            </p>
          </div>
        </div>
      </div>
    `;
    return;
  }

  container.innerHTML = familyBadges.map(b => `
    <div class="clay-card family-badge-card">
      <div style="display:flex; justify-content:space-between; align-items:flex-start;">
        <div style="display:flex; align-items:center; gap:10px;">
          <span style="font-size:26px;">${b.emoji || '👨‍👩‍👧'}</span>
          <div>
            <div style="display:flex; align-items:center; gap:6px;">
              <strong>${b.title}</strong>
              <span class="tag-family">Familia</span>
            </div>
            <p style="font-size:11px; color:var(--text-muted); margin-top:2px;">${b.teacherNote ? `"${b.teacherNote}"` : b.description}</p>
          </div>
        </div>
        <span class="reward-price">+${b.creditReward || 50} 🪙</span>
      </div>
      <div style="font-size:10px; color:var(--text-muted); margin-top:6px;">Otorgado por: ${b.unlockedByTeacher || 'Docente Titular'}</div>
    </div>
  `).join('');
}

function renderParentTasks() {
  const container = document.getElementById('parent-tasks-list');
  if (!container || !currentUser) return;

  const linkedCode = currentUser.linkedStudentId || '';
  const child = allUsers.find(u => u.studentCode === linkedCode || u.id === linkedCode);
  const childId = child ? child.id : '';

  const tasks = userTasks.filter(t => !childId || t.studentId === childId || !t.studentId);

  if (tasks.length === 0) {
    container.innerHTML = `<p style="font-size:12px; color:var(--text-muted);">No hay tareas pendientes registradas para tu hijo/a.</p>`;
    return;
  }

  container.innerHTML = tasks.slice(0, 5).map(t => `
    <div class="clay-card" style="padding:10px 14px;">
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <div>
          <strong>${t.title}</strong>
          <p style="font-size:11px; color:var(--text-muted);">${t.subject} • Entrega: ${t.dueDate || 'Pronto'}</p>
        </div>
        <span class="badge ${t.completed ? 'badge-priority-baja' : 'badge-priority-alta'}">
          ${t.completed ? 'Completada ✅' : 'Pendiente ⏳'}
        </span>
      </div>
    </div>
  `).join('');
}

function renderParentTardies() {
  const container = document.getElementById('parent-tardies-list');
  if (!container || !currentUser) return;

  const linkedCode = currentUser.linkedStudentId || '';
  const child = allUsers.find(u => u.studentCode === linkedCode || u.id === linkedCode);
  const childId = child ? child.id : '';

  const tardies = allTardies.filter(td => !childId || td.studentId === childId);

  if (tardies.length === 0) {
    container.innerHTML = `
      <div style="display:flex; align-items:center; gap:8px; padding:6px 0;">
        <span style="font-size:20px;">✅</span>
        <span style="font-size:12px; color:var(--success); font-weight:700;">Asistencia Impecable (0 llegadas tarde)</span>
      </div>
    `;
    return;
  }

  container.innerHTML = tardies.map(td => `
    <div class="clay-card" style="padding:10px 14px; border-left:4px solid var(--streak-orange);">
      <div style="display:flex; justify-content:space-between;">
        <strong>Llegada Tarde (+${td.delayMinutes || 10} min)</strong>
        <span style="font-size:11px; font-weight:700; color:${td.status === 'JUSTIFICADO' ? 'var(--success)' : 'var(--streak-orange)'};">${td.status || 'REGISTRADO'}</span>
      </div>
      <p style="font-size:11px; color:var(--text-muted); margin-top:2px;">Materia: ${td.subject || 'Clase'} • Motivo: ${td.reason || 'Sin justificar'}</p>
    </div>
  `).join('');
}

async function handleLinkChild(e) {
  e.preventDefault();
  const code = document.getElementById('link-child-input-code').value.trim().toUpperCase();
  if (!code) return;

  try {
    await db.collection('users').doc(currentUser.id).update({
      linkedStudentId: code
    });
    currentUser.linkedStudentId = code;
    closeModal('modal-link-child');
    showToast(`🔗 Vinculado al código: ${code}`);
    renderParentDashboard();
  } catch (err) {
    alert("Error al vincular: " + err.message);
  }
}

async function handleTransferParentPoints(e) {
  e.preventDefault();
  const amount = parseInt(document.getElementById('transfer-points-amount').value, 10);
  const reason = document.getElementById('transfer-points-reason').value.trim();

  const linkedCode = currentUser.linkedStudentId || '';
  const child = allUsers.find(u => u.studentCode === linkedCode || u.id === linkedCode);

  if (!child) {
    alert("Primero debes vincular el código de tu hijo/a.");
    return;
  }

  try {
    await db.collection('users').doc(child.id).update({
      credits: (child.credits || 0) + amount,
      xp: (child.xp || 0) + (amount * 2)
    });
    closeModal('modal-transfer-parent-points');
    showToast(`🪙 ¡Transferiste +${amount} créditos a ${child.name}! Motivo: ${reason}`);
  } catch (err) {
    alert("Error al transferir puntos: " + err.message);
  }
}

// 10. Comprehensive School / Teacher Admin Dashboard Logic
let adminUserSearchQuery = '';
let adminUserRoleFilter = 'ALL';
let adminPassSearchQuery = '';

function switchAdminTab(subtab) {
  activeAdminTab = subtab;
  ['users', 'obligations', 'badges', 'passes', 'tardies', 'penalties', 'grades'].forEach(tab => {
    const btn = document.getElementById(`btn-admin-tab-${tab}`);
    const section = document.getElementById(`admin-subtab-${tab}`);
    if (btn && section) {
      if (tab === subtab) {
        btn.className = 'btn-primary btn-filter active';
        section.classList.remove('hidden');
      } else {
        btn.className = 'btn-secondary btn-filter';
        section.classList.add('hidden');
      }
    }
  });

  if (subtab === 'users') renderAdminUsers();
  if (subtab === 'obligations') renderAdminObligations();
  if (subtab === 'badges') renderAdminBadges();
  if (subtab === 'passes') renderAdminPasses();
  if (subtab === 'tardies') renderAdminTardies();
  if (subtab === 'penalties') renderAdminPenalties();
  if (subtab === 'grades') renderAdminGrades();
}

function handleAdminUserSearch(query) {
  adminUserSearchQuery = query.toLowerCase().trim();
  renderAdminUsers();
}

function filterAdminUserRole(role) {
  adminUserRoleFilter = role;
  document.querySelectorAll('.admin-role-filter').forEach(btn => {
    if (btn.getAttribute('data-role') === role) {
      btn.className = 'btn-primary btn-sm admin-role-filter active';
    } else {
      btn.className = 'btn-secondary btn-sm admin-role-filter';
    }
  });
  renderAdminUsers();
}

function handleAdminPassSearch(query) {
  adminPassSearchQuery = query.toLowerCase().trim();
  renderAdminPasses();
}

function renderAdminMetrics() {
  const students = allUsers.filter(u => u.role === 'STUDENT' || !u.role);
  const teachers = allUsers.filter(u => u.role === 'TEACHER');
  const parents = allUsers.filter(u => u.role === 'PARENT');
  const activeBadges = allBadges.filter(b => b.status !== 'REJECTED');
  const redemptions = userRedemptions;

  const elStudents = document.getElementById('metric-students-count');
  const elTeachers = document.getElementById('metric-teachers-count');
  const elParents = document.getElementById('metric-parents-count');
  const elBadges = document.getElementById('metric-badges-count');
  const elPasses = document.getElementById('metric-passes-count');

  if (elStudents) elStudents.textContent = students.length;
  if (elTeachers) elTeachers.textContent = teachers.length;
  if (elParents) elParents.textContent = parents.length;
  if (elBadges) elBadges.textContent = activeBadges.length;
  if (elPasses) elPasses.textContent = redemptions.length;
}

function renderAdminUsers() {
  const container = document.getElementById('admin-users-list');
  if (!container) return;

  renderAdminMetrics();

  let filtered = allUsers;
  if (adminUserRoleFilter !== 'ALL') {
    filtered = filtered.filter(u => (u.role || 'STUDENT') === adminUserRoleFilter);
  }

  if (adminUserSearchQuery) {
    filtered = filtered.filter(u => {
      const name = (u.name || '').toLowerCase();
      const email = (u.email || '').toLowerCase();
      const grade = (u.gradeSection || u.teacherSubject || '').toLowerCase();
      const code = (u.studentCode || u.teacherCode || '').toLowerCase();
      return name.includes(adminUserSearchQuery) || email.includes(adminUserSearchQuery) || grade.includes(adminUserSearchQuery) || code.includes(adminUserSearchQuery);
    });
  }

  if (filtered.length === 0) {
    container.innerHTML = `
      <div class="clay-card" style="text-align:center; padding:24px;">
        <span style="font-size:32px;">👥</span>
        <p style="font-size:13px; color:var(--text-muted); margin-top:8px;">No se encontraron personas con ese criterio de búsqueda.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(u => {
    const role = u.role || 'STUDENT';
    const roleBadge = role === 'TEACHER' ? `<span class="badge" style="background:#d1fae5; color:#065f46;">👨‍🏫 Docente</span>` : (role === 'PARENT' ? `<span class="badge" style="background:#ede9fe; color:#5b21b6;">👨‍👩‍👧 Acudiente</span>` : `<span class="badge badge-priority-media">🎓 Estudiante</span>`);
    const avatar = u.photoUri ? `<img src="${u.photoUri}" alt="${u.name}" class="avatar-img-fit">` : (u.avatarEmoji || (role === 'TEACHER' ? '👨‍🏫' : (role === 'PARENT' ? '👨‍👩‍👧' : '🎓')));
    const code = role === 'TEACHER' ? (u.teacherCode || 'DOC-...') : (u.studentCode || 'ESC-...');
    
    let extraInfo = '';
    let linkParentBtn = '';
    if (role === 'TEACHER') {
      extraInfo = u.teacherSubject || 'Docente Titular';
    } else if (role === 'PARENT') {
      const child = allUsers.find(s => s.id === u.linkedStudentId || (s.studentCode && s.studentCode === u.linkedStudentId));
      if (child) {
        extraInfo = `<strong style="color:var(--primary);">🎓 Hijo/a: ${child.name} (${child.gradeSection || '10° Grado'})</strong>`;
      } else if (u.linkedStudentId) {
        extraInfo = `<span>Hijo enlazado: ${u.linkedStudentId}</span>`;
      } else {
        extraInfo = `<span style="color:#ef4444; font-weight:800;">⚠️ Sin hijo vinculado</span>`;
      }
      linkParentBtn = `<button class="btn-primary btn-sm" style="background:#2563eb; color:#fff;" onclick="openLinkParentModal('${u.id}')" title="Vincular con estudiante">🔗 Vincular Hijo</button>`;
    } else {
      extraInfo = `${u.gradeSection || '10° Grado'}${u.linkedTeacherCode ? ` • Aula: ${u.linkedTeacherCode}` : ''}`;
    }

    return `
      <div class="admin-user-card">
        <div class="admin-user-left">
          <div class="admin-user-avatar">${avatar}</div>
          <div class="admin-user-details">
            <div class="admin-user-name">
              <span>${u.name || 'Sin Nombre'}</span>
              ${roleBadge}
            </div>
            <div class="admin-user-meta">
              <span>📧 ${u.email || 'Sin correo'}</span>
              <span>• ${extraInfo}</span>
              <span class="admin-user-code-pill">${code}</span>
            </div>
          </div>
        </div>

        <div style="display:flex; align-items:center; gap:12px; flex-wrap:wrap;">
          <div style="text-align:right;">
            <div style="font-size:13px; font-weight:800; color:var(--coin-gold);">🪙 ${u.credits || 0}</div>
            <div style="font-size:11px; font-weight:700; color:var(--primary);">⚡ ${u.xp || 0} XP (Nv. ${Math.floor((u.xp || 0) / 300) + 1})</div>
          </div>

          <div class="admin-user-actions">
            ${linkParentBtn}
            <button class="btn-secondary btn-sm" onclick="openEditUserModal('${u.id}')" title="Editar datos">✏️ Editar</button>
            <button class="btn-secondary btn-sm" onclick="openAdjustPointsModal('${u.id}')" title="Ajustar puntos">🪙 Puntos</button>
            ${u.id !== currentUser.id ? `
              <button class="btn-secondary btn-sm btn-danger-outline" onclick="handleDeleteUser('${u.id}', '${u.name}')" title="Eliminar usuario">🗑️</button>
            ` : ''}
          </div>
        </div>
      </div>
    `;
  }).join('');
}

// User CRUD & Parent Linking Helpers
function populateStudentSelect(selectId, selectedValue = '') {
  const select = document.getElementById(selectId);
  if (!select) return;
  const students = allUsers
    .filter(u => (u.role || 'STUDENT') === 'STUDENT')
    .sort((a, b) => (a.name || '').localeCompare(b.name || ''));

  select.innerHTML = '<option value="">-- Sin vincular / Ninguno --</option>' +
    students.map(s => {
      const isSelected = (s.id === selectedValue || (s.studentCode && s.studentCode === selectedValue));
      return `<option value="${s.id}" ${isSelected ? 'selected' : ''}>${s.name} • ${s.gradeSection || '10° Grado'} (${s.studentCode || s.id})</option>`;
    }).join('');
}

function toggleAddUserRoleFields(role) {
  const gradeGroup = document.getElementById('add-user-grade-group');
  const subjectGroup = document.getElementById('add-user-subject-group');
  const childGroup = document.getElementById('add-user-childcode-group');

  if (gradeGroup) gradeGroup.classList.toggle('hidden', role !== 'STUDENT');
  if (subjectGroup) subjectGroup.classList.toggle('hidden', role !== 'TEACHER');
  if (childGroup) {
    childGroup.classList.toggle('hidden', role !== 'PARENT');
    if (role === 'PARENT') populateStudentSelect('add-user-child-select');
  }
}

function toggleEditUserRoleFields(role) {
  const gradeGroup = document.getElementById('edit-user-grade-group');
  const subjectGroup = document.getElementById('edit-user-subject-group');
  const childGroup = document.getElementById('edit-user-child-group');

  if (gradeGroup) gradeGroup.classList.toggle('hidden', role !== 'STUDENT');
  if (subjectGroup) subjectGroup.classList.toggle('hidden', role !== 'TEACHER');
  if (childGroup) {
    childGroup.classList.toggle('hidden', role !== 'PARENT');
    if (role === 'PARENT') {
      const user = allUsers.find(u => u.id === document.getElementById('edit-user-id')?.value);
      populateStudentSelect('edit-user-child-select', user?.linkedStudentId || '');
    }
  }
}

function openLinkParentModal(parentId) {
  const parent = allUsers.find(u => u.id === parentId);
  if (!parent) return;

  const idInput = document.getElementById('link-parent-id');
  const nameDisplay = document.getElementById('link-parent-name-display');

  if (idInput) idInput.value = parent.id;
  if (nameDisplay) nameDisplay.value = `${parent.name} (${parent.email || 'Sin correo'})`;

  populateStudentSelect('link-parent-student-select', parent.linkedStudentId || '');
  openModal('modal-link-parent');
}

async function handleSaveParentLink(e) {
  e.preventDefault();
  const parentId = document.getElementById('link-parent-id').value;
  const select = document.getElementById('link-parent-student-select');
  const selectedStudentId = select ? select.value.trim() : '';

  if (!parentId) return;

  try {
    showToast("Guardando vinculación...");
    await db.collection('users').doc(parentId).update({
      linkedStudentId: selectedStudentId || null,
      updatedAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    const parent = allUsers.find(u => u.id === parentId);
    if (parent) parent.linkedStudentId = selectedStudentId || null;
    if (currentUser && currentUser.id === parentId) currentUser.linkedStudentId = selectedStudentId || null;

    closeModal('modal-link-parent');
    renderAdminUsers();

    const student = allUsers.find(u => u.id === selectedStudentId);
    if (student) {
      showToast(`🎉 ¡${parent ? parent.name : 'Acudiente'} vinculado exitosamente con ${student.name}!`);
    } else {
      showToast("ℹ️ Acudiente desvinculado con éxito");
    }
  } catch (err) {
    alert("Error al vincular: " + err.message);
  }
}

async function handleUnlinkParent() {
  const parentId = document.getElementById('link-parent-id').value;
  if (!parentId) return;
  if (!confirm("¿Deseas desvincular a este acudiente del estudiante actual?")) return;

  try {
    showToast("Desvinculando acudiente...");
    await db.collection('users').doc(parentId).update({
      linkedStudentId: null,
      updatedAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    const parent = allUsers.find(u => u.id === parentId);
    if (parent) parent.linkedStudentId = null;
    if (currentUser && currentUser.id === parentId) currentUser.linkedStudentId = null;

    closeModal('modal-link-parent');
    renderAdminUsers();
    showToast("ℹ️ Acudiente desvinculado correctamente.");
  } catch (err) {
    alert("Error al desvincular: " + err.message);
  }
}

async function handleCreateUser(e) {
  e.preventDefault();
  const name = document.getElementById('add-user-name').value.trim();
  const email = document.getElementById('add-user-email').value.trim();
  const role = document.getElementById('add-user-role').value;
  const grade = document.getElementById('add-user-grade').value;
  const subject = document.getElementById('add-user-subject') ? document.getElementById('add-user-subject').value.trim() : '';
  const childSelect = document.getElementById('add-user-child-select');
  const childCode = childSelect ? childSelect.value.trim() : '';
  const credits = parseInt(document.getElementById('add-user-credits').value, 10) || 0;
  const xp = parseInt(document.getElementById('add-user-xp').value, 10) || 0;

  if (!name || !email) {
    alert("Por favor completa los campos obligatorios.");
    return;
  }

  try {
    const newId = 'usr_' + Math.random().toString(36).substring(2, 10);
    const newUser = {
      name: name,
      email: email,
      role: role,
      gradeSection: role === 'STUDENT' ? grade : (role === 'TEACHER' ? (subject || 'Docente Titular') : 'Familiar'),
      teacherSubject: role === 'TEACHER' ? subject : '',
      linkedStudentId: role === 'PARENT' ? (childCode || null) : '',
      credits: credits,
      xp: xp,
      streakDays: 1,
      studentCode: role === 'STUDENT' ? ('ESC-' + Math.random().toString(36).substring(2, 8).toUpperCase()) : '',
      teacherCode: role === 'TEACHER' ? ('DOC-' + Math.random().toString(36).substring(2, 8).toUpperCase()) : '',
      avatarEmoji: role === 'TEACHER' ? '👨‍🏫' : (role === 'PARENT' ? '👨‍👩‍👧' : '🎓'),
      avatarColorHex: 0xFF2563EB,
      bio: role === 'TEACHER' ? 'Docente en Escolaris 👨‍🏫' : (role === 'PARENT' ? 'Acudiente en Escolaris 👨‍👩‍👧' : 'Estudiante en Escolaris 🚀'),
      roleConfigured: true,
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    };

    await db.collection('users').doc(newId).set(newUser);
    closeModal('modal-add-user');
    showToast(`🎉 ¡Usuario ${name} registrado con éxito!`);
  } catch (err) {
    alert("Error al agregar usuario: " + err.message);
  }
}

function openEditUserModal(userId) {
  const user = allUsers.find(u => u.id === userId);
  if (!user) return;

  document.getElementById('edit-user-id').value = user.id;
  document.getElementById('edit-user-name').value = user.name || '';
  document.getElementById('edit-user-email').value = user.email || '';
  document.getElementById('edit-user-role').value = user.role || 'STUDENT';
  document.getElementById('edit-user-grade').value = user.gradeSection || '10° Grado';
  document.getElementById('edit-user-subject').value = user.teacherSubject || '';
  document.getElementById('edit-user-credits').value = user.credits || 0;
  document.getElementById('edit-user-xp').value = user.xp || 0;
  document.getElementById('edit-user-code').value = user.role === 'TEACHER' ? (user.teacherCode || '') : (user.studentCode || '');

  populateStudentSelect('edit-user-child-select', user.linkedStudentId || '');
  toggleEditUserRoleFields(user.role || 'STUDENT');
  openModal('modal-edit-user');
}

async function handleUpdateUser(e) {
  e.preventDefault();
  const id = document.getElementById('edit-user-id').value;
  const name = document.getElementById('edit-user-name').value.trim();
  const email = document.getElementById('edit-user-email').value.trim().toLowerCase();
  const role = document.getElementById('edit-user-role').value;
  const grade = document.getElementById('edit-user-grade').value.trim();
  const subject = document.getElementById('edit-user-subject').value.trim();
  const childSelect = document.getElementById('edit-user-child-select');
  const childId = childSelect ? childSelect.value.trim() : '';
  const credits = parseInt(document.getElementById('edit-user-credits').value, 10) || 0;
  const xp = parseInt(document.getElementById('edit-user-xp').value, 10) || 0;
  const code = document.getElementById('edit-user-code').value.trim().toUpperCase();

  try {
    const updates = {
      name: name,
      email: email,
      role: role,
      gradeSection: role === 'STUDENT' ? grade : (role === 'TEACHER' ? subject : 'Familiar'),
      teacherSubject: role === 'TEACHER' ? subject : '',
      credits: credits,
      xp: xp,
      updatedAt: firebase.firestore.FieldValue.serverTimestamp()
    };

    if (role === 'PARENT') {
      updates.linkedStudentId = childId || null;
    } else {
      updates.linkedStudentId = null;
    }
    if (role === 'TEACHER' && code) updates.teacherCode = code;
    if (role === 'STUDENT' && code) updates.studentCode = code;

    await db.collection('users').doc(id).update(updates);
    closeModal('modal-edit-user');
    showToast("💾 Datos de usuario actualizados correctamente");
  } catch (err) {
    alert("Error al actualizar usuario: " + err.message);
  }
}

async function handleDeleteUser(userId, userName) {
  if (!confirm(`⚠️ ¿Estás seguro de que deseas eliminar permanentemente a "${userName}" de la plataforma Escolaris?\n\nEsta acción no se puede deshacer y borrará sus registros de la nube.`)) {
    return;
  }
  try {
    // 1. Eliminar subcolecciones asociadas si existen (user_badges, notifications, badges)
    const subcollections = ['user_badges', 'notifications', 'badges'];
    for (const sub of subcollections) {
      try {
        const subSnap = await db.collection('users').doc(userId).collection(sub).get();
        if (!subSnap.empty) {
          const batch = db.batch();
          subSnap.forEach(d => batch.delete(d.ref));
          await batch.commit();
        }
      } catch (subErr) {
        console.warn(`Aviso al limpiar subcolección ${sub}:`, subErr);
      }
    }

    // 2. Eliminar notificaciones en la colección raíz asociadas a este usuario
    try {
      const notifsSnap = await db.collection('notifications').where('studentId', '==', userId).get();
      if (!notifsSnap.empty) {
        const batch = db.batch();
        notifsSnap.forEach(d => batch.delete(d.ref));
        await batch.commit();
      }
    } catch (nErr) {
      console.warn("Aviso al limpiar notificaciones del usuario:", nErr);
    }

    // 3. Desvincular referencias de acudiente o hijo si existen
    try {
      const linkedParents = allUsers.filter(u => u.linkedStudentId === userId);
      for (const p of linkedParents) {
        await db.collection('users').doc(p.id).update({
          linkedStudentId: firebase.firestore.FieldValue.delete()
        });
      }
    } catch (linkErr) {
      console.warn("Aviso al desvincular estudiante de acudientes:", linkErr);
    }

    // 4. Ejecutar borrado real y definitivo en Cloud Firestore
    await db.collection('users').doc(userId).delete();

    // 5. Actualización optimista de estado local en memoria
    allUsers = allUsers.filter(u => u.id !== userId);
    renderAdminUsers();
    renderLeaderboard();
    populateStudentSelects();

    showToast(`🗑️ Usuario "${userName}" eliminado permanentemente de la nube.`);
  } catch (err) {
    console.error("Error al eliminar usuario en Firestore:", err);
    alert("❌ Error de permisos o conectividad al eliminar usuario:\n" + (err.message || err));
  }
}

function openAdjustPointsModal(userId) {
  const user = allUsers.find(u => u.id === userId);
  if (!user) return;

  document.getElementById('adjust-points-user-id').value = user.id;
  document.getElementById('adjust-points-user-label').textContent = `Usuario: ${user.name} (${user.role || 'Estudiante'}) • Saldo actual: 🪙 ${user.credits || 0} | ⚡ ${user.xp || 0} XP`;
  openModal('modal-adjust-points');
}

async function handleSaveAdjustPoints(e) {
  e.preventDefault();
  const userId = document.getElementById('adjust-points-user-id').value;
  const action = document.getElementById('adjust-points-action').value;
  const amount = parseInt(document.getElementById('adjust-points-amount').value, 10) || 0;
  const reason = document.getElementById('adjust-points-reason').value.trim();

  const user = allUsers.find(u => u.id === userId);
  if (!user) return;

  const diff = action === 'ADD' ? amount : -amount;
  const newCredits = Math.max(0, (user.credits || 0) + diff);
  const newXp = action === 'ADD' ? (user.xp || 0) + (amount * 2) : (user.xp || 0);

  try {
    await db.collection('users').doc(userId).update({
      credits: newCredits,
      xp: newXp
    });
    closeModal('modal-adjust-points');
    showToast(`🪙 Saldo actualizado para ${user.name}: ${newCredits} créditos.`);
  } catch (err) {
    alert("Error al ajustar puntos: " + err.message);
  }
}

// Badges Approval & Revocation Logic
function renderAdminBadges() {
  const container = document.getElementById('admin-badges-list');
  const pendingContainer = document.getElementById('admin-pending-badges-list');

  // 1. Pending Approval Requests
  if (pendingContainer) {
    const pendingBadges = allBadges.filter(b => b.status === 'PENDING');
    if (pendingBadges.length === 0) {
      pendingContainer.innerHTML = `<p style="font-size:12px; color:var(--text-muted); text-align:center; padding:10px;">No hay solicitudes de logros pendientes por aprobar. ✅</p>`;
    } else {
      pendingContainer.innerHTML = pendingBadges.map(b => `
        <div class="clay-card admin-badge-req-card">
          <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:10px; flex-wrap:wrap;">
            <div style="display:flex; align-items:center; gap:10px;">
              <span style="font-size:28px;">${b.emoji || '🎖️'}</span>
              <div>
                <strong>${b.title}</strong>
                <p style="font-size:12px; color:var(--text-muted); margin-top:2px;">
                  Solicitado para: <strong>${b.studentName || 'Estudiante'}</strong> • ${b.description}
                </p>
                <span class="badge ${b.category === 'FAMILY' ? 'tag-family' : 'badge-priority-media'}">${b.category === 'FAMILY' ? 'Logro Familiar 👨‍👩‍👧' : 'Mérito Escolar 🎓'}</span>
              </div>
            </div>
            <div style="display:flex; gap:6px; align-items:center;">
              <span class="reward-price" style="margin-right:4px;">+${b.creditReward || 50} 🪙</span>
              <button class="btn-primary btn-sm" onclick="approveBadge('${b.id}')">Aprobar ✅</button>
              <button class="btn-secondary btn-sm btn-danger-outline" onclick="rejectBadge('${b.id}')">Rechazar ✕</button>
            </div>
          </div>
        </div>
      `).join('');
    }
  }

  // 2. Active Granted Badges History
  if (container) {
    const activeBadges = allBadges.filter(b => b.status !== 'PENDING' && b.status !== 'REJECTED');
    if (activeBadges.length === 0) {
      container.innerHTML = `<p style="font-size:12px; color:var(--text-muted); text-align:center; padding:16px;">Aún no se han otorgado logros oficiales.</p>`;
      return;
    }

    container.innerHTML = activeBadges.map(b => `
      <div class="clay-card ${b.category === 'FAMILY' ? 'family-badge-card' : ''}" style="padding:12px 14px;">
        <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
          <div style="display:flex; align-items:center; gap:10px;">
            <span style="font-size:26px;">${b.emoji || '🎖️'}</span>
            <div>
              <strong>${b.title}</strong> — <span style="color:var(--primary); font-weight:700;">${b.studentName || 'Estudiante'}</span>
              <p style="font-size:11px; color:var(--text-muted); margin-top:2px;">${b.teacherNote ? `"${b.teacherNote}"` : b.description}</p>
            </div>
          </div>
          <div style="display:flex; align-items:center; gap:8px;">
            <span class="badge ${b.category === 'FAMILY' ? 'tag-family' : 'badge-priority-media'}">${b.category === 'FAMILY' ? 'Familia 👨‍👩‍👧' : 'Honor 🎓'}</span>
            <span class="reward-price">+${b.creditReward || 50} 🪙</span>
            <button class="btn-secondary btn-sm btn-danger-outline" onclick="revokeBadge('${b.id}')" title="Revocar logro y restar puntos">Revocar 🗑️</button>
          </div>
        </div>
      </div>
    `).join('');
  }
}

async function approveBadge(badgeId) {
  const badge = allBadges.find(b => b.id === badgeId);
  if (!badge) return;

  try {
    await db.collection('badges').doc(badgeId).update({
      status: 'ACTIVE',
      approvedAt: firebase.firestore.FieldValue.serverTimestamp(),
      unlockedByTeacher: currentUser.name || 'Docente Titular'
    });

    // Award credits & XP to student
    if (badge.studentId) {
      const student = allUsers.find(u => u.id === badge.studentId);
      if (student) {
        const reward = badge.creditReward || 50;
        await db.collection('users').doc(badge.studentId).update({
          credits: (student.credits || 0) + reward,
          xp: (student.xp || 0) + (reward * 2)
        });
      }
    }

    showToast(`🎖️ ¡Logro "${badge.title}" aprobado con éxito!`);
  } catch (err) {
    alert("Error al aprobar logro: " + err.message);
  }
}

async function rejectBadge(badgeId) {
  if (confirm("¿Deseas rechazar esta solicitud de logro?")) {
    try {
      await db.collection('badges').doc(badgeId).update({
        status: 'REJECTED'
      });
      showToast("Solicitud rechazada.");
    } catch (err) {
      alert("Error: " + err.message);
    }
  }
}

async function revokeBadge(badgeId) {
  const badge = allBadges.find(b => b.id === badgeId);
  if (!badge) return;

  if (confirm(`⚠️ ¿Deseas revocar el logro "${badge.title}" de ${badge.studentName || 'Estudiante'}?\n\nSe restarán los ${badge.creditReward || 50} créditos otorgados.`)) {
    try {
      if (badge.studentId) {
        const student = allUsers.find(u => u.id === badge.studentId);
        if (student) {
          const reward = badge.creditReward || 50;
          await db.collection('users').doc(badge.studentId).update({
            credits: Math.max(0, (student.credits || 0) - reward),
            xp: Math.max(0, (student.xp || 0) - (reward * 2))
          });
        }
      }

      await db.collection('badges').doc(badgeId).delete();
      showToast("🗑️ Logro revocado correctamente.");
    } catch (err) {
      alert("Error al revocar logro: " + err.message);
    }
  }
}

function renderAdminPasses() {
  const container = document.getElementById('admin-passes-list');
  if (!container) return;

  let passes = userRedemptions;
  if (adminPassSearchQuery) {
    passes = passes.filter(p => (p.voucherCode || '').toLowerCase().includes(adminPassSearchQuery) || (p.studentName || '').toLowerCase().includes(adminPassSearchQuery));
  }

  if (passes.length === 0) {
    container.innerHTML = `<p style="font-size:12px; color:var(--text-muted); text-align:center; padding:16px;">No hay solicitudes ni canjes registrados.</p>`;
    return;
  }

  const pending = passes.filter(p => p.status === 'PENDING_APPROVAL');
  const history = passes.filter(p => p.status !== 'PENDING_APPROVAL');

  let html = '';

  // 1. Solicitudes Pendientes de Aprobación
  if (pending.length > 0) {
    html += `
      <div style="margin-bottom:14px;">
        <div style="font-size:12px; font-weight:800; color:#b45309; margin-bottom:6px; display:flex; align-items:center; gap:6px;">
          <span>⏳</span> Solicitudes Pendientes de Aprobación (${pending.length})
        </div>
        <div class="cards-column" style="gap:8px;">
          ${pending.map(p => `
            <div class="clay-card" style="padding:12px 14px; border-left:4px solid #f59e0b;">
              <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
                <div>
                  <strong style="font-size:13.5px; color:var(--text-main);">${escapeHtml(p.rewardTitle || 'Pase Escolar')}</strong>
                  <p style="font-size:11.5px; color:var(--text-muted); margin:2px 0 0 0;">
                    Estudiante: <strong>${escapeHtml(p.studentName || 'Alumno')}</strong> • Código: <span class="admin-user-code-pill">${p.voucherCode || 'ESC-000'}</span> • Valor: <strong style="color:#b45309;">🪙 ${p.costCredits || 0}</strong>
                  </p>
                </div>
                <div style="display:flex; gap:6px; align-items:center;">
                  <button class="btn-primary btn-sm" style="background:#059669; font-weight:800;" onclick="approveStudentCanje('${p.id}')">
                    ✅ Aceptar Canje
                  </button>
                  <button class="btn-secondary btn-sm btn-danger-outline" onclick="rejectStudentCanje('${p.id}')">
                    ❌ Rechazar
                  </button>
                </div>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    `;
  }

  // 2. Historial de Canjes Procesados
  if (history.length > 0) {
    html += `
      <div>
        <div style="font-size:12px; font-weight:800; color:var(--text-muted); margin-bottom:6px;">
          Historial de Canjes Aprobados y Procesados (${history.length})
        </div>
        <div class="cards-column" style="gap:8px;">
          ${history.map(p => {
            let statusPill = '';
            let actionBtn = '';
            if (p.status === 'APPROVED' || p.status === 'ACTIVE') {
              statusPill = `<span class="badge" style="background:#dcfce7; color:#15803d;">Aprobado / Listo para Usar 🎟️</span>`;
              actionBtn = `<button class="btn-secondary btn-sm" onclick="markCanjeUsed('${p.id}')">Marcar Utilizado ✓</button>`;
            } else if (p.status === 'USED') {
              statusPill = `<span class="badge" style="background:#e5e7eb; color:#4b5563;">Canjeado / Utilizado ✅</span>`;
            } else if (p.status === 'REJECTED') {
              statusPill = `<span class="badge" style="background:#fee2e2; color:#b91c1c;">Rechazado (Créditos Reembolsados) ↩️</span>`;
            }

            return `
              <div class="clay-card" style="padding:12px 14px;">
                <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
                  <div>
                    <strong>${escapeHtml(p.rewardTitle || 'Pase Escolar')}</strong>
                    <p style="font-size:11px; color:var(--text-muted); margin:2px 0 0 0;">
                      Estudiante: <strong>${escapeHtml(p.studentName || 'Alumno')}</strong> • Código: <span class="admin-user-code-pill">${p.voucherCode || 'ESC-000'}</span>
                    </p>
                  </div>
                  <div style="display:flex; align-items:center; gap:8px;">
                    ${statusPill}
                    ${actionBtn}
                  </div>
                </div>
              </div>
            `;
          }).join('')}
        </div>
      </div>
    `;
  }

  container.innerHTML = html;
}

async function approveStudentCanje(passId) {
  try {
    await db.collection('redemptions').doc(passId).update({
      status: 'APPROVED',
      approvedByTeacher: currentUser ? currentUser.name : 'Docente Titular',
      approvedAtMillis: Date.now()
    });
    showToast("✅ Canje aceptado con éxito. Puntos descontados.");
  } catch (err) {
    alert("Error al aprobar canje: " + err.message);
  }
}

async function rejectStudentCanje(passId) {
  const pass = userRedemptions.find(p => p.id === passId);
  if (!pass) return;

  if (confirm(`¿Deseas rechazar la solicitud de canje "${pass.rewardTitle}" de ${pass.studentName}?\n\nSe reembolsarán automáticamente los 🪙 ${pass.costCredits || 0} créditos al estudiante.`)) {
    try {
      // 1. Reembolsar los créditos al estudiante
      if (pass.studentId && pass.costCredits) {
        await db.collection('users').doc(pass.studentId).update({
          credits: firebase.firestore.FieldValue.increment(pass.costCredits)
        });
      }

      // 2. Actualizar estado de la solicitud a REJECTED
      await db.collection('redemptions').doc(passId).update({
        status: 'REJECTED',
        rejectedByTeacher: currentUser ? currentUser.name : 'Docente Titular',
        rejectedAtMillis: Date.now()
      });

      showToast(`↩️ Canje rechazado. Se devolvieron 🪙 ${pass.costCredits} créditos a ${pass.studentName}.`);
    } catch (err) {
      alert("Error al rechazar canje: " + err.message);
    }
  }
}

async function markCanjeUsed(passId) {
  try {
    await db.collection('redemptions').doc(passId).update({
      status: 'USED',
      usedAtMillis: Date.now()
    });
    showToast("🎟️ Canje marcado como utilizado en clase.");
  } catch (err) {
    alert("Error: " + err.message);
  }
}

function renderAdminGrades() {
  const container = document.getElementById('admin-exams-grade-list');
  if (!container) return;
  const exams = userTasks.filter(t => t.type === 'EXAM');
  if (exams.length === 0) {
    container.innerHTML = `<p style="font-size:12px; color:var(--text-muted); text-align:center; padding:16px;">No hay evaluaciones pendientes por calificar.</p>`;
    return;
  }
  container.innerHTML = exams.map(e => `
    <div class="clay-card" style="padding:10px 14px; display:flex; justify-content:space-between; align-items:center;">
      <div>
        <strong>${e.title}</strong>
        <p style="font-size:11px; color:var(--text-muted);">${e.subject} • Fecha: ${e.dueDate || 'Pronto'}</p>
      </div>
      <span class="badge badge-priority-media">Pendiente Calificar</span>
    </div>
  `).join('');
}

// 11. Disciplinary Penalties & Fines Logic (Multas Escolares)
function selectPenaltyPreset(reason, points, btnElement) {
  document.querySelectorAll('.preset-fine-btn').forEach(b => b.classList.remove('selected'));
  if (btnElement) btnElement.classList.add('selected');

  const reasonInput = document.getElementById('penalty-reason-input');
  const pointsInput = document.getElementById('penalty-points-input');
  if (reasonInput) reasonInput.value = reason;
  if (pointsInput) pointsInput.value = points;
}

async function handleCreatePenalty(e) {
  e.preventDefault();
  const studentId = document.getElementById('penalty-student-select').value;
  const reason = document.getElementById('penalty-reason-input').value.trim();
  const points = parseInt(document.getElementById('penalty-points-input').value, 10) || 30;
  const observation = document.getElementById('penalty-observation-input').value.trim();
  const notifyParents = document.getElementById('penalty-notify-parents').checked;

  const student = allUsers.find(u => u.id === studentId);
  if (!student) {
    alert("Por favor selecciona un estudiante válido.");
    return;
  }

  const teacherName = currentUser ? currentUser.name : 'Docente Titular';

  try {
    // 1. Deduct credits from student
    const currentCredits = student.credits || 0;
    const newCredits = Math.max(0, currentCredits - points);
    await db.collection('users').doc(studentId).update({
      credits: newCredits
    });

    // 2. Save penalty record in Firestore
    const penaltyDoc = {
      studentId: studentId,
      studentName: student.name,
      reason: reason,
      pointsDeducted: points,
      teacherName: teacherName,
      observation: observation,
      status: 'APLICADA',
      timestamp: Date.now(),
      createdAt: firebase.firestore.FieldValue.serverTimestamp(),
      notifiedParents: notifyParents
    };
    await db.collection('penalties').add(penaltyDoc);

    // 3. Create notification for parents
    if (notifyParents) {
      const notifDoc = {
        title: `🚨 Sanción Disciplinaria: ${student.name}`,
        message: `El docente ${teacherName} aplicó una sanción de -${points} créditos a ${student.name}. Motivo: ${reason}. ${observation ? `Observación: "${observation}"` : ''}`,
        type: 'PENALTY',
        targetScreen: 'parent-dashboard',
        isRead: false,
        timestamp: Date.now(),
        createdAt: firebase.firestore.FieldValue.serverTimestamp()
      };
      await db.collection('notifications').add(notifDoc);
    }

    closeModal('modal-add-penalty');
    showToast(`🚨 Sanción de -${points} créditos aplicada a ${student.name}`, 'danger');
  } catch (err) {
    alert("Error al registrar sanción: " + err.message);
  }
}

async function handleRevokePenalty(penaltyId) {
  const penalty = allPenalties.find(p => p.id === penaltyId);
  if (!penalty) return;

  if (confirm(`⚠️ ¿Deseas anular la sanción de "${penalty.reason}" y reintegrar +${penalty.pointsDeducted} créditos a ${penalty.studentName}?`)) {
    try {
      const student = allUsers.find(u => u.id === penalty.studentId);
      if (student) {
        await db.collection('users').doc(penalty.studentId).update({
          credits: (student.credits || 0) + penalty.pointsDeducted
        });
      }

      await db.collection('penalties').doc(penaltyId).update({
        status: 'REVOCADA',
        revokedAt: firebase.firestore.FieldValue.serverTimestamp(),
        revokedBy: currentUser ? currentUser.name : 'Docente'
      });

      showToast(`✨ Sanción anulada y +${penalty.pointsDeducted} créditos devueltos a ${penalty.studentName}`);
    } catch (err) {
      alert("Error al anular sanción: " + err.message);
    }
  }
}

function renderAdminTardies() {
  const container = document.getElementById('admin-tardies-list');
  if (!container) return;

  if (!allTardies || allTardies.length === 0) {
    container.innerHTML = `
      <div class="clay-card" style="text-align:center; padding:24px;">
        <span style="font-size:32px;">⏰</span>
        <p style="font-size:13px; color:var(--text-muted); margin-top:8px;">No hay registros de llegadas tarde.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = allTardies.map(td => {
    const isJustified = td.status === 'JUSTIFICADO';
    const dateStr = td.dateMillis ? new Date(td.dateMillis).toLocaleDateString('es-CO', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : (td.arrivalTime || 'Hoy');
    return `
      <div class="clay-card card-actionable" style="display:flex; justify-content:space-between; align-items:center; padding:12px 16px; margin-bottom:8px; border-left: 4px solid ${isJustified ? 'var(--success)' : 'var(--streak-orange)'};">
        <div>
          <div style="font-weight:800; font-size:14px; color:var(--text-main);">${escapeHtml(td.studentName || 'Estudiante')}</div>
          <div style="font-size:12px; color:var(--text-muted); margin-top:2px;">
            <span>${escapeHtml(td.subject || 'Clase')}</span> • <span>${dateStr}</span> • <span style="color:${isJustified ? 'var(--success)' : '#ef4444'}; font-weight:700;">+${td.delayMinutes || 15} min</span>
          </div>
          ${td.reason ? `<div style="font-size:11px; color:var(--text-muted); font-style:italic; margin-top:2px;">Motivo: ${escapeHtml(td.reason)}</div>` : ''}
        </div>
        <div style="display:flex; align-items:center; gap:8px;">
          <span class="badge" style="background:${isJustified ? '#dcfce7' : '#fee2e2'}; color:${isJustified ? '#166534' : '#991b1b'}; font-weight:800; font-size:11px;">${td.status || 'REGISTRADO'}</span>
          ${!isJustified ? `<button class="btn-secondary btn-sm" onclick="justifyTardy('${td.id}')" style="font-size:11px; padding:4px 8px;">Justificar</button>` : ''}
        </div>
      </div>
    `;
  }).join('');
}

async function justifyTardy(tardyId) {
  try {
    await db.collection('tardies').doc(tardyId).update({
      status: 'JUSTIFICADO',
      justifiedAt: firebase.firestore.FieldValue.serverTimestamp()
    });
    showToast("✅ Retardo marcado como justificado");
  } catch (err) {
    alert("Error al justificar retardo: " + err.message);
  }
}

function renderAdminPenalties() {
  const container = document.getElementById('admin-penalties-list');
  if (!container) return;

  if (allPenalties.length === 0) {
    container.innerHTML = `
      <div class="clay-card" style="text-align:center; padding:24px;">
        <span style="font-size:32px;">🕊️</span>
        <p style="font-size:13px; color:var(--text-muted); margin-top:8px;">No hay sanciones disciplinarias ni multas registradas en la institución.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = allPenalties.map(p => {
    const isRevoked = p.status === 'REVOCADA';
    const dateStr = p.timestamp ? new Date(p.timestamp).toLocaleDateString('es-CO', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : 'Reciente';

    return `
      <div class="penalty-card ${isRevoked ? 'revoked' : ''}">
        <div style="flex:1;">
          <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
            <strong style="font-size:14px; color:var(--text-main);">${p.studentName || 'Estudiante'}</strong>
            <span class="penalty-badge-deduct">${isRevoked ? 'Anulada' : `-${p.pointsDeducted} 🪙 créditos`}</span>
            ${p.notifiedParents ? `<span style="font-size:10px; background:#ede9fe; color:#6b21a8; font-weight:800; padding:2px 6px; border-radius:6px;">👨‍👩‍👧 Acudiente Notificado</span>` : ''}
          </div>
          <div style="font-size:13px; font-weight:700; color:var(--danger); margin-top:4px;">
            Falta: ${p.reason}
          </div>
          ${p.observation ? `<div style="font-size:12px; color:var(--text-main); margin-top:2px;">Obs: <em>"${p.observation}"</em></div>` : ''}
          <div style="font-size:11px; color:var(--text-muted); margin-top:6px;">
            Docente: ${p.teacherName || 'Docente'} • ${dateStr}
          </div>
        </div>

        <div>
          ${!isRevoked ? `
            <button class="btn-secondary btn-sm btn-danger-outline" onclick="handleRevokePenalty('${p.id}')" title="Anular sanción y devolver créditos">Anular / Devolver</button>
          ` : `
            <span style="font-size:11px; color:var(--text-muted); font-weight:700;">Reintegrada</span>
          `}
        </div>
      </div>
    `;
  }).join('');
}

function renderParentPenalties() {
  const section = document.getElementById('parent-penalties-section');
  const container = document.getElementById('parent-penalties-list');
  if (!section || !container || !currentUser) return;

  const linkedCode = currentUser.linkedStudentId || '';
  const child = allUsers.find(u => u.studentCode === linkedCode || u.id === linkedCode);
  const childId = child ? child.id : linkedCode;

  const childPenalties = allPenalties.filter(p => p.studentId === childId);

  if (childPenalties.length === 0) {
    section.classList.add('hidden');
    return;
  }

  section.classList.remove('hidden');
  container.innerHTML = childPenalties.map(p => {
    const isRevoked = p.status === 'REVOCADA';
    const dateStr = p.timestamp ? new Date(p.timestamp).toLocaleDateString('es-CO', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : 'Reciente';

    return `
      <div class="clay-card" style="padding:12px; border:1px solid ${isRevoked ? 'var(--border-color)' : 'rgba(239,68,68,0.4)'}; background:var(--surface-color);">
        <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:8px;">
          <div>
            <div style="font-size:13px; font-weight:800; color:${isRevoked ? 'var(--text-muted)' : 'var(--danger)'};">
              ${p.reason}
            </div>
            ${p.observation ? `<div style="font-size:12px; color:var(--text-main); margin-top:2px;">"${p.observation}"</div>` : ''}
            <div style="font-size:11px; color:var(--text-muted); margin-top:4px;">
              Reportado por: ${p.teacherName || 'Docente'} • ${dateStr}
            </div>
          </div>
          <span class="penalty-badge-deduct" style="font-size:11px;">
            ${isRevoked ? 'Sanción Anulada' : `-${p.pointsDeducted} 🪙 Escolaris`}
          </span>
        </div>
      </div>
    `;
  }).join('');
}

function populateStudentSelects() {
  const students = allUsers.filter(u => u.role === 'STUDENT' || !u.role);
  const selects = ['badge-student-select', 'tardy-student-select', 'penalty-student-select'];

  selects.forEach(id => {
    const sel = document.getElementById(id);
    if (sel) {
      sel.innerHTML = students.map(s => `
        <option value="${s.id}" data-name="${s.name}">${s.name} (${s.gradeSection || 'Estudiante'} • Saldo: 🪙 ${s.credits || 0})</option>
      `).join('');
    }
  });

  populateBadgePresetSelect();
}

function selectBadgeType(type) {
  selectedBadgeType = type;
  document.getElementById('badge-type-family').classList.toggle('selected', type === 'FAMILY');
  document.getElementById('badge-type-student').classList.toggle('selected', type === 'HONOR');
  populateBadgePresetSelect();
}

function populateBadgePresetSelect() {
  const sel = document.getElementById('badge-preset-select');
  const noteInput = document.getElementById('badge-note');
  if (!sel) return;

  const presets = selectedBadgeType === 'FAMILY' ? familyBadgePresets : studentBadgePresets;
  const rewardCredits = selectedBadgeType === 'FAMILY' ? 80 : 50;
  const rewardXp = selectedBadgeType === 'FAMILY' ? 150 : 100;

  sel.innerHTML = presets.map(p => `
    <option value="${p.key}">${p.emoji} ${p.title} (+${rewardCredits} 🪙 / +${rewardXp} XP) — ${p.desc}</option>
  `).join('');

  if (noteInput && (!noteInput.value || noteInput.value.startsWith('¡'))) {
    noteInput.value = selectedBadgeType === 'FAMILY' 
      ? '¡Agradecemos a la familia por su valioso compromiso y acompañamiento educativo!' 
      : '¡Felicitaciones por tu esfuerzo, disciplina y excelencia escolar!';
  }
}

async function handleGrantBadge(e) {
  e.preventDefault();
  const studentSelect = document.getElementById('badge-student-select');
  const studentId = studentSelect.value;
  const studentName = studentSelect.options[studentSelect.selectedIndex]?.dataset.name || 'Estudiante';
  const badgeKey = document.getElementById('badge-preset-select').value;
  const note = document.getElementById('badge-note').value.trim();

  const presets = selectedBadgeType === 'FAMILY' ? familyBadgePresets : studentBadgePresets;
  const preset = presets.find(p => p.key === badgeKey) || presets[0];

  try {
    await db.collection('badges').add({
      studentId: studentId,
      studentName: studentName,
      badgeKey: preset.key,
      title: preset.title,
      description: preset.desc,
      emoji: preset.emoji,
      category: selectedBadgeType,
      teacherNote: note || '¡Excelente acompañamiento y compromiso escolar!',
      unlockedByTeacher: currentUser ? currentUser.name : 'Docente Titular',
      creditReward: selectedBadgeType === 'FAMILY' ? 80 : 50,
      xpReward: selectedBadgeType === 'FAMILY' ? 150 : 100,
      unlockedAtMillis: Date.now(),
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    // Reward the student
    const studentDoc = await db.collection('users').doc(studentId).get();
    if (studentDoc.exists) {
      const sData = studentDoc.data();
      await db.collection('users').doc(studentId).update({
        credits: (sData.credits || 0) + (selectedBadgeType === 'FAMILY' ? 80 : 50),
        xp: (sData.xp || 0) + (selectedBadgeType === 'FAMILY' ? 150 : 100)
      });
    }

    closeModal('modal-grant-badge');
    showToast(`🎖️ ¡Logro otorgado a ${studentName}!`);
  } catch (err) {
    alert("Error al otorgar logro: " + err.message);
  }
}

async function handleRegisterTardy(e) {
  e.preventDefault();
  const studentSelect = document.getElementById('tardy-student-select');
  const studentId = studentSelect.value;
  const studentName = studentSelect.options[studentSelect.selectedIndex]?.dataset.name || 'Estudiante';
  const subject = document.getElementById('tardy-subject').value.trim();
  const delay = parseInt(document.getElementById('tardy-delay').value, 10);
  const reason = document.getElementById('tardy-reason').value.trim();

  try {
    await db.collection('tardies').add({
      studentId: studentId,
      studentName: studentName,
      subject: subject,
      delayMinutes: delay,
      reason: reason,
      status: 'REGISTRADO',
      dateMillis: Date.now(),
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    closeModal('modal-register-tardy');
    showToast(`⏰ Llegada tarde registrada para ${studentName}`);
  } catch (err) {
    alert("Error al registrar retardo: " + err.message);
  }
}

function timeAgo(millis) {
  if (!millis) return 'Reciente';
  const num = Number(millis);
  if (isNaN(num) || num <= 0) return 'Reciente';
  const diff = Math.floor((Date.now() - num) / 1000);
  if (diff < 60) return 'Hace un momento';
  if (diff < 3600) return `Hace ${Math.floor(diff / 60)} min`;
  if (diff < 86400) return `Hace ${Math.floor(diff / 3600)} h`;
  if (diff < 604800) return `Hace ${Math.floor(diff / 86400)} d`;
  return new Date(num).toLocaleDateString('es-CO', { day: '2-digit', month: 'short' });
}

// 11. Feed Logic (With Desktop Widget Previews)
function matchesCategory(post, filter) {
  if (!filter || filter === 'all' || filter === 'todos') return true;

  const postCat = (post.category || post.postType || '').trim().toLowerCase();
  const activeFilter = filter.trim().toLowerCase();

  // Mapeo para "Avisos" (contempla sinónimos de Android y Web)
  if (activeFilter === 'avisos' || activeFilter === 'aviso' || activeFilter === 'announcement' || activeFilter === 'general') {
    return (
      postCat === 'avisos' ||
      postCat === 'aviso' ||
      postCat === 'announcement' ||
      postCat === 'general' ||
      postCat === 'comunicado' ||
      postCat.includes('aviso') ||
      postCat.includes('comunicado')
    );
  }

  // Mapeo para "Tareas & Exámenes"
  if (activeFilter.includes('tarea') || activeFilter.includes('examen') || activeFilter === 'academic' || activeFilter === 'task') {
    return (
      postCat.includes('tarea') ||
      postCat.includes('examen') ||
      postCat === 'academic' ||
      postCat === 'task' ||
      postCat === 'study' ||
      postCat.includes('estudio') ||
      postCat === 'late_help' ||
      postCat.includes('apunte')
    );
  }

  return postCat === activeFilter;
}
window.matchesCategory = matchesCategory;

function filterFeed(filter) {
  currentFeedFilter = filter || 'todos';
  const filterNorm = currentFeedFilter.trim().toLowerCase();

  // Actualizar estilos activos de los botones de filtro
  const buttons = document.querySelectorAll('.feed-filter-bar .btn-filter');
  buttons.forEach(btn => {
    btn.classList.remove('btn-primary', 'active');
    btn.classList.add('btn-secondary');
  });

  let targetId = 'btn-feed-filter-todos';
  if (filterNorm === 'avisos' || filterNorm === 'aviso' || filterNorm === 'announcement') {
    targetId = 'btn-feed-filter-avisos';
  } else if (filterNorm.includes('tarea') || filterNorm.includes('examen') || filterNorm === 'academic') {
    targetId = 'btn-feed-filter-tareas';
  }

  const activeBtn = document.getElementById(targetId);
  if (activeBtn) {
    activeBtn.classList.remove('btn-secondary');
    activeBtn.classList.add('btn-primary', 'active');
  }

  renderFeed();
}
window.filterFeed = filterFeed;
window.setFeedFilter = filterFeed;

function renderFeed() {
  const container = document.getElementById('feed-posts-list');
  if (container) {
    if (feedPosts.length === 0) {
      container.innerHTML = `<p style="font-size:12px; color:var(--text-muted); text-align:center; padding:20px;">No hay publicaciones en el muro aún.</p>`;
    } else {
      const filteredPosts = feedPosts.filter(p => matchesCategory(p, currentFeedFilter));

      if (filteredPosts.length === 0) {
        container.innerHTML = `<p style="font-size:12px; color:var(--text-muted); text-align:center; padding:20px;">No hay publicaciones en esta categoría.</p>`;
      } else {
        const isTeacherOrAdmin = currentUser && (
          currentUser.role === 'TEACHER' ||
          currentUser.role === 'DOCENTE' ||
          currentUser.role === 'ADMIN' ||
          currentUser.role === 'SUPERADMIN' ||
          currentUser.email === 'moz658@gmail.com'
        );
        container.innerHTML = filteredPosts.map(p => {
          const canDelete = isTeacherOrAdmin || (currentUser && (currentUser.id === p.authorId || currentUser.uid === p.authorId));
          return `
        <article class="feed-card">
          <!-- Cabecera: Avatar y datos del autor -->
          <header class="feed-card-header">
            <div class="feed-avatar-badge">${p.authorEmoji || '👨‍🏫'}</div>
            <div class="feed-author-meta">
              <div class="feed-author-name-row">
                <h4 class="feed-author-name">${escapeHtml(p.authorName || 'Usuario')}</h4>
                <span class="feed-role-pill ${p.authorRole?.includes('Docente') || p.authorRole?.toUpperCase() === 'TEACHER' ? 'role-teacher' : 'role-student'}">
                  ${escapeHtml(p.authorRole || 'Comunidad')}
                </span>
              </div>
              <span class="feed-time">${timeAgo(p.timestampMillis || p.timestamp)}</span>
            </div>
            <div style="display:flex; align-items:center; gap:8px;">
              <div class="feed-category-tag">${escapeHtml(p.category || p.postType || 'General')}</div>
              ${canDelete ? `<button onclick="handleDeletePost('${p.id}')" title="Eliminar publicación" style="background:none; border:none; cursor:pointer; font-size:15px; opacity:0.6; padding:2px 4px; border-radius:4px;" onmouseover="this.style.opacity='1'" onmouseout="this.style.opacity='0.6'">🗑️</button>` : ''}
            </div>
          </header>

          <!-- Cuerpo: Contenido del mensaje -->
          <div class="feed-card-body">
            <p class="feed-content-text">${escapeHtml(p.content || '')}</p>
          </div>

          <!-- Pie: Interacciones -->
          <footer class="feed-card-footer">
            <button class="feed-like-btn" onclick="handleLikePost('${p.id}')">
              <span class="heart-icon">❤️</span>
              <span class="like-count">${p.likes || p.likesCount || 0}</span>
            </button>
          </footer>
        </article>
      `;
        }).join('');
      }
    }
  }

  // Render Desktop Right Sidebar Widgets
  const tasksWidget = document.getElementById('feed-sidebar-tasks-preview');
  if (tasksWidget) {
    const pending = userTasks.filter(t => !t.completed).slice(0, 3);
    if (pending.length === 0) {
      tasksWidget.innerHTML = `<p style="font-size:11px; color:var(--text-muted);">Sin tareas pendientes hoy 🎉</p>`;
    } else {
      tasksWidget.innerHTML = pending.map(t => `
        <div style="display:flex; justify-content:space-between; align-items:center; padding:6px 0; border-bottom:1px solid var(--border-color); font-size:11px;">
          <span style="font-weight:700; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; max-width:180px;">${escapeHtml(t.title)}</span>
          <span style="color:var(--primary); font-size:10px; font-weight:800;">${t.dueDate || 'Pronto'}</span>
        </div>
      `).join('');
    }
  }

  // Student Family Pension Status Banner
  const studentPensionBanner = document.getElementById('student-family-pension-banner');
  if (studentPensionBanner) {
    const isStudent = !currentUser || currentUser.role === 'STUDENT' || !currentUser.role;
    const completedPension = allParentObligations.find(o => o.category === 'PENSION' && o.isCompleted);
    const pendingPension = allParentObligations.find(o => o.category === 'PENSION' && !o.isCompleted);

    if (isStudent && completedPension) {
      studentPensionBanner.classList.remove('hidden');
      studentPensionBanner.style.background = '#f0fdf4';
      studentPensionBanner.style.borderColor = '#86efac';
      const tEl = document.getElementById('student-family-pension-title');
      const pEl = document.getElementById('student-family-pension-pill');
      const dEl = document.getElementById('student-family-pension-desc');
      if (tEl) {
        tEl.textContent = '¡Tu familia está al día con la pensión escolar!';
        tEl.style.color = '#15803d';
      }
      if (pEl) {
        pEl.textContent = `+${completedPension.rewardCredits || 100} 🪙 Escolaris`;
        pEl.style.background = '#dcfce7';
        pEl.style.color = '#15803d';
      }
      if (dEl) {
        dEl.innerHTML = `Tus acudientes cancelaron oportunamente la pensión de <strong>${completedPension.month || 'Octubre'}</strong>. Has recibido la insignia <strong>${completedPension.rewardBadgeTitle || 'Pago Oportuno 💳'}</strong> y créditos acreditados en tu cuenta.`;
        dEl.style.color = '#166534';
      }
    } else if (isStudent && pendingPension) {
      studentPensionBanner.classList.remove('hidden');
      studentPensionBanner.style.background = '#FFFBEB';
      studentPensionBanner.style.borderColor = '#FDE68A';
      const tEl = document.getElementById('student-family-pension-title');
      const pEl = document.getElementById('student-family-pension-pill');
      const dEl = document.getElementById('student-family-pension-desc');
      if (tEl) {
        tEl.textContent = `Pensión Escolar (${pendingPension.month || 'Octubre'})`;
        tEl.style.color = '#B45309';
      }
      if (pEl) {
        pEl.textContent = `+${pendingPension.rewardCredits || 100} 🪙 por ganar`;
        pEl.style.background = '#FEF3C7';
        pEl.style.color = '#B45309';
      }
      if (dEl) {
        dEl.innerHTML = `Recuerda a tu acudiente pagar en los primeros 5 días del mes para ganar el logro escolar y <strong>+${pendingPension.rewardCredits || 100} 🪙 créditos Escolaris</strong> para tu tienda.`;
        dEl.style.color = '#92400E';
      }
    } else {
      studentPensionBanner.classList.add('hidden');
    }
  }

  const rankingWidget = document.getElementById('feed-sidebar-ranking-preview');
  if (rankingWidget) {
    const topStudents = leaderboardUsers.filter(u => u.role === 'STUDENT' || !u.role).slice(0, 3);
    if (topStudents.length === 0) {
      if (allUsers.length === 0) {
        rankingWidget.innerHTML = `<p style="font-size:11px; color:var(--text-muted);">Cargando ranking...</p>`;
      } else {
        rankingWidget.innerHTML = `<p style="font-size:11px; color:var(--text-muted);">Sin estudiantes en ranking aún</p>`;
      }
    } else {
      rankingWidget.innerHTML = topStudents.map((u, i) => `
        <div style="display:flex; justify-content:space-between; align-items:center; padding:6px 0; font-size:11px;">
          <div style="display:flex; align-items:center; gap:6px;">
            <span style="font-weight:800; color:var(--primary);">${i === 0 ? '🥇' : (i === 1 ? '🥈' : '🥉')}</span>
            <span>${escapeHtml(u.name || 'Estudiante')}</span>
          </div>
          <span style="font-weight:800; color:var(--primary); font-size:10px;">${u.xp || 0} XP</span>
        </div>
      `).join('');
    }
  }
}

async function likePost(postId, newLikes) {
  try {
    await db.collection('feed_posts').doc(postId).update({ 
      likes: newLikes,
      likesCount: newLikes
    });
  } catch (e) {
    console.error("Error al actualizar likes:", e);
  }
}
window.likePost = likePost;

async function handleLikePost(postId) {
  const p = feedPosts.find(x => x.id === postId);
  const currentLikes = p ? (p.likes || p.likesCount || 0) : 0;
  await likePost(postId, currentLikes + 1);
}
window.handleLikePost = handleLikePost;

async function handleDeletePost(postId) {
  if (!confirm('¿Estás seguro de que deseas eliminar esta publicación del muro escolar?')) return;
  try {
    const postDocRef = db.collection('feed_posts').doc(postId.toString());
    await postDocRef.delete();

    // Eliminar comentarios asociados en Firestore
    const numericId = Number(postId);
    const commentsSnap = await db.collection('post_comments').get();
    const batch = db.batch();
    let hasCommentsToDelete = false;
    commentsSnap.forEach(doc => {
      const c = doc.data();
      if (c.postId == postId || (numericId && c.postId == numericId)) {
        batch.delete(doc.ref);
        hasCommentsToDelete = true;
      }
    });
    if (hasCommentsToDelete) {
      await batch.commit();
    }
    if (typeof showToast === 'function') {
      showToast('Publicación eliminada correctamente', 'success');
    }
  } catch (err) {
    console.error('Error eliminando publicación:', err);
    if (typeof showToast === 'function') {
      showToast('Error al eliminar publicación: ' + err.message, 'danger');
    } else {
      alert('Error al eliminar: ' + err.message);
    }
  }
}
window.handleDeletePost = handleDeletePost;

async function handleCreatePost(e) {
  e.preventDefault();
  const category = document.getElementById('post-category').value;
  const subject = document.getElementById('post-subject')?.value || '';
  const rawContent = document.getElementById('post-content').value.trim();
  const content = subject ? `[${subject}] ${rawContent}` : rawContent;

  try {
    const postRef = db.collection('feed_posts').doc();
    const now = Date.now();
    await postRef.set({
      id: postRef.id,
      title: subject ? `[${subject}] ${category}` : (category || 'Aviso Escolar'),
      category: category,
      postType: category,
      subject: subject || null,
      content: content,
      authorName: currentUser ? currentUser.name : 'Usuario',
      authorRole: currentUser ? (currentUser.role === 'TEACHER' ? `Docente • ${currentUser.teacherSubject || 'Titular'}` : `Estudiante • ${currentUser.gradeSection || '10°'}`) : 'Comunidad',
      authorEmoji: currentUser ? currentUser.avatarEmoji : '🎓',
      likes: 0,
      likesCount: 0,
      timestamp: now,
      timestampMillis: now,
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    if (currentUser) {
      await db.collection('users').doc(currentUser.id).update({
        credits: (currentUser.credits || 0) + 25,
        xp: (currentUser.xp || 0) + 50
      });
      showToast("✨ Publicado en el muro (+25 🪙)");
    }
    closeModal('modal-post');
  } catch (err) {
    alert("Error al publicar: " + err.message);
  }
}

// 12. Tasks Logic
function filterTasks(filter) {
  activeTaskFilter = filter;
  document.getElementById('task-filter-pending').className = filter === 'pending' ? 'btn-primary btn-filter active' : 'btn-secondary btn-filter';
  document.getElementById('task-filter-completed').className = filter === 'completed' ? 'btn-primary btn-filter active' : 'btn-secondary btn-filter';
  document.getElementById('task-filter-exams').className = filter === 'exams' ? 'btn-primary btn-filter active' : 'btn-secondary btn-filter';
  renderTasks();
}

function renderTasks() {
  const container = document.getElementById('tasks-list');
  if (!container) return;

  const isCompletedView = activeTaskFilter === 'completed';
  const isExamView = activeTaskFilter === 'exams';

  let filtered = userTasks.filter(t => {
    const isDone = (t.completed === true || t.status === 'COMPLETED');
    if (isExamView) return t.isExam === true;
    return isCompletedView ? isDone : !isDone;
  });

  if (filtered.length === 0) {
    container.innerHTML = `<p style="font-size:12px; color:var(--text-muted); text-align:center; padding:20px;">No hay actividades en esta sección.</p>`;
    return;
  }

  container.innerHTML = filtered.map(t => {
    const isDone = (t.completed === true || t.status === 'COMPLETED');
    const formattedDate = t.dueDate || (t.dueDateMillis ? new Date(t.dueDateMillis).toLocaleDateString('es-CO') : 'Sin fecha');
    return `
      <div class="clay-card task-item ${isDone ? 'completed' : ''}">
        ${currentUser && currentUser.role !== 'PARENT' ? `
          <div class="task-checkbox ${isDone ? 'checked' : ''}" onclick="toggleTaskStatus('${t.id}', ${!isDone})">
            ${isDone ? '✓' : ''}
          </div>
        ` : `<div style="font-size:18px;">${isDone ? '✅' : '⏳'}</div>`}
        <div class="task-info">
          <div class="task-title">${escapeHtml(t.title)}</div>
          <div class="task-meta">
            <span>${t.subject}</span> • <span>Entrega: ${formattedDate}</span>
            <span class="badge badge-priority-${(t.priority || 'media').toLowerCase()}">${t.priority || 'Media'}</span>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

async function toggleTaskStatus(taskId, completed) {
  try {
    const taskObj = userTasks.find(t => t.id === taskId);
    const isHighPriority = taskObj && (taskObj.priority === 'ALTA' || taskObj.priority === 'High');
    const rewardCredits = isHighPriority ? 40 : 30;
    const rewardXp = isHighPriority ? 80 : 60;

    await db.collection('tasks').doc(taskId).update({
      completed: completed,
      status: completed ? 'COMPLETED' : 'PENDING'
    });

    if (completed && currentUser) {
      const newCredits = (currentUser.credits || 0) + rewardCredits;
      const newXp = (currentUser.xp || 0) + rewardXp;
      await db.collection('users').doc(currentUser.id).update({
        credits: newCredits,
        xp: newXp
      });
      currentUser.credits = newCredits;
      currentUser.xp = newXp;
      try { localStorage.setItem('escolaris_cached_user', JSON.stringify(currentUser)); } catch (e) {}
      showToast(`🎉 ¡Actividad completada (+${rewardCredits} 🪙 / +${rewardXp} XP)!`);
      renderRewards();
      renderProfileStore();
    }
  } catch (e) {
    console.error("Error toggling task status:", e);
  }
}

async function handleCreateTask(e) {
  e.preventDefault();
  const type = document.getElementById('task-type').value;
  const title = document.getElementById('task-title').value.trim();
  const subject = document.getElementById('task-subject').value;
  const dueDate = document.getElementById('task-due-date').value;
  const priority = document.getElementById('task-priority').value;
  const desc = document.getElementById('task-desc').value.trim();

  try {
    const dueMillis = dueDate ? new Date(dueDate + 'T23:59:59').getTime() : Date.now();
    await db.collection('tasks').add({
      title: title,
      subject: subject,
      dueDate: dueDate,
      dueDateMillis: dueMillis,
      priority: priority,
      description: desc,
      isExam: type === 'EXAM',
      completed: false,
      status: 'PENDING',
      studentId: 'ALL',
      rewardCredits: priority === 'ALTA' ? 40 : 30,
      createdBy: currentUser ? currentUser.name : 'Docente',
      timestampMillis: Date.now(),
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    closeModal('modal-task');
    showToast("📝 Actividad agendada con éxito");
  } catch (err) {
    alert("Error al guardar tarea: " + err.message);
  }
}

// 13. Rewards Store Logic
function switchStoreTab(tab) {
  const catalog = document.getElementById('rewards-tab-catalog');
  const passes = document.getElementById('rewards-tab-passes');
  const btnCatalog = document.getElementById('btn-store-catalog-tab');
  const btnCreateReward = document.getElementById('btn-create-reward-header');
  const isTeacher = currentUser && currentUser.role === 'TEACHER';
  
  if (tab === 'passes') {
    if (catalog) catalog.classList.add('hidden');
    if (passes) passes.classList.remove('hidden');
    if (btnCatalog) btnCatalog.classList.remove('hidden');
    if (btnCreateReward) btnCreateReward.classList.add('hidden');
    renderPasses();
  } else {
    if (catalog) catalog.classList.remove('hidden');
    if (passes) passes.classList.add('hidden');
    if (btnCatalog) btnCatalog.classList.add('hidden');
    if (btnCreateReward) {
      if (isTeacher) btnCreateReward.classList.remove('hidden');
      else btnCreateReward.classList.add('hidden');
    }
    renderRewards();
  }
}

function filterRewardsCategory(category) {
  activeRewardsCategory = category;
  const categories = ['ALL', 'AULA', 'ACADEMICO', 'RECREO', 'CAFETERIA', 'DISTINCION'];
  categories.forEach(cat => {
    const btn = document.getElementById(`btn-store-cat-${cat}`);
    if (btn) {
      if (cat === category) btn.classList.add('active');
      else btn.classList.remove('active');
    }
  });
  renderRewards();
}

function renderRewards() {
  const container = document.getElementById('rewards-tab-catalog');
  if (!container) return;

  const isTeacher = currentUser && currentUser.role === 'TEACHER';
  const isParent = currentUser && currentUser.role === 'PARENT';
  const btnCreateReward = document.getElementById('btn-create-reward-header');
  if (btnCreateReward) {
    if (isTeacher) btnCreateReward.classList.remove('hidden');
    else btnCreateReward.classList.add('hidden');
  }

  const balanceText = document.getElementById('store-balance-text');
  if (balanceText && currentUser) {
    balanceText.textContent = `🪙 ${currentUser.credits || 0}`;
  }

  let htmlContent = '';

  // 1. If Parent, Render Dedicated "Ceder Escolaris" Transfer Panel Exclusively (No Catalog)
  if (isParent) {
    const linkedCode = currentUser.linkedStudentId || '';
    const child = allUsers.find(u => u.studentCode === linkedCode || u.id === linkedCode);
    const parentCredits = currentUser.credits || 0;

    htmlContent = `
      <div class="clay-card" style="grid-column: 1 / -1; padding: 20px; border: 2px solid var(--coin-gold); background: var(--surface-color);">
        <div style="display:flex; align-items:center; gap:12px; margin-bottom:12px;">
          <span style="font-size:32px;">🪙</span>
          <div>
            <div style="font-size:18px; font-weight:800; color:var(--text-main);">Ceder Escolaris a tu Hijo/a</div>
            <div style="font-size:12px; color:var(--text-muted);">Como acudiente, tus Escolaris son para premiar y motivar el esfuerzo académico de tu hijo/a. No tienes catálogo ni recompensas personales para canjear.</div>
          </div>
        </div>

        <div style="background:rgba(245,158,11,0.1); border:1px solid rgba(245,158,11,0.3); border-radius:12px; padding:12px 16px; display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; flex-wrap:wrap; gap:10px;">
          <div>
            <div style="font-size:12px; color:#b45309; font-weight:700;">Tu Saldo para Ceder:</div>
            <div style="font-size:22px; font-weight:900; color:#b45309;">🪙 ${parentCredits} Escolaris</div>
          </div>
          ${child ? `
            <div style="text-align:right;">
              <div style="font-size:12px; color:var(--primary); font-weight:700;">Hijo/a: <strong>${child.name}</strong> (${child.gradeSection || 'Estudiante'})</div>
              <div style="font-size:14px; font-weight:800; color:var(--primary);">Saldo actual: 🪙 ${child.credits || 0} Escolaris</div>
            </div>
          ` : `
            <span style="font-size:12px; color:var(--danger); font-weight:700;">⚠️ No tienes un estudiante vinculado aún</span>
          `}
        </div>

        ${child ? `
          <div style="display:flex; flex-direction:column; gap:10px;">
            <label style="font-size:12px; font-weight:700; color:var(--text-main);">Selecciona o ingresa la cantidad de Escolaris a transferir:</label>
            <div style="display:flex; gap:8px; flex-wrap:wrap;">
              <button class="btn-secondary btn-sm" onclick="document.getElementById('web-parent-transfer-amount').value = '10'">+10 🪙</button>
              <button class="btn-secondary btn-sm" onclick="document.getElementById('web-parent-transfer-amount').value = '25'">+25 🪙</button>
              <button class="btn-secondary btn-sm" onclick="document.getElementById('web-parent-transfer-amount').value = '50'">+50 🪙</button>
              <button class="btn-secondary btn-sm" onclick="document.getElementById('web-parent-transfer-amount').value = '100'">+100 🪙</button>
            </div>
            <div style="display:grid; grid-template-columns: 1fr 2fr; gap:10px;">
              <input type="number" id="web-parent-transfer-amount" class="form-input" value="25" min="1" max="${parentCredits}" placeholder="Cantidad">
              <input type="text" id="web-parent-transfer-reason" class="form-input" placeholder="Motivo (Ej. ¡Excelente desempeño escolar y tareas al día!)" value="¡Reconocimiento por esfuerzo académico y apoyo en casa!">
            </div>
            <button class="btn-primary" style="margin-top:6px; background:var(--coin-gold); color:#000; font-weight:800;" onclick="handleParentTransferWeb()">
              Ceder Escolaris a ${child.name} 🚀
            </button>
          </div>
        ` : ''}
      </div>
    `;
    container.innerHTML = htmlContent;
    return;
  }

  // Filter rewards by category
  const filtered = activeRewardsCategory === 'ALL'
    ? rewardsList
    : rewardsList.filter(r => (r.category || 'ACADEMICO').toUpperCase() === activeRewardsCategory.toUpperCase());

  if (filtered.length === 0) {
    htmlContent += `
      <div class="clay-card" style="text-align: center; padding: 28px; grid-column: 1 / -1;">
        <div style="font-size: 36px; margin-bottom: 8px;">🎁</div>
        <div style="font-weight: 800; font-size: 15px;">No hay recompensas en esta categoría</div>
        <div style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">Selecciona "Todas" o explora otra categoría escolar.</div>
        <button class="btn-secondary btn-sm" style="margin-top:12px;" onclick="filterRewardsCategory('ALL')">Ver Todas las Recompensas</button>
      </div>
    `;
    container.innerHTML = htmlContent;
    return;
  }

  const categoryLabels = {
    'AULA': '🪑 Aula',
    'ACADEMICO': '📚 Académico',
    'RECREO': '⚽ Recreo',
    'CAFETERIA': '🥪 Cafetería',
    'DISTINCION': '🏆 Distinción'
  };

  htmlContent += filtered.map(r => {
    const cost = r.costCredits || 100;
    const stock = r.stockAvailable !== undefined ? r.stockAvailable : (r.stock !== undefined ? r.stock : 10);
    const canAfford = currentUser && (currentUser.credits || 0) >= cost;
    const isOutOfStock = stock <= 0;
    const rawTeacher = (r.teacherName || '').trim();
    const resolvedTeacher = (!rawTeacher || rawTeacher === 'Docente Titular' || rawTeacher === 'Prof. Titular') ? (currentUser && currentUser.role === 'TEACHER' ? currentUser.name : 'Manuel Muñoz') : rawTeacher;
    const cleanTeacherLabel = resolvedTeacher.startsWith('Prof') ? resolvedTeacher : `Prof. ${resolvedTeacher}`;
    const catKey = (r.category || 'ACADEMICO').toUpperCase();
    const catLabel = categoryLabels[catKey] || '🎟️ Escolar';

    return `
      <div class="clay-card reward-card">
        <div>
          <div class="reward-header">
            <div class="reward-icon-box">${r.icon || '🎁'}</div>
            <div style="display:flex; align-items:center; gap:6px;">
              <span class="badge" style="font-size:10px; background:var(--surface-variant); color:var(--text-muted); font-weight:800; white-space:nowrap !important;">${catLabel}</span>
              <span class="reward-price">🪙 ${cost}</span>
            </div>
          </div>
          
          <div class="reward-title" title="${escapeHtml(r.title)}">${escapeHtml(r.title)}</div>
          <div class="reward-teacher-tag" title="${escapeHtml(cleanTeacherLabel)}">
            <span>👨‍🏫</span> <span>${escapeHtml(cleanTeacherLabel)}</span>
          </div>
          <div class="reward-desc" title="${escapeHtml(r.description || 'Beneficio escolar formativo')}">${escapeHtml(r.description || 'Beneficio escolar')}</div>
          
          <div class="reward-meta-row">
            <span style="color:${isOutOfStock ? 'var(--danger)' : 'var(--text-muted)'};">
              ${isOutOfStock ? '⚠️ Agotado' : `📦 Stock: ${stock} pases`}
            </span>
            <span style="color:var(--primary); font-size:10.5px; font-weight:800;">Nivel ${r.tier || 1}</span>
          </div>
        </div>

        <div class="reward-footer">
          ${isTeacher ? `
            <div style="display:flex; gap:6px;">
              <button class="btn-secondary btn-sm flex-1" onclick="openEditRewardModal('${r.id}')" title="Editar nombre, descripción y valor">
                ✏️ Editar
              </button>
              <button class="btn-secondary btn-sm btn-danger-outline" onclick="handleDeleteReward('${r.id}', '${escapeHtml(r.title)}')" title="Eliminar recompensa">
                🗑️
              </button>
            </div>
          ` : `
            <button class="btn-primary w-full" style="padding: 9px 12px; font-size: 12.5px; font-weight:800;" 
                    onclick="redeemReward('${r.id}', '${escapeHtml(r.title)}', ${cost})"
                    ${!canAfford || isOutOfStock ? 'disabled style="opacity:0.55; cursor:not-allowed;"' : ''}>
              ${isOutOfStock ? 'Agotado' : (canAfford ? 'Canjear Pase 🎟️' : 'Faltan Créditos 🪙')}
            </button>
          `}
        </div>
      </div>
    `;
  }).join('');

  container.innerHTML = htmlContent;
}

async function handleParentTransferWeb(customAmt) {
  if (!currentUser || currentUser.role !== 'PARENT') return;
  const amountInput = document.getElementById('web-parent-transfer-amount');
  const amount = customAmt || (amountInput ? parseInt(amountInput.value, 10) : 25) || 25;
  const reasonInput = document.getElementById('web-parent-transfer-reason');
  const reason = (reasonInput ? reasonInput.value.trim() : '') || '¡Reconocimiento por esfuerzo académico y apoyo en casa!';
  const parentCredits = currentUser.credits || 0;

  if (amount <= 0) {
    alert("Por favor ingresa una cantidad válida de Escolaris a ceder.");
    return;
  }
  if (parentCredits < amount) {
    alert(`Saldo insuficiente de Escolaris. Dispones de 🪙 ${parentCredits} Escolaris.`);
    return;
  }

  const linkedCode = currentUser.linkedStudentId || '';
  const child = allUsers.find(u => u.studentCode === linkedCode || u.id === linkedCode);
  if (!child) {
    alert("Primero debes vincular el código de estudiante de tu hijo/a para cederle Escolaris.");
    return;
  }

  try {
    // 1. Deduct from parent
    const newParentCredits = Math.max(0, parentCredits - amount);
    await db.collection('users').doc(currentUser.id).update({
      credits: newParentCredits,
      parentIncentiveCredits: newParentCredits
    });
    currentUser.credits = newParentCredits;
    currentUser.parentIncentiveCredits = newParentCredits;

    // 2. Add to student
    const childDoc = await db.collection('users').doc(child.id).get();
    const currentChildCredits = childDoc.exists ? (childDoc.data().credits || 0) : (child.credits || 0);
    const currentChildXp = childDoc.exists ? (childDoc.data().xp || 0) : (child.xp || 0);
    await db.collection('users').doc(child.id).update({
      credits: currentChildCredits + amount,
      xp: currentChildXp + (amount * 2)
    });
    child.credits = currentChildCredits + amount;
    child.xp = currentChildXp + (amount * 2);

    // 3. Notification for student
    await db.collection('notifications').add({
      title: "🎁 ¡Tus Padres te han cedido Escolaris!",
      message: `Tus acudientes te han transferido +${amount} 🪙 Escolaris a tu saldo escolar. Motivo: ${reason}`,
      type: "REWARD",
      targetScreen: "store",
      studentId: child.id,
      isRead: false,
      timestamp: Date.now(),
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    showToast(`🎁 ¡Has cedido exitosamente +${amount} 🪙 Escolaris a ${child.name}!`);
    renderRewards();
  } catch (err) {
    alert("Error al transferir Escolaris: " + err.message);
  }
}

function openEditRewardModal(rewardId) {
  const rew = rewardsList.find(r => r.id === rewardId);
  if (!rew) return;

  document.getElementById('edit-reward-id').value = rew.id;
  document.getElementById('edit-reward-title').value = rew.title || '';
  document.getElementById('edit-reward-desc').value = rew.description || '';
  document.getElementById('edit-reward-cost').value = rew.costCredits || 100;
  document.getElementById('edit-reward-stock').value = rew.stockAvailable !== undefined ? rew.stockAvailable : 10;
  document.getElementById('edit-reward-icon').value = rew.icon || '🎁';
  document.getElementById('edit-reward-category').value = rew.category || 'ACADEMICO';

  openModal('modal-edit-reward');
}

async function handleUpdateReward(e) {
  e.preventDefault();
  const id = document.getElementById('edit-reward-id').value;
  const title = document.getElementById('edit-reward-title').value.trim();
  const description = document.getElementById('edit-reward-desc').value.trim();
  const costCredits = parseInt(document.getElementById('edit-reward-cost').value, 10) || 100;
  const stockAvailable = parseInt(document.getElementById('edit-reward-stock').value, 10) || 0;
  const icon = document.getElementById('edit-reward-icon').value.trim() || '🎁';
  const category = document.getElementById('edit-reward-category').value || 'ACADEMICO';

  if (!id || !title) return;

  try {
    await db.collection('rewards').doc(id).update({
      title: title,
      description: description,
      costCredits: costCredits,
      stockAvailable: stockAvailable,
      icon: icon,
      category: category,
      updatedAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    closeModal('modal-edit-reward');
    showToast("✏️ Recompensa actualizada correctamente");
  } catch (err) {
    alert("Error al actualizar la recompensa: " + err.message);
  }
}

async function handleCreateReward(e) {
  e.preventDefault();
  const title = document.getElementById('create-reward-title').value.trim();
  const description = document.getElementById('create-reward-desc').value.trim();
  const costCredits = parseInt(document.getElementById('create-reward-cost').value, 10) || 100;
  const stockAvailable = parseInt(document.getElementById('create-reward-stock').value, 10) || 10;
  const icon = document.getElementById('create-reward-icon').value.trim() || '🎁';
  const category = document.getElementById('create-reward-category').value || 'ACADEMICO';

  if (!title) return;

  try {
    await db.collection('rewards').add({
      title: title,
      description: description,
      costCredits: costCredits,
      stockAvailable: stockAvailable,
      icon: icon,
      category: category,
      teacherName: currentUser ? currentUser.name : 'Docente Titular',
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    closeModal('modal-create-reward');
    document.getElementById('form-create-reward').reset();
    showToast("🎁 Recompensa publicada en el catálogo escolar");
  } catch (err) {
    alert("Error al crear la recompensa: " + err.message);
  }
}

async function handleDeleteReward(rewardId, rewardTitle) {
  if (!confirm(`¿Estás seguro de eliminar la recompensa "${rewardTitle}" del catálogo escolar?`)) return;

  try {
    await db.collection('rewards').doc(rewardId).delete();
    showToast("🗑️ Recompensa eliminada del catálogo");
  } catch (err) {
    alert("Error al eliminar recompensa: " + err.message);
  }
}

async function redeemReward(id, title, cost) {
  if (!currentUser) return;
  if ((currentUser.credits || 0) < cost) {
    alert(`Saldo insuficiente. Necesitas 🪙 ${cost} créditos Escolaris para canjear este beneficio. Tu saldo actual es de 🪙 ${currentUser.credits || 0}. ¡Cumple tus tareas y deberes para acumular más!`);
    return;
  }

  const voucherCode = 'ESC-' + Math.random().toString(36).substring(2, 8).toUpperCase();
  try {
    // 1. Descontar temporalmente los créditos (reserva de puntos)
    const newCredits = Math.max(0, (currentUser.credits || 0) - cost);
    await db.collection('users').doc(currentUser.id).update({
      credits: newCredits
    });
    currentUser.credits = newCredits;

    // 2. Registrar el canje en estado PENDING_APPROVAL para revisión del docente
    await db.collection('redemptions').add({
      studentId: currentUser.id,
      studentName: currentUser.name,
      rewardId: id,
      rewardTitle: title,
      costCredits: cost,
      voucherCode: voucherCode,
      status: 'PENDING_APPROVAL',
      redeemedAtMillis: Date.now(),
      createdAt: firebase.firestore.FieldValue.serverTimestamp()
    });

    showToast("🎟️ ¡Solicitud de canje enviada! El docente revisará y aceptará tu beneficio.");
    renderUserProfile();
    if (activeProfileSubtab === 'store') renderProfileStore();
    renderPasses();
    showQrCode(title, voucherCode);
  } catch (err) {
    alert("Error al solicitar canje: " + err.message);
  }
}

function renderPasses() {
  const container = document.getElementById('rewards-tab-passes');
  if (!container || !currentUser) return;

  const myPasses = userRedemptions.filter(p => p.studentId === currentUser.id);

  if (myPasses.length === 0) {
    container.innerHTML = `
      <div class="clay-card" style="text-align: center; padding: 24px;">
        <div style="font-size: 32px; margin-bottom: 6px;">🎟️</div>
        <div style="font-weight: 700;">No tienes pases activos</div>
        <div style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">Canjea tus créditos en la tienda para obtener beneficios escolares.</div>
      </div>
    `;
    return;
  }

  container.innerHTML = myPasses.map(p => `
    <div class="clay-card reward-card" style="border-left: 4px solid var(--primary);">
      <div class="reward-header">
        <div style="font-size: 24px;">🎟️</div>
        <span class="badge" style="${p.status === 'PENDING_APPROVAL' ? 'background:#fef3c7; color:#b45309;' : (p.status === 'REJECTED' ? 'background:#fee2e2; color:#b91c1c;' : (p.status === 'USED' ? 'background:#e5e7eb; color:#4b5563;' : 'background:#dcfce7; color:#15803d;'))}">
          ${p.status === 'PENDING_APPROVAL' ? '⏳ Pendiente Aprobación' : (p.status === 'REJECTED' ? '❌ Rechazado (Devuelto)' : (p.status === 'USED' ? 'Utilizado ✅' : 'Disponible 🎟️'))}
        </span>
      </div>
      <div class="reward-title">${p.rewardTitle}</div>
      <div style="font-size: 12px; font-weight: 800; color: var(--primary); margin: 4px 0;">
        CÓDIGO: ${p.voucherCode}
      </div>
      ${p.status !== 'USED' ? `
        <button class="btn-secondary btn-sm" onclick="showQrCode('${p.rewardTitle}', '${p.voucherCode}')">
          Mostrar QR al Docente
        </button>
      ` : ''}
    </div>
  `).join('');
}

function showQrCode(title, code) {
  document.getElementById('qr-title').textContent = title;
  document.getElementById('qr-code-text').textContent = code;
  const qrBox = document.getElementById('qrcode-box');
  qrBox.innerHTML = '';

  new QRCode(qrBox, {
    text: `ESCOLARIS:${code}:${currentUser ? currentUser.id : ''}`,
    width: 140,
    height: 140,
    colorDark: "#0f172a",
    colorLight: "#ffffff"
  });

  openModal('modal-qr');
}

// 14. Leaderboard & Profile Badges Logic
function renderLeaderboard() {
  const container = document.getElementById('leaderboard-list');
  if (!container) return;

  const students = leaderboardUsers.filter(u => u.role === 'STUDENT' || !u.role);
  if (students.length === 0) {
    if (allUsers.length === 0) {
      container.innerHTML = `<p style="font-size:12px; color:var(--text-muted); text-align:center; padding:20px;">Cargando tabla de honor...</p>`;
    } else {
      container.innerHTML = `<p style="font-size:12px; color:var(--text-muted); text-align:center; padding:20px;">No hay estudiantes registrados en la tabla de honor aún.</p>`;
    }
    return;
  }

  container.innerHTML = students.slice(0, 10).map((u, i) => {
    let rankBadgeClass = '';
    if (i === 0) rankBadgeClass = 'gold';
    else if (i === 1) rankBadgeClass = 'silver';
    else if (i === 2) rankBadgeClass = 'bronze';

    return `
      <div class="clay-card leaderboard-item">
        <div class="rank-badge ${rankBadgeClass}">${i + 1}</div>
        <div style="font-size: 24px;">${u.avatarEmoji || '🎓'}</div>
        <div class="task-info">
          <div class="task-title">${escapeHtml(u.name || 'Estudiante')}</div>
          <div class="task-meta">${escapeHtml(u.gradeSection || '10° Grado')} • 🔥 ${u.streakDays || 1} días</div>
        </div>
        <div style="font-weight: 800; font-size: 13px; color: var(--primary); text-align: right;">
          <div>${u.xp || 0} XP</div>
          <div style="font-size: 11px; color: var(--text-muted); font-weight: 600;">🪙 ${u.credits || 0}</div>
        </div>
      </div>
    `;
  }).join('');
}

function renderProfileBadges() {
  const container = document.getElementById('profile-badges-list');
  if (!container || !currentUser) return;

  const isParent = currentUser.role === 'PARENT';
  const presets = isParent ? familyBadgePresets : studentBadgePresets;
  const myBadges = allBadges.filter(b => (isParent ? (b.studentId === currentUser.id || b.category === 'FAMILY') : b.studentId === currentUser.id));

  const userBadgesMap = {};
  if (currentUser.badges && Array.isArray(currentUser.badges)) {
    currentUser.badges.forEach(b => {
      if (b.id) userBadgesMap[b.id.toUpperCase()] = b;
      if (b.title) userBadgesMap[b.title.trim().toLowerCase()] = b;
    });
  }

  const unlockedMap = {};
  myBadges.forEach(b => {
    if (b.badgeKey) unlockedMap[b.badgeKey.toUpperCase()] = b;
    if (b.title) unlockedMap[b.title.trim().toLowerCase()] = b;
  });
  if (currentUser.badges && Array.isArray(currentUser.badges)) {
    currentUser.badges.forEach(b => {
      if (b.isUnlocked) {
        if (b.id) unlockedMap[b.id.toUpperCase()] = b;
        if (b.title) unlockedMap[b.title.trim().toLowerCase()] = b;
      }
    });
  }

  const totalCount = presets.length;
  const unlockedCount = presets.filter(p => {
    const ub = userBadgesMap[(p.id || '').toUpperCase()] || userBadgesMap[(p.key || '').toUpperCase()] || userBadgesMap[p.title.trim().toLowerCase()];
    return unlockedMap[p.key.toUpperCase()] || unlockedMap[p.title.trim().toLowerCase()] || (ub && ub.isUnlocked);
  }).length;
  const progressPercent = totalCount > 0 ? Math.min(100, Math.round((unlockedCount / totalCount) * 100)) : 0;

  let html = `
    <div class="clay-card" style="padding:14px; margin-bottom:12px; border-left:4px solid var(--primary);">
      <div style="display:flex; justify-content:space-between; align-items:center;">
        <div>
          <div style="font-size:14px; font-weight:800; color:var(--text-main);">🎖️ Muro de Condecoraciones</div>
          <div style="font-size:11.5px; font-weight:700; color:var(--primary);">${unlockedCount} de ${totalCount} Desbloqueadas (${progressPercent}%)</div>
        </div>
        <span class="badge ${unlockedCount > 0 ? 'gold' : ''}" style="font-size:11px; font-weight:800;">
          ${unlockedCount > 0 ? `🌟 ${progressPercent}%` : '🔒 0%'}
        </span>
      </div>
      <div style="margin-top:8px; width:100%; height:7px; background:rgba(0,0,0,0.08); border-radius:99px; overflow:hidden;">
        <div style="width:${progressPercent}%; height:100%; background:var(--primary); transition:width 0.4s ease;"></div>
      </div>
      <div style="margin-top:8px; font-size:10.5px; color:var(--text-muted); line-height:1.3;">
        ✨ <em>Las ganadas brillan a todo color. Las bloqueadas aparecen en baja opacidad con su reto y progreso para desbloquear.</em>
      </div>
    </div>
  `;

  // Render presets
  html += presets.map(p => {
    const userBadge = userBadgesMap[(p.id || '').toUpperCase()] || userBadgesMap[(p.key || '').toUpperCase()] || userBadgesMap[p.title.trim().toLowerCase()];
    const earned = unlockedMap[p.key.toUpperCase()] || unlockedMap[p.title.trim().toLowerCase()] || (userBadge && userBadge.isUnlocked);
    const curProg = earned ? (userBadge?.targetProgress || p.targetProgress || 1) : (userBadge ? (userBadge.currentProgress || 0) : (p.currentProgress || 0));
    const tgtProg = userBadge ? (userBadge.targetProgress || p.targetProgress || 1) : (p.targetProgress || 1);
    const progFraction = tgtProg > 0 ? Math.min(1, curProg / tgtProg) : 0;
    const progPct = Math.round(progFraction * 100);

    if (earned) {
      return `
        <div class="clay-card ${earned.category === 'FAMILY' ? 'family-badge-card' : ''}" style="padding:12px 14px; margin-bottom:8px; border-left:4px solid #10b981;">
          <div style="display:flex; justify-content:space-between; align-items:center;">
            <div style="display:flex; align-items:center; gap:10px;">
              <span style="font-size:26px;">${earned.emoji || p.emoji || '🎖️'}</span>
              <div>
                <div style="display:flex; align-items:center; gap:6px; flex-wrap:wrap;">
                  <strong style="font-size:13px; color:var(--text-main);">${escapeHtml(earned.title || p.title)}</strong>
                  <span class="badge" style="background:#dcfce7; color:#15803d; font-size:10px; font-weight:800;">✅ Desbloqueada</span>
                  ${earned.category === 'FAMILY' ? `<span class="tag-family" style="font-size:10px;">Familia</span>` : ''}
                </div>
                <p style="font-size:11px; color:var(--text-muted); margin-top:2px;">${escapeHtml(earned.description || p.desc)}</p>
                ${earned.teacherNote ? `<p style="font-size:10.5px; color:#6b21a8; font-style:italic; margin-top:2px;">"${escapeHtml(earned.teacherNote)}"</p>` : ''}
              </div>
            </div>
            <span class="reward-price" style="font-size:12px; font-weight:800; white-space:nowrap; margin-left:8px;">+${earned.creditReward || p.creditReward || 50} 🪙</span>
          </div>
        </div>
      `;
    } else {
      return `
        <div class="clay-card" style="padding:12px 14px; margin-bottom:8px; opacity:0.6; filter:grayscale(60%); border:1px dashed #cbd5e1; cursor:pointer;" onclick="showToast('🔒 Reto: ${escapeHtml(p.desc)}')">
          <div style="display:flex; justify-content:space-between; align-items:center;">
            <div style="display:flex; align-items:center; gap:10px; width:100%;">
              <span style="font-size:24px;">${p.emoji || '🎖️'}</span>
              <div style="flex:1;">
                <div style="display:flex; align-items:center; justify-content:space-between; gap:6px; flex-wrap:wrap;">
                  <strong style="font-size:13px; color:var(--text-main);">${escapeHtml(p.title)}</strong>
                  <span class="badge" style="background:#f1f5f9; color:#64748b; font-size:10px; font-weight:800;">🔒 Bloqueada (${curProg}/${tgtProg})</span>
                </div>
                <p style="font-size:11px; color:var(--text-muted); margin-top:2px;">🎯 <strong>Reto:</strong> ${escapeHtml(p.desc)}</p>
                <div style="margin-top:6px; display:flex; align-items:center; gap:8px;">
                  <div style="flex:1; height:6px; background:rgba(0,0,0,0.06); border-radius:99px; overflow:hidden;">
                    <div style="width:${progPct}%; height:100%; background:var(--primary); transition:width 0.3s ease;"></div>
                  </div>
                  <span style="font-size:10px; font-weight:800; color:var(--text-muted);">${curProg} / ${tgtProg}</span>
                </div>
              </div>
            </div>
            <span style="font-size:11px; font-weight:700; color:var(--text-muted); white-space:nowrap; margin-left:8px;">+${p.creditReward || 50} 🪙</span>
          </div>
        </div>
      `;
    }
  }).join('');

  // Render any custom teacher badges not in presets
  const presetKeys = new Set(presets.map(p => p.key.toUpperCase()));
  const presetTitles = new Set(presets.map(p => p.title.trim().toLowerCase()));
  const customBadges = myBadges.filter(b => !presetKeys.has((b.badgeKey || '').toUpperCase()) && !presetTitles.has((b.title || '').trim().toLowerCase()));

  if (customBadges.length > 0) {
    html += `<div style="font-size:12px; font-weight:800; color:var(--text-main); margin:12px 0 6px 0;">🎖️ Condecoraciones Especiales:</div>`;
    html += customBadges.map(b => `
      <div class="clay-card" style="padding:12px 14px; margin-bottom:8px; border-left:4px solid #f59e0b;">
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <div style="display:flex; align-items:center; gap:10px;">
            <span style="font-size:26px;">${b.emoji || '🎖️'}</span>
            <div>
              <div style="display:flex; align-items:center; gap:6px;">
                <strong style="font-size:13px; color:var(--text-main);">${escapeHtml(b.title)}</strong>
                <span class="badge" style="background:#fef3c7; color:#b45309; font-size:10px; font-weight:800;">🏆 Honor Especial</span>
              </div>
              <p style="font-size:11px; color:var(--text-muted); margin-top:2px;">${escapeHtml(b.description)}</p>
              ${b.teacherNote ? `<p style="font-size:10.5px; color:#6b21a8; font-style:italic; margin-top:2px;">"${escapeHtml(b.teacherNote)}"</p>` : ''}
            </div>
          </div>
          <span class="reward-price" style="font-size:12px; font-weight:800;">+${b.creditReward || 50} 🪙</span>
        </div>
      </div>
    `).join('');
  }

  container.innerHTML = html;
}

// 15. Schedule & Teacher Directory Screen Logic (Interactive & Editable)
let activeScheduleSubtab = 'classes';
activeScheduleDay = 'Lunes';

function switchScheduleSubtab(subtabName) {
  if (subtabName === 'calendar') {
    switchNav('calendar');
    return;
  }

  activeScheduleSubtab = subtabName;
  
  const tabClasses = document.getElementById('btn-sched-tab-classes');
  const tabDirectory = document.getElementById('btn-sched-tab-directory');

  const viewClasses = document.getElementById('schedule-view-classes');
  const viewDirectory = document.getElementById('schedule-view-directory');

  if (tabClasses) tabClasses.className = subtabName === 'classes' ? 'btn-primary btn-filter active' : 'btn-secondary btn-filter';
  if (tabDirectory) tabDirectory.className = subtabName === 'directory' ? 'btn-primary btn-filter active' : 'btn-secondary btn-filter';

  if (viewClasses) {
    if (subtabName === 'classes') viewClasses.classList.remove('hidden');
    else viewClasses.classList.add('hidden');
  }
  if (viewDirectory) {
    if (subtabName === 'directory') viewDirectory.classList.remove('hidden');
    else viewDirectory.classList.add('hidden');
  }

  if (subtabName === 'classes') {
    renderSchedule();
  } else if (subtabName === 'directory') {
    renderTeacherDirectory();
  }
}

const DEFAULT_WEEKLY_SCHEDULE = {
  'Lunes': [
    { time: '7:00 - 7:50', subject: 'Dirección de grupo', teacher: 'Manuel Muñoz', icon: '👨‍🏫', isBreak: false },
    { time: '7:50 - 8:40', subject: 'Tecnología e Informática', teacher: 'Manuel Muñoz', icon: '💻', isBreak: false },
    { time: '8:40 - 9:30', subject: 'Biología', teacher: 'Anna Fulí', icon: '🔬', isBreak: false },
    { time: '9:30 - 10:10', subject: 'Descanso', teacher: 'Comunidad Escolar', icon: '🥪', isBreak: true },
    { time: '10:10 - 11:00', subject: 'Inglés', teacher: 'Ángela Rendón', icon: '🇬🇧', isBreak: false },
    { time: '11:00 - 11:50', subject: 'Ciencias Sociales', teacher: 'Ibón Ocampo', icon: '🌍', isBreak: false },
    { time: '11:50 - 12:35', subject: 'Español', teacher: 'Katherine Castro', icon: '📖', isBreak: false },
    { time: '12:35 - 1:20', subject: 'Español', teacher: 'Katherine Castro', icon: '📖', isBreak: false }
  ],
  'Martes': [
    { time: '7:00 - 7:50', subject: 'Geometría', teacher: 'Carlos Erazo', icon: '📐', isBreak: false },
    { time: '7:50 - 8:40', subject: 'Matemáticas', teacher: 'Manuel Muñoz', icon: '➕', isBreak: false },
    { time: '8:40 - 9:30', subject: 'Matemáticas', teacher: 'Manuel Muñoz', icon: '➕', isBreak: false },
    { time: '9:30 - 10:10', subject: 'Descanso', teacher: 'Comunidad Escolar', icon: '🥪', isBreak: true },
    { time: '10:10 - 11:00', subject: 'Estética', teacher: 'Sirley Palta', icon: '🎨', isBreak: false },
    { time: '11:00 - 11:50', subject: 'Biología', teacher: 'Anna Fulí', icon: '🔬', isBreak: false },
    { time: '11:50 - 12:35', subject: 'Música', teacher: 'Danilo Daza', icon: '🎵', isBreak: false },
    { time: '12:35 - 1:20', subject: 'Ciencias Sociales', teacher: 'Ibón Ocampo', icon: '🌍', isBreak: false }
  ],
  'Miércoles': [
    { time: '7:00 - 7:50', subject: 'Cátedra emocional', teacher: 'Irnalda Tintinago', icon: '💛', isBreak: false },
    { time: '7:50 - 8:40', subject: 'Ciencias Sociales', teacher: 'Ibón Ocampo', icon: '🌍', isBreak: false },
    { time: '8:40 - 9:30', subject: 'Física', teacher: 'Víctor Toro', icon: '⚡', isBreak: false },
    { time: '9:30 - 10:10', subject: 'Descanso', teacher: 'Comunidad Escolar', icon: '🥪', isBreak: true },
    { time: '10:10 - 11:00', subject: 'Español', teacher: 'Katherine Castro', icon: '📖', isBreak: false },
    { time: '11:00 - 11:50', subject: 'Matemáticas', teacher: 'Manuel Muñoz', icon: '➕', isBreak: false },
    { time: '11:50 - 12:35', subject: 'Matemáticas', teacher: 'Manuel Muñoz', icon: '➕', isBreak: false },
    { time: '12:35 - 1:20', subject: 'Educación Física', teacher: 'José Benavides', icon: '⚽', isBreak: false }
  ],
  'Jueves': [
    { time: '7:00 - 7:50', subject: 'Inglés', teacher: 'Ángela Rendón', icon: '🇬🇧', isBreak: false },
    { time: '7:50 - 8:40', subject: 'Ética', teacher: 'Irnalda Tintinago', icon: '🤝', isBreak: false },
    { time: '8:40 - 9:30', subject: 'Química', teacher: 'Anna Fulí', icon: '🧪', isBreak: false },
    { time: '9:30 - 10:10', subject: 'Descanso', teacher: 'Comunidad Escolar', icon: '🥪', isBreak: true },
    { time: '10:10 - 11:00', subject: 'Química', teacher: 'Anna Fulí', icon: '🧪', isBreak: false },
    { time: '11:00 - 11:50', subject: 'Ciencias Sociales', teacher: 'Ibón Ocampo', icon: '🌍', isBreak: false },
    { time: '11:50 - 12:35', subject: 'Religión', teacher: 'Mónica García', icon: '🕊️', isBreak: false },
    { time: '12:35 - 1:20', subject: 'Español', teacher: 'Katherine Castro', icon: '📖', isBreak: false }
  ],
  'Viernes': [
    { time: '7:00 - 7:50', subject: 'Inglés', teacher: 'Ángela Rendón', icon: '🇬🇧', isBreak: false },
    { time: '7:50 - 8:40', subject: 'Razonamiento Matemático', teacher: 'Víctor Toro', icon: '🔢', isBreak: false },
    { time: '8:40 - 9:30', subject: 'Español', teacher: 'Katherine Castro', icon: '📖', isBreak: false },
    { time: '9:30 - 10:10', subject: 'Descanso', teacher: 'Comunidad Escolar', icon: '🥪', isBreak: true },
    { time: '10:10 - 11:00', subject: 'Biología', teacher: 'Anna Fulí', icon: '🔬', isBreak: false },
    { time: '11:00 - 11:50', subject: 'Biología', teacher: 'Anna Fulí', icon: '🔬', isBreak: false },
    { time: '11:50 - 12:35', subject: 'Matemáticas', teacher: 'Manuel Muñoz', icon: '➕', isBreak: false },
    { time: '12:35 - 1:20', subject: 'Dirección de grupo', teacher: 'Manuel Muñoz', icon: '👨‍🏫', isBreak: false }
  ]
};

const DEFAULT_TEACHER_DIRECTORY = [
  {
    name: 'Hna. Myriam Marmolejo',
    role: 'Rectora',
    subject: 'Rectoría & Dirección General',
    subjectsIn7th: '',
    email: null,
    attentionDay: 'Lunes a Viernes',
    attentionHours: '9:00 AM a 12:00 M',
    avatarEmoji: '👩‍💼'
  },
  {
    name: 'Marisol Ruiz',
    role: 'Coordinadora Académica',
    subject: 'Coordinación Académica & Pedagógica',
    subjectsIn7th: '',
    email: 'coordinacionacademica@colegiohogarmadrededios.edu.co',
    attentionDay: 'Lunes a Jueves',
    attentionHours: '2:00 PM a 4:00 PM',
    avatarEmoji: '👩‍🏫'
  },
  {
    name: 'Irnalda Tintinago',
    role: 'Docente & Psicóloga',
    subject: 'Cátedra emocional, Ética',
    subjectsIn7th: 'Cátedra emocional, Ética',
    email: 'irnalda.tintinago@colegiohogarmadrededios.edu.co',
    attentionDay: 'Miércoles',
    attentionHours: '12:30 PM - 1:20 PM',
    avatarEmoji: '💛'
  },
  {
    name: 'Sirley Palta',
    role: 'Director de grado Transición',
    subject: 'Estética',
    subjectsIn7th: 'Estética',
    email: 'anyi.palta@colegiohogarmadrededios.edu.co',
    attentionDay: 'Lunes',
    attentionHours: '12:30 PM - 1:20 PM',
    avatarEmoji: '🎨'
  },
  {
    name: 'Martha Campo',
    role: 'Director de grado 1°',
    subject: 'Docente Titular Primaria',
    subjectsIn7th: '',
    email: null,
    attentionDay: 'Martes',
    attentionHours: '1:20 PM - 2:00 PM',
    avatarEmoji: '👩‍🏫'
  },
  {
    name: 'Mónica García',
    role: 'Director de grado 2°',
    subject: 'Religión',
    subjectsIn7th: 'Religión',
    email: 'monica.garcia@colegiohogarmadrededios.edu.co',
    attentionDay: 'Jueves',
    attentionHours: '12:30 PM - 1:20 PM',
    avatarEmoji: '🕊️'
  },
  {
    name: 'Danilo Daza',
    role: 'Director de grado 3°',
    subject: 'Música',
    subjectsIn7th: 'Música',
    email: 'danilo.daza@colegiohogarmadrededios.edu.co',
    attentionDay: 'Miércoles',
    attentionHours: '12:30 PM - 1:20 PM',
    avatarEmoji: '🎵'
  },
  {
    name: 'Ángela Rendón',
    role: 'Director de grado 4A',
    subject: 'Inglés',
    subjectsIn7th: 'Inglés',
    email: 'angela.rendon@colegiohogarmadrededios.edu.co',
    attentionDay: 'Jueves',
    attentionHours: '1:20 PM - 2:00 PM',
    avatarEmoji: '🇬🇧'
  },
  {
    name: 'Katherine Castro',
    role: 'Director de grado 4B',
    subject: 'Español',
    subjectsIn7th: 'Español',
    email: 'Katherine.castro@colegiohogarmadrededios.edu.co',
    attentionDay: 'Jueves',
    attentionHours: '1:20 PM - 2:00 PM',
    avatarEmoji: '📖'
  },
  {
    name: 'María Piedad Rodríguez',
    role: 'Director de grado 5°',
    subject: 'Docente Titular Primaria',
    subjectsIn7th: '',
    email: null,
    attentionDay: 'Martes',
    attentionHours: '12:30 PM - 1:20 PM',
    avatarEmoji: '👩‍🏫'
  },
  {
    name: 'Ibón Ocampo',
    role: 'Director de grado 6°',
    subject: 'Ciencias Sociales',
    subjectsIn7th: 'Ciencias Sociales',
    email: 'ibon.ocampo@colegiohogarmadrededios.edu.co',
    attentionDay: 'Martes',
    attentionHours: '1:20 PM - 2:00 PM',
    avatarEmoji: '🌍'
  },
  {
    name: 'Manuel Muñoz',
    role: 'Director de grado 7°',
    subject: 'Matemáticas, Tecnología e Informática',
    subjectsIn7th: 'Matemáticas, Tecnología e Informática',
    email: 'nformaticachmd@colegiohogarmadrededios.edu.co',
    attentionDay: 'Miércoles',
    attentionHours: '12:30 PM - 1:20 PM',
    avatarEmoji: '👨‍🏫'
  },
  {
    name: 'José Benavides',
    role: 'Director de grado 8°',
    subject: 'Educación Física',
    subjectsIn7th: 'Educación Física',
    email: 'jose.benavides@colegiohogarmadrededios.edu.co',
    attentionDay: 'Martes',
    attentionHours: '12:30 PM - 1:20 PM',
    avatarEmoji: '⚽'
  },
  {
    name: 'Víctor Toro',
    role: 'Director de grado 9°',
    subject: 'Física, Razonamiento Matemático',
    subjectsIn7th: 'Física, Razonamiento Matemático',
    email: 'victor.toro@colegiohogarmadrededios.edu.co',
    attentionDay: 'Jueves',
    attentionHours: '1:20 PM - 2:00 PM',
    avatarEmoji: '⚡'
  },
  {
    name: 'Carlos Erazo',
    role: 'Director de grado 10°',
    subject: 'Geometría',
    subjectsIn7th: 'Geometría',
    email: 'carlos.erazo@colegiohogarmadrededios.edu.co',
    attentionDay: 'Lunes',
    attentionHours: '12:30 PM - 1:20 PM',
    avatarEmoji: '📐'
  },
  {
    name: 'Anna Fulí',
    role: 'Director de grado 11°',
    subject: 'Biología, Química',
    subjectsIn7th: 'Biología, Química',
    email: 'ana.fuli@colegiohogarmadrededios.edu.co',
    attentionDay: 'Miércoles',
    attentionHours: '12:30 PM - 1:20 PM',
    avatarEmoji: '🔬'
  }
];

const DEFAULT_OFFICIAL_CALENDAR_EVENTS = [
  // SEPTIEMBRE
  {
    id: 'ev_sep_01',
    month: 'Septiembre',
    dateStr: 'Septiembre 1',
    dayNumber: '1',
    monthShort: 'SEP',
    title: 'Bienvenida escolar',
    time: '',
    location: '',
    category: 'INSTITUTIONAL',
    categoryLabel: 'Institucional 🏫',
    description: '',
    emoji: '🎒',
    colorHex: '#2563eb'
  },
  {
    id: 'ev_sep_03',
    month: 'Septiembre',
    dateStr: 'Septiembre 3',
    dayNumber: '3',
    monthShort: 'SEP',
    title: 'Reunión de padres de familia',
    time: '5:00 PM',
    location: '',
    category: 'COMMUNITY',
    categoryLabel: 'Padres de Familia 👨‍👩‍👧',
    description: '',
    emoji: '👨‍👩‍👧',
    colorHex: '#7c3aed'
  },
  {
    id: 'ev_sep_11',
    month: 'Septiembre',
    dateStr: 'Septiembre 11',
    dayNumber: '11',
    monthShort: 'SEP',
    title: 'Pascua madre Elisa • Día del estudiante elisiano',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Celebración Elisiana 🕊️',
    description: '',
    emoji: '⭐',
    colorHex: '#f59e0b'
  },
  {
    id: 'ev_sep_14',
    month: 'Septiembre',
    dateStr: 'Septiembre 14',
    dayNumber: '14',
    monthShort: 'SEP',
    title: 'Presentación personeras',
    time: '',
    location: '',
    category: 'CIVIC',
    categoryLabel: 'Democracia Escolar 🗳️',
    description: '',
    emoji: '📢',
    colorHex: '#10b981'
  },
  {
    id: 'ev_sep_18',
    month: 'Septiembre',
    dateStr: 'Septiembre 18',
    dayNumber: '18',
    monthShort: 'SEP',
    title: 'Amor y amistad - Jeanday',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Jean Day 👖',
    description: '',
    emoji: '👖',
    colorHex: '#ec4899'
  },
  {
    id: 'ev_sep_25',
    month: 'Septiembre',
    dateStr: 'Septiembre 25',
    dayNumber: '25',
    monthShort: 'SEP',
    title: 'Menú especial de la cafetería',
    time: '',
    location: '',
    category: 'CAFETERIA',
    categoryLabel: 'Menú Cafetería 🍔',
    description: '',
    emoji: '🍔',
    colorHex: '#f97316'
  },
  {
    id: 'ev_sep_30',
    month: 'Septiembre',
    dateStr: 'Septiembre 30',
    dayNumber: '30',
    monthShort: 'SEP',
    title: 'Debate personeras',
    time: '',
    location: '',
    category: 'CIVIC',
    categoryLabel: 'Democracia Escolar 🗳️',
    description: '',
    emoji: '🎙️',
    colorHex: '#6366f1'
  },

  // OCTUBRE
  {
    id: 'ev_oct_02',
    month: 'Octubre',
    dateStr: 'Octubre 2',
    dayNumber: '2',
    monthShort: 'OCT',
    title: 'Elección de la personera',
    time: '',
    location: '',
    category: 'CIVIC',
    categoryLabel: 'Elecciones 🗳️',
    description: '',
    emoji: '🗳️',
    colorHex: '#10b981'
  },
  {
    id: 'ev_oct_05',
    month: 'Octubre',
    dateStr: 'Octubre 5 - 9',
    dayNumber: '5-9',
    monthShort: 'OCT',
    title: 'Semana de receso escolar',
    time: '',
    location: '',
    category: 'INSTITUTIONAL',
    categoryLabel: 'Receso Escolar 🌴',
    description: 'Octubre 5 - 9',
    emoji: '🌴',
    colorHex: '#059669'
  },
  {
    id: 'ev_oct_14',
    month: 'Octubre',
    dateStr: 'Octubre 14',
    dayNumber: '14',
    monthShort: 'OCT',
    title: 'Izada de bandera',
    time: '',
    location: '',
    category: 'CIVIC',
    categoryLabel: 'Acto Cívico 🇨🇴',
    description: '',
    emoji: '🇨🇴',
    colorHex: '#2563eb'
  },
  {
    id: 'ev_oct_15',
    month: 'Octubre',
    dateStr: 'Octubre 15',
    dayNumber: '15',
    monthShort: 'OCT',
    title: 'Escuela de padres',
    time: '5:00 PM',
    location: '',
    category: 'COMMUNITY',
    categoryLabel: 'Escuela de Padres 👨‍👩‍👧',
    description: 'Hora : 5:00pm',
    emoji: '👨‍👩‍👧',
    colorHex: '#7c3aed'
  },
  {
    id: 'ev_oct_26',
    month: 'Octubre',
    dateStr: 'Octubre 26 - 30',
    dayNumber: '26-30',
    monthShort: 'OCT',
    title: 'Evaluaciones de primer periodo',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Exámenes 📝',
    description: 'Octubre 26 - 30',
    emoji: '📝',
    colorHex: '#dc2626'
  },
  {
    id: 'ev_oct_30',
    month: 'Octubre',
    dateStr: 'Octubre 30',
    dayNumber: '30',
    monthShort: 'OCT',
    title: 'Jean day - Menú especial cafeteria',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Jean Day & Menú 🍕',
    description: '',
    emoji: '🍕',
    colorHex: '#f97316'
  },

  // NOVIEMBRE
  {
    id: 'ev_nov_03',
    month: 'Noviembre',
    dateStr: 'Noviembre 3 y 4',
    dayNumber: '3-4',
    monthShort: 'NOV',
    title: 'Actividades de refuerzo',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Refuerzos 📚',
    description: 'Noviembre 3 y 4',
    emoji: '📖',
    colorHex: '#d97706'
  },
  {
    id: 'ev_nov_05',
    month: 'Noviembre',
    dateStr: 'Noviembre 5',
    dayNumber: '5',
    monthShort: 'NOV',
    title: 'Inicia segundo periodo',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Periodo Académico 🚀',
    description: '',
    emoji: '🚀',
    colorHex: '#2563eb'
  },
  {
    id: 'ev_nov_12',
    month: 'Noviembre',
    dateStr: 'Noviembre 12',
    dayNumber: '12',
    monthShort: 'NOV',
    title: 'Izada de bandera rendimiento academico ( a cargo de grado 7°)',
    time: '',
    location: '',
    category: 'CIVIC',
    categoryLabel: 'Cuadro de Honor 🌟',
    description: 'A cargo de grado 7°',
    emoji: '🌟',
    colorHex: '#10b981'
  },
  {
    id: 'ev_nov_20',
    month: 'Noviembre',
    dateStr: 'Noviembre 20',
    dayNumber: '20',
    monthShort: 'NOV',
    title: '5 festival folclorico Elisiano',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Festival Folclórico 🎭',
    description: '',
    emoji: '🎭',
    colorHex: '#9333ea'
  },
  {
    id: 'ev_nov_26',
    month: 'Noviembre',
    dateStr: 'Noviembre 26',
    dayNumber: '26',
    monthShort: 'NOV',
    title: 'Inicio de las novenas de navidad',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Novenas Navideñas 🎄',
    description: '',
    emoji: '🎄',
    colorHex: '#16a34a'
  },
  {
    id: 'ev_nov_27',
    month: 'Noviembre',
    dateStr: 'Noviembre 27',
    dayNumber: '27',
    monthShort: 'NOV',
    title: 'Menú especial de la cafeteria',
    time: '',
    location: '',
    category: 'CAFETERIA',
    categoryLabel: 'Menú Cafetería 🍗',
    description: '',
    emoji: '🍗',
    colorHex: '#ea580c'
  },

  // DICIEMBRE
  {
    id: 'ev_dic_04',
    month: 'Diciembre',
    dateStr: 'Diciembre 4',
    dayNumber: '4',
    monthShort: 'DIC',
    title: 'Entrega de boletines (No hay clases)',
    time: '6:45 AM',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Entrega de Boletines 📊',
    description: 'No hay clases',
    emoji: '📊',
    colorHex: '#2563eb'
  },
  {
    id: 'ev_dic_07',
    month: 'Diciembre',
    dateStr: 'Diciembre 7',
    dayNumber: '7',
    monthShort: 'DIC',
    title: 'Novena grado 7° y 8°',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Novena 🕯️',
    description: '',
    emoji: '🕯️',
    colorHex: '#f59e0b'
  },
  {
    id: 'ev_dic_10',
    month: 'Diciembre',
    dateStr: 'Diciembre 10',
    dayNumber: '10',
    monthShort: 'DIC',
    title: 'Vacaciones de navidad',
    time: '',
    location: '',
    category: 'INSTITUTIONAL',
    categoryLabel: 'Vacaciones 🎅',
    description: '',
    emoji: '🎅',
    colorHex: '#dc2626'
  },

  // ENERO
  {
    id: 'ev_ene_12',
    month: 'Enero',
    dateStr: 'Enero 12',
    dayNumber: '12',
    monthShort: 'ENE',
    title: 'Inicio de clases',
    time: '',
    location: '',
    category: 'INSTITUTIONAL',
    categoryLabel: 'Regreso a Clases 🎒',
    description: '',
    emoji: '🎒',
    colorHex: '#2563eb'
  },
  {
    id: 'ev_ene_14',
    month: 'Enero',
    dateStr: 'Enero 14',
    dayNumber: '14',
    monthShort: 'ENE',
    title: 'Izada de bandera',
    time: '',
    location: '',
    category: 'CIVIC',
    categoryLabel: 'Acto Cívico 🇨🇴',
    description: '',
    emoji: '🇨🇴',
    colorHex: '#10b981'
  },
  {
    id: 'ev_ene_27',
    month: 'Enero',
    dateStr: 'Enero 27 - Febrero 2',
    dayNumber: '27-2',
    monthShort: 'ENE',
    title: 'Evaluaciones de segundo periodo',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Exámenes 📝',
    description: 'Enero 27 - febrero 2',
    emoji: '📝',
    colorHex: '#dc2626'
  },
  {
    id: 'ev_ene_29',
    month: 'Enero',
    dateStr: 'Enero 29',
    dayNumber: '29',
    monthShort: 'ENE',
    title: 'Menú especial de la cafeteria',
    time: '',
    location: '',
    category: 'CAFETERIA',
    categoryLabel: 'Menú Cafetería 🌮',
    description: '',
    emoji: '🌮',
    colorHex: '#f97316'
  },

  // FEBRERO
  {
    id: 'ev_feb_03',
    month: 'Febrero',
    dateStr: 'Febrero 3 y 4',
    dayNumber: '3-4',
    monthShort: 'FEB',
    title: 'Actividades de refuerzo',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Refuerzos 📖',
    description: 'Febrero 3 y 4',
    emoji: '📖',
    colorHex: '#d97706'
  },
  {
    id: 'ev_feb_08',
    month: 'Febrero',
    dateStr: 'Febrero 8',
    dayNumber: '8',
    monthShort: 'FEB',
    title: 'Inicio de tercer periodo',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Periodo Académico 🚀',
    description: '',
    emoji: '🚀',
    colorHex: '#2563eb'
  },
  {
    id: 'ev_feb_09',
    month: 'Febrero',
    dateStr: 'Febrero 9',
    dayNumber: '9',
    monthShort: 'FEB',
    title: 'Izada de bandera rendimiento academico',
    time: '',
    location: '',
    category: 'CIVIC',
    categoryLabel: 'Cuadro de Honor 🌟',
    description: '',
    emoji: '🌟',
    colorHex: '#10b981'
  },
  {
    id: 'ev_feb_10',
    month: 'Febrero',
    dateStr: 'Febrero 10',
    dayNumber: '10',
    monthShort: 'FEB',
    title: 'Entrega de boletines',
    time: '5:00 PM',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Entrega de Boletines 📊',
    description: 'Hora:5:00pm',
    emoji: '📊',
    colorHex: '#7c3aed'
  },
  {
    id: 'ev_feb_18',
    month: 'Febrero',
    dateStr: 'Febrero 18',
    dayNumber: '18',
    monthShort: 'FEB',
    title: 'Escuela de padres',
    time: '5:00 PM',
    location: '',
    category: 'COMMUNITY',
    categoryLabel: 'Escuela de Padres 👨‍👩‍👧',
    description: 'Hora 5:00pm',
    emoji: '👨‍👩‍👧',
    colorHex: '#7c3aed'
  },
  {
    id: 'ev_feb_26',
    month: 'Febrero',
    dateStr: 'Febrero 26',
    dayNumber: '26',
    monthShort: 'FEB',
    title: 'Menú especial cafeteria',
    time: '',
    location: '',
    category: 'CAFETERIA',
    categoryLabel: 'Menú Cafetería 🍔',
    description: '',
    emoji: '🍔',
    colorHex: '#f97316'
  },

  // MARZO
  {
    id: 'ev_mar_08',
    month: 'Marzo',
    dateStr: 'Marzo 8',
    dayNumber: '8',
    monthShort: 'MAR',
    title: 'Dia de la mujer',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Conmemoración 💐',
    description: '',
    emoji: '💐',
    colorHex: '#ec4899'
  },
  {
    id: 'ev_mar_14',
    month: 'Marzo',
    dateStr: 'Marzo 14',
    dayNumber: '14',
    monthShort: 'MAR',
    title: 'Bazar de la familia elisiana',
    time: '',
    location: '',
    category: 'COMMUNITY',
    categoryLabel: 'Bazar Familiar 🎪',
    description: '',
    emoji: '🎪',
    colorHex: '#f59e0b'
  },
  {
    id: 'ev_mar_15',
    month: 'Marzo',
    dateStr: 'Marzo 15',
    dayNumber: '15',
    monthShort: 'MAR',
    title: 'No hay clase',
    time: '',
    location: '',
    category: 'INSTITUTIONAL',
    categoryLabel: 'Sin Clases 🛌',
    description: '',
    emoji: '🛌',
    colorHex: '#6b7280'
  },
  {
    id: 'ev_mar_16',
    month: 'Marzo',
    dateStr: 'Marzo 16 al 19',
    dayNumber: '16-19',
    monthShort: 'MAR',
    title: 'Semana elisiana',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Semana Patronal 🎉',
    description: 'Marzo 16 al 19',
    emoji: '🎉',
    colorHex: '#8b5cf6'
  },
  {
    id: 'ev_mar_18',
    month: 'Marzo',
    dateStr: 'Marzo 18',
    dayNumber: '18',
    monthShort: 'MAR',
    title: 'Semana elisiana responsable 5°,6°,7°',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Actividades Grados 🏆',
    description: '',
    emoji: '🏆',
    colorHex: '#8b5cf6'
  },
  {
    id: 'ev_mar_19',
    month: 'Marzo',
    dateStr: 'Marzo 19',
    dayNumber: '19',
    monthShort: 'MAR',
    title: 'Menú especial de la cafeteria',
    time: '',
    location: '',
    category: 'CAFETERIA',
    categoryLabel: 'Menú Cafetería 🍕',
    description: '',
    emoji: '🍕',
    colorHex: '#f97316'
  },
  {
    id: 'ev_mar_22',
    month: 'Marzo',
    dateStr: 'Marzo 22 - 26',
    dayNumber: '22-26',
    monthShort: 'MAR',
    title: 'Semana santa',
    time: '',
    location: '',
    category: 'INSTITUTIONAL',
    categoryLabel: 'Semana Santa 🕊️',
    description: 'Marzo 22 - 26',
    emoji: '🕊️',
    colorHex: '#059669'
  },
  {
    id: 'ev_mar_29',
    month: 'Marzo',
    dateStr: 'Marzo 29',
    dayNumber: '29',
    monthShort: 'MAR',
    title: 'Reinicio de labores escolares',
    time: '',
    location: '',
    category: 'INSTITUTIONAL',
    categoryLabel: 'Regreso a Clases 🎒',
    description: '',
    emoji: '🎒',
    colorHex: '#2563eb'
  },

  // ABRIL
  {
    id: 'ev_abr_07',
    month: 'Abril',
    dateStr: 'Abril 7-13',
    dayNumber: '7-13',
    monthShort: 'ABR',
    title: 'Evaluaciones de tercer periodo',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Exámenes 📝',
    description: 'Abril 7-13',
    emoji: '📝',
    colorHex: '#dc2626'
  },
  {
    id: 'ev_abr_14',
    month: 'Abril',
    dateStr: 'Abril 14-15',
    dayNumber: '14-15',
    monthShort: 'ABR',
    title: 'Actividades de refuerzo',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Refuerzos 📖',
    description: 'Abril 14-15',
    emoji: '📖',
    colorHex: '#d97706'
  },
  {
    id: 'ev_abr_16',
    month: 'Abril',
    dateStr: 'Abril 16',
    dayNumber: '16',
    monthShort: 'ABR',
    title: 'Inicia el 4to periodo',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Periodo Final 🏁',
    description: '',
    emoji: '🏁',
    colorHex: '#2563eb'
  },
  {
    id: 'ev_abr_23',
    month: 'Abril',
    dateStr: 'Abril 23',
    dayNumber: '23',
    monthShort: 'ABR',
    title: 'Izada de bandera dia del idioma - Menú especial de la cafeteria',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Día del Idioma & Menú 📚',
    description: '',
    emoji: '📚',
    colorHex: '#10b981'
  },
  {
    id: 'ev_abr_28',
    month: 'Abril',
    dateStr: 'Abril 28',
    dayNumber: '28',
    monthShort: 'ABR',
    title: 'Entrega de boletines personalizada',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Boletines 📋',
    description: '',
    emoji: '📋',
    colorHex: '#7c3aed'
  },

  // MAYO
  {
    id: 'ev_may_13',
    month: 'Mayo',
    dateStr: 'Mayo 13',
    dayNumber: '13',
    monthShort: 'MAY',
    title: 'Celebracion dia del maestro - Jeanday',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Día del Maestro 👨‍🏫',
    description: '',
    emoji: '👨‍🏫',
    colorHex: '#6366f1'
  },
  {
    id: 'ev_may_14',
    month: 'Mayo',
    dateStr: 'Mayo 14',
    dayNumber: '14',
    monthShort: 'MAY',
    title: 'No hay clase',
    time: '',
    location: '',
    category: 'INSTITUTIONAL',
    categoryLabel: 'Sin Clases 🏖️',
    description: '',
    emoji: '🏖️',
    colorHex: '#6b7280'
  },
  {
    id: 'ev_may_17',
    month: 'Mayo',
    dateStr: 'Mayo 17 - 21',
    dayNumber: '17-21',
    monthShort: 'MAY',
    title: 'Semana de la familia',
    time: '',
    location: '',
    category: 'CULTURAL',
    categoryLabel: 'Semana de la Familia 👨‍👩‍👧',
    description: 'Mayo 17 - 21',
    emoji: '👨‍👩‍👧',
    colorHex: '#ec4899'
  },
  {
    id: 'ev_may_21',
    month: 'Mayo',
    dateStr: 'Mayo 21',
    dayNumber: '21',
    monthShort: 'MAY',
    title: 'Dia de la familia',
    time: '',
    location: '',
    category: 'COMMUNITY',
    categoryLabel: 'Día de la Familia 💖',
    description: '',
    emoji: '💖',
    colorHex: '#ec4899'
  },
  {
    id: 'ev_may_27',
    month: 'Mayo',
    dateStr: 'Mayo 27',
    dayNumber: '27',
    monthShort: 'MAY',
    title: 'Menú especial de la cafeteria',
    time: '',
    location: '',
    category: 'CAFETERIA',
    categoryLabel: 'Menú Cafetería 🍨',
    description: '',
    emoji: '🍨',
    colorHex: '#f97316'
  },
  {
    id: 'ev_may_28',
    month: 'Mayo',
    dateStr: 'Mayo 28',
    dayNumber: '28',
    monthShort: 'MAY',
    title: 'Entrega de insignias',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Entrega de Insignias 🎖️',
    description: '',
    emoji: '🎖️',
    colorHex: '#f59e0b'
  },

  // JUNIO
  {
    id: 'ev_jun_08',
    month: 'Junio',
    dateStr: 'Junio 8-14',
    dayNumber: '8-14',
    monthShort: 'JUN',
    title: 'Evaluaciones de cuarto periodo',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Exámenes Finales 📝',
    description: 'Junio 8-14',
    emoji: '📝',
    colorHex: '#dc2626'
  },
  {
    id: 'ev_jun_15',
    month: 'Junio',
    dateStr: 'Junio 15-16',
    dayNumber: '15-16',
    monthShort: 'JUN',
    title: 'Actividades de refuerzo',
    time: '',
    location: '',
    category: 'ACADEMIC',
    categoryLabel: 'Refuerzos Finales 📚',
    description: 'Junio 15-16',
    emoji: '📚',
    colorHex: '#d97706'
  },
  {
    id: 'ev_jun_23',
    month: 'Junio',
    dateStr: 'Junio 23',
    dayNumber: '23',
    monthShort: 'JUN',
    title: 'Clausuras',
    time: '',
    location: '',
    category: 'INSTITUTIONAL',
    categoryLabel: 'Clausuras 🎓',
    description: '',
    emoji: '🎓',
    colorHex: '#10b981'
  }
];

// Load persisted Schedule or fallback to default
let OFFICIAL_WEEKLY_SCHEDULE;
try {
  const savedSched = localStorage.getItem('escolaris_weekly_schedule');
  OFFICIAL_WEEKLY_SCHEDULE = savedSched ? JSON.parse(savedSched) : JSON.parse(JSON.stringify(DEFAULT_WEEKLY_SCHEDULE));
} catch (e) {
  OFFICIAL_WEEKLY_SCHEDULE = JSON.parse(JSON.stringify(DEFAULT_WEEKLY_SCHEDULE));
}

// Sanitize schedule: remove rooms, set breaks to 'Descanso', and update Thursday Biology -> Chemistry
if (OFFICIAL_WEEKLY_SCHEDULE && typeof OFFICIAL_WEEKLY_SCHEDULE === 'object') {
  Object.keys(OFFICIAL_WEEKLY_SCHEDULE).forEach(day => {
    const slots = OFFICIAL_WEEKLY_SCHEDULE[day] || [];
    slots.forEach(slot => {
      delete slot.room;
      if (slot.isBreak) {
        slot.subject = 'Descanso';
      }
      if (day === 'Jueves') {
        const sub = (slot.subject || '').toLowerCase();
        if (!slot.isBreak && (sub.includes('biolog') || slot.time === '8:40 - 9:30' || slot.time === '10:10 - 11:00')) {
          slot.subject = 'Química';
          slot.icon = '🧪';
          slot.teacher = 'Anna Fulí';
        }
      }
      if (slot.teacher) {
        slot.teacher = formatTeacherShortName(slot.teacher);
      }
    });
  });
}

function formatTeacherShortName(fullName) {
  if (!fullName) return '';
  const map = {
    'Hna. Myriam Marmolejo Millán': 'Hna. Myriam Marmolejo',
    'Lic. Marisol Ruiz Arango': 'Marisol Ruiz',
    'Manuel Alejandro Muñoz': 'Manuel Muñoz',
    'Manuel Alejandro Muñoz Palomino': 'Manuel Muñoz',
    'Anna Carolina Fulí': 'Anna Fulí',
    'Carlos Libardo Erazo': 'Carlos Erazo',
    'Ángela María Rendón': 'Ángela Rendón',
    'Katherine Vanessa Castro': 'Katherine Castro',
    'José Luis Benavides': 'José Benavides',
    'Martha Lucía Campo': 'Martha Campo'
  };
  return map[fullName] || fullName;
}

// Load persisted Teacher Directory or fallback to default
let TEACHER_DIRECTORY;
try {
  const savedTeachers = localStorage.getItem('escolaris_teacher_directory');
  if (savedTeachers) {
    TEACHER_DIRECTORY = JSON.parse(savedTeachers);
    // Sanitize subjectsIn7th and emails
    TEACHER_DIRECTORY.forEach(t => {
      t.name = formatTeacherShortName(t.name);
      if (t.name.includes('Marisol')) {
        t.email = 'coordinacionacademica@colegiohogarmadrededios.edu.co';
      }
      if (t.name.includes('Manuel')) {
        t.email = 'nformaticachmd@colegiohogarmadrededios.edu.co';
        t.subjectsIn7th = 'Matemáticas, Tecnología e Informática';
      }
      if (t.name.includes('Myriam') || t.name.includes('Marisol') || t.name.includes('Martha') || t.name.includes('Piedad')) {
        t.subjectsIn7th = '';
      }
      if (t.name.includes('Anna')) {
        t.subject = 'Biología, Química';
        t.subjectsIn7th = 'Biología, Química';
      }
    });
  } else {
    TEACHER_DIRECTORY = JSON.parse(JSON.stringify(DEFAULT_TEACHER_DIRECTORY));
  }
} catch (e) {
  TEACHER_DIRECTORY = JSON.parse(JSON.stringify(DEFAULT_TEACHER_DIRECTORY));
}

// Load persisted School Calendar Events or fallback to default
let OFFICIAL_CALENDAR_EVENTS;
try {
  const savedEvents = localStorage.getItem('escolaris_calendar_events');
  OFFICIAL_CALENDAR_EVENTS = savedEvents ? JSON.parse(savedEvents) : JSON.parse(JSON.stringify(DEFAULT_OFFICIAL_CALENDAR_EVENTS));
} catch (e) {
  OFFICIAL_CALENDAR_EVENTS = JSON.parse(JSON.stringify(DEFAULT_OFFICIAL_CALENDAR_EVENTS));
}

// Restauración Garantizada del evento del 3 de Septiembre (Reunión de Padres)
const sep3OfficialEvent = DEFAULT_OFFICIAL_CALENDAR_EVENTS.find(e => e.id === 'ev_sep_03');
if (sep3OfficialEvent && Array.isArray(OFFICIAL_CALENDAR_EVENTS)) {
  const hasSep3 = OFFICIAL_CALENDAR_EVENTS.some(e => e.id === 'ev_sep_03' || (e.dayNumber === '3' && (e.month === 'Septiembre' || e.monthShort === 'SEP')));
  if (!hasSep3) {
    OFFICIAL_CALENDAR_EVENTS.splice(1, 0, JSON.parse(JSON.stringify(sep3OfficialEvent)));
    saveCalendarEventsToStorage();
  }
}

function saveScheduleToStorage() {
  try {
    localStorage.setItem('escolaris_weekly_schedule', JSON.stringify(OFFICIAL_WEEKLY_SCHEDULE));
  } catch (e) {
    console.error("Error saving schedule to storage:", e);
  }
}
const saveWeeklyScheduleToStorage = saveScheduleToStorage;

function saveTeacherDirectoryToStorage() {
  try {
    localStorage.setItem('escolaris_teacher_directory', JSON.stringify(TEACHER_DIRECTORY));
  } catch (e) {
    console.error("Error saving teacher directory to storage:", e);
  }
}

function saveCalendarEventsToStorage() {
  try {
    localStorage.setItem('escolaris_calendar_events', JSON.stringify(OFFICIAL_CALENDAR_EVENTS));
  } catch (e) {
    console.error("Error saving calendar events to storage:", e);
  }
}

function renderSchedule() {
  const container = document.getElementById('schedule-classes-list');
  if (!container) return;

  const isTeacher = currentUser && currentUser.role === 'TEACHER';
  const addSlotBtn = document.getElementById('btn-add-class-slot');
  if (addSlotBtn) addSlotBtn.style.display = isTeacher ? 'inline-flex' : 'none';

  const classes = OFFICIAL_WEEKLY_SCHEDULE[activeScheduleDay] || [];
  if (classes.length === 0) {
    container.innerHTML = `
      <div class="clay-card text-center" style="padding:20px;">
        <span style="font-size:30px;">📅</span>
        <p style="font-size:13px; color:var(--text-muted); margin-top:8px;">No hay bloques programados para el ${activeScheduleDay}.</p>
        ${isTeacher ? `<button class="btn-primary btn-sm" onclick="openAddClassSlotModal()" style="margin-top:8px;">+ Agregar Bloque</button>` : ''}
      </div>
    `;
    return;
  }

  container.innerHTML = classes.map((c, index) => {
    if (c.isBreak) {
      return `
        <div class="clay-card" style="padding:10px 14px; background:#FFFBEB; border:1px dashed #F59E0B;">
          <div style="display:flex; justify-content:space-between; align-items:center; gap:8px;">
            <div style="display:flex; align-items:center; gap:10px; flex:1; min-width:0;">
              <span style="font-size:20px; line-height:1;">${c.icon || '🥪'}</span>
              <div style="min-width:0;">
                <strong style="color:#B45309; font-size:14px; display:block; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${escapeHtml(c.subject)}</strong>
              </div>
            </div>
            <div style="display:flex; align-items:center; gap:6px; flex-shrink:0;">
              <span style="font-size:11.5px; font-weight:800; color:#B45309; background:#FEF3C7; padding:3px 8px; border-radius:6px; white-space:nowrap;">${c.time}</span>
              ${isTeacher ? `
                <button class="btn-secondary btn-sm" style="padding:3px 7px; font-size:11px;" onclick="openEditClassSlotModal('${activeScheduleDay}', ${index})" title="Editar bloque">✏️</button>
                <button class="btn-secondary btn-sm btn-danger-outline" style="padding:3px 7px; font-size:11px;" onclick="deleteClassSlot('${activeScheduleDay}', ${index})" title="Eliminar bloque">🗑️</button>
              ` : ''}
            </div>
          </div>
        </div>
      `;
    }

    return `
      <div class="clay-card" style="padding:10px 14px; border-left:4px solid var(--primary);">
        <div style="display:flex; justify-content:space-between; align-items:center; gap:8px;">
          <div style="display:flex; align-items:center; gap:10px; flex:1; min-width:0;">
            <span style="font-size:20px; line-height:1;">${c.icon || '📚'}</span>
            <div style="min-width:0;">
              <strong style="font-size:14px; color:var(--text-main); display:block; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${escapeHtml(c.subject)}</strong>
              <div style="font-size:11.5px; color:var(--text-muted); margin-top:2px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
                <span>👨‍🏫 Prof. ${escapeHtml(c.teacher)}</span>
              </div>
            </div>
          </div>
          <div style="display:flex; align-items:center; gap:6px; flex-shrink:0;">
            <span style="font-size:11.5px; font-weight:800; color:var(--primary); background:rgba(37,99,235,0.08); padding:3px 8px; border-radius:6px; white-space:nowrap;">
              ${c.time}
            </span>
            ${isTeacher ? `
              <button class="btn-secondary btn-sm" style="padding:3px 7px; font-size:11px;" onclick="openEditClassSlotModal('${activeScheduleDay}', ${index})" title="Editar Bloque">✏️</button>
              <button class="btn-secondary btn-sm btn-danger-outline" style="padding:3px 7px; font-size:11px;" onclick="deleteClassSlot('${activeScheduleDay}', ${index})" title="Eliminar Bloque">🗑️</button>
            ` : ''}
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function openAddClassSlotModal() {
  const daySelect = document.getElementById('edit-slot-select-day');
  const slotDay = document.getElementById('edit-slot-day');
  const slotIndex = document.getElementById('edit-slot-index');
  const title = document.getElementById('edit-slot-modal-title');
  const timeIn = document.getElementById('edit-slot-time');
  const subjIn = document.getElementById('edit-slot-subject');
  const teacherIn = document.getElementById('edit-slot-teacher');
  const iconIn = document.getElementById('edit-slot-icon');
  const isBreakIn = document.getElementById('edit-slot-isbreak');

  if (title) title.textContent = "Agregar Bloque de Clase";
  if (slotDay) slotDay.value = activeScheduleDay;
  if (daySelect) daySelect.value = activeScheduleDay;
  if (slotIndex) slotIndex.value = "-1";
  if (timeIn) timeIn.value = "7:00 - 7:50";
  if (subjIn) subjIn.value = "";
  if (teacherIn) teacherIn.value = "";
  if (iconIn) iconIn.value = "📚";
  if (isBreakIn) isBreakIn.checked = false;

  openModal('modal-edit-class-slot');
}

function openEditClassSlotModal(day, index) {
  const list = OFFICIAL_WEEKLY_SCHEDULE[day] || [];
  const slot = list[index];
  if (!slot) return;

  const daySelect = document.getElementById('edit-slot-select-day');
  const slotDay = document.getElementById('edit-slot-day');
  const slotIndex = document.getElementById('edit-slot-index');
  const title = document.getElementById('edit-slot-modal-title');
  const timeIn = document.getElementById('edit-slot-time');
  const subjIn = document.getElementById('edit-slot-subject');
  const teacherIn = document.getElementById('edit-slot-teacher');
  const iconIn = document.getElementById('edit-slot-icon');
  const isBreakIn = document.getElementById('edit-slot-isbreak');

  if (title) title.textContent = `Editar Bloque (${day})`;
  if (slotDay) slotDay.value = day;
  if (daySelect) daySelect.value = day;
  if (slotIndex) slotIndex.value = index;
  if (timeIn) timeIn.value = slot.time || '';
  if (subjIn) subjIn.value = slot.subject || '';
  if (teacherIn) teacherIn.value = slot.teacher || '';
  if (iconIn) iconIn.value = slot.icon || '📚';
  if (isBreakIn) isBreakIn.checked = !!slot.isBreak;

  openModal('modal-edit-class-slot');
}

function handleSaveClassSlot(event) {
  event.preventDefault();
  const day = document.getElementById('edit-slot-select-day')?.value || activeScheduleDay;
  const index = parseInt(document.getElementById('edit-slot-index')?.value || '-1', 10);
  const time = document.getElementById('edit-slot-time')?.value.trim() || '7:00 - 7:50';
  const subject = document.getElementById('edit-slot-subject')?.value.trim() || 'Materia';
  const teacher = document.getElementById('edit-slot-teacher')?.value.trim() || 'Docente';
  const icon = document.getElementById('edit-slot-icon')?.value.trim() || '📚';
  const isBreak = document.getElementById('edit-slot-isbreak')?.checked || false;

  const newSlot = { time, subject, teacher, icon, isBreak };

  if (!OFFICIAL_WEEKLY_SCHEDULE[day]) {
    OFFICIAL_WEEKLY_SCHEDULE[day] = [];
  }

  if (index >= 0 && index < OFFICIAL_WEEKLY_SCHEDULE[day].length) {
    OFFICIAL_WEEKLY_SCHEDULE[day][index] = newSlot;
    showToast(`Bloque de "${subject}" actualizado correctamente. ✅`);
  } else {
    OFFICIAL_WEEKLY_SCHEDULE[day].push(newSlot);
    showToast(`Nuevo bloque de "${subject}" agregado al día ${day}. ✅`);
  }

  saveWeeklyScheduleToStorage();
  closeModal('modal-edit-class-slot');
  filterScheduleDay(day);
}

function deleteClassSlot(day, index) {
  const list = OFFICIAL_WEEKLY_SCHEDULE[day] || [];
  if (index >= 0 && index < list.length) {
    const deleted = list.splice(index, 1);
    saveWeeklyScheduleToStorage();
    showToast(`Bloque "${deleted[0]?.subject}" eliminado. 🗑️`);
    filterScheduleDay(day);
  }
}

function renderTeacherDirectory(filteredList = null) {
  const container = document.getElementById('teacher-directory-list');
  if (!container) return;

  const isTeacher = currentUser && currentUser.role === 'TEACHER';
  const addTeacherBtn = document.getElementById('btn-add-teacher');
  if (addTeacherBtn) addTeacherBtn.style.display = isTeacher ? 'inline-flex' : 'none';

  const list = filteredList || TEACHER_DIRECTORY;
  if (list.length === 0) {
    container.innerHTML = `
      <div class="clay-card" style="text-align:center; padding:24px;">
        <span style="font-size:32px;">🔍</span>
        <p style="font-size:13px; color:var(--text-muted); margin-top:8px;">No se encontraron docentes con ese criterio de búsqueda.</p>
        ${isTeacher ? `<button class="btn-primary btn-sm" onclick="openAddTeacherModal()" style="margin-top:10px;">+ Agregar Docente</button>` : ''}
      </div>
    `;
    return;
  }

  container.innerHTML = list.map((t, idx) => `
    <div class="clay-card teacher-directory-card" style="padding:14px; border-left:4px solid #7c3aed; cursor:pointer;" onclick="openTeacherDetailModal(${idx})">
      <div style="display:flex; justify-content:space-between; align-items:center; gap:10px;">
        <div style="display:flex; align-items:center; gap:12px; flex:1; min-width:0;">
          <div style="font-size:28px; background:rgba(124,58,237,0.1); border-radius:12px; padding:6px 10px; line-height:1; flex-shrink:0;">
            ${t.avatarEmoji || '👨‍🏫'}
          </div>
          <div style="min-width:0; flex:1;">
            <div style="font-size:15.5px; font-weight:800; color:var(--text-main); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
              ${escapeHtml(t.name)}
            </div>
            <div style="font-size:11.5px; font-weight:700; color:var(--text-muted); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; margin-top:2px;">
              Materia: ${escapeHtml(t.subject)}
            </div>
          </div>
        </div>

        <div style="display:flex; align-items:center; gap:4px; flex-shrink:0;" onclick="event.stopPropagation()">
          ${t.email ? `
            <button class="btn-secondary btn-sm" style="color:#EA4335; padding:4px 8px; font-size:13px; border-radius:8px; border-color:#fca5a5;" onclick="openTeacherGmail('${t.email}', '${escapeHtml(t.name)}')" title="Enviar correo a ${escapeHtml(t.name)}">
              ✉️
            </button>
          ` : ''}
          ${isTeacher ? `
            <button class="btn-secondary btn-sm" style="padding:4px 7px; font-size:12px; border-radius:8px;" onclick="openEditTeacherModal(${idx})" title="Editar docente">
              ✏️
            </button>
            <button class="btn-secondary btn-sm btn-danger-outline" style="padding:4px 7px; font-size:12px; border-radius:8px;" onclick="deleteTeacher(${idx})" title="Eliminar docente">
              🗑️
            </button>
          ` : ''}
        </div>
      </div>

      <div style="margin-top:10px; background:#ede9fe; padding:4px 10px; border-radius:8px; display:flex; align-items:center; gap:6px;">
        <span style="font-size:12px;">🎓</span>
        <span style="color:#6b21a8; font-weight:900; font-size:12px;">${escapeHtml(t.role)}</span>
      </div>

      ${t.subjectsIn7th ? `
        <div style="margin-top:6px; background:#eff6ff; padding:4px 10px; border-radius:8px; border:1px solid #bfdbfe; display:flex; align-items:center; gap:6px;">
          <span style="font-size:12px;">📖</span>
          <span style="color:#1d4ed8; font-weight:800; font-size:11.5px;">En 7°: ${escapeHtml(t.subjectsIn7th)}</span>
        </div>
      ` : ''}

      <div style="margin-top:8px; display:flex; align-items:center; gap:6px; font-size:11.5px; background:rgba(16,185,129,0.08); padding:6px 10px; border-radius:8px; border:1px solid #86efac; color:#15803d; font-weight:700;">
        <span>🗓️ Atención Presencial:</span>
        <strong style="white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${escapeHtml(t.attentionDay)} (${escapeHtml(t.attentionHours)})</strong>
      </div>

      ${t.email ? `
        <div style="margin-top:8px; font-size:11.5px; color:var(--text-muted); display:flex; align-items:center; gap:6px; flex-wrap:wrap;">
          <strong>Correo:</strong>
          <span style="color:var(--primary); font-weight:700;">
            ${escapeHtml(t.email)}
          </span>
        </div>
      ` : `
        <div style="margin-top:6px; font-size:11px; color:var(--text-muted);">
          <strong>Atención:</strong> <em>Presencial en sede institucional</em>
        </div>
      `}

      <div style="margin-top:6px; text-align:right; font-size:10px; font-weight:800; color:#7c3aed;">
        Toca para ver en detalle 🔍
      </div>
    </div>
  `).join('');
}

let currentDetailTeacherEmail = "";

function openTeacherDetailModal(index) {
  const t = TEACHER_DIRECTORY[index];
  if (!t) return;

  const avatarEl = document.getElementById('detail-teacher-avatar');
  const nameEl = document.getElementById('detail-teacher-name');
  const roleEl = document.getElementById('detail-teacher-role');
  const subjects7thBox = document.getElementById('detail-teacher-subjects-7th-box');
  const subjects7thEl = document.getElementById('detail-teacher-subjects-7th');
  const subjectEl = document.getElementById('detail-teacher-subject');
  const attentionEl = document.getElementById('detail-teacher-attention');
  const emailBox = document.getElementById('detail-teacher-email-box');
  const emailEl = document.getElementById('detail-teacher-email');
  const gmailBtn = document.getElementById('detail-teacher-gmail-btn');

  if (avatarEl) avatarEl.textContent = t.avatarEmoji || '👨‍🏫';
  if (nameEl) nameEl.textContent = t.name;
  if (roleEl) roleEl.textContent = `🎓 ${t.role}`;
  if (subjects7thBox) {
    if (t.subjectsIn7th) {
      subjects7thBox.style.display = 'block';
      if (subjects7thEl) subjects7thEl.textContent = t.subjectsIn7th;
    } else {
      subjects7thBox.style.display = 'none';
    }
  }
  if (subjectEl) subjectEl.textContent = t.subject;
  if (attentionEl) attentionEl.textContent = `${t.attentionDay} (${t.attentionHours})`;

  currentDetailTeacherEmail = t.email || "";

  if (t.email) {
    if (emailBox) emailBox.style.display = 'block';
    if (emailEl) emailEl.textContent = t.email;
    if (gmailBtn) {
      gmailBtn.onclick = () => openTeacherGmail(t.email, t.name);
    }
  } else {
    if (emailBox) emailBox.style.display = 'none';
  }

  openModal('modal-teacher-detail');
}

function copyDetailTeacherEmail() {
  if (!currentDetailTeacherEmail) return;
  navigator.clipboard.writeText(currentDetailTeacherEmail).then(() => {
    showToast("Correo copiado al portapapeles 📋");
  }).catch(() => {
    showToast("Correo: " + currentDetailTeacherEmail);
  });
}

function downloadScheduleWeb() {
  const days = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'];
  const docTitle = "Horario escolar grado Séptimo 2026-2027";

  let scheduleHtml = `
    <!DOCTYPE html>
    <html lang="es">
    <head>
      <meta charset="UTF-8">
      <title>${docTitle}</title>
      <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; padding: 24px; color: #1e293b; max-width: 800px; margin: 0 auto; background: #f8fafc; }
        .header { background: #1e3a8a; color: white; padding: 18px 24px; border-radius: 10px; margin-bottom: 20px; }
        .header h1 { margin: 0; font-size: 20px; letter-spacing: 0.5px; }
        .header h2 { margin: 4px 0 0 0; font-size: 14px; color: #fef08a; font-weight: bold; }
        .header p { margin: 4px 0 0 0; font-size: 11px; color: #e0e7ff; }
        .day-block { margin-bottom: 16px; background: white; border-radius: 8px; border: 1px solid #e2e8f0; overflow: hidden; }
        .day-header { background: #eff6ff; color: #1e40af; font-weight: 800; font-size: 13px; padding: 8px 14px; border-bottom: 1px solid #e2e8f0; }
        table { width: 100%; border-collapse: collapse; font-size: 12.5px; }
        th { text-align: left; padding: 8px 12px; background: #f1f5f9; color: #475569; font-size: 11px; text-transform: uppercase; }
        td { padding: 8px 12px; border-bottom: 1px solid #f1f5f9; }
        .break-row { background: #fef3c7; color: #92400e; font-weight: bold; }
        @media print {
          body { padding: 0; max-width: 100%; background: white; }
          .header { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
          .day-header { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
          .break-row { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
        }
      </style>
    </head>
    <body>
      <div class="header">
        <h1>COLEGIO HOGAR MADRE DE DIOS</h1>
        <h2>HORARIO ESCOLAR GRADO SÉPTIMO 2026-2027</h2>
        <p>Jornada: 7:00 AM a 1:20 PM • Plataforma Escolaris</p>
      </div>
  `;

  days.forEach(day => {
    const slots = OFFICIAL_WEEKLY_SCHEDULE[day] || [];
    scheduleHtml += `
      <div class="day-block">
        <div class="day-header">🗓️ ${day.toUpperCase()}</div>
        <table>
          <thead>
            <tr>
              <th style="width: 120px;">Hora</th>
              <th>Materia / Actividad</th>
              <th>Docente Encargado</th>
            </tr>
          </thead>
          <tbody>
    `;

    slots.forEach(s => {
      if (s.isBreak) {
        scheduleHtml += `
          <tr class="break-row">
            <td>${s.time}</td>
            <td colspan="2">☕ Descanso</td>
          </tr>
        `;
      } else {
        const shortTeacher = formatTeacherShortName(s.teacher || '');
        scheduleHtml += `
          <tr>
            <td style="font-weight: 700; color: #2563eb;">${s.time}</td>
            <td style="font-weight: 700;">${s.subject}</td>
            <td>Prof. ${shortTeacher}</td>
          </tr>
        `;
      }
    });

    scheduleHtml += `
          </tbody>
        </table>
      </div>
    `;
  });

  scheduleHtml += `
      <div style="margin-top: 16px; font-size: 10px; color: #94a3b8; text-align: center;">
        Documento oficial emitido por la Plataforma Escolaris • Colegio Hogar Madre de Dios
      </div>
      <script>
        document.title = "${docTitle}";
        window.onload = function() {
          window.print();
        }
      </script>
    </body>
    </html>
  `;

  const printWindow = window.open('', '_blank');
  if (printWindow) {
    printWindow.document.write(scheduleHtml);
    printWindow.document.close();
  } else {
    // Fallback con descarga de archivo Blob directo con el nombre solicitado
    const blob = new Blob([scheduleHtml], { type: 'text/html;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${docTitle}.html`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    showToast(`Horario descargado como "${docTitle}.html" 📥`);
  }
}

function openAddTeacherModal() {
  const idxIn = document.getElementById('edit-teacher-index');
  const title = document.getElementById('edit-teacher-modal-title');
  const nameIn = document.getElementById('edit-teacher-name');
  const roleIn = document.getElementById('edit-teacher-role');
  const emojiIn = document.getElementById('edit-teacher-emoji');
  const subj7thIn = document.getElementById('edit-teacher-subjects-7th');
  const subjIn = document.getElementById('edit-teacher-subject');
  const emailIn = document.getElementById('edit-teacher-email');
  const dayIn = document.getElementById('edit-teacher-day');
  const hoursIn = document.getElementById('edit-teacher-hours');

  if (title) title.textContent = "Agregar Nuevo Docente al Directorio";
  if (idxIn) idxIn.value = "-1";
  if (nameIn) nameIn.value = "";
  if (roleIn) roleIn.value = "Docente Titular";
  if (emojiIn) emojiIn.value = "👨‍🏫";
  if (subj7thIn) subj7thIn.value = "";
  if (subjIn) subjIn.value = "";
  if (emailIn) emailIn.value = "";
  if (dayIn) dayIn.value = "Lunes a Viernes";
  if (hoursIn) hoursIn.value = "12:30 PM - 1:20 PM";

  openModal('modal-edit-teacher');
}

function openEditTeacherModal(index) {
  const teacher = TEACHER_DIRECTORY[index];
  if (!teacher) return;

  const idxIn = document.getElementById('edit-teacher-index');
  const title = document.getElementById('edit-teacher-modal-title');
  const nameIn = document.getElementById('edit-teacher-name');
  const roleIn = document.getElementById('edit-teacher-role');
  const emojiIn = document.getElementById('edit-teacher-emoji');
  const subj7thIn = document.getElementById('edit-teacher-subjects-7th');
  const subjIn = document.getElementById('edit-teacher-subject');
  const emailIn = document.getElementById('edit-teacher-email');
  const dayIn = document.getElementById('edit-teacher-day');
  const hoursIn = document.getElementById('edit-teacher-hours');

  if (title) title.textContent = `Editar Docente: ${teacher.name}`;
  if (idxIn) idxIn.value = index;
  if (nameIn) nameIn.value = teacher.name || '';
  if (roleIn) roleIn.value = teacher.role || '';
  if (emojiIn) emojiIn.value = teacher.avatarEmoji || '👨‍🏫';
  if (subj7thIn) subj7thIn.value = teacher.subjectsIn7th || '';
  if (subjIn) subjIn.value = teacher.subject || '';
  if (emailIn) emailIn.value = teacher.email || '';
  if (dayIn) dayIn.value = teacher.attentionDay || '';
  if (hoursIn) hoursIn.value = teacher.attentionHours || '';

  openModal('modal-edit-teacher');
}

function handleSaveTeacher(event) {
  event.preventDefault();
  const index = parseInt(document.getElementById('edit-teacher-index')?.value || '-1', 10);
  const name = document.getElementById('edit-teacher-name')?.value.trim() || 'Docente';
  const role = document.getElementById('edit-teacher-role')?.value.trim() || 'Docente Titular';
  const avatarEmoji = document.getElementById('edit-teacher-emoji')?.value.trim() || '👨‍🏫';
  const subjectsIn7th = document.getElementById('edit-teacher-subjects-7th')?.value.trim() || '';
  const subject = document.getElementById('edit-teacher-subject')?.value.trim() || 'Materia General';
  const rawEmail = document.getElementById('edit-teacher-email')?.value.trim();
  const email = rawEmail ? rawEmail : null;
  const attentionDay = document.getElementById('edit-teacher-day')?.value.trim() || 'Lunes a Viernes';
  const attentionHours = document.getElementById('edit-teacher-hours')?.value.trim() || '12:30 PM - 1:20 PM';

  const teacherData = { name, role, subject, subjectsIn7th, email, attentionDay, attentionHours, avatarEmoji };

  if (index >= 0 && index < TEACHER_DIRECTORY.length) {
    TEACHER_DIRECTORY[index] = teacherData;
    showToast(`Docente "${name}" actualizado con éxito. 💾`);
  } else {
    TEACHER_DIRECTORY.push(teacherData);
    showToast(`Docente "${name}" agregado al directorio. ✅`);
  }

  saveTeacherDirectoryToStorage();
  closeModal('modal-edit-teacher');
  renderTeacherDirectory();
}

function deleteTeacher(index) {
  const teacher = TEACHER_DIRECTORY[index];
  if (!teacher) return;

  if (confirm(`¿Estás seguro de eliminar a "${teacher.name}" del directorio docente?`)) {
    TEACHER_DIRECTORY.splice(index, 1);
    saveTeacherDirectoryToStorage();
    showToast("Docente eliminado del directorio. 🗑️");
    renderTeacherDirectory();
  }
}

function filterTeacherDirectory() {
  const query = (document.getElementById('input-search-teacher')?.value || '').toLowerCase().trim();
  if (!query) {
    renderTeacherDirectory(TEACHER_DIRECTORY);
    return;
  }

  const filtered = TEACHER_DIRECTORY.filter(t => 
    t.name.toLowerCase().includes(query) ||
    t.subject.toLowerCase().includes(query) ||
    t.role.toLowerCase().includes(query) ||
    (t.email && t.email.toLowerCase().includes(query)) ||
    t.attentionDay.toLowerCase().includes(query)
  );

  renderTeacherDirectory(filtered);
}

function openTeacherGmail(email, teacherName) {
  const subject = encodeURIComponent("Consulta Institucional - Colegio Hogar Madre de Dios");
  const body = encodeURIComponent(`Estimado/a ${teacherName},\n\nLe escribo cordialmente para realizar una consulta respecto a...\n\nAtentamente,\n${currentUser ? currentUser.name : 'Acudiente / Estudiante'}`);
  const gmailWebUrl = `https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(email)}&su=${subject}&body=${body}`;
  
  // Try opening web Gmail in a new window/tab, with mailto fallback
  const newWin = window.open(gmailWebUrl, '_blank');
  if (!newWin || newWin.closed || typeof newWin.closed === 'undefined') {
    window.location.href = `mailto:${email}?subject=${subject}&body=${body}`;
  }
}

// 15.3 School Calendar Events Logic (Calendario General Oficial & Calendario Mensual Interactivo)
const CAL_MONTH_NAMES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
];

let calendarActiveView = 'month'; // 'month' or 'list'
let calCurrentYear = new Date().getFullYear();
let calCurrentMonth = new Date().getMonth(); // 0 - 11
let calSelectedDay = new Date().getDate();
let activeCalendarMonth = 'TODOS';
let activeCalendarSearchQuery = '';

function switchCalendarView(viewMode) {
  calendarActiveView = viewMode;
  const btnMonth = document.getElementById('btn-cal-view-month');
  const btnList = document.getElementById('btn-cal-view-list');
  const monthContainer = document.getElementById('calendar-monthly-container');
  const listContainer = document.getElementById('calendar-list-container');

  if (btnMonth) {
    if (viewMode === 'month') {
      btnMonth.classList.add('active');
      btnMonth.classList.remove('btn-secondary');
    } else {
      btnMonth.classList.remove('active');
    }
  }
  if (btnList) {
    if (viewMode === 'list') {
      btnList.classList.add('active');
      btnList.classList.remove('btn-secondary');
    } else {
      btnList.classList.remove('active');
    }
  }

  if (monthContainer) {
    if (viewMode === 'month') monthContainer.classList.remove('hidden');
    else monthContainer.classList.add('hidden');
  }
  if (listContainer) {
    if (viewMode === 'list') listContainer.classList.remove('hidden');
    else listContainer.classList.add('hidden');
  }

  if (viewMode === 'month') {
    renderMonthlyCalendar();
  } else {
    renderSchoolEvents();
  }
}

function renderCalendar() {
  if (calendarActiveView === 'list') {
    renderSchoolEvents();
  } else {
    renderMonthlyCalendar();
  }
}

function normalizeMonthString(mStr) {
  if (!mStr) return -1;
  const clean = mStr.toString().toLowerCase().trim();
  const map = {
    'enero': 0, 'ene': 0, 'january': 0, 'jan': 0, '01': 0, '1': 0,
    'febrero': 1, 'feb': 1, 'february': 1, '02': 1, '2': 1,
    'marzo': 2, 'mar': 2, 'march': 2, '03': 2, '3': 2,
    'abril': 3, 'abr': 3, 'apr': 3, 'april': 3, '04': 3, '4': 3,
    'mayo': 4, 'may': 4, '05': 4, '5': 4,
    'junio': 5, 'jun': 5, 'june': 5, '06': 5, '6': 5,
    'julio': 6, 'jul': 6, 'july': 6, '07': 6, '7': 6,
    'agosto': 7, 'ago': 7, 'aug': 7, 'august': 7, '08': 7, '8': 7,
    'septiembre': 8, 'sep': 8, 'sept': 8, 'september': 8, '09': 8, '9': 8,
    'octubre': 9, 'oct': 9, 'october': 9, '10': 9,
    'noviembre': 10, 'nov': 10, 'november': 10, '11': 10,
    'diciembre': 11, 'dic': 11, 'dec': 11, 'december': 11, '12': 11
  };
  return map[clean] !== undefined ? map[clean] : -1;
}

function isSchoolEventOnDay(ev, year, monthIndex, day) {
  if (!ev) return false;
  const evMonth = normalizeMonthString(ev.month);
  if (evMonth !== monthIndex) return false;

  const rawDayStr = (ev.dayNumber || '').toString().trim();
  if (!rawDayStr) {
    const dStr = (ev.dateStr || '').toString().toLowerCase();
    const match = dStr.match(/\b(\d{1,2})\b/);
    if (match) return parseInt(match[1], 10) === day;
    return false;
  }

  if (rawDayStr.includes('-')) {
    const parts = rawDayStr.split('-').map(p => parseInt(p.replace(/[^0-9]/g, ''), 10));
    if (parts.length === 2 && !isNaN(parts[0]) && !isNaN(parts[1])) {
      if (parts[0] <= parts[1]) {
        return day >= parts[0] && day <= parts[1];
      } else {
        return day >= parts[0] || day <= parts[1];
      }
    }
  }

  const singleDay = parseInt(rawDayStr.replace(/[^0-9]/g, ''), 10);
  return singleDay === day;
}

function isTaskOnDay(t, year, monthIndex, day) {
  if (!t) return false;
  if (t.dueDate) {
    const parts = t.dueDate.toString().split('-');
    if (parts.length === 3) {
      const y = parseInt(parts[0], 10);
      const m = parseInt(parts[1], 10) - 1;
      const d = parseInt(parts[2], 10);
      if (y === year && m === monthIndex && d === day) return true;
    }
  }
  if (t.dueDateMillis) {
    const dObj = new Date(t.dueDateMillis);
    if (dObj.getFullYear() === year && dObj.getMonth() === monthIndex && dObj.getDate() === day) return true;
  }
  return false;
}

function getActivitiesForDay(year, monthIndex, day) {
  const events = (OFFICIAL_CALENDAR_EVENTS || []).filter(e => isSchoolEventOnDay(e, year, monthIndex, day));
  const tasksForDay = (userTasks || []).filter(t => isTaskOnDay(t, year, monthIndex, day));
  const exams = tasksForDay.filter(t => t.isExam === true || t.type === 'EXAM');
  const tasks = tasksForDay.filter(t => !t.isExam && t.type !== 'EXAM');
  return { events, exams, tasks };
}

function renderMonthlyCalendar() {
  const gridContainer = document.getElementById('calendar-days-grid');
  const monthLabel = document.getElementById('cal-current-month-label');
  const quickMonthSelect = document.getElementById('cal-quick-month-select');
  const quickYearSelect = document.getElementById('cal-quick-year-select');

  if (!gridContainer) return;

  if (monthLabel) {
    monthLabel.textContent = `${CAL_MONTH_NAMES[calCurrentMonth]} ${calCurrentYear}`;
  }
  if (quickMonthSelect) quickMonthSelect.value = calCurrentMonth;
  if (quickYearSelect) quickYearSelect.value = calCurrentYear;

  const firstDay = new Date(calCurrentYear, calCurrentMonth, 1);
  const startDayOfWeek = (firstDay.getDay() + 6) % 7; // Monday = 0, ..., Sunday = 6
  const daysInMonth = new Date(calCurrentYear, calCurrentMonth + 1, 0).getDate();
  const daysInPrevMonth = new Date(calCurrentYear, calCurrentMonth, 0).getDate();

  const totalCells = (startDayOfWeek + daysInMonth) > 35 ? 42 : 35;
  const today = new Date();
  const isCurrentYearAndMonth = (today.getFullYear() === calCurrentYear && today.getMonth() === calCurrentMonth);
  const todayDateNum = today.getDate();

  if (calSelectedDay > daysInMonth) {
    calSelectedDay = daysInMonth;
  }

  let html = '';

  for (let i = 0; i < totalCells; i++) {
    if (i < startDayOfWeek) {
      // Prev month day
      const dNum = daysInPrevMonth - startDayOfWeek + 1 + i;
      html += `
        <div class="calendar-day-cell day-other-month" onclick="calendarPrevMonthSelectDay(${dNum})">
          <div class="calendar-day-header-inner">
            <span class="calendar-day-num">${dNum}</span>
          </div>
        </div>
      `;
    } else if (i < startDayOfWeek + daysInMonth) {
      // Current month day
      const dNum = i - startDayOfWeek + 1;
      const isToday = isCurrentYearAndMonth && (dNum === todayDateNum);
      const isSelected = (dNum === calSelectedDay);
      const activities = getActivitiesForDay(calCurrentYear, calCurrentMonth, dNum);
      const hasActivities = activities.events.length > 0 || activities.exams.length > 0 || activities.tasks.length > 0;

      let dotsHtml = '';
      if (activities.events.length > 0) {
        dotsHtml += `<span class="cal-dot cal-dot-event" title="${activities.events.length} evento(s)"></span>`;
      }
      if (activities.exams.length > 0) {
        dotsHtml += `<span class="cal-dot cal-dot-exam" title="${activities.exams.length} examen(es)"></span>`;
      }
      if (activities.tasks.length > 0) {
        dotsHtml += `<span class="cal-dot cal-dot-task" title="${activities.tasks.length} tarea(s)"></span>`;
      }

      let miniBadgesHtml = '';
      if (activities.events.length > 0) {
        const topEvent = activities.events[0];
        miniBadgesHtml += `<span class="cal-badge-pill event">${escapeHtml((topEvent.emoji ? topEvent.emoji + ' ' : '') + topEvent.title)}</span>`;
      } else if (activities.exams.length > 0) {
        miniBadgesHtml += `<span class="cal-badge-pill exam">📝 Examen</span>`;
      } else if (activities.tasks.length > 0) {
        miniBadgesHtml += `<span class="cal-badge-pill task">📚 Tarea</span>`;
      }

      html += `
        <div class="calendar-day-cell ${isToday ? 'day-today' : ''} ${isSelected ? 'day-selected' : ''}" 
             id="cal-cell-${dNum}" 
             onclick="selectCalendarDay(${dNum})">
          <div class="calendar-day-header-inner">
            <span class="calendar-day-num">${dNum}</span>
            ${hasActivities ? `<div class="calendar-day-dots">${dotsHtml}</div>` : ''}
          </div>
          <div>${miniBadgesHtml}</div>
        </div>
      `;
    } else {
      // Next month day
      const dNum = i - (startDayOfWeek + daysInMonth) + 1;
      html += `
        <div class="calendar-day-cell day-other-month" onclick="calendarNextMonthSelectDay(${dNum})">
          <div class="calendar-day-header-inner">
            <span class="calendar-day-num">${dNum}</span>
          </div>
        </div>
      `;
    }
  }

  gridContainer.innerHTML = html;
  renderCalendarDayAgenda(calSelectedDay, calCurrentMonth, calCurrentYear);
}

function selectCalendarDay(day) {
  calSelectedDay = day;
  const allCells = document.querySelectorAll('.calendar-day-cell');
  allCells.forEach(c => c.classList.remove('day-selected'));
  const target = document.getElementById(`cal-cell-${day}`);
  if (target) target.classList.add('day-selected');
  renderCalendarDayAgenda(day, calCurrentMonth, calCurrentYear);
}

function calendarPrevMonth() {
  calCurrentMonth--;
  if (calCurrentMonth < 0) {
    calCurrentMonth = 11;
    calCurrentYear--;
  }
  renderMonthlyCalendar();
}

function calendarNextMonth() {
  calCurrentMonth++;
  if (calCurrentMonth > 11) {
    calCurrentMonth = 0;
    calCurrentYear++;
  }
  renderMonthlyCalendar();
}

function calendarPrevMonthSelectDay(day) {
  calCurrentMonth--;
  if (calCurrentMonth < 0) {
    calCurrentMonth = 11;
    calCurrentYear--;
  }
  calSelectedDay = day;
  renderMonthlyCalendar();
}

function calendarNextMonthSelectDay(day) {
  calCurrentMonth++;
  if (calCurrentMonth > 11) {
    calCurrentMonth = 0;
    calCurrentYear++;
  }
  calSelectedDay = day;
  renderMonthlyCalendar();
}

function calendarGoToToday() {
  const now = new Date();
  calCurrentYear = now.getFullYear();
  calCurrentMonth = now.getMonth();
  calSelectedDay = now.getDate();
  renderMonthlyCalendar();
}

function handleCalendarMonthSelectChange(val) {
  calCurrentMonth = parseInt(val, 10);
  renderMonthlyCalendar();
}

function handleCalendarYearSelectChange(val) {
  calCurrentYear = parseInt(val, 10);
  renderMonthlyCalendar();
}

function renderCalendarDayAgenda(day, monthIndex, year) {
  const container = document.getElementById('calendar-day-agenda');
  if (!container) return;

  const dateObj = new Date(year, monthIndex, day);
  const weekdayName = dateObj.toLocaleDateString('es-ES', { weekday: 'long' });
  const capitalizedWeekday = weekdayName.charAt(0).toUpperCase() + weekdayName.slice(1);
  const monthName = CAL_MONTH_NAMES[monthIndex];
  const dateTitle = `${capitalizedWeekday}, ${day} de ${monthName} de ${year}`;
  const isoDate = `${year}-${String(monthIndex + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  const prefillDateStr = `${monthName} ${day}`;

  const activities = getActivitiesForDay(year, monthIndex, day);
  const totalCount = activities.events.length + activities.exams.length + activities.tasks.length;

  let headerHtml = `
    <div class="agenda-header-row">
      <div class="agenda-date-title">
        <span>📋</span>
        <span>${escapeHtml(dateTitle)}</span>
        <span class="badge" style="background:rgba(37,99,235,0.1); color:var(--primary); font-size:11px; font-weight:800; padding:2px 8px;">
          ${totalCount} ${totalCount === 1 ? 'actividad' : 'actividades'}
        </span>
      </div>
      <div style="display:flex; gap:6px; align-items:center;">
        <button class="btn-primary btn-sm" onclick="openAddSchoolEventOnDate('${prefillDateStr}', '${monthName}')" title="Agregar evento para este día">
          ➕ Evento
        </button>
        <button class="btn-secondary btn-sm" onclick="openAddTaskOnDate('${isoDate}')" title="Programar tarea o evaluación para este día">
          ➕ Tarea
        </button>
      </div>
    </div>
  `;

  if (totalCount === 0) {
    container.innerHTML = headerHtml + `
      <div class="agenda-empty-state">
        <span style="font-size:28px;">📅</span>
        <p style="font-size:13px; font-weight:800; color:var(--text-main); margin-top:8px;">Sin actividades programadas para este día</p>
        <p style="font-size:11.5px; color:var(--text-muted); margin-top:2px;">No hay eventos institucionales, exámenes ni tareas registradas para el ${day} de ${monthName}.</p>
        <div style="margin-top:10px; display:flex; justify-content:center; gap:8px;">
          <button class="btn-primary btn-sm" onclick="openAddSchoolEventOnDate('${prefillDateStr}', '${monthName}')">➕ Agregar Evento</button>
          <button class="btn-secondary btn-sm" onclick="openAddTaskOnDate('${isoDate}')">➕ Programar Tarea</button>
        </div>
      </div>
    `;
    return;
  }

  let itemsHtml = '<div class="agenda-cards-list">';

  // 1. School Events
  activities.events.forEach(ev => {
    const originalIndex = OFFICIAL_CALENDAR_EVENTS.indexOf(ev);
    const categoryBadgeColor = ev.colorHex || (ev.category === 'ACADEMIC' ? '#dc2626' : 
                               (ev.category === 'COMMUNITY' ? '#7c3aed' : 
                               (ev.category === 'CIVIC' ? '#2563eb' : 
                               (ev.category === 'CAFETERIA' ? '#ea580c' : 
                               (ev.category === 'CULTURAL' ? '#db2777' : '#059669')))));
    itemsHtml += `
      <div class="agenda-item-card is-event" style="border-left-color: ${categoryBadgeColor};">
        <div style="flex:1;">
          <div style="display:flex; align-items:center; gap:6px; flex-wrap:wrap;">
            <span class="badge" style="background:${categoryBadgeColor}15; color:${categoryBadgeColor}; font-weight:800; font-size:10px;">
              ${ev.categoryLabel || 'Institucional 🏫'}
            </span>
            ${ev.time ? `<span style="font-size:11px; font-weight:700; color:var(--text-muted);">⏰ ${escapeHtml(ev.time)}</span>` : ''}
            ${ev.location ? `<span style="font-size:11px; color:var(--text-muted);">📍 ${escapeHtml(ev.location)}</span>` : ''}
          </div>
          <div style="font-size:14px; font-weight:800; color:var(--text-main); margin-top:4px;">
            ${ev.emoji ? `${ev.emoji} ` : '📅 '}${escapeHtml(ev.title)}
          </div>
          ${ev.description ? `<p style="font-size:11.5px; color:var(--text-muted); margin:4px 0 0 0;">${escapeHtml(ev.description)}</p>` : ''}
        </div>
        ${currentUser && currentUser.role === 'TEACHER' ? `
        <div style="display:flex; gap:4px; align-self:center;">
          <button class="btn-secondary btn-sm" style="padding:3px 7px; font-size:11px;" onclick="openEditSchoolEventModal(${originalIndex})" title="Editar evento">
            ✏️
          </button>
          <button class="btn-secondary btn-sm btn-danger-outline" style="padding:3px 7px; font-size:11px;" onclick="deleteSchoolEvent(${originalIndex})" title="Eliminar">
            🗑️
          </button>
        </div>
        ` : ''}
      </div>
    `;
  });

  // 2. Exams
  activities.exams.forEach(ex => {
    itemsHtml += `
      <div class="agenda-item-card is-exam">
        <div style="flex:1;">
          <div style="display:flex; align-items:center; gap:6px; flex-wrap:wrap;">
            <span class="badge" style="background:rgba(234, 88, 12, 0.12); color:#ea580c; font-weight:800; font-size:10px;">
              📝 Examen / Evaluación
            </span>
            <span style="font-size:11px; font-weight:800; color:var(--text-main);">${escapeHtml(ex.subject || 'General')}</span>
          </div>
          <div style="font-size:14px; font-weight:800; color:var(--text-main); margin-top:4px;">
            ${escapeHtml(ex.title)}
          </div>
          ${ex.description ? `<p style="font-size:11.5px; color:var(--text-muted); margin:4px 0 0 0;">${escapeHtml(ex.description)}</p>` : ''}
        </div>
        <div style="align-self:center;">
          <button class="btn-secondary btn-sm" style="padding:3px 7px; font-size:11px;" onclick="switchNav('tasks')">
            Ver en Tareas
          </button>
        </div>
      </div>
    `;
  });

  // 3. Tasks
  activities.tasks.forEach(t => {
    itemsHtml += `
      <div class="agenda-item-card is-task ${t.completed ? 'completed' : ''}">
        <div style="display:flex; align-items:flex-start; gap:8px; flex:1;">
          ${currentUser && currentUser.role !== 'PARENT' ? `
            <div class="task-checkbox ${t.completed ? 'checked' : ''}" style="margin-top:2px;" onclick="toggleTaskStatus('${t.id}', ${!t.completed})">
              ${t.completed ? '✓' : ''}
            </div>
          ` : `<div style="font-size:14px; margin-top:2px;">${t.completed ? '✅' : '⏳'}</div>`}
          <div>
            <div style="display:flex; align-items:center; gap:6px; flex-wrap:wrap;">
              <span class="badge" style="background:rgba(16, 185, 129, 0.12); color:#10b981; font-weight:800; font-size:10px;">
                📚 Tarea
              </span>
              <span style="font-size:11px; font-weight:800; color:var(--text-main);">${escapeHtml(t.subject || 'Materia')}</span>
              <span class="badge badge-priority-${(t.priority || 'media').toLowerCase()}" style="font-size:9.5px;">${t.priority || 'Media'}</span>
            </div>
            <div style="font-size:14px; font-weight:800; color:var(--text-main); margin-top:4px; ${t.completed ? 'text-decoration:line-through; opacity:0.7;' : ''}">
              ${escapeHtml(t.title)}
            </div>
            ${t.description ? `<p style="font-size:11.5px; color:var(--text-muted); margin:4px 0 0 0;">${escapeHtml(t.description)}</p>` : ''}
          </div>
        </div>
        <div style="align-self:center;">
          <button class="btn-secondary btn-sm" style="padding:3px 7px; font-size:11px;" onclick="switchNav('tasks')">
            Ir a Tareas
          </button>
        </div>
      </div>
    `;
  });

  itemsHtml += '</div>';
  container.innerHTML = headerHtml + itemsHtml;
}

function openAddSchoolEventOnDate(dateStr, monthName) {
  openAddSchoolEventModal(dateStr, monthName);
}

function openAddTaskOnDate(isoDate) {
  openModal('modal-task');
  const dateInput = document.getElementById('task-due-date');
  if (dateInput) dateInput.value = isoDate;
}

// 15.3.1 School Calendar Events Logic (Cronograma Anual en Lista)
function renderSchoolEvents(customList = null) {
  const container = document.getElementById('school-events-list');
  if (!container) return;

  let list = customList || OFFICIAL_CALENDAR_EVENTS;

  if (activeCalendarMonth && activeCalendarMonth !== 'TODOS') {
    list = list.filter(e => e.month === activeCalendarMonth);
  }

  if (activeCalendarSearchQuery) {
    const q = activeCalendarSearchQuery.toLowerCase();
    list = list.filter(e => 
      (e.title && e.title.toLowerCase().includes(q)) ||
      (e.dateStr && e.dateStr.toLowerCase().includes(q)) ||
      (e.description && e.description.toLowerCase().includes(q)) ||
      (e.location && e.location.toLowerCase().includes(q)) ||
      (e.categoryLabel && e.categoryLabel.toLowerCase().includes(q))
    );
  }

  if (list.length === 0) {
    container.innerHTML = `
      <div class="clay-card" style="text-align:center; padding:24px;">
        <span style="font-size:32px;">🔍</span>
        <p style="font-size:13px; color:var(--text-muted); margin-top:8px;">No se encontraron eventos programados para este filtro.</p>
        <button class="btn-primary btn-sm" onclick="openAddSchoolEventModal()" style="margin-top:10px;">+ Agregar Evento</button>
      </div>
    `;
    return;
  }

  container.innerHTML = list.map(ev => {
    const originalIndex = OFFICIAL_CALENDAR_EVENTS.indexOf(ev);
    const categoryBadgeColor = ev.category === 'ACADEMIC' ? '#dc2626' : 
                               (ev.category === 'COMMUNITY' ? '#7c3aed' : 
                               (ev.category === 'CIVIC' ? '#2563eb' : 
                               (ev.category === 'CAFETERIA' ? '#ea580c' : 
                               (ev.category === 'CULTURAL' ? '#db2777' : '#059669'))));
    
    const categoryLabel = (ev.categoryLabel && !['COMMUNITY','CIVIC','ACADEMIC','CULTURAL','CAFETERIA','INSTITUTIONAL'].includes(ev.categoryLabel))
      ? ev.categoryLabel
      : (ev.category === 'ACADEMIC' ? 'Académico 📝' : 
        (ev.category === 'COMMUNITY' ? 'Padres de Familia 👨‍👩‍👧' : 
        (ev.category === 'CIVIC' ? 'Cívico 🗳️' : 
        (ev.category === 'CAFETERIA' ? 'Cafetería 🍔' : 
        (ev.category === 'CULTURAL' ? 'Cultural 🎨' : 'Institucional 🏫')))));

    const cleanDesc = (ev.description || '').trim();
    const isRedundantTime = cleanDesc.toLowerCase().startsWith('hora') || cleanDesc.toLowerCase() === (ev.time || '').toLowerCase();
    
    return `
      <div class="clay-card school-event-card" style="padding:14px; border-left:4px solid ${ev.colorHex || categoryBadgeColor};">
        <div style="display:flex; justify-content:space-between; align-items:flex-start; flex-wrap:wrap; gap:10px;">
          <div style="display:flex; align-items:flex-start; gap:12px; flex:1; min-width:240px;">
            <div style="display:flex; flex-direction:column; align-items:center; justify-content:center; background:rgba(37,99,235,0.08); border:1px solid rgba(37,99,235,0.2); border-radius:12px; padding:6px 10px; min-width:54px; text-align:center;">
              <span style="font-size:10px; font-weight:900; color:var(--primary); text-transform:uppercase;">${ev.monthShort || (ev.month ? ev.month.substring(0,3).toUpperCase() : 'CAL')}</span>
              <span style="font-size:17px; font-weight:900; color:var(--text-main); line-height:1.1;">${ev.dayNumber || '📅'}</span>
            </div>

            <div style="flex:1;">
              <div style="display:flex; align-items:center; gap:6px; flex-wrap:wrap;">
                <span class="badge" style="background:${categoryBadgeColor}15; color:${categoryBadgeColor}; font-weight:800; font-size:10.5px; white-space:nowrap;">${categoryLabel}</span>
                <span style="font-size:11px; font-weight:800; color:var(--text-muted);">🗓️ ${escapeHtml(ev.dateStr)}</span>
              </div>

              <div style="font-size:15px; font-weight:800; color:var(--text-main); margin-top:4px;">
                ${ev.emoji ? `${ev.emoji} ` : ''}${escapeHtml(ev.title)}
              </div>

              ${cleanDesc && !isRedundantTime ? `
                <p style="font-size:12px; color:var(--text-muted); margin:4px 0 0 0; line-height:1.4;">
                  ${escapeHtml(cleanDesc)}
                </p>
              ` : ''}

              ${(ev.time || ev.location) ? `
                <div style="margin-top:8px; display:flex; align-items:center; gap:10px; font-size:11.5px; color:var(--text-muted); flex-wrap:wrap;">
                  ${ev.time ? `<span style="font-weight:800; color:var(--text-main); background:rgba(0,0,0,0.04); padding:2px 6px; border-radius:4px;">⏰ ${escapeHtml(ev.time)}</span>` : ''}
                  ${ev.location ? `<span>📍 ${escapeHtml(ev.location)}</span>` : ''}
                </div>
              ` : ''}
            </div>
          </div>

          ${currentUser && currentUser.role === 'TEACHER' ? `
          <div style="display:flex; gap:6px; align-self:center;">
            <button class="btn-secondary btn-sm" style="padding:4px 8px; font-size:11.5px;" onclick="openEditSchoolEventModal(${originalIndex})" title="Editar datos del evento">
              ✏️ Editar
            </button>
            <button class="btn-secondary btn-sm btn-danger-outline" style="padding:4px 8px; font-size:11.5px;" onclick="deleteSchoolEvent(${originalIndex})" title="Eliminar evento">
              🗑️
            </button>
          </div>
          ` : ''}
        </div>
      </div>
    `;
  }).join('');
}

function filterCalendarMonth(month) {
  activeCalendarMonth = month;
  const months = ['TODOS', 'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
  months.forEach(m => {
    const btn = document.getElementById(`btn-cal-month-${m}`);
    if (btn) {
      btn.className = m === month ? 'btn-primary btn-filter active' : 'btn-secondary btn-filter';
    }
  });
  renderSchoolEvents();
}

function filterCalendarQuery() {
  activeCalendarSearchQuery = (document.getElementById('input-search-events')?.value || '').trim();
  renderSchoolEvents();
}

function openAddSchoolEventModal(prefillDateStr = null, prefillMonth = null) {
  if (!currentUser || currentUser.role !== 'TEACHER') {
    showToast("Solo los docentes y directivos pueden agregar eventos al calendario. 🔒");
    return;
  }
  const titleEl = document.getElementById('edit-event-modal-title');
  const idxIn = document.getElementById('edit-event-index');
  const titleIn = document.getElementById('edit-event-title');
  const dateStrIn = document.getElementById('edit-event-date-str');
  const monthIn = document.getElementById('edit-event-month');
  const timeIn = document.getElementById('edit-event-time');
  const catIn = document.getElementById('edit-event-category');
  const locIn = document.getElementById('edit-event-location');
  const emojiIn = document.getElementById('edit-event-emoji');
  const descIn = document.getElementById('edit-event-desc');

  if (titleEl) titleEl.textContent = "Agregar Evento al Calendario";
  if (idxIn) idxIn.value = "-1";
  if (titleIn) titleIn.value = "";
  if (dateStrIn) dateStrIn.value = prefillDateStr || (activeCalendarMonth !== 'TODOS' ? `${activeCalendarMonth} 1` : "Septiembre 1");
  if (monthIn) monthIn.value = prefillMonth || (activeCalendarMonth !== 'TODOS' ? activeCalendarMonth : "Septiembre");
  if (timeIn) timeIn.value = "08:00 AM";
  if (catIn) catIn.value = "INSTITUTIONAL";
  if (locIn) locIn.value = "";
  if (emojiIn) emojiIn.value = "📅";
  if (descIn) descIn.value = "";

  openModal('modal-edit-school-event');
}

function openEditSchoolEventModal(index) {
  if (!currentUser || currentUser.role !== 'TEACHER') {
    showToast("Solo los docentes y directivos pueden modificar eventos del calendario. 🔒");
    return;
  }
  const ev = OFFICIAL_CALENDAR_EVENTS[index];
  if (!ev) return;

  const titleEl = document.getElementById('edit-event-modal-title');
  const idxIn = document.getElementById('edit-event-index');
  const titleIn = document.getElementById('edit-event-title');
  const dateStrIn = document.getElementById('edit-event-date-str');
  const monthIn = document.getElementById('edit-event-month');
  const timeIn = document.getElementById('edit-event-time');
  const catIn = document.getElementById('edit-event-category');
  const locIn = document.getElementById('edit-event-location');
  const emojiIn = document.getElementById('edit-event-emoji');
  const descIn = document.getElementById('edit-event-desc');

  if (titleEl) titleEl.textContent = `Editar Evento: ${ev.title}`;
  if (idxIn) idxIn.value = index;
  if (titleIn) titleIn.value = ev.title || '';
  if (dateStrIn) dateStrIn.value = ev.dateStr || '';
  if (monthIn) monthIn.value = ev.month || 'Septiembre';
  if (timeIn) timeIn.value = ev.time || '';
  if (catIn) catIn.value = ev.category || 'INSTITUTIONAL';
  if (locIn) locIn.value = ev.location || '';
  if (emojiIn) emojiIn.value = ev.emoji || '📅';
  if (descIn) descIn.value = ev.description || '';

  openModal('modal-edit-school-event');
}

function handleSaveSchoolEvent(event) {
  event.preventDefault();
  const index = parseInt(document.getElementById('edit-event-index')?.value || '-1', 10);
  const title = document.getElementById('edit-event-title')?.value.trim() || 'Evento Escolar';
  const dateStr = document.getElementById('edit-event-date-str')?.value.trim() || 'Fecha';
  const month = document.getElementById('edit-event-month')?.value || 'Septiembre';
  const time = document.getElementById('edit-event-time')?.value.trim() || '08:00 AM';
  const category = document.getElementById('edit-event-category')?.value || 'INSTITUTIONAL';
  const location = document.getElementById('edit-event-location')?.value.trim() || 'Sede Institucional';
  const emoji = document.getElementById('edit-event-emoji')?.value.trim() || '📅';
  const description = document.getElementById('edit-event-desc')?.value.trim() || '';

  const dayNumber = dateStr.replace(/[^0-9\-]/g, '') || '1';
  const monthShort = month.substring(0, 3).toUpperCase();
  const categoryLabels = {
    'INSTITUTIONAL': 'Institucional 🏫',
    'COMMUNITY': 'Padres de Familia 👨‍👩‍👧',
    'ACADEMIC': 'Académico / Exámenes 📝',
    'CIVIC': 'Cívico / Izada 🇨🇴',
    'CULTURAL': 'Cultural / Jean Day 🎭',
    'CAFETERIA': 'Menú Cafetería 🍔'
  };

  const newEvent = {
    id: 'ev_' + Date.now(),
    title,
    dateStr,
    dayNumber,
    month,
    monthShort,
    time,
    category,
    categoryLabel: categoryLabels[category] || 'Institucional 🏫',
    location,
    emoji,
    description,
    colorHex: category === 'ACADEMIC' ? '#dc2626' : (category === 'COMMUNITY' ? '#7c3aed' : (category === 'CIVIC' ? '#2563eb' : (category === 'CAFETERIA' ? '#ea580c' : (category === 'CULTURAL' ? '#db2777' : '#059669'))))
  };

  if (index >= 0 && index < OFFICIAL_CALENDAR_EVENTS.length) {
    OFFICIAL_CALENDAR_EVENTS[index] = newEvent;
    showToast(`Evento "${title}" actualizado. 💾`);
  } else {
    OFFICIAL_CALENDAR_EVENTS.push(newEvent);
    showToast(`Evento "${title}" agregado al calendario. 📅`);
  }

  saveCalendarEventsToStorage();
  closeModal('modal-edit-school-event');
  renderCalendar();
  renderSchoolEvents();
}

function deleteSchoolEvent(index) {
  if (!currentUser || currentUser.role !== 'TEACHER') {
    showToast("Solo los docentes y directivos pueden eliminar eventos del calendario. 🔒");
    return;
  }
  const ev = OFFICIAL_CALENDAR_EVENTS[index];
  if (!ev) return;

  if (confirm(`¿Estás seguro de eliminar el evento "${ev.title}" del calendario escolar?`)) {
    OFFICIAL_CALENDAR_EVENTS.splice(index, 1);
    saveCalendarEventsToStorage();
    showToast("Evento eliminado del calendario. 🗑️");
    renderCalendar();
    renderSchoolEvents();
  }
}

// 15.4 Mobile Quick Hub (Cinta táctil de acceso rápido para estudiantes en móvil)
function renderMobileQuickHub() {
  const hub = document.getElementById('mobile-student-quick-hub');
  if (!hub) return;

  if (!currentUser || currentUser.role !== 'STUDENT') {
    hub.style.display = 'none';
    return;
  }
  hub.style.display = 'flex';

  const items = [
    { id: 'feed', icon: '📢', label: 'Muro' },
    { id: 'schedule', icon: '⏰', label: 'Horario' },
    { id: 'journal', icon: '📖', label: 'Bitácora' },
    { id: 'rewards', icon: '🎁', label: 'Tienda' },
    { id: 'calendar', icon: '📅', label: 'Calendario' },
    { id: 'tasks', icon: '📝', label: 'Tareas' },
    { id: 'gamification', icon: '🏆', label: 'Ranking' }
  ];

  hub.innerHTML = items.map(item => `
    <button class="quick-hub-chip ${currentTab === item.id ? 'active' : ''}" onclick="switchNav('${item.id}')">
      <span>${item.icon}</span>
      <span>${item.label}</span>
    </button>
  `).join('');
}

// 15.5 Bitácora / Diario Escolar Estudiantil
const JOURNAL_MOODS = {
  'FELIZ': { label: 'Feliz / Animada', emoji: '😊', color: '#10b981', bg: 'rgba(16, 185, 129, 0.12)' },
  'INSPIRADA': { label: 'Inspirada', emoji: '🌟', color: '#8b5cf6', bg: 'rgba(139, 92, 246, 0.12)' },
  'CREATIVA': { label: 'Creativa', emoji: '💡', color: '#f59e0b', bg: 'rgba(245, 158, 11, 0.12)' },
  'TRANQUILA': { label: 'Tranquila', emoji: '🧘', color: '#06b6d4', bg: 'rgba(6, 182, 212, 0.12)' },
  'ENFOCADA': { label: 'Muy enfocada', emoji: '📚', color: '#2563eb', bg: 'rgba(37, 99, 235, 0.12)' },
  'CANSADA': { label: 'Cansada', emoji: '🥱', color: '#ec4899', bg: 'rgba(236, 72, 153, 0.12)' }
};

const DEFAULT_STUDENT_JOURNAL_ENTRIES = [
  {
    id: 'journal_1',
    date: '2026-09-01',
    title: '¡Primer día del nuevo periodo y reto de Ciencias!',
    content: 'Hoy empezamos el proyecto de robótica y biología aplicada. La profesora nos enseñó cómo los sensores imitan los reflejos de los seres vivos. ¡Me sentí súper inspirada para armar mi maqueta con mi grupo de trabajo!',
    subject: 'Ciencias / Biología 🔬',
    mood: 'INSPIRADA',
    sticker: '🚀',
    privacy: 'PUBLIC',
    authorId: 'demo',
    authorName: 'Estudiante Escolaris',
    authorAvatar: '🎓',
    createdAt: Date.now() - 7200000
  },
  {
    id: 'journal_2',
    date: '2026-08-31',
    title: 'Repaso de geometría y figuras espaciales',
    content: 'El profesor Carlos explicó el cálculo de áreas en prismas. Al principio me costó un poco visualizar las tres dimensiones, pero después de hacer dos ejercicios prácticos con plastilina en clase lo comprendí todo.',
    subject: 'Matemáticas 📐',
    mood: 'ENFOCADA',
    sticker: '⭐',
    privacy: 'PUBLIC',
    authorId: 'demo',
    authorName: 'Estudiante Escolaris',
    authorAvatar: '🎓',
    createdAt: Date.now() - 86400000
  }
];

let studentJournalEntries = [];
let activeJournalMood = 'TODOS';
let activeJournalSearchQuery = '';

try {
  const savedJournals = localStorage.getItem('escolaris_student_journals');
  studentJournalEntries = savedJournals ? JSON.parse(savedJournals) : JSON.parse(JSON.stringify(DEFAULT_STUDENT_JOURNAL_ENTRIES));
} catch (e) {
  studentJournalEntries = JSON.parse(JSON.stringify(DEFAULT_STUDENT_JOURNAL_ENTRIES));
}

function saveStudentJournalsToStorage() {
  try {
    localStorage.setItem('escolaris_student_journals', JSON.stringify(studentJournalEntries));
  } catch (e) {
    console.error("Error guardando bitácoras:", e);
  }
}

function selectJournalMood(moodKey, btnEl) {
  const moodInput = document.getElementById('journal-entry-mood');
  if (moodInput) moodInput.value = moodKey;

  const allBtns = document.querySelectorAll('.mood-option-btn');
  allBtns.forEach(b => b.classList.remove('selected'));
  if (btnEl) btnEl.classList.add('selected');
}

function filterJournalMood(mood) {
  activeJournalMood = mood;
  const moods = ['TODOS', 'INSPIRADA', 'FELIZ', 'CREATIVA', 'TRANQUILA', 'ENFOCADA', 'CANSADA'];
  moods.forEach(m => {
    const btn = document.getElementById(`btn-journal-mood-${m}`);
    if (btn) {
      btn.className = m === mood ? 'btn-primary btn-filter active btn-sm' : 'btn-secondary btn-filter btn-sm';
    }
  });
  renderStudentJournal();
}

function filterJournalQuery() {
  activeJournalSearchQuery = (document.getElementById('input-search-journal')?.value || '').toLowerCase().trim();
  renderStudentJournal();
}

function openNewJournalModal() {
  const idIn = document.getElementById('journal-entry-id');
  const titleEl = document.getElementById('modal-journal-title');
  const dateIn = document.getElementById('journal-entry-date');
  const subjectIn = document.getElementById('journal-entry-subject');
  const moodIn = document.getElementById('journal-entry-mood');
  const titleIn = document.getElementById('journal-entry-title-input');
  const contentIn = document.getElementById('journal-entry-content');
  const stickerIn = document.getElementById('journal-entry-sticker');
  const privacyIn = document.getElementById('journal-entry-privacy');

  if (titleEl) titleEl.textContent = "Nueva Entrada en mi Bitácora";
  if (idIn) idIn.value = "";
  if (dateIn) dateIn.value = new Date().toISOString().split('T')[0];
  if (subjectIn) subjectIn.value = "General 🌟";
  if (moodIn) moodIn.value = "FELIZ";
  if (titleIn) titleIn.value = "";
  if (contentIn) contentIn.value = "";
  if (stickerIn) stickerIn.value = "⭐";
  if (privacyIn) privacyIn.value = "PUBLIC";

  // Select FELIZ visually in modal
  const moodBtns = document.querySelectorAll('.mood-option-btn');
  moodBtns.forEach((b, idx) => {
    if (idx === 0) b.classList.add('selected');
    else b.classList.remove('selected');
  });

  openModal('modal-journal-entry');
}

function openEditJournalModal(id) {
  const entry = studentJournalEntries.find(e => e.id === id);
  if (!entry) return;

  const idIn = document.getElementById('journal-entry-id');
  const titleEl = document.getElementById('modal-journal-title');
  const dateIn = document.getElementById('journal-entry-date');
  const subjectIn = document.getElementById('journal-entry-subject');
  const moodIn = document.getElementById('journal-entry-mood');
  const titleIn = document.getElementById('journal-entry-title-input');
  const contentIn = document.getElementById('journal-entry-content');
  const stickerIn = document.getElementById('journal-entry-sticker');
  const privacyIn = document.getElementById('journal-entry-privacy');

  if (titleEl) titleEl.textContent = "Editar Entrada de Bitácora";
  if (idIn) idIn.value = entry.id;
  if (dateIn) dateIn.value = entry.date || new Date().toISOString().split('T')[0];
  if (subjectIn) subjectIn.value = entry.subject || "General 🌟";
  if (moodIn) moodIn.value = entry.mood || "FELIZ";
  if (titleIn) titleIn.value = entry.title || "";
  if (contentIn) contentIn.value = entry.content || "";
  if (stickerIn) stickerIn.value = entry.sticker || "⭐";
  if (privacyIn) privacyIn.value = entry.privacy || "PUBLIC";

  // Visual mood button selection
  const moodBtns = document.querySelectorAll('.mood-option-btn');
  moodBtns.forEach(b => {
    const text = b.textContent || '';
    const moodConfig = JOURNAL_MOODS[entry.mood];
    if (moodConfig && text.includes(moodConfig.label.substring(0, 5))) {
      b.classList.add('selected');
    } else {
      b.classList.remove('selected');
    }
  });

  openModal('modal-journal-entry');
}

function handleSaveJournalEntry(event) {
  event.preventDefault();
  const idIn = document.getElementById('journal-entry-id')?.value.trim();
  const date = document.getElementById('journal-entry-date')?.value || new Date().toISOString().split('T')[0];
  const subject = document.getElementById('journal-entry-subject')?.value || "General 🌟";
  const mood = document.getElementById('journal-entry-mood')?.value || "FELIZ";
  const title = document.getElementById('journal-entry-title-input')?.value.trim() || "Entrada de Bitácora";
  const content = document.getElementById('journal-entry-content')?.value.trim() || "";
  const sticker = document.getElementById('journal-entry-sticker')?.value || "⭐";
  const privacy = document.getElementById('journal-entry-privacy')?.value || "PUBLIC";

  if (!content) {
    showToast("Por favor escribe tus pensamientos o aprendizajes en la bitácora. ✍️");
    return;
  }

  const authorName = currentUser ? currentUser.name : 'Estudiante';
  const authorAvatar = currentUser ? (currentUser.avatarEmoji || '🎓') : '🎓';
  const authorId = currentUser ? currentUser.uid : 'anon';

  if (idIn) {
    const idx = studentJournalEntries.findIndex(e => e.id === idIn);
    if (idx !== -1) {
      studentJournalEntries[idx] = {
        ...studentJournalEntries[idx],
        date,
        subject,
        mood,
        title,
        content,
        sticker,
        privacy,
        updatedAt: Date.now()
      };
      showToast("¡Entrada de bitácora actualizada con éxito! 💾✨");
    }
  } else {
    const newEntry = {
      id: 'journal_' + Date.now(),
      date,
      subject,
      mood,
      title,
      content,
      sticker,
      privacy,
      authorId,
      authorName,
      authorAvatar,
      createdAt: Date.now()
    };
    studentJournalEntries.unshift(newEntry);
    showToast("¡Entrada guardada en tu bitácora escolar! 📖✨ (+10 🪙 / +20 XP)");
    
    // Reward student with +10 credits and +20 XP for daily reflection
    if (currentUser && (currentUser.uid || currentUser.id) && typeof db !== 'undefined') {
      const uId = currentUser.uid || currentUser.id;
      try {
        db.collection('users').doc(uId).update({
          credits: firebase.firestore.FieldValue.increment(10),
          xp: firebase.firestore.FieldValue.increment(20)
        }).catch(err => console.log("Credits/XP increment error:", err));
        currentUser.credits = (currentUser.credits || 0) + 10;
        currentUser.xp = (currentUser.xp || 0) + 20;
        try { localStorage.setItem('escolaris_cached_user', JSON.stringify(currentUser)); } catch (e) {}
        renderRewards();
        renderProfileStore();
      } catch (e) {
        console.log("Error updating XP/Credits:", e);
      }
    }
  }

  saveStudentJournalsToStorage();
  closeModal('modal-journal-entry');
  renderStudentJournal();
}

function deleteJournalEntry(id) {
  const entry = studentJournalEntries.find(e => e.id === id);
  if (!entry) return;

  if (confirm(`¿Estás segura de eliminar la entrada "${entry.title}" de tu bitácora?`)) {
    studentJournalEntries = studentJournalEntries.filter(e => e.id !== id);
    saveStudentJournalsToStorage();
    showToast("Entrada eliminada de tu bitácora. 🗑️");
    renderStudentJournal();
  }
}

function renderStudentJournal() {
  const container = document.getElementById('journal-entries-list');
  if (!container) return;

  let entries = studentJournalEntries;

  // Filter privacy: students see all their entries, teachers & parents see PUBLIC entries
  if (currentUser && currentUser.role !== 'STUDENT') {
    entries = entries.filter(e => e.privacy === 'PUBLIC');
  }

  // Filter by mood
  if (activeJournalMood && activeJournalMood !== 'TODOS') {
    entries = entries.filter(e => e.mood === activeJournalMood);
  }

  // Filter by search query
  if (activeJournalSearchQuery) {
    const q = activeJournalSearchQuery;
    entries = entries.filter(e => 
      (e.title && e.title.toLowerCase().includes(q)) ||
      (e.content && e.content.toLowerCase().includes(q)) ||
      (e.subject && e.subject.toLowerCase().includes(q)) ||
      (e.authorName && e.authorName.toLowerCase().includes(q))
    );
  }

  if (entries.length === 0) {
    container.innerHTML = `
      <div class="journal-empty-state">
        <span style="font-size:36px;">📖</span>
        <h4 style="font-size:15px; font-weight:800; color:var(--text-main); margin:8px 0 4px 0;">Tu bitácora está esperando tu historia</h4>
        <p style="font-size:12px; color:var(--text-muted); line-height:1.5; margin:0 0 12px 0;">
          Escribe sobre lo que aprendiste hoy, tus retos superados o los momentos especiales en el colegio.
        </p>
        <button class="btn-primary btn-sm" onclick="openNewJournalModal()">
          ✍️ Escribir mi Primera Entrada
        </button>
      </div>
    `;
    return;
  }

  const isStudent = !currentUser || currentUser.role === 'STUDENT';

  container.innerHTML = entries.map(entry => {
    const moodConfig = JOURNAL_MOODS[entry.mood] || JOURNAL_MOODS['FELIZ'];
    const formattedDate = entry.date ? entry.date : 'Fecha reciente';
    const isOwner = isStudent || (currentUser && currentUser.uid === entry.authorId);

    return `
      <div class="clay-card journal-entry-card" style="border-left: 4px solid ${moodConfig.color};">
        <div class="journal-entry-header">
          <div class="journal-entry-meta">
            <span class="journal-mood-badge" style="background:${moodConfig.bg}; color:${moodConfig.color};">
              <span>${moodConfig.emoji}</span>
              <span>${moodConfig.label}</span>
            </span>
            <span style="font-size:12px;">${entry.sticker || '⭐'}</span>
            <span style="font-size:11.5px; font-weight:700; color:var(--text-muted);">🗓️ ${formattedDate}</span>
            <span class="journal-privacy-badge">
              ${entry.privacy === 'PRIVATE' ? '🔒 Privado' : '👨‍🏫 Compartido'}
            </span>
          </div>

          ${isOwner ? `
            <div style="display:flex; gap:4px; align-items:center;">
              <button class="btn-secondary btn-sm" style="padding:3px 7px; font-size:11px;" onclick="openEditJournalModal('${entry.id}')" title="Editar entrada">
                ✏️
              </button>
              <button class="btn-secondary btn-sm btn-danger-outline" style="padding:3px 7px; font-size:11px;" onclick="deleteJournalEntry('${entry.id}')" title="Eliminar entrada">
                🗑️
              </button>
            </div>
          ` : ''}
        </div>

        <h4 class="journal-entry-title">${escapeHtml(entry.title)}</h4>
        
        <p class="journal-entry-content">${escapeHtml(entry.content)}</p>

        <div class="journal-entry-footer">
          <span class="journal-subject-pill">${escapeHtml(entry.subject || 'General')}</span>
          ${entry.authorName ? `
            <span style="font-size:11px; color:var(--text-muted); font-weight:700;">
              ${entry.authorAvatar || '🎓'} ${escapeHtml(entry.authorName)}
            </span>
          ` : ''}
        </div>
      </div>
    `;
  }).join('');
}

// 16. Utility Helpers
function copyStudentCode() {
  if (!currentUser || !currentUser.studentCode) return;
  navigator.clipboard.writeText(currentUser.studentCode).then(() => {
    showToast(`📋 Código copiado: ${currentUser.studentCode}`);
  }).catch(() => {
    prompt("Copia tu código:", currentUser.studentCode);
  });
}

function copyTeacherCode() {
  if (!currentUser || !currentUser.teacherCode) return;
  navigator.clipboard.writeText(currentUser.teacherCode).then(() => {
    showToast(`📋 Código de clase copiado: ${currentUser.teacherCode}`);
  }).catch(() => {
    prompt("Copia tu código de clase:", currentUser.teacherCode);
  });
}

function handleProfilePhotoUpload(event) {
  const file = event.target.files && event.target.files[0];
  if (!file || !currentUser) return;

  if (!file.type.startsWith('image/')) {
    alert("Por favor selecciona un archivo de imagen válido (PNG, JPG, WEBP).");
    return;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    const img = new Image();
    img.onload = () => {
      // Client-side resize and compression to max 320x320
      const canvas = document.createElement('canvas');
      const MAX_SIZE = 320;
      let width = img.width;
      let height = img.height;

      if (width > height) {
        if (width > MAX_SIZE) {
          height *= MAX_SIZE / width;
          width = MAX_SIZE;
        }
      } else {
        if (height > MAX_SIZE) {
          width *= MAX_SIZE / height;
          height = MAX_SIZE;
        }
      }

      canvas.width = width;
      canvas.height = height;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(img, 0, 0, width, height);

      const base64Data = canvas.toDataURL('image/jpeg', 0.85);

      db.collection('users').doc(currentUser.id).update({
        photoUri: base64Data
      }).then(() => {
        currentUser.photoUri = base64Data;
        renderUserProfile();
        showToast("📸 ¡Foto de perfil actualizada con éxito!");
      }).catch((err) => {
        alert("Error al guardar foto: " + err.message);
      });
    };
    img.src = e.target.result;
  };
  reader.readAsDataURL(file);
}

function handleRemoveProfilePhoto() {
  if (!currentUser) return;
  if (confirm("¿Deseas quitar tu foto de perfil y volver al avatar predeterminado?")) {
    db.collection('users').doc(currentUser.id).update({
      photoUri: null
    }).then(() => {
      currentUser.photoUri = null;
      renderUserProfile();
      showToast("Foto eliminada. Se usará tu avatar emoji.");
    }).catch((err) => {
      alert("Error: " + err.message);
    });
  }
}

function handleJoinTeacherClass() {
  const input = document.getElementById('input-join-class-code');
  if (!input || !currentUser) return;
  const code = input.value.trim().toUpperCase();
  if (!code) {
    alert("Por favor ingresa el código del docente (Ej: DOC-8K2P10)");
    return;
  }

  const teacher = allUsers.find(u => u.teacherCode === code || u.id === code);
  if (!teacher) {
    alert("No se encontró ningún docente con el código " + code + ". Verifica e intenta nuevamente.");
    return;
  }

  db.collection('users').doc(currentUser.id).update({
    linkedTeacherCode: code
  }).then(() => {
    currentUser.linkedTeacherCode = code;
    renderUserProfile();
    showToast(`🎉 ¡Te has unido al aula del docente ${teacher.name}!`);
  }).catch((err) => {
    alert("Error al unirse a la clase: " + err.message);
  });
}

function handleLeaveTeacherClass() {
  if (!currentUser) return;
  if (confirm("¿Deseas desvincularte del aula del docente titular actual?")) {
    db.collection('users').doc(currentUser.id).update({
      linkedTeacherCode: null
    }).then(() => {
      currentUser.linkedTeacherCode = null;
      renderUserProfile();
      showToast("Te has desvinculado del aula.");
    }).catch((err) => {
      alert("Error: " + err.message);
    });
  }
}

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (!modal) return;
  modal.classList.add('open');

  if (modalId === 'modal-add-penalty' || modalId === 'modal-grant-badge' || modalId === 'modal-register-tardy') {
    populateStudentSelects();
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove('open');
}

function showToast(msg) {
  const toast = document.getElementById('toast');
  if (!toast) return;
  toast.textContent = msg;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 3200);
}

function toggleDarkMode() {
  document.body.classList.toggle('dark-mode');
  const isDark = document.body.classList.contains('dark-mode');
  localStorage.setItem('theme_mode', isDark ? 'dark' : 'light');
}

function checkIosPrompt() {
  const isIos = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream;
  const isStandalone = window.navigator.standalone || window.matchMedia('(display-mode: standalone)').matches;
  const dismissed = localStorage.getItem('escolaris_ios_banner_dismissed');

  if (isIos && !isStandalone && !dismissed) {
    const banner = document.getElementById('ios-pwa-banner');
    if (banner) banner.classList.remove('hidden');
  }
}

function dismissIosBanner() {
  const banner = document.getElementById('ios-pwa-banner');
  if (banner) banner.classList.add('hidden');
  localStorage.setItem('escolaris_ios_banner_dismissed', 'true');
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}
