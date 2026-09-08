# Sorting Scope

An Android-based mobile application that makes learning sorting algorithms simple, visual, and interactive through step-by-step animations, algorithm comparison, quizzes, and progress tracking.

## 📱 About the Project

Sorting algorithms are a fundamental concept in computer science, but many students find them difficult to understand through traditional methods such as textbooks, code, and static diagrams. Sorting Scope solves this by using real-time animations, color coding, and audio cues to help students visually understand how sorting algorithms work.


## ✨ Features

- **Sorting Visualizer** — Animates 7 sorting algorithms (Bubble, Insertion, Selection, Merge, Quick, Heap, Shell Sort) step-by-step using custom bar animations.
- **Custom Input** — Users can enter their own numbers to sort, or use default values.
- **Algorithm Comparison** — Compare two sorting algorithms side-by-side based on comparisons, swaps, and execution time.
- **Quiz Module** — Multiple-choice questions to test understanding of sorting algorithms, with instant feedback.
- **Progress Tracking** — Tracks quiz scores over time and displays them as a line graph.
- **User Profile** — Simple one-time profile setup (name and ID) saved locally.

## 🛠️ Built With

- **Java** — Core programming language
- **XML** — UI layout design
- **Android Studio** — Development IDE
- **SharedPreferences** — Local data storage (profile, quiz scores, history)
- **JSON** — Question bank storage, parsed using `org.json`
- **MPAndroidChart** — Library used for the progress trend line chart

## 🚀 How to Run

1. Clone this repository:
   git clone https://github.com/MeerabAfzal-eng/Meerabapp
2. Open the project in **Android Studio**.
3. Let Gradle sync finish.
4. Connect an Android device or start an emulator (minimum SDK: Android 8.1 / API 26).
5. Click **Run ▶** to build and launch the app.

## 📂 Project Structure

```
app/src/main/java/com/example/meerabapp/
├── SplashActivity.java
├── WelcomeActivity.java
├── ProfileActivity.java
├── MainActivity.java
├── VisualizationActivity.java
├── ComparisonScreen.java
├── activity_quiz.java
├── activity_progress.java
```

## 👩‍💻 Team

| Name | Roll No |
|---|---|
| Meerab Afzal | 085329 |
| Maham Izhar Khan | 085309 |

## 📄 License

This project was developed for academic purposes as part of a Final Year Project (2026).
