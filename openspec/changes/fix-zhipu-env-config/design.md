# Design

## Before

`backend/src/main/resources/application.yml` defines:

```yaml
zhipu:
  api-key: <hardcoded secret>
  base-url: https://open.bigmodel.cn/api/paas/v4
```

The backend reads the property with:

```java
@Value("${zhipu.api-key}")
private String apiKey;
```

## After

`application.yml` will define:

```yaml
zhipu:
  api-key: ${ZHIPU_API_KEY:}
  base-url: https://open.bigmodel.cn/api/paas/v4
```

Spring Boot resolves `zhipu.api-key` from the process environment variable `ZHIPU_API_KEY`. If the variable is missing, the property resolves to an empty string. Existing controller logic already returns a clear `500` error for blank keys.

## Data Model Changes

None.

## API Changes

None.

## Risk Assessment

- Local AI calls will fail unless `ZHIPU_API_KEY` is exported before starting the backend.
- Existing non-AI fallback paths remain unchanged.
- The hardcoded value will be removed from active backend configuration, but prior Git history may still contain it.

## Rollback Plan

Restore the previous `zhipu.api-key` value in `application.yml` if emergency local demo startup is more important than credential hygiene. This rollback is not recommended for committed code.
