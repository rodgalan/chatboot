# User Story 1: User Registration via Email and Password

## Goal
As an unauthenticated user, I want to register an account using my email and password so that I can create a secure profile on the platform and activate it via two-step verification.

## Product Requirements

### 1. Account Creation
* The user provides a valid email address and a secure password.
* **Password Policy:** Enforce minimum security requirements (e.g., minimum length, complexity rules).
* **Email Uniqueness:** The email address must be unique across the system.
* **Default Role:** Every self-registered account is automatically assigned the **User** role.
* The account is created in a `NonValidated` state until email verification is completed.

### 2. Two-Step Email Verification
* Upon successful registration, the system automatically triggers a verification email containing a secure token/link.
* The verification token must have a defined expiration time.
* The account transitions to `Active` state only after the user verifies their email via the link.
* Unverified accounts cannot authenticate or access protected system resources.

### 3. Tech requirements
* **Client-Agnostic Core:** Endpoints and domain logic must be client-agnostic to support both Web and Mobile apps.

## Out of Scope
* **Client-side UI/UX Implementation:** Web and Mobile UI screens or components.
* **Social Logins (OAuth):** Handled in a separate story.
* **Specific Role-Based Permissions (RBAC Details):** Granular access controls per role.
* ** Roles management
* **Password Reset / Forgot Password Flow:** Handled in a separate story.