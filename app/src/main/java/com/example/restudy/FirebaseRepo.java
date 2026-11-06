package com.example.restudy;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * All Firebase code lives here so the screens only call simple methods.
 *
 * HOW IT WORKS
 * 1. Login: FirebaseAuth.signInAnonymously() creates an invisible account the first time the app
 *    runs and gives it a user id (uid). Firebase remembers that login on the phone, so the same
 *    uid comes back every launch, until the app is uninstalled or its data is cleared.
 *
 * 2. Where data goes (Firestore):
 *      users/{uid}/sessions/{sessionId}
 *          createdAt : server timestamp (when the "before" check-in was saved)
 *          before    : { stress: 1-5, mood: 1-5, energy: 1-5, savedAt: timestamp }
 *          after     : { stress: 1-5, mood: 1-5, energy: 1-5, savedAt: timestamp }
 *    One "session" = one run of the flow: check-in before -> lockdown -> check-in after.
 *    Everything sits under the user's own uid, so the project's Firestore rules protect it:
 *
 *      match /users/{uid}/{document=**} {
 *        allow read, write: if request.auth != null && request.auth.uid == uid;
 *      }
 *
 *    {document=**} covers every sub-collection, so users/{uid}/sessions/... is allowed.
 *    request.auth is only set after signInAnonymously() succeeds, which is why saveCheckIn()
 *    checks uid() first. Writing to another user's uid would fail with PERMISSION_DENIED.
 *
 * 3. Saving: we use set(..., SetOptions.merge()). "merge" means writing "after" adds that field
 *    to the existing document without erasing "before" (and vice versa).
 *
 * 4. Offline: Firestore keeps a local cache on the phone. If there's no internet, writes are
 *    stored locally and sent automatically once the phone is back online. So we never block the
 *    user waiting for the network; we fire the write and move to the next screen.
 */
public final class FirebaseRepo {

    private static final String TAG = "FirebaseRepo";

    private FirebaseRepo() {}

    /**
     * Call once when the app opens (MainActivity). Does nothing if already signed in.
     * Safe to call again; Firebase won't create a second account.
     */
    public static void ensureSignedIn() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) return;

        auth.signInAnonymously()
                .addOnSuccessListener(r -> Log.d(TAG, "Signed in anonymously: " + r.getUser().getUid()))
                // Usually means no internet or Anonymous sign-in is disabled in the Firebase console
                .addOnFailureListener(e -> Log.e(TAG, "Anonymous sign-in failed", e));
    }

    /** The logged-in user's id, or null if sign-in hasn't finished/failed. */
    @Nullable
    public static String uid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? null : user.getUid();
    }

    /**
     * Makes a new unique id for a session. This works offline: Firestore generates the id on the
     * phone, it doesn't ask the server. Nothing is written yet.
     */
    public static String newSessionId() {
        return FirebaseFirestore.getInstance().collection("sessions").document().getId();
    }

    /**
     * Saves one check-in (the before or after answers) into the session document.
     *
     * @param sessionId id from newSessionId(), passed between screens with an Intent extra
     * @param isAfter   false = "before" field, true = "after" field
     * @param stress    1-5, or 0 if the user didn't pick one (then the value is not saved)
     */
    public static void saveCheckIn(String sessionId, boolean isAfter, int stress, int mood, int energy) {
        String uid = uid();
        if (uid == null) {
            // Not logged in yet (e.g. first launch with no internet). The rules would reject the
            // write anyway, so try to sign in again and skip this one.
            Log.w(TAG, "Not signed in, check-in not saved");
            ensureSignedIn();
            return;
        }

        // Only store answers the user actually picked
        Map<String, Object> answers = new HashMap<>();
        if (stress > 0) answers.put("stress", stress);
        if (mood > 0) answers.put("mood", mood);
        if (energy > 0) answers.put("energy", energy);
        if (answers.isEmpty()) return; // nothing selected, nothing to save
        answers.put("savedAt", FieldValue.serverTimestamp()); // time from Google's server, not the phone

        Map<String, Object> doc = new HashMap<>();
        doc.put(isAfter ? "after" : "before", answers);
        if (!isAfter) doc.put("createdAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("sessions").document(sessionId)
                .set(doc, SetOptions.merge())
                // These run when the SERVER confirms. Offline, they run later (or never if the
                // app is closed first); the data is still queued and will upload.
                .addOnSuccessListener(v -> Log.d(TAG, "Saved " + (isAfter ? "after" : "before")
                        + " check-in for session " + sessionId))
                // PERMISSION_DENIED here = the security rules don't match the path above
                .addOnFailureListener(e -> Log.e(TAG, "Saving check-in failed", e));
    }
}
