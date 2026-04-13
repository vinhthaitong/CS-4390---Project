# CSCE 4390 Network Math Application

## Overview
This project implements a TCP-based network application with:
- One centralized math server
- Two or more concurrent clients
The server accepts math expressions from clients, evaluates them, returns results, and logs client session activity.

## Current Features
- Parser:
  - Parse a math expression string into a numeric result.
  - Support +, -, *, /, unary signs, decimals, and parentheses.
  - Respect operator precedence with expression/term/factor parsing.
  - Throw DIVIDE_BY_ZERO or Invalid expression when parsing fails.
- Client: 
  - Send the server request, wait until ACK. 
  - User input their name.
  - Send basic math calculation requests (example: 1+2*2/4 *ENTER*).
  - Type "CLOSE" or "close" to close connection. 
- Server:
  - Accept multiple clients at the same time (one thread per client).
  - Server receives JOIN, EXPR, and CLOSE messages from each client.
  - Send ACK/RESULT/ERROR/BYE responses back to the client.
  - Keeping track of connection time, disconnection time, and duration in seconds.

## Project Files
- `TCPServer.java`: server socket, client session handler, protocol handling, logging
- `TCPClient.java`: client socket and interactive request loop
- `Parser.java`: expression parser and evaluator (`+`, `-`, `*`, `/`, unary signs, parentheses, decimals)
- `Makefile`: build/run shortcuts

## Protocol Design
All messages are line-based text messages.

Client to Server:
- `JOIN: <name>`
- `EXPR: <expression>`
- `CLOSE`

Server to Client:
- `ACK: <name>`
- `RESULT = <value>`
- `ERROR: <message>`
- `BYE`

## Parameters Needed During Execution
- Server is listening on port: `6789` 
- Client server IP: `127.0.0.1` (localhost)
- Client sends request to server port: `6789` (hardcoded)
- Client name: prompted at runtime

## Build and Run
From the project directory:

```bash
make
```

Start server (Terminal 1):

```bash
make server
```

Start first client (Terminal 2):

```bash
make client
```

Start second client (Terminal 3):

```bash
make client
```

Clean compiled classes:

```bash
make clean
```

## Client Usage
After connection:
1. Enter your name.
2. Enter expressions one by one (example: `1+2*2/4`).
3. Type `CLOSE` to terminate the session.

## Expression Support
Supported operators:
- `+`, `-`, `*`, `/`
- unary `+` and unary `-`
- parentheses
- decimal numbers

Examples:
- `2 + 3 * 4`
- `(10 - 2) / 4`
- `-(3 + 2) * 5`
