const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate() {
  return new Date(Date.now() - 2000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot list chat_directory, chat_requests, chats, or call_sessions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("chat_directory").where("isPublic", "==", true).get());
  await assertFails(unauthDb.collection("chat_requests").where("participantIds", "array-contains", ALICE_UID).get());
  await assertFails(unauthDb.collection("chats").where("participantIds", "array-contains", ALICE_UID).get());
  await assertFails(unauthDb.collection("call_sessions").where("participantIds", "array-contains", ALICE_UID).get());
});

test("Chat directory: authenticated user can publish own non-PII card and search all public users, cannot spoof another user", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("chat_directory").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Dev",
      displayNameLower: "alice dev",
      photoUrl: "preset:python",
      levelNumber: 3,
      levelTitle: "Aprendiz",
      totalScore: 800,
      isPublic: true,
      updatedAt: pastDate(),
    })
  );

  // Cross-user spoof fails
  await assertFails(
    bobDb.collection("chat_directory").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Spoofed",
      displayNameLower: "spoofed",
      isPublic: true,
      updatedAt: pastDate(),
    })
  );

  // Ghost field fails
  await assertFails(
    bobDb.collection("chat_directory").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob Dev",
      displayNameLower: "bob dev",
      isPublic: true,
      isAdmin: true,
      updatedAt: pastDate(),
    })
  );

  // Bob can query public directory with isPublic == true
  await assertSucceeds(
    bobDb.collection("chat_directory").where("isPublic", "==", true).get()
  );
  // Unfiltered directory list fails
  await assertFails(bobDb.collection("chat_directory").get());
});

test("Chat requests: Alice sends pending request to Bob, Charlie cannot read/accept, Bob accepts", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const reqId = `${ALICE_UID}_${BOB_UID}`;
  const chatId = `${ALICE_UID}_${BOB_UID}`;
  const ts = pastDate();

  // Alice cannot pre-accept her own request on creation
  await assertFails(
    aliceDb.collection("chat_requests").doc(reqId).set({
      senderId: ALICE_UID,
      senderName: "Alice",
      recipientId: BOB_UID,
      recipientName: "Bob",
      participantIds: [ALICE_UID, BOB_UID],
      status: "accepted",
      chatId,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Alice creates valid pending request
  await assertSucceeds(
    aliceDb.collection("chat_requests").doc(reqId).set({
      senderId: ALICE_UID,
      senderName: "Alice",
      recipientId: BOB_UID,
      recipientName: "Bob",
      participantIds: [ALICE_UID, BOB_UID],
      status: "pending",
      introMessage: "Oi Bob, bora estudar SQL?",
      chatId,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Charlie cannot read or update Alice<->Bob request
  await assertFails(charlieDb.collection("chat_requests").doc(reqId).get());
  await assertFails(
    charlieDb.collection("chat_requests").doc(reqId).update({
      status: "accepted",
      updatedAt: pastDate(),
    })
  );

  // Alice cannot self-accept her own request
  await assertFails(
    aliceDb.collection("chat_requests").doc(reqId).update({
      status: "accepted",
      updatedAt: pastDate(),
    })
  );

  // Bob accepts the request
  await assertSucceeds(
    bobDb.collection("chat_requests").doc(reqId).update({
      status: "accepted",
      recipientName: "Bob",
      recipientPhotoUrl: "",
      updatedAt: pastDate(),
    })
  );

  // Participants can query their requests using participantIds filter
  await assertSucceeds(
    bobDb.collection("chat_requests").where("participantIds", "array-contains", BOB_UID).get()
  );
});

test("Chats, messages (text/image/video/file), chunks, and message/conversation deletion enforce participant isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const chatId = `${ALICE_UID}_${BOB_UID}`;
  const ts = pastDate();

  await assertSucceeds(
    bobDb.collection("chats").doc(chatId).set({
      participantIds: [ALICE_UID, BOB_UID],
      participantNames: { [ALICE_UID]: "Alice", [BOB_UID]: "Bob" },
      participantPhotos: { [ALICE_UID]: "", [BOB_UID]: "" },
      lastMessageText: "Solicitação aceita!",
      lastMessageType: "text",
      lastSenderId: BOB_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Charlie cannot read or send messages in Alice<->Bob chat
  await assertFails(charlieDb.collection("chats").doc(chatId).get());
  await assertFails(
    charlieDb.collection("chats").doc(chatId).collection("messages").doc("msg_bad").set({
      chatId,
      senderId: CHARLIE_UID,
      senderName: "Charlie",
      participantIds: [ALICE_UID, BOB_UID],
      messageType: "text",
      text: "Intruder",
      createdAt: ts,
    })
  );

  // Alice sends a video message with 1 chunk
  await assertSucceeds(
    aliceDb.collection("chats").doc(chatId).collection("messages").doc("msg_1").set({
      chatId,
      senderId: ALICE_UID,
      senderName: "Alice",
      participantIds: [ALICE_UID, BOB_UID],
      messageType: "video",
      text: "Resumo SQL Join",
      mediaName: "sql_join.mp4",
      mediaMimeType: "video/mp4",
      mediaSizeBytes: 102400,
      chunkCount: 1,
      createdAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("chats").doc(chatId).collection("messages").doc("msg_1").collection("chunks").doc("chunk_0").set({
      senderId: ALICE_UID,
      participantIds: [ALICE_UID, BOB_UID],
      chunkIndex: 0,
      dataBase64: "AAAA",
      createdAt: ts,
    })
  );

  // Bob reads messages and chunks with participantIds filter
  await assertSucceeds(
    bobDb.collection("chats").doc(chatId).collection("messages").where("participantIds", "array-contains", BOB_UID).get()
  );
  await assertSucceeds(
    bobDb.collection("chats").doc(chatId).collection("messages").doc("msg_1").collection("chunks").where("participantIds", "array-contains", BOB_UID).get()
  );

  // Alice deletes her message & chunk, then conversation
  await assertSucceeds(
    aliceDb.collection("chats").doc(chatId).collection("messages").doc("msg_1").collection("chunks").doc("chunk_0").delete()
  );
  await assertSucceeds(
    aliceDb.collection("chats").doc(chatId).collection("messages").doc("msg_1").delete()
  );
  await assertSucceeds(
    aliceDb.collection("chats").doc(chatId).delete()
  );
});

test("Call sessions & ICE candidates: Alice calls Bob, Bob answers, Charlie is blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const callId = "call_1";
  const chatId = `${ALICE_UID}_${BOB_UID}`;
  const ts = pastDate();

  await assertSucceeds(
    aliceDb.collection("call_sessions").doc(callId).set({
      chatId,
      callerId: ALICE_UID,
      callerName: "Alice",
      callerPhotoUrl: "",
      calleeId: BOB_UID,
      calleeName: "Bob",
      participantIds: [ALICE_UID, BOB_UID],
      callType: "video",
      status: "ringing",
      offerSdp: "v=0...",
      answerSdp: "",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Charlie cannot read or answer
  await assertFails(charlieDb.collection("call_sessions").doc(callId).get());
  await assertFails(
    charlieDb.collection("call_sessions").doc(callId).update({
      status: "accepted",
      answerSdp: "v=0...",
      updatedAt: pastDate(),
    })
  );

  // Bob accepts with answerSdp
  await assertSucceeds(
    bobDb.collection("call_sessions").doc(callId).update({
      status: "accepted",
      answerSdp: "v=0-answer...",
      updatedAt: pastDate(),
    })
  );

  // ICE candidate exchange
  await assertSucceeds(
    aliceDb.collection("call_sessions").doc(callId).collection("candidates").doc("cand_1").set({
      senderId: ALICE_UID,
      participantIds: [ALICE_UID, BOB_UID],
      sdpMid: "0",
      sdpMLineIndex: 0,
      candidate: "candidate:1 1 udp 2122260223 192.168.1.2 54321 typ host",
      createdAt: pastDate(),
    })
  );

  await assertSucceeds(
    bobDb.collection("call_sessions").doc(callId).collection("candidates").where("participantIds", "array-contains", BOB_UID).get()
  );
});

test("Live study rooms: host creates live room, participant joins/chats, signals isolated by peerPair, only host can end", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const roomId = "live_sql_1";
  const ts = pastDate();

  // Unauthenticated cannot list live rooms
  await assertFails(unauthDb.collection("live_rooms").where("status", "==", "live").get());

  // Alice starts a live study room
  await assertSucceeds(
    aliceDb.collection("live_rooms").doc(roomId).set({
      hostId: ALICE_UID,
      hostName: "Alice",
      hostPhotoUrl: "",
      title: "Live de SQL Joins e Subqueries",
      topicTechId: "sql",
      topicLabel: "SQL",
      description: "Vamos tirar dúvidas juntos!",
      liveMode: "video",
      status: "live",
      participantIds: [ALICE_UID],
      participantNames: { [ALICE_UID]: "Alice" },
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Bob lists active live rooms and joins participantIds
  await assertSucceeds(bobDb.collection("live_rooms").where("status", "==", "live").get());
  await assertSucceeds(
    bobDb.collection("live_rooms").doc(roomId).update({
      participantIds: [ALICE_UID, BOB_UID],
      participantNames: { [ALICE_UID]: "Alice", [BOB_UID]: "Bob" },
      updatedAt: pastDate(),
    })
  );

  // Non-host Bob CANNOT end Alice's live room
  await assertFails(
    bobDb.collection("live_rooms").doc(roomId).update({
      status: "ended",
      updatedAt: pastDate(),
    })
  );

  // Bob posts a discussion question inside the live room
  await assertSucceeds(
    bobDb.collection("live_rooms").doc(roomId).collection("messages").doc("lmsg_1").set({
      roomId,
      senderId: BOB_UID,
      senderName: "Bob",
      text: "Qual a diferença prática entre LEFT JOIN e INNER JOIN?",
      messageType: "text",
      createdAt: pastDate(),
    })
  );

  // Alice sends a live WebRTC offer signal to Bob; Charlie cannot read it
  await assertSucceeds(
    aliceDb.collection("live_rooms").doc(roomId).collection("signals").doc("sig_1").set({
      fromUid: ALICE_UID,
      toUid: BOB_UID,
      peerPair: [ALICE_UID, BOB_UID],
      type: "offer",
      sdp: "v=0-live-offer",
      createdAt: pastDate(),
    })
  );
  await assertFails(charlieDb.collection("live_rooms").doc(roomId).collection("signals").doc("sig_1").get());
  await assertSucceeds(
    bobDb.collection("live_rooms").doc(roomId).collection("signals").where("peerPair", "array-contains", BOB_UID).get()
  );

  // Host Alice ends the live room
  await assertSucceeds(
    aliceDb.collection("live_rooms").doc(roomId).update({
      status: "ended",
      updatedAt: pastDate(),
    })
  );
});

test("User target subjects preferences: owner can create, update, list, and delete; unauth/cross-user/invalid payloads fail", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const alicePrefs = aliceDb.collection("users").doc(ALICE_UID).collection("preferences");
  const prefDoc = alicePrefs.doc("target_subjects");
  const ts = pastDate();

  // Unauthenticated cannot read or write target subjects preferences
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("preferences").doc("target_subjects").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("preferences").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("preferences").doc("target_subjects").set({
      userId: ALICE_UID,
      subjectIds: ["python", "sql"],
      dailyGoalMinutes: 30,
      learningTrack: "backend_data",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Empty subjectIds fails
  await assertFails(
    prefDoc.set({
      userId: ALICE_UID,
      subjectIds: [],
      dailyGoalMinutes: 30,
      learningTrack: "custom",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Unknown technology ID fails allowlist
  await assertFails(
    prefDoc.set({
      userId: ALICE_UID,
      subjectIds: ["python", "cobol"],
      dailyGoalMinutes: 30,
      learningTrack: "custom",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Ghost field / @DocumentId 'id' fails
  await assertFails(
    prefDoc.set({
      id: "target_subjects",
      userId: ALICE_UID,
      subjectIds: ["python", "sql", "html"],
      dailyGoalMinutes: 30,
      learningTrack: "balanced",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Valid create payload succeeds
  await assertSucceeds(
    prefDoc.set({
      userId: ALICE_UID,
      subjectIds: ["python", "sql", "html"],
      dailyGoalMinutes: 45,
      learningTrack: "custom",
      studyGoalNote: "Focus on Python APIs & SQL joins",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Owner list & get succeed
  await assertSucceeds(alicePrefs.get());
  await assertSucceeds(prefDoc.get());

  // Cross-user Bob cannot read, list, update, or delete Alice's preferences
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("preferences").doc("target_subjects").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("preferences").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("preferences").doc("target_subjects").update({
      subjectIds: ["go"],
      updatedAt: pastDate(),
    })
  );
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("preferences").doc("target_subjects").delete());

  // Owner partial update preserving createdAt & userId succeeds
  await assertSucceeds(
    prefDoc.update({
      subjectIds: ["python", "sql", "html", "kotlin"],
      dailyGoalMinutes: 60,
      learningTrack: "backend_data",
      studyGoalNote: "Preparing for mobile & backend interviews",
      updatedAt: pastDate(),
    })
  );

  // Immutable createdAt mutation fails
  await assertFails(
    prefDoc.update({
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  // Owner delete succeeds
  await assertSucceeds(prefDoc.delete());
});

