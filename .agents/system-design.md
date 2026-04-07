# 1) Core Features (keep it tight)

Don’t overbuild v1.

**Must-have:**

* Log water intake (quick add buttons like 100ml / 250ml / custom)
* Daily goal tracking
* Progress visualization
* Reminder notifications
* History (daily logs)

**Nice-to-have later:**

* Smart reminders (based on habits)
* Health integrations (Google Fit)
* Widgets
* Dark mode / themes

---

# 2) High-Level Architecture

Use **MVVM + Clean Architecture**. It’s the sweet spot for Android.

```
UI (Activity / Fragment / Compose)
   ↓
ViewModel
   ↓
Use Cases (optional but good practice)
   ↓
Repository
   ↓
Local DB (Room)
   ↓
System Services (AlarmManager / WorkManager)
```

---

# 3) Tech Stack (Kotlin-first)

* Language: **Kotlin**
* UI: **Jetpack Compose** (recommended) or XML
* Architecture: **MVVM**
* DB: **Room**
* Dependency Injection: **Hilt**
* Background tasks: **WorkManager**
* Notifications: Android Notification API
* DataStore: for lightweight settings (goal, units)

---

# 4) Data Model

Keep it simple.

### WaterEntry
Single log entry per drink.

Fields:
* `id` (Long, PK, auto)
* `timestamp` (Instant stored as epoch millis)
* `amountMl` (Int)
* `source` (String, optional: `quick_button`, `custom`, `reminder`)

Kotlin:
```kotlin
@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val amountMl: Int,
    val source: String? = null,
)
```


### DailySummary (optional, or compute on the fly)
Derived view, not stored.

Compute by date (local timezone):
* `date` (LocalDate)
* `totalMl` (Int)
* `goalMl` (Int)
* `progressPct` (Float 0..1)

If you later need cached summaries, add a `daily_summaries` table and recompute on insert/update.


---

# 5) Database Layer (Room)

### DAO
Use Flow for reactive UI.

Queries:
* Insert water entry
* Delete water entry
* Get entries for day
* Get total for day
* Get entries for range (history)

Kotlin:
```kotlin
@Dao
interface WaterEntryDao {
    @Insert
    suspend fun insert(entry: WaterEntry)

    @Delete
    suspend fun delete(entry: WaterEntry)

    @Query("SELECT * FROM water_entries WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    fun entriesForDay(start: Long, end: Long): Flow<List<WaterEntry>>

    @Query("SELECT SUM(amountMl) FROM water_entries WHERE timestamp BETWEEN :start AND :end")
    fun totalForDay(start: Long, end: Long): Flow<Int?>

    @Query("SELECT * FROM water_entries WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun entriesForRange(start: Long, end: Long): Flow<List<WaterEntry>>
}
```



---

# 6) Repository Layer
Expose domain-friendly APIs and hide time boundaries.

Interface:
```kotlin
interface HydrationRepository {
    suspend fun addEntry(amountMl: Int, source: String? = null)
    suspend fun deleteEntry(entry: WaterEntry)
    fun dayEntries(date: LocalDate): Flow<List<WaterEntry>>
    fun dayTotal(date: LocalDate): Flow<Int>
    fun history(start: LocalDate, end: LocalDate): Flow<List<WaterEntry>>
}
```

Implementation notes:
* Convert `LocalDate` to start/end epoch millis using current timezone.
* Map null totals to 0.


---

# 7) ViewModel
One ViewModel per screen.

`HomeViewModel`:
* `todayTotal: StateFlow<Int>`
* `goalMl: StateFlow<Int>`
* `progress: StateFlow<Float>`
* `todayEntries: StateFlow<List<WaterEntry>>`
* `addQuick(amountMl)`
* `addCustom(amountMl)`
* `delete(entry)`

`HistoryViewModel`:
* `rangeEntries`
* `selectedDate`

State:
```kotlin
data class HomeUiState(
    val totalMl: Int = 0,
    val goalMl: Int = 2000,
    val progress: Float = 0f,
    val entries: List<WaterEntry> = emptyList(),
)
```



---

# 8) UI (Jetpack Compose)
Screens:
* Home (progress ring + quick add + recent entries)
* History (calendar or list grouped by day)
* Settings (goal, reminder interval, units)

Home layout:
* Top: daily goal and progress ring
* Middle: quick add buttons (100/250/500) + custom input
* Bottom: today list (time + amount)

Compose sketch:
```kotlin
@Composable
fun HomeScreen(state: HomeUiState, onAdd: (Int) -> Unit, onDelete: (WaterEntry) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ProgressRing(total = state.totalMl, goal = state.goalMl)
        Spacer(Modifier.height(16.dp))
        QuickAddRow(onAdd = onAdd)
        Spacer(Modifier.height(16.dp))
        EntryList(entries = state.entries, onDelete = onDelete)
    }
}
```



---

# 9) Reminder System

Use **WorkManager**, not AlarmManager (more reliable).

### Periodic Reminder
Schedule a periodic work request with flex:
* Interval: user setting (ex: 2 hours)
* Flex: 15 minutes
* Constraint: battery not low (optional)

```kotlin
val request = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
    repeatInterval = intervalHours, TimeUnit.HOURS,
    flexTimeInterval = 15, TimeUnit.MINUTES
).build()
```


### Worker
* Check quiet hours (optional)
* Post notification if within allowed window
* Use `NotificationManagerCompat`

```kotlin
class HydrationReminderWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        NotificationHelper.showHydrationReminder(applicationContext)
        return Result.success()
    }
}
```


---

# 10) Notifications

* Channel: "hydration_reminder"
* Simple message: “Time to drink water”

---

# 11) Settings (DataStore)

Store:

* Daily goal (ml)
* Reminder interval
* Units (ml / oz)

---

# 12) Scaling Thoughts (if you go bigger)

Only needed if app grows:

* Cloud sync → Firebase
* User accounts
* Analytics
* AI hydration suggestions (based on weather/activity)

---

# 13) Common Mistakes (avoid these)

* Overengineering (no need for microservices… it’s a water app)
* Using LiveData instead of Flow (Flow is better now)
* Storing daily totals instead of computing → causes bugs
* Using exact alarms → battery issues + restrictions

---

# Bottom Line

Build it like this:

* **MVVM + Room + WorkManager + Compose**
* Keep logic simple
* Focus on UX (fast logging + clean UI)
* Notifications are the real value driver
