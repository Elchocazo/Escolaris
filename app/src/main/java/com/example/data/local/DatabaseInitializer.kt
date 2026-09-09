package com.example.data.local

import com.example.data.local.dao.SchoolDao
import com.example.data.local.entity.ParentObligationEntity
import com.example.data.local.entity.RewardEntity
import com.example.data.local.entity.SchoolEventEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

object DatabaseInitializer {

    private fun eventTimeMillis(year: Int, month: Int, day: Int, hour: Int = 8, min: Int = 0): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Seeds default institutional catalog, parent obligations and official annual school events if empty.
     */
    suspend fun seedDatabaseIfEmpty(dao: SchoolDao) = withContext(Dispatchers.IO) {
        val defaultCatalogRewards = listOf(
            // Tier 1: Cotidianos & Privilegios de Aula (40 a 65 créditos)
            RewardEntity(title = "Salida Anticipada al Receso (3 min)", description = "Sal 3 minutos antes al descanso para comprar primero en cafetería.", costCredits = 45, category = "AULA", iconKey = "PASS", stockAvailable = 30, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Elegir Puesto de Clase por 1 Día", description = "Escoge tu puesto favorito en el aula durante toda la jornada.", costCredits = 50, category = "AULA", iconKey = "STAR", stockAvailable = 20, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Turno Preferencial en Cancha Escolar", description = "Prioridad para usar la cancha múltiple durante el descanso escolar.", costCredits = 55, category = "RECREO", iconKey = "STAR", stockAvailable = 15, teacherName = "José Benavides"),
            RewardEntity(title = "Música de Fondo en Taller Autónomo", description = "Elige la lista de reproducción ambiental autorizada para la clase.", costCredits = 65, category = "AULA", iconKey = "BOOK", stockAvailable = 10, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Préstamo de Juego de Mesa en Descanso", description = "Acceso a ajedrez o juegos de mesa institucionales en el recreo.", costCredits = 40, category = "RECREO", iconKey = "STAR", stockAvailable = 25, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Uso de Marcadores Especiales en Tablero", description = "Escribe tus ejercicios en clase con marcadores de colores especiales.", costCredits = 45, category = "AULA", iconKey = "STAR", stockAvailable = 20, teacherName = "Manuel Muñoz"),

            // Tier 2: Beneficios Académicos Formativos (85 a 150 créditos)
            RewardEntity(title = "Pase de Prórroga en Tarea (+24h)", description = "Plazo adicional de un día escolar para entregar una tarea sin penalización.", costCredits = 110, category = "ACADEMICO", iconKey = "PASS", stockAvailable = 25, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Profesor Asistente / Monitor por 1 Día", description = "Acompaña al docente titular coordinando actividades y dinámicas de clase.", costCredits = 130, category = "AULA", iconKey = "BADGE", stockAvailable = 10, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Descarte de Peor Nota en Taller Corto", description = "Anula la calificación más baja obtenida en un quiz o taller menor.", costCredits = 150, category = "ACADEMICO", iconKey = "GRADE", stockAvailable = 15, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Tolerancia de Retardo (Emergencia)", description = "Exonera una llegada tarde justificada sin registro disciplinario.", costCredits = 95, category = "AULA", iconKey = "PASS", stockAvailable = 20, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Elegir Compañero en Trabajo Grupal", description = "Selecciona con quién trabajar en la próxima actividad grupal o taller.", costCredits = 120, category = "ACADEMICO", iconKey = "STAR", stockAvailable = 20, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Elegir Tema o Dinámica de Clase", description = "Propón un tema de debate o ejercicio práctico para una sesión académica.", costCredits = 85, category = "ACADEMICO", iconKey = "BOOK", stockAvailable = 10, teacherName = "Manuel Muñoz"),

            // Tier 3: Privilegios Destacados & Cafetería (200 a 290 créditos)
            RewardEntity(title = "Punto Extra (+0.5) en Evaluación", description = "Suma +0.5 a una evaluación o prueba formativa del periodo escolar.", costCredits = 240, category = "ACADEMICO", iconKey = "GRADE", stockAvailable = 20, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Bono de Merienda en Cafetería Escolar", description = "Reclama un snack y jugo natural en la tienda escolar del colegio.", costCredits = 260, category = "CAFETERIA", iconKey = "CUP", stockAvailable = 15, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Elegir Puesto Fijo por 1 Semana", description = "Ubicación preferencial asegurada durante cinco días hábiles de clase.", costCredits = 220, category = "AULA", iconKey = "STAR", stockAvailable = 10, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Multiplicador x2 de Créditos en Tarea", description = "Duplica los créditos ganados al entregar tu próxima tarea impecable.", costCredits = 200, category = "ACADEMICO", iconKey = "STAR", stockAvailable = 15, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Kit Escolar de Creatividad & Útiles", description = "Cuaderno institucional, bolígrafos de gel y resaltadores pastel.", costCredits = 290, category = "ACADEMICO", iconKey = "BOOK", stockAvailable = 12, teacherName = "Manuel Muñoz"),

            // Tier 4: Grandes Distinciones Institucionales (480 a 580 créditos)
            RewardEntity(title = "Pase Dorado: Exención de 1 Tarea Menor", description = "Quedas exento de una tarea regular con calificación máxima automática (5.0).", costCredits = 520, category = "ACADEMICO", iconKey = "PASS", stockAvailable = 8, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Combo Almuerzo Especial en Cafetería", description = "Almuerzo completo premium en la cafetería escolar del colegio.", costCredits = 580, category = "CAFETERIA", iconKey = "CUP", stockAvailable = 10, teacherName = "Manuel Muñoz"),
            RewardEntity(title = "Diploma de Honor Elisiano en Bandera", description = "Reconocimiento solemne en formación matutina por constancia y mérito.", costCredits = 480, category = "DISTINCION", iconKey = "CUP", stockAvailable = 5, teacherName = "Manuel Muñoz")
        )

        val existingRewards = dao.getAllRewardsList()
        val existingTitles = existingRewards.map { it.title.lowercase().trim() }.toSet()
        val rewardsToInsert = defaultCatalogRewards.filter { !existingTitles.contains(it.title.lowercase().trim()) }
        if (rewardsToInsert.isNotEmpty()) {
            dao.insertRewards(rewardsToInsert)
        }

        if (dao.getParentObligationsCount() == 0) {
            val defaultParentObligations = listOf(
                ParentObligationEntity(
                    parentId = "ALL",
                    studentId = "ALL",
                    title = "Pago Oportuno de Pensión (Octubre)",
                    description = "La pensión escolar se cancela dentro de los 5 primeros días del mes. ¡Cumple a tiempo para ganar la insignia de Pago Oportuno y 100 🪙 para tu hijo/a! ⭐",
                    category = "PENSION",
                    month = "Octubre",
                    dueDayOfMonth = 5,
                    dueDateMillis = System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000),
                    isCompleted = false,
                    rewardBadgeKey = "PARENT_PENSION_OCTUBRE",
                    rewardBadgeTitle = "Pensión al Día - Octubre 💳",
                    rewardBadgeEmoji = "💳",
                    rewardCredits = 100,
                    rewardXp = 150,
                    whatsappMessage = "¡Hola estimado acudiente! 👋 Les recordamos que la pensión escolar se cancela dentro de los 5 primeros días del mes. ¡Cumple a tiempo para ganar la insignia de Pago Oportuno y 100 🪙 para tu hijo/a! ⭐",
                    createdByTeacher = "Tesorería Escolar & Dirección"
                ),
                ParentObligationEntity(
                    parentId = "ALL",
                    studentId = "ALL",
                    title = "Asistencia a Reunión de Padres de Familia",
                    description = "Reunión general de padres de familia y directores de grado programada para el 3 de Septiembre a las 5:00 PM.",
                    category = "EVENT",
                    month = "Septiembre",
                    dueDayOfMonth = 3,
                    dueDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 3, 17, 0),
                    isCompleted = false,
                    rewardBadgeKey = "PARENT_MEETING_1",
                    rewardBadgeTitle = "Asistencia a 1ª Reunión 👨‍👩‍👧",
                    rewardBadgeEmoji = "👨‍👩‍👧",
                    rewardCredits = 80,
                    rewardXp = 120,
                    whatsappMessage = "Apreciada familia: Los esperamos en la Reunión General de Padres de Familia el 3 de Septiembre a las 5:00 PM.",
                    createdByTeacher = "Dirección de Grupo"
                ),
                ParentObligationEntity(
                    parentId = "ALL",
                    studentId = "ALL",
                    title = "Asistencia a Escuela de Padres",
                    description = "Taller formativo y acompañamiento familiar en valores programado para el 15 de Octubre a las 5:00 PM.",
                    category = "EVENT",
                    month = "Octubre",
                    dueDayOfMonth = 15,
                    dueDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 15, 17, 0),
                    isCompleted = false,
                    rewardBadgeKey = "PARENT_COLLABORATOR",
                    rewardBadgeTitle = "Escuela de Padres 🤝",
                    rewardBadgeEmoji = "🤝",
                    rewardCredits = 100,
                    rewardXp = 140,
                    whatsappMessage = "Estimados padres: Cordial invitación al encuentro de Escuela de Padres el 15 de Octubre a las 5:00 PM.",
                    createdByTeacher = "Orientación Escolar & Psicología"
                ),
                ParentObligationEntity(
                    parentId = "ALL",
                    studentId = "ALL",
                    title = "Asistencia a Entrega de Informes Académicos",
                    description = "Jornada institucional de entrega de notas y seguimiento académico con el docente titular.",
                    category = "EVENT",
                    month = "Noviembre",
                    dueDayOfMonth = 13,
                    dueDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 13, 8, 0),
                    isCompleted = false,
                    rewardBadgeKey = "PARENT_REPORT_CARD",
                    rewardBadgeTitle = "Entrega de Informes 📋",
                    rewardBadgeEmoji = "📋",
                    rewardCredits = 80,
                    rewardXp = 120,
                    whatsappMessage = "Apreciados acudientes: Los esperamos en la jornada de entrega de informes académicos del periodo.",
                    createdByTeacher = "Docente Titular"
                )
            )
            dao.insertParentObligations(defaultParentObligations)
        }

        if (dao.getSchoolEventsCount() == 0) {
            val officialSchoolEvents = listOf(
                // SEPTIEMBRE
                SchoolEventEntity(
                    title = "Bienvenida escolar",
                    category = "INSTITUTIONAL",
                    eventDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 1),
                    endDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 1),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF2563EB
                ),
                SchoolEventEntity(
                    title = "Reunión de padres de familia",
                    category = "COMMUNITY",
                    eventDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 3, 17, 0),
                    endDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 3, 17, 0),
                    eventTime = "5:00 PM",
                    location = "",
                    description = "",
                    colorHex = 0xFF7C3AED
                ),
                SchoolEventEntity(
                    title = "Pascua madre Elisa • Día del estudiante elisiano",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 11),
                    endDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 11),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFF59E0B
                ),
                SchoolEventEntity(
                    title = "Presentación personeras",
                    category = "CIVIC",
                    eventDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 14),
                    endDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 14),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF10B981
                ),
                SchoolEventEntity(
                    title = "Amor y amistad - Jeanday",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 18),
                    endDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 18),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFEC4899
                ),
                SchoolEventEntity(
                    title = "Menú especial de la cafetería",
                    category = "CAFETERIA",
                    eventDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 25),
                    endDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 25),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFF97316
                ),
                SchoolEventEntity(
                    title = "Debate personeras",
                    category = "CIVIC",
                    eventDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 30),
                    endDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 30),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF6366F1
                ),

                // OCTUBRE
                SchoolEventEntity(
                    title = "Elección de la personera",
                    category = "CIVIC",
                    eventDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 2),
                    endDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 2),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF10B981
                ),
                SchoolEventEntity(
                    title = "Semana de receso escolar (Octubre 5 - 9)",
                    category = "INSTITUTIONAL",
                    eventDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 5),
                    endDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 9),
                    eventTime = "",
                    location = "",
                    description = "Octubre 5 - 9",
                    colorHex = 0xFF059669
                ),
                SchoolEventEntity(
                    title = "Izada de bandera",
                    category = "CIVIC",
                    eventDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 14),
                    endDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 14),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF2563EB
                ),
                SchoolEventEntity(
                    title = "Escuela de padres",
                    category = "COMMUNITY",
                    eventDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 15, 17, 0),
                    endDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 15, 17, 0),
                    eventTime = "5:00 PM",
                    location = "",
                    description = "",
                    colorHex = 0xFF7C3AED
                ),
                SchoolEventEntity(
                    title = "Evaluaciones de primer periodo (Octubre 26 - 30)",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 26),
                    endDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 30),
                    eventTime = "",
                    location = "",
                    description = "Octubre 26 - 30",
                    colorHex = 0xFFDC2626
                ),
                SchoolEventEntity(
                    title = "Jean day - Menú especial cafeteria",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 30),
                    endDateMillis = eventTimeMillis(2026, Calendar.OCTOBER, 30),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFF97316
                ),

                // NOVIEMBRE
                SchoolEventEntity(
                    title = "Actividades de refuerzo (Noviembre 3 y 4)",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 3),
                    endDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 4),
                    eventTime = "",
                    location = "",
                    description = "Noviembre 3 y 4",
                    colorHex = 0xFFD97706
                ),
                SchoolEventEntity(
                    title = "Inicia segundo periodo",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 5),
                    endDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 5),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF2563EB
                ),
                SchoolEventEntity(
                    title = "Izada de bandera rendimiento académico (a cargo de grado 7°)",
                    category = "CIVIC",
                    eventDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 12),
                    endDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 12),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF10B981
                ),
                SchoolEventEntity(
                    title = "5° festival folclórico Elisiano",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 20),
                    endDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 20),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF9333EA
                ),
                SchoolEventEntity(
                    title = "Inicio de las novenas de navidad",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 26),
                    endDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 26),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF16A34A
                ),
                SchoolEventEntity(
                    title = "Menú especial de la cafetería",
                    category = "CAFETERIA",
                    eventDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 27),
                    endDateMillis = eventTimeMillis(2026, Calendar.NOVEMBER, 27),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFEA580C
                ),

                // DICIEMBRE
                SchoolEventEntity(
                    title = "Entrega de boletines (No hay clases)",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2026, Calendar.DECEMBER, 4, 6, 45),
                    endDateMillis = eventTimeMillis(2026, Calendar.DECEMBER, 4, 6, 45),
                    eventTime = "6:45 AM",
                    location = "",
                    description = "No hay clases",
                    colorHex = 0xFF2563EB
                ),
                SchoolEventEntity(
                    title = "Novena grado 7° y 8°",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2026, Calendar.DECEMBER, 7),
                    endDateMillis = eventTimeMillis(2026, Calendar.DECEMBER, 7),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFF59E0B
                ),
                SchoolEventEntity(
                    title = "Vacaciones de navidad",
                    category = "INSTITUTIONAL",
                    eventDateMillis = eventTimeMillis(2026, Calendar.DECEMBER, 10),
                    endDateMillis = eventTimeMillis(2026, Calendar.DECEMBER, 10),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFDC2626
                ),

                // ENERO
                SchoolEventEntity(
                    title = "Inicio de clases",
                    category = "INSTITUTIONAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.JANUARY, 12),
                    endDateMillis = eventTimeMillis(2027, Calendar.JANUARY, 12),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF2563EB
                ),
                SchoolEventEntity(
                    title = "Izada de bandera",
                    category = "CIVIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.JANUARY, 14),
                    endDateMillis = eventTimeMillis(2027, Calendar.JANUARY, 14),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF10B981
                ),
                SchoolEventEntity(
                    title = "Evaluaciones de segundo periodo (Enero 27 - Febrero 2)",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.JANUARY, 27),
                    endDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 2),
                    eventTime = "",
                    location = "",
                    description = "Enero 27 - febrero 2",
                    colorHex = 0xFFDC2626
                ),
                SchoolEventEntity(
                    title = "Menú especial de la cafetería",
                    category = "CAFETERIA",
                    eventDateMillis = eventTimeMillis(2027, Calendar.JANUARY, 29),
                    endDateMillis = eventTimeMillis(2027, Calendar.JANUARY, 29),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFF97316
                ),

                // FEBRERO
                SchoolEventEntity(
                    title = "Actividades de refuerzo (Febrero 3 y 4)",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 3),
                    endDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 4),
                    eventTime = "",
                    location = "",
                    description = "Febrero 3 y 4",
                    colorHex = 0xFFD97706
                ),
                SchoolEventEntity(
                    title = "Inicio de tercer periodo",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 8),
                    endDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 8),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF2563EB
                ),
                SchoolEventEntity(
                    title = "Izada de bandera rendimiento académico",
                    category = "CIVIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 9),
                    endDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 9),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF10B981
                ),
                SchoolEventEntity(
                    title = "Entrega de boletines",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 10, 17, 0),
                    endDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 10, 17, 0),
                    eventTime = "5:00 PM",
                    location = "",
                    description = "",
                    colorHex = 0xFF7C3AED
                ),
                SchoolEventEntity(
                    title = "Escuela de padres",
                    category = "COMMUNITY",
                    eventDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 18, 17, 0),
                    endDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 18, 17, 0),
                    eventTime = "5:00 PM",
                    location = "",
                    description = "",
                    colorHex = 0xFF7C3AED
                ),
                SchoolEventEntity(
                    title = "Menú especial cafeteria",
                    category = "CAFETERIA",
                    eventDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 26),
                    endDateMillis = eventTimeMillis(2027, Calendar.FEBRUARY, 26),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFF97316
                ),

                // MARZO
                SchoolEventEntity(
                    title = "Día de la mujer",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MARCH, 8),
                    endDateMillis = eventTimeMillis(2027, Calendar.MARCH, 8),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFEC4899
                ),
                SchoolEventEntity(
                    title = "Bazar de la familia elisiana",
                    category = "COMMUNITY",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MARCH, 14),
                    endDateMillis = eventTimeMillis(2027, Calendar.MARCH, 14),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFF59E0B
                ),
                SchoolEventEntity(
                    title = "No hay clase",
                    category = "INSTITUTIONAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MARCH, 15),
                    endDateMillis = eventTimeMillis(2027, Calendar.MARCH, 15),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF6B7280
                ),
                SchoolEventEntity(
                    title = "Semana elisiana (Marzo 16 al 19)",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MARCH, 16),
                    endDateMillis = eventTimeMillis(2027, Calendar.MARCH, 19),
                    eventTime = "",
                    location = "",
                    description = "Marzo 16 al 19",
                    colorHex = 0xFF8B5CF6
                ),
                SchoolEventEntity(
                    title = "Semana elisiana responsable 5°, 6°, 7°",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MARCH, 18),
                    endDateMillis = eventTimeMillis(2027, Calendar.MARCH, 18),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF8B5CF6
                ),
                SchoolEventEntity(
                    title = "Menú especial de la cafetería",
                    category = "CAFETERIA",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MARCH, 19),
                    endDateMillis = eventTimeMillis(2027, Calendar.MARCH, 19),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFF97316
                ),
                SchoolEventEntity(
                    title = "Semana santa (Marzo 22 - 26)",
                    category = "INSTITUTIONAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MARCH, 22),
                    endDateMillis = eventTimeMillis(2027, Calendar.MARCH, 26),
                    eventTime = "",
                    location = "",
                    description = "Marzo 22 - 26",
                    colorHex = 0xFF059669
                ),
                SchoolEventEntity(
                    title = "Reinicio de labores escolares",
                    category = "INSTITUTIONAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MARCH, 29),
                    endDateMillis = eventTimeMillis(2027, Calendar.MARCH, 29),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF2563EB
                ),

                // ABRIL
                SchoolEventEntity(
                    title = "Evaluaciones de tercer periodo (Abril 7 - 13)",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.APRIL, 7),
                    endDateMillis = eventTimeMillis(2027, Calendar.APRIL, 13),
                    eventTime = "",
                    location = "",
                    description = "Abril 7 - 13",
                    colorHex = 0xFFDC2626
                ),
                SchoolEventEntity(
                    title = "Actividades de refuerzo (Abril 14 - 15)",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.APRIL, 14),
                    endDateMillis = eventTimeMillis(2027, Calendar.APRIL, 15),
                    eventTime = "",
                    location = "",
                    description = "Abril 14 - 15",
                    colorHex = 0xFFD97706
                ),
                SchoolEventEntity(
                    title = "Inicia el 4to periodo",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.APRIL, 16),
                    endDateMillis = eventTimeMillis(2027, Calendar.APRIL, 16),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF2563EB
                ),
                SchoolEventEntity(
                    title = "Izada de bandera día del idioma - Menú especial de la cafetería",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.APRIL, 23),
                    endDateMillis = eventTimeMillis(2027, Calendar.APRIL, 23),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF10B981
                ),
                SchoolEventEntity(
                    title = "Entrega de boletines personalizada",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.APRIL, 28),
                    endDateMillis = eventTimeMillis(2027, Calendar.APRIL, 28),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF7C3AED
                ),

                // MAYO
                SchoolEventEntity(
                    title = "Celebración día del maestro - Jeanday",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MAY, 13),
                    endDateMillis = eventTimeMillis(2027, Calendar.MAY, 13),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF6366F1
                ),
                SchoolEventEntity(
                    title = "No hay clase",
                    category = "INSTITUTIONAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MAY, 14),
                    endDateMillis = eventTimeMillis(2027, Calendar.MAY, 14),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF6B7280
                ),
                SchoolEventEntity(
                    title = "Semana de la familia (Mayo 17 - 21)",
                    category = "CULTURAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MAY, 17),
                    endDateMillis = eventTimeMillis(2027, Calendar.MAY, 21),
                    eventTime = "",
                    location = "",
                    description = "Mayo 17 - 21",
                    colorHex = 0xFFEC4899
                ),
                SchoolEventEntity(
                    title = "Día de la familia",
                    category = "COMMUNITY",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MAY, 21),
                    endDateMillis = eventTimeMillis(2027, Calendar.MAY, 21),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFEC4899
                ),
                SchoolEventEntity(
                    title = "Menú especial de la cafetería",
                    category = "CAFETERIA",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MAY, 27),
                    endDateMillis = eventTimeMillis(2027, Calendar.MAY, 27),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFF97316
                ),
                SchoolEventEntity(
                    title = "Entrega de insignias",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.MAY, 28),
                    endDateMillis = eventTimeMillis(2027, Calendar.MAY, 28),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFFF59E0B
                ),

                // JUNIO
                SchoolEventEntity(
                    title = "Evaluaciones de cuarto periodo (Junio 8 - 14)",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.JUNE, 8),
                    endDateMillis = eventTimeMillis(2027, Calendar.JUNE, 14),
                    eventTime = "",
                    location = "",
                    description = "Junio 8 - 14",
                    colorHex = 0xFFDC2626
                ),
                SchoolEventEntity(
                    title = "Actividades de refuerzo (Junio 15 - 16)",
                    category = "ACADEMIC",
                    eventDateMillis = eventTimeMillis(2027, Calendar.JUNE, 15),
                    endDateMillis = eventTimeMillis(2027, Calendar.JUNE, 16),
                    eventTime = "",
                    location = "",
                    description = "Junio 15 - 16",
                    colorHex = 0xFFD97706
                ),
                SchoolEventEntity(
                    title = "Clausuras",
                    category = "INSTITUTIONAL",
                    eventDateMillis = eventTimeMillis(2027, Calendar.JUNE, 23),
                    endDateMillis = eventTimeMillis(2027, Calendar.JUNE, 23),
                    eventTime = "",
                    location = "",
                    description = "",
                    colorHex = 0xFF10B981
                )
            )
            dao.insertSchoolEvents(officialSchoolEvents)
        }

        // Restauración garantizada del evento del 3 de Septiembre
        val allEvents = dao.getAllSchoolEventsList()
        val hasSept3Meeting = allEvents.any { it.title.contains("Reunión de padres", ignoreCase = true) }
        if (!hasSept3Meeting) {
            dao.insertSchoolEvent(
                SchoolEventEntity(
                    title = "Reunión de padres de familia",
                    category = "COMMUNITY",
                    eventDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 3, 17, 0),
                    endDateMillis = eventTimeMillis(2026, Calendar.SEPTEMBER, 3, 17, 0),
                    eventTime = "5:00 PM",
                    location = "Salón Múltiple",
                    description = "Reunión general de padres de familia y directores de grado.",
                    colorHex = 0xFF7C3AED
                )
            )
        }

        // Sembrado automático del listado institucional de 7° grado (Estudiantes y Acudientes)
        seedInstitutionalRoster(dao)
    }

    /**
     * Seeds the institutional roster of 7th grade students and their parents.
     * Preserves any existing records without overwriting modifications.
     */
    suspend fun seedInstitutionalRoster(dao: SchoolDao) = withContext(Dispatchers.IO) {
        // 1. Normalizar todos los usuarios en la base de datos local para que sus nombres propios siempre inicien con Mayúscula
        try {
            val allExisting = dao.getAllUsersDirect()
            for (u in allExisting) {
                val properName = com.example.domain.validation.ValidationUtils.formatProperNoun(u.name)
                val properLastName = com.example.domain.validation.ValidationUtils.formatProperNoun(u.lastName)
                if (properName != u.name || properLastName != u.lastName) {
                    dao.updateUser(u.copy(name = properName, lastName = properLastName))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Sembrado de la lista institucional garantizando mayúsculas en nombres propios
        for (rawUser in OFFICIAL_INSTITUTIONAL_ROSTER) {
            val user = rawUser.copy(
                name = com.example.domain.validation.ValidationUtils.formatProperNoun(rawUser.name),
                lastName = com.example.domain.validation.ValidationUtils.formatProperNoun(rawUser.lastName)
            )
            val existing = dao.getUserDirect(user.id)
            if (existing == null) {
                val existingByEmail = dao.getUserByEmail(user.email)
                if (existingByEmail == null) {
                    dao.insertUser(user)
                }
            } else {
                val properExistingName = com.example.domain.validation.ValidationUtils.formatProperNoun(existing.name)
                val properExistingLastName = com.example.domain.validation.ValidationUtils.formatProperNoun(existing.lastName)
                if (existing.email != user.email || 
                    (user.linkedStudentId != null && existing.linkedStudentId != user.linkedStudentId) ||
                    existing.name != properExistingName ||
                    existing.lastName != properExistingLastName
                ) {
                    dao.updateUser(
                        existing.copy(
                            name = properExistingName,
                            lastName = properExistingLastName,
                            email = user.email,
                            linkedStudentId = user.linkedStudentId ?: existing.linkedStudentId
                        )
                    )
                }
            }
        }
    }

    val OFFICIAL_INSTITUTIONAL_ROSTER: List<UserEntity> = listOf(
        // ==========================================
        // 1. JUAN ANDRÉS NARVÁEZ CALAMBAS & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1059247264",
            name = "Juan Andrés Narváez Calambas",
            email = "juan.narvaez1059247264@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-247264",
            avatarColorHex = 0xFF2563EB,
            avatarInitials = "JN",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Sanitas | RH: A+ | TI: 1059247264 | F.Nac: 15/07/2015",
            avatarEmoji = "🎓",
            phoneNumber = "3019384187"
        ),
        UserEntity(
            id = "par_1059247264_1",
            name = "Cristina Isabel Calambas Erazo",
            email = "cristinacalambas96@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "CC",
            gradeSection = "Padre/Tutor de Juan Andrés Narváez",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1059247264",
            bio = "Acudiente de Juan Andrés Narváez Calambas | Tel: 3019384187 | Calle 5 #27-24",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3019384187"
        ),

        // ==========================================
        // 2. PAULA ANDREA URREA SANDOVAL & ACUDIENTES
        // ==========================================
        UserEntity(
            id = "std_1058937460",
            name = "Paula Andrea Urrea Sandoval",
            email = "paula.urrea1058937460@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-937460",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "PU",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Sanitas | RH: A+ | TI: 1058937460 | F.Nac: 06/11/2014",
            avatarEmoji = "🎓",
            phoneNumber = "3206374657"
        ),
        UserEntity(
            id = "par_1058937460_1",
            name = "Sandra Milena Sandoval",
            email = "smsandoval7@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "SS",
            gradeSection = "Padre/Tutor de Paula Andrea Urrea",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1058937460",
            bio = "Acudiente de Paula Andrea Urrea Sandoval | Tel: 3206374657 | Tv. 9 Nte. #56N-78 Condominio Monserrat",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3206374657"
        ),
        UserEntity(
            id = "par_1058937460_2",
            name = "Juan Urrea Murillo",
            email = "juan.urrea.murillo@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "JU",
            gradeSection = "Padre/Tutor de Paula Andrea Urrea",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1058937460",
            bio = "Acudiente de Paula Andrea Urrea Sandoval | Tel: 3206374657 | Tv. 9 Nte. #56N-78 Condominio Monserrat",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3206374657"
        ),

        // ==========================================
        // 3. ANGELLY DANIELA GONZÁLEZ MOSQUERA & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1058551766",
            name = "Angelly Daniela González Mosquera",
            email = "angelly.gonzalez1058551766@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-551766",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "AG",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Fomag | RH: O+ | TI: 1058551766 | F.Nac: 03/03/2014",
            avatarEmoji = "🎓",
            phoneNumber = "3165799079"
        ),
        UserEntity(
            id = "par_1058551766_1",
            name = "Yency Mosquera Lopez",
            email = "molyml241@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "YM",
            gradeSection = "Padre/Tutor de Angelly Daniela González",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1058551766",
            bio = "Acudiente de Angelly Daniela González Mosquera | Tel: 3165799079 | Santana Cajete",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3165799079"
        ),

        // ==========================================
        // 4. ANNY SOFÍA ASTAIZA LÓPEZ & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1166465051",
            name = "Anny Sofía Astaiza López",
            email = "anny.astaiza1166465051@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-465051",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "AA",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1166465051 | F.Nac: 26/10/2013",
            avatarEmoji = "🎓",
            phoneNumber = "3103618032"
        ),
        UserEntity(
            id = "par_1166465051_1",
            name = "Nohemi Lopez Tobar",
            email = "nelcynohemilopez2705@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "NL",
            gradeSection = "Padre/Tutor de Anny Sofía Astaiza",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1166465051",
            bio = "Acudiente de Anny Sofía Astaiza López | Tel: 3103618032 | Torres de San Eduardo Torre 2a apto 403",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3103618032"
        ),

        // ==========================================
        // 5. ANA SOFÍA BONILLA CAMACHO & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1061791248",
            name = "Ana Sofía Bonilla Camacho",
            email = "ana.bonilla1061791248@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-791248",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "AB",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Nueva EPS | RH: B+ | TI: 1061791248 | F.Nac: 10/06/2014",
            avatarEmoji = "🎓",
            phoneNumber = "3137636281"
        ),
        UserEntity(
            id = "par_1061791248_1",
            name = "Nelson Eduardo Bonilla González",
            email = "nelson13bonilla@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "NB",
            gradeSection = "Padre/Tutor de Ana Sofía Bonilla",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1061791248",
            bio = "Acudiente de Ana Sofía Bonilla Camacho | Tel: 3137636281 - 3135313054 | Conjunto arrayanes T1 Apto 301 / Calle 3A #13-58",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3137636281"
        ),

        // ==========================================
        // 6. MARIANGEL CASTILLO PINO (ESTUDIANTE CON CORREO) & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1058974668",
            name = "Mariangel Castillo Pino",
            email = "castillopinomariangel@gmail.com",
            role = "STUDENT",
            studentCode = "ESC-897468",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "MC",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Nueva EPS | RH: A+ | TI: 1058974668 | F.Nac: 13/12/2013",
            avatarEmoji = "🎓",
            phoneNumber = "3127764316"
        ),
        UserEntity(
            id = "par_1058974668_1",
            name = "Liseth Natalia Pino Perez",
            email = "liseth.pino1058974668@escolaris.edu.co",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "LP",
            gradeSection = "Padre/Tutor de Mariangel Castillo",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1058974668",
            bio = "Acudiente de Mariangel Castillo Pino | Tel: 3127764316 | Cra 12 # 7A-29 B/Valencia",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3127764316"
        ),

        // ==========================================
        // 7. MARÍA ISABELLA SAMBONÍ OROZCO & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1058937457",
            name = "María Isabella Samboní Orozco",
            email = "maria.samboni1058937457@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-937457",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "MS",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1058937457 | F.Nac: 24/10/2014",
            avatarEmoji = "🎓",
            phoneNumber = "3136659212"
        ),
        UserEntity(
            id = "par_1058937457_1",
            name = "Erika Yoslany Orozco Mampotes",
            email = "eyorozcom@ut.edu.co",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "EO",
            gradeSection = "Padre/Tutor de María Isabella Samboní",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1058937457",
            bio = "Acudiente de María Isabella Samboní Orozco | Tel: 3136659212 - 3164252273 | Cra 13 b # 12 a 39",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3136659212"
        ),

        // ==========================================
        // 8. MEGAN LUCIANA OLAYA CAICEDO & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1058552648",
            name = "Megan Luciana Olaya Caicedo",
            email = "megan.olaya1058552648@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-552648",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "MO",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: S.O.S | RH: O+ | TI: 1058552648 | F.Nac: 16/12/2015",
            avatarEmoji = "🎓",
            phoneNumber = "3102841302"
        ),
        UserEntity(
            id = "par_1058552648_1",
            name = "Claudia Caicedo",
            email = "claudiacaicedo22@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "CC",
            gradeSection = "Padre/Tutor de Megan Luciana Olaya",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1058552648",
            bio = "Acudiente de Megan Luciana Olaya Caicedo | Tel: 3102841302 | Calle 8b # 21a 32",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3102841302"
        ),

        // ==========================================
        // 9. DAVID SANTIAGO RUIZ BOLAÑOS & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1061796113",
            name = "David Santiago Ruiz Bolaños",
            email = "david.ruiz1061796113@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-796113",
            avatarColorHex = 0xFF2563EB,
            avatarInitials = "DR",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1061796113 | F.Nac: 30/11/2014",
            avatarEmoji = "🎓",
            phoneNumber = "3138618318"
        ),
        UserEntity(
            id = "par_1061796113_1",
            name = "Lisseth Bolaños",
            email = "lissbolanos93@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "LB",
            gradeSection = "Padre/Tutor de David Santiago Ruiz",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1061796113",
            bio = "Acudiente de David Santiago Ruiz Bolaños | Tel: 3138618318 | Calle 2c # 58-02",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3138618318"
        ),

        // ==========================================
        // 10. MARÍA PAULA BURBANO CHATE & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1058552020",
            name = "María Paula Burbano Chate",
            email = "maria.burbano1058552020@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-552020",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "MB",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Sanitas | RH: A+ | TI: 1058552020 | F.Nac: 06/08/2014",
            avatarEmoji = "🎓",
            phoneNumber = "3113217331"
        ),
        UserEntity(
            id = "par_1058552020_1",
            name = "María Chate",
            email = "malychate69@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "MC",
            gradeSection = "Padre/Tutor de María Paula Burbano",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1058552020",
            bio = "Acudiente de María Paula Burbano Chate | Tel: 3113217331 | Calle 15 # 17-97",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3113217331"
        ),

        // ==========================================
        // 11. IHARA DANIELLA RIASCOS TRÓCHEZ & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1058552265",
            name = "Ihara Daniella Riascos Tróchez",
            email = "Iharadanielariascostrochez@gmail.com",
            role = "STUDENT",
            studentCode = "ESC-552265",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "IR",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1058552265 | F.Nac: 17/04/2015",
            avatarEmoji = "🎓",
            phoneNumber = "3216031855"
        ),
        UserEntity(
            id = "par_1058552265_1",
            name = "Alba Miryam Tróchez Tombé",
            email = "miryamtrochez@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "AT",
            gradeSection = "Padre/Tutor de Ihara Daniella Riascos",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1058552265",
            bio = "Acudiente de Ihara Daniella Riascos Tróchez | Tel: 3216031855 | Cra 7ma # 16-20 1ro de Mayo",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3216031855"
        ),

        // ==========================================
        // 12. EMILY DANIELA CAICEDO NAVIA & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1061791400",
            name = "Emily Daniela Caicedo Navia",
            email = "emily.caicedo1061791400@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-791400",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "EC",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Sanitas | RH: B+ | TI: 1061791400 | F.Nac: 21/06/2014",
            avatarEmoji = "🎓",
            phoneNumber = "3217510339"
        ),

        // ==========================================
        // 13. VALERIA OROZCO GUTIÉRREZ & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1166464830",
            name = "Valeria Orozco Gutiérrez",
            email = "valeria.orozco1166464830@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-464830",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "VO",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1166464830 | F.Nac: 21/07/2013",
            avatarEmoji = "🎓",
            phoneNumber = "3233835048"
        ),
        UserEntity(
            id = "par_1166464830_1",
            name = "Sandra Magali Gutiérrez",
            email = "samidrobo@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "SG",
            gradeSection = "Padre/Tutor de Valeria Orozco",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1166464830",
            bio = "Acudiente de Valeria Orozco Gutiérrez | Tel: 3233835048 - 3104356309 | Vereda Alto Puelenje",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3233835048"
        ),

        // ==========================================
        // 14. DANNA SALOMÉ BOLAÑOS ORDÓÑEZ & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_129621021",
            name = "Danna Salomé Bolaños Ordóñez",
            email = "danna.bolanos129621021@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-621021",
            avatarColorHex = 0xFFE11D74,
            avatarInitials = "DB",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Sanitas | RH: A+ | TI: 129621021 | F.Nac: 23/09/2013",
            avatarEmoji = "🎓",
            phoneNumber = "3103571787"
        ),
        UserEntity(
            id = "rAEDBZFlFOYOV5e1B9UhG0ZI95F2",
            name = "Lina Ordoñez",
            email = "marceordo9521@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "LO",
            gradeSection = "Padre/Tutor de Danna Salomé Bolaños",
            streakDays = 1,
            xp = 350,
            level = 1,
            credits = 300,
            parentIncentiveCredits = 200,
            linkedStudentId = "std_129621021",
            bio = "Acudiente de Danna Salomé Bolaños Ordóñez | Tel: 3103571787 | Altos de Santa Inés Torre C Apto 803",
            avatarEmoji = "👨‍👩‍👧",
            photoUri = "https://lh3.googleusercontent.com/a/ACg8ocIYv9bKOzTGooFmmfVT_8hHyw0PpFoF5wyYQgIfe4dB2RgQjg=s96-c",
            phoneNumber = "3103571787"
        ),

        // ==========================================
        // 15. SAMUEL ECHAVARRIA PIZO & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1059246862",
            name = "Samuel Echavarria Pizo",
            email = "samuel.echavarria1059246862@escolaris.edu.co",
            role = "STUDENT",
            studentCode = "ESC-246862",
            avatarColorHex = 0xFF2563EB,
            avatarInitials = "SE",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Emssanar | RH: A+ | TI: 1059246862 | F.Nac: 22/06/2014",
            avatarEmoji = "🎓",
            phoneNumber = "3158908125"
        ),
        UserEntity(
            id = "45ffface-2f81-4fbd-978d-f7ba59663232",
            name = "María Mercedes Echavarria Pizo",
            email = "mekisa192227@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "ME",
            gradeSection = "Padre/Tutor de Samuel Echavarria",
            streakDays = 1,
            xp = 350,
            level = 1,
            credits = 300,
            parentIncentiveCredits = 200,
            linkedStudentId = "std_1059246862",
            bio = "Acudiente de Samuel Echavarria Pizo | Tel: 3235421090 | Calle 7A # 12-36",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3235421090"
        ),

        // ==========================================
        // 16. ANDRES FELIPE VACA BAHOS & ACUDIENTE
        // ==========================================
        UserEntity(
            id = "std_1166465828",
            name = "Andres Felipe Vaca Bahos",
            email = "anfevaba2015@gmail.com",
            role = "STUDENT",
            studentCode = "ESC-465828",
            avatarColorHex = 0xFF2563EB,
            avatarInitials = "AV",
            gradeSection = "7° Grado",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            bio = "Estudiante 7° Grado | EPS: Sanitas | RH: O+ | TI: 1166465828 | F.Nac: 20/01/2015",
            avatarEmoji = "🎓",
            phoneNumber = "3134068425"
        ),
        UserEntity(
            id = "par_1166465828_1",
            name = "Sandra Ximena Bahos Quinayás",
            email = "sandy.xime15@gmail.com",
            role = "PARENT",
            studentCode = "",
            avatarColorHex = 0xFF059669,
            avatarInitials = "SB",
            gradeSection = "Padre/Tutor de Andres Felipe Vaca",
            streakDays = 1,
            xp = 50,
            level = 1,
            credits = 100,
            parentIncentiveCredits = 100,
            linkedStudentId = "std_1166465828",
            bio = "Acudiente de Andres Felipe Vaca Bahos | Tel: 3134068425 | Cra 15 # 8N-188",
            avatarEmoji = "👨‍👩‍👧",
            phoneNumber = "3134068425"
        )
    )
}


