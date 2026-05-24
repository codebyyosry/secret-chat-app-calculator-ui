# Secret Chat Calculator 🤫📱

A stealth-focused messaging application disguised as a fully functional calculator. Designed with privacy in mind, this app allows users to access a hidden real-time chat room only by entering a specific secret PIN on the calculator interface.

Built natively for Android, this project demonstrates modern development practices, utilizing a strictly decoupled Clean Architecture approach alongside the MVP (Model-View-Presenter) pattern, all brought to life with Jetpack Compose.

## 🌟 Features

### 🧮 The Disguise (Calculator Mode)
* **Fully Functional:** Operates as a standard calculator for basic arithmetic (Addition, Subtraction, Multiplication, Division).
* **Stealth Access:** The calculator acts as a secure vault door. Entering a registered user PIN triggers seamless navigation to the hidden chat environment.
* **Dynamic Theming:** Adapts seamlessly to Light and Dark modes with a custom, high-contrast color palette.

### 💬 The Vault (Chat Mode)
* **Real-Time Messaging:** Powered by Firebase Firestore for instant message delivery and cross-device synchronization.
* **Live Typing Indicators:** Dynamic UI updates that show when the other user is typing, complete with custom bouncing-dot animations.
* **Smart Message Grouping:** WhatsApp-style date categorization (Today, Yesterday, Custom Dates) for a clean, organized chat history.
* **Message Management:** * Long-press context menus to select and delete specific messages.
  * Contextual Top App Bar with a "Clear Chat" option to wipe the entire history.
* **Keyboard Handling:** Automatically dismisses the software keyboard upon sending a message for a smooth user experience.

## 🛠️ Tech Stack & Architecture

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose, Compose Navigation
* **Architecture:** * Clean Architecture (Domain, Data, Presentation layers)
  * MVP (Model-View-Presenter) adapted for Compose using Unidirectional Data Flow (UDF)
* **Concurrency:** Kotlin Coroutines, StateFlow, SharedFlow
* **Backend:** Firebase Firestore (Real-time Snapshot Listeners)

## 🏗️ Architecture Overview

The app is strictly separated into three layers to ensure testability and scalability:
1. **Domain Layer:** Contains core business models (`Message`) and the `ChatRepository` interface. Completely independent of Android framework and Firebase.
2. **Data Layer:** Implements the repository interface using `FirebaseFirestore`, handling real-time snapshots, message caching, and typing status sync.
3. **Presentation Layer (UI):** Uses `ViewModel` as the Presenter, exposing a reactive `StateFlow` to the Jetpack Compose UI. UI events are handled via `SharedFlow`.

## 🚀 Setup & Installation

To run this project locally, you will need to connect it to your own Firebase project.

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/yourusername/Secret-Chat-Calculator.git](https://github.com/yourusername/Secret-Chat-Calculator.git)