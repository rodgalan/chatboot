# User Story 2: User Registration via Google OAuth

## Goal
As an unauthenticated user, I want to register or sign up using my Google account so that I can quickly access the platform without manually creating a new password.

---

## Requirements

### 1. Google OAuth Authentication Flow
* Integrate standard Google OAuth 2.0 / OpenID Connect (OIDC) flow on the backend.
* Validate the Google ID token returned by the OAuth provider.
* Extract necessary user profile information (email, full name, profile picture/avatar if applicable).

### 2. Account Creation & Immediate Activation
* Upon successful Google authentication, the account is created and set to `Active` immediately (email is pre-verified by Google).
* No two-step email verification flow is triggered for Google registrations.

### 3. Conflict Handling & Account Linking
* **Email Uniqueness:** Check if the Google email address already exists in the system:
    * If the account exists via password registration, return a clear error response or handle provider linking according to security guidelines (e.g., require signing in with credentials first).
    * If the account already exists via Google, proceed as a standard login.

### 4. Role Assignment & Identity
* Every account created via Google self-registration is automatically assigned the **User** role by default.
* User identity and session tokens must include the assigned role claim for downstream authorization.
* **Client-Agnostic Core:** Backend endpoints must handle OAuth token exchanges for both Web and Mobile client applications.

---

## Out of Scope
* **Client-side UI/UX Implementation:** "Sign in with Google" button rendering or client redirect handlers.
* **Email & Password Registration:** Handled in US 1.
* **Additional OAuth Providers:** Social logins other than Google (e.g., Apple, GitHub).
* **Specific Role-Based Permissions (RBAC Details):** Granular access controls per role.