# Movie RMI System

A beginner distributed system project built with Java RMI (Remote Method Invocation).  
The idea is simple: a **client** can call methods on a **server** as if they were local calls — but the communication actually happens over a network socket between two separate JVM processes.

---

## What is RMI?

RMI (Remote Method Invocation) is a Java feature that lets one program call a method on an object that lives in a **different JVM process** — possibly on a different machine entirely.

From the client's perspective, it looks like a normal method call:
```java
movieService.getAllMovies(); // looks local, but actually runs on the server
```

Under the hood, RMI serializes the call, sends it over a TCP socket, the server executes it, and the result comes back. The network part is invisible to the programmer.

---

## Project Structure

```
src/
├── MovieService.java        # Remote interface: defines movie operations
├── MovieServiceImpl.java    # Server-side implementation of MovieService
├── AuthService.java         # Remote interface: defines auth operations
├── AuthServiceImpl.java     # Server-side implementation of AuthService
├── Server.java              # Starts the RMI registry and binds both services
└── Client.java              # Connects to the server and provides the user menu
```

---

## Architecture

Tested on a single laptop using two terminals. Each terminal runs a separate JVM process — they do not share memory and communicate purely through a TCP socket on `localhost:1099`.

```
Your Laptop
┌──────────────────────────────────────────────┐
│                                              │
│  Terminal 1 (Server)    Terminal 2 (Client)  │
│  ┌──────────────────┐   ┌──────────────────┐ │
│  │ Server.java      │   │ Client.java      │ │
│  │                  │◄─►│                  │ │
│  │ AuthServiceImpl  │   │ authService      │ │
│  │ MovieServiceImpl │   │ movieService     │ │
│  │                  │   │                  │ │
│  │ Registry :1099   │   │ localhost:1099   │ │
│  └──────────────────┘   └──────────────────┘ │
│                                              │
└──────────────────────────────────────────────┘
```

Even though both processes run on the same machine, the RMI call still goes through the full cycle: **serialize → send over socket → execute on server → return result**. This is real distributed system behavior, not a simulation.

---

## System Components

This system has **4 main components:**

| Component | File | Role |
|-----------|------|------|
| Client | `Client.java` | User terminal UI, initiates all remote calls |
| RMI Registry | inside `Server.java` | Naming service on port 1099, binds and serves lookups |
| AuthService | `AuthServiceImpl.java` | Handles register and login remotely |
| MovieService | `MovieServiceImpl.java` | Handles movie queries remotely |

---

## Stub and Skeleton

RMI communication works through two hidden layers:

**Stub (Client side)**  
When the client calls `registry.lookup("AuthService")`, Java automatically creates a Stub — a proxy object that looks exactly like the real `AuthService`. When the client calls a method on it, the Stub serializes the call and sends it over TCP to the server. The client never knows a network call happened.

**Skeleton (Server side)**  
The Skeleton lives inside `UnicastRemoteObject`, which both `AuthServiceImpl` and `MovieServiceImpl` extend. It listens for incoming serialized calls, unpacks them, and forwards them to the real method implementation. Java handles this automatically — no skeleton code is written manually.

```
Client.java
    │
    ▼
  Stub  ──── serializes call ────► Skeleton
                  TCP :1099              │
                                         ▼
                               AuthServiceImpl / MovieServiceImpl
```

---

## Services

### AuthService
Handles user registration and login.

| Method | Description |
|--------|-------------|
| `register(username, password)` | Creates a new user account |
| `login(username, password)` | Authenticates the user. Password is AES-encrypted before sending. |

### MovieService
Handles movie data queries.

| Method | Description |
|--------|-------------|
| `getAllMovies()` | Returns a list of all movie titles |
| `searchMovie(title)` | Searches for a movie by partial title match |
| `getMovieDetails(title)` | Returns full details (year, director, genre, rating) |

---

## How Communication Works

The communication in this system is **entirely driven by the Client**. Every user action becomes a remote method call that travels over RMI to the Server.

**Server terminal** — stays silent after startup. This is correct and expected behavior. The Server is simply hosting the services; Java RMI handles all transport invisibly behind the scenes.

**Client terminal** — shows all communication clearly:

```
[SEND] Register request | username: lusa
[RECV] Registration successful!

[SEND] Login request | username: lusa
[RECV] Login successful! Welcome lusa

[SEND] Get all movies request
[RECV] Returning 5 movies

[SEND] Search request | title: "The Godfather"
[RECV] Found: The Godfather
```

This design honestly reflects how RMI works — the programmer sees the distributed calls from the Client's perspective, while the Server remains passive and lets Java handle the networking.
Client.java calls authService.register()
    → Stub serializes the call
        → travels over TCP :1099
            → Skeleton unpacks it
                → AuthServiceImpl.register() executes
                    → result marshals back
                        → Stub returns it to Client.java

---

## How to Run

### Step 1 — Compile all files
```bash
cd src
javac *.java
```

### Step 2 — Start the Server (Terminal 1)
```bash
java Server
```
You should see:
```
Server is running...
AuthService registered
MovieService registered
Movie RMI System ready
```

### Step 3 — Start the Client (Terminal 2)
```bash
java Client
```
You should see:
```
Welcome to Movie RMI System!
================================

1. Register
2. Login
3. Exit
```

> **Important:** Always start the server before the client. The client cannot connect if the registry does not exist yet.

---

## Running on Two Laptops (Future)

The project currently runs on one laptop using `localhost`. To test across two real laptops on the same WiFi:

1. Find the server laptop's local IP — run `ipconfig` (Windows) or `ifconfig` (Mac/Linux)
2. Change one line in `Client.java`:
   ```java
   // Before
   Registry registry = LocateRegistry.getRegistry("localhost", 1099);

   // After — use the server laptop's actual IP
   Registry registry = LocateRegistry.getRegistry("192.168.1.10", 1099);
   ```
3. Recompile and run — everything else stays exactly the same.
4. Make sure the server laptop's firewall allows port `1099`.

---

## Security Notes

This project uses **AES-128 encryption (ECB mode)** to encrypt the password before sending it during login. This was added as a learning exercise.

For a real production system:
- Use **TLS** for the transport layer instead of manual AES
- Use **AES/GCM** instead of AES/ECB (ECB is weak — identical inputs produce identical outputs)
- **Hash passwords** with bcrypt or SHA-256 before storing them, never store plaintext
- Return a **session token** from login so the server can verify identity on each request

---

## Key Concepts

| Concept | Where it appears |
|---------|-----------------|
| Remote Interface | `MovieService.java`, `AuthService.java` |
| Remote Implementation | `MovieServiceImpl.java`, `AuthServiceImpl.java` |
| RMI Registry | `Server.java` — `createRegistry(1099)` |
| Service Binding | `Server.java` — `registry.rebind(...)` |
| Service Lookup | `Client.java` — `registry.lookup(...)` |
| Stub | Auto-generated by Java RMI on `registry.lookup()` |
| Skeleton | Built into `UnicastRemoteObject` (extended by both impl classes) |
| Network-transparent calls | `Client.java` — calling remote methods like local ones |
| AES Encryption | `Client.java` + `AuthServiceImpl.java` |