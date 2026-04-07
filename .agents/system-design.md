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


### DailySummary (optional, or compute on the fly)


---

# 5) Database Layer (Room)

### DAO



---

# 6) Repository Layer


---

# 7) ViewModel



---

# 8) UI (Jetpack Compose)



---

# 9) Reminder System

Use **WorkManager**, not AlarmManager (more reliable).

### Periodic Reminder


### Worker


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
