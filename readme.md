# Movie RMI System

A distributed system built with Java RMI (Remote Method Invocation) that provides a movie service to clients over a network. The client invokes methods on the server remotely — through a **Stub** and **Skeleton** — as if they were local calls, while all transport happens transparently over TCP.

---

## What is RMI?

RMI (Remote Method Invocation) is a Java feature that lets one program call a method on an object that lives in a **different JVM process** — possibly on a different machine entirely.

From the client's perspective it looks like a normal method call:
```java
movieService.getAllMovies(); // looks local, but actually runs on the server JVM
```

Under the hood RMI serializes the call, sends it over a TCP socket, the server executes it, and the result comes back. The network part is completely invisible to the programmer — that is exactly why it is called **Remote Method Invocation**.

---

## System Architecture

```
CLIENT MACHINE                          SERVER MACHINE
──────────────                          ──────────────
Client.java                             Server.java
     │                                       │
     │  1. registry.lookup("AuthService")    │  createRegistry(1099)
     │─────────────────────────────────────► │  rebind("AuthService", authService)
     │◄──────────── returns Stub ────────────│  rebind("MovieService", movieService)
     │                                       │
     │  2. authService.login(user, token)    │
     │  [Stub marshals → bytes → TCP] ─────► │  [Skeleton unmarshals → AuthServiceImpl.login()]
     │◄───────────── result ─────────────────│
     │                                       │
     │  3. movieService.getAllMovies(token)   │
     │  [Stub marshals → bytes → TCP] ─────► │  [Skeleton unmarshals → MovieServiceImpl.getAllMovies()]
     │◄───────────── result ─────────────────│
```

---

## System Components

This system has **9 components** across 4 layers:

| Layer | Component | File | Role |
|---|---|---|---|
| Client layer | Client | `Client.java` | Terminal UI, initiates all remote calls, stores session token |
| Client layer | Stub | auto-generated | Marshals method calls into bytes, sends over TCP |
| Interface layer | AuthService | `AuthService.java` | Remote interface defining auth operations |
| Interface layer | MovieService | `MovieService.java` | Remote interface defining movie operations |
| Middleware layer | RMI Registry | inside `Server.java` | Name lookup table on port 1099 |
| Middleware layer | Skeleton | inside `UnicastRemoteObject` | Receives bytes, unmarshals, calls real implementation |
| Server layer | Server | `Server.java` | Starts registry, binds both services |
| Server layer | AuthServiceImpl | `AuthServiceImpl.java` | Actual auth logic — register, login, token management |
| Server layer | MovieServiceImpl | `MovieServiceImpl.java` | Actual movie logic — list, search, details |

---

## Stub and Skeleton

The two most important hidden components in RMI:

### Stub (Client side)
When the client calls `registry.lookup("MovieService")`, Java automatically creates a **Stub** — a proxy object that looks exactly like the real `MovieService` interface. When the client calls any method on it, the Stub:
1. **Marshals** (serializes) the method name + arguments into bytes
2. **Sends** those bytes over TCP to the server
3. **Waits** for the result bytes to come back
4. **Unmarshals** the result and returns it to the client

The client never knows a network call happened.

### Skeleton (Server side)
The Skeleton lives inside `UnicastRemoteObject`, which both `AuthServiceImpl` and `MovieServiceImpl` extend. It:
1. **Listens** for incoming bytes on the TCP socket
2. **Unmarshals** the bytes back into a real Java method call
3. **Invokes** the actual method on the implementation class
4. **Marshals** the result and sends it back

Java handles the Skeleton automatically — no skeleton code is written manually.

```
Client.java
    │  calls method normally
    ▼
  Stub  ──[marshal]──► bytes ──[TCP :1099]──► bytes ──[unmarshal]──► Skeleton
                                                                           │
                                                                           ▼
                                                              AuthServiceImpl / MovieServiceImpl
                                                                           │
  Client gets result ◄──[unmarshal]──► bytes ◄──[TCP]──► bytes ◄──[marshal]──┘
```

---

## Services

### AuthService — `AuthService.java` / `AuthServiceImpl.java`

Handles user registration, login, logout, and token validation.

| Method | Argument(s) | Returns | Description |
|---|---|---|---|
| `register(username, password)` | username, plaintext password | success/fail message | Creates a new user account stored in HashMap |
| `login(username, password)` | username, AES-encrypted password | token string or error | Verifies credentials, generates UUID session token on success |
| `logout(token)` | session token | success/fail message | Removes token from server — session ended |
| `validateToken(token)` | session token | true / false | Checks if token exists in server memory |

### MovieService — `MovieService.java` / `MovieServiceImpl.java`

Handles movie data queries. Every method requires a valid token — unauthorized requests are rejected.

| Method | Argument(s) | Returns | Description |
|---|---|---|---|
| `getAllMovies(token)` | session token | List of movie titles | Returns all movies dynamically from HashMap |
| `searchMovie(token, title)` | session token, search string | found/not found message | Partial title match search |
| `getMovieDetails(token, title)` | session token, search string | full movie details | Returns year, director, genre, rating |

---

## Token-Based Authentication

After a successful login the server generates a **UUID session token** and returns it to the client. Every subsequent movie request must carry this token so the server can verify the caller's identity.

```
register()   → no token        (just store user credentials)
login() ❌   → no token        (wrong credentials)
login() ✅   → TOKEN GENERATED (UUID random string)
getAllMovies(token) ✅ → authorized, returns movies
getAllMovies(fake)  ❌ → "Unauthorized access!"
logout(token) → token destroyed (session ended)
```

**Why UUID?**
- Mathematically impossible to generate the same UUID twice
- Cannot be guessed by an attacker
- Each login produces a completely new token — even same username and password

**Token stored on server in HashMap:**
```
tokens = {
    "13ce69f3-f08e-4498-8c7d-69d07e04b81b" → "lusa",
    "9a4f82c1-b3d7-4521-a1e8-32f09c17d4e2" → "john"
}
```

This means the server always knows **which user** owns which token and can identify multiple clients simultaneously.

---

## Server-Side Logging

All method executions on the server are logged using the format:

```
[NODE-ID][SERVICE-NAME] method() → details → result
```

Example output when a client interacts with the system:

```
[NODE-001][AUTH-SERVICE] register() → user: lusa → stored successfully!
[NODE-001][AUTH-SERVICE] login() → user: lusa → match! → generated token: 13ce69f3-f08e-4498-8c7d-69d07e04b81b
[NODE-001][AUTH-SERVICE] validateToken() → token: 13ce69f3... → user: lusa → valid ✅
[NODE-001][MOVIE-SERVICE] getAllMovies() → token: 13ce69f3... → returning 5 movies ✅
[NODE-001][MOVIE-SERVICE] searchMovie() → token: 13ce69f3... → searching: Inception → found: Inception ✅
[NODE-001][MOVIE-SERVICE] searchMovie() → token: 13ce69f3... → searching: Batman → not found! ❌
[NODE-001][AUTH-SERVICE] logout() → user: lusa → token removed → session ended!
```

**Why NODE-001?**
In a real distributed system multiple server nodes run simultaneously (NODE-001, NODE-002, NODE-003...). Each node must identify itself in logs so operators know which machine handled which request. Even with one server, using NODE-001 reflects proper distributed system conventions and makes the system ready to scale.

---

## What the Skeleton Catches

When the client sends a request, the Skeleton receives a **structured package** — not just a plain text message. It contains:

| Field | Example |
|---|---|
| Object ID | `"MovieService"` — which service to call |
| Method ID | `getAllMovies` — which method to invoke |
| Arguments | `"13ce69f3..."` — the token string |
| Return type | `List<String>` — what to serialize back |

This is why both client and server must share the same interface files (`AuthService.java`, `MovieService.java`) — they must agree on method signatures, argument types, and return types beforehand.

---

## Password Encryption

The password is **AES-128 encrypted (ECB mode)** on the client before being sent over RMI:

```
Client                              Server
──────                              ──────
plaintext password                  receives encrypted bytes
    │                                    │
    ▼                                    ▼
AES encrypt (key: 1234567890123456) AES decrypt (same key)
    │                                    │
    ▼                                    ▼
encrypted bytes ──[TCP]──────────► compare with stored password
```

This protects the password on the wire — the server never receives plaintext.

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
Expected output:
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
Expected output:
```
Welcome to Movie RMI System!
================================

1. Register
2. Login
3. Exit
Choose:
```

> **Important:** Always start the server before the client. The client cannot connect if the RMI registry does not exist yet.

---

## Running on Two Machines

The project currently runs on one machine using `localhost`. To run across two real machines on the same network:

1. Find the server machine IP — run `ipconfig` (Windows) or `ifconfig` (Mac/Linux)
2. Change one line in `Client.java`:
```java
// Before
Registry registry = LocateRegistry.getRegistry("localhost", 1099);

// After — use the server machine's actual IP
Registry registry = LocateRegistry.getRegistry("192.168.1.10", 1099);
```
3. Recompile and run — everything else stays exactly the same
4. Make sure the server machine's firewall allows port `1099`

---

## Key Concepts Summary

| Concept | Where it appears in this project |
|---|---|
| Remote Interface | `AuthService.java`, `MovieService.java` |
| Remote Implementation | `AuthServiceImpl.java`, `MovieServiceImpl.java` |
| RMI Registry | `Server.java` — `createRegistry(1099)` |
| Service Binding | `Server.java` — `registry.rebind(...)` |
| Service Lookup | `Client.java` — `registry.lookup(...)` |
| Stub | Auto-generated by Java RMI on `registry.lookup()` |
| Skeleton | Built into `UnicastRemoteObject` extended by both impl classes |
| Marshal / Unmarshal | Handled automatically by Java RMI |
| Node ID | `NODE-001` constant in both impl files |
| Session Token | UUID generated in `AuthServiceImpl.login()` |
| AES Encryption | `Client.java` encrypts, `AuthServiceImpl.java` decrypts |