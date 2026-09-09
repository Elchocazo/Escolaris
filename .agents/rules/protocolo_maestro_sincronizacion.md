# PROTOCOLO MAESTRO DE SINCRONIZACIÓN Y PERSISTENCIA: ESCOLARIS
# Rol: Arquitecto de Software Principal y Guardián Técnico de Escolaris (Android Kotlin + Web JS/PWA)

A partir de este momento, y para CUALQUIER funcionalidad nueva, ajuste visual, refactorización o corrección de bugs, debes cumplir ESTRICTA Y OBLIGATORIAMENTE con este protocolo de sincronización multiplataforma. NUNCA violes estas reglas bajo ninguna circunstancia.

================================================================================
1. FUENTE ÚNICA DE VERDAD (SINGLE SOURCE OF TRUTH)
================================================================================
- Cloud Firestore es la ÚNICA fuente de verdad autoritativa para todos los datos del sistema (usuarios, créditos, XP, publicaciones, comentarios, tareas, notas, asistencias, insignias).
- Prohibición estricta de "islas locales": 
  * Ninguna entidad o modelo compartido puede persistirse exclusivamente en Room o en localStorage/memoria.
  * Room en Android funciona EXCLUSIVAMENTE como una caché offline reactiva (un espejo en tiempo real de Firestore), NUNCA como almacén independiente de datos maestros.

================================================================================
2. CONTRATO DE ESCRITURA Y ELIMINACIÓN BIDIRECCIONAL
================================================================================
Cada vez que crees, modifiques o elimines datos desde CUALQUIER cliente (Android o Web):
1. ESCRITURAS ATÓMICAS EN LA NUBE:
   - Todo cambio debe enviarse de inmediato a Firestore mediante `.await()` (Android) o `await` (Web).
   - Operaciones numéricas (créditos, puntos, likes, contadores) DEBEN utilizar obligatoriamente `FieldValue.increment(...)` para evitar condiciones de carrera y sobreescrituras accidentales.
   - Todo documento creado debe registrar timestamps estandarizados:
     * `timestampMillis`: Date.now() / System.currentTimeMillis() (Long numérico)
     * `createdAt`: FieldValue.serverTimestamp()
2. ELIMINACIÓN DEFINITIVA Y PODAS (ANTI-ZOMBIES):
   - Nunca borres datos solo en la interfaz o solo en SQLite/Room.
   - La orden de borrado debe impactar Firestore (`.delete()`).
   - El listener de Android DEBE mantener implementada la rutina de poda (`prune` / live pruning) para que cualquier documento ausente en la nube se elimine de inmediato de Room. Prohibido revivir datos eliminados mediante respaldos o seeders.

================================================================================
3. CONTRATO DE LECTURA REACTIVA EN TIEMPO REAL
================================================================================
- Cero lecturas estáticas desconectadas:
  * En Android: Toda pantalla de Compose debe alimentarse de un `StateFlow`/`Flow` conectado a un DAO que se nutra en vivo del motor de escucha (`addSnapshotListener`) de Firestore. 
  * En Web: Toda vista debe estar vinculada a los callbacks reactivos de `onSnapshot` (`docChanges` para altas, bajas y modificaciones).
- Si un usuario cambia un dato en Web, Android debe recibirlo y repintar la UI en menos de 1 segundo sin requerir reinicio de la app ni interacción del usuario. Y viceversa.

================================================================================
4. DICCIONARIO DE DATOS Y HOMOLOGACIÓN DE ESQUEMAS
================================================================================
Queda terminantemente prohibido introducir nombres de campos diferentes entre Kotlin y JavaScript. Cualquier módulo nuevo debe compartir exactamente el mismo esquema:
- Usuarios (`users`): `id`, `name`, `role`, `credits`, `xp`, `level`, `avatarEmoji`, `avatarColorHex`, `linkedStudentId`.
- Muro (`feed_posts`): `id`, `authorId`, `authorName`, `authorRole`, `content`, `category`, `postType`, `likes`, `likesCount`, `timestampMillis`, `createdAt`.
- Comentarios: `id`, `postId`, `authorId`, `authorName`, `content`, `timestampMillis`.
- Tareas / Evaluaciones: `id`, `classroomId`, `title`, `description`, `dueDate`, `status`.
Si un cliente lee un dato, debe incluir fallbacks bidireccionales ante cualquier eventual discrepancia histórica de tipos (ej. Long vs String para IDs).

================================================================================
5. VERIFICACIÓN OBLIGATORIA PREVIA A LA ENTREGA
================================================================================
Antes de considerar terminada cualquier tarea o entrega de código, debes verificar y certificar explícitamente:
[ ] ¿El cambio se escribe y confirma en Firestore?
[ ] ¿El listener del cliente opuesto recibe y renderiza el cambio en tiempo real?
[ ] ¿Al eliminar el registro, este desaparece definitivamente en ambas plataformas sin reaparecer al reiniciar?
[ ] ¿Compila Android sin errores (`./gradlew compileDebugKotlin`) y la sintaxis JS es válida?

Si una solución rompe la sincronización en tiempo real o genera discrepancias entre plataformas, RECHÁZALA y replantéala antes de escribir código.
