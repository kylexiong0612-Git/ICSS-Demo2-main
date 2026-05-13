# Fix Zhipu API Key Environment Configuration

## Problem

The project documentation and security rules require the Zhipu GLM API key to be injected through the server-side `ZHIPU_API_KEY` environment variable. The current backend configuration stores the key directly in `backend/src/main/resources/application.yml`, which conflicts with the documented security constraint.

## Goals

- Remove the hardcoded Zhipu API key from backend configuration.
- Bind `zhipu.api-key` to the `ZHIPU_API_KEY` environment variable.
- Keep the existing backend code paths unchanged, since `AiController` and `HandoffAnalysisService` already read `zhipu.api-key`.
- Preserve existing base URL behavior.

## Non-Goals

- No AI provider change.
- No frontend behavior change.
- No API contract change.
- No database or task lifecycle change.

## Value

This aligns runtime configuration with the project security requirement and prevents model credentials from being committed in source configuration.
